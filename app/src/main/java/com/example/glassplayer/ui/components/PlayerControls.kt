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
import com.example.glassplayer.ui.theme.Frost

/**
 * Controles del reproductor (como la referencia movil):
 *  1. barra de progreso fina con los tiempos
 *  2. anterior / play-pausa (circulo lleno) / siguiente
 *  3. volumen
 *  4. fila de acciones: aleatorio, repetir, salida de audio y cola
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
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 },
    ) {
        Column(
            modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Progreso
            ThinSlider(
                value = state.progress,
                onValueChange = onSeek,
                fill = accent,
                track = Frost.content(0.18f),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatTime(state.positionMs), style = MaterialTheme.typography.labelSmall, color = Frost.TextSecondary)
                Text(formatTime(state.durationMs), style = MaterialTheme.typography.labelSmall, color = Frost.TextSecondary)
            }

            // Transporte
            Row(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlainIconButton(Icons.Rounded.SkipPrevious, "Anterior", onPrevious, size = 60.dp, iconSize = 40.dp)
                Box(
                    Modifier
                        .size(70.dp)
                        .pressClick(onTogglePlay, pressedScale = 0.92f)
                        .clip(CircleShape)
                        .background(Frost.TextPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        if (state.isPlaying) "Pausar" else "Reproducir",
                        tint = Frost.Inverse,
                        modifier = Modifier.size(38.dp),
                    )
                }
                PlainIconButton(Icons.Rounded.SkipNext, "Siguiente", onNext, size = 60.dp, iconSize = 40.dp)
            }

            // Volumen
            VolumeControl(Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp))

            // Acciones
            Row(
                Modifier
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .card(RoundedCornerShape(22.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
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

/** Icono de la fila de acciones: tenue si esta apagado, con el acento y un punto debajo si esta activo. */
@Composable
private fun PillIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    active: Boolean = false,
    badge: Int = 0,
) {
    val accent = Frost.Accent
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        PlainIconButton(
            icon, description, onClick,
            size = 44.dp, iconSize = 24.dp,
            tint = if (active) accent else Frost.TextSecondary,
        )
        if (active) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-3).dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
        if (badge > 0) {
            CountBadge(badge, Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
        }
    }
}
