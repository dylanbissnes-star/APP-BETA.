package com.example.glassplayer.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri

data class Folder(val uri: String, val name: String)

/**
 * Guarda las carpetas que el usuario eligio (Storage Access Framework).
 * El permiso de lectura se hace persistente para que sigan funcionando al reiniciar.
 */
object FolderStore {
    private const val PREFS = "glass_prefs"
    private const val KEY = "folders"

    fun load(context: Context): List<Folder> =
        prefs(context).getStringSet(KEY, emptySet()).orEmpty()
            .map { Folder(it, nameFor(it.toUri())) }
            .sortedBy { it.name.lowercase() }

    fun add(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val set = prefs(context).getStringSet(KEY, emptySet()).orEmpty().toMutableSet()
        set += uri.toString()
        prefs(context).edit().putStringSet(KEY, set).apply()
    }

    fun remove(context: Context, uriString: String) {
        val set = prefs(context).getStringSet(KEY, emptySet()).orEmpty().toMutableSet()
        set -= uriString
        prefs(context).edit().putStringSet(KEY, set).apply()
    }

    /** "primary:Music/Rock" -> "Rock" */
    fun nameFor(uri: Uri): String {
        val id = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return "Carpeta"
        val last = id.substringAfter(':', id).trimEnd('/').substringAfterLast('/')
        return last.ifBlank { "Almacenamiento" }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
