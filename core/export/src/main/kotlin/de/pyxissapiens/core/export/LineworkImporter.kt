package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Linework
import de.pyxissapiens.core.domain.model.LineworkKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/** Imports LineString/Polygon geometry from GeoJSON or KML into [Linework] records. */
object LineworkImporter {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun importGeoJson(text: String, projectId: String = "default", now: Long = System.currentTimeMillis()): List<Linework> {
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return emptyList()
        val features = root["features"]?.jsonArray ?: return emptyList()
        return features.mapNotNull { element ->
            val feature = element.jsonObject
            val geometry = feature["geometry"]?.jsonObject ?: return@mapNotNull null
            val type = geometry["type"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val coordinates = geometry["coordinates"]?.jsonArray ?: return@mapNotNull null
            when (type) {
                "LineString" -> linework(LineworkKind.CONTACT, coordinates.toPoints(), projectId, now)
                "Polygon" -> {
                    val ring = coordinates.firstOrNull()?.jsonArray ?: return@mapNotNull null
                    linework(LineworkKind.POLYGON, ring.toPoints(), projectId, now)
                }
                else -> null
            }
        }
    }

    fun importKml(text: String, projectId: String = "default", now: Long = System.currentTimeMillis()): List<Linework> {
        val result = ArrayList<Linework>()
        val blockRegex = Regex("<(LineString|Polygon)>(.*?)</\\1>", RegexOption.DOT_MATCHES_ALL)
        val coordRegex = Regex("<coordinates>(.*?)</coordinates>", RegexOption.DOT_MATCHES_ALL)
        for (block in blockRegex.findAll(text)) {
            val type = block.groupValues[1]
            val body = block.groupValues[2]
            val coords = coordRegex.find(body)?.groupValues?.get(1) ?: continue
            val points = coords.trim().split(Regex("\\s+")).mapNotNull { token ->
                val parts = token.split(',')
                if (parts.size < 2) null else parts[0].toDoubleOrNull()?.let { lon ->
                    parts[1].toDoubleOrNull()?.let { lat -> GeoPoint(lat, lon) }
                }
            }
            val kind = if (type == "Polygon") LineworkKind.POLYGON else LineworkKind.CONTACT
            linework(kind, points, projectId, now)?.let { result.add(it) }
        }
        return result
    }

    private fun linework(kind: LineworkKind, points: List<GeoPoint>, projectId: String, now: Long): Linework? {
        if (points.size < 2) return null
        return Linework(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            kind = kind,
            coordinates = points,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun JsonArray.toPoints(): List<GeoPoint> = mapNotNull { element ->
        val pair = element.jsonArray
        val lon = pair.getOrNull(0)?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
        val lat = pair.getOrNull(1)?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
        val alt = pair.getOrNull(2)?.jsonPrimitive?.content?.toDoubleOrNull()
        GeoPoint(lat, lon, alt)
    }
}
