package com.shrii.app

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object Overlay {
    private var box: LinearLayout? = null
    private var tv: TextView? = null
    private val h = Handler(Looper.getMainLooper())

    fun show(ctx: Context, text: String) = h.post {
        val a = ctx.applicationContext
        if (!Settings.canDrawOverlays(a)) return@post
        if (box == null) {
            tv = TextView(a).apply { setTextColor(Color.WHITE); textSize = 13f; setPadding(28, 12, 28, 12) }
            val stop = Button(a).apply {
                this.text = "STOP"; setTextColor(Color.WHITE); setBackgroundColor(0xFFE5484D.toInt())
                setOnClickListener { Agent.stop = true; hide(a) }
            }
            box = LinearLayout(a).apply {
                gravity = Gravity.CENTER_VERTICAL; setPadding(16, 8, 16, 8)
                background = GradientDrawable().apply { setColor(0xEE0B0D1E.toInt()); cornerRadius = 48f; setStroke(3, 0xFF3B6BFF.toInt()) }
                addView(tv); addView(stop)
            }
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 120 }
            a.getSystemService(WindowManager::class.java).addView(box, lp)
        }
        tv?.text = "✦ $text"
    }

    fun hide(ctx: Context) = h.post {
        box?.let { ctx.applicationContext.getSystemService(WindowManager::class.java).removeView(it) }
        box = null
    }
}
