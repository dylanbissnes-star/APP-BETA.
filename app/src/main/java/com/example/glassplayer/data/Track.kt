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

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val uri: Uri,
    val artUri: Uri? = null,
    val durationMs: Long = 0L,
) {
    fun toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(artUri)
                    .build()
            )
            .build()
}

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
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (c.moveToNext() && result.size < 200) {
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
                    )
                }
            }
        }
        result
    }

    /** Pistas de prueba (streaming) para ver la app funcionando sin musica local. */
    fun demo(): List<Track> = listOf(
        Track("demo1", "SoundHelix Song 1", "SoundHelix",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3".toUri()),
        Track("demo2", "SoundHelix Song 2", "SoundHelix",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3".toUri()),
        Track("demo3", "SoundHelix Song 3", "SoundHelix",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3".toUri()),
        Track("demo4", "SoundHelix Song 4", "SoundHelix",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3".toUri()),
    )
}
