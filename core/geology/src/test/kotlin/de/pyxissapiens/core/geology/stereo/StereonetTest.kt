package de.pyxissapiens.core.geology.stereo

import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StereonetTest {

    private fun near(trend: Double, plunge: Double, i: Int, jitterRad: Double = 0.02): Vector3 {
        val t = Math.toRadians(trend + jitterRad * 57.2957795 * Math.sin(i.toDouble()))
        val p = Math.toRadians(plunge + jitterRad * 57.2957795 * Math.cos(i * 1.7))
        return Vector3(Math.sin(t) * Math.cos(p), Math.cos(t) * Math.cos(p), -Math.sin(p)).normalized()
    }

    @Test
    fun projectionRoundTrip() {
        for (projection in Projection.values()) {
            val cases = listOf(0.0 to 0.0, 90.0 to 0.0, 148.0 to 34.0, 250.0 to 75.0, 45.0 to 89.0)
            for ((trend, plunge) in cases) {
                val p = Stereonet.plotLine(trend, plunge, projection)
                val (t2, p2) = GeoMath.vectorToLineation(Stereonet.unproject(p, projection))
                assertEquals(trend, t2, 1e-3, "$projection trend $trend/$plunge")
                assertEquals(plunge, p2, 1e-3, "$projection plunge $trend/$plunge")
            }
        }
    }

    @Test
    fun horizontalLineIsOnRim() {
        for (projection in Projection.values()) {
            val p = Stereonet.plotLine(120.0, 0.0, projection)
            assertEquals(1.0, p.radius, 1e-6, projection.toString())
        }
    }

    @Test
    fun verticalLineIsInCentre() {
        for (projection in Projection.values()) {
            val p = Stereonet.plotLine(200.0, 90.0, projection)
            assertEquals(0.0, p.radius, 1e-9, projection.toString())
        }
    }

    @Test
    fun greatCircleStaysInDisk() {
        for (projection in Projection.values()) {
            val pts = Stereonet.greatCircle(120.0, 40.0, projection)
            assertTrue(pts.isNotEmpty())
            assertTrue(pts.all { it.radius <= 1.0 + 1e-6 })
        }
    }

    @Test
    fun fisherOfCluster() {
        val vectors = (0 until 40).map { near(120.0, 30.0, it) }
        val result = Statistics.fisher(vectors)
        val (trend, plunge) = GeoMath.vectorToLineation(result.mean)
        assertEquals(120.0, trend, 3.0)
        assertEquals(30.0, plunge, 3.0)
        assertTrue(result.kappa > 100.0, "kappa=${result.kappa}")
        assertTrue(result.alpha95Deg < 5.0, "alpha95=${result.alpha95Deg}")
    }

    @Test
    fun eigenValuesSumToOne() {
        val vectors = (0 until 30).map { near(120.0, 30.0, it) }
        val eigen = Statistics.eigen(vectors)
        val sum = eigen.values.sum()
        assertEquals(1.0, sum, 1e-6)
        assertTrue(eigen.values[0] >= eigen.values[1] && eigen.values[1] >= eigen.values[2])
    }

    @Test
    fun girdleFoldAxisIsVertical() {
        // Horizontal lines span a horizontal great circle; its pole (fold axis) is vertical.
        val vectors = (0 until 36).map {
            val t = Math.toRadians(it * 10.0)
            Vector3(Math.sin(t), Math.cos(t), 0.0).normalized()
        }
        val eigen = Statistics.eigen(vectors)
        val axis = Statistics.foldAxis(eigen)
        assertEquals(1.0, abs(axis.z), 0.02, "fold axis should be vertical, was $axis")
        assertTrue(eigen.values[2] < 0.05, "smallest eigenvalue=${eigen.values[2]}")
    }

    @Test
    fun woodcockClusterExceedsGirdle() {
        val cluster = Statistics.woodcock(Statistics.eigen((0 until 40).map { near(120.0, 30.0, it) }))
        val girdle = Statistics.woodcock(
            Statistics.eigen((0 until 36).map { val t = Math.toRadians(it * 10.0); Vector3(Math.sin(t), Math.cos(t), 0.0) }),
        )
        assertTrue(cluster.k > girdle.k, "cluster K=${cluster.k} girdle K=${girdle.k}")
    }

    @Test
    fun planeIntersectionIsPerpendicularToBoth() {
        val n1 = GeoMath.planeToPole(120.0, 30.0)
        val n2 = GeoMath.planeToPole(200.0, 60.0)
        val beta = Statistics.intersectPlanes(n1, n2)
        assertEquals(90.0, GeoMath.angleBetween(beta, n1), 1e-3)
        assertEquals(90.0, GeoMath.angleBetween(beta, n2), 1e-3)
    }

    @Test
    fun azimuthHistogramCountsAll() {
        val az = listOf(0.0, 350.0, 10.0, 120.0, 120.0)
        val hist = Statistics.azimuthHistogram(az, 10.0)
        assertEquals(az.size, hist.sum())
    }

    @Test
    fun densityPeaksNearClusterAndContoursExist() {
        val directions = (0 until 50).map { near(120.0, 30.0, it, 0.05) }
        val grid = Density.compute(
            directions = directions,
            projection = Projection.SCHMIDT,
            method = DensityMethod.KAMB,
            gridSize = 61,
            countingAngleDeg = 12.0,
            kernel = KambKernel.LINEAR,
        )
        // Find the grid node with maximum density and check it projects near the cluster.
        var bestIx = 0; var bestIy = 0; var best = -1.0
        for (iy in 0 until grid.size) for (ix in 0 until grid.size) {
            val v = grid[ix, iy]
            if (v > best) { best = v; bestIx = ix; bestIy = iy }
        }
        val x = -1.0 + 2.0 * bestIx / (grid.size - 1)
        val y = -1.0 + 2.0 * bestIy / (grid.size - 1)
        val (trend, plunge) = GeoMath.vectorToLineation(Stereonet.unproject(Point2(x, y), Projection.SCHMIDT))
        assertEquals(120.0, trend, 12.0)
        assertEquals(30.0, plunge, 12.0)
        assertEquals(1.0, grid.max.let { grid.values.max() }, 1e-9)
        assertTrue(Density.contours(grid, 0.5).isNotEmpty())
    }

    @Test
    fun fisherKernelDensityProducesField() {
        val directions = (0 until 20).map { near(300.0, 50.0, it, 0.04) }
        val grid = Density.compute(
            directions, Projection.WULFF, DensityMethod.FISHER_KERNEL,
            gridSize = 41, fisherKappa = 40.0,
        )
        assertTrue(grid.max > 0.0)
        assertTrue(grid.values.any { it > 0.9 })
    }
}
