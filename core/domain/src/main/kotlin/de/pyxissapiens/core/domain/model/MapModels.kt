package de.pyxissapiens.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class LineworkKind { CONTACT, FAULT, POLYGON }

/** A mapped line or polygon (contact, fault or outcrop area). */
@Serializable
data class Linework(
    val id: String,
    val projectId: String,
    val kind: LineworkKind,
    val coordinates: List<GeoPoint>,
    val unitId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/** A recorded GPS track. */
@Serializable
data class Track(
    val id: String,
    val projectId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationMillis: Long,
)
