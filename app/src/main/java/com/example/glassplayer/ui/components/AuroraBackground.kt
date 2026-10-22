package com.example.glassplayer.ui.components

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.glassplayer.ui.theme.GlassBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

/** Colores de ambiente cuando no hay portada: grises azulados muy sobrios. */
val DefaultAurora = listOf(Color(0xFF3C3C4A), Color(0xFF5C5C6C), Color(0xFF22222C))

/** Extrae 3 colores de la portada para teñir (muy suave) el ambiente. */
@Composable
fun rememberCoverPalette(art: Any?): List<Color> {
    val context = LocalContext.current
    var colors by remember { mutableStateOf(DefaultAurora) }
    LaunchedEffect(art) {
        if (art == null) {
            colors = DefaultAurora
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(art)
            .allowHardware(false)
            .size(128)
            .build()
        val result = context.imageLoader.execute(request) as? SuccessResult ?: return@LaunchedEffect
        val bitmap: Bitmap = result.drawable.toBitmap()
        val palette = withContext(Dispatchers.Default) { Palette.from(bitmap).generate() }
        val fallback = DefaultAurora
        colors = listOf(
            Color(palette.getVibrantColor(palette.getDominantColor(fallback[0].toArgb()))),
            Color(palette.getLightVibrantColor(palette.getMutedColor(fallback[1].toArgb()))),
            Color(palette.getDarkVibrantColor(palette.getDarkMutedColor(fallback[2].toArgb()))),
        )
    }
    return colors
}

/**
 * Fondo NEGRO con degradado ambiental:
 *  - la portada actual, muy difuminada y apagada, como luz de ambiente (Android 12+)
 *  - manchas suaves con los colores de la portada, en movimiento lento
 *  - un haz de luz tenue desde la esquina superior derecha
 *  - vineta oscura arriba y abajo
 */
@Composable
fun AuroraBackground(colors: List<Color>, art: Any?, modifier: Modifier = Modifier) {
    val c1 by animateColorAsState(colors[0], tween(1400), label = "c1")
    val c2 by animateColorAsState(colors[1], tween(1400), label = "c2")
    val c3 by animateColorAsState(colors[2], tween(1400), label = "c3")

    val t = rememberInfiniteTransition(label = "ambient")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )

    Box(modifier.fillMaxSize().background(GlassBase)) {
        // Portada difuminada como luz de ambiente
        if (Build.VERSION.SDK_INT >= 31) {
            Crossfade(targetState = art, animationSpec = tween(900), label = "artBackdrop") { current ->
                if (current != null) {
                    AsyncImage(
                        model = current,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(90.dp)
                            .alpha(0.30f),
                    )
                }
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            fun blob(color: Color, cx: Float, cy: Float, r: Float, alpha: Float) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )
            }

            // Tintes de la portada (suaves, para que predomine el negro)
            blob(c1, w * (0.25f + 0.10f * cos(phase)), h * (0.42f + 0.05f * sin(phase)), w * 0.90f, 0.30f)
            blob(c2, w * (0.80f + 0.08f * sin(phase * 1.3f)), h * (0.55f + 0.05f * cos(phase)), w * 0.70f, 0.16f)
            blob(c3, w * (0.45f + 0.12f * sin(phase * 0.8f)), h * (0.90f + 0.03f * cos(phase * 1.1f)), w * 0.95f, 0.22f)

            // Haz de luz tenue desde arriba a la derecha (como en la referencia)
            blob(Color.White, w * 0.95f, -h * 0.03f, w * 1.25f, 0.11f + 0.025f * sin(phase))

            // Vineta: negro profundo arriba y abajo
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.45f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.70f),
                    )
                )
            )
        }
    }
}
