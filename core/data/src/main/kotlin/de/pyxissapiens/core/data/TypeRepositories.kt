package de.pyxissapiens.core.data

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.database.entity.DataTypeEntity
import de.pyxissapiens.core.database.entity.UnitEntity
import de.pyxissapiens.core.domain.measure.MagnetometerCalibration
import de.pyxissapiens.core.domain.measure.TiltCalibration
import de.pyxissapiens.core.domain.model.DataType
import de.pyxissapiens.core.domain.model.RockUnit
import de.pyxissapiens.core.geology.math.Vector3
import de.pyxissapiens.core.ports.MagCalibration
import de.pyxissapiens.core.ports.MagCalibrationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataTypeRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeAll(): Flow<List<DataType>> = db.dataTypeDao().observeAll().map { rows -> rows.map { it.toDomain() } }
    suspend fun upsert(dataType: DataType) = db.dataTypeDao().upsert(dataType.toEntity())
    suspend fun delete(id: String) = db.dataTypeDao().delete(id)

    private fun DataTypeEntity.toDomain() = DataType(id, name, colorHex, symbol)
    private fun DataType.toEntity() = DataTypeEntity(id, name, colorHex, symbol)
}

@Singleton
class UnitRepository @Inject constructor(private val db: PyxisDatabase) {
    fun observeAll(): Flow<List<RockUnit>> = db.unitDao().observeAll().map { rows -> rows.map { it.toDomain() } }
    suspend fun upsert(unit: RockUnit) = db.unitDao().upsert(unit.toEntity())
    suspend fun delete(id: String) = db.unitDao().delete(id)

    private fun UnitEntity.toDomain() = RockUnit(id, name, parentId, code)
    private fun RockUnit.toEntity() = UnitEntity(id, name, parentId, code)
}

/** Persists device-specific calibration (per hardware fingerprint) as JSON. */
@Singleton
class CalibrationRepository @Inject constructor(
    @ApplicationContext context: Context,
) : MagCalibrationProvider {

    override fun magnetometerCalibration(): MagCalibration? = magnetometer()?.let {
        MagCalibration(
            offset = Vector3(it.offsetX, it.offsetY, it.offsetZ),
            scale = Vector3(it.scaleX, it.scaleY, it.scaleZ),
        )
    }

    private val prefs = context.getSharedPreferences("pyxis_calibration", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val deviceKey = (Build.FINGERPRINT ?: Build.MODEL).replace(Regex("[^A-Za-z0-9]"), "_")

    fun magnetometer(): MagnetometerCalibration? =
        prefs.getString("mag_$deviceKey", null)?.let {
            runCatching { json.decodeFromString<MagnetometerCalibration>(it) }.getOrNull()
        }

    fun saveMagnetometer(calibration: MagnetometerCalibration) {
        prefs.edit().putString("mag_$deviceKey", json.encodeToString(calibration)).apply()
    }

    fun tilt(): TiltCalibration? =
        prefs.getString("tilt_$deviceKey", null)?.let {
            runCatching { json.decodeFromString<TiltCalibration>(it) }.getOrNull()
        }

    fun saveTilt(calibration: TiltCalibration) {
        prefs.edit().putString("tilt_$deviceKey", json.encodeToString(calibration)).apply()
    }

    fun clear() {
        prefs.edit().remove("mag_$deviceKey").remove("tilt_$deviceKey").apply()
    }
}
