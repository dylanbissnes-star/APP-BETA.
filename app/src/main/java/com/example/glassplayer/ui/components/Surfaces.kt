package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/**
 * Tarjeta solida: relleno del tema con un contorno fino.
 * `raised` usa el tono elevado (botones, campos, pildoras, elementos activos).
 */
fun Modifier.card(shape: Shape, raised: Boolean = false, outlined: Boolean = true): Modifier = composed {
    val fill = if (raised) Frost.SurfaceHigh else Frost.Surface
    val line = Frost.Outline
    this
        .clip(shape)
        .background(fill)
        .then(if (outlined) Modifier.border(1.dp, line, shape) else Modifier)
}
