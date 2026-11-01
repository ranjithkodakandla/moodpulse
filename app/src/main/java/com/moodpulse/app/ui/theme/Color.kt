package com.moodpulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light palette — Warm Biophilic Minimalism
val md_primary = Color(0xFF333C2A)
val md_onPrimary = Color(0xFFFFFFFF)
val md_primaryContainer = Color(0xFF4A5340)
val md_onPrimaryContainer = Color(0xFFBCC6AE)
val md_secondary = Color(0xFF97472E)
val md_onSecondary = Color(0xFFFFFFFF)
val md_secondaryContainer = Color(0xFFFE997A)
val md_onSecondaryContainer = Color(0xFF772F18)
val md_tertiary = Color(0xFF453629)
val md_onTertiary = Color(0xFFFFFFFF)
val md_tertiaryContainer = Color(0xFF5D4D3E)
val md_onTertiaryContainer = Color(0xFFD4BFAC)
val md_error = Color(0xFFBA1A1A)
val md_onError = Color(0xFFFFFFFF)
val md_errorContainer = Color(0xFFFFDAD6)
val md_onErrorContainer = Color(0xFF93000A)
val md_background = Color(0xFFFEF8F6)
val md_onBackground = Color(0xFF1D1B1A)
val md_surface = Color(0xFFFEF8F6)
val md_onSurface = Color(0xFF1D1B1A)
val md_surfaceVariant = Color(0xFFE7E1DF)
val md_onSurfaceVariant = Color(0xFF454840)
val md_outline = Color(0xFF767870)
val md_outlineVariant = Color(0xFFC6C7BE)
val md_inverseSurface = Color(0xFF32302F)
val md_inverseOnSurface = Color(0xFFF5EFED)
val md_inversePrimary = Color(0xFFC0CAB2)
val md_surfaceTint = Color(0xFF58614E)
val md_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_surfaceContainerLow = Color(0xFFF8F2F0)
val md_surfaceContainer = Color(0xFFF2EDEB)
val md_surfaceContainerHigh = Color(0xFFEDE7E5)
val md_surfaceContainerHighest = Color(0xFFE7E1DF)
val md_surfaceDim = Color(0xFFDED9D7)

// Dark palette
val md_dark_primary = Color(0xFFC0CAB2)
val md_dark_onPrimary = Color(0xFF2A3320)
val md_dark_primaryContainer = Color(0xFF3D4634)
val md_dark_onPrimaryContainer = Color(0xFFBCC6AE)
val md_dark_secondary = Color(0xFFFFB59F)
val md_dark_onSecondary = Color(0xFF561F0A)
val md_dark_secondaryContainer = Color(0xFF7B3013)
val md_dark_onSecondaryContainer = Color(0xFFFFDAD0)
val md_dark_tertiary = Color(0xFFD9C3B0)
val md_dark_onTertiary = Color(0xFF3B2A1E)
val md_dark_tertiaryContainer = Color(0xFF4E3F32)
val md_dark_onTertiaryContainer = Color(0xFFF6DECB)
val md_dark_error = Color(0xFFFFB4AB)
val md_dark_onError = Color(0xFF690005)
val md_dark_errorContainer = Color(0xFF93000A)
val md_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_dark_background = Color(0xFF14120F)
val md_dark_onBackground = Color(0xFFEDE7E5)
val md_dark_surface = Color(0xFF14120F)
val md_dark_onSurface = Color(0xFFEDE7E5)
val md_dark_surfaceVariant = Color(0xFF454840)
val md_dark_onSurfaceVariant = Color(0xFFC6C7BE)
val md_dark_outline = Color(0xFF909488)
val md_dark_outlineVariant = Color(0xFF454840)
val md_dark_inverseSurface = Color(0xFFEDE7E5)
val md_dark_inverseOnSurface = Color(0xFF32302F)
val md_dark_inversePrimary = Color(0xFF4A5340)
val md_dark_surfaceContainerLowest = Color(0xFF0F0D0A)
val md_dark_surfaceContainerLow = Color(0xFF1D1B18)
val md_dark_surfaceContainer = Color(0xFF21201C)
val md_dark_surfaceContainerHigh = Color(0xFF2C2A27)
val md_dark_surfaceContainerHighest = Color(0xFF373431)

// Mood colors (consistent across themes)
val moodVeryLow = Color(0xFFC86D51)   // terracotta
val moodLow = Color(0xFFE8A87C)
val moodOkay = Color(0xFFD9C3B0)     // warm sand
val moodGood = Color(0xFF8FA87A)
val moodGreat = Color(0xFF4A5340)    // deep olive

fun moodColor(value: Int): Color = when (value) {
    1 -> moodVeryLow
    2 -> moodLow
    3 -> moodOkay
    4 -> moodGood
    5 -> moodGreat
    else -> moodOkay
}
