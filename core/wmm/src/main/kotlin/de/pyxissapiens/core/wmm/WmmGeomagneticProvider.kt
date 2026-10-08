package de.pyxissapiens.core.wmm

import de.pyxissapiens.core.ports.GeomagneticPort

/**
 * World Magnetic Model provider.
 *
 * Scaffold stub: returns a neutral field. The real implementation loads the WMM coefficient
 * file (bundled, offline) and evaluates the spherical-harmonic model to produce declination,
 * inclination and total field for the given location/time. See docs/03 §4.
 */
class WmmGeomagneticProvider : GeomagneticPort {

    override fun fieldAt(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double,
        epochMillis: Long,
    ): GeomagneticPort.Field {
        // TODO(wmm): evaluate bundled WMM coefficients (declination, inclination, |B|).
        return GeomagneticPort.Field(
            declinationDeg = 0.0,
            inclinationDeg = 0.0,
            totalFieldMicroTesla = 0.0,
        )
    }
}
