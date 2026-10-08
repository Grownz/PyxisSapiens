package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.Measurement

/**
 * Export encoders. Scaffold: CSV and a minimal GeoJSON writer.
 * Validation (range/coordinate/duplicate checks) is added during Epic 3.
 */
object CsvExporter {

    private val header = listOf(
        "id", "siteId", "kind", "dip", "dipDirection", "trend", "plunge", "bearing",
        "latitude", "longitude", "altitude", "accuracy", "positionManual",
        "qualityScore", "sigma", "kappa", "typeId", "unitId", "note", "createdAt", "updatedAt",
    )

    fun export(measurements: List<Measurement>): String {
        val sb = StringBuilder()
        sb.append(header.joinToString(",")).append('\n')
        for (m in measurements) {
            val row = listOf(
                m.id, m.siteId, m.kind.name,
                m.attitude?.dip?.toString() ?: "",
                m.attitude?.dipDirection?.toString() ?: "",
                m.lineation?.trend?.toString() ?: "",
                m.lineation?.plunge?.toString() ?: "",
                m.bearingDeg?.toString() ?: "",
                m.location?.latitude?.toString() ?: "",
                m.location?.longitude?.toString() ?: "",
                m.location?.altitudeMeters?.toString() ?: "",
                m.location?.accuracyMeters?.toString() ?: "",
                m.positionManual.toString(),
                m.quality?.score?.toString() ?: "",
                m.quality?.sigmaDeg?.toString() ?: "",
                m.quality?.kappa?.toString() ?: "",
                m.typeId ?: "", m.unitId ?: "", m.note ?: "",
                m.createdAt.toString(), m.updatedAt.toString(),
            )
            sb.append(row.joinToString(",") { escape(it) }).append('\n')
        }
        return sb.toString()
    }

    private fun escape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
}
