package com.example.glassplayer.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.SOURCE_DEMO
import com.example.glassplayer.data.SOURCE_DEVICE
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.FolderShape
import com.example.glassplayer.ui.components.backdropBlur
import com.example.glassplayer.ui.components.backdropSource
import com.example.glassplayer.ui.components.pressClick
import com.example.glassplayer.ui.components.rememberBackdropState
import com.example.glassplayer.ui.theme.Frost
import kotlin.math.abs

/** Una "carpeta" del menu principal: toda la musica, el dispositivo, la demo o una carpeta agregada. */
data class FolderEntry(val key: String, val label: String, val tracks: List<Track>)

fun folderEntries(state: PlayerUiState): List<FolderEntry> {
    val bySource = state.library.groupBy { it.sourceId }
    return buildList {
        if (state.library.isNotEmpty()) add(FolderEntry(SOURCE_ALL, "Toda la música", state.library))
        bySource[SOURCE_DEVICE]?.let { add(FolderEntry(SOURCE_DEVICE, "Dispositivo", it)) }
        bySource[SOURCE_DEMO]?.let { add(FolderEntry(SOURCE_DEMO, "Demo", it)) }
        state.folders.forEach { add(FolderEntry(it.uri, it.name, bySource[it.uri].orEmpty())) }
    }
}

/**
 * Menu principal: una carpeta de cristal por cada carpeta de musica, con las portadas de 3 albumes
 * asomando por detras, las primeras canciones dentro y un boton de nota musical para reproducirla.
 * Desliza de lado para cambiar de carpeta; toca la carpeta para abrir su listado.
 * El boton «Agregar carpeta» queda siempre visible debajo.
 */
@Composable
fun HomeMenu(
    state: PlayerUiState,
    onOpenFolder: (FolderEntry) -> Unit,
    onPlayFolder: (FolderEntry) -> Unit,
    onAddFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(state.library, state.folders, state.scanning) {
        folderEntries(state).ifEmpty {
            listOf(FolderEntry(SOURCE_ALL, if (state.scanning) "Buscando música…" else "Sin música", emptyList()))
        }
    }
    val pagerState = rememberPagerState { entries.size }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 156.dp), // espacio para el mini reproductor y el dock
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(10.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(392.dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            pageSpacing = 10.dp,
        ) { page ->
            val entry = entries[page]
            val dist = abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceAtMost(1f)
            FolderPage(
                entry = entry,
                onOpen = { onOpenFolder(entry) },
                onPlay = { if (entry.tracks.isNotEmpty()) onPlayFolder(entry) },
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - 0.55f * dist
                    val s = 1f - 0.06f * dist
                    scaleX = s
                    scaleY = s
                },
            )
        }

        // Indicador de pagina
        if (entries.size > 1) {
            Row(
                Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(entries.size) { i ->
                    val selected = i == pagerState.currentPage
                    val w by animateDpAsState(if (selected) 20.dp else 6.dp, label = "dot")
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(w)
                            .clip(CircleShape)
                            .background(Frost.content(if (selected) 0.85f else 0.25f))
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        AddFolderButton(onAddFolder)
        Text(
            "Elige una carpeta de tu teléfono y sus álbumes aparecen aquí",
            style = MaterialTheme.typography.labelSmall,
            color = Frost.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp, start = 32.dp, end = 32.dp),
        )
    }
}

@Composable
private fun FolderPage(
    entry: FolderEntry,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FolderIllustration(entry, onOpen, onPlay)
        Spacer(Modifier.height(20.dp))
        Text(
            entry.label.uppercase(),
            color = Frost.TextPrimary,
            fontSize = if (entry.label.length > 14) 26.sp else 34.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (entry.tracks.isEmpty()) "0 canciones" else "${entry.tracks.size} canciones",
            color = Frost.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Frost.content(0.08f))
                .padding(horizontal = 20.dp, vertical = 7.dp),
        )
    }
}

/**
 * La carpeta de la referencia: dos placas grises y tres portadas con marco blanco (la central al
 * frente, las laterales inclinadas) asomando detras de un frente de cristal con pestana, que
 * desenfoca las portadas y muestra las primeras canciones y un boton redondo con una nota.
 */
@Composable
private fun FolderIllustration(
    entry: FolderEntry,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    val onDark = Frost.OnDark
    val covers: List<Any> = remember(entry.tracks) {
        entry.tracks.mapNotNull { it.artUri }.distinct().take(3)
    }
    val art: List<Any?> = if (covers.isEmpty()) listOf(null, null, null) else List(3) { covers[it % covers.size] }
    val shape = remember { FolderShape() }
    val backdrop = rememberBackdropState()

    BoxWithConstraints(
        Modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .height(280.dp)
            .pressClick(onOpen, pressedScale = 0.98f),
    ) {
        val w = maxWidth
        val h = maxHeight
        val plate = if (onDark) Color(0xFF2B2B31) else Color(0xFFE2E2E5)

        // Todo lo que queda DETRAS del frente: el frente lo desenfoca
        Box(Modifier.fillMaxSize().backdropSource(backdrop)) {
            Box(
                Modifier
                    .offset(w * 0.12f, h * 0.02f)
                    .size(w * 0.40f, h * 0.42f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(plate)
            )
            Box(
                Modifier
                    .offset(w * 0.60f, h * 0.09f)
                    .size(w * 0.30f, h * 0.40f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(plate)
            )
            MiniCover(art[1], w * 0.34f, Modifier.offset(w * 0.02f, h * 0.10f).rotate(-12f))
            MiniCover(art[2], w * 0.30f, Modifier.offset(w * 0.66f, h * 0.14f).rotate(10f))
            MiniCover(art[0], w * 0.42f, Modifier.offset(w * 0.29f, 0.dp))
        }

        // Frente de cristal con pestana
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .width(w * 0.80f)
                .height(h * 0.58f)
                .shadow(16.dp, shape, ambientColor = Color(0x55000000), spotColor = Color(0x55000000))
                .clip(shape)
                .backdropBlur(backdrop, 18.dp)
                .background(
                    Brush.verticalGradient(
                        if (onDark) listOf(Color(0xEB5A5A62), Color(0xF02A2A30), Color(0xFF17171B))
                        else listOf(Color(0xEB7C7C82), Color(0xF0C9C9CE), Color(0xFFFDFDFE))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.55f), shape),
        ) {
            // Primeras canciones (titulo y artista, como en la referencia)
            Column(
                Modifier.padding(start = 20.dp, top = 24.dp, end = 72.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                val lines = entry.tracks.take(3)
                if (lines.isEmpty()) {
                    FolderLine("Sin canciones", "Agrega una carpeta")
                } else {
                    lines.forEach { FolderLine(it.title, it.artist) }
                }
            }

            Text(
                "*${entry.label}",
                color = if (onDark) Color(0xFFA0A0A8) else Color(0xFF6E6E73),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 14.dp, end = 76.dp),
            )

            // Boton redondo con la nota: reproduce la carpeta
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 12.dp)
                    .size(48.dp)
                    .pressClick(onPlay, pressedScale = 0.9f)
                    .shadow(8.dp, CircleShape, ambientColor = Color(0x66000000), spotColor = Color(0x66000000))
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2C))
                    .border(3.dp, if (onDark) Color(0x40FFFFFF) else Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MusicNote, "Reproducir carpeta", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun FolderLine(title: String, artist: String) {
    Column {
        Text(
            title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            artist,
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Portada con marco blanco y sombra (tarjeta que asoma de la carpeta). */
@Composable
private fun MiniCover(art: Any?, width: Dp, modifier: Modifier = Modifier) {
    val outer = RoundedCornerShape(17.dp)
    Box(
        modifier
            .size(width, width * 1.3f)
            .shadow(10.dp, outer, ambientColor = Color(0x55000000), spotColor = Color(0x55000000))
            .background(Color.White, outer)
            .padding(5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFB69CFF), Color(0xFF6C7BFF)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.MusicNote, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(30.dp))
        if (art != null) {
            AsyncImage(
                model = art,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/** Boton principal y siempre visible para traer musica de una carpeta del telefono. */
@Composable
private fun AddFolderButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .pressClick(onClick, pressedScale = 0.95f)
            .shadow(10.dp, shape, ambientColor = Frost.Tint, spotColor = Frost.Tint)
            .clip(shape)
            .background(Frost.TextPrimary)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.CreateNewFolder, null, tint = Frost.Inverse, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Agregar carpeta", color = Frost.Inverse, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
