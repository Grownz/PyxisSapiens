package de.pyxissapiens.core.geology.math

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoMathTest {

    private val tol = 1e-6

    @Test
    fun planePoleRoundTrip() {
        val cases = listOf(
            0.0 to 0.0,
            0.0 to 90.0,
            130.0 to 34.2,
            270.0 to 10.0,
            45.0 to 60.0,
        )
        for ((dd, dip) in cases) {
            val (outDd, outDip) = GeoMath.poleToPlane(GeoMath.planeToPole(dd, dip))
            assertEquals(dd, outDd, 1e-4, "dipDirection for $dd/$dip")
            assertEquals(dip, outDip, 1e-4, "dip for $dd/$dip")
        }
    }

    @Test
    fun lineationRoundTrip() {
        val cases = listOf(0.0 to 0.0, 90.0 to 30.0, 210.0 to 42.0, 359.0 to 5.0)
        for ((trend, plunge) in cases) {
            val (outTrend, outPlunge) = GeoMath.vectorToLineation(GeoMath.lineationToVector(trend, plunge))
            assertEquals(trend, outTrend, 1e-4)
            assertEquals(plunge, outPlunge, 1e-4)
        }
    }

    @Test
    fun strikeDipDirectionAreConsistent() {
        assertEquals(60.0, GeoMath.strikeFromDipDirection(150.0), tol)
        assertEquals(150.0, GeoMath.dipDirectionFromStrike(60.0), tol)
        assertEquals(0.0, GeoMath.strikeFromDipDirection(90.0), tol)
    }

    @Test
    fun verticalPlaneNormalIsHorizontal() {
        // Plane dipping 90° towards north -> horizontal normal pointing south.
        val pole = GeoMath.planeToPole(0.0, 90.0)
        assertEquals(0.0, pole.z, 1e-9)
        assertEquals(-1.0, pole.y, 1e-9)
    }

    @Test
    fun angleBetweenPerpendicularIs90() {
        val a = GeoMath.lineationToVector(0.0, 0.0)
        val b = GeoMath.lineationToVector(90.0, 0.0)
        assertEquals(90.0, GeoMath.angleBetween(a, b), 1e-4)
    }
}
