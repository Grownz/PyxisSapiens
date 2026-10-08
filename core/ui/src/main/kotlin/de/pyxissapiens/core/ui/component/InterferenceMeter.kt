package de.pyxissapiens.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.pyxissapiens.core.ui.theme.LocalPyxisTokens
import java.util.Locale
import kotlin.math.abs

/**
 * Live magnetic field strength with distortion warning (docs/04 §4.2).
 * Warns when the measured field deviates from the expected WMM total field.
 */
@Composable
fun InterferenceMeter(
    microTesla: Float,
    modifier: Modifier = Modifier,
    expectedMicroTesla: Float? = null,
    toleranceMicroTesla: Float = 6f,
) {
    val tokens = LocalPyxisTokens.current
    val disturbed = expectedMicroTesla != null &&
        abs(microTesla - expectedMicroTesla) > toleranceMicroTesla
    val color = if (disturbed) tokens.critical else tokens.ok

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "|B| " + String.format(Locale.US, "%.1f", microTesla) + " µT",
            color = color,
            style = MaterialTheme.typography.labelMedium,
        )
        if (disturbed) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Magnetische Störung",
                color = color,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}
