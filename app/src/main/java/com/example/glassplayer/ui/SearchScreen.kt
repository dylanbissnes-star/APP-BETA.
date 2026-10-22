package com.example.glassplayer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.GlassSearchField
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.theme.Frost
import java.text.Normalizer

/** Pantalla "Buscar": busca por titulo, artista o album (sin distinguir mayusculas ni acentos). */
@Composable
fun SearchScreen(
    state: PlayerUiState,
    onPlay: (List<Track>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query, state.library) {
        if (query.isBlank()) emptyList()
        else {
            val q = normalize(query)
            state.library.filter {
                normalize(it.title).contains(q) || normalize(it.artist).contains(q) || normalize(it.album).contains(q)
            }
        }
    }
    val currentId = state.current?.id

    Column(
        modifier.fillMaxSize()
    ) {
        ScreenHeader(
            title = "Buscar",
            subtitle = if (query.isBlank()) "Canciones, artistas y álbumes" else "${results.size} resultados",
        )
        GlassSearchField(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )

        when {
            query.isBlank() -> Hint("Escribe para buscar en tu música")
            results.isEmpty() -> Hint("Sin resultados para «${query.trim()}»")
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 176.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(results, key = { _, t -> t.id }) { index, track ->
                    TrackRow(
                        track = track,
                        isCurrent = track.id == currentId,
                        playing = state.isPlaying,
                        onClick = { onPlay(results, index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Frost.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 40.dp),
        )
    }
}

private fun normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()
