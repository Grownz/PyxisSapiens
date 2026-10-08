package de.pyxissapiens.core.geology.stereo

import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Fisher statistics of a set of directions. */
data class FisherResult(
    val mean: Vector3,
    val n: Int,
    val resultantLength: Double,
    val meanResultantLength: Double,
    val kappa: Double,
    val alpha95Deg: Double,
)

/** Eigen decomposition of a symmetric orientation tensor (eigenvalues descending). */
data class Eigen3(
    val values: DoubleArray,
    val vectors: List<Vector3>,
) {
    val s1: Vector3 get() = vectors[0]
    val s2: Vector3 get() = vectors[1]
    val s3: Vector3 get() = vectors[2]
}

/** Woodcock shape parameters. */
data class Woodcock(val k: Double, val c: Double)

object Statistics {

    private const val DEG = 180.0 / Math.PI

    fun fisher(vectors: List<Vector3>): FisherResult {
        require(vectors.isNotEmpty()) { "No data" }
        var sum = Vector3.ZERO
        vectors.forEach { sum += it.normalized() }
        val n = vectors.size
        val r = sum.length
        val rbar = (r / n).coerceIn(0.0, 1.0)
        val kappa = kappa(rbar)
        val alpha95 = if (n > 1 && rbar > 0.0 && rbar < 1.0) {
            val a = Math.pow(0.05, -1.0 / (n - 1))
            val term = (n - r) / r * (a - 1.0)
            Math.acos((1.0 - term).coerceIn(-1.0, 1.0)) * DEG
        } else 0.0
        return FisherResult(sum.normalized(), n, r, rbar, kappa, alpha95)
    }

    /** Fisher concentration from mean resultant length (overflow-safe bisection). */
    fun kappa(rbar: Double): Double {
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

    fun orientationTensor(vectors: List<Vector3>): Array<DoubleArray> {
        require(vectors.isNotEmpty()) { "No data" }
        val t = Array(3) { DoubleArray(3) }
        vectors.forEach { v ->
            val n = v.normalized()
            val c = doubleArrayOf(n.x, n.y, n.z)
            for (i in 0..2) for (j in 0..2) t[i][j] += c[i] * c[j]
        }
        for (i in 0..2) for (j in 0..2) t[i][j] /= vectors.size
        return t
    }

    /** Eigenvalues/vectors of the orientation tensor via Jacobi rotations (3x3 symmetric). */
    fun eigen(vectors: List<Vector3>): Eigen3 {
        val a = orientationTensor(vectors)
        val n = 3
        val v = Array(n) { DoubleArray(n) }
        for (i in 0 until n) v[i][i] = 1.0
        val m = Array(n) { DoubleArray(n) }
        for (i in 0 until n) for (j in 0 until n) m[i][j] = a[i][j]

        var iter = 0
        while (iter < 100) {
            var p = 0; var q = 1; var max = abs(m[0][1])
            if (abs(m[0][2]) > max) { max = abs(m[0][2]); p = 0; q = 2 }
            if (abs(m[1][2]) > max) { max = abs(m[1][2]); p = 1; q = 2 }
            if (max < 1e-14) break
            val theta = 0.5 * atan2(2.0 * m[p][q], m[p][p] - m[q][q])
            val c = cos(theta)
            val s = sin(theta)
            for (k in 0 until n) {
                val mkp = m[k][p]; val mkq = m[k][q]
                m[k][p] = c * mkp - s * mkq
                m[k][q] = s * mkp + c * mkq
            }
            for (k in 0 until n) {
                val mpk = m[p][k]; val mqk = m[q][k]
                m[p][k] = c * mpk - s * mqk
                m[q][k] = s * mpk + c * mqk
            }
            for (k in 0 until n) {
                val vkp = v[k][p]; val vkq = v[k][q]
                v[k][p] = c * vkp - s * vkq
                v[k][q] = s * vkp + c * vkq
            }
            iter++
        }

        val idx = (0 until n).sortedByDescending { m[it][it] }
        val values = DoubleArray(n) { m[idx[it]][idx[it]] }
        val vectors = idx.map { j -> Vector3(v[0][j], v[1][j], v[2][j]).normalized() }
        return Eigen3(values, vectors)
    }

    /** Woodcock K (cluster>1, girdle<1) and C from the orientation tensor eigenvalues. */
    fun woodcock(eigen: Eigen3): Woodcock {
        val l1 = eigen.values[0].coerceAtLeast(1e-12)
        val l2 = eigen.values[1].coerceAtLeast(1e-12)
        val l3 = eigen.values[2].coerceAtLeast(1e-12)
        fun ln(x: Double) = Math.log(x)
        val denom = ln(l2 / l3)
        if (abs(denom) < 1e-9) return Woodcock(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        return Woodcock(ln(l1 / l3) / denom, ln(l1 / l2) / denom)
    }

    /** Best-fit girdle / fold axis: eigenvector of the smallest eigenvalue. */
    fun foldAxis(eigen: Eigen3): Vector3 = eigen.s3

    /** Mean plane from a set of plane normals; returns (dipDirection, dip). */
    fun meanPlane(normals: List<Vector3>): Pair<Double, Double> =
        GeoMath.poleToPlane(fisher(normals).mean)

    /** Intersection (beta axis) of two planes given by their normals. */
    fun intersectPlanes(n1: Vector3, n2: Vector3): Vector3 = n1.cross(n2).normalized()

    /** Histogram of azimuths into bins of [binDeg]. Returns counts per bin. */
    fun azimuthHistogram(azimuths: List<Double>, binDeg: Double): IntArray {
        val bins = (360.0 / binDeg).toInt().coerceAtLeast(1)
        val out = IntArray(bins)
        azimuths.forEach { a ->
            val norm = ((a % 360.0) + 360.0) % 360.0
            out[(norm / binDeg).toInt().coerceIn(0, bins - 1)]++
        }
        return out
    }

    /** Histogram of dip/plunge values (0..90) into bins of [binDeg]. */
    fun dipHistogram(dips: List<Double>, binDeg: Double): IntArray {
        val bins = (90.0 / binDeg).toInt().coerceAtLeast(1)
        val out = IntArray(bins)
        dips.forEach { d ->
            out[(d.coerceIn(0.0, 90.0) / binDeg).toInt().coerceIn(0, bins - 1)]++
        }
        return out
    }
}
