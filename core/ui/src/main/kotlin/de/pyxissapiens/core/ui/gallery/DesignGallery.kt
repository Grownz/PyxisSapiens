package de.pyxissapiens.core.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.pyxissapiens.core.ui.component.ClinometerGauge
import de.pyxissapiens.core.ui.component.CompassFace
import de.pyxissapiens.core.ui.component.EmptyState
import de.pyxissapiens.core.ui.component.HoldButton
import de.pyxissapiens.core.ui.component.InterferenceMeter
import de.pyxissapiens.core.ui.component.MeasurementListItem
import de.pyxissapiens.core.ui.component.NotationToggle
import de.pyxissapiens.core.ui.component.NorthRefToggle
import de.pyxissapiens.core.ui.component.NumericReadout
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.QualityBadge
import de.pyxissapiens.core.ui.component.SectionHeader
import de.pyxissapiens.core.ui.component.StabilizationRing
import de.pyxissapiens.core.ui.component.StatusPill
import de.pyxissapiens.core.ui.icon.PyxisIcons
import de.pyxissapiens.core.ui.theme.PyxisTheme

/**
 * Visual gallery of the design system (docs/04). Suited for on-device review and previews.
 */
@Composable
fun DesignGallery(modifier: Modifier = Modifier) {
    var geographic by remember { mutableStateOf(true) }
    var notation by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Design-System", style = MaterialTheme.typography.headlineSmall)

        SectionHeader("Messwerte / Readouts")
        PyxisPanel {
            Row {
                NumericReadout(label = "DIP", value = "34,2", unit = "°")
                NumericReadout(label = "DIP-DIR", value = "148", unit = "°")
            }
        }

        SectionHeader("Kompass & Klinometer")
        PyxisPanel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CompassFace(azimuthDeg = 148f, modifier = Modifier.size(140.dp))
                ClinometerGauge(dipDeg = 34f, modifier = Modifier.weight(1f).height(90.dp))
            }
        }

        SectionHeader("Stabilisierung")
        PyxisPanel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                StabilizationRing(progress = 0.65f, modifier = Modifier.size(88.dp), label = "27/40")
                StabilizationRing(progress = 1f, modifier = Modifier.size(64.dp), label = "OK")
            }
        }

        SectionHeader("Qualität & Sensor")
        PyxisPanel {
            QualityBadge(score = 88, uncertaintyDeg = 1.3)
            QualityBadge(score = 60, uncertaintyDeg = 2.4)
            QualityBadge(score = 30)
            InterferenceMeter(microTesla = 49f, expectedMicroTesla = 48f)
            InterferenceMeter(microTesla = 72f, expectedMicroTesla = 48f)
        }

        SectionHeader("Referenz & Notation")
        PyxisPanel {
            NorthRefToggle(geographic = geographic, onValueChange = { geographic = it })
            NotationToggle(
                options = listOf("DD/Dip", "RHR", "Quadrant"),
                selectedIndex = notation,
                onSelected = { notation = it },
            )
        }

        SectionHeader("Aktion & Status")
        PyxisPanel {
            HoldButton(text = "MESSEN", onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(text = "GPS ±3 m", color = MaterialTheme.colorScheme.tertiary)
                StatusPill(text = "Sensor GUT", color = MaterialTheme.colorScheme.primary)
            }
        }

        SectionHeader("Liste")
        PyxisPanel {
            MeasurementListItem(
                symbol = PyxisIcons.StrikeDip,
                primary = "148 / 34",
                secondary = "Bergkamm N-1 · bedding",
                qualityScore = 88,
            )
            MeasurementListItem(
                symbol = PyxisIcons.Lineation,
                primary = "032 → 18",
                secondary = "Bergkamm S-2 · lineation",
                qualityScore = 45,
            )
            MeasurementListItem(
                symbol = PyxisIcons.Foliation,
                primary = "210 / 42",
                secondary = "Bergkamm S-2 · foliation",
                qualityScore = 92,
            )
        }

        SectionHeader("Geologie-Symbole")
        PyxisPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(PyxisIcons.StrikeDip, null, Modifier.size(28.dp))
                Icon(PyxisIcons.Lineation, null, Modifier.size(28.dp))
                Icon(PyxisIcons.Foliation, null, Modifier.size(28.dp))
                Icon(PyxisIcons.Fault, null, Modifier.size(28.dp))
                Icon(PyxisIcons.Joint, null, Modifier.size(28.dp))
                Icon(PyxisIcons.Fold, null, Modifier.size(28.dp))
                Icon(PyxisIcons.Bearing, null, Modifier.size(28.dp))
            }
        }

        SectionHeader("Leerzustand")
        PyxisPanel(modifier = Modifier.height(220.dp)) {
            EmptyState(
                title = "Keine Messungen",
                subtitle = "Noch keine Daten erfasst.",
                icon = PyxisIcons.StrikeDip,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 1600)
@Composable
private fun DesignGalleryPreview() {
    PyxisTheme(darkTheme = true) { DesignGallery() }
}
