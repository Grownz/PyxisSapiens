package de.pyxissapiens.core.wmm

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Validates the bundled WMM2025 implementation against the official NOAA test values
 * (WMM2025_TEST_VALUES.txt). Columns: date, height(km), lat, lon, ..., F(nT), I(deg), D(deg).
 */
class WmmGeomagneticProviderTest {

    private val wmm = WmmGeomagneticProvider()

    data class Reference(
        val year: Double,
        val altitudeKm: Double,
        val lat: Double,
        val lon: Double,
        val totalFieldNanoTesla: Double,
        val inclinationDeg: Double,
        val declinationDeg: Double,
    )

    private val references = listOf(
        Reference(2025.0, 0.0, 80.0, 0.0, 55178.5, 83.21, 1.28),
        Reference(2025.0, 0.0, 0.0, 120.0, 41064.3, -14.93, -0.16),
        Reference(2025.0, 0.0, -80.0, 240.0, 54698.2, -72.00, 68.78),
        Reference(2025.0, 100.0, 80.0, 0.0, 52964.9, 83.26, 0.85),
        Reference(2027.5, 0.0, 80.0, 0.0, 55253.9, 83.24, 2.59),
        Reference(2027.5, 100.0, 0.0, 120.0, 39007.4, -14.81, -0.23),
    )

    @Test
    fun matchesOfficialWmm2025TestValues() {
        for (r in references) {
            val field = wmm.fieldAtDecimalYear(r.lat, r.lon, r.altitudeKm, r.year)
            val context = "lat=${r.lat} lon=${r.lon} alt=${r.altitudeKm} year=${r.year}"
            assertEquals(r.declinationDeg, field.declinationDeg, 0.05, "declination ($context)")
            assertEquals(r.inclinationDeg, field.inclinationDeg, 0.05, "inclination ($context)")
            assertEquals(
                r.totalFieldNanoTesla,
                field.totalFieldMicroTesla * 1000.0,
                5.0,
                "total field ($context)",
            )
        }
    }
}
