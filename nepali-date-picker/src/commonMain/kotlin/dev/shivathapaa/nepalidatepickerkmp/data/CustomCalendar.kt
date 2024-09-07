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

package dev.shivathapaa.nepalidatepickerkmp.data

import androidx.compose.runtime.Immutable

/**
 * Represents a simple date with year, month, and day of the month.
 *
 * @property year The Nepali year.
 * @property month The Nepali month (1-12).
 * @property dayOfMonth The day of the month (1-32). Defaults to 1.
 */
@Immutable
data class SimpleDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int = 1
) {
    /**
     * Returns the position of a [SimpleDate] within given years range.
     */
    fun indexIn(years: IntRange): Int {
        return (year - years.first) * 12 + month - 1
    }
}

/**
 * Represents a 24Hrs format time of day (hour, minute, second, nanosecond).
 * Strictly adjusted to the `Asia/Kathmandu` TimeZone.
 *
 * @property hour Hour of the day (0-23).
 * @property minute Minute of the hour (0-59).
 * @property second Second of the minute (0-59).
 * @property nanosecond Nanosecond of the second (0-999,999,999).
 */
@Immutable
data class SimpleTime(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nanosecond: Int
)

/**
 * Represents a date in a custom calendar system with detailed information.
 *
 * This data class holds information about a specific date, including its year, month, day,
 * era (AD or BS), and various other properties related to the day and week within the month and year.
 *
 * @property year The year in the custom calendar.
 * @property month The month in the custom calendar (1-12).
 * @property dayOfMonth The day of the month (1-32).
 * @property era The era of the calendar (1 for AD, 2 for BS).
 * @property firstDayOfMonth The day of the week (1-7) for the first day of the month.
 * @property lastDayOfMonth The day of the week (1-7) for the last day of the month.
 * @property totalDaysInMonth The total number of days in the month.
 * @property dayOfWeekInMonth The number of times the day of the week occurs in the month (e.g., 5 for the fifth Friday of the month). Defaults to -1 if not applicable.
 * @property dayOfWeek The day of the week (1-7, e.g., 1 for Sunday). Defaults to -1 if not applicable.
 * @property dayOfYear The day of the year (1-366). Defaults to -1 if not applicable.
 * @property weekOfMonth The week of the month (1-5). Defaults to -1 if not applicable.
 * @property weekOfYear The week of the year (1-53). Defaults to -1 if not applicable.
 */
@Immutable
data class CustomCalendar(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val era: Int,  // 1 for AD, 2 for BS
    val firstDayOfMonth: Int,
    val lastDayOfMonth: Int,
    val totalDaysInMonth: Int,
    val dayOfWeekInMonth: Int = -1, // no of day repeated in month
    val dayOfWeek: Int = -1,
    val dayOfYear: Int = -1,
    val weekOfMonth: Int = -1,
    val weekOfYear: Int = -1
)

/**
 * Represents a calendar month in the Nepali calendar system.
 *
 * This data class provides information about a specific month in a Nepali calendar year,
 * including the total number of days, the day of the week for the first and last days of the month,
 * and the number of days from the start of the week to the first day of the month.
 *
 * @property year The Nepali year.
 * @property month The Nepali month (1-12).
 * @property totalDaysInMonth The total number of days in the month (1-32).
 * @property firstDayOfMonth The day of the week (1-7, where 1 is Sunday) for the first day of the month.
 * @property lastDayOfMonth The day of the week (1-7, where 1 is Sunday) for the last day of the month.
 * @property daysFromStartOfWeekToFirstOfMonth The number of days from the start of the week (Sunday) to the first day of the month.
 */
@Immutable
data class NepaliMonthCalendar(
    val year: Int,
    val month: Int,
    val totalDaysInMonth: Int,
    val firstDayOfMonth: Int,
    val lastDayOfMonth: Int,
    val daysFromStartOfWeekToFirstOfMonth: Int = firstDayOfMonth - 1
) {
    /**
     * Returns the position of a [CustomCalendar] within given years range.
     */
    fun indexIn(years: IntRange): Int {
        return (year - years.first) * 12 + month - 1
    }
}

/**
 * Converts a [CustomCalendar] object to a [SimpleDate] object.
 *
 * This function extracts the year, month, and day of the month from the [CustomCalendar]
 * and creates a new [SimpleDate] object with those values.
 *
 * @return A [SimpleDate] object representing the same date as the [CustomCalendar].
 */
fun CustomCalendar.toSimpleDate(): SimpleDate {
    return SimpleDate(
        year = year, month = month, dayOfMonth = dayOfMonth
    )
}

/**
 * Converts a [CustomCalendar] object to a [NepaliMonthCalendar] object.
 *
 * This function extracts the year, month, total days in the month, first day of the month,
 * and last day of the month from the [CustomCalendar] and creates a new [NepaliMonthCalendar]
 * object with those values.
 *
 * @return A [NepaliMonthCalendar] object representing the same month as the [CustomCalendar].
 */
fun CustomCalendar.toNepaliMonthCalendar(): NepaliMonthCalendar {
    return NepaliMonthCalendar(
        year = year,
        month = month,
        totalDaysInMonth = totalDaysInMonth,
        firstDayOfMonth = firstDayOfMonth,
        lastDayOfMonth = lastDayOfMonth,
        daysFromStartOfWeekToFirstOfMonth = firstDayOfMonth - 1
    )
}