/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedAppearanceSlots
import dev.shivathapaa.nepalidatepickerkmp.embed.derivedPickerColorScheme

/**
 * The colours every hosted picker, field and dialog draws with, shared by the whole app.
 *
 * This is an appearance proxy: set it once at launch, or whenever the app's theme changes, and
 * every picker already on screen repaints. It exists because a View-based caller, such as a
 * Flutter host or an XML activity, has no way to install a Compose theme around a hosted view.
 * It maps slot for slot onto the iOS `NepaliPickerAppearance`, so one palette themes both hosts.
 *
 * Every colour is an ARGB integer, and `0` means "use Material's own value for this role", so an
 * app can override only its accent and leave the rest alone. Set [brightness] to pin light or
 * dark; the default follows the device.
 *
 * ```kotlin
 * NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
 * NepaliPickerAppearance.primaryArgb = 0xFFB1D18A.toInt()
 * ```
 */
object NepaliPickerAppearance {

    /** Whether the pickers follow the device's dark-mode setting, or pin light or dark. */
    var brightness: NepaliPickerBrightness by mutableStateOf(NepaliPickerBrightness.System)

    /** The accent: selected days, the today ring, the confirm button, the cursor. */
    var primaryArgb: Int by mutableStateOf(0)

    /** Content drawn on top of [primaryArgb], such as the number inside a selected day. */
    var onPrimaryArgb: Int by mutableStateOf(0)

    /** The filled container behind a selected year and a range's endpoints. */
    var primaryContainerArgb: Int by mutableStateOf(0)

    /** Content drawn on top of [primaryContainerArgb]. */
    var onPrimaryContainerArgb: Int by mutableStateOf(0)

    /** The softer container behind the days inside a selected range. */
    var secondaryContainerArgb: Int by mutableStateOf(0)

    /** Content drawn on top of [secondaryContainerArgb]. */
    var onSecondaryContainerArgb: Int by mutableStateOf(0)

    /** The picker's own background. Dialog, menu and field surfaces are derived from it. */
    var surfaceArgb: Int by mutableStateOf(0)

    /** Day numbers, headlines and labels. */
    var onSurfaceArgb: Int by mutableStateOf(0)

    /** The raised surface behind fields and the highest container tone. */
    var surfaceVariantArgb: Int by mutableStateOf(0)

    /** Weekday letters, supporting text and the unselected navigation icons. */
    var onSurfaceVariantArgb: Int by mutableStateOf(0)

    /** Field borders, dividers and the outline of an unselected day. */
    var outlineArgb: Int by mutableStateOf(0)

    /** Restores every role to Material's own value and the brightness to the device setting. */
    fun reset() {
        brightness = NepaliPickerBrightness.System
        primaryArgb = 0
        onPrimaryArgb = 0
        primaryContainerArgb = 0
        onPrimaryContainerArgb = 0
        secondaryContainerArgb = 0
        onSecondaryContainerArgb = 0
        surfaceArgb = 0
        onSurfaceArgb = 0
        surfaceVariantArgb = 0
        onSurfaceVariantArgb = 0
        outlineArgb = 0
    }
}

/**
 * The scheme the hosted pickers render with, following [NepaliPickerAppearance] and the device's
 * dark-mode setting. The slot-to-role mapping is shared with the iOS host, so the same palette
 * produces the same calendar on both platforms.
 */
@Composable
internal fun appearanceColorScheme(): ColorScheme {
    val systemDark = isSystemInDarkTheme()
    val dark = when (NepaliPickerAppearance.brightness) {
        NepaliPickerBrightness.System -> systemDark
        NepaliPickerBrightness.Light -> false
        NepaliPickerBrightness.Dark -> true
    }
    return derivedPickerColorScheme(
        dark = dark,
        slots = EmbeddedAppearanceSlots(
            primaryArgb = NepaliPickerAppearance.primaryArgb,
            onPrimaryArgb = NepaliPickerAppearance.onPrimaryArgb,
            primaryContainerArgb = NepaliPickerAppearance.primaryContainerArgb,
            onPrimaryContainerArgb = NepaliPickerAppearance.onPrimaryContainerArgb,
            secondaryContainerArgb = NepaliPickerAppearance.secondaryContainerArgb,
            onSecondaryContainerArgb = NepaliPickerAppearance.onSecondaryContainerArgb,
            surfaceArgb = NepaliPickerAppearance.surfaceArgb,
            onSurfaceArgb = NepaliPickerAppearance.onSurfaceArgb,
            surfaceVariantArgb = NepaliPickerAppearance.surfaceVariantArgb,
            onSurfaceVariantArgb = NepaliPickerAppearance.onSurfaceVariantArgb,
            outlineArgb = NepaliPickerAppearance.outlineArgb
        )
    )
}
