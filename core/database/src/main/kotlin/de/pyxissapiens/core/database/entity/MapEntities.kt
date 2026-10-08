package de.pyxissapiens.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A mapped line or polygon (contact, fault or outcrop area), geometry stored as GeoJSON. */
@Entity(tableName = "linework", indices = [Index("projectId")])
data class LineworkEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val kind: String,
    val geometryGeoJson: String,
    val unitId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

/** A recorded GPS track, geometry stored as a GeoJSON LineString. */
@Entity(tableName = "tracks", indices = [Index("projectId")])
data class TrackEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val geometryGeoJson: String,
    val distanceMeters: Double,
    val durationMillis: Long,
)
