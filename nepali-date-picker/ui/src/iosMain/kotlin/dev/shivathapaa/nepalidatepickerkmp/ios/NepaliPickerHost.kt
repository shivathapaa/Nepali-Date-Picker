/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.embed.MeasuredContent
import platform.UIKit.UIViewController

/**
 * Wraps a picker in the Material theme and a surface, then hosts it in a [UIViewController].
 *
 * The theme comes from [NepaliPickerAppearance], so the controller follows the device's interface
 * style and any colours the app has set, and repaints when either changes.
 *
 * The content wraps its own height rather than filling the controller, and reports that height
 * through [onHeightChange]. Use [nepaliDialogViewController] for a dialog.
 *
 * @param onHeightChange receives the content height in points whenever it changes.
 */
internal fun nepaliPickerViewController(
    onHeightChange: (Float) -> Unit,
    content: @Composable () -> Unit
): UIViewController = ComposeUIViewController {
    MaterialTheme(colorScheme = appearanceColorScheme()) {
        Surface {
            MeasuredContent(onHeightChange, content)
        }
    }
}

/**
 * Wraps a dialog in the Material theme and hosts it in a transparent [UIViewController], themed from
 * [NepaliPickerAppearance] like the inline pickers.
 *
 * A dialog covers the screen with its own scrim and floats its surface above that, so nothing is
 * painted behind it. Add the returned controller over the app's own content rather than presenting
 * it modally.
 *
 * @param onHeightChange receives the height of the inline content in points, which is zero for a
 * dialog.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun nepaliDialogViewController(
    onHeightChange: (Float) -> Unit,
    content: @Composable () -> Unit
): UIViewController = ComposeUIViewController(configure = { opaque = false }) {
    MaterialTheme(colorScheme = appearanceColorScheme()) {
        MeasuredContent(onHeightChange, content)
    }
}

/**
 * Resolves the selectable-date policy for a factory parameter.
 *
 * Swift has no access to Kotlin default arguments, so every factory takes a nullable policy and
 * treats `null` as "every date is selectable".
 */
internal fun NepaliSelectableDates?.orAllDates(): NepaliSelectableDates =
    this ?: NepaliDatePickerDefaults.AllDates

/**
 * Builds the year range from two plain integers.
 *
 * `IntRange` is clumsy to construct from Swift, so the factories accept its bounds instead.
 */
internal fun yearRangeOf(start: Int, end: Int): IntRange = start..end
