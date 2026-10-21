package com.example.glassplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.ui.components.BlackGlassButton
import com.example.glassplayer.ui.components.BlackGlassPill
import com.example.glassplayer.ui.components.TrackRow

/**
 * Pantalla "Cola de reproduccion":
 *  - Reproduciendo ahora
 *  - En cola: lo que agregaste deslizando en la biblioteca (sonara primero)
 *  - A continuacion: el resto de la lista actual
 */
@Composable
fun QueueScreen(
    state: PlayerUiState,
    onBack: () -> Unit,
    onJump: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearQueued: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks = state.tracks
    val queuedStart = (state.currentIndex + 1).coerceAtMost(tracks.size)
    val queuedEnd = (state.currentIndex + 1 + state.queuedCount).coerceIn(queuedStart, tracks.size)
    val pending = tracks.size - queuedStart

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.40f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BlackGlassButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text("Cola de reproducción", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    "$pending por reproducir",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            if (state.queuedCount > 0) {
                BlackGlassPill("Vaciar cola", onClearQueued)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Reproduciendo ahora
            state.current?.let { now ->
                item(key = "h-now") { SectionLabel("Reproduciendo ahora") }
                item(key = "now") { TrackRow(now, isCurrent = true, onClick = {}) }
            }

            // En cola (lo que agregaste)
            item(key = "h-queued") { SectionLabel("En cola · ${state.queuedCount}") }
            if (queuedEnd == queuedStart) {
                item(key = "queued-empty") {
                    Text(
                        "Nada en cola. Desliza una canción a la izquierda en la biblioteca para añadirla.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    )
                }
            }
            items(count = queuedEnd - queuedStart, key = { "q${queuedStart + it}" }) { i ->
                val index = queuedStart + i
                QueueRow(state, index, onJump, onRemove)
            }

            // A continuacion
            if (queuedEnd < tracks.size) {
                item(key = "h-upcoming") {
                    SectionLabel(
                        if (state.shuffle) "A continuación (aleatorio: el orden puede variar)"
                        else "A continuación"
                    )
                }
                items(count = tracks.size - queuedEnd, key = { "u${queuedEnd + it}" }) { i ->
                    val index = queuedEnd + i
                    QueueRow(state, index, onJump, onRemove)
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
) {
    TrackRow(
        track = state.tracks[index],
        isCurrent = false,
        onClick = { onJump(index) },
        trailing = {
            BlackGlassButton(Icons.Rounded.Close, "Quitar de la cola", { onRemove(index) }, size = 36.dp)
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
    )
}
