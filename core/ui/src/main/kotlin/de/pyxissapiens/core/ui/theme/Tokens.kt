package de.pyxissapiens.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Raw palette values behind the "instrument" design language (docs/04 §2.1). */
object PyxisPalette {
    // Dark (default)
    val BgBase = Color(0xFF0B0F14)
    val BgPanel = Color(0xFF131A22)
    val BgRaised = Color(0xFF1B2631)
    val TextPrimary = Color(0xFFE8EEF5)
    val TextSecondary = Color(0xFF9AA7B4)
    val Accent = Color(0xFFFFB000)
    val Ok = Color(0xFF37D67A)
    val Warn = Color(0xFFFFB020)
    val Critical = Color(0xFFFF5C5C)
    val North = Color(0xFF4EA1FF)
    val Grid = Color(0xFF2A3644)

    // High-contrast light (direct sunlight)
    val LightBgBase = Color(0xFFFFFFFF)
    val LightBgPanel = Color(0xFFF2F4F7)
    val LightBgRaised = Color(0xFFE6E9EE)
    val LightTextPrimary = Color(0xFF000000)
    val LightTextSecondary = Color(0xFF3A434D)
    val LightAccent = Color(0xFFB36B00)
    val LightOk = Color(0xFF1B7F3B)
    val LightWarn = Color(0xFF8A5A00)
    val LightCritical = Color(0xFFB00020)
    val LightNorth = Color(0xFF004C99)
    val LightGrid = Color(0xFFBFC7D1)
}

/**
 * Semantic tokens that go beyond the Material [androidx.compose.material3.ColorScheme]:
 * measurement-quality colours and layout metrics.
 */
@Immutable
data class PyxisTokens(
    val ok: Color,
    val warn: Color,
    val critical: Color,
    val north: Color,
    val grid: Color,
    val onSurfaceMuted: Color,
    val spacingXs: Dp = 4.dp,
    val spacingSm: Dp = 8.dp,
    val spacingMd: Dp = 16.dp,
    val spacingLg: Dp = 24.dp,
    val touchTargetMin: Dp = 56.dp,
)

val PyxisTokensDark = PyxisTokens(
    ok = PyxisPalette.Ok,
    warn = PyxisPalette.Warn,
    critical = PyxisPalette.Critical,
    north = PyxisPalette.North,
    grid = PyxisPalette.Grid,
    onSurfaceMuted = PyxisPalette.TextSecondary,
)

val PyxisTokensLight = PyxisTokens(
    ok = PyxisPalette.LightOk,
    warn = PyxisPalette.LightWarn,
    critical = PyxisPalette.LightCritical,
    north = PyxisPalette.LightNorth,
    grid = PyxisPalette.LightGrid,
    onSurfaceMuted = PyxisPalette.LightTextSecondary,
)

val LocalPyxisTokens = staticCompositionLocalOf { PyxisTokensDark }
