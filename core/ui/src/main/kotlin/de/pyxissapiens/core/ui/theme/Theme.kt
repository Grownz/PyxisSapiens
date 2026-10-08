package de.pyxissapiens.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// "Instrument" theme: amber accent, dark default, high-contrast light variant for sunlight.

private val InstrumentDark = darkColorScheme(
    primary = PyxisPalette.Accent,
    onPrimary = PyxisPalette.BgBase,
    background = PyxisPalette.BgBase,
    onBackground = PyxisPalette.TextPrimary,
    surface = PyxisPalette.BgPanel,
    onSurface = PyxisPalette.TextPrimary,
    surfaceVariant = PyxisPalette.BgRaised,
    onSurfaceVariant = PyxisPalette.TextSecondary,
    outline = PyxisPalette.Grid,
    error = PyxisPalette.Critical,
    tertiary = PyxisPalette.North,
)

private val HighContrastLight = lightColorScheme(
    primary = PyxisPalette.LightAccent,
    onPrimary = PyxisPalette.LightBgBase,
    background = PyxisPalette.LightBgBase,
    onBackground = PyxisPalette.LightTextPrimary,
    surface = PyxisPalette.LightBgBase,
    onSurface = PyxisPalette.LightTextPrimary,
    surfaceVariant = PyxisPalette.LightBgPanel,
    onSurfaceVariant = PyxisPalette.LightTextSecondary,
    outline = PyxisPalette.LightGrid,
    error = PyxisPalette.LightCritical,
    tertiary = PyxisPalette.LightNorth,
)

@Composable
fun PyxisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens = if (darkTheme) PyxisTokensDark else PyxisTokensLight
    CompositionLocalProvider(LocalPyxisTokens provides tokens) {
        MaterialTheme(
            colorScheme = if (darkTheme) InstrumentDark else HighContrastLight,
            typography = PyxisTypography,
            content = content,
        )
    }
}
