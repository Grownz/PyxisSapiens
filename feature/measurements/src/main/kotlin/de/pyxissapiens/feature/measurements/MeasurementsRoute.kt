package de.pyxissapiens.feature.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.ui.component.EmptyState
import de.pyxissapiens.core.ui.component.MeasurementListItem
import de.pyxissapiens.core.ui.component.SectionHeader
import de.pyxissapiens.core.ui.icon.PyxisIcons
import java.util.Locale

@Composable
fun MeasurementsRoute(
    modifier: Modifier = Modifier,
    viewModel: MeasurementsViewModel = hiltViewModel(),
) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("Messungen (${measurements.size})")
        if (measurements.isEmpty()) {
            EmptyState(
                title = "Keine Messungen",
                subtitle = "Erfasse im Reiter „Messen\" die erste Messung.",
                icon = PyxisIcons.StrikeDip,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(measurements, key = { it.id }) { measurement ->
                    MeasurementListItem(
                        symbol = symbolFor(measurement.kind),
                        primary = primaryText(measurement),
                        secondary = secondaryText(measurement),
                        qualityScore = measurement.quality?.score,
                    )
                }
            }
        }
    }
}

private fun symbolFor(kind: MeasurementKind): ImageVector = when (kind) {
    MeasurementKind.PLANE -> PyxisIcons.StrikeDip
    MeasurementKind.LINE -> PyxisIcons.Lineation
    MeasurementKind.BEARING -> PyxisIcons.Bearing
}

private fun primaryText(m: Measurement): String = when (m.kind) {
    MeasurementKind.PLANE -> "${fmt(m.attitude?.dipDirection)} / ${fmt(m.attitude?.dip)}"
    MeasurementKind.LINE -> "${fmt(m.lineation?.trend)} → ${fmt(m.lineation?.plunge)}"
    MeasurementKind.BEARING -> "${fmt(m.bearingDeg)}°"
}

private fun secondaryText(m: Measurement): String {
    val kind = when (m.kind) {
        MeasurementKind.PLANE -> "Fläche"
        MeasurementKind.LINE -> "Linear"
        MeasurementKind.BEARING -> "Peilung"
    }
    return "Aufschluss · $kind"
}

private fun fmt(value: Double?): String =
    if (value == null) "–" else String.format(Locale.US, "%.1f", value)
