package com.example.glassplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.glassplayer.ui.theme.vividOr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 5 colores de respaldo cuando no hay portada: [0] cian, [1] violeta, [2] rosa, [3] azul, [4] lima. */
val DefaultAurora = listOf(
    Color(0xFF38C6E8),
    Color(0xFF9B5CF6),
    Color(0xFFFF5C9A),
    Color(0xFF4C6FFF),
    Color(0xFFE4F06A),
)

/** Extrae hasta 5 colores vivos de la portada; los que falten se rellenan con los de respaldo. */
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
