package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost

/**
 * Mini reproductor (tarjeta oscura con portada, titulo, corazon y pausa, como el de la referencia),
 * con una linea fina de progreso abajo. Al tocarlo se abre el reproductor.
 */
@Composable
fun MiniPlayer(
    track: Track,
    playing: Boolean,
    progress: Float,
    favorite: Boolean,
    onToggle: () -> Unit,
    onFavorite: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val accent = Frost.Accent
    Column(
        modifier
            .fillMaxWidth()
            .pressClick(onOpen, pressedScale = 0.985f)
            .shadow(14.dp, shape, ambientColor = Frost.Tint, spotColor = Frost.Tint)
            .card(shape, raised = true),
    ) {
        Row(
            Modifier.padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverThumb(track.artUri, 44.dp, corner = 12.dp)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Frost.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Frost.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlainIconButton(
                if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                if (favorite) "Quitar de favoritos" else "Añadir a favoritos",
                onFavorite, size = 42.dp, iconSize = 22.dp,
                tint = if (favorite) accent else Frost.TextSecondary,
            )
            PlainIconButton(
                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                if (playing) "Pausar" else "Reproducir",
                onToggle, size = 44.dp, iconSize = 30.dp,
                tint = accent,
            )
        }
        // Linea de progreso
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Frost.Outline)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(accent)
            )
        }
    }
}
