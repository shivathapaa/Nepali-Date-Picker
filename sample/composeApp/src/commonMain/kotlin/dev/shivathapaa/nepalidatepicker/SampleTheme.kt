/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Wraps the showcase in the [palette] and [mode] the appearance menu currently holds.
 *
 * Nothing below this point names a colour. The pickers, the fields and the event markers all read
 * `MaterialTheme.colorScheme`, so what the menu changes here is what a consuming app changes by
 * supplying its own theme.
 */
@Composable
fun SampleTheme(
    palette: SamplePalette,
    mode: SampleThemeMode,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        SampleThemeMode.System -> isSystemInDarkTheme()
        SampleThemeMode.Light -> false
        SampleThemeMode.Dark -> true
    }
    val colorScheme = remember(palette, dark) { colorSchemeFor(palette, dark) }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

/**
 * The top bar's appearance control: one menu holding the three brightness modes and every palette.
 *
 * Both lists mark the current choice, so the menu doubles as a readout of what the screenshot below
 * it is showing.
 */
@Composable
fun SampleThemeMenu(
    palette: SamplePalette,
    mode: SampleThemeMode,
    onPaletteChange: (SamplePalette) -> Unit,
    onModeChange: (SampleThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.Settings, contentDescription = "Appearance")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        MenuSectionLabel("Brightness")
        SampleThemeMode.entries.forEach { entry ->
            DropdownMenuItem(
                text = { Text(entry.label) },
                onClick = {
                    onModeChange(entry)
                    expanded = false
                },
                trailingIcon = { SelectedMark(selected = entry == mode) }
            )
        }

        HorizontalDivider()

        MenuSectionLabel("Palette")
        SamplePalette.entries.forEach { entry ->
            DropdownMenuItem(
                text = { Text(entry.label) },
                onClick = {
                    onPaletteChange(entry)
                    expanded = false
                },
                leadingIcon = { PaletteSwatch(entry) },
                trailingIcon = { SelectedMark(selected = entry == palette) }
            )
        }
    }
}

/** A heading inside the appearance menu, separating the brightness modes from the palettes. */
@Composable
private fun MenuSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = LabelStartPadding, top = LabelTopPadding, bottom = LabelBottomPadding),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

/** A filled dot in a palette's primary colour, so the menu can be read without opening each entry. */
@Composable
private fun PaletteSwatch(palette: SamplePalette) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val swatch = remember(palette, dark) { colorSchemeFor(palette, dark).primary }
    Box(
        modifier = Modifier
            .size(SwatchSize)
            .background(swatch, CircleShape)
            .border(SwatchBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    )
}

/** The tick marking the active brightness mode or palette, occupying the slot either way. */
@Composable
private fun SelectedMark(selected: Boolean) {
    if (selected) {
        Icon(Icons.Default.Check, contentDescription = "Selected", modifier = Modifier.size(SwatchSize))
    } else {
        Box(modifier = Modifier.size(SwatchSize))
    }
}

private val SwatchSize = 18.dp
private val SwatchBorder = 1.dp
private val LabelStartPadding = 12.dp
private val LabelTopPadding = 8.dp
private val LabelBottomPadding = 4.dp
