package com.example.glassplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.CoverThumb
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.components.card
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.theme.Frost
import java.util.Locale

/**
 * Inicio (estilo cristal ahumado): un tablero con una columna de cristal por carpeta,
 * o una lista plana de canciones. Arriba: buscador, boton fantasma «Carpeta» y boton blanco «Reproducir».
 */
@Composable
fun HomeScreen(
    state: PlayerUiState,
    onPlayAll: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenFolder: (FolderEntry) -> Unit,
    onPlayFolder: (FolderEntry) -> Unit,
    onAddFolder: () -> Unit,
    onPlay: (List<Track>, Int) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenArtists: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val folders = remember(state.library, state.folders) {
        folderEntries(state).filter { it.tracks.isNotEmpty() && it.key != SOURCE_ALL }
    }
    val artists = remember(state.library) { artistsOf(state.library).take(12) }
    val songs = remember(state.library) { state.library.take(30) }
    val currentId = state.current?.id
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 tablero, 1 lista

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 0.dp, bottom = 196.dp),
    ) {
        item(key = "top") {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Biblioteca", color = Frost.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${state.library.size} canciones · ${folders.size} carpetas",
                    color = Frost.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Light,
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .pressClick(onOpenSearch, pressedScale = 0.98f)
                        .card(RoundedCornerShape(12.dp), raised = true)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Search, null, tint = Frost.TextSecondary, modifier = Modifier.size(20.dp))
                    Text(
                        "Buscar canción, álbum, artista…",
                        color = Frost.TextTertiary, fontSize = 13.sp, fontWeight = FontWeight.Light,
                        maxLines = 1, modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    GhostButton("Carpeta", onAddFolder)
                    SolidButton(
                        "Reproducir",
                        onClick = if (state.library.isEmpty()) onAddFolder else onPlayAll,
                    )
                    Spacer(Modifier.weight(1f))
                    Segmented(mode) { mode = it }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (mode == 0) {
            item(key = "board") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(folders, key = { it.key }) { entry ->
                        BoardColumn(
                            entry = entry,
                            currentId = currentId,
                            onOpen = { onOpenFolder(entry) },
                            onPlayFolder = { onPlayFolder(entry) },
                            onPlayAt = { i -> onPlay(entry.tracks, i) },
                        )
                    }
                    item(key = "add") { AddColumn(onAddFolder) }
                }
            }
            if (artists.isNotEmpty()) {
                item(key = "h-artists") {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "ARTISTAS", color = Frost.TextSecondary, fontSize = 12.sp,
                            fontWeight = FontWeight.Medium, letterSpacing = 1.4.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Ver todos", color = Frost.TextSecondary, fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.pressClick(onOpenArtists, pressedScale = 0.95f),
                        )
                    }
                }
                item(key = "artists") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                    ) {
                        items(artists, key = { "ar-${it.name}" }) { artist ->
                            Column(
                                Modifier.width(72.dp).pressClick({ onOpenArtist(artist.name) }, pressedScale = 0.95f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CoverThumb(artist.cover, 64.dp, corner = 32.dp)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    artist.name, color = Frost.TextSecondary, fontSize = 11.sp,
                                    fontWeight = FontWeight.Light, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            itemsIndexed(songs, key = { _, t -> "s-${t.id}" }) { i, track ->
                TrackRow(
                    track = track,
                    isCurrent = track.id == currentId,
                    playing = state.isPlaying,
                    onClick = { onPlay(songs, i) },
                    index = i + 1,
                    favorite = track.id in state.favorites,
                    onFavorite = { onToggleFavorite(track.id) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            if (state.library.size > songs.size) {
                item(key = "more") {
                    Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                        GhostButton("Ver toda la biblioteca", onOpenLibrary)
                    }
                }
            }
        }
    }
}

/** Boton con solo contorno de cristal (como «Import» de la referencia). */
@Composable
private fun GhostButton(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .pressClick(onClick, pressedScale = 0.96f)
            .clip(shape)
            .border(1.dp, Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark)), shape)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CreateNewFolder, null, tint = Frost.TextPrimary, modifier = Modifier.size(16.dp))
        Text(label, color = Frost.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 7.dp))
    }
}

/** Boton blanco principal (como «New loan» de la referencia). */
@Composable
private fun SolidButton(label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .pressClick(onClick, pressedScale = 0.96f)
            .clip(RoundedCornerShape(12.dp))
            .background(Frost.AccentFill)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.PlayArrow, null, tint = Frost.OnAccent, modifier = Modifier.size(17.dp))
        Text(label, color = Frost.OnAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 5.dp))
    }
}

/** Control segmentado Tablero / Lista. */
@Composable
private fun Segmented(selected: Int, onSelect: (Int) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .clip(shape)
            .border(1.dp, Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark)), shape)
            .padding(3.dp),
    ) {
        listOf("Tablero", "Lista").forEachIndexed { i, label ->
            val on = i == selected
            Text(
                label,
                color = if (on) Frost.TextPrimary else Frost.TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (on) FontWeight.Medium else FontWeight.Light,
                modifier = Modifier
                    .pressClick({ onSelect(i) }, pressedScale = 0.96f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (on) Frost.SurfaceHigh else androidx.compose.ui.graphics.Color.Transparent)
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            )
        }
    }
}

/** Columna del tablero: una carpeta, con contador, reproducir y sus primeras canciones. */
@Composable
private fun BoardColumn(
    entry: FolderEntry,
    currentId: String?,
    onOpen: () -> Unit,
    onPlayFolder: () -> Unit,
    onPlayAt: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(Modifier.width(284.dp).card(shape).padding(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.label, color = Frost.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).pressClick(onOpen, pressedScale = 0.98f),
            )
            Box(
                Modifier.padding(start = 8.dp).clip(CircleShape).background(Frost.SurfaceHigh)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(String.format(Locale.US, "%d", entry.tracks.size), color = Frost.TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(30.dp).pressClick(onPlayFolder, pressedScale = 0.9f).clip(CircleShape)
                    .border(1.dp, Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark)), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.PlayArrow, "Reproducir carpeta", tint = Frost.TextPrimary, modifier = Modifier.size(18.dp)) }
        }
        entry.tracks.take(5).forEachIndexed { i, t ->
            val cur = t.id == currentId
            Row(
                Modifier
                    .padding(vertical = 3.dp)
                    .fillMaxWidth()
                    .pressClick({ onPlayAt(i) }, pressedScale = 0.985f)
                    .card(RoundedCornerShape(14.dp), raised = cur)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoverThumb(t.artUri, 38.dp, corner = 10.dp)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        t.title, color = if (cur) Frost.Accent else Frost.TextPrimary,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        t.artist, color = Frost.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Light,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp).pressClick(onOpen, pressedScale = 0.98f).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Add, null, tint = Frost.TextSecondary, modifier = Modifier.size(16.dp))
            Text(
                if (entry.tracks.size > 5) "Ver las ${entry.tracks.size} canciones" else "Abrir carpeta",
                color = Frost.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

/** Columna final «Agregar carpeta», con borde de cristal. */
@Composable
private fun AddColumn(onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .width(180.dp)
            .height(120.dp)
            .pressClick(onClick, pressedScale = 0.97f)
            .clip(shape)
            .border(1.dp, Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark)), shape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.CreateNewFolder, null, tint = Frost.TextPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text("Agregar carpeta", color = Frost.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** Fila numerada de artista (portada, nombre, conteo y posicion), como "Top Artist" de la referencia. */
@Composable
fun ArtistRow(position: Int, artist: ArtistEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.985f)
            .card(RoundedCornerShape(16.dp))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverThumb(artist.cover, 50.dp, corner = 25.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                artist.name,
                color = Frost.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                buildString {
                    append(songsLabel(artist.tracks.size))
                    if (artist.albumCount > 0) append(" · ${artist.albumCount} ${if (artist.albumCount == 1) "álbum" else "álbumes"}")
                },
                color = Frost.TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            String.format(Locale.US, "%02d", position),
            color = Frost.TextTertiary,
            fontSize = 13.sp,
            modifier = Modifier.padding(end = 4.dp),
        )
    }
}
