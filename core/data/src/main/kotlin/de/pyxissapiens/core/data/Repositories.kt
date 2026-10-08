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
 * Repository layer: entity<->domain mapping plus read/observe and save access.
 * Undo/history and validation are added during MVP refinement.
 */

const val DEFAULT_PROJECT_ID = "default"
const val DEFAULT_SITE_ID = "field"

// --- entity -> domain -------------------------------------------------------------------------

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
        location = if (lat != null && lon != null) GeoPoint(lat, lon, altitudeMeters, accuracyMeters) else null,
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
        attitude = if (dipValue != null && dipDirectionValue != null) Attitude(dipValue, dipDirectionValue) else null,
        lineation = if (trendValue != null && plungeValue != null) Lineation(trendValue, plungeValue) else null,
        bearingDeg = bearing,
        location = if (lat != null && lon != null) GeoPoint(lat, lon, altitudeMeters, accuracyMeters) else null,
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

// --- domain -> entity -------------------------------------------------------------------------

private fun Project.toEntity() = ProjectEntity(
    id = id, name = name, author = author, description = description,
    crs = crs, createdAt = createdAt, updatedAt = updatedAt,
)

private fun Site.toEntity() = SiteEntity(
    id = id,
    projectId = projectId,
    name = name,
    latitude = location?.latitude,
    longitude = location?.longitude,
    altitudeMeters = location?.altitudeMeters,
    accuracyMeters = location?.accuracyMeters,
    description = description,
)

private fun Measurement.toEntity() = MeasurementEntity(
    id = id,
    siteId = siteId,
    kind = kind.name,
    dip = attitude?.dip,
    dipDirection = attitude?.dipDirection,
    trend = lineation?.trend,
    plunge = lineation?.plunge,
    bearing = bearingDeg,
    latitude = location?.latitude,
    longitude = location?.longitude,
    altitudeMeters = location?.altitudeMeters,
    accuracyMeters = location?.accuracyMeters,
    positionManual = positionManual,
    qualityScore = quality?.score,
    sensorAccuracy = quality?.sensorAccuracy,
    stabilityDeg = quality?.stabilityDeg,
    sampleCount = quality?.sampleCount ?: 1,
    sigmaDeg = quality?.sigmaDeg,
    kappa = quality?.kappa,
    typeId = typeId,
    unitId = unitId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// --- repositories -----------------------------------------------------------------------------

@Singleton
class ProjectRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeProjects(): Flow<List<Project>> =
        db.projectDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun getProject(id: String): Project? = db.projectDao().getById(id)?.toDomain()

    suspend fun upsert(project: Project) = db.projectDao().upsert(project.toEntity())

    /** Returns the default project, creating it if the database is empty. */
    suspend fun ensureDefaultProject(now: Long = System.currentTimeMillis()): Project {
        db.projectDao().getById(DEFAULT_PROJECT_ID)?.let { return it.toDomain() }
        val project = Project(
            id = DEFAULT_PROJECT_ID,
            name = "Feldarbeit",
            description = null,
            crs = "WGS84",
            createdAt = now,
            updatedAt = now,
        )
        db.projectDao().upsert(project.toEntity())
        return project
    }
}

@Singleton
class SiteRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeSites(projectId: String): Flow<List<Site>> =
        db.siteDao().observeByProject(projectId).map { rows -> rows.map { it.toDomain() } }

    suspend fun upsert(site: Site) = db.siteDao().upsert(site.toEntity())

    suspend fun ensureDefaultSite(projectId: String = DEFAULT_PROJECT_ID): Site {
        val site = Site(
            id = DEFAULT_SITE_ID,
            projectId = projectId,
            name = "Feldaufschluss",
            location = null,
            description = null,
        )
        db.siteDao().upsert(site.toEntity())
        return site
    }
}

@Singleton
class MeasurementRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeMeasurements(siteId: String): Flow<List<Measurement>> =
        db.measurementDao().observeBySite(siteId).map { rows -> rows.map { it.toDomain() } }

    fun observeAll(): Flow<List<Measurement>> =
        db.measurementDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun getMeasurement(id: String): Measurement? =
        db.measurementDao().getById(id)?.toDomain()

    suspend fun add(measurement: Measurement) = db.measurementDao().upsert(measurement.toEntity())

    suspend fun delete(id: String) = db.measurementDao().delete(id)
}
