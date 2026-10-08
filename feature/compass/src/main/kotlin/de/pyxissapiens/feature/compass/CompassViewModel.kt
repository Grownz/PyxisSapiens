package de.pyxissapiens.feature.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.CalibrationRepository
import de.pyxissapiens.core.data.DEFAULT_SITE_ID
import de.pyxissapiens.core.data.MeasurementEditManager
import de.pyxissapiens.core.data.ProjectRepository
import de.pyxissapiens.core.data.SiteRepository
import de.pyxissapiens.core.domain.measure.Calibration
import de.pyxissapiens.core.domain.measure.ContactSurface
import de.pyxissapiens.core.domain.measure.MeasurementEngine
import de.pyxissapiens.core.domain.measure.OrientationSample
import de.pyxissapiens.core.domain.model.Attitude
import de.pyxissapiens.core.domain.model.DataQuality
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Lineation
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.domain.model.NorthReference
import de.pyxissapiens.core.ports.DeviceFrame
import de.pyxissapiens.core.ports.GeomagneticPort
import de.pyxissapiens.core.ports.LocationPort
import de.pyxissapiens.core.ports.OrientationPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** A frozen (held) measurement awaiting save/discard. */
data class HeldMeasurement(
    val kind: MeasurementKind,
    val dip: Double?,
    val dipDirection: Double?,
    val trend: Double?,
    val plunge: Double?,
    val bearing: Double?,
    val quality: DataQuality,
    val northReference: NorthReference,
    val declinationDeg: Double?,
)

data class CompassUiState(
    val mode: MeasurementKind = MeasurementKind.PLANE,
    val contact: ContactSurface = ContactSurface.BACK_ON_PLANE,
    val geographic: Boolean = true,
    val liveDip: Double? = null,
    val liveDipDirection: Double? = null,
    val liveTrend: Double? = null,
    val livePlunge: Double? = null,
    val liveBearing: Double? = null,
    val stability: Float = 0f,
    val sampleCount: Int = 0,
    val microTesla: Float? = null,
    val expectedMicroTesla: Double? = null,
    val declinationDeg: Double? = null,
    val sensorAccuracy: Int? = null,
    val held: HeldMeasurement? = null,
    val savedCount: Int = 0,
    val statusMessage: String? = null,
)

@HiltViewModel
class CompassViewModel @Inject constructor(
    private val orientation: OrientationPort,
    private val geomagnetic: GeomagneticPort,
    private val location: LocationPort,
    private val projects: ProjectRepository,
    private val sites: SiteRepository,
    private val editManager: MeasurementEditManager,
    private val calibration: CalibrationRepository,
) : ViewModel() {

    private val buffer = ArrayDeque<DeviceFrame>()
    private var lastFix: GeoPoint? = null
    private var declination: Double? = null
    private var expectedMicroTesla: Double? = null
    private val tiltCorrection = calibration.tilt()?.correction
    private val magCalibration = calibration.magnetometer()

    private val _state = MutableStateFlow(CompassUiState())
    val state: StateFlow<CompassUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            projects.ensureDefaultProject()
            sites.ensureDefaultSite()
        }
        viewModelScope.launch {
            orientation.frames().collect { onFrame(it) }
        }
        viewModelScope.launch {
            orientation.magneticVectorMicroTesla().collect { raw ->
                val magnitude = magCalibration?.let { Calibration.correctedMagnitude(it, raw) } ?: raw.length
                _state.update { it.copy(microTesla = magnitude.toFloat()) }
            }
        }
        viewModelScope.launch {
            location.lastKnown()?.let { fix ->
                lastFix = GeoPoint(fix.latitude, fix.longitude, fix.altitudeMeters, fix.accuracyMeters)
                val field = geomagnetic.fieldAt(
                    fix.latitude, fix.longitude, fix.altitudeMeters ?: 0.0, System.currentTimeMillis(),
                )
                declination = field.declinationDeg
                expectedMicroTesla = field.totalFieldMicroTesla
                _state.update {
                    it.copy(
                        declinationDeg = field.declinationDeg,
                        expectedMicroTesla = field.totalFieldMicroTesla,
                    )
                }
            }
        }
    }

    fun setMode(mode: MeasurementKind) = _state.update { it.copy(mode = mode, held = null) }
    fun setContact(contact: ContactSurface) = _state.update { it.copy(contact = contact, held = null) }
    fun setGeographic(geographic: Boolean) = _state.update { it.copy(geographic = geographic) }

    fun hold() {
        val s = _state.value
        val samples = buffer.map { OrientationSample(it, it.magnetometerAccuracy) }
        if (samples.isEmpty()) return
        val north = northReference()
        when (s.mode) {
            MeasurementKind.PLANE -> {
                val r = MeasurementEngine.measurePlane(samples, s.contact)
                _state.update {
                    it.copy(
                        held = HeldMeasurement(
                            kind = s.mode,
                            dip = r.dip,
                            dipDirection = applyDeclination(r.dipDirection),
                            trend = null, plunge = null, bearing = null,
                            quality = r.quality, northReference = north, declinationDeg = declination,
                        ),
                    )
                }
            }
            MeasurementKind.LINE -> {
                val r = MeasurementEngine.measureLine(samples)
                _state.update {
                    it.copy(
                        held = HeldMeasurement(
                            kind = s.mode,
                            dip = null, dipDirection = null,
                            trend = applyDeclination(r.trend), plunge = r.plunge, bearing = null,
                            quality = r.quality, northReference = north, declinationDeg = declination,
                        ),
                    )
                }
            }
            MeasurementKind.BEARING -> {
                val r = MeasurementEngine.measureBearing(samples)
                _state.update {
                    it.copy(
                        held = HeldMeasurement(
                            kind = s.mode,
                            dip = null, dipDirection = null, trend = null, plunge = null,
                            bearing = applyDeclination(r.azimuth),
                            quality = r.quality, northReference = north, declinationDeg = declination,
                        ),
                    )
                }
            }
        }
    }

    fun discard() = _state.update { it.copy(held = null) }

    fun save() {
        val held = _state.value.held ?: return
        val now = System.currentTimeMillis()
        val measurement = Measurement(
            id = UUID.randomUUID().toString(),
            siteId = DEFAULT_SITE_ID,
            kind = held.kind,
            attitude = if (held.dip != null && held.dipDirection != null) {
                Attitude(held.dip, held.dipDirection, held.northReference)
            } else null,
            lineation = if (held.trend != null && held.plunge != null) {
                Lineation(held.trend, held.plunge, held.northReference)
            } else null,
            bearingDeg = held.bearing,
            location = lastFix,
            positionManual = false,
            quality = held.quality,
            createdAt = now,
            updatedAt = now,
        )
        viewModelScope.launch {
            editManager.add(measurement)
            _state.update { it.copy(held = null, savedCount = it.savedCount + 1, statusMessage = "Gespeichert") }
        }
    }

    private fun onFrame(frame: DeviceFrame) {
        val corrected = applyTilt(frame)
        while (buffer.size >= MAX_SAMPLES) buffer.removeFirst()
        buffer.addLast(corrected)
        recomputeLive()
    }

    private fun applyTilt(frame: DeviceFrame): DeviceFrame {
        val m = tiltCorrection ?: return frame
        return frame.copy(
            x = Calibration.applyMatrix(m, frame.x),
            y = Calibration.applyMatrix(m, frame.y),
            z = Calibration.applyMatrix(m, frame.z),
        )
    }

    private fun recomputeLive() {
        val s = _state.value
        val samples = buffer.map { OrientationSample(it, it.magnetometerAccuracy) }
        val sigma: Double
        when (s.mode) {
            MeasurementKind.PLANE -> {
                val r = MeasurementEngine.measurePlane(samples, s.contact)
                sigma = r.quality.sigmaDeg ?: 0.0
                _state.update {
                    it.copy(
                        liveDip = r.dip,
                        liveDipDirection = applyDeclination(r.dipDirection),
                        liveTrend = null, livePlunge = null, liveBearing = null,
                        sampleCount = r.quality.sampleCount,
                        sensorAccuracy = it.sensorAccuracy ?: r.quality.sensorAccuracy,
                        stability = stability(r.quality.sampleCount, sigma),
                    )
                }
            }
            MeasurementKind.LINE -> {
                val r = MeasurementEngine.measureLine(samples)
                sigma = r.quality.sigmaDeg ?: 0.0
                _state.update {
                    it.copy(
                        liveDip = null, liveDipDirection = null,
                        liveTrend = applyDeclination(r.trend), livePlunge = r.plunge, liveBearing = null,
                        sampleCount = r.quality.sampleCount,
                        sensorAccuracy = it.sensorAccuracy ?: r.quality.sensorAccuracy,
                        stability = stability(r.quality.sampleCount, sigma),
                    )
                }
            }
            MeasurementKind.BEARING -> {
                val r = MeasurementEngine.measureBearing(samples)
                sigma = r.quality.sigmaDeg ?: 0.0
                _state.update {
                    it.copy(
                        liveDip = null, liveDipDirection = null, liveTrend = null, livePlunge = null,
                        liveBearing = applyDeclination(r.azimuth),
                        sampleCount = r.quality.sampleCount,
                        sensorAccuracy = it.sensorAccuracy ?: r.quality.sensorAccuracy,
                        stability = stability(r.quality.sampleCount, sigma),
                    )
                }
            }
        }
    }

    private fun stability(sampleCount: Int, sigmaDeg: Double): Float {
        val coverage = (sampleCount.toFloat() / TARGET_SAMPLES).coerceAtMost(1f)
        val quiet = (1.0 - (sigmaDeg / 10.0)).coerceIn(0.0, 1.0)
        return (coverage * quiet).toFloat()
    }

    private fun northReference(): NorthReference =
        if (_state.value.geographic && declination != null) NorthReference.GEOGRAPHIC else NorthReference.MAGNETIC

    private fun applyDeclination(azimuth: Double): Double {
        val dec = if (_state.value.geographic) (declination ?: 0.0) else 0.0
        return norm360(azimuth + dec)
    }

    private fun norm360(deg: Double): Double {
        val m = deg % 360.0
        return if (m < 0) m + 360.0 else m
    }

    private companion object {
        const val MAX_SAMPLES = 40
        const val TARGET_SAMPLES = 10
    }
}
