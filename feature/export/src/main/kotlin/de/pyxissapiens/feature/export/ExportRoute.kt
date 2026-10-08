package de.pyxissapiens.feature.export

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
fun ExportRoute(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Export / Import", style = MaterialTheme.typography.headlineSmall)
        Text("Platzhalter für CSV, GeoJSON/KML/KMZ, PDF und Projektarchiv (Epic 3).", style = MaterialTheme.typography.bodyMedium)
    }
}
