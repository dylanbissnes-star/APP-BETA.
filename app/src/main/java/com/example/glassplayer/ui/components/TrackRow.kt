package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost
import java.util.Locale

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}

/** Miniatura de portada con icono de respaldo si la cancion no tiene imagen. */
@Composable
fun CoverThumb(artUri: Any?, size: Dp, corner: Dp = 12.dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(listOf(Color(0xFF6C7BFF), Color(0xFF9B5CF6)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.MusicNote, null,
            tint = Color.White.copy(alpha = 0.80f),
            modifier = Modifier.size(size * 0.45f),
        )
        if (artUri != null) {
            AsyncImage(
                model = artUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * Fila de cancion estilo tabla (como "Top 50" de la referencia): numero opcional, portada,
 * titulo y artista, tiempo y corazon de favorito, con una linea fina de separacion.
 * La usan inicio, biblioteca, busqueda, favoritos, artistas y la cola.
 */
@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playing: Boolean = false,
    index: Int? = null,
    favorite: Boolean = false,
    onFavorite: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val line = Frost.Outline
    val accent = Frost.Accent
    Row(
        modifier
            .fillMaxWidth()
            .background(Frost.Bg)
            .pressClick(onClick, pressedScale = 0.985f)
            .drawBehind {
                drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1f)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (index != null) {
            Text(
                String.format(Locale.US, "%02d", index),
                color = Frost.TextTertiary,
                fontSize = 13.sp,
                modifier = Modifier.width(30.dp),
            )
        }
        CoverThumb(track.artUri, 46.dp, corner = 12.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isCurrent) accent else Frost.TextPrimary,
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
        if (trailing != null) {
            trailing()
        } else {
            if (isCurrent) {
                EqBars(active = playing, color = accent, modifier = Modifier.padding(end = 8.dp))
            } else if (track.durationMs > 0) {
                Text(
                    formatTime(track.durationMs),
                    color = Frost.TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            if (onFavorite != null) {
                PlainIconButton(
                    if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    if (favorite) "Quitar de favoritos" else "Añadir a favoritos",
                    onFavorite,
                    size = 38.dp, iconSize = 20.dp,
                    tint = if (favorite) accent else Frost.TextTertiary,
                )
            }
        }
    }
}
