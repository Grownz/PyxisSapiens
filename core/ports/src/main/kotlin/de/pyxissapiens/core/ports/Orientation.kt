package de.pyxissapiens.core.ports

import de.pyxissapiens.core.geology.math.Vector3
import kotlinx.coroutines.flow.Flow

/**
 * A fused device orientation expressed in a world ENU frame (X=East, Y=North, Z=Up):
 * each field is the corresponding device axis as a unit vector in world coordinates.
 *
 *  - [x]: device +X (to the right of the screen)
 *  - [y]: device +Y (towards the top of the screen)
 *  - [z]: device +Z (out of the screen, towards the user)
 */
data class DeviceFrame(
    val x: Vector3,
    val y: Vector3,
    val z: Vector3,
    val timestampNanos: Long,
    val magnetometerAccuracy: Int? = null,
)

/** Source of fused device orientation (e.g. Android's rotation-vector sensor). */
interface OrientationPort {
    /** Emits device orientation frames while collected. */
    fun frames(): Flow<DeviceFrame>

    /** Raw magnetic field magnitude in microtesla (used for interference detection). */
    fun magneticFieldMicroTesla(): Flow<Float>
}
