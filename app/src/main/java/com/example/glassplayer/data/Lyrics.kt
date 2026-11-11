package com.example.glassplayer.data

/** Una linea de letra con el instante (ms) en que empieza. */
data class LyricLine(val timeMs: Long, val text: String)

/** Letra de una cancion: sincronizada (con tiempos) y/o en texto plano. */
data class Lyrics(
    val synced: List<LyricLine>,
    val plain: String,
    val instrumental: Boolean,
) {
    val hasSynced: Boolean get() = synced.isNotEmpty()
    val isEmpty: Boolean get() = !instrumental && synced.isEmpty() && plain.isBlank()
}

/** Estado de la letra de la cancion actual. */
sealed interface LyricsState {
    val trackId: String?

    data object Idle : LyricsState { override val trackId: String? = null }
    data class Loading(override val trackId: String) : LyricsState
    data class Ready(override val trackId: String, val lyrics: Lyrics) : LyricsState
    data class Missing(override val trackId: String) : LyricsState
    data class Failed(override val trackId: String) : LyricsState
}

private val TIME_TAG = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")

/** Lee el formato LRC: `[mm:ss.xx] texto` (una linea puede traer varios tiempos). */
fun parseLrc(raw: String): List<LyricLine> {
    val out = ArrayList<LyricLine>()
    for (line in raw.lineSequence()) {
        val tags = TIME_TAG.findAll(line).toList()
        if (tags.isEmpty()) continue
        val text = line.substring(tags.last().range.last + 1).trim()
        for (m in tags) {
            val min = m.groupValues[1].toLong()
            val sec = m.groupValues[2].toLong()
            val frac = m.groupValues[3]
            val ms = when (frac.length) {
                0 -> 0L
                1 -> frac.toLong() * 100
                2 -> frac.toLong() * 10
                else -> frac.take(3).toLong()
            }
            out += LyricLine(min * 60_000 + sec * 1_000 + ms, text)
        }
    }
    return out.sortedBy { it.timeMs }
}
