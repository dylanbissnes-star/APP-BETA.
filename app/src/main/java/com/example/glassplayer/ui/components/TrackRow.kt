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
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
            .background(Brush.linearGradient(listOf(Color(0xFFB69CFF), Color(0xFF6C7BFF)))),
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
 * Fila de cancion en cristal (portada, titulo, artista y un triangulo de "reproducir" a la derecha,
 * como en la referencia). La usan la biblioteca, la busqueda y la cola.
 */
@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playing: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.97f)
            .glass(shape, strength = if (isCurrent) 1.7f else 0.8f, darkness = 0.04f)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverThumb(track.artUri, 50.dp, corner = 14.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
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
            Box(Modifier.padding(start = 8.dp)) { trailing() }
        } else if (isCurrent) {
            EqBars(active = playing, modifier = Modifier.padding(end = 10.dp))
        } else {
            Icon(
                Icons.Rounded.PlayArrow, "Reproducir",
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(26.dp),
            )
        }
    }
}
