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
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SimpleDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int = 1
)

@Immutable
@Serializable
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

@Immutable
@Serializable
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

fun CustomCalendar.toSimpleDate(): SimpleDate {
    return SimpleDate(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth
    )
}

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