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

package dev.shivathapaa.nepalidatepickerkmp.data

import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable

/**
 * The geometry of one month in a given [CalendarSystem]: enough to lay out a weekday grid without
 * knowing which calendar it came from.
 *
 * [NepaliMonthCalendar] is the Bikram Sambat-only equivalent that predates this type and stays the
 * published shape of `NepaliDatePickerState.displayedMonth`. Use [MonthCalendar] when the calendar
 * is a runtime choice, and [toNepaliMonthCalendar] / [toMonthCalendar] to move between them.
 *
 * @property calendarSystem the calendar [year] and [month] are expressed in.
 * @property year the year in [calendarSystem].
 * @property month the month (1-12). 1 is Baisakh in Bikram Sambat, January in Gregorian.
 * @property totalDaysInMonth the number of days in the month (28-32, depending on the calendar).
 * @property firstDayOfMonth the day of the week (1-7, 1 = Sunday) the month starts on.
 * @property lastDayOfMonth the day of the week (1-7, 1 = Sunday) the month ends on.
 */
@Immutable
data class MonthCalendar(
    val calendarSystem: CalendarSystem,
    val year: Int,
    val month: Int,
    val totalDaysInMonth: Int,
    val firstDayOfMonth: Int,
    val lastDayOfMonth: Int
) {
    /** Leading blank cells before day 1 when the grid starts on Sunday. */
    val daysFromStartOfWeekToFirstOfMonth: Int get() = firstDayOfMonth - 1

    /**
     * The position of this month within [years], counting 12 months per year.
     *
     * [years] must be a range in the same [calendarSystem]; the pagers hold one range per system.
     */
    fun indexIn(years: IntRange): Int = (year - years.first) * 12 + month - 1
}

/** Reads this Bikram Sambat month as a calendar-tagged [MonthCalendar]. */
fun NepaliMonthCalendar.toMonthCalendar(): MonthCalendar = MonthCalendar(
    calendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    year = year,
    month = month,
    totalDaysInMonth = totalDaysInMonth,
    firstDayOfMonth = firstDayOfMonth,
    lastDayOfMonth = lastDayOfMonth
)

/**
 * Narrows this month to the Bikram Sambat-only [NepaliMonthCalendar].
 *
 * The year and month are copied verbatim, so calling this on a [CalendarSystem.GREGORIAN] month
 * produces a `NepaliMonthCalendar` holding Gregorian numbers. Convert the month to Bikram Sambat
 * first when that matters.
 */
fun MonthCalendar.toNepaliMonthCalendar(): NepaliMonthCalendar = NepaliMonthCalendar(
    year = year,
    month = month,
    totalDaysInMonth = totalDaysInMonth,
    firstDayOfMonth = firstDayOfMonth,
    lastDayOfMonth = lastDayOfMonth
)
