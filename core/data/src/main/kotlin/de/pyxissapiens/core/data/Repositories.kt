package de.pyxissapiens.core.data

import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.Project
import de.pyxissapiens.core.domain.model.Site
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

const val DEFAULT_PROJECT_ID = "default"
const val DEFAULT_SITE_ID = "field"

@Singleton
class ProjectRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeProjects(): Flow<List<Project>> =
        db.projectDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun getProject(id: String): Project? = db.projectDao().getById(id)?.toDomain()

    suspend fun upsert(project: Project) = db.projectDao().upsert(project.toEntity())

    suspend fun ensureDefaultProject(now: Long = System.currentTimeMillis()): Project {
        db.projectDao().getById(DEFAULT_PROJECT_ID)?.let { return it.toDomain() }
        val project = Project(
            id = DEFAULT_PROJECT_ID, name = "Feldarbeit", description = null,
            crs = "WGS84", createdAt = now, updatedAt = now,
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
        val site = Site(id = DEFAULT_SITE_ID, projectId = projectId, name = "Feldaufschluss")
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

    suspend fun getMeasurement(id: String): Measurement? = db.measurementDao().getById(id)?.toDomain()

    suspend fun add(measurement: Measurement) = db.measurementDao().upsert(measurement.toEntity())

    suspend fun update(measurement: Measurement) = db.measurementDao().upsert(measurement.toEntity())

    suspend fun delete(id: String) = db.measurementDao().delete(id)

    suspend fun history(id: String): List<de.pyxissapiens.core.domain.model.HistoryEntry> =
        db.measurementHistoryDao().historyOf(id).map {
            de.pyxissapiens.core.domain.model.HistoryEntry(
                measurementId = it.measurementId,
                field = it.field,
                oldValue = it.oldValue,
                newValue = it.newValue,
                atEpochMillis = it.atEpochMillis,
            )
        }
}
