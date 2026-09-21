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

package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import dev.shivathapaa.nepalidatepickerkmp.NepaliDaysInWeek
import dev.shivathapaa.nepalidatepickerkmp.NepaliMaxCalendarRows
import dev.shivathapaa.nepalidatepickerkmp.RecommendedSizeForAccessibility
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus

/**
 * The weekday letters over a calendar grid, each as wide as the column it labels, so the letters
 * stay above their days however wide the calendar is drawn.
 *
 * Each letter announces its full weekday name, which is what a screen reader reads instead of the
 * single character.
 */
@Composable
internal fun NepaliCalendarWeekDays(
    colors: NepaliDatePickerColors,
    language: NepaliDatePickerLang,
    weekDayFormat: NameFormat,
    modifier: Modifier = Modifier
) {
    val dayNames = remember(language, weekDayFormat) {
        val firstDayOfWeek = NepaliDatePickerDefaults.FIRST_DAY_OF_WEEK
        (firstDayOfWeek..firstDayOfWeek + NepaliDaysInWeek - 1).map { dayIndex ->
            val weekday = language.weekdays[dayIndex - 1]
            val display = if (weekDayFormat == NameFormat.SHORT) weekday.short else weekday.medium
            display to weekday.full
        }
    }

    Row(
        modifier = modifier.fillMaxWidth()
            .defaultMinSize(minHeight = RecommendedSizeForAccessibility),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayNames.forEach { (display, fullName) ->
            Text(
                text = display,
                modifier = Modifier.weight(1f)
                    .clearAndSetSemantics { contentDescription = fullName },
                color = colors.weekdayContentColor,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * One month of a calendar, six rows of seven cells filling the width it is given.
 *
 * The grid is always six rows tall, whatever the month's shape, so paging between months never
 * moves the content below it. A slot with no day in it, at the edges of the year range or of a grid
 * drawn without its neighbours' days, is left empty.
 *
 * Days are marked from one [NepaliCalendarPolicy.monthStatus] pass per Bikram Sambat month the grid
 * reaches into, and [dayDecorator] is laid over that, so an app's own marks sit on top of the
 * policy's without either hiding the other.
 *
 * @param onDayClick invoked with the tapped day and what the policy says about it. A day with no
 *   Bikram Sambat date, which only happens before the conversion anchor, is inert and never calls it.
 * @param onNavigateToMonth invoked with a pager index when a day of a neighbouring month is tapped.
 */
@Composable
internal fun NepaliCalendarMonth(
    monthCalendar: MonthCalendar,
    adapter: CalendarViewAdapter,
    calendarModel: NepaliCalendarModel,
    today: SimpleDate,
    selectedDate: CustomCalendar?,
    policy: NepaliCalendarPolicy,
    markerColors: NepaliDayMarkerColors,
    eventDisplayStyle: NepaliEventDisplayStyle,
    dayDecorator: NepaliDayDecorator?,
    colors: NepaliDatePickerColors,
    secondaryDateLocale: NepaliDateLocale?,
    showAdjacentMonthDays: Boolean,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDayClick: ((CustomCalendar, NepaliDayStatus) -> Unit)?,
    onNavigateToMonth: (monthIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cells = remember(monthCalendar, adapter, secondaryDateLocale, showAdjacentMonthDays) {
        adapter.monthGrid(
            month = monthCalendar,
            withSecondary = secondaryDateLocale != null,
            withAdjacentDays = showAdjacentMonthDays
        )
    }
    val statusByDate = remember(cells, policy) { monthStatusByDate(cells, policy) }
    val decorator = remember(statusByDate, markerColors, eventDisplayStyle, dayDecorator) {
        val policyMarks = NepaliDayDecorator { day ->
            statusByDate[day.date.toSimpleDate()]?.toDayDecoration(markerColors, eventDisplayStyle)
        }
        if (dayDecorator == null) policyMarks else policyMarks.then(dayDecorator)
    }
    val monthIndex = remember(monthCalendar, adapter) { monthCalendar.indexIn(adapter.yearRange) }

    // The full date a screen reader reads for a cell, and the other calendar's date after it. LONG
    // for the second one, since the weekday it would add has already been said once.
    val accessibilityLocale = remember(calendarModel) {
        calendarModel.locale.copy(dateFormat = NepaliDateFormatStyle.FULL)
    }
    val secondaryAccessibilityLocale = remember(calendarModel, secondaryDateLocale) {
        secondaryDateLocale?.let {
            calendarModel.locale.copy(language = it.language, dateFormat = NepaliDateFormatStyle.LONG)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
            .requiredHeight(RecommendedSizeForAccessibility * NepaliMaxCalendarRows)
    ) {
        for (weekIndex in 0 until NepaliMaxCalendarRows) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .requiredHeight(RecommendedSizeForAccessibility),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayIndex in 0 until NepaliDaysInWeek) {
                    val cell = cells[weekIndex * NepaliDaysInWeek + dayIndex]
                    // The seven columns share the width, and each holds the pickers' own day cell
                    // at its own size, so a calendar drawn wider than a picker spreads its days
                    // rather than stretching them.
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell != null) {
                            CalendarDayCell(
                                cell = cell,
                                adapter = adapter,
                                calendarModel = calendarModel,
                                today = today,
                                selectedDate = selectedDate,
                                statusByDate = statusByDate,
                                decorator = decorator,
                                colors = colors,
                                showsSecondaryDates = secondaryDateLocale != null,
                                secondaryDateLanguage = secondaryDateLocale?.language,
                                accessibilityLocale = accessibilityLocale,
                                secondaryAccessibilityLocale = secondaryAccessibilityLocale,
                                monthIndex = monthIndex,
                                onDateSelectionChange = onDateSelectionChange,
                                onDayClick = onDayClick,
                                onNavigateToMonth = onNavigateToMonth
                            )
                        }
                    }
                }
            }
        }
    }
}
