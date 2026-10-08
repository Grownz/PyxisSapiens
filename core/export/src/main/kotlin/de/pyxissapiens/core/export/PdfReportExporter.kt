package de.pyxissapiens.core.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.geology.stereo.Projection
import de.pyxissapiens.core.geology.stereo.Stereonet
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
 * PDF field report using the framework [PdfDocument] (no extra dependency).
 * Includes a simple stereonet sketch drawn directly on the page canvas.
 */
object PdfReportExporter {

    fun export(
        documentName: String,
        measurements: List<Measurement>,
        projection: Projection = Projection.SCHMIDT,
        statsText: String? = null,
    ): ByteArray {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val title = Paint().apply { textSize = 20f; isAntiAlias = true; isFakeBoldText = true }
        val header = Paint().apply { textSize = 13f; isAntiAlias = true; isFakeBoldText = true }
        val body = Paint().apply { textSize = 11f; isAntiAlias = true }
        val muted = Paint().apply { textSize = 10f; isAntiAlias = true; color = Color.DKGRAY; }
        val stroke = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; color = Color.DKGRAY; isAntiAlias = true }
        val polePaint = Paint().apply { color = Color.rgb(78, 161, 255); isAntiAlias = true }
        val linePaint = Paint().apply { color = Color.rgb(55, 214, 122); isAntiAlias = true }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = 50f

        canvas.drawText(documentName, 40f, y, title); y += 24f
        canvas.drawText("PyxisSapiens · Feldbericht · ${measurements.size} Messungen", 40f, y, muted); y += 20f

        // Simple stereonet sketch (lower hemisphere)
        val cx = pageWidth / 2f
        val cy = y + 110f
        val r = 100f
        canvas.drawCircle(cx, cy, r, stroke)
        for (m in measurements) {
            m.attitude?.let {
                val p = Stereonet.plotPlane(it.dipDirection, it.dip, projection)
                canvas.drawCircle(cx + (p.x * r).toFloat(), cy - (p.y * r).toFloat(), 2.5f, polePaint)
            }
            m.lineation?.let {
                val p = Stereonet.plotLine(it.trend, it.plunge, projection)
                canvas.drawCircle(cx + (p.x * r).toFloat(), cy - (p.y * r).toFloat(), 3.0f, linePaint)
            }
        }
        canvas.drawText("N", cx - 4f, cy - r - 6f, muted)
        y = cy + r + 24f

        if (statsText != null) {
            for (line in statsText.split("\n")) {
                canvas.drawText(line, 40f, y, muted); y += 14f
            }
            y += 8f
        }

        val columns = listOf("Kind", "Dip/Trend", "Dir/Plunge", "Lat", "Lon", "Q")
        val xPositions = listOf(40f, 120f, 210f, 300f, 400f, 510f)
        columns.forEachIndexed { i, c -> canvas.drawText(c, xPositions[i], y, header) }
        y += 6f
        canvas.drawLine(40f, y, pageWidth - 40f, y, muted); y += 16f

        for (m in measurements) {
            if (y > pageHeight - 40f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = 50f
            }
            val kind = when (m.kind) {
                MeasurementKind.PLANE -> "Fläche"
                MeasurementKind.LINE -> "Linear"
                MeasurementKind.BEARING -> "Peilung"
            }
            val a = when (m.kind) {
                MeasurementKind.PLANE -> fmt(m.attitude?.dip)
                MeasurementKind.LINE -> fmt(m.lineation?.trend)
                MeasurementKind.BEARING -> fmt(m.bearingDeg)
            }
            val b = when (m.kind) {
                MeasurementKind.PLANE -> fmt(m.attitude?.dipDirection)
                MeasurementKind.LINE -> fmt(m.lineation?.plunge)
                MeasurementKind.BEARING -> "—"
            }
            val values = listOf(kind, a, b, fmt(m.location?.latitude), fmt(m.location?.longitude), m.quality?.score?.toString() ?: "—")
            values.forEachIndexed { i, v -> canvas.drawText(v, xPositions[i], y, body) }
            y += 15f
        }
        document.finishPage(page)

        val out = ByteArrayOutputStream()
        document.writeTo(out)
        document.close()
        return out.toByteArray()
    }

    private fun fmt(value: Double?): String =
        if (value == null) "—" else String.format(Locale.US, "%.1f", value)
}
