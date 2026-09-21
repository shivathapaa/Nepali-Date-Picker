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

package dev.shivathapaa.nepalidatepickerkmp.event

import dev.shivathapaa.nepalidatepickerkmp.annotation.Stable
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/**
 * Service-provider interface for supplying a calendar's events: public holidays, festivals,
 * programmes, anything an app wants named on a day.
 *
 * This library ships **no data** by design. Nepali holiday lists change year to year and every
 * institution keeps its own, so nothing here could be right for long. Implement this with your own
 * source (a static map, a CMS, an HR API, the Patro paid catalog) and hand it to
 * [NepaliCalendarPolicy].
 *
 * Implementations must:
 *   - return a stable [Set] for a given [year] (calling twice should yield the same contents),
 *   - be safe to call from the UI thread (a calendar asks once per visible cell, so answer from a
 *     memoized source),
 *   - not throw for years outside
 *     [dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults.NepaliYearRange];
 *     return [emptySet] instead.
 *
 * Example:
 * ```
 * object MyEvents : NepaliEventProvider {
 *     private val byYear: Map<Int, Set<NepaliCalendarEvent>> = mapOf(
 *         2082 to setOf(
 *             NepaliCalendarEvent(SimpleDate(2082, 1, 1), "नयाँ वर्ष", NepaliEventKind.GovernmentPublic),
 *             // …
 *         )
 *     )
 *     override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
 * }
 * ```
 *
 * The reference paid implementation is `dev.shivathapaa.patro:patro-calendar`, which ships curated
 * holiday and festival data alongside its inline calendar UI.
 */
@Stable
interface NepaliEventProvider {

    /** Everything named in [year], Bikram Sambat. An empty set is a valid answer. */
    fun events(year: Int): Set<NepaliCalendarEvent>

    /**
     * Whether the institution is shut on [date], which is what the working-day helpers count by.
     *
     * The default answers from [events], counting only the entries whose
     * [NepaliCalendarEvent.closesOffices] is set, so a day carrying nothing but an observance is
     * still a working day. Override with a memoized implementation if you call this in tight loops.
     */
    fun closesOn(date: SimpleDate): Boolean =
        events(date.year).any { it.date == date && it.closesOffices }
}

/**
 * No-op provider. Use as a default when an app wants the event-aware APIs but has not wired a data
 * source yet. Behaves as if nothing is ever named and nothing ever closes.
 */
object NoOpEventProvider : NepaliEventProvider {
    override fun events(year: Int): Set<NepaliCalendarEvent> = emptySet()
    override fun closesOn(date: SimpleDate): Boolean = false
}

/**
 * Day-of-week conventions.
 *
 * The library uses a 1-based-Sunday convention everywhere: Sunday = 1, Monday = 2, …, Saturday = 7.
 * See [dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults.FIRST_DAY_OF_WEEK].
 */
object NepaliWeekend {

    /**
     * Default weekend in Nepal: Saturday only.
     *
     * Most other libraries default to two-day weekends (Sat + Sun). Nepal observes a single-day
     * weekend, so working-day arithmetic that uses [Default] matches what a Nepali office actually
     * counts as "5 working days from today".
     */
    val Default: Set<Int> = setOf(7)
}
