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

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp.android

import android.content.Context
import android.view.View
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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedPickerDefaults

/**
 * The range picker as a plain [View], for hosts that cannot compose. Mirrors the iOS
 * `NepaliDateRangePickerViewController` parameter for parameter.
 *
 * [onRangeSelected] fires with the full calendars for the start and end whenever either changes;
 * an incomplete range reports `null` for the missing end.
 */
fun NepaliDateRangePickerView(
    context: Context,
    initialSelectedStartDate: SimpleDate? = null,
    initialSelectedEndDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showMonthsVertically: Boolean = true,
    showYearPickerAndMonthNavigation: Boolean = true,
    showEnglishDate: Boolean = false,
    englishDateLocale: NepaliDateLocale? = null,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onRangeSelected: (CustomCalendar?, CustomCalendar?) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    val dayMarks = events.toDecorator()
    val state = remember {
        NepaliDateRangePickerState(
            initialSelectedStartNepaliDate = initialSelectedStartDate,
            initialSelectedEndNepaliDate = initialSelectedEndDate,
            initialDisplayedMonth = initialSelectedStartDate,
            yearRange = yearRange,
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = initialCalendarSystem
        )
    }
    LaunchedEffect(state.selectedStartNepaliDate, state.selectedEndNepaliDate) {
        onRangeSelected(state.selectedStartNepaliDate, state.selectedEndNepaliDate)
    }

    if (showEnglishDate) {
        NepaliDateRangePickerWithEnglishDate(
            dayDecorator = dayMarks,
            state = state,
            englishDateLocale = englishDateLocale ?: locale,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showMonthsVertically = showMonthsVertically,
            showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    } else {
        NepaliDateRangePicker(
            dayDecorator = dayMarks,
            state = state,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showMonthsVertically = showMonthsVertically,
            showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    }
}

/**
 * The range text field, with an optional picker dialog behind it, as a plain [View]. Mirrors the
 * iOS `NepaliDateRangeFieldViewController`.
 *
 * [outlined] chooses the bare text field; the filled variant opens the calendar dialog and takes
 * the confirm and dismiss texts. [onRangeChange] fires with the plain dates as they are typed or
 * picked, `null` for a side that is empty or unparseable.
 */
fun NepaliDateRangeFieldView(
    context: Context,
    initialStartValue: SimpleDate? = null,
    initialEndValue: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale,
    dateFormat: Pattern = Pattern.YYYY_SLASH_MM_SLASH_DD,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    outlined: Boolean = true,
    startLabel: String? = null,
    endLabel: String? = null,
    supportingText: String? = null,
    isStartError: Boolean = false,
    isEndError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    confirmButtonText: String? = null,
    dismissButtonText: String? = null,
    cornerRadius: Float = EmbeddedPickerDefaults.FIELD_CORNER_RADIUS,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onRangeChange: (SimpleDate?, SimpleDate?) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    val dayMarks = events.toDecorator()
    var start by remember { mutableStateOf(initialStartValue) }
    var end by remember { mutableStateOf(initialEndValue) }

    val shape = RoundedCornerShape(cornerRadius.dp)
    val startLabelSlot: @Composable () -> Unit =
        startLabel?.let { text -> { Text(text) } } ?: { Text(locale.language.startDate) }
    val endLabelSlot: @Composable () -> Unit =
        endLabel?.let { text -> { Text(text) } } ?: { Text(locale.language.endDate) }
    val supportingTextSlot: (@Composable () -> Unit)? =
        supportingText?.let { text -> { Text(text) } }

    val handleChange: (SimpleDate?, SimpleDate?) -> Unit = { newStart, newEnd ->
        start = newStart
        end = newEnd
        onRangeChange(newStart, newEnd)
    }

    if (outlined) {
        NepaliDateRangeTextField(
            modifier = Modifier.fillMaxWidth(),
            startValue = start,
            endValue = end,
            onRangeChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRange,
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            startLabel = startLabelSlot,
            endLabel = endLabelSlot,
            supportingText = supportingTextSlot,
            isStartError = isStartError,
            isEndError = isEndError,
            enabled = enabled,
            readOnly = readOnly,
            shape = shape,
            calendarSystem = initialCalendarSystem
        )
    } else {
        NepaliDateRangeField(
            dayDecorator = dayMarks,
            modifier = Modifier.fillMaxWidth(),
            startValue = start,
            endValue = end,
            onRangeChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRange,
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            startLabel = startLabelSlot,
            endLabel = endLabelSlot,
            supportingText = supportingTextSlot,
            isStartError = isStartError,
            isEndError = isEndError,
            enabled = enabled,
            readOnly = readOnly,
            shape = shape,
            confirmButtonText = confirmButtonText ?: locale.language.okText,
            dismissButtonText = dismissButtonText ?: locale.language.cancelText,
            calendarSystem = initialCalendarSystem,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    }
}
