package de.pyxissapiens.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.ports.DeviceFrame
import de.pyxissapiens.core.ports.OrientationPort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orientation source based on Android's fused rotation-vector sensor.
 *
 * The rotation matrix R from `getRotationMatrixFromVector` maps a device vector to the world ENU
 * frame (X=East, Y=North, Z=Up). Its columns are therefore the device axes expressed in world
 * coordinates, which is exactly what [DeviceFrame] expects.
 *
 * Hard/soft-iron calibration, magnetic declination, averaging and quality are applied in the
 * domain layer (see MeasurementEngine), so this adapter stays a thin sensor bridge.
 */
@Singleton
class AndroidOrientationPort @Inject constructor(
    @ApplicationContext private val context: Context,
) : OrientationPort {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override fun frames(): Flow<DeviceFrame> = callbackFlow {
        val rotationMatrix = FloatArray(9)
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val frame = DeviceFrame(
                    x = Vector3(rotationMatrix[0].toDouble(), rotationMatrix[3].toDouble(), rotationMatrix[6].toDouble()),
                    y = Vector3(rotationMatrix[1].toDouble(), rotationMatrix[4].toDouble(), rotationMatrix[7].toDouble()),
                    z = Vector3(rotationMatrix[2].toDouble(), rotationMatrix[5].toDouble(), rotationMatrix[8].toDouble()),
                    timestampNanos = event.timestamp,
                    magnetometerAccuracy = lastAccuracy,
                )
                trySend(frame)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                lastAccuracy = accuracy
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    override fun magneticFieldMicroTesla(): Flow<Float> = callbackFlow {
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
                val v = event.values
                val magnitude = kotlin.math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
                trySend(magnitude)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (magnetometer != null) {
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
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
        if (magnetometer != null) {
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    @Volatile
    private var lastAccuracy: Int? = null
}
