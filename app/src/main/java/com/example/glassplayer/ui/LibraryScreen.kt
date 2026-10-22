package com.example.glassplayer.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.SOURCE_DEMO
import com.example.glassplayer.data.SOURCE_DEVICE
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.BlackGlassButton
import com.example.glassplayer.ui.components.BlackGlassPill
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.components.blackGlass
import com.example.glassplayer.ui.components.glass
import com.example.glassplayer.ui.components.pressClick

/**
 * Pantalla "Biblioteca": listado completo de canciones.
 *  - Chips para filtrar por origen (todas, dispositivo o cada carpeta agregada).
 *  - Toca una cancion para reproducirla.
 *  - Desliza una cancion a la IZQUIERDA para agregarla a la cola de reproduccion.
 */
@Composable
fun LibraryScreen(
    state: PlayerUiState,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onSelectSource: (String) -> Unit,
    onPlay: (List<Track>, Int) -> Unit,
    onQueue: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = remember(state.library, state.selectedSource) {
        if (state.selectedSource == SOURCE_ALL) state.library
        else state.library.filter { it.sourceId == state.selectedSource }
    }
    val counts = remember(state.library) { state.library.groupingBy { it.sourceId }.eachCount() }
    val currentId = state.current?.id

    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScreenHeader(
            title = "Biblioteca",
            subtitle = "${visible.size} canciones",
            trailing = {
                BlackGlassButton(Icons.Rounded.CreateNewFolder, "Agregar carpeta", onAddFolder)
            },
        )

        // Filtros por origen
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = SOURCE_ALL) {
                SourceChip("Todas · ${state.library.size}", state.selectedSource == SOURCE_ALL, { onSelectSource(SOURCE_ALL) })
            }
            val deviceCount = counts[SOURCE_DEVICE] ?: 0
            if (deviceCount > 0) {
                item(key = SOURCE_DEVICE) {
                    SourceChip("Dispositivo · $deviceCount", state.selectedSource == SOURCE_DEVICE, { onSelectSource(SOURCE_DEVICE) })
                }
            }
            if ((counts[SOURCE_DEMO] ?: 0) > 0) {
                item(key = SOURCE_DEMO) {
                    SourceChip("Demo · ${counts[SOURCE_DEMO]}", state.selectedSource == SOURCE_DEMO, { onSelectSource(SOURCE_DEMO) })
                }
            }
            items(state.folders, key = { it.uri }) { folder ->
                val selected = state.selectedSource == folder.uri
                SourceChip(
                    label = "${folder.name} · ${counts[folder.uri] ?: 0}",
                    selected = selected,
                    onClick = { onSelectSource(folder.uri) },
                    folder = true,
                    onRemove = if (selected) ({ onRemoveFolder(folder.uri) }) else null,
                )
            }
        }

        Text(
            "Desliza una canción a la izquierda para añadirla a la cola",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )

        if (visible.isEmpty()) {
            EmptyLibrary(scanning = state.scanning, onAddFolder = onAddFolder)
        } else {
            LazyColumn(
                // Espacio inferior para que el dock flotante no tape la ultima fila
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(visible, key = { _, t -> t.id }) { index, track ->
                    SwipeableTrackRow(
                        track = track,
                        isCurrent = track.id == currentId,
                        playing = state.isPlaying,
                        onClick = { onPlay(visible, index) },
                        onQueue = { onQueue(track) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTrackRow(
    track: Track,
    isCurrent: Boolean,
    playing: Boolean,
    onClick: () -> Unit,
    onQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val latestOnQueue by rememberUpdatedState(onQueue)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                latestOnQueue()
            }
            false // la fila siempre vuelve a su sitio; solo disparamos la accion
        },
        positionalThreshold = { total -> total * 0.35f },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(end = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "A la cola",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    BlackGlassButton(Icons.AutoMirrored.Rounded.PlaylistAdd, "Añadir a la cola", {}, size = 40.dp, active = true)
                }
            }
        },
    ) {
        TrackRow(track = track, isCurrent = isCurrent, playing = playing, onClick = onClick)
    }
}

@Composable
private fun SourceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    folder: Boolean = false,
    onRemove: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .pressClick(onClick, pressedScale = 0.94f)
            .then(if (selected) Modifier.blackGlass(shape) else Modifier.glass(shape, strength = 0.8f))
            .animateContentSize()
            .padding(start = 14.dp, end = if (onRemove != null) 6.dp else 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (folder) {
            Icon(Icons.Rounded.Folder, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            Box(Modifier.size(6.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onRemove != null) {
            Box(Modifier.size(6.dp))
            Box(
                Modifier
                    .size(22.dp)
                    .pressClick(onRemove)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, "Quitar carpeta", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun EmptyLibrary(scanning: Boolean, onAddFolder: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (scanning) {
            CircularProgressIndicator(color = Color.White)
            Text(
                "Buscando música…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            Text(
                "No hay canciones aquí",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                "Agrega una carpeta de tu teléfono para traer su música.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            BlackGlassPill("Agregar carpeta", onAddFolder, icon = Icons.Rounded.CreateNewFolder)
        }
    }
}
