package de.pyxissapiens.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.MeasurementEditManager
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.domain.model.HistoryEntry
import de.pyxissapiens.core.domain.model.Measurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val repository: MeasurementRepository,
    private val editManager: MeasurementEditManager,
) : ViewModel() {

    val measurements: StateFlow<List<Measurement>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val canUndo: StateFlow<Boolean> = editManager.canUndo
    val canRedo: StateFlow<Boolean> = editManager.canRedo

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun delete(id: String) {
        viewModelScope.launch {
            if (editManager.delete(id)) _message.value = "Gelöscht – Undo möglich"
        }
    }

    fun undo() {
        viewModelScope.launch { if (editManager.undo()) _message.value = "Rückgängig" }
    }

    fun redo() {
        viewModelScope.launch { if (editManager.redo()) _message.value = "Wiederhergestellt" }
    }

    fun showHistory(id: String) {
        viewModelScope.launch { _history.value = repository.history(id) }
    }

    fun clearHistory() { _history.value = emptyList() }
    fun clearMessage() { _message.value = null }
}
