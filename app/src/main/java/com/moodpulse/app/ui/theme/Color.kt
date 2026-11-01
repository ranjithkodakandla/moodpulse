package com.moodpulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Obsidian & Emerald Theme (Prototype Inspired) ──
val ObsidianBg = Color(0xFF080B09)
val ObsidianSurface = Color(0xFF0C100D)
val ObsidianCard = Color(0xFF131A15)
val ObsidianBottomNav = Color(0xFF0F140F)
val TextLight = Color(0xFFE8F0E9)
val TextMuted = Color(0xFF5A6B5C)
val MintAccent = Color(0xFFA8D5A2)
val EmeraldGlow = Color(0xFF00C853)
val CardBorder = Color(0x14FFFFFF)

// ── 5 Vibrant Mood Colors ──
val MoodGreat = Color(0xFF00C853)    // Vivid Emerald (5)
val MoodGood = Color(0xFF76D275)     // Luminous Mint (4)
val MoodOkay = Color(0xFFFFB74D)     // Warm Golden Amber (3)
val MoodLow = Color(0xFFFF7043)      // Coral Flame (2)
val MoodVeryLow = Color(0xFFEF5350)  // Crimson Red (1)

// ── 3D Glossy Mood Orb Gradient Pairs (c1: highlight, c2: shadow) ──
val OrbGreat1 = Color(0xFF00E676)
val OrbGreat2 = Color(0xFF004F1A)

val OrbGood1 = Color(0xFF81C784)
val OrbGood2 = Color(0xFF1B4D1E)

val OrbOkay1 = Color(0xFFFFCA28)
val OrbOkay2 = Color(0xFF7A4A00)

val OrbLow1 = Color(0xFFFF8A65)
val OrbLow2 = Color(0xFF6D1F00)

val OrbVeryLow1 = Color(0xFFFF6B6B)
val OrbVeryLow2 = Color(0xFF5C0000)

// Helper mapping
fun moodColor(value: Int): Color = when (value) {
    1 -> MoodVeryLow
    2 -> MoodLow
    3 -> MoodOkay
    4 -> MoodGood
    5 -> MoodGreat
    else -> MoodOkay
}

// ── Backward-compatible Material 3 Palette Variables ──
val md_dark_primary = Color(0xFF00C853)
val md_dark_onPrimary = Color(0xFF003D14)
val md_dark_primaryContainer = Color(0xFF004D20)
val md_dark_onPrimaryContainer = MintAccent
val md_dark_secondary = MintAccent
val md_dark_onSecondary = Color(0xFF113815)
val md_dark_secondaryContainer = Color(0xFF234E26)
val md_dark_onSecondaryContainer = Color(0xFFCEECC8)
val md_dark_tertiary = Color(0xFFFFB74D)
val md_dark_onTertiary = Color(0xFF452B00)
val md_dark_tertiaryContainer = Color(0xFF633F00)
val md_dark_onTertiaryContainer = Color(0xFFFFDDB3)
val md_dark_error = Color(0xFFFFB4AB)
val md_dark_onError = Color(0xFF690005)
val md_dark_errorContainer = Color(0xFF93000A)
val md_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_dark_background = ObsidianBg
val md_dark_onBackground = TextLight
val md_dark_surface = ObsidianSurface
val md_dark_onSurface = TextLight
val md_dark_surfaceVariant = Color(0xFF19201A)
val md_dark_onSurfaceVariant = Color(0xFF9CA99E)
val md_dark_outline = Color(0xFF414B42)
val md_dark_outlineVariant = CardBorder
val md_dark_inverseSurface = TextLight
val md_dark_inverseOnSurface = ObsidianBg
val md_dark_inversePrimary = Color(0xFF006D2C)
val md_dark_surfaceContainerLowest = Color(0xFF050806)
val md_dark_surfaceContainerLow = Color(0xFF0C100D)
val md_dark_surfaceContainer = Color(0xFF101612)
val md_dark_surfaceContainerHigh = Color(0xFF151D17)
val md_dark_surfaceContainerHighest = Color(0xFF1B241E)

// Light palette fallback
val md_primary = Color(0xFF008938)
val md_onPrimary = Color(0xFFFFFFFF)
val md_primaryContainer = Color(0xFF76D275)
val md_onPrimaryContainer = Color(0xFF002107)
val md_secondary = Color(0xFF386A41)
val md_onSecondary = Color(0xFFFFFFFF)
val md_secondaryContainer = Color(0xFFBAF0BE)
val md_onSecondaryContainer = Color(0xFF002108)
val md_tertiary = Color(0xFF8B5000)
val md_onTertiary = Color(0xFFFFFFFF)
val md_tertiaryContainer = Color(0xFFFFDCBE)
val md_onTertiaryContainer = Color(0xFF2C1600)
val md_error = Color(0xFFBA1A1A)
val md_onError = Color(0xFFFFFFFF)
val md_errorContainer = Color(0xFFFFDAD6)
val md_onErrorContainer = Color(0xFF93000A)
val md_background = Color(0xFFF9FAF9)
val md_onBackground = Color(0xFF171D18)
val md_surface = Color(0xFFFFFFFF)
val md_onSurface = Color(0xFF171D18)
val md_surfaceVariant = Color(0xFFDCE5DC)
val md_onSurfaceVariant = Color(0xFF414942)
val md_outline = Color(0xFF717972)
val md_outlineVariant = Color(0xFFC0C9C0)
val md_inverseSurface = Color(0xFF2C322C)
val md_inverseOnSurface = Color(0xFFEDF2EC)
val md_inversePrimary = Color(0xFF00C853)
val md_surfaceTint = Color(0xFF008938)
val md_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_surfaceContainerLow = Color(0xFFF3F5F2)
val md_surfaceContainer = Color(0xFFECEFEA)
val md_surfaceContainerHigh = Color(0xFFE6EAE4)
val md_surfaceContainerHighest = Color(0xFFE0E4DE)
val md_surfaceDim = Color(0xFFD7DCD6)

// Old aliases for compatibility
val moodGreat = MoodGreat
val moodGood = MoodGood
val moodOkay = MoodOkay
val moodLow = MoodLow
val moodVeryLow = MoodVeryLow
