package de.pyxissapiens.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.database.PyxisDatabase
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Permanently erases all project data, including georeferenced media and exports. */
@Singleton
class DataEraser @Inject constructor(
    private val db: PyxisDatabase,
    @ApplicationContext private val context: Context,
) {
    suspend fun eraseAll() {
        db.measurementHistoryDao().clear()
        db.measurementDao().clear()
        db.siteDao().clear()
        db.projectDao().clear()
        db.lineworkDao().clear()
        db.trackDao().clear()
        listOf("projects", "exports").forEach { name ->
            File(context.getExternalFilesDir(null) ?: context.filesDir, name).deleteRecursively()
            File(context.filesDir, name).deleteRecursively()
        }
    }
}
