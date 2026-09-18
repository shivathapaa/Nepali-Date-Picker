/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendarSystemToggle
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import platform.UIKit.UIViewController

/**
 * Hosts the standalone `B.S.` / `A.D.` switch for a Swift caller.
 *
 * The pickers draw this themselves when `options.showCalendarSystemToggle` is on, so reach for the
 * factory only when the switch belongs in your own chrome (a navigation bar, a settings row) and you
 * drive the picker's calendar from there.
 *
 * @param initialCalendarSystem the calendar shown as selected when the switch first appears.
 * @param language language the segment labels and their accessibility descriptions are written in.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onCalendarSystemChange invoked with the tapped calendar. Fires only on an actual change.
 */
fun NepaliCalendarSystemToggleViewController(
    initialCalendarSystem: CalendarSystem,
    language: NepaliDatePickerLang,
    onHeightChange: (Float) -> Unit,
    onCalendarSystemChange: (CalendarSystem) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    var calendarSystem by remember { mutableStateOf(initialCalendarSystem) }

    NepaliCalendarSystemToggle(
        calendarSystem = calendarSystem,
        onCalendarSystemChange = { tapped ->
            calendarSystem = tapped
            onCalendarSystemChange(tapped)
        },
        language = language
    )
}
