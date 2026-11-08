package com.example.glassplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.ui.components.ActionPill
import com.example.glassplayer.ui.components.CoverThumb
import com.example.glassplayer.ui.components.EqBars
import com.example.glassplayer.ui.components.RoundButton
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.components.card
import com.example.glassplayer.ui.theme.Frost

/**
 * "Siguiente en la cola" (como "Playing Next" de la referencia):
 *  - Reproduciendo ahora (tarjeta)
 *  - En cola: lo que agregaste deslizando en la biblioteca (sonara primero)
 *  - A continuacion: el resto de la lista actual
 */
@Composable
fun QueueScreen(
    state: PlayerUiState,
    onJump: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearQueued: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks = state.tracks
    val queuedStart = (state.currentIndex + 1).coerceAtMost(tracks.size)
    val queuedEnd = (state.currentIndex + 1 + state.queuedCount).coerceIn(queuedStart, tracks.size)
    val pending = tracks.size - queuedStart

    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Siguiente",
            subtitle = "$pending por reproducir",
            trailing = {
                if (state.queuedCount > 0) ActionPill("Vaciar cola", onClearQueued)
            },
        )

        LazyColumn(
            // Espacio inferior para el mini reproductor y la barra de pestanas
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 168.dp),
        ) {
            // Reproduciendo ahora
            state.current?.let { now ->
                item(key = "h-now") { SectionLabel("Reproduciendo ahora") }
                item(key = "now") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .card(RoundedCornerShape(20.dp), raised = true)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CoverThumb(now.artUri, 58.dp, corner = 14.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(
                                now.title,
                                color = Frost.Accent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                now.artist,
                                color = Frost.TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        EqBars(active = state.isPlaying, color = Frost.Accent, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }

            // En cola (lo que agregaste)
            item(key = "h-queued") { SectionLabel("En cola · ${state.queuedCount}") }
            if (queuedEnd == queuedStart) {
                item(key = "queued-empty") {
                    Text(
                        "Nada en cola. Desliza una canción a la izquierda en la biblioteca para añadirla.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Frost.TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
            items(count = queuedEnd - queuedStart, key = { "q${queuedStart + it}" }) { i ->
                val index = queuedStart + i
                QueueRow(state, index, onJump, onRemove, Modifier.animateItem())
            }

            // A continuacion
            if (queuedEnd < tracks.size) {
                item(key = "h-upcoming") {
                    SectionLabel(
                        if (state.shuffle) "A continuación · aleatorio, el orden puede variar"
                        else "A continuación"
                    )
                }
                items(count = tracks.size - queuedEnd, key = { "u${queuedEnd + it}" }) { i ->
                    val index = queuedEnd + i
                    QueueRow(state, index, onJump, onRemove, Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    state: PlayerUiState,
    index: Int,
    onJump: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackRow(
        track = state.tracks[index],
        isCurrent = false,
        onClick = { onJump(index) },
        modifier = modifier,
        trailing = {
            RoundButton(Icons.Rounded.Close, "Quitar de la cola", { onRemove(index) }, size = 34.dp, iconSize = 16.dp)
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Frost.TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}
