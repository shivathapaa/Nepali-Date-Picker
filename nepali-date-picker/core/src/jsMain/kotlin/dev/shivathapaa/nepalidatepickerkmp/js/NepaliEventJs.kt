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

@file:OptIn(ExperimentalJsExport::class)

package dev.shivathapaa.nepalidatepickerkmp.js

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween

/**
 * JavaScript / TypeScript facade over the holiday model, alongside the conversion facade in
 * `NepaliDateConverterJs.kt` and written the same way: flat exported classes, lowercase enum
 * strings, and no default parameters.
 *
 * The library ships no holiday data. A consumer passes its own list to [createCalendarPolicy] and
 * gets back an object that answers what a day is and how many working days lie between two of them.
 */

/**
 * One thing on one day: a public holiday, a festival, a programme, a meeting.
 *
 * @property kind `governmentPublic`, `religious`, `regional`, or `observance`; see [createEvent].
 * @property closesOffices whether the institution is shut for this, which is what the working-day
 *   arithmetic counts by. A school programme is named without closing anything.
 * @property id an identifier carried back untouched, for correlating with the app's own record.
 * @property payload anything else the app wants back, as an opaque string. Never parsed here.
 */
@JsExport
class NepaliEvent internal constructor(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val name: String,
    val kind: String,
    val closesOffices: Boolean,
    val id: String?,
    val payload: String?
)

/**
 * What one day is under a policy.
 *
 * @property isWeeklyOff the week closes the day, whatever else is named on it.
 * @property isNonWorking the day is closed for either reason. A day that is both a weekly off day
 *   and a holiday is closed once, not twice.
 * @property primaryKind the kind describing the day best, or `null` when nothing is named on it.
 * @property names the names of the day's events, strongest kind first.
 * @property events the same events in full, in the same order.
 * @property closures only the events that actually shut the institution.
 */
@JsExport
class NepaliDayStatusInfo internal constructor(
    val isWeeklyOff: Boolean,
    val isNonWorking: Boolean,
    val primaryKind: String?,
    val names: Array<String>,
    val events: Array<NepaliEvent>,
    val closures: Array<NepaliEvent>
)

/**
 * One institution's closed days: the weekdays it never opens plus the holidays it keeps.
 *
 * Build one with [createCalendarPolicy]. Every method that resolves a day of the week goes through
 * the conversion table, so a date outside the supported Bikram Sambat range throws, exactly as the
 * conversion functions do.
 */
@JsExport
class NepaliCalendarPolicyInfo internal constructor(
    private val policy: NepaliCalendarPolicy
) {
    /** The weekdays this policy closes, 1 for Sunday through 7 for Saturday. */
    val weeklyOffDays: Array<Int> = policy.weeklyOffDays.sorted().toTypedArray()

    /** Whether [dayOfWeek] (1 = Sunday) is one of the weekly off days. */
    fun isWeeklyOff(dayOfWeek: Int): Boolean = policy.isWeeklyOff(dayOfWeek)

    /** What the given Bikram Sambat date is: closed by the week, by a holiday, by both, or neither. */
    fun statusOf(year: Int, month: Int, dayOfMonth: Int): NepaliDayStatusInfo =
        policy.statusOf(SimpleDate(year, month, dayOfMonth)).let { status ->
            NepaliDayStatusInfo(
                isWeeklyOff = status.isWeeklyOff,
                isNonWorking = status.isNonWorking,
                primaryKind = status.primaryKind?.let { kindName(it) },
                names = status.names.toTypedArray(),
                events = status.events.map { it.toJs() }.toTypedArray(),
                closures = status.closures.map { it.toJs() }.toTypedArray()
            )
        }

    /** The holidays on one Bikram Sambat date, strongest kind first, empty when there are none. */
    fun eventsOn(year: Int, month: Int, dayOfMonth: Int): Array<NepaliEvent> =
        policy.eventsOn(SimpleDate(year, month, dayOfMonth)).map { it.toJs() }.toTypedArray()

    /** Every holiday in one Bikram Sambat month, in date order, for a wall-calendar list. */
    fun eventsIn(year: Int, month: Int): Array<NepaliEvent> =
        policy.eventsIn(year, month).map { it.toJs() }.toTypedArray()

    /**
     * Every day of one Bikram Sambat month, in day order: index `0` is day 1.
     *
     * The month's first weekday is resolved once and the week walked forward from it, so a grid or
     * an agenda costs one conversion rather than one per day.
     */
    fun monthStatus(year: Int, month: Int): Array<NepaliDayStatusInfo> =
        policy.monthStatus(year, month).map { it.toJs() }.toTypedArray()

    /** Whether the institution is closed on the given date, for either reason. */
    fun isNonWorkingDay(year: Int, month: Int, dayOfMonth: Int): Boolean =
        policy.isNonWorkingDay(SimpleDate(year, month, dayOfMonth))

    /** Working days in the half-open range `[start, end)`. Add 1 to include the end date. */
    fun workingDaysBetween(
        startYear: Int, startMonth: Int, startDay: Int,
        endYear: Int, endMonth: Int, endDay: Int
    ): Int = NepaliDateConverter.workingDaysBetween(
        SimpleDate(startYear, startMonth, startDay),
        SimpleDate(endYear, endMonth, endDay),
        policy
    )

    /** The first working day at or after the given date, which may be that date itself. */
    fun nextWorkingDay(year: Int, month: Int, dayOfMonth: Int): NepaliDate =
        NepaliDateConverter.nextWorkingDay(SimpleDate(year, month, dayOfMonth), policy)
            .toCalendar()

    /**
     * The date [days] working days away, with Excel `WORKDAY` semantics: `0` stays put, a positive
     * count lands strictly after, a negative one strictly before.
     */
    fun addWorkingDays(year: Int, month: Int, dayOfMonth: Int, days: Int): NepaliDate =
        NepaliDateConverter.addWorkingDays(SimpleDate(year, month, dayOfMonth), days, policy)
            .toCalendar()
}

/**
 * One holiday for [createCalendarPolicy].
 *
 * @param kind `governmentPublic` (offices close), `religious` (a festival), `regional` (kept in one
 *   province or district), or `observance` (recognized, but offices stay open). Also accepts
 *   `government-public` and `public`. Anything unrecognized is read as `observance`, the kind that
 *   claims the least.
 */
@JsExport
fun createEvent(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    name: String,
    kind: String
): NepaliEvent = eventKind(kind).let { resolved ->
    NepaliEvent(
        year, month, dayOfMonth, name, kindName(resolved),
        closesOffices = resolved.closesOfficesByDefault,
        id = null,
        payload = null
    )
}

/**
 * The same as [createEvent], with everything the shorter form leaves to its kind spelled out.
 *
 * @param closesOffices whether the institution is shut for this, whatever its kind usually means.
 *   A regional holiday closes one district and not the next; a programme closes nothing.
 * @param id an identifier handed back untouched, or `null`.
 * @param payload anything else the app wants back, as an opaque string, or `null`. Never parsed.
 */
@JsExport
fun createDetailedEvent(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    name: String,
    kind: String,
    closesOffices: Boolean,
    id: String?,
    payload: String?
): NepaliEvent = NepaliEvent(
    year, month, dayOfMonth, name, kindName(eventKind(kind)), closesOffices, id, payload
)

/**
 * A policy over [weeklyOffDays] (1 for Sunday through 7 for Saturday) and [events].
 *
 * Nepal's office week is `[7]`; a school closed Saturday and Sunday is `[7, 1]`; `[]` is an
 * institution that never closes for the week alone. A number outside 1..7 throws, since a set
 * written to JavaScript's own 0-based weekday convention would otherwise close nothing at all.
 */
@JsExport
fun createCalendarPolicy(
    weeklyOffDays: Array<Int>,
    events: Array<NepaliEvent>
): NepaliCalendarPolicyInfo {
    val byYear = events.map { it.toCore() }
        .groupBy { it.date.year }
        .mapValues { (_, list) -> list.toSet() }
    val provider = object : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
    }
    return NepaliCalendarPolicyInfo(
        NepaliCalendarPolicy(weeklyOffDays = weeklyOffDays.toSet(), provider = provider)
    )
}

internal fun NepaliEvent.toCore(): NepaliCalendarEvent = NepaliCalendarEvent(
    date = SimpleDate(year, month, dayOfMonth),
    name = name,
    kind = eventKind(kind),
    closesOffices = closesOffices,
    id = id,
    payload = payload
)

internal fun NepaliCalendarEvent.toJs(): NepaliEvent = NepaliEvent(
    year = date.year,
    month = date.month,
    dayOfMonth = date.dayOfMonth,
    name = name,
    kind = kindName(kind),
    closesOffices = closesOffices,
    id = id,
    payload = payload
)

private fun NepaliDayStatus.toJs(): NepaliDayStatusInfo = NepaliDayStatusInfo(
    isWeeklyOff = isWeeklyOff,
    isNonWorking = isNonWorking,
    primaryKind = primaryKind?.let { kindName(it) },
    names = names.toTypedArray(),
    events = events.map { it.toJs() }.toTypedArray(),
    closures = closures.map { it.toJs() }.toTypedArray()
)

private fun SimpleDate.toCalendar(): NepaliDate =
    getBsCalendar(year, month, dayOfMonth)

internal fun kindName(kind: NepaliEventKind): String = when (kind) {
    NepaliEventKind.GovernmentPublic -> "governmentPublic"
    NepaliEventKind.Religious -> "religious"
    NepaliEventKind.Regional -> "regional"
    NepaliEventKind.Observance -> "observance"
}

internal fun eventKind(value: String): NepaliEventKind = when (value.lowercase()) {
    "governmentpublic", "government-public", "public" -> NepaliEventKind.GovernmentPublic
    "religious", "festival" -> NepaliEventKind.Religious
    "regional", "local" -> NepaliEventKind.Regional
    else -> NepaliEventKind.Observance
}
