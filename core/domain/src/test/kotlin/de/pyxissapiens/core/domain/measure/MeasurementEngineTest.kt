package de.pyxissapiens.core.domain.measure

import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.ports.DeviceFrame
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MeasurementEngineTest {

    /** Builds a device frame whose +Z (out of screen) equals [n]. */
    private fun frameWithZ(n: Vector3): DeviceFrame {
        val z = n.normalized()
        val ref = if (abs(z.z) < 0.9) Vector3(0.0, 0.0, 1.0) else Vector3(0.0, 1.0, 0.0)
        val x = ref.cross(z).normalized()
        val y = z.cross(x).normalized()
        return DeviceFrame(x = x, y = y, z = z, timestampNanos = 0L)
    }

    /** Builds a device frame whose +Y (top of screen) equals [v]. */
    private fun frameWithY(v: Vector3): DeviceFrame {
        val y = v.normalized()
        val ref = if (abs(y.y) < 0.9) Vector3(0.0, 1.0, 0.0) else Vector3(1.0, 0.0, 0.0)
        val x = ref.cross(y).normalized()
        val z = x.cross(y).normalized()
        return DeviceFrame(x = x, y = y, z = z, timestampNanos = 0L)
    }

    private fun one(frame: DeviceFrame) = listOf(OrientationSample(frame))

    @Test
    fun planeMeasurementRoundTrip() {
        val cases = listOf(120.0 to 30.0, 0.0 to 0.0, 250.0 to 75.0, 90.0 to 45.0)
        for ((dd, dip) in cases) {
            val frame = frameWithZ(GeoMath.planeToPole(dd, dip))
            val result = MeasurementEngine.measurePlane(one(frame), ContactSurface.BACK_ON_PLANE)
            assertEquals(dd, result.dipDirection, 1e-3, "dipDirection for $dd/$dip")
            assertEquals(dip, result.dip, 1e-3, "dip for $dd/$dip")
        }
    }

    @Test
    fun verticalPlaneMeasuredCorrectly() {
        // Plane dipping 90 deg towards north -> a vertical plane.
        val frame = frameWithZ(GeoMath.planeToPole(0.0, 90.0))
        val result = MeasurementEngine.measurePlane(one(frame), ContactSurface.BACK_ON_PLANE)
        assertEquals(90.0, result.dip, 1e-3)
        assertEquals(0.0, result.dipDirection, 1e-3)
    }

    @Test
    fun contactSurfaceSwitchesNormal() {
        val n = GeoMath.planeToPole(140.0, 40.0)
        val back = MeasurementEngine.measurePlane(one(frameWithZ(n)), ContactSurface.BACK_ON_PLANE)
        // Screen against the plane means device +Z points downward (opposite the upward normal).
        val screen = MeasurementEngine.measurePlane(one(frameWithZ(n * -1.0)), ContactSurface.SCREEN_ON_PLANE)
        assertEquals(back.dip, screen.dip, 1e-3)
        assertEquals(back.dipDirection, screen.dipDirection, 1e-3)
    }

    @Test
    fun lineationMeasurementRoundTrip() {
        val cases = listOf(45.0 to 20.0, 200.0 to 65.0, 330.0 to 5.0)
        for ((trend, plunge) in cases) {
            val frame = frameWithY(GeoMath.lineationToVector(trend, plunge))
            val result = MeasurementEngine.measureLine(one(frame))
            assertEquals(trend, result.trend, 1e-3, "trend for $trend/$plunge")
            assertEquals(plunge, result.plunge, 1e-3, "plunge for $trend/$plunge")
        }
    }

    @Test
    fun lineationNormalisesToDownPlunge() {
        val v = GeoMath.lineationToVector(45.0, 20.0)
        val up = MeasurementEngine.measureLine(one(frameWithY(v * -1.0)))
        assertEquals(45.0, up.trend, 1e-3)
        assertEquals(20.0, up.plunge, 1e-3)
    }

    @Test
    fun bearingRoundTrip() {
        val frame = frameWithY(GeoMath.lineationToVector(148.0, 0.0))
        val result = MeasurementEngine.measureBearing(one(frame))
        assertEquals(148.0, result.azimuth, 1e-3)
    }

    @Test
    fun averagingSuppressesNoise() {
        val n = GeoMath.planeToPole(120.0, 30.0)
        val frames = (0 until 20).map { k ->
            val jitter = 0.015
            val noisy = (n + Vector3(
                x = jitter * Math.sin(k.toDouble()),
                y = jitter * Math.cos(k.toDouble()),
                z = jitter * 0.5 * Math.sin(k * 0.7),
            )).normalized()
            OrientationSample(frameWithZ(noisy), sensorAccuracy = 3)
        }
        val result = MeasurementEngine.measurePlane(frames, ContactSurface.BACK_ON_PLANE)
        assertEquals(120.0, result.dipDirection, 2.0)
        assertEquals(30.0, result.dip, 2.0)
        assertTrue(result.quality.sampleCount >= 15, "n=${result.quality.sampleCount} sigma=${result.quality.sigmaDeg} dip=${result.dip} dd=${result.dipDirection}")
        assertTrue(result.quality.score >= 60, "score was ${result.quality.score}")
        assertTrue(result.quality.kappa != null && result.quality.kappa!! > 100)
    }

    @Test
    fun fisherKappaIsMonotonic() {
        val k1 = MeasurementEngine.fisherKappa(0.5)
        val k2 = MeasurementEngine.fisherKappa(0.8)
        val k3 = MeasurementEngine.fisherKappa(0.95)
        assertTrue(k1 < k2 && k2 < k3, "expected kappa to increase: $k1 < $k2 < $k3")
        assertTrue(k1 > 0.0)
    }

    @Test
    fun qualityScorePenalisesFewSamplesAndLowAccuracy() {
        val good = MeasurementEngine.score(sampleCount = 10, targetSamples = 10, sigmaDeg = 0.3, sensorAccuracy = 3)
        val poor = MeasurementEngine.score(sampleCount = 2, targetSamples = 10, sigmaDeg = 2.5, sensorAccuracy = 1)
        assertTrue(good >= 90, "good=$good")
        assertTrue(poor < good, "poor=$poor good=$good")
    }
}
