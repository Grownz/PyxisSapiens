package de.pyxissapiens.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.domain.model.Measurement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    repository: MeasurementRepository,
) : ViewModel() {

    val measurements: StateFlow<List<Measurement>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
