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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/**
 * This event on each of [days] consecutive days, starting on its own date, for a festival or a
 * leave that runs longer than one day.
 *
 * A [NepaliCalendarEvent] covers a single day, so a span is a list of entries rather than a range.
 * Every entry keeps this event's [name][NepaliCalendarEvent.name],
 * [kind][NepaliCalendarEvent.kind], [closesOffices][NepaliCalendarEvent.closesOffices],
 * [id][NepaliCalendarEvent.id] and [payload][NepaliCalendarEvent.payload], and differs only in its
 * date. A span running out of Chaitra into Baisakh yields entries in both years, so each one is
 * reported by the year a [NepaliEventProvider] is asked for.
 *
 * Give the event an [id][NepaliCalendarEvent.id] first when the days have to be recognized as one
 * thing again: `eventsIn(year, month).distinctBy { it.id }` is then a single agenda row for the
 * whole span. Nepal's published holiday lists name each day separately, so prefer real per-day
 * names where they exist and keep this for a span an app owns.
 *
 * ```
 * NepaliCalendarEvent(
 *     date = SimpleDate(2082, 6, 17),
 *     name = "Dashain",
 *     kind = NepaliEventKind.Religious,
 *     id = "dashain-2082"
 * ).spanningDays(10)
 * ```
 *
 * The event's own date is taken as stated, the way every other call here treats it, so only the
 * length of the span is checked.
 *
 * @param days how many days the span covers, counting the first. `1` returns this event alone.
 * @throws IllegalArgumentException if [days] is below 1, or if the span runs past
 *   [NepaliCalendarDefaults.NepaliYearRange].
 */
fun NepaliCalendarEvent.spanningDays(days: Int): List<NepaliCalendarEvent> {
    require(days >= 1) { "days must be at least 1, but was $days" }
    return List(days) { offset ->
        if (offset == 0) this else copy(date = date.plusDays(offset))
    }
}

/**
 * This event on each day from its own date through [end], both ends included, which is the shape
 * published holiday lists and leave requests usually state a span in.
 *
 * Expands the same way [spanningDays] does: an entry per day, carrying everything but the date
 * unchanged. An [end] equal to this event's date returns the event alone.
 *
 * ```
 * NepaliCalendarEvent(
 *     date = SimpleDate(2082, 6, 17),
 *     name = "Annual leave",
 *     kind = NepaliEventKind.Observance,
 *     id = "leave-42"
 * ).spanningThrough(SimpleDate(2082, 6, 26))
 * ```
 *
 * @throws IllegalArgumentException if [end] falls before this event's date, if [end] is not a day
 *   its month has, or if the span runs past [NepaliCalendarDefaults.NepaliYearRange]. A day the
 *   month does not reach is refused rather than rolled into the next one, since rolling would
 *   silently return a span of the wrong length.
 */
fun NepaliCalendarEvent.spanningThrough(end: SimpleDate): List<NepaliCalendarEvent> {
    require(end >= date) { "end ($end) must be on or after the event's date ($date)" }
    val daysInEndMonth = NepaliDateConverter
        .getNepaliMonthCalendar(end.year, end.month)
        .totalDaysInMonth
    require(end.dayOfMonth in 1..daysInEndMonth) {
        "end ($end) is not a day of its month, which has $daysInEndMonth days"
    }
    return spanningDays(NepaliDateConverter.getNepaliDaysInBetween(date, end) + 1)
}

/** The date [days] days on, which for a span is always a step forward. */
private fun SimpleDate.plusDays(days: Int): SimpleDate {
    val moved = NepaliDateConverter
        .getNepaliCalendarAfterAdditionOrSubtraction(year, month, dayOfMonth, days)
    return SimpleDate(moved.year, moved.month, moved.dayOfMonth)
}
