package de.pyxissapiens.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import de.pyxissapiens.core.ui.R

/** JetBrains Mono (SIL Open Font License) — tabular, non-jittering digits for readouts. */
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

private val base = Typography()

// Display/headline/title styles use the monospace family; body labels stay in the system UI font
// for comfortable reading.
val PyxisTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
    displayMedium = base.displayMedium.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
    displaySmall = base.displaySmall.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
    headlineMedium = base.headlineMedium.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
    headlineSmall = base.headlineSmall.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
    titleLarge = base.titleLarge.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
    titleMedium = base.titleMedium.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium),
)
