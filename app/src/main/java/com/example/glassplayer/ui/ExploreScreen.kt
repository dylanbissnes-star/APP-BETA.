package com.example.glassplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.ui.components.CoverThumb
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.card
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.theme.Frost
import androidx.compose.foundation.layout.BoxWithConstraints

/**
 * Explorar (como "Browse"/"Discover" de las referencias): rejilla de artistas o de albumes,
 * con un selector arriba. Tocar un elemento abre su pagina.
 */
@Composable
fun ExploreScreen(
    state: PlayerUiState,
    tab: Int,
    onTab: (Int) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artists = remember(state.library) { artistsOf(state.library) }
    val albums = remember(state.library) { albumsOf(state.library) }

    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Explorar",
            subtitle = if (tab == 0) "${artists.size} artistas" else "${albums.size} álbumes",
        )
        Segmented(
            options = listOf("Artistas", "Álbumes"),
            selected = tab,
            onSelect = onTab,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        )

        val empty = if (tab == 0) artists.isEmpty() else albums.isEmpty()
        if (empty) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    "Aún no hay ${if (tab == 0) "artistas" else "álbumes"}. Agrega una carpeta con música desde Inicio.",
                    color = Frost.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 40.dp),
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 168.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (tab == 0) {
                    items(artists, key = { "ar-${it.name}" }) { a ->
                        GridCard(
                            cover = a.cover,
                            title = a.name,
                            subtitle = songsLabel(a.tracks.size),
                            onClick = { onOpenArtist(a.name) },
                        )
                    }
                } else {
                    items(albums, key = { "al-${it.name}" }) { al ->
                        GridCard(
                            cover = al.cover,
                            title = al.name,
                            subtitle = listOf(al.artist, songsLabel(al.tracks.size)).filter { it.isNotBlank() }.joinToString(" · "),
                            onClick = { onOpenAlbum(al.name) },
                        )
                    }
                }
            }
        }
    }
}

/** Tarjeta de rejilla: portada cuadrada redondeada, titulo y subtitulo. */
@Composable
fun GridCard(cover: Any?, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.97f),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f).card(RoundedCornerShape(22.dp))) {
            CoverThumb(cover, maxWidth, corner = 22.dp)
        }
        Text(
            title,
            color = Frost.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            subtitle,
            color = Frost.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Selector de dos o mas opciones: la activa se rellena con el color del texto. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .card(shape, raised = true)
            .padding(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .pressClick({ onSelect(i) }, pressedScale = 0.96f)
                    .clip(shape)
                    .background(if (on) Frost.AccentFill else androidx.compose.ui.graphics.Color.Transparent)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (on) Frost.OnAccent else Frost.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
