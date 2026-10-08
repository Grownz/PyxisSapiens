package de.pyxissapiens.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.CalibrationRepository
import de.pyxissapiens.core.domain.measure.Calibration
import de.pyxissapiens.core.domain.measure.MagnetometerCalibration
import de.pyxissapiens.core.domain.measure.TiltCalibration
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.ports.OrientationPort
import de.pyxissapiens.core.ports.SensorPort
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalibrationUiState(
    val magnetometerAvailable: Boolean = false,
    val gyroscopeAvailable: Boolean = false,
    val magnetometer: MagnetometerCalibration? = null,
    val tilt: TiltCalibration? = null,
    val measuring: Boolean = false,
    val sampleCount: Int = 0,
    val message: String? = null,
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val orientation: OrientationPort,
    private val sensorPort: SensorPort,
    private val calibration: CalibrationRepository,
) : ViewModel() {

    private val samples = ArrayList<Vector3>()
    private var job: Job? = null

    private val _state = MutableStateFlow(
        CalibrationUiState(
            magnetometerAvailable = sensorPort.isMagnetometerAvailable(),
            gyroscopeAvailable = sensorPort.isGyroscopeAvailable(),
            magnetometer = calibration.magnetometer(),
            tilt = calibration.tilt(),
        ),
    )
    val state: StateFlow<CalibrationUiState> = _state.asStateFlow()

    fun startMagCalibration() {
        samples.clear()
        job?.cancel()
        _state.update { it.copy(measuring = true, sampleCount = 0, message = "Gerät in Achten drehen …") }
        job = viewModelScope.launch {
            orientation.magneticVectorMicroTesla().collect { v ->
                if (samples.size < 3000) samples.add(v)
                _state.update { it.copy(sampleCount = samples.size) }
            }
        }
    }

    fun stopMagCalibration() {
        job?.cancel()
        job = null
        val fitted = Calibration.fit(samples)
        if (fitted == null) {
            _state.update { it.copy(measuring = false, message = "Nicht genug Daten – weiter drehen") }
            return
        }
        calibration.saveMagnetometer(fitted)
        _state.update {
            it.copy(
                measuring = false, magnetometer = fitted,
                message = "Magnetometer kalibriert (Qualität ${(fitted.quality * 100).toInt()} %)",
            )
        }
    }

    fun captureTiltReference() {
        job?.cancel()
        _state.update { it.copy(message = "Gerät flach auf Referenz halten …") }
        job = viewModelScope.launch {
            var sum = Vector3.ZERO
            var n = 0
            orientation.frames().take(60).collect { frame ->
                sum += frame.z.normalized()
                n++
            }
            if (n > 0) {
                val normal = sum.normalized()
                val tilt = Calibration.tiltFrom(normal)
                calibration.saveTilt(tilt)
                _state.update { it.copy(tilt = tilt, message = "Auflageflächen-Korrektur gespeichert") }
            }
        }
    }

    fun clearAll() {
        calibration.clear()
        _state.value = _state.value.copy(magnetometer = null, tilt = null, message = "Kalibrierung gelöscht")
    }
}
