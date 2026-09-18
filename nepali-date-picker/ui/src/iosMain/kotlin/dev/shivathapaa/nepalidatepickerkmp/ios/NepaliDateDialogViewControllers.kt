/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerFullScreenDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import platform.UIKit.UIViewController

/**
 * Hosts [NepaliDatePickerDialog] with a calendar inside it.
 *
 * The controller renders the dialog immediately, so present it modally from Swift (a SwiftUI
 * `fullScreenCover` works well) and dismiss it when [onDismiss] fires.
 *
 * @param initialSelectedDate date selected when the dialog opens, or `null` for no selection.
 * @param locale language, date format and digit script the dialog renders with.
 * @param yearRangeStart first Bikram Sambat year the picker allows.
 * @param yearRangeEnd last Bikram Sambat year the picker allows.
 * @param selectableDates policy deciding which dates are enabled, or `null` to allow every date.
 * @param calendarOptions knobs for the calendar inside the dialog, or `null` for the defaults.
 * @param options dialog chrome, or `null` for the defaults.
 * @param onHeightChange receives the content height in points. A dialog is an overlay, so this
 * reports the inline content only and is normally ignored.
 * @param onConfirm invoked with the selection when the confirming button is tapped.
 * @param onDismiss invoked when the dialog is dismissed without confirming.
 */
fun NepaliDatePickerDialogViewController(
    initialSelectedDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    calendarOptions: NepaliCalendarOptions?,
    options: NepaliDialogOptions?,
    onHeightChange: (Float) -> Unit,
    onConfirm: (CustomCalendar?) -> Unit,
    onDismiss: () -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliDialogOptions()
    val calendar = calendarOptions ?: NepaliCalendarOptions()
    val state = remember {
        NepaliDatePickerState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = calendar.initialCalendarSystem
        )
    }

    NepaliDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.selectedDate) }) { Text(opts.confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(opts.dismissText) }
        },
        shape = RoundedCornerShape(opts.cornerRadius.dp),
        tonalElevation = opts.tonalElevation.dp
    ) {
        if (calendar.showEnglishDate) {
            NepaliDatePickerWithEnglishDate(
                state = state,
                englishDateLocale = calendar.englishDateLocale ?: locale,
                showModeToggle = calendar.showModeToggle,
                showTodayButton = calendar.showTodayButton,
                showCalendarSystemToggle = calendar.showCalendarSystemToggle,
                showAdjacentMonthDays = calendar.showAdjacentMonthDays
            )
        } else {
            NepaliDatePicker(
                state = state,
                showModeToggle = calendar.showModeToggle,
                showTodayButton = calendar.showTodayButton,
                showCalendarSystemToggle = calendar.showCalendarSystemToggle,
                showAdjacentMonthDays = calendar.showAdjacentMonthDays
            )
        }
    }
}

/**
 * Hosts [NepaliDatePickerFullScreenDialog], the edge-to-edge variant of the dialog.
 *
 * @param initialSelectedDate date selected when the dialog opens, or `null` for no selection.
 * @param locale language, date format and digit script the dialog renders with.
 * @param yearRangeStart first Bikram Sambat year the picker allows.
 * @param yearRangeEnd last Bikram Sambat year the picker allows.
 * @param selectableDates policy deciding which dates are enabled, or `null` to allow every date.
 * @param calendarOptions knobs for the calendar inside the dialog, or `null` for the defaults.
 * @param options dialog chrome, or `null` for the defaults. `tonalElevation` is ignored here,
 * because a full-screen dialog has no floating surface.
 * @param onHeightChange receives the content height in points. Normally ignored, as above.
 * @param onConfirm invoked with the selection when the confirming button is tapped.
 * @param onDismiss invoked when the dialog is dismissed without confirming.
 */
fun NepaliDatePickerFullScreenDialogViewController(
    initialSelectedDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    calendarOptions: NepaliCalendarOptions?,
    options: NepaliDialogOptions?,
    onHeightChange: (Float) -> Unit,
    onConfirm: (CustomCalendar?) -> Unit,
    onDismiss: () -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliDialogOptions()
    val calendar = calendarOptions ?: NepaliCalendarOptions()
    val state = remember {
        NepaliDatePickerState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = calendar.initialCalendarSystem
        )
    }

    NepaliDatePickerFullScreenDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.selectedDate) }) { Text(opts.confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(opts.dismissText) }
        },
        title = opts.title?.let { text -> { Text(text) } }
    ) {
        if (calendar.showEnglishDate) {
            NepaliDatePickerWithEnglishDate(
                state = state,
                englishDateLocale = calendar.englishDateLocale ?: locale,
                showModeToggle = calendar.showModeToggle,
                showTodayButton = calendar.showTodayButton,
                showCalendarSystemToggle = calendar.showCalendarSystemToggle,
                showAdjacentMonthDays = calendar.showAdjacentMonthDays
            )
        } else {
            NepaliDatePicker(
                state = state,
                showModeToggle = calendar.showModeToggle,
                showTodayButton = calendar.showTodayButton,
                showCalendarSystemToggle = calendar.showCalendarSystemToggle,
                showAdjacentMonthDays = calendar.showAdjacentMonthDays
            )
        }
    }
}
