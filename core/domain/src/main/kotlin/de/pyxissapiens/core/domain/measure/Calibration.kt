package de.pyxissapiens.core.domain.measure

import de.pyxissapiens.core.geology.math.Vector3
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Magnetometer hard/soft-iron calibration (diagonal soft-iron scaling). */
@Serializable
data class MagnetometerCalibration(
    val offsetX: Double,
    val offsetY: Double,
    val offsetZ: Double,
    val scaleX: Double,
    val scaleY: Double,
    val scaleZ: Double,
    val quality: Double,
    val sampleCount: Int,
    val createdAt: Long,
) {
    val offset: Vector3 get() = Vector3(offsetX, offsetY, offsetZ)
    val radius: Double get() = 1.0 / ((scaleX + scaleY + scaleZ) / 3.0)
}

/** Contact-plane / camera-bump correction: a rotation applied to device frames. */
@Serializable
data class TiltCalibration(
    /** Row-major 3x3 rotation matrix. */
    val correction: List<Double>,
    val createdAt: Long,
)

/**
 * Simple, robust magnetometer calibration from an arbitrary rotation (figure-eight):
 * the bounding box of the samples gives the hard-iron offset (centre) and per-axis
 * soft-iron scaling normalises the field to a sphere. Quality reflects how spherical
 * the corrected data is and how well the rotation covered all axes.
 */
object Calibration {

    fun applyMatrix(m: List<Double>, v: Vector3): Vector3 = Vector3(
        m[0] * v.x + m[1] * v.y + m[2] * v.z,
        m[3] * v.x + m[4] * v.y + m[5] * v.z,
        m[6] * v.x + m[7] * v.y + m[8] * v.z,
    )

    fun applyMagnetometer(cal: MagnetometerCalibration, v: Vector3): Vector3 = Vector3(
        (v.x - cal.offsetX) * cal.scaleX,
        (v.y - cal.offsetY) * cal.scaleY,
        (v.z - cal.offsetZ) * cal.scaleZ,
    )

    fun fit(samples: List<Vector3>, now: Long = System.currentTimeMillis()): MagnetometerCalibration? {
        if (samples.size < 20) return null
        val minX = samples.minOf { it.x }; val maxX = samples.maxOf { it.x }
        val minY = samples.minOf { it.y }; val maxY = samples.maxOf { it.y }
        val minZ = samples.minOf { it.z }; val maxZ = samples.maxOf { it.z }
        val rX = (maxX - minX) / 2.0
        val rY = (maxY - minY) / 2.0
        val rZ = (maxZ - minZ) / 2.0
        if (rX < 1e-6 || rY < 1e-6 || rZ < 1e-6) return null

        val avg = (rX + rY + rZ) / 3.0

        val offsetX = (minX + maxX) / 2.0
        val offsetY = (minY + maxY) / 2.0
        val offsetZ = (minZ + maxZ) / 2.0

        // Quality: angular coverage of the rotation (fraction of occupied octants).
        val octants = BooleanArray(8)
        for (s in samples) {
            val dx = s.x - offsetX; val dy = s.y - offsetY; val dz = s.z - offsetZ
            if (dx * dx + dy * dy + dz * dz < 1e-9) continue
            val idx = (if (dx > 0) 1 else 0) or (if (dy > 0) 2 else 0) or (if (dz > 0) 4 else 0)
            octants[idx] = true
        }
        val coverage = octants.count { it } / 8.0

        return MagnetometerCalibration(
            offsetX = offsetX,
            offsetY = offsetY,
            offsetZ = offsetZ,
            scaleX = avg / rX,
            scaleY = avg / rY,
            scaleZ = avg / rZ,
            quality = coverage,
            sampleCount = samples.size,
            createdAt = now,
        )
    }

    /** Corrected total-field magnitude (microtesla) for interference detection. */
    fun correctedMagnitude(cal: MagnetometerCalibration, raw: Vector3): Double =
        applyMagnetometer(cal, raw).length

    /** Rotation that maps [from] to [to] (Rodrigues), row-major 3x3. */
    fun rotationFromTo(from: Vector3, to: Vector3): List<Double> {
        val a = from.normalized()
        val b = to.normalized()
        val v = a.cross(b)
        val c = a.dot(b)
        if (v.length < 1e-9) {
            return if (c > 0) listOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)
            else rotationAboutAxis(Vector3(1.0, 0.0, 0.0), Math.PI)
        }
        val k = 1.0 / (1.0 + c)
        val vx = doubleArrayOf(0.0, -v.z, v.y, v.z, 0.0, -v.x, -v.y, v.x, 0.0)
        val vx2 = multiply(vx, vx)
        val r = DoubleArray(9)
        for (i in 0 until 9) r[i] = (if (i % 4 == 0) 1.0 else 0.0) + vx[i] + vx2[i] * k
        return r.toList()
    }

    private fun rotationAboutAxis(axis: Vector3, angle: Double): List<Double> {
        val a = axis.normalized()
        val c = Math.cos(angle); val s = Math.sin(angle)
        return listOf(
            c + a.x * a.x * (1 - c), a.x * a.y * (1 - c) - a.z * s, a.x * a.z * (1 - c) + a.y * s,
            a.y * a.x * (1 - c) + a.z * s, c + a.y * a.y * (1 - c), a.y * a.z * (1 - c) - a.x * s,
            a.z * a.x * (1 - c) - a.y * s, a.z * a.y * (1 - c) + a.x * s, c + a.z * a.z * (1 - c),
        )
    }

    private fun multiply(a: DoubleArray, b: DoubleArray): DoubleArray {
        val r = DoubleArray(9)
        for (i in 0 until 3) for (j in 0 until 3) for (k in 0 until 3) r[i * 3 + j] += a[i * 3 + k] * b[k * 3 + j]
        return r
    }

    /** Builds a tilt correction that makes [referenceNormal] point up. */
    fun tiltFrom(referenceNormal: Vector3, now: Long = System.currentTimeMillis()): TiltCalibration =
        TiltCalibration(rotationFromTo(referenceNormal, Vector3(0.0, 0.0, 1.0)), now)
}
