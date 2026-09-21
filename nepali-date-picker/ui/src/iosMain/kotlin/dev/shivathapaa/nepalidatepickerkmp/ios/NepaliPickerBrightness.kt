/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

/**
 * Which brightness the hosted pickers draw at.
 *
 * [System] follows the device's interface style and switches with it while a picker is on screen.
 * [Light] and [Dark] pin the picker regardless of the device setting, for an app that themes itself
 * independently of iOS.
 */
enum class NepaliPickerBrightness {
    System,
    Light,
    Dark
}
