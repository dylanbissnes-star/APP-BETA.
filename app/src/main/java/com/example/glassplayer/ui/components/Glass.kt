package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/**
 * Cristal esmerilado que se adapta a la superficie:
 *  - sobre superficie CLARA: panel blanco casi opaco con borde gris fino y una sombra suave
 *  - sobre superficie OSCURA (modo oscuro o portada desenfocada): velo blanco translucido con
 *    borde degradado, reflejo diagonal e hilo de luz arriba
 * Con `backdrop` desenfoca lo que hay detras (Android 12+).
 * `strength` sube/baja la presencia del velo; `darkness` oscurece la base (solo en superficie oscura).
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color = Color.White,
    strength: Float = 1f,
    darkness: Float = 0.08f,
    backdrop: BackdropState? = null,
): Modifier = composed {
    val onDark = Frost.OnDark
    val s = strength.coerceIn(0.4f, 3f)
    val base = if (onDark) Modifier else Modifier.shadow(
        elevation = 5.dp,
        shape = shape,
        ambientColor = Color(0x33000000),
        spotColor = Color(0x33000000),
    )
    base
        .clip(shape)
        .then(if (backdrop != null) Modifier.backdropBlur(backdrop) else Modifier)
        .background(Frost.Tint.copy(alpha = if (onDark) darkness.coerceIn(0f, 0.9f) else 0f))
        .background(
            if (onDark) {
                Brush.verticalGradient(
                    listOf(
                        tint.copy(alpha = (0.24f * s).coerceAtMost(0.8f)),
                        tint.copy(alpha = (0.11f * s).coerceAtMost(0.7f)),
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = (0.80f + 0.06f * s).coerceAtMost(0.97f)),
                        Color.White.copy(alpha = (0.60f + 0.08f * s).coerceAtMost(0.92f)),
                    )
                )
            }
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = if (onDark) listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.40f),
                ) else listOf(
                    Color.Black.copy(alpha = 0.12f),
                    Color.Black.copy(alpha = 0.04f),
                    Color.Black.copy(alpha = 0.09f),
                ),
                start = Offset.Zero,
                end = Offset.Infinite,
            ),
            shape = shape,
        )
        .drawWithContent {
            drawContent()
            if (onDark) {
                // Reflejo diagonal suave
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                        start = Offset.Zero,
                        end = Offset(size.width * 0.7f, size.height * 0.7f),
                    )
                )
            }
            // Hilo de luz en el borde superior
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (onDark) 0.75f else 0.95f),
                        Color.Transparent,
                    ),
                ),
                size = Size(size.width, 1.dp.toPx()),
            )
        }
}

/**
 * Cristal mas luminoso para botones y elementos activos (chips seleccionados, avisos, botones).
 * `boost` lo hace aun mas claro / marcado.
 */
fun Modifier.glassStrong(shape: Shape, boost: Float = 0f): Modifier = composed {
    val onDark = Frost.OnDark
    this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                if (onDark) listOf(
                    Color.White.copy(alpha = (0.40f + boost).coerceAtMost(0.85f)),
                    Color.White.copy(alpha = (0.20f + boost).coerceAtMost(0.7f)),
                ) else listOf(
                    Color.White,
                    Color(0xFFEDEDF0).copy(alpha = 1f),
                )
            )
        )
        .border(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = if (onDark) listOf(
                    Color.White.copy(alpha = 0.90f),
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.60f),
                ) else listOf(
                    Color.Black.copy(alpha = (0.22f + boost).coerceAtMost(0.5f)),
                    Color.Black.copy(alpha = 0.08f),
                    Color.Black.copy(alpha = (0.18f + boost).coerceAtMost(0.45f)),
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
                    colors = listOf(Color.White.copy(alpha = if (onDark) 0.18f else 0.6f), Color.Transparent),
                    endY = size.height * 0.5f,
                )
            )
        }
}
