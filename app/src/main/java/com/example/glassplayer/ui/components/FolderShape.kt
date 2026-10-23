package com.example.glassplayer.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Silueta del frente de una carpeta: esquinas redondeadas y una "pestana" a la izquierda
 * que baja con una curva suave hasta el borde superior derecho (que queda un poco mas bajo).
 */
class FolderShape(
    private val corner: Dp = 26.dp,
    private val tabFraction: Float = 0.52f,
    private val dropFraction: Float = 0.085f,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { corner.toPx() }
        val w = size.width
        val h = size.height
        val dy = h * dropFraction
        val tabEnd = w * tabFraction
        val c = h * 0.10f

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(tabEnd - c, 0f)
            cubicTo(tabEnd + c * 0.2f, 0f, tabEnd - c * 0.2f, dy, tabEnd + c, dy)
            lineTo(w - r, dy)
            quadraticBezierTo(w, dy, w, dy + r)
            lineTo(w, h - r)
            quadraticBezierTo(w, h, w - r, h)
            lineTo(r, h)
            quadraticBezierTo(0f, h, 0f, h - r)
            lineTo(0f, r)
            quadraticBezierTo(0f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
