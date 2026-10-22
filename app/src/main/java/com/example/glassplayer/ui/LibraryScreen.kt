package com.example.glassplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PhoneAndroid
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.SOURCE_DEMO
import com.example.glassplayer.data.SOURCE_DEVICE
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.GlassButton
import com.example.glassplayer.ui.components.GlassPill
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.components.glass
import com.example.glassplayer.ui.components.glassStrong
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.theme.Frost

private data class SourceItem(
    val key: String,
    val label: String,
    val count: Int,
    val icon: ImageVector,
    val folder: Boolean,
)

/**
 * Pantalla "Biblioteca" (estilo de la referencia: "For You" + "You Might Like"):
 *  - rejilla de tarjetas pequenas con las fuentes (todas, dispositivo, demo y cada carpeta agregada)
 *  - lista de canciones con triangulo de reproducir
 *  - desliza una cancion a la IZQUIERDA para agregarla a la cola de reproduccion
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
    val sources = remember(state.library, state.folders) {
        val counts = state.library.groupingBy { it.sourceId }.eachCount()
        buildList {
            add(SourceItem(SOURCE_ALL, "Todas", state.library.size, Icons.Rounded.LibraryMusic, false))
            val device = counts[SOURCE_DEVICE] ?: 0
            if (device > 0) add(SourceItem(SOURCE_DEVICE, "Dispositivo", device, Icons.Rounded.PhoneAndroid, false))
            val demo = counts[SOURCE_DEMO] ?: 0
            if (demo > 0) add(SourceItem(SOURCE_DEMO, "Demo", demo, Icons.Rounded.Cloud, false))
            state.folders.forEach { add(SourceItem(it.uri, it.name, counts[it.uri] ?: 0, Icons.Rounded.Folder, true)) }
        }
    }
    val currentId = state.current?.id

    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Biblioteca",
            subtitle = "${visible.size} canciones",
            trailing = {
                GlassButton(Icons.Rounded.CreateNewFolder, "Agregar carpeta", onAddFolder)
            },
        )

        LazyColumn(
            // Espacio inferior para que el mini reproductor y el dock no tapen la ultima fila
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 176.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "h-sources") { SectionTitle("Tus fuentes") }
            item(key = "sources") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sources.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { item ->
                                val selected = state.selectedSource == item.key
                                SourceChip(
                                    item = item,
                                    selected = selected,
                                    onClick = { onSelectSource(item.key) },
                                    onRemove = if (selected && item.folder) ({ onRemoveFolder(item.key) }) else null,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            item(key = "h-songs") {
                SectionTitle(
                    "Canciones · ${visible.size}",
                    hint = "Desliza una canción a la izquierda para añadirla a la cola",
                )
            }

            if (visible.isEmpty()) {
                item(key = "empty") { EmptyLibrary(scanning = state.scanning, onAddFolder = onAddFolder) }
            } else {
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

@Composable
private fun SectionTitle(title: String, hint: String? = null) {
    Column(Modifier.padding(start = 4.dp, top = 8.dp)) {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.labelSmall, color = Frost.TextSecondary)
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
                    GlassButton(Icons.AutoMirrored.Rounded.PlaylistAdd, "Añadir a la cola", {}, size = 40.dp, active = true)
                }
            }
        },
    ) {
        TrackRow(track = track, isCurrent = isCurrent, playing = playing, onClick = onClick)
    }
}

/** Tarjeta pequena de fuente (icono + nombre + cantidad), como los mosaicos de "For You". */
@Composable
private fun SourceChip(
    item: SourceItem,
    selected: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .pressClick(onClick, pressedScale = 0.95f)
            .then(if (selected) Modifier.glassStrong(shape) else Modifier.glass(shape, strength = 0.9f, darkness = 0.04f))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White.copy(alpha = 0.26f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, null, tint = Color.White, modifier = Modifier.size(19.dp))
        }
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 9.dp)
        ) {
            Text(
                item.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${item.count} canciones",
                style = MaterialTheme.typography.labelSmall,
                color = Frost.TextSecondary,
                maxLines = 1,
            )
        }
        if (onRemove != null) {
            Box(
                Modifier
                    .size(26.dp)
                    .pressClick(onRemove)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, "Quitar carpeta", tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun EmptyLibrary(scanning: Boolean, onAddFolder: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (scanning) {
            CircularProgressIndicator(color = Color.White)
            Text(
                "Buscando música…",
                style = MaterialTheme.typography.bodyMedium,
                color = Frost.TextSecondary,
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
                color = Frost.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            GlassPill("Agregar carpeta", onAddFolder, icon = Icons.Rounded.CreateNewFolder)
        }
    }
}
