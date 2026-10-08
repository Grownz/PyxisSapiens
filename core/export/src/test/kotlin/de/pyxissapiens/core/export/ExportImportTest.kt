package de.pyxissapiens.core.export

import de.pyxissapiens.core.domain.model.Attitude
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Lineation
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ExportImportTest {

    private val now = 1_700_000_000_000L

    private fun sample() = listOf(
        Measurement(
            id = "m1", siteId = "field", kind = MeasurementKind.PLANE,
            attitude = Attitude(34.2, 148.0), location = GeoPoint(47.1, 11.4, 1200.0, 3f),
            note = "bedding", createdAt = now, updatedAt = now,
        ),
        Measurement(
            id = "m2", siteId = "field", kind = MeasurementKind.LINE,
            lineation = Lineation(210.0, 42.0), createdAt = now, updatedAt = now,
        ),
    )

    @Test
    fun csvRoundTrip() {
        val csv = CsvExporter.export(sample())
        val result = CsvImporter.parse(csv, now = now)
        assertEquals(2, result.measurements.size)
        val plane = result.measurements.first { it.kind == MeasurementKind.PLANE }
        assertEquals(34.2, plane.attitude!!.dip, 1e-6)
        assertEquals(148.0, plane.attitude!!.dipDirection, 1e-6)
        val line = result.measurements.first { it.kind == MeasurementKind.LINE }
        assertEquals(210.0, line.lineation!!.trend, 1e-6)
        assertEquals(42.0, line.lineation!!.plunge, 1e-6)
    }

    @Test
    fun csvStrikeIsConvertedToDipDirection() {
        val csv = "id,dip,strike\nx,30,60\n"
        val result = CsvImporter.parse(csv, now = now)
        assertEquals(1, result.measurements.size)
        assertEquals(150.0, result.measurements.first().attitude!!.dipDirection, 1e-6)
    }

    @Test
    fun encryptedArchiveRoundTrip() {
        val files = mapOf("export.csv" to "a,b\n1,2\n".toByteArray(), "manifest.json" to "{}".toByteArray())
        val bytes = ProjectArchive.write(files, password = "geheim")
        assertTrue(ProjectArchive.isEncrypted(bytes))
        val read = ProjectArchive.read(bytes, "geheim")
        assertEquals("a,b\n1,2\n", String(read["export.csv"]!!))
        try {
            ProjectArchive.read(bytes, "falsch")
            fail("Expected decryption to fail with wrong password")
        } catch (expected: Exception) {
            // ok
        }
    }

    @Test
    fun plainArchiveRoundTrip() {
        val files = mapOf("doc.kml" to "<kml/>".toByteArray())
        val bytes = ProjectArchive.write(files, password = null)
        assertTrue(!ProjectArchive.isEncrypted(bytes))
        assertEquals("<kml/>", String(ProjectArchive.read(bytes, null)["doc.kml"]!!))
    }

    @Test
    fun validatorFlagsOutOfRange() {
        val bad = listOf(
            Measurement(
                id = "bad", siteId = "s", kind = MeasurementKind.PLANE,
                attitude = Attitude(120.0, 400.0), createdAt = now, updatedAt = now,
            ),
        )
        val report = ExportValidator.validate(bad)
        assertTrue(!report.ok)
        assertTrue(report.warnings.size >= 2)
    }

    @Test
    fun kmzContainsDocKml() {
        val kml = KmlExporter.export(sample(), "Test")
        val kmz = KmzExporter.export(kml)
        val unzipped = ProjectArchive.read(kmz, null)
        assertTrue(unzipped.containsKey("doc.kml"))
    }
}
