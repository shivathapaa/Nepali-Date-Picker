/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.NepaliWheelDatePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import platform.UIKit.UIViewController

/**
 * Hosts the full-size [NepaliDatePicker] for a Swift caller.
 *
 * Composable functions cannot be called from Swift, so this factory is the iOS entry point to the
 * calendar grid. Wrap the returned controller in a `UIViewControllerRepresentable` to embed it in
 * SwiftUI.
 *
 * @param initialSelectedDate date selected when the picker first appears, or `null` for no selection.
 * @param locale language, date format and digit script the picker renders with.
 * @param yearRangeStart first Bikram Sambat year the picker allows.
 * @param yearRangeEnd last Bikram Sambat year the picker allows.
 * @param selectableDates policy deciding which dates are enabled, or `null` to allow every date.
 * @param options appearance and behaviour knobs, or `null` for the library defaults.
 * @param events the days to mark, or `null` to draw the calendar plain. Marking never blocks a
 * date; pass a policy's `asSelectableDates()` through `selectableDates` for that.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onDateSelected invoked with the current selection whenever it changes, including the
 * initial value.
 */
fun NepaliDatePickerViewController(
    initialSelectedDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    options: NepaliCalendarOptions?,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit,
    onDateSelected: (CustomCalendar?) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliCalendarOptions()
    val dayMarks = events.toDecorator()
    val state = remember {
        NepaliDatePickerState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = opts.initialCalendarSystem
        )
    }

    LaunchedEffect(state.selectedDate) { onDateSelected(state.selectedDate) }

    if (opts.showEnglishDate) {
        NepaliDatePickerWithEnglishDate(
            dayDecorator = dayMarks,
            state = state,
            englishDateLocale = opts.englishDateLocale ?: locale,
            showModeToggle = opts.showModeToggle,
            showTodayButton = opts.showTodayButton,
            showCalendarSystemToggle = opts.showCalendarSystemToggle,
            showAdjacentMonthDays = opts.showAdjacentMonthDays
        )
    } else {
        NepaliDatePicker(
            dayDecorator = dayMarks,
            state = state,
            showModeToggle = opts.showModeToggle,
            showTodayButton = opts.showTodayButton,
            showCalendarSystemToggle = opts.showCalendarSystemToggle,
            showAdjacentMonthDays = opts.showAdjacentMonthDays
        )
    }
}

/**
 * Hosts [NepaliDatePickerDocked], the compact text field that opens the calendar in a popup.
 *
 * @param initialSelectedDate date selected when the field first appears, or `null` for no selection.
 * @param locale language, date format and digit script the field renders with.
 * @param yearRangeStart first Bikram Sambat year the picker allows.
 * @param yearRangeEnd last Bikram Sambat year the picker allows.
 * @param selectableDates policy deciding which dates are enabled, or `null` to allow every date.
 * @param options appearance and behaviour knobs, or `null` for the library defaults.
 * @param events the days to mark, or `null` to draw the calendar plain. Marking never blocks a
 * date; pass a policy's `asSelectableDates()` through `selectableDates` for that.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onDateSelected invoked with the current selection whenever it changes.
 */
fun NepaliDatePickerDockedViewController(
    initialSelectedDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    options: NepaliDockedOptions?,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit,
    onDateSelected: (CustomCalendar?) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliDockedOptions()
    val dayMarks = events.toDecorator()
    val state = remember {
        NepaliDatePickerState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = opts.initialCalendarSystem
        )
    }

    LaunchedEffect(state.selectedDate) { onDateSelected(state.selectedDate) }

    NepaliDatePickerDocked(
        dayDecorator = dayMarks,
        modifier = Modifier.fillMaxWidth(),
        state = state,
        label = opts.label?.let { text -> { Text(text) } },
        placeholder = opts.placeholder?.let { text -> { Text(text) } },
        dateFormatStyle = opts.dateFormatStyle,
        showTodayButton = opts.showTodayButton,
        showCalendarSystemToggle = opts.showCalendarSystemToggle,
        showAdjacentMonthDays = opts.showAdjacentMonthDays,
        shape = RoundedCornerShape(opts.cornerRadius.dp),
        popupShadowElevation = opts.popupShadowElevation.dp
    )
}

/**
 * Hosts [NepaliWheelDatePicker], the scrolling year/month/day wheel.
 *
 * Unlike the calendar variants this picker always has a selection, so [onDateChange] never receives
 * `null`.
 *
 * @param initialDate date the wheels start on. Defaults to today when `null`.
 * @param locale language, date format and digit script the wheels render with.
 * @param yearRangeStart first Bikram Sambat year the wheel allows.
 * @param yearRangeEnd last Bikram Sambat year the wheel allows.
 * @param selectableDates policy deciding which dates are enabled, or `null` to allow every date.
 * @param options appearance knobs, or `null` for the library defaults.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onDateChange invoked every time the wheels settle on a new date.
 */
fun NepaliWheelDatePickerViewController(
    initialDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    options: NepaliWheelOptions?,
    onHeightChange: (Float) -> Unit,
    onDateChange: (CustomCalendar) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliWheelOptions()

    NepaliWheelDatePicker(
        initialDate = initialDate ?: NepaliDateConverter.todayNepaliSimpleDate,
        yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
        locale = locale,
        selectableDates = selectableDates.orAllDates(),
        itemHeight = opts.itemHeight.dp,
        visibleItemCount = opts.visibleItemCount,
        shape = RoundedCornerShape(opts.cornerRadius.dp),
        initialCalendarSystem = opts.initialCalendarSystem,
        showCalendarSystemToggle = opts.showCalendarSystemToggle,
        onDateChange = onDateChange
    )
}
