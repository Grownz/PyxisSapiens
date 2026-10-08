package de.pyxissapiens.core.wmm

import de.pyxissapiens.core.ports.GeomagneticPort
import java.time.Instant
import java.time.Year
import java.time.ZoneOffset

/**
 * World Magnetic Model (WMM2025) provider.
 *
 * Uses the public-domain NOAA/Los Alamos reference implementation (`TSAGeoMag`), which reads the
 * bundled coefficient file `WMM.COF` from the classpath and evaluates the spherical-harmonic model
 * offline. Output is the declination, inclination and total field for a location and instant.
 *
 * Total intensity is returned in microtesla (the reference model works in nanotesla).
 */
class WmmGeomagneticProvider : GeomagneticPort {

    private val model = TSAGeoMag()

    override fun fieldAt(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double,
        epochMillis: Long,
    ): GeomagneticPort.Field = fieldAtDecimalYear(
        latitude = latitude,
        longitude = longitude,
        altitudeKm = altitudeMeters / 1000.0,
        year = decimalYear(epochMillis),
    )

    /** Direct evaluation for a decimal year (used by tests against the official test values). */
    fun fieldAtDecimalYear(
        latitude: Double,
        longitude: Double,
        altitudeKm: Double,
        year: Double,
    ): GeomagneticPort.Field {
        val declination = model.getDeclination(latitude, longitude, year, altitudeKm)
        val inclination = model.getDipAngle(latitude, longitude, year, altitudeKm)
        val totalNanoTesla = model.getIntensity(latitude, longitude, year, altitudeKm)
        return GeomagneticPort.Field(
            declinationDeg = declination,
            inclinationDeg = inclination,
            totalFieldMicroTesla = totalNanoTesla / 1000.0,
        )
    }

    private fun decimalYear(epochMillis: Long): Double {
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC)
        val daysInYear = if (Year.isLeap(zdt.year.toLong())) 366.0 else 365.0
        return zdt.year + zdt.dayOfYear / daysInYear
    }
}
