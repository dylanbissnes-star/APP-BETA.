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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
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
            .background(Brush.linearGradient(listOf(Frost.SurfaceHigh, Frost.Surface))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.MusicNote, null,
            tint = Frost.TextTertiary,
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
 * Fila de cancion (como "Recently Listened" de la referencia neon): numero opcional, portada,
 * titulo en negrita y artista, tiempo y corazon. La que suena se rellena con el acento.
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
    val shape = RoundedCornerShape(14.dp)
    val accent = Frost.Accent
    val titleColor = Frost.TextPrimary
    val subColor = Frost.TextSecondary
    val dimColor = Frost.TextTertiary
    // La cancion que suena se eleva (cristal mas claro) y lleva una barra de acento a la izquierda
    Row(
        modifier
            .padding(vertical = 3.dp)
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.985f)
            .card(shape, raised = isCurrent)
            .drawBehind {
                if (isCurrent) {
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(0f, size.height * 0.28f),
                        size = Size(3.dp.toPx(), size.height * 0.44f),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                }
            }
            .padding(start = if (index != null) 12.dp else 10.dp, end = 4.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (index != null) {
            Text(
                String.format(Locale.US, "%02d", index),
                color = dimColor,
                fontSize = 11.sp,
                modifier = Modifier.width(24.dp),
            )
        }
        CoverThumb(track.artUri, 44.dp, corner = 10.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 14.sp,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = subColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            if (isCurrent) {
                EqBars(active = playing, color = Frost.Accent, modifier = Modifier.padding(end = 8.dp))
            } else if (track.durationMs > 0) {
                Text(
                    formatTime(track.durationMs),
                    color = dimColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            if (onFavorite != null) {
                PlainIconButton(
                    if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    if (favorite) "Quitar de favoritos" else "Añadir a favoritos",
                    onFavorite,
                    size = 38.dp, iconSize = 19.dp,
                    tint = if (favorite) Frost.Accent else dimColor,
                )
            }
        }
    }
}
