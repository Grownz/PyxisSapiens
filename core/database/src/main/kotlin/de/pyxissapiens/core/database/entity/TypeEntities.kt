package de.pyxissapiens.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** User-defined measurement data type (e.g. bedding, cleavage, fault). */
@Entity(tableName = "data_types")
data class DataTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val symbol: String,
)

/** Stratigraphic / rock unit. */
@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String?,
    val code: String?,
)
