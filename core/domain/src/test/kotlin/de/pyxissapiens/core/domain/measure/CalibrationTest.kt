package de.pyxissapiens.core.domain.measure

import de.pyxissapiens.core.geology.math.Vector3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CalibrationTest {

    @Test
    fun fitRecoversOffsetAndNormalises() {
        // Synthetic sphere with offset (+8,-4,+3) and per-axis scale (1.2, 0.8, 1.0).
        val offset = Vector3(8.0, -4.0, 3.0)
        val scale = Vector3(1.2, 0.8, 1.0)
        val count = 200
        val samples = (0 until count).map { i ->
            val theta = Math.acos(1.0 - 2.0 * (i + 0.5) / count)
            val phi = Math.PI * (1.0 + Math.sqrt(5.0)) * i
            val dir = Vector3(
                Math.sin(theta) * Math.cos(phi) * 50.0 * scale.x,
                Math.sin(theta) * Math.sin(phi) * 50.0 * scale.y,
                Math.cos(theta) * 50.0 * scale.z,
            )
            dir + offset
        }
        val cal = Calibration.fit(samples, now = 0L)
        assertNotNull(cal)
        assertEquals(8.0, cal.offsetX, 0.5)
        assertEquals(-4.0, cal.offsetY, 0.5)
        assertEquals(3.0, cal.offsetZ, 0.5)
        assertTrue(cal.quality > 0.9, "quality=${cal.quality}")

        // Corrected magnitudes should all be ~50 µT.
        samples.take(30).forEach { raw ->
            assertEquals(50.0, Calibration.correctedMagnitude(cal, raw), 1.5)
        }
    }

    @Test
    fun tiltCorrectionMapsNormalToUp() {
        val normal = Vector3(0.2, -0.1, 0.97).normalized()
        val cal = Calibration.tiltFrom(normal, now = 0L)
        val corrected = Calibration.applyMatrix(cal.correction, normal)
        assertEquals(0.0, corrected.x, 1e-6)
        assertEquals(0.0, corrected.y, 1e-6)
        assertEquals(1.0, corrected.z, 1e-6)
    }

    @Test
    fun fitRejectsInsufficientSamples() {
        assertEquals(null, Calibration.fit(listOf(Vector3(1.0, 0.0, 0.0))))
    }
}
