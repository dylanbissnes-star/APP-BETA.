package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Cristal translucido sobre fondo negro:
 *  - base oscura + velo claro muy tenue (se ve el ambiente detras)
 *  - borde suave con brillo en esquinas opuestas
 *  - reflejo diagonal (como luz sobre el vidrio)
 *
 * Nota: Compose no trae "backdrop blur" nativo; el efecto se logra con translucidez,
 * borde y reflejo sobre un fondo ya difuso (ver README para blur real con Haze).
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color = Color.White,
    strength: Float = 1f,
): Modifier = this
    .clip(shape)
    .background(Color.Black.copy(alpha = 0.30f))
    .background(
        Brush.verticalGradient(
            listOf(
                tint.copy(alpha = 0.11f * strength),
                tint.copy(alpha = 0.035f * strength),
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.42f),
                Color.White.copy(alpha = 0.04f),
                Color.White.copy(alpha = 0.20f),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
    .drawWithContent {
        drawContent()
        // Reflejo diagonal en la esquina superior izquierda
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.10f * strength.coerceAtMost(1.5f)), Color.Transparent),
                start = Offset.Zero,
                end = Offset(size.width * 0.85f, size.height * 0.65f),
            )
        )
    }

/**
 * Vidrio NEGRO para botones flotantes: relleno casi negro, brillo suave arriba
 * y borde con degradado (claro en esquinas opuestas, apagado en medio).
 */
fun Modifier.blackGlass(shape: Shape): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(Color(0xEE1C1C23), Color(0xF4040405))
        )
    )
    .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.08f),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                endY = size.height * 0.5f,
            )
        )
    }
