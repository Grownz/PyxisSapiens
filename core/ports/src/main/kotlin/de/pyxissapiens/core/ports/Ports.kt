package de.pyxissapiens.core.ports

import kotlinx.coroutines.flow.Flow

/**
 * Platform ports. The domain layer depends on these interfaces only; concrete implementations
 * live in the Android-only modules (sensors, location, media, ...). This keeps the domain
 * Android-free and portable.
 */

/** One raw/derived device motion sample. */
data class MotionSample(
    val timestampNanos: Long,
    val accel: FloatArray?,          // x,y,z m/s^2
    val gyro: FloatArray?,           // x,y,z rad/s
    val magnetometer: FloatArray?,   // x,y,z uT
    val gravity: FloatArray? = null, // x,y,z m/s^2
    val pressureHpa: Float? = null,
) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

/** Source of fused/raw orientation sensor data. */
interface SensorPort {
    fun motion(): Flow<MotionSample>
    fun isMagnetometerAvailable(): Boolean
    fun isGyroscopeAvailable(): Boolean
}

data class FixSample(
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val accuracyMeters: Float?,
)

interface LocationPort {
    fun fixes(): Flow<FixSample>
    suspend fun lastKnown(): FixSample?
}

/** Provides magnetic declination/inclination and total field for a location & time. */
interface GeomagneticPort {
    data class Field(val declinationDeg: Double, val inclinationDeg: Double, val totalFieldMicroTesla: Double)
    fun fieldAt(latitude: Double, longitude: Double, altitudeMeters: Double, epochMillis: Long): Field
}
