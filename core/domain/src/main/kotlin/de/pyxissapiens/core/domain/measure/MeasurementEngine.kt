package de.pyxissapiens.core.domain.measure

import de.pyxissapiens.core.domain.model.DataQuality
import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.ports.DeviceFrame
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/** Which device face is placed against the measured structure. */
enum class ContactSurface {
    /** Back of the phone on the plane, screen facing up: plane normal = device +Z. */
    BACK_ON_PLANE,

    /** Screen on the plane, back facing up: plane normal = -device +Z. */
    SCREEN_ON_PLANE,
}

data class OrientationSample(
    val frame: DeviceFrame,
    val sensorAccuracy: Int? = null,
    val magneticMicroTesla: Float? = null,
)

/** Result of the average/quality computation for a set of samples. */
data class AggregatedOrientation(
    val mean: Vector3,
    val sampleCount: Int,
    val meanResultantLength: Double,
    val sigmaDeg: Double,
    val kappa: Double,
)

sealed interface MeasureResult
data class PlaneResult(val dip: Double, val dipDirection: Double, val quality: DataQuality) : MeasureResult
data class LineResult(val trend: Double, val plunge: Double, val quality: DataQuality) : MeasureResult
data class BearingResult(val azimuth: Double, val quality: DataQuality) : MeasureResult

/**
 * Portable measurement core: converts device orientation frames into structural attitudes,
 * averages samples as unit vectors (never angles), rejects outliers and derives a quality score.
 *
 * Conventions (see docs/03 §4):
 *  - world ENU (X=East, Y=North, Z=Up); world = R * device for the Android rotation matrix,
 *    so [DeviceFrame] axes are the device basis vectors expressed in world coordinates.
 *  - plane normal is the upward normal; dip = angle to horizontal, dip direction = azimuth of
 *    the horizontal projection of the down-dip direction (GeoMath.poleToPlane).
 */
object MeasurementEngine {

    const val DEFAULT_TARGET_SAMPLES = 10
    const val DEFAULT_OUTLIER_DEG = 3.0

    // --- Single-sample conversions -------------------------------------------------------------

    fun planeNormal(frame: DeviceFrame, contact: ContactSurface): Vector3 {
        val z = frame.z.normalized()
        return if (contact == ContactSurface.BACK_ON_PLANE) z else z * -1.0
    }

    fun dipAndDipDirection(frame: DeviceFrame, contact: ContactSurface): Pair<Double, Double> =
        GeoMath.poleToPlane(planeNormal(frame, contact))

    /** Orientation vector of a lineation (normalised to point down-plunge, plunge >= 0). */
    fun lineationVector(frame: DeviceFrame): Vector3 {
        val y = frame.y.normalized()
        return if (y.z > 0.0) y * -1.0 else y
    }

    fun trendAndPlunge(frame: DeviceFrame): Pair<Double, Double> {
        val v = lineationVector(frame)
        val trend = norm360(Math.toDegrees(atan2(v.x, v.y)))
        val plunge = Math.toDegrees(asin((-v.z).coerceIn(-1.0, 1.0)))
        return trend to plunge
    }

    fun bearing(frame: DeviceFrame): Double {
        val y = frame.y.normalized()
        return norm360(Math.toDegrees(atan2(y.x, y.y)))
    }

    // --- Aggregation + measurement -------------------------------------------------------------

    fun aggregate(
        vectors: List<Vector3>,
        outlierDeg: Double = DEFAULT_OUTLIER_DEG,
    ): AggregatedOrientation {
        require(vectors.isNotEmpty()) { "At least one sample is required" }
        val unit = vectors.map { it.normalized() }

        var sum = Vector3.ZERO
        unit.forEach { sum += it }
        var mean = sum.normalized()

        val kept = unit.filter { GeoMath.angleBetween(it, mean) <= outlierDeg }
        val used = if (kept.isEmpty()) unit else kept

        sum = Vector3.ZERO
        used.forEach { sum += it }
        mean = sum.normalized()
        val rbar = (sum.length / used.size).coerceIn(0.0, 1.0)

        var sq = 0.0
        used.forEach { val a = GeoMath.angleBetween(it, mean); sq += a * a }
        val sigma = sqrt(sq / used.size)

        return AggregatedOrientation(
            mean = mean,
            sampleCount = used.size,
            meanResultantLength = rbar,
            sigmaDeg = sigma,
            kappa = fisherKappa(rbar),
        )
    }

    fun measurePlane(
        samples: List<OrientationSample>,
        contact: ContactSurface,
        targetSamples: Int = DEFAULT_TARGET_SAMPLES,
    ): PlaneResult {
        require(samples.isNotEmpty()) { "No samples" }
        val normals = samples.map { planeNormal(it.frame, contact) }
        val agg = aggregate(normals)
        val (dipDirection, dip) = GeoMath.poleToPlane(agg.mean)
        return PlaneResult(
            dip = dip,
            dipDirection = dipDirection,
            quality = qualityOf(agg, samples, targetSamples),
        )
    }

    fun measureLine(
        samples: List<OrientationSample>,
        targetSamples: Int = DEFAULT_TARGET_SAMPLES,
    ): LineResult {
        require(samples.isNotEmpty()) { "No samples" }
        val vectors = samples.map { lineationVector(it.frame) }
        val agg = aggregate(vectors)
        val trend = norm360(Math.toDegrees(atan2(agg.mean.x, agg.mean.y)))
        val plunge = Math.toDegrees(asin((-agg.mean.z).coerceIn(-1.0, 1.0)))
        return LineResult(
            trend = trend,
            plunge = plunge,
            quality = qualityOf(agg, samples, targetSamples),
        )
    }

    fun measureBearing(
        samples: List<OrientationSample>,
        targetSamples: Int = DEFAULT_TARGET_SAMPLES,
    ): BearingResult {
        require(samples.isNotEmpty()) { "No samples" }
        val vectors = samples.map { it.frame.y.normalized() }
        val agg = aggregate(vectors)
        val azimuth = norm360(Math.toDegrees(atan2(agg.mean.x, agg.mean.y)))
        return BearingResult(
            azimuth = azimuth,
            quality = qualityOf(agg, samples, targetSamples),
        )
    }

    // --- Quality -------------------------------------------------------------------------------

    /**
     * Fisher concentration parameter from the mean resultant length (3D).
     * Solves coth(k) - 1/k = rbar by bisection (monotonic, overflow-safe via 1/tanh).
     */
    fun fisherKappa(rbar: Double): Double {
        if (rbar <= 0.0) return 0.0
        if (rbar >= 1.0) return 1.0e7
        var lo = 1.0e-9
        var hi = 1.0e6
        repeat(200) {
            val mid = 0.5 * (lo + hi)
            val f = 1.0 / kotlin.math.tanh(mid) - 1.0 / mid - rbar
            if (f > 0.0) hi = mid else lo = mid
        }
        return 0.5 * (lo + hi)
    }

    /**
     * Heuristic 0..100 quality score combining sample count, angular dispersion and Android sensor
     * accuracy level (3 = high, 2 = medium, 1 = low, 0 = unreliable).
     */
    fun score(sampleCount: Int, targetSamples: Int, sigmaDeg: Double, sensorAccuracy: Int?): Int {
        var s = 100.0
        if (sampleCount < targetSamples && targetSamples > 0) {
            s -= (targetSamples - sampleCount) * (40.0 / targetSamples)
        }
        s -= (sigmaDeg / 3.0).coerceAtMost(1.0) * 30.0
        when (sensorAccuracy) {
            3 -> Unit
            null -> Unit
            2 -> s -= 10.0
            1 -> s -= 25.0
            0 -> s -= 40.0
        }
        return s.coerceIn(0.0, 100.0).toInt()
    }

    private fun qualityOf(
        agg: AggregatedOrientation,
        samples: List<OrientationSample>,
        targetSamples: Int,
    ): DataQuality {
        val accuracy = samples.mapNotNull { it.sensorAccuracy }.minOrNull()
        return DataQuality(
            score = score(agg.sampleCount, targetSamples, agg.sigmaDeg, accuracy),
            sensorAccuracy = accuracy,
            stabilityDeg = agg.sigmaDeg,
            sampleCount = agg.sampleCount,
            sigmaDeg = agg.sigmaDeg,
            kappa = agg.kappa,
        )
    }

    private fun norm360(deg: Double): Double {
        val m = deg % 360.0
        return if (m < 0) m + 360.0 else m
    }
}
