package com.example.glassplayer.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Recorre una carpeta elegida por el usuario (incluidas subcarpetas) y devuelve
 * las canciones que encuentra, leyendo titulo, artista, duracion y portada incrustada.
 */
object FolderScanner {
    private val audioExtensions = setOf("mp3", "m4a", "aac", "wav", "flac", "ogg", "opus", "wma", "amr")
    private const val MAX_TRACKS = 600
    private const val MAX_DEPTH = 8

    suspend fun scan(context: Context, treeUri: Uri): List<Track> = withContext(Dispatchers.IO) {
        val rootId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return@withContext emptyList()
        val files = mutableListOf<Pair<Uri, String>>() // uri del archivo, nombre

        fun walk(docId: String, depth: Int) {
            if (depth > MAX_DEPTH || files.size >= MAX_TRACKS) return
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
            runCatching {
                context.contentResolver.query(
                    children,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                    ),
                    null, null, null,
                )?.use { c ->
                    while (c.moveToNext() && files.size < MAX_TRACKS) {
                        val id = c.getString(0) ?: continue
                        val name = c.getString(1) ?: ""
                        val mime = c.getString(2) ?: ""
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            walk(id, depth + 1)
                        } else if (mime.startsWith("audio/") ||
                            name.substringAfterLast('.', "").lowercase() in audioExtensions
                        ) {
                            files += DocumentsContract.buildDocumentUriUsingTree(treeUri, id) to name
                        }
                    }
                }
            }
        }

        walk(rootId, 0)

        val sourceId = treeUri.toString()
        files
            .map { (uri, name) -> readTrack(context, uri, name, sourceId) }
            .sortedBy { it.title.lowercase() }
    }

    private fun readTrack(context: Context, uri: Uri, fileName: String, sourceId: String): Track {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration = 0L
        var art: Uri? = null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            retriever.embeddedPicture?.let { art = saveArt(context, uri, it) }
        } catch (_: Exception) {
            // Archivo sin metadatos legibles: se usa el nombre del archivo.
        } finally {
            runCatching { retriever.release() }
        }

        return Track(
            id = uri.toString(),
            title = title?.takeIf { it.isNotBlank() } ?: fileName.substringBeforeLast('.'),
            artist = artist?.takeIf { it.isNotBlank() } ?: "Artista desconocido",
            uri = uri,
            artUri = art,
            durationMs = duration,
            sourceId = sourceId,
            album = album?.takeIf { it.isNotBlank() } ?: "",
        )
    }

    /** Guarda la portada incrustada en cache para poder mostrarla con Coil. */
    private fun saveArt(context: Context, uri: Uri, bytes: ByteArray): Uri? = runCatching {
        val dir = File(context.cacheDir, "art").apply { mkdirs() }
        val file = File(dir, "${uri.toString().hashCode()}.jpg")
        if (!file.exists()) file.writeBytes(bytes)
        file.toUri()
    }.getOrNull()
}
