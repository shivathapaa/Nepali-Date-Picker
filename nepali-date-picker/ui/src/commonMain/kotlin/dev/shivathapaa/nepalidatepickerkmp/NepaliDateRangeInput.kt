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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.CalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang

@Composable
internal fun NepaliDateRangeInputContent(
    selectedStartDate: CustomCalendar?,
    selectedEndDate: CustomCalendar?,
    onDatesSelectionChange: (startNepaliCalendar: CustomCalendar?, endNepaliCalendar: CustomCalendar?) -> Unit,
    calendarModel: NepaliCalendarModel,
    adapter: CalendarViewAdapter,
    language: NepaliDatePickerLang,
    nepaliSelectableDates: NepaliSelectableDates,
    colors: NepaliDatePickerColors
) {
    // The fields type dates in whichever calendar the grid was last showing.
    val yearRange = adapter.yearRange
    val errorDateOutOfYearRange =
        calendarModel.localizeNumber(
            stringToLocalize = "${language.errorDateOutOfYearRange} ${yearRange.first} - ${yearRange.last}",
            locale = language
        )

    val dateInputValidator =
        remember(adapter, nepaliSelectableDates, language) {
            NepaliDateInputValidator(
                yearRange = yearRange,
                nepaliSelectableDates = nepaliSelectableDates,
                toCanonical = adapter::toCanonical,
                errorInvalidMonthOrDay = language.errorInvalidMonthOrDay,
                errorDateInvalidInput = language.errorInvalidDay,
                errorDateOutOfYearRange = errorDateOutOfYearRange,
                errorInvalidNotAllowed = language.errorDateNotAllowed,
                errorInvalidRangeInput = language.errorInvalidRange
            )
        }
    // Apply both start and end dates for proper validation.
    dateInputValidator.apply {
        currentStartDate = selectedStartDate
        currentEndDate = selectedEndDate
    }
    Row(
        modifier = Modifier.padding(paddingValues = NepaliDateInputTextFieldPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val startRangeText = language.startDate
        NepaliDateInputTextField(
            modifier = Modifier.weight(0.5f),
            adapter = adapter,
            label = { Text(startRangeText) },
            placeholder = { Text(PatternFormat) },
            initialSelectedDate = selectedStartDate,
            onDateSelectionChange = { startDate ->
                onDatesSelectionChange(startDate, selectedEndDate)
            },
            nepaliDateInputIdentifier = NepaliDateInputIdentifier.StartDateInput,
            nepaliDateInputValidator = dateInputValidator,
            colors = colors
        )

        val endRangeText = language.endDate
        NepaliDateInputTextField(
            modifier = Modifier.weight(0.5f),
            adapter = adapter,
            label = { Text(endRangeText) },
            placeholder = { Text(PatternFormat) },
            initialSelectedDate = selectedEndDate,
            onDateSelectionChange = { endDate ->
                onDatesSelectionChange(selectedStartDate, endDate)
            },
            nepaliDateInputIdentifier = NepaliDateInputIdentifier.EndDateInput,
            nepaliDateInputValidator = dateInputValidator,
            colors = colors
        )
    }
}