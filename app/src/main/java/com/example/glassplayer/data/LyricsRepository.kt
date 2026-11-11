package com.example.glassplayer.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Busca letras en LRCLIB (https://lrclib.net, gratuito y sin clave) y las guarda en el telefono,
 * asi cada cancion se descarga una sola vez y despues funciona sin internet.
 */
object LyricsRepository {
    private const val BASE = "https://lrclib.net/api"
    private const val USER_AGENT = "GlassPlayer (https://github.com/dylanbissnes-star/APP-BETA.)"
    private const val MISSING_TTL_MS = 7L * 24 * 3600 * 1000

    /** Devuelve la letra, o `null` si no existe. Lanza IOException si falla la red. */
    suspend fun load(context: Context, track: Track, force: Boolean = false): Lyrics? =
        withContext(Dispatchers.IO) {
            val file = cacheFile(context, track)
            if (!force) readCache(file)?.let { return@withContext it.value }

            val found = fetch(track)
            writeCache(file, found)
            found
        }

    /** Borra todas las letras guardadas. */
    suspend fun clearCache(context: Context) = withContext(Dispatchers.IO) {
        cacheDir(context).listFiles()?.forEach { it.delete() }
    }

    // ---- red ----

    private fun fetch(track: Track): Lyrics? {
        val artist = track.artist.takeUnless { it.isBlank() || it.contains("unknown", ignoreCase = true) }
        val seconds = (track.durationMs / 1000).toInt()

        // 1) Coincidencia exacta (titulo + artista + album + duracion)
        if (artist != null) {
            val q = buildString {
                append("track_name=").append(enc(track.title))
                append("&artist_name=").append(enc(artist))
                if (track.album.isNotBlank()) append("&album_name=").append(enc(track.album))
                if (seconds > 0) append("&duration=").append(seconds)
            }
            get("$BASE/get?$q")?.let { return parseOne(JSONObject(it)) }
        }

        // 2) Busqueda mas flexible; se elige la que tenga letra y la duracion mas cercana
        val q = if (artist != null) "track_name=${enc(track.title)}&artist_name=${enc(artist)}"
        else "q=${enc(track.title)}"
        val body = get("$BASE/search?$q") ?: return null
        val list = JSONArray(body)
        var best: JSONObject? = null
        var bestScore = Int.MAX_VALUE
        for (i in 0 until list.length()) {
            val o = list.getJSONObject(i)
            val hasText = !o.isNull("syncedLyrics") || !o.isNull("plainLyrics") || o.optBoolean("instrumental")
            if (!hasText) continue
            val diff = if (seconds > 0 && !o.isNull("duration")) abs(o.optDouble("duration") - seconds).toInt() else 0
            val score = diff + if (o.isNull("syncedLyrics")) 3 else 0 // se prefiere la sincronizada
            if (score < bestScore) { best = o; bestScore = score }
        }
        // Una duracion muy distinta suele ser otra version de la cancion
        if (best == null || (seconds > 0 && bestScore > 20)) return null
        return parseOne(best)
    }

    private fun parseOne(o: JSONObject): Lyrics? {
        val synced = if (o.isNull("syncedLyrics")) "" else o.getString("syncedLyrics")
        val plain = if (o.isNull("plainLyrics")) "" else o.getString("plainLyrics")
        val lyrics = Lyrics(parseLrc(synced), plain.trim(), o.optBoolean("instrumental"))
        return lyrics.takeUnless { it.isEmpty }
    }

    /** GET que devuelve el cuerpo, `null` si es 404 y lanza IOException en otros fallos. */
    private fun get(url: String): String? {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 8_000
            c.readTimeout = 10_000
            c.setRequestProperty("User-Agent", USER_AGENT)
            val code = c.responseCode
            if (code == 404) return null
            if (code !in 200..299) throw IOException("HTTP $code")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    // ---- cache en disco ----

    private class Cached(val value: Lyrics?)

    private fun cacheDir(context: Context) = File(context.filesDir, "lyrics").apply { mkdirs() }

    private fun cacheFile(context: Context, track: Track): File {
        val hash = MessageDigest.getInstance("SHA-1").digest(track.id.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir(context), "$hash.json")
    }

    private fun readCache(file: File): Cached? {
        if (!file.exists()) return null
        return try {
            val o = JSONObject(file.readText())
            if (!o.optBoolean("found")) {
                // «No encontrada» solo se recuerda una semana: puede que luego la agreguen
                if (System.currentTimeMillis() - file.lastModified() > MISSING_TTL_MS) null else Cached(null)
            } else {
                Cached(Lyrics(parseLrc(o.optString("synced")), o.optString("plain"), o.optBoolean("instrumental")))
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun writeCache(file: File, lyrics: Lyrics?) {
        val o = JSONObject()
        if (lyrics == null) {
            o.put("found", false)
        } else {
            o.put("found", true)
            o.put("plain", lyrics.plain)
            o.put("instrumental", lyrics.instrumental)
            // Se guarda el LRC original reconstruido (tiempos + texto)
            o.put("synced", lyrics.synced.joinToString("\n") { "[${stamp(it.timeMs)}]${it.text}" })
        }
        runCatching { file.writeText(o.toString()) }
    }

    private fun stamp(ms: Long): String {
        val m = ms / 60_000
        val s = (ms / 1000) % 60
        val cs = (ms % 1000) / 10
        return "%02d:%02d.%02d".format(m, s, cs)
    }
}
