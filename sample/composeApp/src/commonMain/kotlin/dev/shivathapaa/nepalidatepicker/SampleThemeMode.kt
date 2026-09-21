/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

/**
 * Which brightness the showcase renders at.
 *
 * [System] follows the host: the device setting on Android and iOS, the desktop appearance on the
 * JVM, and the browser's `prefers-color-scheme` on the web. [Light] and [Dark] pin it, which is what
 * makes a screenshot of either mode reproducible on a machine set to the other.
 */
enum class SampleThemeMode(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}
