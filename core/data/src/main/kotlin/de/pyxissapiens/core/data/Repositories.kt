package de.pyxissapiens.core.data

import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.database.entity.MeasurementEntity
import de.pyxissapiens.core.database.entity.ProjectEntity
import de.pyxissapiens.core.database.entity.SiteEntity
import de.pyxissapiens.core.domain.model.Attitude
import de.pyxissapiens.core.domain.model.DataQuality
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Lineation
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.domain.model.Project
import de.pyxissapiens.core.domain.model.Site
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer. Scaffold: exposes read/observe access and entity<->domain mapping.
 * Write paths, undo/history and validation are added during MVP implementation.
 */

private fun ProjectEntity.toDomain() = Project(
    id = id, name = name, author = author, description = description,
    crs = crs, createdAt = createdAt, updatedAt = updatedAt,
)

private fun SiteEntity.toDomain(): Site {
    val lat = latitude
    val lon = longitude
    return Site(
        id = id,
        projectId = projectId,
        name = name,
        location = if (lat != null && lon != null) {
            GeoPoint(lat, lon, altitudeMeters, accuracyMeters)
        } else null,
        description = description,
    )
}

private fun MeasurementEntity.toDomain(): Measurement {
    val dipValue = dip
    val dipDirectionValue = dipDirection
    val trendValue = trend
    val plungeValue = plunge
    val lat = latitude
    val lon = longitude
    return Measurement(
        id = id,
        siteId = siteId,
        kind = runCatching { MeasurementKind.valueOf(kind) }.getOrDefault(MeasurementKind.PLANE),
        attitude = if (dipValue != null && dipDirectionValue != null) {
            Attitude(dipValue, dipDirectionValue)
        } else null,
        lineation = if (trendValue != null && plungeValue != null) {
            Lineation(trendValue, plungeValue)
        } else null,
        bearingDeg = bearing,
        location = if (lat != null && lon != null) {
            GeoPoint(lat, lon, altitudeMeters, accuracyMeters)
        } else null,
        positionManual = positionManual,
        quality = qualityScore?.let { score ->
            DataQuality(
                score = score,
                sensorAccuracy = sensorAccuracy,
                stabilityDeg = stabilityDeg,
                sampleCount = sampleCount,
                sigmaDeg = sigmaDeg,
                kappa = kappa,
            )
        },
        typeId = typeId,
        unitId = unitId,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

@Singleton
class ProjectRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeProjects(): Flow<List<Project>> =
        db.projectDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun getProject(id: String): Project? = db.projectDao().getById(id)?.toDomain()
}

@Singleton
class SiteRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeSites(projectId: String): Flow<List<Site>> =
        db.siteDao().observeByProject(projectId).map { rows -> rows.map { it.toDomain() } }
}

@Singleton
class MeasurementRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeMeasurements(siteId: String): Flow<List<Measurement>> =
        db.measurementDao().observeBySite(siteId).map { rows -> rows.map { it.toDomain() } }

    suspend fun getMeasurement(id: String): Measurement? =
        db.measurementDao().getById(id)?.toDomain()
}
