package de.pyxissapiens.core.geology.stereo

import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** Stereographic projection type (both lower hemisphere). */
enum class Projection { SCHMIDT, WULFF }

/** A point on the stereonet unit disk; x = East, y = North, radius <= 1. */
data class Point2(val x: Double, val y: Double) {
    val radius: Double get() = hypot(x, y)
}

/**
 * Stereographic projections and geometry for structural data (lower hemisphere).
 * Pure Kotlin, no Android dependencies.
 */
object Stereonet {

    private const val DEG = 180.0 / Math.PI

    private fun toRad(d: Double) = d / DEG

    /** Projects a direction onto the lower hemisphere. Vectors pointing up are flipped down. */
    fun project(v: Vector3, projection: Projection): Point2 {
        val u = (if (v.z > 0.0) v * -1.0 else v).normalized()
        val plunge = Math.asin((-u.z).coerceIn(-1.0, 1.0)) // >= 0
        val trend = atan2(u.x, u.y)
        val gamma = Math.PI / 2.0 - plunge // angle from the downward axis
        val r = when (projection) {
            Projection.SCHMIDT -> sqrt(2.0) * sin(gamma / 2.0)
            Projection.WULFF -> tan(gamma / 2.0)
        }
        return Point2(r * sin(trend), r * cos(trend))
    }

    /** Inverse of [project]; returns a downward unit vector. */
    fun unproject(p: Point2, projection: Projection): Vector3 {
        val r = p.radius.coerceAtMost(1.0 - 1e-9)
        val az = atan2(p.x, p.y)
        val gamma = when (projection) {
            Projection.SCHMIDT -> 2.0 * Math.asin((r / sqrt(2.0)).coerceIn(-1.0, 1.0))
            Projection.WULFF -> 2.0 * atan(r)
        }
        val plunge = Math.PI / 2.0 - gamma
        return Vector3(sin(az) * cos(plunge), cos(az) * cos(plunge), -sin(plunge)).normalized()
    }

    fun plotLine(trendDeg: Double, plungeDeg: Double, projection: Projection): Point2 =
        project(GeoMath.lineationToVector(trendDeg, plungeDeg), projection)

    fun plotPlane(dipDirectionDeg: Double, dipDeg: Double, projection: Projection): Point2 =
        project(GeoMath.planeToPole(dipDirectionDeg, dipDeg), projection)

    /** Samples a plane's great circle in the projection. */
    fun greatCircle(
        dipDirectionDeg: Double,
        dipDeg: Double,
        projection: Projection,
        steps: Int = 180,
    ): List<Point2> {
        val dd = toRad(dipDirectionDeg)
        val d = toRad(dipDeg)
        // strike direction (horizontal, in-plane) and down-dip direction
        val strike = Vector3(sin(dd - Math.PI / 2.0), cos(dd - Math.PI / 2.0), 0.0).normalized()
        val downDip = Vector3(sin(dd) * cos(d), cos(dd) * cos(d), -sin(d)).normalized()
        val points = ArrayList<Point2>(steps + 1)
        for (i in 0..steps) {
            val theta = Math.PI * i / steps
            val dir = (strike * cos(theta) + downDip * sin(theta)).normalized()
            points.add(project(dir, projection))
        }
        return points
    }

    /** Trend/plunge of a plane's pole (downward), for labelling. */
    fun poleTrendPlunge(dipDirectionDeg: Double, dipDeg: Double): Pair<Double, Double> {
        val n = GeoMath.planeToPole(dipDirectionDeg, dipDeg) * -1.0
        val trend = (atan2(n.x, n.y) * DEG + 360.0) % 360.0
        val plunge = Math.asin((-n.z).coerceIn(-1.0, 1.0)) * DEG
        return trend to plunge
    }

    /** Azimuths (deg) of the great-circle intersections with the horizontal (the strike of the plane). */
    fun strikeAzimuth(dipDirectionDeg: Double): Double = (dipDirectionDeg - 90.0 + 360.0) % 360.0
}
