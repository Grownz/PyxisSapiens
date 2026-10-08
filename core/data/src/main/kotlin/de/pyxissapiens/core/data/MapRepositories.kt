package de.pyxissapiens.core.data

import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.database.entity.LineworkEntity
import de.pyxissapiens.core.database.entity.TrackEntity
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Linework
import de.pyxissapiens.core.domain.model.LineworkKind
import de.pyxissapiens.core.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Minimal GeoJSON helpers for line/polygon geometry (coordinates only). */
object GeoJsonGeom {

    fun toGeoJson(kind: LineworkKind, coords: List<GeoPoint>): String {
        val ring = coords.joinToString(",") { "[" + it.longitude + "," + it.latitude + coordinateZ(it) + "]" }
        return if (kind == LineworkKind.POLYGON) {
            """{"type":"Polygon","coordinates":[[$ring]]}"""
        } else {
            """{"type":"LineString","coordinates":[$ring]}"""
        }
    }

    private fun coordinateZ(p: GeoPoint): String = p.altitudeMeters?.let { ",$it" } ?: ""

    private val pair = Regex("\\[\\s*(-?[0-9.]+)\\s*,\\s*(-?[0-9.]+)\\s*(?:,\\s*(-?[0-9.]+)\\s*)?\\]")

    fun parse(json: String): List<GeoPoint> =
        pair.findAll(json).map { m ->
            GeoPoint(
                longitude = m.groupValues[1].toDouble(),
                latitude = m.groupValues[2].toDouble(),
                altitudeMeters = m.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }?.toDouble(),
            )
        }.toList()
}

@Singleton
class LineworkRepository @Inject constructor(private val db: PyxisDatabase) {

    fun observeAll(): Flow<List<Linework>> =
        db.lineworkDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun upsert(linework: Linework) = db.lineworkDao().upsert(linework.toEntity())

    suspend fun delete(id: String) = db.lineworkDao().delete(id)

    private fun Linework.toEntity() = LineworkEntity(
        id = id, projectId = projectId, kind = kind.name,
        geometryGeoJson = GeoJsonGeom.toGeoJson(kind, coordinates),
        unitId = unitId, createdAt = createdAt, updatedAt = updatedAt,
    )

    private fun LineworkEntity.toDomain() = Linework(
        id = id, projectId = projectId,
        kind = runCatching { LineworkKind.valueOf(kind) }.getOrDefault(LineworkKind.CONTACT),
        coordinates = GeoJsonGeom.parse(geometryGeoJson),
        unitId = unitId, createdAt = createdAt, updatedAt = updatedAt,
    )
}

@Singleton
class TrackRepository @Inject constructor(private val db: PyxisDatabase) {

    fun observeAll(): Flow<List<Track>> =
        db.trackDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun upsert(track: Track) = db.trackDao().upsert(track.toEntity())

    suspend fun delete(id: String) = db.trackDao().delete(id)

    private fun Track.toEntity() = TrackEntity(
        id = id, projectId = projectId, startedAt = startedAt, endedAt = endedAt,
        geometryGeoJson = GeoJsonGeom.toGeoJson(LineworkKind.CONTACT, points),
        distanceMeters = distanceMeters, durationMillis = durationMillis,
    )

    private fun TrackEntity.toDomain() = Track(
        id = id, projectId = projectId, startedAt = startedAt, endedAt = endedAt,
        points = GeoJsonGeom.parse(geometryGeoJson),
        distanceMeters = distanceMeters, durationMillis = durationMillis,
    )
}
