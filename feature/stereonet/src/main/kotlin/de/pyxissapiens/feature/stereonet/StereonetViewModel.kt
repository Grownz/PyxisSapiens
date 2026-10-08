package de.pyxissapiens.feature.stereonet

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.geology.stereo.Density
import de.pyxissapiens.core.geology.stereo.DensityMethod
import de.pyxissapiens.core.geology.stereo.KambKernel
import de.pyxissapiens.core.geology.stereo.Point2
import de.pyxissapiens.core.geology.stereo.Projection
import de.pyxissapiens.core.geology.stereo.Statistics
import de.pyxissapiens.core.geology.stereo.Stereonet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

enum class Dataset { PLANES, LINES }

data class StereonetControls(
    val projection: Projection = Projection.SCHMIDT,
    val method: DensityMethod = DensityMethod.KAMB,
    val kernel: KambKernel = KambKernel.LINEAR,
    val countingAngleDeg: Double = 10.0,
    val fisherKappa: Double = 30.0,
    val dataset: Dataset = Dataset.PLANES,
    val contourLevel: Double = 0.5,
    val showGreatCircles: Boolean = true,
    val showPoles: Boolean = true,
    val showLines: Boolean = true,
    val showDensity: Boolean = true,
)

/** Portable render model derived from the field data. */
data class StereonetRenderModel(
    val gridSize: Int = 0,
    val gridValues: DoubleArray = DoubleArray(0),
    val polePoints: List<Point2> = emptyList(),
    val greatCircles: List<List<Point2>> = emptyList(),
    val linePoints: List<Point2> = emptyList(),
    val contours: List<Pair<Point2, Point2>> = emptyList(),
    val count: Int = 0,
    val fisherText: String? = null,
    val eigenText: String? = null,
    val woodcockText: String? = null,
    val foldAxisText: String? = null,
    val meanPlaneText: String? = null,
)

data class StereonetUiState(
    val controls: StereonetControls = StereonetControls(),
    val model: StereonetRenderModel = StereonetRenderModel(),
    val heatmap: ImageBitmap? = null,
)

@HiltViewModel
class StereonetViewModel @Inject constructor(
    repository: MeasurementRepository,
) : ViewModel() {

    private val controlsState = MutableStateFlow(StereonetControls())
    private val measurements = repository.observeAll()

    val uiState: StateFlow<StereonetUiState> =
        combine(measurements, controlsState) { list, controls ->
            val model = buildModel(list, controls)
            StereonetUiState(controls, model, buildHeatmap(model.gridValues, model.gridSize))
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StereonetUiState())

    fun setProjection(p: Projection) = controlsState.update { it.copy(projection = p) }
    fun setMethod(m: DensityMethod) = controlsState.update { it.copy(method = m) }
    fun setKernel(k: KambKernel) = controlsState.update { it.copy(kernel = k) }
    fun setDataset(d: Dataset) = controlsState.update { it.copy(dataset = d) }
    fun setCountingAngle(a: Double) = controlsState.update { it.copy(countingAngleDeg = a.coerceIn(3.0, 30.0)) }
    fun setKappa(k: Double) = controlsState.update { it.copy(fisherKappa = k.coerceIn(5.0, 200.0)) }
    fun setContourLevel(l: Double) = controlsState.update { it.copy(contourLevel = l.coerceIn(0.1, 0.95)) }
    fun toggleGreatCircles() = controlsState.update { it.copy(showGreatCircles = !it.showGreatCircles) }
    fun togglePoles() = controlsState.update { it.copy(showPoles = !it.showPoles) }
    fun toggleLines() = controlsState.update { it.copy(showLines = !it.showLines) }
    fun toggleDensity() = controlsState.update { it.copy(showDensity = !it.showDensity) }

    private fun buildModel(measurements: List<Measurement>, c: StereonetControls): StereonetRenderModel {
        val planes = measurements.filter { it.kind == MeasurementKind.PLANE && it.attitude != null }
        val lines = measurements.filter { it.kind == MeasurementKind.LINE && it.lineation != null }

        val polePoints = planes.map { Stereonet.plotPlane(it.attitude!!.dipDirection, it.attitude!!.dip, c.projection) }
        val greatCircles = planes.map { Stereonet.greatCircle(it.attitude!!.dipDirection, it.attitude!!.dip, c.projection) }
        val linePoints = lines.map { Stereonet.plotLine(it.lineation!!.trend, it.lineation!!.plunge, c.projection) }

        val directions: List<Vector3> = when (c.dataset) {
            Dataset.PLANES -> planes.map { GeoMath.planeToPole(it.attitude!!.dipDirection, it.attitude!!.dip) }
            Dataset.LINES -> lines.map { GeoMath.lineationToVector(it.lineation!!.trend, it.lineation!!.plunge) }
        }

        val grid = Density.compute(
            directions = directions,
            projection = c.projection,
            method = c.method,
            gridSize = 121,
            countingAngleDeg = c.countingAngleDeg,
            kernel = c.kernel,
            fisherKappa = c.fisherKappa,
        )
        val contours = Density.contours(grid, c.contourLevel)

        var fisherText: String? = null
        var eigenText: String? = null
        var woodcockText: String? = null
        var foldAxisText: String? = null
        var meanPlaneText: String? = null

        if (directions.size >= 3) {
            val fisher = Statistics.fisher(directions)
            val (mt, mp) = GeoMath.vectorToLineation(fisher.mean)
            fisherText = "n=${fisher.n}  R̄=${fmt(fisher.meanResultantLength, 3)}  κ=${fmt(fisher.kappa, 1)}  α95=${fmt(fisher.alpha95Deg, 1)}°  Mittel ${fmt(mt, 0)}/${fmt(mp, 0)}"

            val eigen = Statistics.eigen(directions)
            eigenText = "S1=${fmt(eigen.values[0], 3)} S2=${fmt(eigen.values[1], 3)} S3=${fmt(eigen.values[2], 3)}"
            val woodcock = Statistics.woodcock(eigen)
            woodcockText = "Woodcock K=${fmt(woodcock.k, 2)} C=${fmt(woodcock.c, 2)}"
            val (ft, fp) = GeoMath.vectorToLineation(Statistics.foldAxis(eigen))
            foldAxisText = "Fold-Achse ${fmt(ft, 0)} → ${fmt(fp, 0)}°"
        }
        if (c.dataset == Dataset.PLANES && planes.size >= 3) {
            val (dd, dip) = Statistics.meanPlane(planes.map { GeoMath.planeToPole(it.attitude!!.dipDirection, it.attitude!!.dip) })
            meanPlaneText = "Mittlere Fläche ${fmt(dd, 0)}/${fmt(dip, 0)}"
        }

        return StereonetRenderModel(
            gridSize = grid.size,
            gridValues = grid.values,
            polePoints = polePoints,
            greatCircles = greatCircles,
            linePoints = linePoints,
            contours = contours,
            count = directions.size,
            fisherText = fisherText,
            eigenText = eigenText,
            woodcockText = woodcockText,
            foldAxisText = foldAxisText,
            meanPlaneText = meanPlaneText,
        )
    }

    private fun buildHeatmap(values: DoubleArray, size: Int): ImageBitmap? {
        if (size <= 0 || values.isEmpty()) return null
        val pixels = IntArray(size * size)
        for (iy in 0 until size) {
            // Flip vertically so North is up.
            val row = size - 1 - iy
            for (ix in 0 until size) {
                val t = values[row * size + ix].coerceIn(0.0, 1.0)
                if (t <= 0.02) {
                    pixels[iy * size + ix] = 0
                } else {
                    val hue = (240.0 * (1.0 - t)).toFloat()
                    val rgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.85f, 1f))
                    val alpha = (30 + 200 * t).toInt().coerceIn(0, 255)
                    pixels[iy * size + ix] = (rgb and 0x00FFFFFF) or (alpha shl 24)
                }
            }
        }
        val bitmap = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
        return bitmap.asImageBitmap()
    }

    private fun fmt(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)
}
