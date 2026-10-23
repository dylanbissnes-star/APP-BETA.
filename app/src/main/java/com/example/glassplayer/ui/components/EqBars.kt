package com.example.glassplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/** Tres barras que "bailan" cuando la cancion suena (indicador de reproduccion). */
@Composable
fun EqBars(active: Boolean, modifier: Modifier = Modifier, color: Color = Frost.TextPrimary) {
    val transition = rememberInfiniteTransition(label = "eq")
    val a by transition.animateFloat(
        0.30f, 1f,
        infiniteRepeatable(tween(520, easing = LinearEasing), RepeatMode.Reverse), label = "a",
    )
    val b by transition.animateFloat(
        0.40f, 1f,
        infiniteRepeatable(tween(430, easing = LinearEasing), RepeatMode.Reverse), label = "b",
    )
    val c by transition.animateFloat(
        0.25f, 1f,
        infiniteRepeatable(tween(610, easing = LinearEasing), RepeatMode.Reverse), label = "c",
    )
    Canvas(modifier.size(18.dp)) {
        val barWidth = size.width / 5f
        val heights = if (active) listOf(a, b, c) else listOf(0.35f, 0.35f, 0.35f)
        heights.forEachIndexed { i, h ->
            val barHeight = size.height * h
            drawRoundRect(
                color = color,
                topLeft = Offset(i * 2 * barWidth, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}
