package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.glassplayer.PlayerUiState

/**
 * Barra en forma de capsula de vidrio:
 * [anterior] [play/pausa] [siguiente] | mini tarjeta con progreso | [shuffle] [repeat]
 */
@Composable
fun GlassControlBar(
    state: PlayerUiState,
    onPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val capsule = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .glass(capsule)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        GlassIconButton(Icons.Rounded.SkipPrevious, "Anterior", onPrevious)
        GlassIconButton(
            if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            if (state.isPlaying) "Pausar" else "Reproducir",
            onTogglePlay,
            size = 42.dp,
        )
        GlassIconButton(Icons.Rounded.SkipNext, "Siguiente", onNext)

        MiniNowPlaying(state, onSeek, Modifier.weight(1f).padding(horizontal = 4.dp))

        GlassIconButton(
            Icons.Rounded.Shuffle, "Aleatorio", onShuffle,
            size = 30.dp,
            tint = if (state.shuffle) Color.White else Color.White.copy(alpha = 0.55f),
        )
        GlassIconButton(
            if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
            "Repetir", onRepeat,
            size = 30.dp,
            tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) Color.White else Color.White.copy(alpha = 0.55f),
        )
    }
}

@Composable
private fun MiniNowPlaying(state: PlayerUiState, onSeek: (Float) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    val track = state.current
    Box(
        modifier
            .fillMaxHeight()
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.55f))
    ) {
        Row(
            Modifier.fillMaxWidth().fillMaxHeight().padding(start = 7.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MusicNote, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                if (track?.artUri != null) {
                    AsyncImage(track.artUri, null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    track?.title ?: "—",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    track?.artist ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f)),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Barra de progreso (toca para saltar a esa posicion)
        BoxWithConstraints(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(14.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset -> onSeek(offset.x / size.width) }
                },
            contentAlignment = Alignment.BottomStart,
        ) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = 0.15f)))
            Box(
                Modifier
                    .width(maxWidth * state.progress)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    tint: Color = Color.White,
) {
    Box(
        Modifier.size(size + 6.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(size))
    }
}
