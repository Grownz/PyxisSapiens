package de.pyxissapiens.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Magnetic vs. geographic north selector (docs/04 §4.2). */
@Composable
fun NorthRefToggle(
    geographic: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = geographic,
            onClick = { onValueChange(true) },
            label = { Text("Geo") },
        )
        FilterChip(
            selected = !geographic,
            onClick = { onValueChange(false) },
            label = { Text("Magnetisch") },
        )
    }
}

/** Generic single-choice chip row, e.g. for notation (DD/Dip, RHR, Quadrant). */
@Composable
fun NotationToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
                label = { Text(label) },
            )
        }
    }
}
