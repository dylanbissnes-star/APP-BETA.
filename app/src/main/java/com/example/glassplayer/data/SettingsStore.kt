package com.example.glassplayer.data

import android.content.Context
import com.example.glassplayer.ui.theme.DefaultAccent
import androidx.compose.ui.graphics.toArgb

/** Preferencias de la persona: color de acento, modo oscuro y fondo. */
object SettingsStore {
    private const val PREFS = "glass_prefs"
    private const val KEY_ACCENT = "accent"
    private const val KEY_DARK = "dark"
    private const val KEY_WALLPAPER = "wallpaper"

    fun accent(context: Context): Int = prefs(context).getInt(KEY_ACCENT, DefaultAccent.toArgb())
    fun saveAccent(context: Context, argb: Int) = prefs(context).edit().putInt(KEY_ACCENT, argb).apply()

    fun dark(context: Context): Boolean = prefs(context).getBoolean(KEY_DARK, true)
    fun saveDark(context: Context, dark: Boolean) = prefs(context).edit().putBoolean(KEY_DARK, dark).apply()

    /** Fondo de la app: 0 = tela negra, 1 = bruma, 2 = portada de la cancion (desenfocada). */
    fun wallpaper(context: Context): Int = prefs(context).getInt(KEY_WALLPAPER, 0)
    fun saveWallpaper(context: Context, value: Int) = prefs(context).edit().putInt(KEY_WALLPAPER, value).apply()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
