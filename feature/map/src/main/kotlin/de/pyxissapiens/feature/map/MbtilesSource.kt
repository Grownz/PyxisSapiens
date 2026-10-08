package de.pyxissapiens.feature.map

import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Minimal read-only MBTiles reader. MBTiles is an SQLite database with a
 * `tiles(zoom_level, tile_column, tile_row, tile_data)` table (tile_row is TMS/XYZ-flipped)
 * and a `metadata` table.
 *
 * Import currently stores the file and reports its size. Rendering the tiles through MapLibre
 * requires a custom tile provider and is a follow-up (documented in docs/10).
 */
class MbtilesSource(private val file: File) {

    private fun open(): SQLiteDatabase? =
        runCatching { SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY) }.getOrNull()

    fun tileCount(): Int {
        val db = open() ?: return 0
        return db.use {
            it.rawQuery("SELECT COUNT(*) FROM tiles", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
        }
    }

    fun metadata(): Map<String, String> {
        val db = open() ?: return emptyMap()
        return db.use {
            val result = LinkedHashMap<String, String>()
            runCatching {
                it.rawQuery("SELECT name, value FROM metadata", null).use { cursor ->
                    while (cursor.moveToNext()) result[cursor.getString(0)] = cursor.getString(1)
                }
            }
            result
        }
    }

    /** Returns tile bytes for XYZ coordinates (converted to TMS internally). */
    fun tile(z: Int, x: Int, y: Int): ByteArray? {
        val db = open() ?: return null
        val tmsY = (1 shl z) - 1 - y
        return db.use {
            it.rawQuery(
                "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?",
                arrayOf(z.toString(), x.toString(), tmsY.toString()),
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getBlob(0) else null
            }
        }
    }

    private inline fun <T> SQLiteDatabase.use(block: (SQLiteDatabase) -> T): T =
        try { block(this) } finally { close() }
}
