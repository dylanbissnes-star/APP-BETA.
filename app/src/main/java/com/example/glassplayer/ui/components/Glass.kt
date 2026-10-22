package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/**
 * Cristal esmerilado (frosted glass):
 *  - desenfoque real del fondo si se pasa `backdrop` (Android 12+)
 *  - velo blanco translucido con degradado vertical + un tinte indigo para dar contraste
 *  - borde degradado (brillante en esquinas opuestas), reflejo diagonal e hilo de luz arriba
 *
 * `strength` sube/baja la presencia del velo; `darkness` la opacidad del tinte oscuro.
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color = Color.White,
    strength: Float = 1f,
    darkness: Float = 0.08f,
    backdrop: BackdropState? = null,
): Modifier {
    val s = strength.coerceIn(0.4f, 3f)
    return this
        .clip(shape)
        .then(if (backdrop != null) Modifier.backdropBlur(backdrop) else Modifier)
        .background(Frost.Tint.copy(alpha = darkness.coerceIn(0f, 0.9f)))
        .background(
            Brush.verticalGradient(
                listOf(
                    tint.copy(alpha = (0.24f * s).coerceAtMost(0.8f)),
                    tint.copy(alpha = (0.11f * s).coerceAtMost(0.7f)),
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.40f),
                ),
                start = Offset.Zero,
                end = Offset.Infinite,
            ),
            shape = shape,
        )
        .drawWithContent {
            drawContent()
            // Reflejo diagonal suave
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                    start = Offset.Zero,
                    end = Offset(size.width * 0.7f, size.height * 0.7f),
                )
            )
            // Hilo de luz en el borde superior
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.75f), Color.Transparent),
                ),
                size = Size(size.width, 1.dp.toPx()),
            )
        }
}

/**
 * Cristal mas luminoso para botones y elementos activos (chips seleccionados, avisos, botones).
 * `boost` lo hace aun mas claro.
 */
fun Modifier.glassStrong(shape: Shape, boost: Float = 0f): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = (0.40f + boost).coerceAtMost(0.85f)),
                Color.White.copy(alpha = (0.20f + boost).coerceAtMost(0.7f)),
            )
        )
    )
    .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.60f),
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
                colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                endY = size.height * 0.5f,
            )
        )
    }
