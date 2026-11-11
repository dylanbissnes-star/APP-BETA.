package com.example.glassplayer.data

import android.content.Context
import com.example.glassplayer.ui.theme.DefaultAccent
import androidx.compose.ui.graphics.toArgb

/** Preferencias de la persona: color de acento, modo oscuro y fondo del reproductor. */
object SettingsStore {
    private const val PREFS = "glass_prefs"
    private const val KEY_ACCENT = "accent"
    private const val KEY_DARK = "dark"
    private const val KEY_PLAYER_ACCENT_BG = "player_accent_bg"

    fun accent(context: Context): Int = prefs(context).getInt(KEY_ACCENT, DefaultAccent.toArgb())
    fun saveAccent(context: Context, argb: Int) = prefs(context).edit().putInt(KEY_ACCENT, argb).apply()

    fun dark(context: Context): Boolean = prefs(context).getBoolean(KEY_DARK, true)
    fun saveDark(context: Context, dark: Boolean) = prefs(context).edit().putBoolean(KEY_DARK, dark).apply()

    /** `true`: el reproductor se pinta con el acento; `false`: portada desenfocada. */
    fun playerAccentBg(context: Context): Boolean = prefs(context).getBoolean(KEY_PLAYER_ACCENT_BG, true)
    fun savePlayerAccentBg(context: Context, on: Boolean) =
        prefs(context).edit().putBoolean(KEY_PLAYER_ACCENT_BG, on).apply()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
