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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import dev.shivathapaa.nepalidatepickerkmp.AdjacentMonthDayAlpha
import dev.shivathapaa.nepalidatepickerkmp.DualDateDayCornerRadius
import dev.shivathapaa.nepalidatepickerkmp.NepaliDay
import dev.shivathapaa.nepalidatepickerkmp.NepaliDayNumbers
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus

/**
 * Resolves one occupied slot of a calendar grid and draws it through the pickers' own day cell:
 * what the cell says, whether it is today or picked, and what happens when it is tapped.
 *
 * A calendar shows both calendars' numbers by default and a picker does not, so the shape follows
 * the same rule the pickers use: a disc while the grid draws one number per cell, and the rounded
 * box a dual-date grid needs for two.
 */
@Composable
internal fun CalendarDayCell(
    cell: MonthGridCell,
    adapter: CalendarViewAdapter,
    calendarModel: NepaliCalendarModel,
    today: SimpleDate,
    selectedDate: CustomCalendar?,
    statusByDate: Map<SimpleDate, NepaliDayStatus>,
    decorator: NepaliDayDecorator,
    colors: NepaliDatePickerColors,
    showsSecondaryDates: Boolean,
    secondaryDateLanguage: NepaliDatePickerLang?,
    accessibilityLocale: NepaliDateLocale,
    secondaryAccessibilityLocale: NepaliDateLocale?,
    monthIndex: Int,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDayClick: ((CustomCalendar, NepaliDayStatus) -> Unit)?,
    onNavigateToMonth: (monthIndex: Int) -> Unit
) {
    val day = cell.day
    // Selection and "today" run on the Bikram Sambat date, so they mean the same thing in either
    // calendar. A day with none, which only happens before the conversion anchor, is inert.
    val canonicalDate = day.canonical
    val isToday = canonicalDate != null && today == canonicalDate.toSimpleDate()
    val isSelected = canonicalDate != null && selectedDate == canonicalDate
    val isAdjacentMonth = cell.monthOffset != 0

    val secondaryDayNumber = if (secondaryDateLanguage != null) day.secondary?.dayOfMonth else null

    val decoration = remember(decorator, canonicalDate, day.displayed, isToday, isSelected, cell) {
        canonicalDate?.let {
            decorator.decorate(
                NepaliDayInfo(
                    date = it,
                    displayedDate = day.displayed,
                    isToday = isToday,
                    isSelected = isSelected,
                    isInRange = false,
                    isEnabled = true,
                    isAdjacentMonth = isAdjacentMonth
                )
            )
        }
    }

    val contentDescription = remember(
        day, isToday, cell, adapter, secondaryAccessibilityLocale, decoration
    ) {
        buildString {
            append(adapter.format(day.displayed, accessibilityLocale))
            val secondaryDate = day.secondary
            if (secondaryAccessibilityLocale != null && secondaryDate != null) {
                append(", ")
                append(
                    adapter.formatSecondary(
                        secondaryDate = secondaryDate,
                        calendarModel = calendarModel,
                        locale = secondaryAccessibilityLocale
                    )
                )
            }
            if (isToday) {
                append(", ")
                append(calendarModel.locale.language.today)
            }
            // The fading is what tells a sighted user this cell moves the grid.
            if (isAdjacentMonth) {
                append(", ")
                append(calendarModel.locale.language.adjacentMonthDayContentDescription)
            }
            decoration?.contentDescription?.takeIf { it.isNotEmpty() }?.let {
                append(", ")
                append(it)
            }
        }
    }

    NepaliDay(
        // A neighbouring month's day is faded so it reads as context around the displayed month
        // rather than part of it.
        modifier = if (isAdjacentMonth) Modifier.alpha(AdjacentMonthDayAlpha) else Modifier,
        selected = isSelected,
        onClick = {
            canonicalDate?.let { date ->
                onDateSelectionChange(date)
                onDayClick?.invoke(date, statusByDate[date.toSimpleDate()] ?: NepaliDayStatus.Working)
            }
            if (isAdjacentMonth) onNavigateToMonth(monthIndex + cell.monthOffset)
        },
        animateChecked = isSelected,
        enabled = canonicalDate != null,
        today = isToday,
        colors = colors,
        dateContentDescription = contentDescription,
        // The shape follows the grid rather than the cell, exactly as a picker's does: a day the
        // conversion table cannot pair keeps the box its neighbours are drawn in.
        shape = if (showsSecondaryDates) {
            RoundedCornerShape(DualDateDayCornerRadius)
        } else {
            CircleShape
        },
        decoration = decoration,
        isDualDateCell = secondaryDayNumber != null
    ) {
        NepaliDayNumbers(
            dayNumber = day.displayed.dayOfMonth,
            secondaryDayNumber = secondaryDayNumber,
            calendarModel = calendarModel,
            secondaryDateLanguage = secondaryDateLanguage
        )
    }
}
