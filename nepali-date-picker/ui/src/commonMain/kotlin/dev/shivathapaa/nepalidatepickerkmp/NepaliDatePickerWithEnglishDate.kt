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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale

/**
 * NepaliDatePickerWithEnglishDate lets user select a date and preferably should be embedded into Dialogs.
 * Check [NepaliDatePickerDialog].
 *
 * Nepali date picker lets you pick a Nepali date via a calendar UI which displays both Nepali and
 * English dates.
 *
 * This is [NepaliDatePicker] with its secondary date always on. Reach for [NepaliDatePicker] with
 * `secondaryDateLocale` directly when you also want the `B.S.` / `A.D.` switch, or want the pairing
 * to be conditional.
 *
 * @param state state of the date picker. See [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
 * @param englishDateLocale the locale [NepaliDateLocale] for the english date
 * @param title the title to be displayed in the date picker
 * @param headline the headline to be displayed in the date picker
 * @param showModeToggle the boolean to let user toggle between Date Picker and Date Input
 * @param showTodayButton the boolean to control either to show `TODAY` button or not
 * @param showCalendarSystemToggle the boolean to show the `B.S.` / `A.D.` switch, which drives
 * [NepaliDatePickerState.displayedCalendarSystem]
 * @param showAdjacentMonthDays the boolean to fill the grid's empty cells with the neighbouring
 * months' days, drawn faded. Tapping one selects that day and moves the grid to its month.
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 *
 * Example usage:
 * ```
 * val defaultNepaliDatePickerState = rememberNepaliDatePickerState()
 *
 * NepaliDatePickerWithEnglishDate(state = defaultNepaliDatePickerState)
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePicker
 * @see NepaliDateRangePicker
 * @see NepaliDateRangePickerWithEnglishDate
 */
@Composable
fun NepaliDatePickerWithEnglishDate(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    englishDateLocale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerTitle(
            modifier = Modifier.padding(NepaliDatePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode,
            calendarSystem = state.displayedCalendarSystem
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerHeadlineWithEnglishDate(
            modifier = Modifier.padding(NepaliDatePickerHeadlinePadding),
            selectedDate = state.selectedDate,
            selectedEnglishDate = state.selectedEnglishDate,
            locale = state.locale,
            englishLocale = englishDateLocale,
            displayMode = state.displayMode,
            calendarSystem = state.displayedCalendarSystem
        )
    },
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    NepaliDatePicker(
        state = state,
        modifier = modifier,
        title = title,
        headline = headline,
        secondaryDateLocale = englishDateLocale,
        showModeToggle = showModeToggle,
        showTodayButton = showTodayButton,
        showCalendarSystemToggle = showCalendarSystemToggle,
        showAdjacentMonthDays = showAdjacentMonthDays,
        colors = colors
    )
}

private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
