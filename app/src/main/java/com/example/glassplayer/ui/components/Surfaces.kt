package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/**
 * Tarjeta del tema. En oscuro es vidrio: relleno blanco translucido con un contorno fino claro
 * (sobre el degradado se ve esmerilado). En claro es una tarjeta blanca con sombra suave.
 * `raised` usa el tono elevado (botones, campos, pildoras, elementos activos).
 */
fun Modifier.card(shape: Shape, raised: Boolean = false, outlined: Boolean = true): Modifier = composed {
    val fill = if (raised) Frost.SurfaceHigh else Frost.Surface
    val line = Frost.Outline
    val soft = !Frost.OnDark
    this
        .then(
            if (soft && !raised) Modifier.shadow(6.dp, shape, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            else Modifier
        )
        .clip(shape)
        .background(fill)
        .then(if (outlined && !soft) Modifier.border(1.dp, line, shape) else Modifier)
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
