package de.pyxissapiens.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.SectionHeader

@Composable
fun TypesRoute(
    modifier: Modifier = Modifier,
    viewModel: TypesViewModel = hiltViewModel(),
) {
    val types by viewModel.dataTypeList.collectAsStateWithLifecycle()
    val units by viewModel.unitList.collectAsStateWithLifecycle()

    var typeName by remember { mutableStateOf("") }
    var typeColor by remember { mutableStateOf("#FFB000") }
    var unitName by remember { mutableStateOf("") }
    var unitCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Datentypen")
        PyxisPanel {
            types.forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("● ${type.name}", color = parseColor(type.colorHex), modifier = Modifier.weight(1f))
                    Text(type.symbol, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { viewModel.deleteDataType(type.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                    }
                }
            }
            OutlinedTextField(value = typeName, onValueChange = { typeName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = typeColor, onValueChange = { typeColor = it }, label = { Text("Farbe (#RRGGBB)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.addDataType(typeName, typeColor, "STRIKE_DIP"); typeName = "" }) { Text("Datentyp hinzufügen") }
        }

        SectionHeader("Einheiten / Stratigraphie")
        PyxisPanel {
            units.forEach { unit ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(unit.code?.let { "$it · ${unit.name}" } ?: unit.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteUnit(unit.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                    }
                }
            }
            OutlinedTextField(value = unitName, onValueChange = { unitName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = unitCode, onValueChange = { unitCode = it }, label = { Text("Kürzel (optional)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.addUnit(unitName, unitCode); unitName = ""; unitCode = "" }) { Text("Einheit hinzufügen") }
        }
    }
}

private fun parseColor(hex: String) = runCatching {
    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(androidx.compose.ui.graphics.Color(0xFFFFB000))
