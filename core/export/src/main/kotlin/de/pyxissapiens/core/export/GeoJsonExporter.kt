package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.Measurement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Minimal GeoJSON writer (FeatureCollection of points). Scaffold.
 * Symbol styling / KMZ packaging (with media references) follow during Epic 3.
 */
object GeoJsonExporter {

    fun export(measurements: List<Measurement>): String {
        val features = buildJsonArray {
            measurements.forEach { m ->
                val loc = m.location ?: return@forEach
                addJsonObject {
                    put("type", "Feature")
                    putJsonObject("geometry") {
                        put("type", "Point")
                        putJsonArray("coordinates") {
                            add(JsonPrimitive(loc.longitude))
                            add(JsonPrimitive(loc.latitude))
                            loc.altitudeMeters?.let { add(JsonPrimitive(it)) }
                        }
                    }
                    putJsonObject("properties") {
                        put("id", m.id)
                        put("kind", m.kind.name)
                        m.attitude?.let {
                            put("dip", it.dip)
                            put("dipDirection", it.dipDirection)
                        }
                        m.lineation?.let {
                            put("trend", it.trend)
                            put("plunge", it.plunge)
                        }
                        m.quality?.let { put("quality", it.score) }
                    }
                }
            }
        }
        return buildJsonObject {
            put("type", "FeatureCollection")
            put("features", features)
        }.toString()
    }
}
