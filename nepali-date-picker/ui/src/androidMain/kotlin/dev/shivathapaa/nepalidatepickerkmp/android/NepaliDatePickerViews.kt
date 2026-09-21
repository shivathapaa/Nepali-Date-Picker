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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedPickerDefaults

/**
 * The calendar picker as a plain [View], for hosts that cannot compose: a Flutter platform view
 * or an XML layout. Mirrors the iOS `NepaliDatePickerViewController` parameter for parameter.
 *
 * [onDateSelected] fires with the full calendar for every selection, and with `null` when the
 * selection is cleared. [onHeightChange] reports the content height in density independent points
 * whenever it changes, for hosts that size the view themselves.
 */
fun NepaliDatePickerView(
    context: Context,
    initialSelectedDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showEnglishDate: Boolean = false,
    englishDateLocale: NepaliDateLocale? = null,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onDateSelected: (CustomCalendar?) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    val dayMarks = events.toDecorator()
    val state = remember {
        NepaliDatePickerState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRange,
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = initialCalendarSystem
        )
    }
    LaunchedEffect(state.selectedDate) { onDateSelected(state.selectedDate) }

    if (showEnglishDate) {
        NepaliDatePickerWithEnglishDate(
            dayDecorator = dayMarks,
            state = state,
            englishDateLocale = englishDateLocale ?: locale,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    } else {
        NepaliDatePicker(
            dayDecorator = dayMarks,
            state = state,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    }
}

/**
 * The docked picker, a text field opening a calendar popup, as a plain [View]. Mirrors the iOS
 * `NepaliDatePickerDockedViewController`.
 *
 * [label] and [placeholder] arrive as plain strings because a View-based host has no composable
 * slots to fill.
 */
fun NepaliDatePickerDockedView(
    context: Context,
    initialSelectedDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    dateFormatStyle: NepaliDateFormatStyle = NepaliDateFormatStyle.MEDIUM,
    showTodayButton: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    cornerRadius: Float = EmbeddedPickerDefaults.FIELD_CORNER_RADIUS,
    popupShadowElevation: Float = EmbeddedPickerDefaults.DOCKED_POPUP_ELEVATION,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onDateSelected: (CustomCalendar?) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    val dayMarks = events.toDecorator()
    val state = remember {
        NepaliDatePickerState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRange,
            initialDisplayMode = DisplayMode.Picker,
            nepaliSelectableDates = selectableDates.orAllDates(),
            locale = locale,
            initialCalendarSystem = initialCalendarSystem
        )
    }
    LaunchedEffect(state.selectedDate) { onDateSelected(state.selectedDate) }

    NepaliDatePickerDocked(
        dayDecorator = dayMarks,
        modifier = Modifier.fillMaxWidth(),
        state = state,
        label = label?.let { text -> { Text(text) } },
        placeholder = placeholder?.let { text -> { Text(text) } },
        dateFormatStyle = dateFormatStyle,
        showTodayButton = showTodayButton,
        showCalendarSystemToggle = showCalendarSystemToggle,
        showAdjacentMonthDays = showAdjacentMonthDays,
        shape = RoundedCornerShape(cornerRadius.dp),
        popupShadowElevation = popupShadowElevation.dp
    )
}

/**
 * The wheel picker as a plain [View]. Mirrors the iOS `NepaliWheelDatePickerViewController`.
 *
 * The wheel always has a selection, so [onDateChange] fires with a full calendar for the initial
 * date and for every spin after it.
 */
fun NepaliWheelDatePickerView(
    context: Context,
    initialDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    itemHeight: Float = EmbeddedPickerDefaults.WHEEL_ITEM_HEIGHT,
    visibleItemCount: Int = EmbeddedPickerDefaults.WHEEL_VISIBLE_ITEM_COUNT,
    cornerRadius: Float = EmbeddedPickerDefaults.WHEEL_CORNER_RADIUS,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    onHeightChange: (Float) -> Unit = {},
    onDateChange: (CustomCalendar) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    NepaliWheelDatePicker(
        initialDate = initialDate ?: NepaliDateConverter.todayNepaliSimpleDate,
        yearRange = yearRange,
        locale = locale,
        selectableDates = selectableDates.orAllDates(),
        itemHeight = itemHeight.dp,
        visibleItemCount = visibleItemCount,
        shape = RoundedCornerShape(cornerRadius.dp),
        initialCalendarSystem = initialCalendarSystem,
        showCalendarSystemToggle = showCalendarSystemToggle,
        onDateChange = onDateChange
    )
}
