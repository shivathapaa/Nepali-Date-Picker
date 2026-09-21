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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeTextField
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import platform.UIKit.UIViewController

/**
 * Hosts [NepaliDateRangePicker], the two-ended calendar used to pick a start and an end date.
 *
 * @param initialSelectedStartDate start of the pre-selected range, or `null` for no selection.
 * @param initialSelectedEndDate end of the pre-selected range, or `null` for no selection.
 * @param locale language, date format and digit script the picker renders with.
 * @param yearRangeStart first Bikram Sambat year the picker allows.
 * @param yearRangeEnd last Bikram Sambat year the picker allows.
 * @param selectableDates policy deciding which dates are enabled, or `null` to allow every date.
 * @param options appearance and behaviour knobs, or `null` for the library defaults.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onRangeSelected invoked with both ends whenever either changes. Either end may be `null`
 * while the range is still being built.
 */
fun NepaliDateRangePickerViewController(
    initialSelectedStartDate: SimpleDate?,
    initialSelectedEndDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    options: NepaliRangeCalendarOptions?,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit,
    onRangeSelected: (CustomCalendar?, CustomCalendar?) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliRangeCalendarOptions()
    val dayMarks = events.toDecorator()
    val state = remember {
        NepaliDateRangePickerState(
            initialSelectedStartNepaliDate = initialSelectedStartDate,
            initialSelectedEndNepaliDate = initialSelectedEndDate,
            initialDisplayedMonth = initialSelectedStartDate,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = opts.initialCalendarSystem
        )
    }

    LaunchedEffect(state.selectedStartNepaliDate, state.selectedEndNepaliDate) {
        onRangeSelected(state.selectedStartNepaliDate, state.selectedEndNepaliDate)
    }

    if (opts.showEnglishDate) {
        NepaliDateRangePickerWithEnglishDate(
            dayDecorator = dayMarks,
            state = state,
            englishDateLocale = opts.englishDateLocale ?: locale,
            showModeToggle = opts.showModeToggle,
            showTodayButton = opts.showTodayButton,
            showMonthsVertically = opts.showMonthsVertically,
            showYearPickerAndMonthNavigation = opts.showYearPickerAndMonthNavigation,
            showCalendarSystemToggle = opts.showCalendarSystemToggle,
            showAdjacentMonthDays = opts.showAdjacentMonthDays
        )
    } else {
        NepaliDateRangePicker(
            dayDecorator = dayMarks,
            state = state,
            showModeToggle = opts.showModeToggle,
            showTodayButton = opts.showTodayButton,
            showMonthsVertically = opts.showMonthsVertically,
            showYearPickerAndMonthNavigation = opts.showYearPickerAndMonthNavigation,
            showCalendarSystemToggle = opts.showCalendarSystemToggle,
            showAdjacentMonthDays = opts.showAdjacentMonthDays
        )
    }
}

/**
 * Hosts the pair of typed entry fields for a date range.
 *
 * `options.outlined` picks [NepaliDateRangeTextField] when true and the filled
 * [NepaliDateRangeField] when false. The controller owns the field contents, so Swift only observes
 * them through [onRangeChange].
 *
 * @param initialStartValue start date the fields open with, or `null` for empty.
 * @param initialEndValue end date the fields open with, or `null` for empty.
 * @param locale language, date format and digit script the fields render with.
 * @param dateFormat digit layout the user types into.
 * @param yearRangeStart first Bikram Sambat year accepted.
 * @param yearRangeEnd last Bikram Sambat year accepted.
 * @param selectableDates policy deciding which dates are valid, or `null` to accept every date.
 * @param options appearance and behaviour knobs, or `null` for the library defaults.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onRangeChange invoked whenever either field parses to a new value. An end that has not
 * been typed yet arrives as `null`.
 */
fun NepaliDateRangeFieldViewController(
    initialStartValue: SimpleDate?,
    initialEndValue: SimpleDate?,
    locale: NepaliDateLocale,
    dateFormat: Pattern,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    options: NepaliRangeFieldOptions?,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit,
    onRangeChange: (SimpleDate?, SimpleDate?) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliRangeFieldOptions()
    val dayMarks = events.toDecorator()
    var start by remember { mutableStateOf(initialStartValue) }
    var end by remember { mutableStateOf(initialEndValue) }

    val shape = RoundedCornerShape(opts.cornerRadius.dp)
    // A null label keeps the composable's own localized default rather than removing the label.
    val startLabel: @Composable () -> Unit =
        opts.startLabel?.let { text -> { Text(text) } } ?: { Text(locale.language.startDate) }
    val endLabel: @Composable () -> Unit =
        opts.endLabel?.let { text -> { Text(text) } } ?: { Text(locale.language.endDate) }
    val supportingText: (@Composable () -> Unit)? =
        opts.supportingText?.let { text -> { Text(text) } }

    val handleChange: (SimpleDate?, SimpleDate?) -> Unit = { newStart, newEnd ->
        start = newStart
        end = newEnd
        onRangeChange(newStart, newEnd)
    }

    if (opts.outlined) {
        NepaliDateRangeTextField(
            modifier = Modifier.fillMaxWidth(),
            startValue = start,
            endValue = end,
            onRangeChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            startLabel = startLabel,
            endLabel = endLabel,
            supportingText = supportingText,
            isStartError = opts.isStartError,
            isEndError = opts.isEndError,
            enabled = opts.enabled,
            readOnly = opts.readOnly,
            shape = shape,
            calendarSystem = opts.initialCalendarSystem
        )
    } else {
        NepaliDateRangeField(
            dayDecorator = dayMarks,
            modifier = Modifier.fillMaxWidth(),
            startValue = start,
            endValue = end,
            onRangeChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            startLabel = startLabel,
            endLabel = endLabel,
            supportingText = supportingText,
            isStartError = opts.isStartError,
            isEndError = opts.isEndError,
            enabled = opts.enabled,
            readOnly = opts.readOnly,
            shape = shape,
            confirmButtonText = opts.confirmButtonText ?: locale.language.okText,
            dismissButtonText = opts.dismissButtonText ?: locale.language.cancelText,
            calendarSystem = opts.initialCalendarSystem,
            showCalendarSystemToggle = opts.showCalendarSystemToggle,
            showAdjacentMonthDays = opts.showAdjacentMonthDays
        )
    }
}
