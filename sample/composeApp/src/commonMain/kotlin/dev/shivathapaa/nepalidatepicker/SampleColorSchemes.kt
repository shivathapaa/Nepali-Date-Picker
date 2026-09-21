/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * The Material color scheme [palette] renders with, in dark or light mode.
 *
 * The pickers take every colour they draw from `MaterialTheme.colorScheme` unless the caller
 * overrides a slot, so swapping the scheme here restyles the grids, the fields, the dialogs and the
 * event markers together.
 */
internal fun colorSchemeFor(palette: SamplePalette, dark: Boolean): ColorScheme = when (palette) {
    SamplePalette.Default -> defaultScheme(dark)
    SamplePalette.Green -> greenScheme(dark)
    SamplePalette.Blue -> blueScheme(dark)
    SamplePalette.Orange -> orangeScheme(dark)
    SamplePalette.Red -> redScheme(dark)
    SamplePalette.Yellow -> yellowScheme(dark)
}

/**
 * A light scheme built from the roles that carry a palette's hue.
 *
 * The surface container family and the inverse roles are derived from [surface] and
 * [surfaceVariant] rather than listed per palette. Material places those tones on the same neutral
 * ramp the two surfaces sit on, so interpolating between them keeps cards, sheets and menus in the
 * palette instead of falling back to the baseline purple the Material defaults would supply.
 */
private fun sampleLightColorScheme(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    tertiary: Color,
    tertiaryContainer: Color,
    onTertiaryContainer: Color,
    surface: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    outline: Color,
    outlineVariant: Color
): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    inversePrimary = primaryContainer,
    secondary = secondary,
    onSecondary = onPrimary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onPrimary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = surface,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceTint = primary,
    inverseSurface = lerp(onSurface, surfaceVariant, 0.2f),
    inverseOnSurface = surface,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = Color.Black,
    surfaceBright = surface,
    surfaceDim = lerp(surface, surfaceVariant, 0.85f),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = lerp(surface, surfaceVariant, 0.25f),
    surfaceContainer = lerp(surface, surfaceVariant, 0.45f),
    surfaceContainerHigh = lerp(surface, surfaceVariant, 0.65f),
    surfaceContainerHighest = surfaceVariant
)

/** The dark counterpart of [sampleLightColorScheme], derived the same way. */
private fun sampleDarkColorScheme(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    tertiary: Color,
    onTertiary: Color,
    tertiaryContainer: Color,
    onTertiaryContainer: Color,
    surface: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    outline: Color,
    outlineVariant: Color
): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    inversePrimary = primaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorContainerLight,
    background = surface,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceTint = primary,
    inverseSurface = onSurface,
    inverseOnSurface = surface,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = Color.Black,
    surfaceBright = lerp(surface, surfaceVariant, 0.75f),
    surfaceDim = surface,
    surfaceContainerLowest = lerp(surface, Color.Black, 0.3f),
    surfaceContainerLow = lerp(surface, surfaceVariant, 0.17f),
    surfaceContainer = lerp(surface, surfaceVariant, 0.25f),
    surfaceContainerHigh = lerp(surface, surfaceVariant, 0.46f),
    surfaceContainerHighest = lerp(surface, surfaceVariant, 0.67f)
)

private val ErrorLight = Color(0xFFBA1A1A)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF93000A)
private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)

/** Material's own baseline palette, so the sample opens on the scheme the library is specified against. */
private fun defaultScheme(dark: Boolean): ColorScheme = if (dark) {
    sampleDarkColorScheme(
        primary = Color(0xFFD0BCFF), onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B), onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC), onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458), onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFFEFB8C8), onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48), onTertiaryContainer = Color(0xFFFFD8E4),
        surface = Color(0xFF141218), onSurface = Color(0xFFE6E0E9),
        surfaceVariant = Color(0xFF49454F), onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99), outlineVariant = Color(0xFF49454F)
    )
} else {
    sampleLightColorScheme(
        primary = Color(0xFF6750A4), onPrimary = Color.White,
        primaryContainer = Color(0xFFEADDFF), onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        secondaryContainer = Color(0xFFE8DEF8), onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF7D5260),
        tertiaryContainer = Color(0xFFFFD8E4), onTertiaryContainer = Color(0xFF31111D),
        surface = Color(0xFFFEF7FF), onSurface = Color(0xFF1D1B20),
        surfaceVariant = Color(0xFFE7E0EC), onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E), outlineVariant = Color(0xFFCAC4D0)
    )
}

private fun greenScheme(dark: Boolean): ColorScheme = if (dark) {
    sampleDarkColorScheme(
        primary = Color(0xFFB1D18A), onPrimary = Color(0xFF1F3701),
        primaryContainer = Color(0xFF354E16), onPrimaryContainer = Color(0xFFCDEDA3),
        secondary = Color(0xFFBFCBAD), onSecondary = Color(0xFF2A331E),
        secondaryContainer = Color(0xFF404A33), onSecondaryContainer = Color(0xFFDCE7C8),
        tertiary = Color(0xFFA0D0CB), onTertiary = Color(0xFF003735),
        tertiaryContainer = Color(0xFF1F4E4B), onTertiaryContainer = Color(0xFFBCECE7),
        surface = Color(0xFF12140E), onSurface = Color(0xFFE2E3D8),
        surfaceVariant = Color(0xFF44483D), onSurfaceVariant = Color(0xFFC5C8BA),
        outline = Color(0xFF8F9285), outlineVariant = Color(0xFF44483D)
    )
} else {
    sampleLightColorScheme(
        primary = Color(0xFF4C662B), onPrimary = Color.White,
        primaryContainer = Color(0xFFCDEDA3), onPrimaryContainer = Color(0xFF354E16),
        secondary = Color(0xFF586249),
        secondaryContainer = Color(0xFFDCE7C8), onSecondaryContainer = Color(0xFF404A33),
        tertiary = Color(0xFF386663),
        tertiaryContainer = Color(0xFFBCECE7), onTertiaryContainer = Color(0xFF1F4E4B),
        surface = Color(0xFFF9FAEF), onSurface = Color(0xFF1A1C16),
        surfaceVariant = Color(0xFFE1E4D5), onSurfaceVariant = Color(0xFF44483D),
        outline = Color(0xFF75796C), outlineVariant = Color(0xFFC5C8BA)
    )
}

private fun blueScheme(dark: Boolean): ColorScheme = if (dark) {
    sampleDarkColorScheme(
        primary = Color(0xFFAAC7FF), onPrimary = Color(0xFF0A305F),
        primaryContainer = Color(0xFF284777), onPrimaryContainer = Color(0xFFD6E3FF),
        secondary = Color(0xFFBEC6DC), onSecondary = Color(0xFF283141),
        secondaryContainer = Color(0xFF3E4759), onSecondaryContainer = Color(0xFFDAE2F9),
        tertiary = Color(0xFFDDBCE0), onTertiary = Color(0xFF3F2844),
        tertiaryContainer = Color(0xFF573E5C), onTertiaryContainer = Color(0xFFFAD8FD),
        surface = Color(0xFF111318), onSurface = Color(0xFFE2E2E9),
        surfaceVariant = Color(0xFF44474E), onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099), outlineVariant = Color(0xFF44474E)
    )
} else {
    sampleLightColorScheme(
        primary = Color(0xFF415F91), onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E3FF), onPrimaryContainer = Color(0xFF284777),
        secondary = Color(0xFF565F71),
        secondaryContainer = Color(0xFFDAE2F9), onSecondaryContainer = Color(0xFF3E4759),
        tertiary = Color(0xFF705575),
        tertiaryContainer = Color(0xFFFAD8FD), onTertiaryContainer = Color(0xFF573E5C),
        surface = Color(0xFFF9F9FF), onSurface = Color(0xFF191C20),
        surfaceVariant = Color(0xFFE0E2EC), onSurfaceVariant = Color(0xFF44474E),
        outline = Color(0xFF74777F), outlineVariant = Color(0xFFC4C6D0)
    )
}

private fun orangeScheme(dark: Boolean): ColorScheme = if (dark) {
    sampleDarkColorScheme(
        primary = Color(0xFFFFB870), onPrimary = Color(0xFF4A2800),
        primaryContainer = Color(0xFF6A3B00), onPrimaryContainer = Color(0xFFFFDCC2),
        secondary = Color(0xFFE3C0A6), onSecondary = Color(0xFF422C19),
        secondaryContainer = Color(0xFF5A422D), onSecondaryContainer = Color(0xFFFFDCC2),
        tertiary = Color(0xFFC3CB95), onTertiary = Color(0xFF2D330C),
        tertiaryContainer = Color(0xFF434A21), onTertiaryContainer = Color(0xFFDFE7B0),
        surface = Color(0xFF1A120C), onSurface = Color(0xFFF0DFD4),
        surfaceVariant = Color(0xFF51453A), onSurfaceVariant = Color(0xFFD6C3B5),
        outline = Color(0xFF9E8E81), outlineVariant = Color(0xFF51453A)
    )
} else {
    sampleLightColorScheme(
        primary = Color(0xFF8B5000), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDCC2), onPrimaryContainer = Color(0xFF6A3B00),
        secondary = Color(0xFF745943),
        secondaryContainer = Color(0xFFFFDCC2), onSecondaryContainer = Color(0xFF5A422D),
        tertiary = Color(0xFF5B6236),
        tertiaryContainer = Color(0xFFDFE7B0), onTertiaryContainer = Color(0xFF434A21),
        surface = Color(0xFFFFF8F5), onSurface = Color(0xFF221A14),
        surfaceVariant = Color(0xFFF3DFD1), onSurfaceVariant = Color(0xFF51453A),
        outline = Color(0xFF837468), outlineVariant = Color(0xFFD6C3B5)
    )
}

private fun redScheme(dark: Boolean): ColorScheme = if (dark) {
    sampleDarkColorScheme(
        primary = Color(0xFFFFB4AB), onPrimary = Color(0xFF561E19),
        primaryContainer = Color(0xFF73342D), onPrimaryContainer = Color(0xFFFFDAD6),
        secondary = Color(0xFFE7BDB8), onSecondary = Color(0xFF442926),
        secondaryContainer = Color(0xFF5D3F3B), onSecondaryContainer = Color(0xFFFFDAD6),
        tertiary = Color(0xFFE0C38C), onTertiary = Color(0xFF3F2D04),
        tertiaryContainer = Color(0xFF574419), onTertiaryContainer = Color(0xFFFDDFA6),
        surface = Color(0xFF1A1110), onSurface = Color(0xFFF1DEDC),
        surfaceVariant = Color(0xFF534341), onSurfaceVariant = Color(0xFFD8C2BE),
        outline = Color(0xFFA08C8A), outlineVariant = Color(0xFF534341)
    )
} else {
    sampleLightColorScheme(
        primary = Color(0xFF904A43), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDAD6), onPrimaryContainer = Color(0xFF73342D),
        secondary = Color(0xFF775652),
        secondaryContainer = Color(0xFFFFDAD6), onSecondaryContainer = Color(0xFF5D3F3B),
        tertiary = Color(0xFF715B2E),
        tertiaryContainer = Color(0xFFFDDFA6), onTertiaryContainer = Color(0xFF574419),
        surface = Color(0xFFFFF8F7), onSurface = Color(0xFF231919),
        surfaceVariant = Color(0xFFF5DDDA), onSurfaceVariant = Color(0xFF534341),
        outline = Color(0xFF857371), outlineVariant = Color(0xFFD8C2BE)
    )
}

private fun yellowScheme(dark: Boolean): ColorScheme = if (dark) {
    sampleDarkColorScheme(
        primary = Color(0xFFDBC66E), onPrimary = Color(0xFF3A3000),
        primaryContainer = Color(0xFF534600), onPrimaryContainer = Color(0xFFF8E287),
        secondary = Color(0xFFD1C6A1), onSecondary = Color(0xFF362F15),
        secondaryContainer = Color(0xFF4E472A), onSecondaryContainer = Color(0xFFEEE2BC),
        tertiary = Color(0xFFA9D0B3), onTertiary = Color(0xFF143723),
        tertiaryContainer = Color(0xFF2C4E38), onTertiaryContainer = Color(0xFFC5ECCE),
        surface = Color(0xFF15130B), onSurface = Color(0xFFE9E2D0),
        surfaceVariant = Color(0xFF4B4739), onSurfaceVariant = Color(0xFFCDC6B4),
        outline = Color(0xFF969080), outlineVariant = Color(0xFF4B4739)
    )
} else {
    sampleLightColorScheme(
        primary = Color(0xFF6D5E0F), onPrimary = Color.White,
        primaryContainer = Color(0xFFF8E287), onPrimaryContainer = Color(0xFF534600),
        secondary = Color(0xFF665E40),
        secondaryContainer = Color(0xFFEEE2BC), onSecondaryContainer = Color(0xFF4E472A),
        tertiary = Color(0xFF43664E),
        tertiaryContainer = Color(0xFFC5ECCE), onTertiaryContainer = Color(0xFF2C4E38),
        surface = Color(0xFFFFF9EE), onSurface = Color(0xFF1E1B13),
        surfaceVariant = Color(0xFFEAE2D0), onSurfaceVariant = Color(0xFF4B4739),
        outline = Color(0xFF7C7767), outlineVariant = Color(0xFFCDC6B4)
    )
}
