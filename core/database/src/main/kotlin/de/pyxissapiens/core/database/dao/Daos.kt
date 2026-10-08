package de.pyxissapiens.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.pyxissapiens.core.database.entity.MeasurementEntity
import de.pyxissapiens.core.database.entity.MeasurementHistoryEntity
import de.pyxissapiens.core.database.entity.ProjectEntity
import de.pyxissapiens.core.database.entity.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: String): ProjectEntity?

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites WHERE projectId = :projectId ORDER BY name")
    fun observeByProject(projectId: String): Flow<List<SiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(site: SiteEntity)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun observeBySite(siteId: String): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getById(id: String): MeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measurement: MeasurementEntity)

    @Update
    suspend fun update(measurement: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MeasurementHistoryDao {
    @Insert
    suspend fun append(entry: MeasurementHistoryEntity)

    @Query("SELECT * FROM measurement_history WHERE measurementId = :measurementId ORDER BY atEpochMillis")
    suspend fun historyOf(measurementId: String): List<MeasurementHistoryEntity>
}
