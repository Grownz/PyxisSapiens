package de.pyxissapiens.feature.map

import android.content.Context
import java.io.File

/** Persists basemap settings (style URL and active offline MBTiles). */
object MapSettings {
    private const val PREFS = "pyxis_map"
    private const val KEY_STYLE = "style_url"
    private const val KEY_ACTIVE = "active_mbtiles"
    private const val KEY_GEOLOGY = "geology_url"
    private const val KEY_GEOLOGY_ENABLED = "geology_enabled"

    fun styleUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_STYLE, null) ?: DEFAULT_STYLE_URL

    fun setStyleUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_STYLE, url.ifBlank { DEFAULT_STYLE_URL }).apply()
    }

    fun activeMbtiles(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACTIVE, null)

    fun setActiveMbtiles(context: Context, path: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE, path).apply()
    }

    fun geologyUrl(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_GEOLOGY, null)

    fun setGeologyUrl(context: Context, url: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_GEOLOGY, url?.ifBlank { null }).apply()
    }

    fun geologyEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_GEOLOGY_ENABLED, false)

    fun setGeologyEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_GEOLOGY_ENABLED, enabled).apply()
    }

    fun mbtilesDir(context: Context): File = File(context.filesDir, "maps")

    fun listMbtiles(context: Context): List<File> =
        mbtilesDir(context).listFiles { f -> f.isFile && f.extension == "mbtiles" }?.sortedBy { it.name } ?: emptyList()
}
