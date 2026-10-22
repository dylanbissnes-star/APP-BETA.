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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.glassplayer.ui.theme.Frost
import com.example.glassplayer.ui.theme.vividOr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

/**
 * 5 colores de ambiente cuando no hay portada (azul-violeta-rosa como la referencia):
 * [0] cian, [1] violeta, [2] rosa, [3] azul, [4] lima.
 */
val DefaultAurora = listOf(
    Color(0xFF38C6E8),
    Color(0xFF9B5CF6),
    Color(0xFFFF5C9A),
    Color(0xFF4C6FFF),
    Color(0xFFE4F06A),
)

/** Extrae hasta 5 colores vivos de la portada; los que falten se rellenan con los de defecto. */
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
        val raw = listOf(
            palette.vibrantSwatch?.rgb,
            palette.lightVibrantSwatch?.rgb,
            palette.darkVibrantSwatch?.rgb,
            palette.mutedSwatch?.rgb,
            palette.lightMutedSwatch?.rgb,
        )
        colors = raw.mapIndexed { i, rgb ->
            if (rgb == null) DefaultAurora[i] else Color(rgb).vividOr(DefaultAurora[i])
        }
    }
    return colors
}

private class Sphere(val x: Float, val y: Float, val r: Float, val color: Int, val phase: Float)

private val Spheres = listOf(
    Sphere(0.02f, 0.20f, 0.40f, 0, 0.0f),
    Sphere(0.98f, 0.07f, 0.30f, 4, 1.7f),
    Sphere(0.15f, 0.92f, 0.46f, 2, 3.1f),
    Sphere(0.92f, 0.78f, 0.44f, 1, 4.4f),
    Sphere(0.90f, 0.42f, 0.12f, 2, 2.2f),
    Sphere(0.12f, 0.56f, 0.10f, 2, 5.0f),
    Sphere(0.58f, 0.99f, 0.20f, 3, 0.9f),
)

/**
 * Fondo de color con degradado diagonal y esferas brillantes que flotan despacio
 * (como los globos de la referencia). Los colores salen de la portada que suena.
 * Los paneles de cristal desenfocan esto, que es lo que da el efecto "esmerilado".
 */
@Composable
fun SphereBackground(colors: List<Color>, modifier: Modifier = Modifier) {
    val c0 by animateColorAsState(colors[0], tween(1400), label = "s0")
    val c1 by animateColorAsState(colors[1], tween(1400), label = "s1")
    val c2 by animateColorAsState(colors[2], tween(1400), label = "s2")
    val c3 by animateColorAsState(colors[3], tween(1400), label = "s3")
    val c4 by animateColorAsState(colors[4], tween(1400), label = "s4")

    val t = rememberInfiniteTransition(label = "float")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cols = listOf(c0, c1, c2, c3, c4)

        // Degradado base en diagonal: azul -> violeta -> rosa
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    lerp(c3, Frost.Tint, 0.20f),
                    lerp(c1, Frost.Tint, 0.15f),
                    lerp(c2, Frost.Tint, 0.10f),
                ),
                start = Offset.Zero,
                end = Offset(w, h),
            )
        )

        for (s in Spheres) {
            val cx = w * s.x + w * 0.012f * sin(phase + s.phase)
            val cy = h * s.y + w * 0.015f * cos(phase * 1.2f + s.phase)
            val r = w * s.r
            val col = cols[s.color]

            // Cuerpo: luz arriba a la izquierda, sombra abajo a la derecha
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lerp(col, Color.White, 0.45f),
                        col,
                        lerp(col, Frost.Tint, 0.55f),
                    ),
                    center = Offset(cx - r * 0.35f, cy - r * 0.40f),
                    radius = r * 1.45f,
                ),
                radius = r,
                center = Offset(cx, cy),
            )
            // Brillo especular
            val pivot = Offset(cx - r * 0.50f, cy - r * 0.55f)
            rotate(-32f, pivot) {
                drawOval(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(pivot.x - r * 0.11f, pivot.y - r * 0.05f),
                    size = Size(r * 0.22f, r * 0.10f),
                )
            }
        }

        // Neblina clara arriba a la izquierda (luz de ventana)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(w * 0.15f, 0f),
                radius = w * 0.9f,
            ),
            radius = w * 0.9f,
            center = Offset(w * 0.15f, 0f),
        )
    }
}
