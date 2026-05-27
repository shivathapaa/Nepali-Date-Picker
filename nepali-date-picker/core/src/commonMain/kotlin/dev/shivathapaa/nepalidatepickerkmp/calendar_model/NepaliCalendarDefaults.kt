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

package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar

/**
 * Calendar-only defaults exposed by the `:nepali-date-picker:core` module.
 *
 * Holds the supported year ranges and the boundary [CustomCalendar] instances used
 * by the converter logic. Kept Compose-free so consumers who only need conversion
 * utilities can depend on `:core` without pulling Material3 / UI dependencies.
 */
object NepaliCalendarDefaults {

    /** Supported range of years in the English (Gregorian) calendar. */
    val EnglishYearRange: IntRange = IntRange(1913, 2043)

    /** Supported range of years in the Nepali (Bikram Sambat) calendar. */
    val NepaliYearRange: IntRange = IntRange(1970, 2100)

    /** Starting Nepali date used as the conversion anchor. */
    val startingNepaliCalendar: CustomCalendar = CustomCalendar(
        year = NepaliYearRange.first,
        month = 1,
        dayOfMonth = 1,
        totalDaysInMonth = 31,
        dayOfWeekInMonth = 1,
        dayOfWeek = 1,
        dayOfYear = 1,
        weekOfMonth = 1,
        era = 2,
        weekOfYear = 1,
        firstDayOfMonth = 1,
        lastDayOfMonth = 3
    )

    /** End Nepali calendar. Will change overtime as the supported range grows. */
    val endNepaliCalendar: CustomCalendar = CustomCalendar(
        year = 2100,
        month = 12,
        dayOfMonth = 31,
        era = 2,
        firstDayOfMonth = 2,
        lastDayOfMonth = 4,
        totalDaysInMonth = 31,
        dayOfWeekInMonth = 5,
        dayOfWeek = 4,
        dayOfYear = 366,
        weekOfMonth = 5,
        weekOfYear = 53
    )

    /** Starting English date corresponding to [startingNepaliCalendar]. */
    val startingEnglishCalendar: CustomCalendar = CustomCalendar(
        year = EnglishYearRange.first,
        month = 4,
        dayOfMonth = 13,
        totalDaysInMonth = 30,
        dayOfWeekInMonth = 2,
        dayOfWeek = 1,
        dayOfYear = 103,
        weekOfMonth = 3,
        era = 1,
        weekOfYear = 16,
        firstDayOfMonth = 3,
        lastDayOfMonth = 4
    )
}
