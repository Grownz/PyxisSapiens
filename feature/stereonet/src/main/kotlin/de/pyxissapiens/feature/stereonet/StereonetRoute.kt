package de.pyxissapiens.feature.stereonet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StereonetRoute(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Auswertung / Analysis", style = MaterialTheme.typography.headlineSmall)
        Text("Platzhalter für Stereonet, Statistik, Rose und Hangstabilität (Epic 4, R2).", style = MaterialTheme.typography.bodyMedium)
    }
}
