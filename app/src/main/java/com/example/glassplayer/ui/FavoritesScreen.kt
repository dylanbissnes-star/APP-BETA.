package com.example.glassplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.ActionPill
import com.example.glassplayer.ui.components.EmptyHint
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.TrackRow

/** Favoritos: las canciones marcadas con el corazon, en una lista numerada. */
@Composable
fun FavoritesScreen(
    state: PlayerUiState,
    onPlay: (List<Track>, Int) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favorites = remember(state.library, state.favorites) {
        state.library.filter { it.id in state.favorites }
    }
    val currentId = state.current?.id

    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Favoritos",
            subtitle = if (favorites.isEmpty()) "Tus canciones con corazón" else songsLabel(favorites.size),
            trailing = {
                if (favorites.isNotEmpty()) {
                    ActionPill("Reproducir", { onPlay(favorites, 0) }, icon = Icons.Rounded.PlayArrow, primary = true)
                }
            },
        )

        if (favorites.isEmpty()) {
            EmptyHint(
                "Aún no tienes favoritas",
                "Toca el corazón de cualquier canción para guardarla aquí.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 168.dp),
            ) {
                itemsIndexed(favorites, key = { _, t -> "f-${t.id}" }) { i, track ->
                    TrackRow(
                        track = track,
                        isCurrent = track.id == currentId,
                        playing = state.isPlaying,
                        onClick = { onPlay(favorites, i) },
                        index = i + 1,
                        favorite = true,
                        onFavorite = { onToggleFavorite(track.id) },
                    )
                }
            }
        }
    }
}
