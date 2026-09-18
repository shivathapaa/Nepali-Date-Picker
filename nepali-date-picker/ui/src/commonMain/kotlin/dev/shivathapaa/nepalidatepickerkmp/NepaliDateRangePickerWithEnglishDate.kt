/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
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

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale

/**
 * NepaliDateRangePickerWithEnglishDate let people select a range of Nepali dates and can be embedded
 * into dialogs. See [NepaliDatePickerDialog]
 *
 * Nepali date picker lets you pick Nepali dates via a calendar UI which displays both Nepali and
 * English dates.
 *
 * This is [NepaliDateRangePicker] with its secondary date always on. Reach for
 * [NepaliDateRangePicker] with `secondaryDateLocale` directly when you also want the `B.S.` / `A.D.`
 * switch, or want the pairing to be conditional.
 *
 * @param state state of the Nepali date range picker. See [rememberNepaliDateRangePickerState].
 * @param modifier the [Modifier] to be applied to this Nepali date range picker.
 * @param englishDateLocale the locale [NepaliDateLocale] for the english date
 * @param title the title to be displayed in the Nepali date range picker
 * @param headline the headline to be displayed in the Nepali date range picker
 * @param showModeToggle the boolean to let user toggle between Date Picker and Date Input
 * @param showTodayButton the control to either show or hide `TODAY` button for navigating to today's date.
 * It is also affected by [showYearPickerAndMonthNavigation].
 * @param showMonthsVertically the control to either show months vertically or horizontally.
 * Note: When set to `true` (vertical display), the date picker uses vertical scrolling. This can
 * lead to runtime errors if placed within a composable that also uses vertical scrolling, such as
 * a `Column` with vertical scroll or a `LazyColumn`. Ensure your layout accommodates the vertical
 * scrolling behavior to avoid these errors.
 * @param showYearPickerAndMonthNavigation the control to either show or hide `YearPicker` button and
 * `MonthNavigation` buttons. [showTodayButton] is affected by this.
 * @param showCalendarSystemToggle the boolean to show the `B.S.` / `A.D.` switch, which drives
 * [NepaliDateRangePickerState.displayedCalendarSystem]. It sits at the end of the title, so it is
 * independent of [showYearPickerAndMonthNavigation].
 * @param showAdjacentMonthDays the boolean to fill each month's empty cells with the neighbouring
 * months' days, drawn faded. Tapping one selects that day as the range's start or end and moves the
 * grid to its month.
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 *
 * Example usage:
 * ```
 * val defaultNepaliDateRangePickerState = rememberNepaliDateRangePickerState()
 *
 * NepaliDateRangePickerWithEnglishDate(defaultNepaliDateRangePickerState)
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePicker
 * @see rememberNepaliDateRangePickerState
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDateRangePickerWithEnglishDate(
    state: NepaliDateRangePickerState,
    modifier: Modifier = Modifier,
    englishDateLocale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDateRangePickerTitle(
            modifier = Modifier.padding(NepaliDateRangePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode,
            calendarSystem = state.displayedCalendarSystem
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDateRangePickerHeadlineWithEnglishDate(
            modifier = Modifier.padding(NepaliEnglishDateRangePickerHeadlinePadding),
            selectedNepaliStartDate = state.selectedStartNepaliDate,
            selectedNepaliEndDate = state.selectedEndNepaliDate,
            selectedEnglishStartDate = state.selectedStartEnglishDate,
            selectedEnglishEndDate = state.selectedEndEnglishDate,
            englishLocale = englishDateLocale,
            locale = state.locale,
            calendarSystem = state.displayedCalendarSystem
        )
    },
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showMonthsVertically: Boolean = true,
    showYearPickerAndMonthNavigation: Boolean = true,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    NepaliDateRangePicker(
        state = state,
        modifier = modifier,
        title = title,
        headline = headline,
        showModeToggle = showModeToggle,
        showTodayButton = showTodayButton,
        showMonthsVertically = showMonthsVertically,
        showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation,
        secondaryDateLocale = englishDateLocale,
        showCalendarSystemToggle = showCalendarSystemToggle,
        showAdjacentMonthDays = showAdjacentMonthDays,
        colors = colors
    )
}

private val NepaliEnglishDateRangePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)
