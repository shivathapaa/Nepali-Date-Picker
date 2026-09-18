/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.ComposeUIViewController
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import platform.UIKit.UIViewController

/**
 * Wraps a picker in the Material theme and a surface, then hosts it in a [UIViewController].
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
    MaterialTheme {
        Surface {
            MeasuredContent(onHeightChange, content)
        }
    }
}

/**
 * Wraps a dialog in the Material theme and hosts it in a transparent [UIViewController].
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
    MaterialTheme {
        MeasuredContent(onHeightChange, content)
    }
}

/**
 * Lays the content out at its natural height and reports that height in points.
 *
 * A hosted controller has no intrinsic size on the Swift side, so without this the caller has to
 * guess a frame: too short crops the calendar, too tall leaves dead space below a text field.
 */
@Composable
private fun MeasuredContent(
    onHeightChange: (Float) -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                onHeightChange(size.height / density.density)
            }
    ) {
        content()
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
