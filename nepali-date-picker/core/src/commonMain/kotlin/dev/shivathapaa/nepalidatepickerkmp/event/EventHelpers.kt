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

import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

// NepaliSelectableDates wrappers

/**
 * Returns a [NepaliSelectableDates] that delegates to this one but **additionally** rejects any
 * date [provider] says the institution is shut for.
 *
 * An event that does not close, a programme or a meeting, leaves its day selectable: the day is
 * still worked, and refusing it would surprise anyone booking on it.
 *
 * Year-level rejection still defers to the wrapped predicate, since event data is per-date.
 */
fun NepaliSelectableDates.excludingClosures(
    provider: NepaliEventProvider,
): NepaliSelectableDates {
    val wrapped = this
    return object : NepaliSelectableDates {
        override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
            wrapped.isSelectableDate(customCalendar) &&
                !provider.closesOn(customCalendar.toSimpleDateInternal())

        override fun isSelectableYear(year: Int): Boolean =
            wrapped.isSelectableYear(year)
    }
}

/**
 * Returns a [NepaliSelectableDates] that delegates to this one but **additionally** rejects weekend
 * days as defined by [weekend] (default: Saturday only).
 *
 * The library uses 1-based-Sunday day-of-week numbering throughout: Sunday = 1, …, Saturday = 7.
 * Pass `setOf(6, 7)` for a Friday-and-Saturday weekend, or `setOf(1, 7)` for Sunday-and-Saturday.
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

// Provider composition

/**
 * A provider reporting everything either side reports, which is how a national list and an
 * institution's own list are put together.
 *
 * A date named by both sides keeps both entries, so a day can be a public holiday nationally and
 * something else locally without one hiding the other. A day either side calls a closure stays a
 * closure, including when that side answers [NepaliEventProvider.closesOn] directly rather than by
 * listing an event.
 */
operator fun NepaliEventProvider.plus(other: NepaliEventProvider): NepaliEventProvider {
    val first = this
    return object : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> =
            first.events(year) + other.events(year)

        override fun closesOn(date: SimpleDate): Boolean =
            first.closesOn(date) || other.closesOn(date)
    }
}

/**
 * A view of this provider carrying only the entries [predicate] accepts, for narrowing a shared
 * list to what one screen cares about: `filtered { it.kind == NepaliEventKind.GovernmentPublic }`.
 *
 * [predicate] runs on every entry of a year each time that year is asked for, so keep it a test
 * rather than a lookup.
 *
 * A day stays closed while a closure behind it survives the filter, so narrowing a list narrows the
 * closures with it. A day the source shuts without naming a closing event stays shut, since there
 * is nothing to narrow. `filtered { true }` therefore answers exactly what the source does.
 */
fun NepaliEventProvider.filtered(
    predicate: (NepaliCalendarEvent) -> Boolean
): NepaliEventProvider {
    val source = this
    return object : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> =
            source.events(year).filterTo(mutableSetOf(), predicate)

        override fun closesOn(date: SimpleDate): Boolean {
            if (!source.closesOn(date)) return false
            val closures = source.events(date.year).filter { it.date == date && it.closesOffices }
            return closures.isEmpty() || closures.any(predicate)
        }
    }
}

// Working-day arithmetic

/**
 * Number of working days in the half-open range `[start, end)`, skipping both [weekend] days and
 * the dates [provider] says the institution is shut for.
 *
 * Mirrors the existing `getNepaliDaysInBetween` convention: [end] is **exclusive**. To make it
 * inclusive, add 1 to the result if [end] itself is a working day. A day that is both a weekend and
 * a closure is skipped once.
 *
 * Requires `start <= end`. Returns 0 when `start == end`.
 *
 * @throws IllegalArgumentException if `start > end`.
 */
fun NepaliDateConverter.workingDaysBetween(
    start: SimpleDate,
    end: SimpleDate,
    provider: NepaliEventProvider,
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
        if (provider.closesOn(SimpleDate(cal.year, cal.month, cal.dayOfMonth))) continue
        count++
    }
    return count
}

/**
 * First working day at or after [from], skipping both [weekend] days and the dates [provider] says
 * the institution is shut for.
 *
 * If [from] is itself a working day, returns [from] unchanged. Otherwise scans forward day by day.
 * Bounded scan: gives up after a year rather than looping forever on a policy that closes every day.
 *
 * @throws IllegalStateException if no working day is found within 366 days of [from].
 */
fun NepaliDateConverter.nextWorkingDay(
    from: SimpleDate,
    provider: NepaliEventProvider,
    weekend: Set<Int> = NepaliWeekend.Default,
): SimpleDate {
    var offset = 0
    while (offset <= 366) {
        val cal = getNepaliCalendarAfterAdditionOrSubtraction(
            from.year, from.month, from.dayOfMonth, offset
        )
        val simple = SimpleDate(cal.year, cal.month, cal.dayOfMonth)
        if (cal.dayOfWeek !in weekend && !provider.closesOn(simple)) return simple
        offset++
    }
    error("nextWorkingDay: no working day found within 366 days of $from - check your NepaliEventProvider and weekend set")
}

/**
 * Returns the date that is [days] working days from [from], skipping [weekend] and the closures
 * [provider] reports. Follows Excel `WORKDAY` semantics:
 *
 *   - `days == 0` returns [from] unchanged.
 *   - `days > 0` returns the [days]-th working day *strictly after* [from].
 *   - `days < 0` returns the |[days]|-th working day *strictly before* [from].
 *
 * Note that this means `addWorkingDays(from, 0)` is **not** the same as `nextWorkingDay(from)`: use
 * the latter explicitly if you want adjustment.
 *
 * @throws IllegalStateException if more than ~2 years of scanning fails to find the requested day.
 */
fun NepaliDateConverter.addWorkingDays(
    from: SimpleDate,
    days: Int,
    provider: NepaliEventProvider,
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
        if (provider.closesOn(SimpleDate(cal.year, cal.month, cal.dayOfMonth))) continue
        remaining--
    }
    // Re-compute once at the final offset (cheaper than carrying CustomCalendar through the loop).
    val final = getNepaliCalendarAfterAdditionOrSubtraction(
        from.year, from.month, from.dayOfMonth, offset
    )
    return SimpleDate(final.year, final.month, final.dayOfMonth)
}

// Working-day arithmetic under a policy

/**
 * Number of working days in the half-open range `[start, end)` for the institution [policy]
 * describes, skipping both its weekly off days and the events that close it.
 *
 * Same contract as the [provider][NepaliEventProvider] overload: [end] is exclusive, `start <= end`
 * is required, and a day that is both a weekly off day and a closure is skipped once.
 */
fun NepaliDateConverter.workingDaysBetween(
    start: SimpleDate,
    end: SimpleDate,
    policy: NepaliCalendarPolicy
): Int = workingDaysBetween(start, end, policy.provider, policy.weeklyOffDays)

/**
 * First working day at or after [from] for the institution [policy] describes. Returns [from]
 * itself when that is already a working day.
 */
fun NepaliDateConverter.nextWorkingDay(
    from: SimpleDate,
    policy: NepaliCalendarPolicy
): SimpleDate = nextWorkingDay(from, policy.provider, policy.weeklyOffDays)

/**
 * The date [days] working days from [from] for the institution [policy] describes, with the Excel
 * `WORKDAY` semantics of the [provider][NepaliEventProvider] overload.
 */
fun NepaliDateConverter.addWorkingDays(
    from: SimpleDate,
    days: Int,
    policy: NepaliCalendarPolicy
): SimpleDate = addWorkingDays(from, days, policy.provider, policy.weeklyOffDays)

// internals

/**
 * Helpers call [NepaliEventProvider.closesOn] directly per date rather than caching
 * `provider.events(year)` themselves. Reasons:
 *   - Providers can override either method (or both), and caching the year set silently bypasses
 *     one that only implements `closesOn`, which is a contract-supported shape.
 *   - Real implementations almost always memoize their own data (static map, in-memory CMS cache).
 *     A redundant cache layer here adds little.
 *
 * If a profile shows `provider.closesOn` is the bottleneck for a particular workload, the provider
 * should memoize: the contract documents this expectation.
 */

/** Local helper to avoid a public extension just for the wrapper's needs. */
private fun CustomCalendar.toSimpleDateInternal(): SimpleDate =
    SimpleDate(year = year, month = month, dayOfMonth = dayOfMonth)
