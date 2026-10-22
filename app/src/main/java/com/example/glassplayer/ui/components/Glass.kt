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
import com.example.glassplayer.ui.theme.BlackGlass

/**
 * Cristal ahumado (monocromatico):
 *  - base negra translucida + velo blanco muy tenue (rgba 255,255,255,0.045)
 *  - desenfoque real del fondo si se pasa `backdrop` (Android 12+)
 *  - borde blanco sutil y reflejo discreto en el borde superior
 *
 * `strength` sube/baja la presencia del velo; `darkness` la opacidad del negro de base.
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color = Color.White,
    strength: Float = 1f,
    darkness: Float = 0.42f,
    backdrop: BackdropState? = null,
): Modifier = this
    .clip(shape)
    .then(if (backdrop != null) Modifier.backdropBlur(backdrop) else Modifier)
    .background(Color.Black.copy(alpha = darkness))
    .background(tint.copy(alpha = 0.045f * strength.coerceIn(0.5f, 3f)))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.26f),
                Color.White.copy(alpha = 0.05f),
                BlackGlass.GlassBorder,
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
    .drawWithContent {
        drawContent()
        // Reflejo suave en la parte alta
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                endY = size.height * 0.40f,
            )
        )
        // Hilo de luz en el borde superior
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, BlackGlass.Shine, Color.Transparent),
            ),
            size = Size(size.width, 1.dp.toPx()),
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
            listOf(Color(0xF2141414), Color(0xF8050505))
        )
    )
    .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.34f),
                Color.White.copy(alpha = 0.06f),
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
                colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                endY = size.height * 0.5f,
            )
        )
    }
