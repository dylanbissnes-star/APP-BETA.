package com.example.glassplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val SOURCE_ALL = "all"
const val SOURCE_DEVICE = "device"
const val SOURCE_DEMO = "demo"

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val uri: Uri,
    val artUri: Uri? = null,
    val durationMs: Long = 0L,
    /** "device", "demo" o el URI (texto) de la carpeta de donde viene. */
    val sourceId: String = SOURCE_DEVICE,
    val album: String = "",
) {
    fun toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(artUri)
                    .setDurationMs(durationMs)
                    .build()
            )
            .build()
}

/** Reconstruye la pista a partir de lo que tiene el reproductor. */
fun MediaItem.toTrack(): Track = Track(
    id = mediaId,
    title = mediaMetadata.title?.toString() ?: "Desconocido",
    artist = mediaMetadata.artist?.toString() ?: "",
    uri = localConfiguration?.uri ?: Uri.EMPTY,
    artUri = mediaMetadata.artworkUri,
    durationMs = mediaMetadata.durationMs ?: 0L,
    sourceId = "",
    album = mediaMetadata.albumTitle?.toString() ?: "",
)

object TrackRepository {

    /** Lee la musica local del telefono (MediaStore). Requiere permiso de audio. */
    suspend fun loadLocal(context: Context): List<Track> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 30000"
        val sort = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        runCatching {
            context.contentResolver.query(collection, projection, selection, null, sort)?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumNameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (c.moveToNext() && result.size < 500) {
                    val id = c.getLong(idCol)
                    val albumId = c.getLong(albumCol)
                    result += Track(
                        id = id.toString(),
                        title = c.getString(titleCol) ?: "Desconocido",
                        artist = c.getString(artistCol)?.takeUnless { it == "<unknown>" } ?: "Artista desconocido",
                        uri = ContentUris.withAppendedId(collection, id),
                        artUri = ContentUris.withAppendedId(
                            "content://media/external/audio/albumart".toUri(), albumId
                        ),
                        durationMs = c.getLong(durCol),
                        sourceId = SOURCE_DEVICE,
                        album = c.getString(albumNameCol)?.takeUnless { it == "<unknown>" } ?: "",
                    )
                }
            }
        }
        result
    }

    /** Pistas de prueba (streaming) para ver la app funcionando sin musica local. */
    fun demo(): List<Track> = (1..4).map { n ->
        Track(
            id = "demo$n",
            title = "SoundHelix Song $n",
            artist = "SoundHelix",
            uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$n.mp3".toUri(),
            sourceId = SOURCE_DEMO,
        )
    }
}
