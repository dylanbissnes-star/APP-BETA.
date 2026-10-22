package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import com.example.glassplayer.ui.theme.Frost

/**
 * Controles del reproductor, como en la referencia:
 *  1. anterior / play-pausa / siguiente: simbolos blancos grandes, sin fondo
 *  2. barra de progreso fina (relleno de acento) con los tiempos en los extremos
 *  3. pastilla de cristal con aleatorio, repetir, salida de audio y cola
 * Entra con un fundido y un ligero deslizamiento.
 */
@Composable
fun PlayerControls(
    state: PlayerUiState,
    accent: Color,
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
            modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Transporte (sin fondo)
            Row(
                horizontalArrangement = Arrangement.spacedBy(34.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlainIconButton(Icons.Rounded.SkipPrevious, "Anterior", onPrevious, size = 64.dp, iconSize = 42.dp)
                PlainIconButton(
                    if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (state.isPlaying) "Pausar" else "Reproducir",
                    onTogglePlay,
                    size = 76.dp,
                    iconSize = 58.dp,
                )
                PlainIconButton(Icons.Rounded.SkipNext, "Siguiente", onNext, size = 64.dp, iconSize = 42.dp)
            }

            // Progreso
            ThinSlider(
                value = state.progress,
                onValueChange = onSeek,
                fill = accent,
                track = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 2.dp),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatTime(state.positionMs), style = MaterialTheme.typography.labelSmall, color = Frost.TextSecondary)
                Text(formatTime(state.durationMs), style = MaterialTheme.typography.labelSmall, color = Frost.TextSecondary)
            }

            // Pastilla de acciones
            Row(
                Modifier
                    .padding(top = 18.dp, start = 12.dp, end = 12.dp)
                    .fillMaxWidth()
                    .glass(RoundedCornerShape(28.dp), strength = 1.1f, darkness = 0.10f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillIcon(Icons.Rounded.Shuffle, "Aleatorio", onShuffle, active = state.shuffle)
                PillIcon(
                    if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    "Repetir", onRepeat,
                    active = state.repeatMode != Player.REPEAT_MODE_OFF,
                )
                PillIcon(Icons.Rounded.Speaker, "Salida de audio", onOpenDevices)
                PillIcon(
                    Icons.AutoMirrored.Rounded.QueueMusic, "Cola de reproducción", onOpenQueue,
                    badge = state.queuedCount,
                )
            }
        }
    }
}

/** Icono de la pastilla: tenue si esta apagado, blanco con un punto debajo si esta activo. */
@Composable
private fun PillIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    active: Boolean = false,
    badge: Int = 0,
) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        PlainIconButton(
            icon, description, onClick,
            size = 44.dp, iconSize = 24.dp,
            tint = if (active) Color.White else Color.White.copy(alpha = 0.62f),
        )
        if (active) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-3).dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        if (badge > 0) {
            CountBadge(badge, Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
        }
    }
}
