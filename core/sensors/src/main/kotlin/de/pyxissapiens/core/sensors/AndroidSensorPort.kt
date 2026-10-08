package de.pyxissapiens.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.ports.MotionSample
import de.pyxissapiens.core.ports.SensorPort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sensor port backed by the Android SensorManager. Scaffold: emits raw per-sensor samples.
 * The measurement pipeline (fusion, calibration, averaging) is added on top in the domain layer.
 */
@Singleton
class AndroidSensorPort @Inject constructor(
    @ApplicationContext private val context: Context,
) : SensorPort {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override fun isMagnetometerAvailable(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

    override fun isGyroscopeAvailable(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null

    override fun motion(): Flow<MotionSample> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val ts = event.timestamp
                val v = event.values
                val sample = when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER ->
                        MotionSample(ts, floatArrayOf(v[0], v[1], v[2]), null, null)
                    Sensor.TYPE_GYROSCOPE ->
                        MotionSample(ts, null, floatArrayOf(v[0], v[1], v[2]), null)
                    Sensor.TYPE_MAGNETIC_FIELD ->
                        MotionSample(ts, null, null, floatArrayOf(v[0], v[1], v[2]))
                    Sensor.TYPE_GRAVITY ->
                        MotionSample(ts, null, null, null, gravity = floatArrayOf(v[0], v[1], v[2]))
                    else -> null
                }
                if (sample != null) trySend(sample)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
        ).forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
