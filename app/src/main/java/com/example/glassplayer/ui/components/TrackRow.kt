package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.glassplayer.data.Track
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
            .background(Brush.linearGradient(listOf(Color(0xFF2A2A2A), Color(0xFF0C0C0C)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.MusicNote, null,
            tint = Color.White.copy(alpha = 0.45f),
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

/** Fila de cancion en cristal. La usan la biblioteca y la cola. */
@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playing: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.97f)
            .glass(shape, strength = if (isCurrent) 1.6f else 0.8f)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverThumb(track.artUri, 52.dp, corner = 14.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.60f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isCurrent) {
            EqBars(active = playing, modifier = Modifier.padding(end = 6.dp))
        } else if (trailing == null && track.durationMs > 0) {
            Text(
                formatTime(track.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.50f),
                modifier = Modifier.padding(end = 6.dp),
            )
        }
        if (trailing != null) {
            Box(Modifier.padding(start = 8.dp)) { trailing() }
        }
    }
}
