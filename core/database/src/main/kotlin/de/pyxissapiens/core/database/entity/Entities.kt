package de.pyxissapiens.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val author: String?,
    val description: String?,
    val crs: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "sites", indices = [Index("projectId")])
data class SiteEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val altitudeMeters: Double?,
    val accuracyMeters: Float?,
    val description: String?,
)

@Entity(tableName = "measurements", indices = [Index("siteId"), Index("kind")])
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val siteId: String,
    val kind: String,
    val dip: Double?,
    val dipDirection: Double?,
    val trend: Double?,
    val plunge: Double?,
    val bearing: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val altitudeMeters: Double?,
    val accuracyMeters: Float?,
    val positionManual: Boolean,
    val qualityScore: Int?,
    val sensorAccuracy: Int?,
    val stabilityDeg: Double?,
    val sampleCount: Int,
    val sigmaDeg: Double?,
    val kappa: Double?,
    val typeId: String?,
    val unitId: String?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Immutable edit trail powering undo/redo and audit (docs/03 §5.1). */
@Entity(tableName = "measurement_history", indices = [Index("measurementId")])
data class MeasurementHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measurementId: String,
    val field: String,
    val oldValue: String?,
    val newValue: String?,
    val atEpochMillis: Long,
)
