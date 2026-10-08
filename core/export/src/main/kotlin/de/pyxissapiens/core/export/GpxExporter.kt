package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.GeoPoint
import java.util.Locale

/** GPX 1.1 track export. */
object GpxExporter {

    fun export(name: String, points: List<GeoPoint>, timeMillis: Long = System.currentTimeMillis()): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"PyxisSapiens\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("<trk><name>").append(escape(name)).append("</name><trkseg>\n")
        for (p in points) {
            sb.append("<trkpt lat=\"").append(p.latitude).append("\" lon=\"").append(p.longitude).append("\">")
            p.altitudeMeters?.let { sb.append("<ele>").append(String.format(Locale.US, "%.1f", it)).append("</ele>") }
            sb.append("</trkpt>\n")
        }
        sb.append("</trkseg></trk>\n</gpx>\n")
        return sb.toString()
    }

    private fun escape(s: String): String = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
