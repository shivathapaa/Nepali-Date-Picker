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

package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent

/**
 * One line of a month's event list: an event and the days it runs over.
 *
 * A [NepaliCalendarEvent] covers a single day, so a festival that runs ten days arrives as ten
 * entries. A row gathers the ones that belong together, which is what keeps a ten-day Dashain a
 * single line rather than ten.
 *
 * @property event the first entry of the run, which carries the name, kind and payload the whole row
 *   speaks for.
 * @property firstDate the first day of the run, in the month being listed.
 * @property lastDate the last day of the run, in the month being listed. Equal to [firstDate] for a
 *   single-day event, and clipped to the month's end for a span that runs past it.
 */
@Immutable
internal data class NepaliEventRow(
    val event: NepaliCalendarEvent,
    val firstDate: SimpleDate,
    val lastDate: SimpleDate
) {
    /** Whether the row covers more than one day, which is what earns it a date range. */
    val isSpan: Boolean get() = firstDate != lastDate

    /** Whether [date] falls inside the run, both ends included. */
    fun covers(date: SimpleDate): Boolean = date in firstDate..lastDate
}

/**
 * [events] as list rows, with the consecutive days of one event gathered into a single row.
 *
 * Entries are gathered when they share a non-null [NepaliCalendarEvent.id] and fall on consecutive
 * days, which is the shape [dev.shivathapaa.nepalidatepickerkmp.event.spanningDays] produces. An
 * event without an id stays one row per day, since nothing says those days are the same thing, and a
 * repeated id that skips a day starts a new row rather than swallowing the gap.
 *
 * Takes the order it is given, which from
 * [dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy.eventsIn] is by day and, within a
 * day, strongest kind first.
 */
internal fun collapseEventRows(events: List<NepaliCalendarEvent>): List<NepaliEventRow> {
    val rows = mutableListOf<NepaliEventRow>()
    val openRowsById = mutableMapOf<String, Int>()

    events.forEach { event ->
        val id = event.id
        val openIndex = id?.let { openRowsById[it] }
        val openRow = openIndex?.let { rows[it] }
        if (openRow != null && openRow.lastDate.isDayBefore(event.date)) {
            rows[openIndex] = openRow.copy(lastDate = event.date)
            return@forEach
        }
        rows += NepaliEventRow(event = event, firstDate = event.date, lastDate = event.date)
        if (id != null) openRowsById[id] = rows.lastIndex
    }
    return rows
}

/**
 * Whether [next] is the day straight after this one.
 *
 * Compares within a month by day number and across a month boundary by the calendar's own
 * arithmetic, so a span running from the end of one month into the next stays one run.
 */
private fun SimpleDate.isDayBefore(next: SimpleDate): Boolean {
    if (year == next.year && month == next.month) return next.dayOfMonth == dayOfMonth + 1
    val daysInThisMonth = runCatching {
        NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)
    }.getOrNull() ?: return false
    if (dayOfMonth != daysInThisMonth || next.dayOfMonth != 1) return false
    return if (month == MonthsInYear) {
        next.year == year + 1 && next.month == 1
    } else {
        next.year == year && next.month == month + 1
    }
}
