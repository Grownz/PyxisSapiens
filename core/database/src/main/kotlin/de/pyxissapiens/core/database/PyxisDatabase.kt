package de.pyxissapiens.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.pyxissapiens.core.database.dao.LineworkDao
import de.pyxissapiens.core.database.dao.MeasurementDao
import de.pyxissapiens.core.database.dao.MeasurementHistoryDao
import de.pyxissapiens.core.database.dao.ProjectDao
import de.pyxissapiens.core.database.dao.SiteDao
import de.pyxissapiens.core.database.dao.TrackDao
import de.pyxissapiens.core.database.entity.LineworkEntity
import de.pyxissapiens.core.database.entity.MeasurementEntity
import de.pyxissapiens.core.database.entity.MeasurementHistoryEntity
import de.pyxissapiens.core.database.entity.ProjectEntity
import de.pyxissapiens.core.database.entity.SiteEntity
import de.pyxissapiens.core.database.entity.TrackEntity

@Database(
    entities = [
        ProjectEntity::class,
        SiteEntity::class,
        MeasurementEntity::class,
        MeasurementHistoryEntity::class,
        LineworkEntity::class,
        TrackEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class PyxisDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun siteDao(): SiteDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun measurementHistoryDao(): MeasurementHistoryDao
    abstract fun lineworkDao(): LineworkDao
    abstract fun trackDao(): TrackDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `linework` (" +
                        "`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `kind` TEXT NOT NULL, " +
                        "`geometryGeoJson` TEXT NOT NULL, `unitId` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_linework_projectId` ON `linework` (`projectId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tracks` (" +
                        "`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, " +
                        "`endedAt` INTEGER, `geometryGeoJson` TEXT NOT NULL, " +
                        "`distanceMeters` REAL NOT NULL, `durationMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tracks_projectId` ON `tracks` (`projectId`)")
            }
        }
    }
}
