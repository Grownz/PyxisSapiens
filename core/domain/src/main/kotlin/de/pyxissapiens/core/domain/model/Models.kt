package de.pyxissapiens.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Portable domain model (Android-free). Field names are stable identifiers used by the
 * persistence and export layers.
 */

@Serializable
enum class MeasurementKind { PLANE, LINE, BEARING }

@Serializable
enum class NorthReference { MAGNETIC, GEOGRAPHIC }

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null,
    val accuracyMeters: Float? = null,
)

/** Orientation of a planar structure. Angles in degrees. */
@Serializable
data class Attitude(
    val dip: Double,
    val dipDirection: Double,
    val northReference: NorthReference = NorthReference.GEOGRAPHIC,
)

/** Orientation of a linear structure. Angles in degrees. */
@Serializable
data class Lineation(
    val trend: Double,
    val plunge: Double,
    val northReference: NorthReference = NorthReference.GEOGRAPHIC,
)

/** Trust/quality metadata captured with a measurement. */
@Serializable
data class DataQuality(
    val score: Int,                 // 0..100
    val sensorAccuracy: Int? = null, // platform SensorManager accuracy level
    val stabilityDeg: Double? = null,
    val sampleCount: Int = 1,
    val sigmaDeg: Double? = null,
    val kappa: Double? = null,
    val declinationDeg: Double? = null,
)

@Serializable
data class Measurement(
    val id: String,
    val siteId: String,
    val kind: MeasurementKind,
    val attitude: Attitude? = null,
    val lineation: Lineation? = null,
    val bearingDeg: Double? = null,
    val location: GeoPoint? = null,
    val positionManual: Boolean = false,
    val quality: DataQuality? = null,
    val typeId: String? = null,
    val unitId: String? = null,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class Site(
    val id: String,
    val projectId: String,
    val name: String,
    val location: GeoPoint? = null,
    val description: String? = null,
)

@Serializable
data class Project(
    val id: String,
    val name: String,
    val author: String? = null,
    val description: String? = null,
    val crs: String = "WGS84",
    val createdAt: Long,
    val updatedAt: Long,
)
