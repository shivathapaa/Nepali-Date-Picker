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
 * through [onHeightChange]. A hosted controller has no intrinsic size on the Swift side, so without
 * this the caller has to guess a frame: too short crops the calendar, too tall leaves dead space
 * below a text field.
 *
 * @param onHeightChange receives the content height in points whenever it changes.
 */
internal fun nepaliPickerViewController(
    onHeightChange: (Float) -> Unit,
    content: @Composable () -> Unit
): UIViewController = ComposeUIViewController {
    MaterialTheme {
        Surface {
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
