package de.pyxissapiens.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.pyxissapiens.core.database.entity.LineworkEntity
import de.pyxissapiens.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineworkDao {
    @Query("SELECT * FROM linework ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LineworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(linework: LineworkEntity)

    @Query("DELETE FROM linework WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun delete(id: String)
}
