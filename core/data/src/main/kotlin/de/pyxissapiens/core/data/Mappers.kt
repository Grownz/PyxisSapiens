package de.pyxissapiens.core.data

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

internal fun ProjectEntity.toDomain() = Project(
    id = id, name = name, author = author, description = description,
    crs = crs, createdAt = createdAt, updatedAt = updatedAt,
)

internal fun Project.toEntity() = ProjectEntity(
    id = id, name = name, author = author, description = description,
    crs = crs, createdAt = createdAt, updatedAt = updatedAt,
)

internal fun SiteEntity.toDomain(): Site {
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

internal fun Site.toEntity() = SiteEntity(
    id = id, projectId = projectId, name = name,
    latitude = location?.latitude, longitude = location?.longitude,
    altitudeMeters = location?.altitudeMeters, accuracyMeters = location?.accuracyMeters,
    description = description,
)

internal fun MeasurementEntity.toDomain(): Measurement {
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
                score = score, sensorAccuracy = sensorAccuracy, stabilityDeg = stabilityDeg,
                sampleCount = sampleCount, sigmaDeg = sigmaDeg, kappa = kappa,
            )
        },
        typeId = typeId, unitId = unitId, note = note, createdAt = createdAt, updatedAt = updatedAt,
    )
}

internal fun Measurement.toEntity() = MeasurementEntity(
    id = id, siteId = siteId, kind = kind.name,
    dip = attitude?.dip, dipDirection = attitude?.dipDirection,
    trend = lineation?.trend, plunge = lineation?.plunge, bearing = bearingDeg,
    latitude = location?.latitude, longitude = location?.longitude,
    altitudeMeters = location?.altitudeMeters, accuracyMeters = location?.accuracyMeters,
    positionManual = positionManual,
    qualityScore = quality?.score, sensorAccuracy = quality?.sensorAccuracy,
    stabilityDeg = quality?.stabilityDeg, sampleCount = quality?.sampleCount ?: 1,
    sigmaDeg = quality?.sigmaDeg, kappa = quality?.kappa,
    typeId = typeId, unitId = unitId, note = note, createdAt = createdAt, updatedAt = updatedAt,
)
