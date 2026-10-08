package de.pyxissapiens.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.pyxissapiens.core.database.entity.DataTypeEntity
import de.pyxissapiens.core.database.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataTypeDao {
    @Query("SELECT * FROM data_types ORDER BY name")
    fun observeAll(): Flow<List<DataTypeEntity>>

    @Query("SELECT * FROM data_types")
    suspend fun getAllOnce(): List<DataTypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dataType: DataTypeEntity)

    @Query("DELETE FROM data_types WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface UnitDao {
    @Query("SELECT * FROM units ORDER BY name")
    fun observeAll(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units")
    suspend fun getAllOnce(): List<UnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(unit: UnitEntity)

    @Query("DELETE FROM units WHERE id = :id")
    suspend fun delete(id: String)
}
