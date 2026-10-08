package de.pyxissapiens.core.domain.measure

import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind

/** Domain-level validation of a measurement before persisting. */
object MeasurementValidator {

    fun validate(m: Measurement): List<String> {
        val warnings = ArrayList<String>()
        m.attitude?.let {
            if (it.dip !in 0.0..90.0) warnings.add("Dip außerhalb 0–90° (${it.dip})")
            if (it.dipDirection !in 0.0..360.0) warnings.add("Dip-Direction außerhalb 0–360° (${it.dipDirection})")
        }
        m.lineation?.let {
            if (it.trend !in 0.0..360.0) warnings.add("Trend außerhalb 0–360° (${it.trend})")
            if (it.plunge !in 0.0..90.0) warnings.add("Plunge außerhalb 0–90° (${it.plunge})")
        }
        m.bearingDeg?.let {
            if (it !in 0.0..360.0) warnings.add("Peilung außerhalb 0–360° ($it)")
        }
        m.location?.let {
            if (it.latitude !in -90.0..90.0) warnings.add("Breite außerhalb -90–90° (${it.latitude})")
            if (it.longitude !in -180.0..180.0) warnings.add("Länge außerhalb -180–180° (${it.longitude})")
        }
        when (m.kind) {
            MeasurementKind.PLANE -> if (m.attitude == null) warnings.add("Fläche ohne Dip/Dip-Direction")
            MeasurementKind.LINE -> if (m.lineation == null) warnings.add("Linear ohne Trend/Plunge")
            MeasurementKind.BEARING -> if (m.bearingDeg == null) warnings.add("Peilung ohne Azimut")
        }
        return warnings
    }
}
