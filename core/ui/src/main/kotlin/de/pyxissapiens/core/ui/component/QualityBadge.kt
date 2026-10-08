package de.pyxissapiens.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.pyxissapiens.core.ui.theme.LocalPyxisTokens
import java.util.Locale

enum class QualityLevel { GOOD, FAIR, POOR }

fun qualityLevelFor(score: Int): QualityLevel = when {
    score >= 80 -> QualityLevel.GOOD
    score >= 50 -> QualityLevel.FAIR
    else -> QualityLevel.POOR
}

/**
 * Quality badge for a measurement: symbol + text + optional uncertainty (docs/04 §4.2).
 * Quality is never conveyed by colour alone (an accessibility requirement).
 */
@Composable
fun QualityBadge(
    score: Int,
    modifier: Modifier = Modifier,
    uncertaintyDeg: Double? = null,
) {
    val tokens = LocalPyxisTokens.current
    val (color, symbol, text) = when (qualityLevelFor(score)) {
        QualityLevel.GOOD -> Triple(tokens.ok, "●", "Gut")
        QualityLevel.FAIR -> Triple(tokens.warn, "◐", "Mittel")
        QualityLevel.POOR -> Triple(tokens.critical, "○", "Schlecht")
    }
    val detail: String = buildString {
        append(text)
        if (uncertaintyDeg != null) {
            append(" (±")
            append(String.format(Locale.US, "%.1f", uncertaintyDeg))
            append("°)")
        }
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = symbol, color = color, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(6.dp))
        Text(text = detail, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

/** Small confidence indicator dot only. */
@Composable
fun QualityDot(score: Int, modifier: Modifier = Modifier) {
    val tokens = LocalPyxisTokens.current
    val color: Color = when (qualityLevelFor(score)) {
        QualityLevel.GOOD -> tokens.ok
        QualityLevel.FAIR -> tokens.warn
        QualityLevel.POOR -> tokens.critical
    }
    Text(text = "●", color = color, style = MaterialTheme.typography.labelMedium, modifier = modifier)
}
