package com.example.glassplayer.data

import android.content.Context

/** Guarda los ids de las canciones marcadas con el corazon (persisten al reiniciar). */
object FavoriteStore {
    private const val PREFS = "glass_prefs"
    private const val KEY = "favorites"

    fun load(context: Context): Set<String> =
        prefs(context).getStringSet(KEY, emptySet()).orEmpty().toSet()

    fun save(context: Context, ids: Set<String>) {
        prefs(context).edit().putStringSet(KEY, ids).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
