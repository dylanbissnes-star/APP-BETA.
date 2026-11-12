package com.example.glassplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.ActionPill
import com.example.glassplayer.ui.components.EmptyHint
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.theme.Frost

/**
 * Biblioteca: filtro por carpeta (chips) y lista numerada de canciones con tiempo y corazon.
 * Desliza una cancion a la IZQUIERDA para agregarla a la cola de reproduccion.
 */
@Composable
fun LibraryScreen(
    state: PlayerUiState,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onSelectSource: (String) -> Unit,
    onPlay: (List<Track>, Int) -> Unit,
    onQueue: (Track) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = remember(state.library, state.selectedSource) {
        if (state.selectedSource == SOURCE_ALL) state.library
        else state.library.filter { it.sourceId == state.selectedSource }
    }
    val chips = remember(state.library, state.folders) {
        val entries = folderEntries(state)
        // «Toda la musica» y las demas fuentes; las carpetas agregadas siempre aparecen, aunque esten vacias
        entries.map { SourceChipData(it.key, it.label, it.tracks.size, it.key in state.folders.map { f -> f.uri }) }
    }
    val currentId = state.current?.id

    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Biblioteca",
            subtitle = songsLabel(visible.size),
            trailing = {
                ActionPill("Carpeta", onAddFolder, icon = Icons.Rounded.CreateNewFolder)
            },
        )

        if (chips.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(chips, key = { it.key }) { chip ->
                    val selected = state.selectedSource == chip.key
                    SourceChip(
                        chip = chip,
                        selected = selected,
                        onClick = { onSelectSource(chip.key) },
                        onRemove = if (selected && chip.folder) ({ onRemoveFolder(chip.key) }) else null,
                    )
                }
            }
            Text(
                "Desliza una canción a la izquierda para añadirla a la cola",
                style = MaterialTheme.typography.labelSmall,
                color = Frost.TextTertiary,
                modifier = Modifier.padding(start = 22.dp, top = 10.dp, bottom = 4.dp),
            )
        }

        LazyColumn(
            // Espacio inferior para el mini reproductor y la barra de pestanas
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 168.dp),
        ) {
            if (visible.isEmpty()) {
                item(key = "empty") {
                    if (state.scanning) {
                        EmptyHint("Buscando música…", "Esto puede tardar un momento.") {
                            CircularProgressIndicator(color = Frost.Accent)
                        }
                    } else {
                        EmptyHint(
                            "No hay canciones aquí",
                            "Agrega una carpeta de tu teléfono para traer su música.",
                        ) {
                            ActionPill("Agregar carpeta", onAddFolder, icon = Icons.Rounded.CreateNewFolder, primary = true)
                        }
                    }
                }
            } else {
                itemsIndexed(visible, key = { _, t -> t.id }) { index, track ->
                    SwipeableTrackRow(
                        track = track,
                        index = index + 1,
                        isCurrent = track.id == currentId,
                        playing = state.isPlaying,
                        favorite = track.id in state.favorites,
                        onClick = { onPlay(visible, index) },
                        onQueue = { onQueue(track) },
                        onFavorite = { onToggleFavorite(track.id) },
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
    index: Int,
    isCurrent: Boolean,
    playing: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onQueue: () -> Unit,
    onFavorite: () -> Unit,
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
                        .background(Frost.Surface)
                        .padding(end = 18.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "A la cola",
                        color = Frost.Accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, "Añadir a la cola", tint = Frost.Accent, modifier = Modifier.size(24.dp))
                }
            }
        },
    ) {
        TrackRow(
            track = track,
            isCurrent = isCurrent,
            playing = playing,
            onClick = onClick,
            index = index,
            favorite = favorite,
            onFavorite = onFavorite,
        )
    }
}

private data class SourceChipData(val key: String, val label: String, val count: Int, val folder: Boolean)

/** Chip de carpeta/fuente: la activa se rellena con el color del texto; si es una carpeta, trae un ✕ para quitarla. */
@Composable
private fun SourceChip(
    chip: SourceChipData,
    selected: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .pressClick(onClick, pressedScale = 0.95f)
            .clip(shape)
            .background(if (selected) Frost.AccentFill else Frost.SurfaceHigh)
            .then(if (selected) Modifier else Modifier.border(1.dp, Frost.Outline, shape))
            .padding(start = 14.dp, end = if (onRemove != null) 8.dp else 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            chip.label,
            color = if (selected) Frost.OnAccent else Frost.TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            "  ${chip.count}",
            color = if (selected) Frost.OnAccent.copy(alpha = 0.6f) else Frost.TextTertiary,
            fontSize = 12.sp,
        )
        if (onRemove != null) {
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.Close, "Quitar carpeta",
                tint = Frost.OnAccent,
                modifier = Modifier
                    .size(18.dp)
                    .pressClick(onRemove, pressedScale = 0.85f)
                    .clip(CircleShape)
                    .background(Frost.OnAccent.copy(alpha = 0.15f))
                    .padding(2.dp),
            )
        }
    }
}
