package com.example.glassplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.glassplayer.R
import com.example.glassplayer.ui.theme.Frost

/** Opciones de fondo (se eligen en Ajustes). */
object WallpaperChoice {
    const val SILK = 0
    const val MIST = 1
    const val COVER = 2
}

private data class Source(val choice: Int, val art: Any?)

/**
 * Fondo de toda la app: el cristal se ve sobre el. Tela negra, bruma de atardecer o la portada
 * de la cancion actual muy desenfocada (si aun no hay portada, tela negra). Lleva un velo del
 * tema para que el texto se lea, y cambia con un fundido.
 */
@Composable
fun Wallpaper(choice: Int, art: Any?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val source = if (choice == WallpaperChoice.COVER && art != null) Source(WallpaperChoice.COVER, art)
    else Source(if (choice == WallpaperChoice.MIST) WallpaperChoice.MIST else WallpaperChoice.SILK, null)

    Box(modifier.fillMaxSize().background(Frost.Bg)) {
        Crossfade(targetState = source, animationSpec = tween(700), label = "wallpaper") { s ->
            when (s.choice) {
                WallpaperChoice.COVER -> {
                    val request = remember(s.art) { ImageRequest.Builder(context).data(s.art).size(96).build() }
                    AsyncImage(
                        model = request,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().blur(40.dp),
                    )
                }
                WallpaperChoice.MIST -> Image(
                    painterResource(R.drawable.wallpaper_bruma), null,
                    Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                )
                else -> Image(
                    painterResource(R.drawable.wallpaper_seda), null,
                    Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                )
            }
        }
        Box(Modifier.fillMaxSize().background(Frost.Scrim))
    }
}
