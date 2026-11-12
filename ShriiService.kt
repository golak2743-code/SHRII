package com.shrii.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShriiService : AccessibilityService() {
    companion object { var inst: ShriiService? = null }
    override fun onServiceConnected() { inst = this }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { inst = null; super.onDestroy() }

    fun dump(): String {
        val sb = StringBuilder()
        fun walk(n: AccessibilityNodeInfo?, d: Int) {
            if (n == null || d > 25) return
            val t = n.text ?: n.contentDescription
            if (t != null || n.isClickable || n.isEditable) {
                val r = android.graphics.Rect(); n.getBoundsInScreen(r)
                sb.append("${n.className?.toString()?.substringAfterLast('.')} \"${t ?: ""}\" @${r.centerX()},${r.centerY()}")
                if (n.isClickable) sb.append(" click"); if (n.isEditable) sb.append(" edit"); sb.append('\n')
            }
            for (i in 0 until n.childCount) walk(n.getChild(i), d + 1)
        }
        walk(rootInActiveWindow, 0)
        return sb.toString().take(6000).ifBlank { "(empty screen)" }
    }

    fun clickText(s: String): Boolean {
        val root = rootInActiveWindow ?: return false
        for (n in root.findAccessibilityNodeInfosByText(s)) {
            var c: AccessibilityNodeInfo? = n
            while (c != null && !c.isClickable) c = c.parent
            if (c?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) return true
        }
        return false
    }

    fun typeText(s: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val f = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: findEdit(root) ?: return false
        val b = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, s) }
        return f.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, b)
    }

    private fun findEdit(n: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (n.isEditable) return n
        for (i in 0 until n.childCount) n.getChild(i)?.let { findEdit(it)?.let { r -> return r } }
        return null
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, ms: Long = 300) {
        val p = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(p, 0, ms)).build(), null, null)
    }
    fun tap(x: Float, y: Float) = swipe(x, y, x, y, 50)
}
