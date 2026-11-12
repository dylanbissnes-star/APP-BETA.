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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.glassplayer.ui.components.ActionPill
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.CoverThumb
import com.example.glassplayer.ui.components.SectionHeader
import com.example.glassplayer.ui.components.TrackRow
import com.example.glassplayer.ui.components.card
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.theme.Frost
import java.util.Locale

/**
 * Inicio (estilo Pulseplay):
 *  - Banner con la portada que se funde en el fondo, titular grande y botones
 *  - «Carpetas»: tarjetas con 3 portadas por carpeta + «Agregar carpeta»
 *  - «Favoritas»: tarjetas con portada completa y corazon
 *  - «Colección»: pildoras de cancion con tiempo y corazon
 *  - «Artistas»: lista numerada
 */
@Composable
fun HomeScreen(
    state: PlayerUiState,
    onPlayAll: () -> Unit,
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
    val folders = remember(state.library, state.folders) { folderEntries(state).filter { it.tracks.isNotEmpty() } }
    val artists = remember(state.library) { artistsOf(state.library).take(4) }
    val songs = remember(state.library) { state.library.take(6) }
    val favorites = remember(state.library, state.favorites) {
        state.library.filter { it.id in state.favorites }.take(10)
    }
    val art = state.current?.artUri ?: remember(state.library) { state.library.firstNotNullOfOrNull { it.artUri } }
    val currentId = state.current?.id

    LazyColumn(
        modifier.fillMaxSize(),
        // Espacio inferior para el mini reproductor y la barra de pestanas
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 176.dp),
    ) {
        item(key = "hero") {
            HeroBanner(
                art = art,
                count = state.library.size,
                scanning = state.scanning,
                onPrimary = if (state.library.isEmpty()) onAddFolder else onPlayAll,
                onList = onOpenLibrary,
            )
        }

        item(key = "h-folders") { SectionHeader("Tus", "Carpetas") }
        item(key = "folders") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 20.dp),
            ) {
                item(key = "add") { AddFolderCard(onAddFolder) }
                items(folders, key = { it.key }) { entry ->
                    FolderCard(entry, onOpen = { onOpenFolder(entry) }, onPlay = { onPlayFolder(entry) })
                }
            }
        }

        if (favorites.isNotEmpty()) {
            item(key = "h-favs") { SectionHeader("Lo que más te gusta", "Favoritas") }
            item(key = "favs") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 20.dp),
                ) {
                    itemsIndexed(favorites, key = { _, t -> "f-${t.id}" }) { i, track ->
                        CoverCard(track, onClick = { onPlay(favorites, i) }, onUnfavorite = { onToggleFavorite(track.id) })
                    }
                }
            }
        }

        if (songs.isNotEmpty()) {
            item(key = "h-songs") { SectionHeader("Tu música", "Colección", seeAll = onOpenLibrary) }
            itemsIndexed(songs, key = { _, t -> "s-${t.id}" }) { i, track ->
                TrackRow(
                    track = track,
                    isCurrent = track.id == currentId,
                    playing = state.isPlaying,
                    onClick = { onPlay(songs, i) },
                    favorite = track.id in state.favorites,
                    onFavorite = { onToggleFavorite(track.id) },
                )
            }
        }

        if (artists.isNotEmpty()) {
            item(key = "h-artists") { SectionHeader("Top", "Artistas", seeAll = onOpenArtists) }
            itemsIndexed(artists, key = { _, a -> "a-${a.name}" }) { i, artist ->
                ArtistRow(i + 1, artist, onClick = { onOpenArtist(artist.name) })
            }
        }
    }
}

/** Funde la imagen con el fondo: se desvanece hacia abajo y hacia la izquierda. */
private fun Modifier.fadeIntoBackground(): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            Brush.verticalGradient(0f to Color.Black, 0.5f to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            Brush.horizontalGradient(0f to Color.Transparent, 0.42f to Color.Black, 1f to Color.Black),
            blendMode = BlendMode.DstIn,
        )
    }

/**
 * Banner de inicio (como la portada de Pulseplay): la portada de la cancion se funde con el
 * degradado de fondo y un titular grande y ligero va encima, con los botones debajo.
 */
@Composable
private fun HeroBanner(
    art: Any?,
    count: Int,
    scanning: Boolean,
    onPrimary: () -> Unit,
    onList: () -> Unit,
) {
    val empty = count == 0
    Box(
        Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        if (art != null) {
            AsyncImage(
                model = art,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.9f)
                    .height(280.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .fadeIntoBackground(),
            )
        } else {
            // Sin portada: un resplandor suave del color del tema
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(260.dp)
                    .background(Brush.radialGradient(listOf(Frost.content(0.22f), Color.Transparent)))
            )
        }
        Column(Modifier.align(Alignment.BottomStart)) {
            Text(
                if (empty) "Empieza aquí" else "Tu biblioteca · ${songsLabel(count)}",
                color = Frost.TextSecondary,
                fontSize = 13.sp,
            )
            Text(
                if (empty) (if (scanning) "BUSCANDO\nMÚSICA…" else "AGREGA\nTU MÚSICA") else "TU MÚSICA",
                color = Frost.TextPrimary,
                fontSize = 46.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPill(
                    if (empty) "Agregar carpeta" else "Reproducir",
                    onPrimary,
                    icon = if (empty) Icons.Rounded.CreateNewFolder else Icons.Rounded.PlayArrow,
                    primary = true,
                )
                if (!empty) ActionPill("Ver lista", onList)
            }
        }
    }
}

/** Tarjeta con la portada a pantalla completa, titulo y artista encima y corazon (como "Top Songs"). */
@Composable
private fun CoverCard(track: Track, onClick: () -> Unit, onUnfavorite: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        Modifier
            .width(168.dp)
            .height(212.dp)
            .pressClick(onClick, pressedScale = 0.97f)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Frost.SurfaceHigh, Frost.Surface))),
    ) {
        if (track.artUri != null) {
            AsyncImage(
                model = track.artUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Rounded.MusicNote, null, tint = Frost.TextTertiary,
                modifier = Modifier.align(Alignment.Center).size(48.dp),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.72f)))
        )
        Column(Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 54.dp, bottom = 14.dp)) {
            Text(
                track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .size(36.dp)
                .pressClick(onUnfavorite, pressedScale = 0.88f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.24f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Favorite, "Quitar de favoritos", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

/** Tarjeta de carpeta: 3 portadas superpuestas, nombre, conteo y un boton para reproducirla. */
@Composable
private fun FolderCard(entry: FolderEntry, onOpen: () -> Unit, onPlay: () -> Unit) {
    val covers: List<Any?> = remember(entry.tracks) {
        val uris = entry.tracks.mapNotNull { it.artUri }.distinct().take(3)
        if (uris.isEmpty()) listOf(null, null, null) else uris
    }
    val ring = Frost.Floating
    Column(
        Modifier
            .width(158.dp)
            .height(176.dp)
            .pressClick(onOpen, pressedScale = 0.97f)
            .card(RoundedCornerShape(28.dp))
            .padding(14.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(84.dp)) {
            covers.forEachIndexed { i, art ->
                CoverThumb(
                    art, 62.dp, corner = 14.dp,
                    modifier = Modifier
                        .offset(x = (i * 30).dp, y = if (i == 1) 0.dp else 12.dp)
                        .border(2.dp, ring, RoundedCornerShape(14.dp)),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            entry.label,
            color = Frost.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                songsLabel(entry.tracks.size),
                color = Frost.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Box(
                Modifier
                    .size(34.dp)
                    .pressClick(onPlay, pressedScale = 0.9f)
                    .clip(CircleShape)
                    .background(Frost.AccentFill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, "Reproducir carpeta", tint = Frost.OnAccent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Primera tarjeta de la fila: agregar una carpeta del telefono (siempre visible). */
@Composable
private fun AddFolderCard(onClick: () -> Unit) {
    Column(
        Modifier
            .width(158.dp)
            .height(176.dp)
            .pressClick(onClick, pressedScale = 0.97f)
            .card(RoundedCornerShape(28.dp), raised = true)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Frost.Accent.copy(alpha = 0.20f))
                .border(1.dp, Frost.Accent.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Add, "Agregar carpeta", tint = Frost.Accent, modifier = Modifier.size(28.dp))
        }
        Text(
            "Agregar carpeta",
            color = Frost.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "de tu teléfono",
            color = Frost.TextSecondary,
            fontSize = 12.sp,
        )
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
            .card(RoundedCornerShape(22.dp))
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
