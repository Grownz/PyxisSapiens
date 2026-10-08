package de.pyxissapiens.core.geology.stereo

import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.exp

/** Vollmer-style counting-cricle kernels for the Kamb method. */
enum class KambKernel { RAW, LINEAR, EXPONENTIAL }

enum class DensityMethod { KAMB, FISHER_KERNEL }

/**
 * A density field sampled on a regular grid over the stereonet disk [-1,1]x[-1,1].
 * [values] are normalised to their maximum (0..1); [max] holds the raw maximum.
 * Node (ix,iy) maps to x = -1 + 2*ix/(size-1), y = -1 + 2*iy/(size-1).
 */
data class DensityGrid(val size: Int, val values: DoubleArray, val max: Double) {
    operator fun get(ix: Int, iy: Int): Double = values[iy * size + ix]
}

object Density {

    fun compute(
        directions: List<Vector3>,
        projection: Projection,
        method: DensityMethod,
        gridSize: Int = 121,
        countingAngleDeg: Double = 10.0,
        kernel: KambKernel = KambKernel.LINEAR,
        fisherKappa: Double = 30.0,
    ): DensityGrid {
        val data = directions.map { (if (it.z > 0.0) it * -1.0 else it).normalized() }
        val raw = DoubleArray(gridSize * gridSize)
        var max = 0.0
        for (iy in 0 until gridSize) {
            val y = -1.0 + 2.0 * iy / (gridSize - 1)
            for (ix in 0 until gridSize) {
                val x = -1.0 + 2.0 * ix / (gridSize - 1)
                val r = kotlin.math.hypot(x, y)
                var value = 0.0
                if (r <= 1.0) {
                    val g = Stereonet.unproject(Point2(x, y), projection)
                    value = when (method) {
                        DensityMethod.KAMB -> kambValue(g, data, countingAngleDeg, kernel)
                        DensityMethod.FISHER_KERNEL -> fisherValue(g, data, fisherKappa)
                    }
                }
                raw[iy * gridSize + ix] = value
                if (value > max) max = value
            }
        }
        val normalised = if (max > 0.0) DoubleArray(raw.size) { raw[it] / max } else raw
        return DensityGrid(gridSize, normalised, max)
    }

    private fun kambValue(
        g: Vector3,
        data: List<Vector3>,
        countingAngleDeg: Double,
        kernel: KambKernel,
    ): Double {
        if (data.isEmpty()) return 0.0
        val alpha = countingAngleDeg
        var sum = 0.0
        for (d in data) {
            val theta = GeoMath.angleBetween(g, d)
            if (theta > alpha) continue
            sum += when (kernel) {
                KambKernel.RAW -> 1.0
                KambKernel.LINEAR -> (alpha - theta) / alpha
                KambKernel.EXPONENTIAL -> exp(-2.0 * theta / alpha)
            }
        }
        // Normalise so a uniform distribution yields ~1 (approx via the mean kernel weight).
        val norm = when (kernel) {
            KambKernel.RAW -> 1.0 - cos(Math.toRadians(alpha))
            KambKernel.LINEAR -> 0.25 * (1.0 - cos(Math.toRadians(alpha)))
            KambKernel.EXPONENTIAL -> 0.5 * (1.0 - cos(Math.toRadians(alpha)))
        }.coerceAtLeast(1e-9)
        return sum / data.size / norm
    }

    private fun fisherValue(g: Vector3, data: List<Vector3>, kappa: Double): Double {
        if (data.isEmpty()) return 0.0
        var sum = 0.0
        for (d in data) sum += exp(kappa * (g.dot(d) - 1.0))
        return sum / data.size
    }

    /**
     * Marching-squares contour at [level] (0..1 of the normalised grid).
     * Returns unordered line segments in disk coordinates.
     */
    fun contours(grid: DensityGrid, level: Double): List<Pair<Point2, Point2>> {
        val n = grid.size
        val step = 2.0 / (n - 1)
        fun coord(i: Int) = -1.0 + i * step
        val segments = ArrayList<Pair<Point2, Point2>>()
        for (j in 0 until n - 1) {
            for (i in 0 until n - 1) {
                val a = grid[i, j]
                val b = grid[i + 1, j]
                val c = grid[i + 1, j + 1]
                val d = grid[i, j + 1]
                val x0 = coord(i); val x1 = coord(i + 1)
                val y0 = coord(j); val y1 = coord(j + 1)

                val bottom = interp(a, b, x0, y0, x1, y0, level)
                val right = interp(b, c, x1, y0, x1, y1, level)
                val top = interp(d, c, x0, y1, x1, y1, level)
                val left = interp(a, d, x0, y0, x0, y1, level)

                var mask = 0
                if (a > level) mask = mask or 1
                if (b > level) mask = mask or 2
                if (c > level) mask = mask or 4
                if (d > level) mask = mask or 8

                fun add(p: Point2?, q: Point2?) {
                    if (p != null && q != null) segments.add(p to q)
                }
                when (mask) {
                    1, 14 -> add(left, bottom)
                    2, 13 -> add(bottom, right)
                    3, 12 -> add(left, right)
                    4, 11 -> add(right, top)
                    6, 9 -> add(bottom, top)
                    7, 8 -> add(left, top)
                    5 -> { add(left, top); add(bottom, right) }
                    10 -> { add(left, bottom); add(right, top) }
                }
            }
        }
        return segments
    }

    private fun interp(
        va: Double, vb: Double,
        xa: Double, ya: Double, xb: Double, yb: Double,
        level: Double,
    ): Point2? {
        val dv = vb - va
        if (kotlin.math.abs(dv) < 1e-12) return null
        val t = ((level - va) / dv).coerceIn(0.0, 1.0)
        return Point2(xa + (xb - xa) * t, ya + (yb - ya) * t)
    }
}
