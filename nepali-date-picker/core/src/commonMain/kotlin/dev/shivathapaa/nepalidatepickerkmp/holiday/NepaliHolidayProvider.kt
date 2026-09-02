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

package dev.shivathapaa.nepalidatepickerkmp.holiday

import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable
import dev.shivathapaa.nepalidatepickerkmp.annotation.Stable
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/**
 * Categorization for [HolidayEntry]. Use this to filter or style holidays in your UI
 * (e.g. render `GovernmentPublic` in red, `Observance` in muted gray).
 *
 * Open-ended on purpose - adding more cases here is a breaking change, so the set is
 * deliberately narrow. If your taxonomy needs more granularity, store extra fields on
 * a wrapper type alongside [HolidayEntry].
 */
enum class HolidayKind {
    /** Bank / government office is closed. Sarkari bida. */
    GovernmentPublic,

    /** Religious or cultural holiday - Dashain, Tihar, Holi, Id, Christmas, etc. */
    Religious,

    /** Province- or district-level holiday, not nationally observed. */
    Regional,

    /** Observance - recognized day but offices remain open (Constitution Day in some renderings, World Health Day, etc.). */
    Observance,
}

/**
 * A single holiday in the Nepali (Bikram Sambat) calendar.
 *
 * @property date Bikram Sambat date of the holiday.
 * @property name Display name (e.g. "Dashain - Vijaya Dashami").
 * @property kind Category - see [HolidayKind].
 */
@Immutable
data class HolidayEntry(
    val date: SimpleDate,
    val name: String,
    val kind: HolidayKind,
)

/**
 * Service-provider interface for supplying holiday data to the picker.
 *
 * This library ships **no holiday data** by design - Nepali public, religious, and
 * regional holiday lists change year to year, and we don't want consumers stuck on
 * stale data baked into the library. Implement this interface with your own source
 * (a static map, a CMS, an HR API, the Patro paid catalog, …) and pass it to the
 * helpers in this package.
 *
 * Implementations must:
 *   - return a stable [Set] for a given [year] (calling twice should yield the same
 *     contents),
 *   - be safe to call from the UI thread (results are cached internally by helpers
 *     that need many lookups, but the first call per year still happens on the
 *     caller's thread),
 *   - not throw for years outside [dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults.NepaliYearRange];
 *     return [emptySet] instead.
 *
 * Example:
 * ```
 * object MyHolidays : NepaliHolidayProvider {
 *     private val staticByYear: Map<Int, Set<HolidayEntry>> = mapOf(
 *         2082 to setOf(
 *             HolidayEntry(SimpleDate(2082, 1, 1), "नयाँ वर्ष", HolidayKind.GovernmentPublic),
 *             // …
 *         )
 *     )
 *     override fun holidays(year: Int): Set<HolidayEntry> = staticByYear[year].orEmpty()
 * }
 * ```
 *
 * The reference paid implementation of this SPI is `dev.shivathapaa.patro:patro-calendar`,
 * which ships curated holiday + festival data alongside its inline calendar UI.
 */
@Stable
interface NepaliHolidayProvider {

    /** All holidays for [year], BS. Empty set is a valid answer (no holidays / out of range). */
    fun holidays(year: Int): Set<HolidayEntry>

    /**
     * Convenience - true if any entry returned by [holidays] for `date.year` falls on [date].
     *
     * Default implementation re-queries [holidays] on every call. Override with a memoized
     * implementation if you call this in tight loops; the working-day helpers in
     * [dev.shivathapaa.nepalidatepickerkmp.holiday] already memoize internally.
     */
    fun isHoliday(date: SimpleDate): Boolean =
        holidays(date.year).any { it.date == date }
}

/**
 * No-op provider. Use as a default when the consumer wants the holiday-aware APIs but
 * has not (yet) wired a real data source. Behaves as if no day is a holiday.
 */
object NoOpHolidayProvider : NepaliHolidayProvider {
    override fun holidays(year: Int): Set<HolidayEntry> = emptySet()
    override fun isHoliday(date: SimpleDate): Boolean = false
}

/**
 * Day-of-week conventions.
 *
 * The library uses a 1-based-Sunday convention everywhere - Sunday = 1, Monday = 2,
 * …, Saturday = 7. See [dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults.FIRST_DAY_OF_WEEK].
 */
object NepaliWeekend {

    /**
     * Default weekend in Nepal: Saturday only.
     *
     * Most other libraries default to two-day weekends (Sat + Sun). Nepal observes a
     * single-day weekend, so working-day arithmetic that uses [Default] matches what
     * a Nepali office actually counts as "5 working days from today".
     */
    val Default: Set<Int> = setOf(7)
}
