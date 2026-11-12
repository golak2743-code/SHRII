package com.shrii.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

val Ink = Color(0xFF05060F)
val Saffron = Color(0xFFFF9933)
val Magenta = Color(0xFFE6399B)
val Blue = Color(0xFF3B6BFF)
val Cyan = Color(0xFF22D3EE)

@Composable
fun Orb(state: String, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "orb")
    val spin = if (state == "acting") 2500 else if (state == "thinking") 4500 else 12000
    val beat = if (state == "listening") 450 else if (state == "speaking") 700 else 2400
    val rot by t.animateFloat(0f, 360f, infiniteRepeatable(tween(spin, easing = LinearEasing)), label = "r")
    val pulse by t.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(beat, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "p")
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2 * 0.55f * pulse
        drawCircle(Brush.radialGradient(listOf(Magenta.copy(alpha = 0.35f), Color.Transparent), c, r * 1.9f), r * 1.9f, c)
        rotate(rot, c) {
            drawCircle(Brush.sweepGradient(listOf(Saffron, Magenta, Blue, Cyan, Saffron), c), r, c, style = Stroke(r * 0.13f))
        }
        drawCircle(Brush.radialGradient(listOf(Color(0xFF1B1F3A), Ink), c, r), r * 0.84f, c)
    }
}
