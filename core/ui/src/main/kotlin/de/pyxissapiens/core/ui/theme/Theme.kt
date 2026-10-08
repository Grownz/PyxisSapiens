package de.pyxissapiens.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// "Instrument" design tokens (see docs/04). Amber accent, dark default,
// high-contrast light variant for direct sunlight.

private val InstrumentDark = darkColorScheme(
    primary = Color(0xFFFFB000),      // accent / measured value
    onPrimary = Color(0xFF1A1200),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE8EEF5),
    surface = Color(0xFF131A22),
    onSurface = Color(0xFFE8EEF5),
    surfaceVariant = Color(0xFF1B2631),
    onSurfaceVariant = Color(0xFF9AA7B4),
    error = Color(0xFFFF5C5C),
    tertiary = Color(0xFF4EA1FF),     // north reference
)

private val HighContrastLight = lightColorScheme(
    primary = Color(0xFFB36B00),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFF1A1A1A),
    error = Color(0xFFB00020),
    tertiary = Color(0xFF004C99),
)

@Composable
fun PyxisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) InstrumentDark else HighContrastLight,
        content = content,
    )
}
