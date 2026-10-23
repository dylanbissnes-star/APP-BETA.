package com.example.glassplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Fondo de la app:
 *  - por defecto, blanco (o casi negro en modo oscuro)
 *  - cuando hay una cancion elegida (`showArt`), su portada ocupa toda la pantalla, muy
 *    desenfocada y con un velo oscuro para que el texto se lea; cambia con un fundido
 * El desenfoque es real en Android 12+; antes, la portada pequena ampliada ya se ve suave.
 */
@Composable
fun PageBackground(
    dark: Boolean,
    art: Any?,
    showArt: Boolean,
    modifier: Modifier = Modifier,
) {
    val base by animateColorAsState(
        if (dark) Color(0xFF0E0E10) else Color(0xFFF7F7F8),
        tween(400), label = "pageBase",
    )
    val context = LocalContext.current

    Box(modifier.fillMaxSize().background(base)) {
        Crossfade(
            targetState = if (showArt) art else null,
            animationSpec = tween(700),
            label = "pageArt",
        ) { current ->
            if (current != null) {
                val request = remember(current) {
                    ImageRequest.Builder(context).data(current).size(96).build()
                }
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = request,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(48.dp),
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f)))
                }
            }
        }
    }
}
