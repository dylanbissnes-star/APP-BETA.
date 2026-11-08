package com.example.glassplayer.ui

import android.net.Uri
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.SOURCE_DEMO
import com.example.glassplayer.data.SOURCE_DEVICE
import com.example.glassplayer.data.Track

/** Una "carpeta" de musica: toda la musica, el dispositivo, la demo o una carpeta agregada. */
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

/** Un artista con todas sus canciones. */
data class ArtistEntry(val name: String, val tracks: List<Track>) {
    val cover: Uri? get() = tracks.firstNotNullOfOrNull { it.artUri }
    val albumCount: Int get() = tracks.map { it.album }.filter { it.isNotBlank() }.distinct().size
}

/** Un album con sus canciones. */
data class AlbumEntry(val name: String, val artist: String, val tracks: List<Track>) {
    val cover: Uri? get() = tracks.firstNotNullOfOrNull { it.artUri }
}

/** Artistas de la biblioteca, de mas a menos canciones. */
fun artistsOf(library: List<Track>): List<ArtistEntry> =
    library.filter { it.artist.isNotBlank() }
        .groupBy { it.artist }
        .map { (name, tracks) -> ArtistEntry(name, tracks) }
        .sortedWith(compareByDescending<ArtistEntry> { it.tracks.size }.thenBy { it.name.lowercase() })

/** Albumes de la biblioteca, por orden alfabetico. */
fun albumsOf(library: List<Track>): List<AlbumEntry> =
    library.filter { it.album.isNotBlank() }
        .groupBy { it.album }
        .map { (name, tracks) ->
            val artists = tracks.map { it.artist }.filter { it.isNotBlank() }.distinct()
            AlbumEntry(name, artists.singleOrNull() ?: if (artists.isEmpty()) "" else "Varios artistas", tracks)
        }
        .sortedBy { it.name.lowercase() }

fun songsLabel(n: Int): String = if (n == 1) "1 canción" else "$n canciones"
