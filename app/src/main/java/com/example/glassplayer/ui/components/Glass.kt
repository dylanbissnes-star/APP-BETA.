package com.example.glassplayer.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp

/**
 * Efecto "vidrio": relleno translucido con degradado, borde con brillo en las esquinas
 * y un reflejo suave arriba. Se ve bien sobre el fondo aurora.
 *
 * Nota: Compose no trae "backdrop blur" nativo. Como el fondo ya es difuso,
 * el efecto se consigue con translucidez + borde. Para blur real del fondo
 * puedes integrar la libreria Haze (ver README).
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color = Color.White,
    strength: Float = 1f,
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(
                tint.copy(alpha = 0.26f * strength),
                tint.copy(alpha = 0.10f * strength),
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = 0.30f),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
    .drawWithContent {
        drawContent()
        // Reflejo superior
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                endY = size.height * 0.45f,
            )
        )
    }

/**
 * Vidrio NEGRO para botones flotantes: relleno casi negro y borde con degradado
 * (brillante en las esquinas opuestas, apagado en medio).
 */
fun Modifier.blackGlass(shape: Shape): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(Color(0xEE1A1A21), Color(0xF2050507))
        )
    )
    .border(
        width = 1.5.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.10f),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
