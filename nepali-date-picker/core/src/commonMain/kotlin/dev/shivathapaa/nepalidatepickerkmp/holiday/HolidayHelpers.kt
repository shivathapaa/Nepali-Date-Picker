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

import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

// ── NepaliSelectableDates wrappers ─────────────────────────────────────────

/**
 * Returns a [NepaliSelectableDates] that delegates to this one but **additionally**
 * rejects any date marked as a holiday by [provider].
 *
 * Year-level rejection in this wrapper still defers to the wrapped predicate — holiday
 * data is per-date, not per-year. To also gray out entire holiday-heavy years, compose
 * a custom predicate.
 */
fun NepaliSelectableDates.excludingHolidays(
    provider: NepaliHolidayProvider,
): NepaliSelectableDates {
    val wrapped = this
    return object : NepaliSelectableDates {
        override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
            wrapped.isSelectableDate(customCalendar) &&
                !provider.isHoliday(customCalendar.toSimpleDateInternal())

        override fun isSelectableYear(year: Int): Boolean =
            wrapped.isSelectableYear(year)
    }
}

/**
 * Returns a [NepaliSelectableDates] that delegates to this one but **additionally**
 * rejects weekend days as defined by [weekend] (default: Saturday only).
 *
 * The library uses 1-based-Sunday day-of-week numbering throughout — Sunday = 1,
 * …, Saturday = 7. Pass `setOf(6, 7)` for a Friday-and-Saturday weekend, or
 * `setOf(1, 7)` for Sunday-and-Saturday.
 */
fun NepaliSelectableDates.excludingWeekends(
    weekend: Set<Int> = NepaliWeekend.Default,
): NepaliSelectableDates {
    val wrapped = this
    return object : NepaliSelectableDates {
        override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
            wrapped.isSelectableDate(customCalendar) &&
                customCalendar.dayOfWeek !in weekend

        override fun isSelectableYear(year: Int): Boolean =
            wrapped.isSelectableYear(year)
    }
}

// ── Working-day arithmetic ─────────────────────────────────────────────────

/**
 * Number of working days in the half-open range `[start, end)`, skipping both
 * [weekend] days and dates flagged by [provider].
 *
 * Mirrors the existing `getNepaliDaysInBetween` convention — [end] is **exclusive**.
 * To make it inclusive, add 1 to the result if [end] itself is a working day.
 *
 * Requires `start <= end`. Returns 0 when `start == end`.
 *
 * @throws IllegalArgumentException if `start > end`.
 */
fun NepaliDateConverter.workingDaysBetween(
    start: SimpleDate,
    end: SimpleDate,
    provider: NepaliHolidayProvider,
    weekend: Set<Int> = NepaliWeekend.Default,
): Int {
    require(start <= end) { "start ($start) must be <= end ($end)" }
    val span = getNepaliDaysInBetween(start, end)
    if (span <= 0) return 0

    var count = 0
    for (offset in 0 until span) {
        val cal = getNepaliCalendarAfterAdditionOrSubtraction(
            start.year, start.month, start.dayOfMonth, offset
        )
        if (cal.dayOfWeek in weekend) continue
        if (provider.isHoliday(SimpleDate(cal.year, cal.month, cal.dayOfMonth))) continue
        count++
    }
    return count
}

/**
 * First working day at or after [from], skipping both [weekend] days and dates
 * flagged by [provider].
 *
 * If [from] is itself a working day, returns [from] unchanged. Otherwise scans
 * forward day by day. Bounded scan — gives up after a year to avoid pathological
 * provider implementations that mark every day as a holiday.
 *
 * @throws IllegalStateException if no working day is found within 366 days of [from].
 */
fun NepaliDateConverter.nextWorkingDay(
    from: SimpleDate,
    provider: NepaliHolidayProvider,
    weekend: Set<Int> = NepaliWeekend.Default,
): SimpleDate {
    var offset = 0
    while (offset <= 366) {
        val cal = getNepaliCalendarAfterAdditionOrSubtraction(
            from.year, from.month, from.dayOfMonth, offset
        )
        val simple = SimpleDate(cal.year, cal.month, cal.dayOfMonth)
        if (cal.dayOfWeek !in weekend && !provider.isHoliday(simple)) return simple
        offset++
    }
    error("nextWorkingDay: no working day found within 366 days of $from — check your NepaliHolidayProvider and weekend set")
}

/**
 * Returns the date that is [days] working days from [from], skipping [weekend] and
 * holidays from [provider]. Follows Excel `WORKDAY` semantics:
 *
 *   - `days == 0` returns [from] unchanged.
 *   - `days > 0` returns the [days]-th working day *strictly after* [from].
 *   - `days < 0` returns the |[days]|-th working day *strictly before* [from].
 *
 * Note that this means `addWorkingDays(from, 0)` is **not** the same as
 * `nextWorkingDay(from)` — use the latter explicitly if you want adjustment.
 *
 * @throws IllegalStateException if more than ~2 years of scanning fails to find the
 *   requested day (defends against pathological providers).
 */
fun NepaliDateConverter.addWorkingDays(
    from: SimpleDate,
    days: Int,
    provider: NepaliHolidayProvider,
    weekend: Set<Int> = NepaliWeekend.Default,
): SimpleDate {
    if (days == 0) return from
    val step = if (days > 0) 1 else -1
    var remaining = if (days > 0) days else -days
    var offset = 0
    val maxScan = 732 // ~2 years of slack

    while (remaining > 0) {
        offset += step
        if (offset > maxScan || offset < -maxScan) {
            error("addWorkingDays: exceeded $maxScan-day scan from $from when looking for $days working days")
        }
        val cal = getNepaliCalendarAfterAdditionOrSubtraction(
            from.year, from.month, from.dayOfMonth, offset
        )
        if (cal.dayOfWeek in weekend) continue
        if (provider.isHoliday(SimpleDate(cal.year, cal.month, cal.dayOfMonth))) continue
        remaining--
    }
    // Re-compute once at the final offset (cheaper than carrying CustomCalendar through the loop).
    val final = getNepaliCalendarAfterAdditionOrSubtraction(
        from.year, from.month, from.dayOfMonth, offset
    )
    return SimpleDate(final.year, final.month, final.dayOfMonth)
}

// ── internals ──────────────────────────────────────────────────────────────

/**
 * Helpers call [NepaliHolidayProvider.isHoliday] directly per date rather than
 * caching `provider.holidays(year)` themselves. Reasons:
 *   - Providers can override either method (or both) — caching the year set silently
 *     bypasses providers that only implement `isHoliday`, which is a contract-supported
 *     shape.
 *   - Real implementations almost always memoize their own data (static map, in-memory
 *     CMS cache). A redundant cache layer here adds little.
 *
 * If a profile shows `provider.isHoliday` is the bottleneck for a particular workload,
 * the provider should memoize — the contract documents this expectation.
 */

/** Local helper to avoid a public extension just for the wrapper's needs. */
private fun CustomCalendar.toSimpleDateInternal(): SimpleDate =
    SimpleDate(year = year, month = month, dayOfMonth = dayOfMonth)
