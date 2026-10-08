package de.pyxissapiens.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.DEFAULT_SITE_ID
import de.pyxissapiens.core.data.DataTypeRepository
import de.pyxissapiens.core.data.MeasurementEditManager
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.data.UnitRepository
import de.pyxissapiens.core.domain.model.Attitude
import de.pyxissapiens.core.domain.model.DataType
import de.pyxissapiens.core.domain.model.HistoryEntry
import de.pyxissapiens.core.domain.model.Lineation
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.domain.model.RockUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val repository: MeasurementRepository,
    private val editManager: MeasurementEditManager,
    dataTypes: DataTypeRepository,
    units: UnitRepository,
) : ViewModel() {

    private val filterType = MutableStateFlow<String?>(null)
    private val filterUnit = MutableStateFlow<String?>(null)

    val measurements: StateFlow<List<Measurement>> =
        combine(repository.observeAll(), filterType, filterUnit) { list, typeId, unitId ->
            list.filter { (typeId == null || it.typeId == typeId) && (unitId == null || it.unitId == unitId) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dataTypes: StateFlow<List<DataType>> =
        dataTypes.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val units: StateFlow<List<RockUnit>> =
        units.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedType: StateFlow<String?> = filterType
    val selectedUnit: StateFlow<String?> = filterUnit

    val canUndo: StateFlow<Boolean> = editManager.canUndo
    val canRedo: StateFlow<Boolean> = editManager.canRedo

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun setTypeFilter(typeId: String?) { filterType.value = typeId }
    fun setUnitFilter(unitId: String?) { filterUnit.value = unitId }

    fun delete(id: String) {
        viewModelScope.launch { if (editManager.delete(id)) _message.value = "Gelöscht – Undo möglich" }
    }

    fun undo() { viewModelScope.launch { if (editManager.undo()) _message.value = "Rückgängig" } }
    fun redo() { viewModelScope.launch { if (editManager.redo()) _message.value = "Wiederhergestellt" } }

    fun showHistory(id: String) { viewModelScope.launch { _history.value = repository.history(id) } }
    fun clearHistory() { _history.value = emptyList() }
    fun clearMessage() { _message.value = null }

    suspend fun load(id: String): Measurement? = repository.getMeasurement(id)

    fun save(
        existingId: String?,
        kind: MeasurementKind,
        primary: Double?,
        secondary: Double?,
        note: String?,
        typeId: String?,
        unitId: String?,
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val base = existingId?.let { repository.getMeasurement(it) }
                ?: Measurement(
                    id = UUID.randomUUID().toString(),
                    siteId = DEFAULT_SITE_ID,
                    kind = kind,
                    createdAt = now,
                    updatedAt = now,
                )
            val updated = base.copy(
                kind = kind,
                note = note?.takeIf { it.isNotBlank() },
                typeId = typeId,
                unitId = unitId,
                attitude = if (kind == MeasurementKind.PLANE) Attitude(primary ?: 0.0, secondary ?: 0.0) else null,
                lineation = if (kind == MeasurementKind.LINE) Lineation(primary ?: 0.0, secondary ?: 0.0) else null,
                bearingDeg = if (kind == MeasurementKind.BEARING) primary else null,
                updatedAt = now,
            )
            val warnings = if (existingId == null) editManager.add(updated) else editManager.update(updated)
            _message.value = if (warnings.isEmpty()) "Gespeichert" else warnings.joinToString("; ")
        }
    }
}
