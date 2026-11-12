package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
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
