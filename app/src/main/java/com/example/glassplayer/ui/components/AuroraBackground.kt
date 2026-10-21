package com.example.glassplayer.ui.components

import android.graphics.Bitmap
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.glassplayer.ui.theme.GlassBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

val DefaultAurora = listOf(Color(0xFFE11D1D), Color(0xFFF5B82E), Color(0xFF7A1010))

/** Extrae 3 colores de la portada para pintar el fondo. */
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

/** Fondo difuso y en movimiento lento, tipo "aurora". */
@Composable
fun AuroraBackground(colors: List<Color>, modifier: Modifier = Modifier) {
    val c1 by animateColorAsState(colors[0], tween(1200), label = "c1")
    val c2 by animateColorAsState(colors[1], tween(1200), label = "c2")
    val c3 by animateColorAsState(colors[2], tween(1200), label = "c3")

    val t = rememberInfiniteTransition(label = "aurora")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )

    Box(modifier.fillMaxSize().background(GlassBase)) {
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

            blob(c1, w * (0.30f + 0.10f * cos(phase)), h * (0.38f + 0.06f * sin(phase)), w * 0.85f, 0.95f)
            blob(c2, w * (0.78f + 0.08f * sin(phase * 1.3f)), h * (0.32f + 0.05f * cos(phase)), w * 0.65f, 0.85f)
            blob(c3, w * (0.45f + 0.12f * sin(phase * 0.8f)), h * (0.78f + 0.05f * cos(phase * 1.1f)), w * 0.90f, 0.80f)
            blob(c1, w * (0.85f + 0.05f * cos(phase)), h * 0.75f, w * 0.50f, 0.45f)

            // Viñeta oscura para que el vidrio resalte
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent, Color.Black.copy(alpha = 0.60f))
                )
            )
        }
    }
}
