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
import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/**
 * When one institution is closed, and what its calendar has on it: the days of the week it never
 * opens, plus the events it keeps.
 *
 * The two travel together because they answer the same question, and an app that states them once
 * gets both a calendar that paints the right days and arithmetic that counts the right ones. An
 * office in Nepal is `NepaliCalendarPolicy(provider = myEvents)`, closed Saturdays. A school closed
 * Saturday and Sunday is `NepaliCalendarPolicy(setOf(7, 1), myEvents)`. Two institutions with
 * different lists are two policies over two providers, and a national list shared by both is
 * `national + ownList` (see [plus]).
 *
 * Nothing here blocks a date on its own. [asSelectableDates] turns the policy into a picker rule
 * when that is what you want, so marking a day and refusing it stay separate decisions.
 *
 * @property weeklyOffDays days of the week the institution is closed, 1 for Sunday through 7 for
 *   Saturday. Defaults to [NepaliWeekend.Default], the single Saturday weekend Nepal observes. An
 *   empty set is an institution that never closes for the week alone.
 * @property provider the events the institution keeps. Defaults to [NoOpEventProvider], which is
 *   the right value while an app has only its weekly rule wired.
 *
 * @throws IllegalArgumentException if [weeklyOffDays] holds a number that is not a day of the week.
 *   Other calendars number Sunday `0`, and a set written to that convention would quietly close
 *   nothing, so it is rejected at construction rather than at the end of a month of wrong answers.
 */
@Immutable
class NepaliCalendarPolicy(
    val weeklyOffDays: Set<Int> = NepaliWeekend.Default,
    val provider: NepaliEventProvider = NoOpEventProvider
) {
    init {
        val strays = weeklyOffDays.filterNot { it in 1..7 }
        require(strays.isEmpty()) {
            "weeklyOffDays must hold days of the week, 1 for Sunday through 7 for Saturday, " +
                    "but got $strays"
        }
    }

    /**
     * Whether [dayOfWeek] (1 for Sunday through 7 for Saturday) is one of the institution's weekly
     * off days. Takes the day of the week rather than a date because a calendar grid already knows
     * it, and resolving it again would cost a conversion per cell.
     */
    fun isWeeklyOff(dayOfWeek: Int): Boolean = dayOfWeek in weeklyOffDays

    /**
     * The events on [date], strongest [NepaliEventKind] first, or an empty list when the day
     * carries none. A weekly off day with nothing named on it answers empty.
     */
    fun eventsOn(date: SimpleDate): List<NepaliCalendarEvent> =
        provider.events(date.year)
            .filter { it.date == date }
            .sortedBy { it.kind.priority }

    /**
     * Every event in the Bikram Sambat month [month] of [year], in date order and, within a date,
     * strongest kind first. Empty for a month with none, and for a year the provider does not cover.
     */
    fun eventsIn(year: Int, month: Int): List<NepaliCalendarEvent> =
        provider.events(year)
            .filter { it.date.month == month }
            .sortedWith(compareBy({ it.date.dayOfMonth }, { it.kind.priority }))

    /**
     * What [date] is under this policy: whether the week makes it a day off, and what is named on
     * it. Resolves the day of the week through the conversion table, so prefer [monthStatus] when
     * laying out a whole month.
     */
    fun statusOf(date: SimpleDate): NepaliDayStatus {
        val dayOfWeek = NepaliDateConverter
            .getNepaliCalendar(date.year, date.month, date.dayOfMonth)
            .dayOfWeek
        return NepaliDayStatus(isWeeklyOff = isWeeklyOff(dayOfWeek), events = eventsOn(date))
    }

    /**
     * Every day of one Bikram Sambat month, in day order: index `0` is day 1.
     *
     * The month's first weekday is resolved once and the week walked forward from it, so a grid or
     * an agenda costs one conversion rather than one per day.
     */
    fun monthStatus(year: Int, month: Int): List<NepaliDayStatus> {
        val monthCalendar = NepaliDateConverter.getNepaliMonthCalendar(year, month)
        val eventsByDay = eventsIn(year, month).groupBy { it.date.dayOfMonth }
        return (1..monthCalendar.totalDaysInMonth).map { dayOfMonth ->
            val dayOfWeek = ((monthCalendar.firstDayOfMonth - 1 + dayOfMonth - 1) % 7) + 1
            NepaliDayStatus(
                isWeeklyOff = isWeeklyOff(dayOfWeek),
                events = eventsByDay[dayOfMonth].orEmpty()
            )
        }
    }

    /**
     * Whether the institution is closed on [date], for either reason. A day that is both a weekly
     * off day and a holiday is closed once, and a day carrying only an observance is still worked.
     *
     * Asks [provider] directly rather than going through [statusOf], so a provider that answers
     * [NepaliEventProvider.closesOn] without listing the event behind it is still heard, and the
     * answer agrees with what the working-day helpers count.
     */
    fun isNonWorkingDay(date: SimpleDate): Boolean {
        val dayOfWeek = NepaliDateConverter
            .getNepaliCalendar(date.year, date.month, date.dayOfMonth)
            .dayOfWeek
        return isWeeklyOff(dayOfWeek) || provider.closesOn(date)
    }

    /**
     * The policy as a picker rule: every closed day becomes unselectable. Opt in to this when a
     * screen should refuse the days it marks, and leave it out when the days are only to be seen.
     *
     * An event that does not close the institution, a school programme or a meeting, leaves its day
     * selectable, since the day is still worked.
     */
    fun asSelectableDates(): NepaliSelectableDates =
        AllSelectableDates.excludingWeekends(weeklyOffDays).excludingClosures(provider)

    /**
     * Two policies are equal when they describe the same institution: the same weekly off days over
     * a [provider] equal to the other's. A provider that compares by value therefore keeps a
     * rebuilt policy equal to the last one, which is what lets a Compose decorator be remembered
     * across recompositions instead of rebuilt.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NepaliCalendarPolicy) return false
        return weeklyOffDays == other.weeklyOffDays && provider == other.provider
    }

    override fun hashCode(): Int = 31 * weeklyOffDays.hashCode() + provider.hashCode()

    companion object {
        /** Nepal's usual office week: Saturday off, no event data wired yet. */
        val Default: NepaliCalendarPolicy = NepaliCalendarPolicy()
    }
}

/** The unconstrained rule the policy narrows, kept private so it cannot be mistaken for a default. */
private val AllSelectableDates: NepaliSelectableDates = object : NepaliSelectableDates {}
