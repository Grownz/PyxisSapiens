package de.pyxissapiens.core.geology.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Right-handed vector in a local ENU frame:
 *  x = East, y = North, z = Up.
 */
data class Vector3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vector3) = Vector3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vector3) = Vector3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Double) = Vector3(x * s, y * s, z * s)

    fun dot(o: Vector3): Double = x * o.x + y * o.y + z * o.z

    fun cross(o: Vector3): Vector3 = Vector3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x,
    )

    val length: Double get() = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val l = length
        return if (l == 0.0) this else Vector3(x / l, y / l, z / l)
    }

    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
    }
}

/** Structural-geology orientation math (angles in degrees). */
object GeoMath {

    private const val DEG = 180.0 / Math.PI

    private fun norm360(deg: Double): Double {
        val m = deg % 360.0
        return if (m < 0) m + 360.0 else m
    }

    /** Upward pole (unit normal) of a plane given dip direction and dip. */
    fun planeToPole(dipDirectionDeg: Double, dipDeg: Double): Vector3 {
        val dd = dipDirectionDeg / DEG
        val d = dipDeg / DEG
        return Vector3(
            x = -sin(dd) * sin(d),
            y = -cos(dd) * sin(d),
            z = cos(d),
        ).normalized()
    }

    /** Inverse of [planeToPole]. Returns (dipDirection, dip). The pole is taken as the upward normal. */
    fun poleToPlane(pole: Vector3): Pair<Double, Double> {
        val n0 = pole.normalized()
        val n = if (n0.z < 0.0) n0 * -1.0 else n0
        val dip = Math.acos(n.z.coerceIn(-1.0, 1.0)) * DEG
        val dipDir = if (dip < 1e-7) 0.0 else norm360(atan2(-n.x, -n.y) * DEG)
        return dipDir to dip
    }

    /** Vector for a lineation given trend and plunge (points down-plunge, z = -sin(plunge)). */
    fun lineationToVector(trendDeg: Double, plungeDeg: Double): Vector3 {
        val t = trendDeg / DEG
        val p = plungeDeg / DEG
        return Vector3(
            x = sin(t) * cos(p),
            y = cos(t) * cos(p),
            z = -sin(p),
        ).normalized()
    }

    /** Inverse of [lineationToVector]. Returns (trend, plunge). */
    fun vectorToLineation(v: Vector3): Pair<Double, Double> {
        val n = v.normalized()
        val plunge = Math.asin((-n.z).coerceIn(-1.0, 1.0)) * DEG
        val trend = norm360(atan2(n.x, n.y) * DEG)
        return trend to plunge
    }

    /** Right-hand-rule strike from dip direction. */
    fun strikeFromDipDirection(dipDirectionDeg: Double): Double = norm360(dipDirectionDeg - 90.0)

    /** Dip direction from right-hand-rule strike. */
    fun dipDirectionFromStrike(strikeDeg: Double): Double = norm360(strikeDeg + 90.0)

    /** Smallest angle (degrees) between two directions. */
    fun angleBetween(a: Vector3, b: Vector3): Double {
        val d = a.normalized().dot(b.normalized()).coerceIn(-1.0, 1.0)
        return Math.acos(d) * DEG
    }

    /** Great-circle distance in metres between two WGS84 coordinates (haversine). */
    fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = (lat2 - lat1) / DEG
        val dLon = (lon2 - lon1) / DEG
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 / DEG) * cos(lat2 / DEG) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * Math.asin(sqrt(a))
    }
}
