package de.pyxissapiens.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.pyxissapiens.core.database.dao.MeasurementDao
import de.pyxissapiens.core.database.dao.MeasurementHistoryDao
import de.pyxissapiens.core.database.dao.ProjectDao
import de.pyxissapiens.core.database.dao.SiteDao
import de.pyxissapiens.core.database.entity.MeasurementEntity
import de.pyxissapiens.core.database.entity.MeasurementHistoryEntity
import de.pyxissapiens.core.database.entity.ProjectEntity
import de.pyxissapiens.core.database.entity.SiteEntity

@Database(
    entities = [
        ProjectEntity::class,
        SiteEntity::class,
        MeasurementEntity::class,
        MeasurementHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PyxisDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun siteDao(): SiteDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun measurementHistoryDao(): MeasurementHistoryDao
}
