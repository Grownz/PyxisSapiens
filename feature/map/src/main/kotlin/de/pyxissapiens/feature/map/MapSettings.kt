package de.pyxissapiens.feature.map

import android.content.Context
import java.io.File

/** Persists basemap settings (style URL and active offline MBTiles). */
object MapSettings {
    private const val PREFS = "pyxis_map"
    private const val KEY_STYLE = "style_url"
    private const val KEY_ACTIVE = "active_mbtiles"

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

    fun mbtilesDir(context: Context): File = File(context.filesDir, "maps")

    fun listMbtiles(context: Context): List<File> =
        mbtilesDir(context).listFiles { f -> f.isFile && f.extension == "mbtiles" }?.sortedBy { it.name } ?: emptyList()
}
