package de.pyxissapiens.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.ports.DeviceFrame
import de.pyxissapiens.core.ports.MagCalibrationProvider
import de.pyxissapiens.core.ports.OrientationPort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orientation from a **raw accelerometer + magnetometer fusion** with hard/soft-iron correction
 * applied before fusion (docs/03 §4). `SensorManager.getRotationMatrix` produces R mapping device
 * axes to the world ENU frame (X=East, Y=North, Z=Up); its columns are the device axes in world
 * coordinates — exactly what [DeviceFrame] expects.
 *
 * Falls back to the fused rotation-vector sensor when no magnetometer is available.
 */
@Singleton
class AndroidOrientationPort @Inject constructor(
    @ApplicationContext private val context: Context,
    private val magCalibration: MagCalibrationProvider,
) : OrientationPort {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Volatile private var lastAccuracy: Int? = null

    override fun frames(): Flow<DeviceFrame> {
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        return if (magnetometer != null) rawFusionFrames() else rotationVectorFrames()
    }

    private fun rawFusionFrames(): Flow<DeviceFrame> = callbackFlow {
        val accel = FloatArray(3)
        val mag = FloatArray(3)
        val rotation = FloatArray(9)
        var haveAccel = false
        var haveMag = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, accel, 0, 3); haveAccel = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, mag, 0, 3); haveMag = true
                    }
                }
                if (haveAccel && haveMag) {
                    val corrected = applyCalibration(mag)
                    if (SensorManager.getRotationMatrix(rotation, null, accel, corrected)) {
                        trySend(frameFrom(rotation, event.timestamp))
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) lastAccuracy = accuracy
            }
        }

        listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD).forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private fun rotationVectorFrames(): Flow<DeviceFrame> = callbackFlow {
        val rotation = FloatArray(9)
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                trySend(frameFrom(rotation, event.timestamp))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { lastAccuracy = accuracy }
        }
        if (sensor != null) sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private fun applyCalibration(raw: FloatArray): FloatArray {
        val cal = magCalibration.magnetometerCalibration() ?: return raw
        return floatArrayOf(
            ((raw[0] - cal.offset.x) * cal.scale.x).toFloat(),
            ((raw[1] - cal.offset.y) * cal.scale.y).toFloat(),
            ((raw[2] - cal.offset.z) * cal.scale.z).toFloat(),
        )
    }

    private fun frameFrom(r: FloatArray, timestampNanos: Long) = DeviceFrame(
        x = Vector3(r[0].toDouble(), r[3].toDouble(), r[6].toDouble()),
        y = Vector3(r[1].toDouble(), r[4].toDouble(), r[7].toDouble()),
        z = Vector3(r[2].toDouble(), r[5].toDouble(), r[8].toDouble()),
        timestampNanos = timestampNanos,
        magnetometerAccuracy = lastAccuracy,
    )

    override fun magneticFieldMicroTesla(): Flow<Float> = callbackFlow {
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
                val v = event.values
                trySend(kotlin.math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (magnetometer != null) sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun magneticVectorMicroTesla(): Flow<Vector3> = callbackFlow {
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
                val v = event.values
                trySend(Vector3(v[0].toDouble(), v[1].toDouble(), v[2].toDouble()))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { lastAccuracy = accuracy }
        }
        if (magnetometer != null) sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
