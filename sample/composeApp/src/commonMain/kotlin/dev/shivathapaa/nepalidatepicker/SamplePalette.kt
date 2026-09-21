/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

/**
 * A Material color scheme the showcase can switch to at runtime, named by the hue it is built
 * around.
 *
 * The pickers read their colours from `MaterialTheme.colorScheme`, so every entry here restyles the
 * whole library without a single picker-level colour override. [Default] is Material's own baseline
 * palette.
 */
enum class SamplePalette(val label: String) {
    Default("Default"),
    Green("Green"),
    Blue("Blue"),
    Orange("Orange"),
    Red("Red"),
    Yellow("Yellow")
}
