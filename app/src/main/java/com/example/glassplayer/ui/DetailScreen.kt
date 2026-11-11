package com.example.glassplayer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.ActionPill
import com.example.glassplayer.ui.components.CoverThumb
import com.example.glassplayer.ui.components.EmptyHint
import com.example.glassplayer.ui.components.RoundButton
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.theme.Frost

/**
 * Pagina de un artista o de un album: portada, nombre, botones Reproducir / Aleatorio
 * y la lista numerada de sus canciones.
 */
@Composable
fun DetailScreen(
    kindLabel: String,
    title: String,
    subtitle: String,
    cover: Any?,
    tracks: List<Track>,
    state: PlayerUiState,
    onBack: () -> Unit,
    onPlay: (List<Track>, Int) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentId = state.current?.id

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack)
            Text(
                kindLabel,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Frost.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.size(44.dp)) // equilibra el boton de volver para centrar el titulo
        }

        if (tracks.isEmpty()) {
            EmptyHint("Sin canciones", "No quedan canciones aquí.")
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 168.dp),
        ) {
            item(key = "head") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    CoverThumb(cover, 132.dp, corner = 24.dp)
                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(
                            title,
                            color = Frost.TextPrimary,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            subtitle,
                            color = Frost.TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            item(key = "actions") {
                Row(
                    Modifier.padding(top = 18.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActionPill("Reproducir", { onPlay(tracks, 0) }, icon = Icons.Rounded.PlayArrow, primary = true)
                    ActionPill("Aleatorio", { onPlay(tracks.shuffled(), 0) }, icon = Icons.Rounded.Shuffle)
                }
            }
            itemsIndexed(tracks, key = { _, t -> "d-${t.id}" }) { i, track ->
                TrackRow(
                    track = track,
                    isCurrent = track.id == currentId,
                    playing = state.isPlaying,
                    onClick = { onPlay(tracks, i) },
                    index = i + 1,
                    favorite = track.id in state.favorites,
                    onFavorite = { onToggleFavorite(track.id) },
                )
            }
        }
    }
}
