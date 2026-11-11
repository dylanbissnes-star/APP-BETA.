package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost

/**
 * Titulo grande y artista alineados a la izquierda (como la referencia) y el corazon de favorito a
 * la derecha. Cambia con un fundido y subida suave al cambiar de cancion.
 */
@Composable
fun NowPlayingInfo(
    track: Track?,
    favorite: Boolean,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = track,
            contentKey = { it?.id },
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (fadeIn(tween(340)) + slideInVertically(tween(340)) { it / 5 }) togetherWith fadeOut(tween(160))
            },
            label = "nowPlayingInfo",
        ) { t ->
            Column(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                Text(
                    t?.title ?: "—",
                    color = Frost.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOf(t?.artist.orEmpty(), t?.album.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
                    color = Frost.TextSecondary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (track != null) {
            RoundButton(
                if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                if (favorite) "Quitar de favoritos" else "Añadir a favoritos",
                onFavorite,
                size = 44.dp, iconSize = 22.dp,
                active = favorite,
            )
        }
    }
}
