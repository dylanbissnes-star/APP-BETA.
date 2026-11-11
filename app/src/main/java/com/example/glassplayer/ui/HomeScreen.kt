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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Inicio (estilo del panel de la referencia):
 *  - "Top / Tendencias": banner con degradado y botones Reproducir / Ver lista
 *  - "Tus / Carpetas": tarjetas con 3 portadas por carpeta + «Agregar carpeta»
 *  - "Tu musica / Canciones": tabla con numero, portada, tiempo y corazon
 *  - "Top / Artistas": lista numerada
 */
@Composable
fun HomeScreen(
    state: PlayerUiState,
    palette: List<Color>,
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
    val songs = remember(state.library) { state.library.take(8) }
    val currentId = state.current?.id

    LazyColumn(
        modifier.fillMaxSize(),
        // Espacio inferior para el mini reproductor y la barra de pestanas
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 168.dp),
    ) {
        item(key = "h-hero") { SectionHeader("Top", "Tendencias", seeAll = onOpenLibrary) }
        item(key = "hero") {
            HeroBanner(
                palette = palette,
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

        if (songs.isNotEmpty()) {
            item(key = "h-songs") { SectionHeader("Tu música", "Canciones", seeAll = onOpenLibrary) }
            item(key = "table-head") { TableHead() }
            itemsIndexed(songs, key = { _, t -> "s-${t.id}" }) { i, track ->
                TrackRow(
                    track = track,
                    isCurrent = track.id == currentId,
                    playing = state.isPlaying,
                    onClick = { onPlay(songs, i) },
                    index = i + 1,
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

/** Cabecera de la tabla de canciones (#, nombre, tiempo, like) como en la referencia. */
@Composable
private fun TableHead() {
    val line = Frost.Outline
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1f) }
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("#", color = Frost.TextTertiary, fontSize = 12.sp, modifier = Modifier.width(30.dp))
        Text("Canción", color = Frost.TextTertiary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("Tiempo   Like", color = Frost.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(end = 10.dp))
    }
}

/** Banner con degradado: tres focos de color (rosa arriba a la derecha, azul y violeta) sobre fondo oscuro. */
@Composable
private fun HeroBanner(
    palette: List<Color>,
    count: Int,
    scanning: Boolean,
    onPrimary: () -> Unit,
    onList: () -> Unit,
) {
    val blue = palette[3]
    val violet = palette[1]
    val pink = palette[2]
    val empty = count == 0
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF0B0B14))
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(pink.copy(alpha = 0.95f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.10f),
                        radius = size.width * 0.75f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(blue.copy(alpha = 0.90f), Color.Transparent),
                        center = Offset(size.width * 0.70f, size.height * 1.0f),
                        radius = size.width * 0.70f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(violet.copy(alpha = 0.80f), Color.Transparent),
                        center = Offset(size.width * 0.55f, size.height * 0.50f),
                        radius = size.width * 0.55f,
                    )
                )
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    if (empty) "Empieza aquí" else "Tu biblioteca · ${songsLabel(count)}",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                )
                Text(
                    if (empty) (if (scanning) "Buscando\nmúsica…" else "Agrega tu\nmúsica") else "Tu música\nde hoy",
                    color = Color.White,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroButton(
                    if (empty) "Agregar carpeta" else "Reproducir",
                    if (empty) Icons.Rounded.CreateNewFolder else Icons.Rounded.PlayArrow,
                    filled = true, onClick = onPrimary,
                )
                if (!empty) HeroButton("Ver lista", null, filled = false, onClick = onList)
            }
        }
    }
}

@Composable
private fun HeroButton(label: String, icon: ImageVector?, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val fg = if (filled) Color(0xFF111113) else Color.White
    Row(
        Modifier
            .pressClick(onClick, pressedScale = 0.95f)
            .clip(shape)
            .background(if (filled) Color.White else Color(0x59000000))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Tarjeta de carpeta: 3 portadas superpuestas, nombre, conteo y un boton para reproducirla. */
@Composable
private fun FolderCard(entry: FolderEntry, onOpen: () -> Unit, onPlay: () -> Unit) {
    val covers: List<Any?> = remember(entry.tracks) {
        val uris = entry.tracks.mapNotNull { it.artUri }.distinct().take(3)
        if (uris.isEmpty()) listOf(null, null, null) else uris
    }
    val ring = Frost.Surface
    Column(
        Modifier
            .width(158.dp)
            .height(176.dp)
            .pressClick(onOpen, pressedScale = 0.97f)
            .card(RoundedCornerShape(22.dp))
            .padding(12.dp),
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
            .card(RoundedCornerShape(22.dp), raised = true)
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
    val line = Frost.Outline
    Row(
        modifier
            .fillMaxWidth()
            .pressClick(onClick, pressedScale = 0.985f)
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1f) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverThumb(artist.cover, 50.dp, corner = 14.dp)
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
