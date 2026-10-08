package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.Attitude
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Lineation
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind

/** Result of an import operation with non-fatal warnings. */
data class ImportResult(val measurements: List<Measurement>, val warnings: List<String>)

/** Validates records before export/import. */
object ExportValidator {
    data class Report(val ok: Boolean, val warnings: List<String>)

    fun validate(measurements: List<Measurement>): Report {
        val warnings = ArrayList<String>()
        val ids = HashSet<String>()
        for (m in measurements) {
            if (!ids.add(m.id)) warnings.add("Duplicate id: ${m.id}")
            m.attitude?.let {
                if (it.dip !in 0.0..90.0) warnings.add("Dip out of range (${it.dip}) for ${m.id}")
                if (it.dipDirection !in 0.0..360.0) warnings.add("Dip direction out of range (${it.dipDirection}) for ${m.id}")
            }
            m.lineation?.let {
                if (it.plunge !in 0.0..90.0) warnings.add("Plunge out of range (${it.plunge}) for ${m.id}")
                if (it.trend !in 0.0..360.0) warnings.add("Trend out of range (${it.trend}) for ${m.id}")
            }
            m.location?.let {
                if (it.latitude !in -90.0..90.0) warnings.add("Latitude out of range for ${m.id}")
                if (it.longitude !in -180.0..180.0) warnings.add("Longitude out of range for ${m.id}")
            }
        }
        return Report(warnings.isEmpty(), warnings)
    }
}

/**
 * CSV import with header-based auto-mapping. Supports plane data as
 * dip-direction/dip or right-hand-rule strike/dip, and lineation trend/plunge.
 */
object CsvImporter {

    fun parse(text: String, siteId: String = "import", now: Long = System.currentTimeMillis()): ImportResult {
        val warnings = ArrayList<String>()
        val lines = text.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return ImportResult(emptyList(), listOf("Empty CSV"))
        val header = splitCsv(lines.first()).map { it.trim().lowercase() }

        fun col(vararg names: String): Int = header.indexOfFirst { it in names }

        val dip = col("dip")
        val dipDir = col("dipdirection", "dip_dir", "dip_direction", "dd")
        val strike = col("strike", "rhr")
        val trend = col("trend")
        val plunge = col("plunge")
        val bearing = col("bearing", "azimuth")
        val lat = col("latitude", "lat")
        val lon = col("longitude", "lon", "long")
        val alt = col("altitude", "alt", "elev")
        val acc = col("accuracy", "acc")
        val note = col("note", "notes", "comment")
        val id = col("id")

        if (dip < 0 && trend < 0 && bearing < 0) {
            return ImportResult(emptyList(), listOf("No recognized value columns (dip/trend/bearing)"))
        }

        val result = ArrayList<Measurement>()
        for ((i, line) in lines.drop(1).withIndex()) {
            val cells = splitCsv(line)
            fun cell(index: Int): String? = if (index in 0..cells.lastIndex) cells[index].trim().ifEmpty { null } else null
            fun num(index: Int): Double? = cell(index)?.toDoubleOrNull()

            val rowId = cell(id) ?: "import-${i}-$now"
            val noteText = cell(note)
            val location = if (lat >= 0 && lon >= 0 && num(lat) != null && num(lon) != null) {
                GeoPoint(num(lat)!!, num(lon)!!, alt.let { num(it) }, acc.let { num(it)?.toFloat() })
            } else null

            val dipV = if (dip >= 0) num(dip) else null
            val dipDirV = if (dipDir >= 0) num(dipDir) else null
            val strikeV = if (strike >= 0) num(strike) else null
            val trendV = if (trend >= 0) num(trend) else null
            val plungeV = if (plunge >= 0) num(plunge) else null
            val bearingV = if (bearing >= 0) num(bearing) else null

            val measurement: Measurement = when {
                dipV != null && dipDirV != null ->
                    base(rowId, siteId, MeasurementKind.PLANE, noteText, location, now)
                        .copy(attitude = Attitude(dipV, dipDirV))
                dipV != null && strikeV != null ->
                    base(rowId, siteId, MeasurementKind.PLANE, noteText, location, now)
                        .copy(attitude = Attitude(dipV, ((strikeV + 90.0) % 360.0)))
                trendV != null && plungeV != null ->
                    base(rowId, siteId, MeasurementKind.LINE, noteText, location, now)
                        .copy(lineation = Lineation(trendV, plungeV))
                bearingV != null ->
                    base(rowId, siteId, MeasurementKind.BEARING, noteText, location, now)
                        .copy(bearingDeg = bearingV)
                else -> continue
            }
            result.add(measurement)
        }
        if (result.isEmpty()) warnings.add("No data rows parsed")
        return ImportResult(result, warnings)
    }

    private fun base(
        id: String, siteId: String, kind: MeasurementKind,
        note: String?, location: GeoPoint?, now: Long,
    ) = Measurement(id = id, siteId = siteId, kind = kind, note = note, location = location, createdAt = now, updatedAt = now)

    private fun splitCsv(line: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') { sb.append('"'); i++ }
                    else inQuotes = !inQuotes
                }
                c == ',' && !inQuotes -> { out.add(sb.toString()); sb.clear() }
                else -> sb.append(c)
            }
            i++
        }
        out.add(sb.toString())
        return out
    }
}

/** Minimal KML import: reads Placemarks as measurement points. */
object KmlImporter {

    private val placemark = Regex("<Placemark>(.*?)</Placemark>", RegexOption.DOT_MATCHES_ALL)
    private val name = Regex("<name>(.*?)</name>", RegexOption.DOT_MATCHES_ALL)
    private val coordinates = Regex("<coordinates>\\s*([-0-9.]+)\\s*,\\s*([-0-9.]+)(?:\\s*,\\s*([-0-9.]+))?\\s*</coordinates>")

    fun parse(xml: String, siteId: String = "import", now: Long = System.currentTimeMillis()): ImportResult {
        val warnings = ArrayList<String>()
        val result = ArrayList<Measurement>()
        var index = 0
        for (pm in placemark.findAll(xml)) {
            val body = pm.groupValues[1]
            val nameMatch = name.find(body)?.groupValues?.get(1)
            val coordMatch = coordinates.find(body) ?: run {
                warnings.add("Placemark without coordinates skipped"); null
            } ?: continue
            val lon = coordMatch.groupValues[1].toDoubleOrNull() ?: continue
            val lat = coordMatch.groupValues[2].toDoubleOrNull() ?: continue
            val alt = coordMatch.groupValues.getOrNull(3)?.toDoubleOrNull()
            result.add(
                Measurement(
                    id = "kml-${index++}-$now",
                    siteId = siteId,
                    kind = MeasurementKind.PLANE,
                    location = GeoPoint(lat, lon, alt, null),
                    note = nameMatch,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        if (result.isEmpty()) warnings.add("No placemarks parsed")
        return ImportResult(result, warnings)
    }
}
