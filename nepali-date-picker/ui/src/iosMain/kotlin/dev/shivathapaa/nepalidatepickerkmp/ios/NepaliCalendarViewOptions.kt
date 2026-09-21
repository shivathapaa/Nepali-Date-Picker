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

package dev.shivathapaa.nepalidatepickerkmp.ios

import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale

/**
 * How a hosted month calendar looks and what it carries, for a Swift caller.
 *
 * Kotlin default arguments do not cross the Objective-C bridge, so every switch is a property with
 * the same default the Compose parameter has. The name says `View` because the picker surface
 * already exports `NepaliCalendarOptions`, which describes a date picker rather than a calendar.
 *
 * @property showSecondaryDates whether every cell also shows the same day in the other calendar,
 * which a Nepali calendar is usually read against. On by default.
 * @property secondaryDateLocale the language and digits those second numbers are written in. `null`
 * uses the calendar's own locale.
 * @property showAdjacentMonthDays whether the slots around the month are filled with its
 * neighbours' days. On by default, which is what makes the grid read as a wall calendar.
 * @property showDaySummary whether the picked day is written out under the grid.
 * @property showMonthEvents whether the month's events are listed under the grid, below the day's
 * summary when both are shown. Both stack inside the one hosted view, so the reported height grows
 * with them.
 */
class NepaliCalendarViewOptions {
    var showTodayButton: Boolean = true
    var showCalendarSystemToggle: Boolean = false
    var showAdjacentMonthDays: Boolean = true
    var showSecondaryDates: Boolean = true
    var secondaryDateLocale: NepaliDateLocale? = null
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showDaySummary: Boolean = false
    var showMonthEvents: Boolean = false
}

/**
 * The locale the second number in each cell is written in, or `null` when the host asked for one
 * calendar only. Falls back to the calendar's own [locale] when the host named no separate one.
 */
internal fun NepaliCalendarViewOptions.secondaryLocaleOrNull(
    locale: NepaliDateLocale
): NepaliDateLocale? = if (showSecondaryDates) secondaryDateLocale ?: locale else null
