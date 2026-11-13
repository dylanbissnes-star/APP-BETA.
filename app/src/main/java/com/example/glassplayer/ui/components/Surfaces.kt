package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/**
 * Tarjeta de vidrio: relleno translucido, contorno fino con degradado (mas claro arriba, como el
 * reflejo de un borde de cristal) y sin sombra dura. `raised` usa el tono mas claro.
 */
fun Modifier.card(shape: Shape, raised: Boolean = false, outlined: Boolean = true): Modifier = composed {
    val fill = if (raised) Frost.SurfaceHigh else Frost.Surface
    val dark = Frost.OnDark
    val edge = Brush.verticalGradient(
        if (dark) listOf(Color.White.copy(alpha = 0.26f), Color.White.copy(alpha = 0.05f))
        else listOf(Color.White, Color.White.copy(alpha = 0.35f))
    )
    this
        .clip(shape)
        .background(fill)
        .then(if (outlined) Modifier.border(1.dp, edge, shape) else Modifier)
}

/** Cuadricula muy tenue de fondo (como el fondo de la referencia neon). */
fun Modifier.gridBackdrop(color: Color, step: Dp = 26.dp): Modifier = drawWithCache {
    val stepPx = step.toPx()
    onDrawBehind {
        var x = 0f
        while (x < size.width) {
            drawLine(color, Offset(x, 0f), Offset(x, size.height), 1f)
            x += stepPx
        }
        var y = 0f
        while (y < size.height) {
            drawLine(color, Offset(0f, y), Offset(size.width, y), 1f)
            y += stepPx
        }
    }
}
