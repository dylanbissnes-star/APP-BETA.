package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.ui.theme.BlackGlass

/**
 * Panel flotante de reproduccion (cristal negro):
 *  - progreso fino con tiempos
 *  - aleatorio / anterior / play-pausa / siguiente / repetir (botones circulares negros)
 *  - volumen, dispositivos de audio y cola
 * Entra con un fundido y un ligero deslizamiento.
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
    onOpenQueue: () -> Unit,
    onOpenDevices: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 6 },
    ) {
        Column(
            modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(32.dp), darkness = 0.55f)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Progreso
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatTime(state.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = BlackGlass.TextSecondary,
                )
                ThinSlider(
                    value = state.progress,
                    onValueChange = onSeek,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                )
                Text(
                    formatTime(state.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = BlackGlass.TextSecondary,
                )
            }

            // Transporte
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BlackGlassButton(
                    Icons.Rounded.Shuffle, "Aleatorio", onShuffle,
                    size = 40.dp,
                    active = state.shuffle,
                    tint = if (state.shuffle) Color.White else Color.White.copy(alpha = 0.45f),
                )
                BlackGlassButton(Icons.Rounded.SkipPrevious, "Anterior", onPrevious, size = 52.dp)
                BlackGlassButton(
                    if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (state.isPlaying) "Pausar" else "Reproducir",
                    onTogglePlay,
                    size = 64.dp,
                    iconSize = 32.dp,
                    pulse = state.isPlaying,
                )
                BlackGlassButton(Icons.Rounded.SkipNext, "Siguiente", onNext, size = 52.dp)
                BlackGlassButton(
                    if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    "Repetir", onRepeat,
                    size = 40.dp,
                    active = state.repeatMode != Player.REPEAT_MODE_OFF,
                    tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) Color.White else Color.White.copy(alpha = 0.45f),
                )
            }

            // Volumen, dispositivos y cola
            Row(verticalAlignment = Alignment.CenterVertically) {
                VolumeControl(Modifier.weight(1f))
                GhostIconButton(Icons.Rounded.Speaker, "Dispositivos de audio", onOpenDevices)
                GhostIconButton(Icons.AutoMirrored.Rounded.QueueMusic, "Cola de reproducción", onOpenQueue)
            }
        }
    }
}

/** Icono lineal sin fondo (controles secundarios). */
@Composable
private fun GhostIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .pressClick(onClick, pressedScale = 0.88f)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(22.dp))
    }
}
