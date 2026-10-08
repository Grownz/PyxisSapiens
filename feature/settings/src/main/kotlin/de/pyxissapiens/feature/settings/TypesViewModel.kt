package de.pyxissapiens.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.DataTypeRepository
import de.pyxissapiens.core.data.StratigraphyClient
import de.pyxissapiens.core.data.UnitRepository
import de.pyxissapiens.core.domain.model.DataType
import de.pyxissapiens.core.domain.model.RockUnit
import de.pyxissapiens.core.ports.LocationPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TypesViewModel @Inject constructor(
    private val dataTypes: DataTypeRepository,
    private val units: UnitRepository,
    private val stratigraphy: StratigraphyClient,
    private val location: LocationPort,
) : ViewModel() {

    val dataTypeList: StateFlow<List<DataType>> =
        dataTypes.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unitList: StateFlow<List<RockUnit>> =
        units.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addDataType(name: String, colorHex: String, symbol: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dataTypes.upsert(DataType(id = UUID.randomUUID().toString(), name = name.trim(), colorHex = colorHex, symbol = symbol))
        }
    }

    fun deleteDataType(id: String) = viewModelScope.launch { dataTypes.delete(id) }

    fun addUnit(name: String, code: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            units.upsert(RockUnit(id = UUID.randomUUID().toString(), name = name.trim(), code = code.ifBlank { null }))
        }
    }

    fun deleteUnit(id: String) = viewModelScope.launch { units.delete(id) }

    private val _lookup = MutableStateFlow<List<String>>(emptyList())
    val lookup: StateFlow<List<String>> = _lookup

    private val _lookupMessage = MutableStateFlow<String?>(null)
    val lookupMessage: StateFlow<String?> = _lookupMessage

    fun lookupStratigraphy() {
        viewModelScope.launch {
            val fix = location.lastKnown()
            if (fix == null) { _lookupMessage.value = "Kein Standort verfügbar"; return@launch }
            _lookupMessage.value = "Abfrage läuft …"
            val result = stratigraphy.unitsAt(fix.latitude, fix.longitude)
            _lookup.value = result
            _lookupMessage.value = if (result.isEmpty()) "Keine Einheiten gefunden (offline?)" else "${result.size} Einheiten gefunden"
        }
    }

    fun adoptUnit(name: String) {
        viewModelScope.launch { units.upsert(RockUnit(id = UUID.randomUUID().toString(), name = name)) }
    }
}
