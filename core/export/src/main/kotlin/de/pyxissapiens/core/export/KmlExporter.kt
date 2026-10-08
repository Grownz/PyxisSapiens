package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** KML export (Google Earth). Symbol colour reflects the measurement kind. */
object KmlExporter {

    fun export(measurements: List<Measurement>, documentName: String): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n")
        sb.append("<name>").append(escape(documentName)).append("</name>\n")
        sb.append("<Style id=\"plane\"><IconStyle><color>ff4ea1ff</color><scale>1.1</scale></IconStyle></Style>\n")
        sb.append("<Style id=\"line\"><IconStyle><color>ff37d67a</color><scale>1.1</scale></IconStyle></Style>\n")
        sb.append("<Style id=\"bearing\"><IconStyle><color>ffffb000</color><scale>1.1</scale></IconStyle></Style>\n")

        for (m in measurements) {
            val loc = m.location ?: continue
            val style = when (m.kind) {
                MeasurementKind.PLANE -> "#plane"
                MeasurementKind.LINE -> "#line"
                MeasurementKind.BEARING -> "#bearing"
            }
            sb.append("<Placemark>\n")
            sb.append("<name>").append(escape(primaryLabel(m))).append("</name>\n")
            sb.append("<styleUrl>").append(style).append("</styleUrl>\n")
            sb.append("<description><![CDATA[")
            sb.append("Kind: ").append(m.kind.name)
            m.attitude?.let { sb.append("<br/>Dip: ").append(it.dip).append(" / DipDir: ").append(it.dipDirection) }
            m.lineation?.let { sb.append("<br/>Trend: ").append(it.trend).append(" / Plunge: ").append(it.plunge) }
            m.bearingDeg?.let { sb.append("<br/>Bearing: ").append(it) }
            m.quality?.let { sb.append("<br/>Quality: ").append(it.score) }
            m.note?.let { sb.append("<br/>Note: ").append(escape(it)) }
            sb.append("]]></description>\n")
            sb.append("<Point><coordinates>")
            sb.append(loc.longitude).append(',').append(loc.latitude)
            loc.altitudeMeters?.let { sb.append(',').append(it) }
            sb.append("</coordinates></Point>\n")
            sb.append("</Placemark>\n")
        }
        sb.append("</Document>\n</kml>\n")
        return sb.toString()
    }

    private fun primaryLabel(m: Measurement): String = when (m.kind) {
        MeasurementKind.PLANE -> "${m.attitude?.dipDirection?.toInt() ?: 0}/${m.attitude?.dip?.toInt() ?: 0}"
        MeasurementKind.LINE -> "${m.lineation?.trend?.toInt() ?: 0}→${m.lineation?.plunge?.toInt() ?: 0}"
        MeasurementKind.BEARING -> "${m.bearingDeg?.toInt() ?: 0}°"
    }

    private fun escape(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

/** KMZ export: a ZIP archive containing doc.kml. */
object KmzExporter {
    fun export(kml: String): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry("doc.kml"))
            zip.write(kml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return baos.toByteArray()
    }
}
