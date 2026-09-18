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

/**
 * The calendar a date is expressed in.
 *
 * This is the named form of the `era` integer carried by [CustomCalendar] and [MonthCalendar]
 * (1 = AD, 2 = BS), so the two representations never disagree: [era] is the single mapping.
 *
 * The pickers use this to decide which calendar to *display*. A selected date is always stored as
 * [BIKRAM_SAMBAT], whichever system is on screen, so switching the display is lossless.
 *
 * @property era the `era` value a [CustomCalendar] in this system carries.
 */
enum class CalendarSystem(val era: Int) {
    /** Bikram Sambat, the official calendar of Nepal. `era` 2. */
    BIKRAM_SAMBAT(2),

    /** Gregorian, referred to as the English or AD calendar throughout this library. `era` 1. */
    GREGORIAN(1);

    /** The other system. With two systems this is the toggle target. */
    fun opposite(): CalendarSystem =
        if (this == BIKRAM_SAMBAT) GREGORIAN else BIKRAM_SAMBAT

    companion object {
        /**
         * The system carrying [era], or `null` when [era] is neither 1 nor 2.
         *
         * Returns `null` rather than throwing because `era` reaches this from parsed and restored
         * values that callers are expected to fall back on, not to crash over.
         */
        fun fromEra(era: Int): CalendarSystem? = entries.firstOrNull { it.era == era }
    }
}

/**
 * The [CalendarSystem] this calendar is expressed in, or [CalendarSystem.BIKRAM_SAMBAT] when
 * [CustomCalendar.era] holds an unrecognised value.
 */
val CustomCalendar.calendarSystem: CalendarSystem
    get() = CalendarSystem.fromEra(era) ?: CalendarSystem.BIKRAM_SAMBAT
