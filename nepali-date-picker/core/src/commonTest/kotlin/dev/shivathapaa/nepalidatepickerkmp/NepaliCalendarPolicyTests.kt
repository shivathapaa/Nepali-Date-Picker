/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.excludingWeekends
import dev.shivathapaa.nepalidatepickerkmp.event.filtered
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.plus
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * How a week of closures and a list of holidays resolve into one answer per day, which is what a
 * calendar paints and what the working-day helpers count.
 */
class NepaliCalendarPolicyTests {

    private val sunday = 1
    private val saturday = 7

    private class ListProvider(private val entries: List<NepaliCalendarEvent>) : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> =
            entries.filterTo(mutableSetOf()) { it.date.year == year }
    }

    private fun entry(date: SimpleDate, name: String, kind: NepaliEventKind) =
        NepaliCalendarEvent(date, name, kind)

    /** The date [days] after [from], for spans that must not assume a month length. */
    private fun offsetDay(from: SimpleDate, days: Int): SimpleDate =
        NepaliDateConverter
            .getNepaliCalendarAfterAdditionOrSubtraction(from.year, from.month, from.dayOfMonth, days)
            .let { SimpleDate(it.year, it.month, it.dayOfMonth) }

    /** The first date on or after 2082/01/01 whose day of the week is [dayOfWeek]. */
    private fun firstDayOfWeek(dayOfWeek: Int): SimpleDate {
        for (offset in 0..7) {
            val cal = NepaliDateConverter
                .getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == dayOfWeek) return SimpleDate(cal.year, cal.month, cal.dayOfMonth)
        }
        error("no day of week $dayOfWeek within a week of 2082/01/01")
    }

    // Weekly rules

    @Test
    fun defaultPolicy_closesSaturdayOnly() {
        val policy = NepaliCalendarPolicy.Default

        assertEquals(NepaliWeekend.Default, policy.weeklyOffDays)
        assertTrue(policy.isWeeklyOff(saturday))
        assertFalse(policy.isWeeklyOff(sunday))
    }

    @Test
    fun schoolPolicy_closesBothWeekendDays() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = setOf(saturday, sunday))

        assertTrue(policy.isWeeklyOff(saturday))
        assertTrue(policy.isWeeklyOff(sunday))
        assertFalse(policy.isWeeklyOff(2))
    }

    // statusOf

    @Test
    fun plainWorkingDay_hasNoStatusAtAll() {
        val policy = NepaliCalendarPolicy.Default
        val status = policy.statusOf(firstDayOfWeek(2))

        assertFalse(status.isWeeklyOff)
        assertTrue(status.events.isEmpty())
        assertFalse(status.isNonWorking)
        assertNull(status.primaryKind)
    }

    @Test
    fun weeklyOffDay_isNonWorkingButUnnamed() {
        val policy = NepaliCalendarPolicy.Default
        val status = policy.statusOf(firstDayOfWeek(saturday))

        assertTrue(status.isWeeklyOff)
        assertTrue(status.isNonWorking)
        assertTrue(status.events.isEmpty())
        assertNull(status.primaryKind, "a Saturday is not a named holiday")
        assertTrue(status.names.isEmpty())
    }

    @Test
    fun namedHolidayOnAWorkingDay_carriesItsKindAndName() {
        val workday = firstDayOfWeek(2)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(listOf(entry(workday, "Dashain", NepaliEventKind.Religious)))
        )
        val status = policy.statusOf(workday)

        assertFalse(status.isWeeklyOff)
        assertTrue(status.isNonWorking)
        assertEquals(NepaliEventKind.Religious, status.primaryKind)
        assertEquals(listOf("Dashain"), status.names)
    }

    @Test
    fun severalHolidaysOnOneDay_reportStrongestFirst() {
        val workday = firstDayOfWeek(2)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    entry(workday, "World Health Day", NepaliEventKind.Observance),
                    entry(workday, "Constitution Day", NepaliEventKind.GovernmentPublic),
                    entry(workday, "Local jatra", NepaliEventKind.Regional)
                )
            )
        )
        val status = policy.statusOf(workday)

        assertEquals(3, status.events.size)
        assertEquals(NepaliEventKind.GovernmentPublic, status.primaryKind)
        assertEquals(
            listOf("Constitution Day", "Local jatra", "World Health Day"),
            status.names,
            "a closure is named before a regional day, and an observance last"
        )
    }

    @Test
    fun holidayOnAWeeklyOffDay_reportsBothFacts() {
        val sat = firstDayOfWeek(saturday)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(listOf(entry(sat, "Dashain", NepaliEventKind.Religious)))
        )
        val status = policy.statusOf(sat)

        assertTrue(status.isWeeklyOff)
        assertEquals(NepaliEventKind.Religious, status.primaryKind)
        assertTrue(status.isNonWorking, "still one day off, not two")
    }

    @Test
    fun holidaysIn_listsAMonthInDateOrder() {
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    entry(SimpleDate(2082, 1, 15), "Later", NepaliEventKind.Religious),
                    entry(SimpleDate(2082, 1, 1), "New Year", NepaliEventKind.GovernmentPublic),
                    entry(SimpleDate(2082, 2, 15), "Other month", NepaliEventKind.GovernmentPublic)
                )
            )
        )

        assertEquals(listOf("New Year", "Later"), policy.eventsIn(2082, 1).map { it.name })
        assertEquals(listOf("Other month"), policy.eventsIn(2082, 2).map { it.name })
        assertTrue(policy.eventsIn(2082, 3).isEmpty())
    }

    // Blocking is opt-in

    @Test
    fun asSelectableDates_blocksExactlyTheClosedDays() {
        val sat = firstDayOfWeek(saturday)
        val workdayHoliday = firstDayOfWeek(2)
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = setOf(saturday),
            provider = ListProvider(listOf(entry(workdayHoliday, "Dashain", NepaliEventKind.Religious)))
        )
        val rule = policy.asSelectableDates()

        fun calendarOf(date: SimpleDate) =
            NepaliDateConverter.getNepaliCalendar(date.year, date.month, date.dayOfMonth)

        assertFalse(rule.isSelectableDate(calendarOf(sat)))
        assertFalse(rule.isSelectableDate(calendarOf(workdayHoliday)))
        assertTrue(rule.isSelectableDate(calendarOf(firstDayOfWeek(3))))
        assertTrue(rule.isSelectableYear(2082))
    }

    // Provider composition

    @Test
    fun plus_reportsBothLists() {
        val date = SimpleDate(2082, 1, 1)
        val national = ListProvider(listOf(entry(date, "New Year", NepaliEventKind.GovernmentPublic)))
        val school = ListProvider(listOf(entry(date, "Founders Day", NepaliEventKind.Regional)))
        val policy = NepaliCalendarPolicy(provider = national + school)

        assertEquals(listOf("New Year", "Founders Day"), policy.statusOf(date).names)
    }

    @Test
    fun filtered_narrowsToTheKindsAsked() {
        val date = SimpleDate(2082, 1, 1)
        val mixed = ListProvider(
            listOf(
                entry(date, "New Year", NepaliEventKind.GovernmentPublic),
                entry(date, "Observed thing", NepaliEventKind.Observance)
            )
        )
        val policy = NepaliCalendarPolicy(
            provider = mixed.filtered { it.kind == NepaliEventKind.GovernmentPublic }
        )

        assertEquals(listOf("New Year"), policy.statusOf(date).names)
    }

    // Arithmetic under a policy

    @Test
    fun workingDays_followTheInstitutionsWeek() {
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 1, 15) // a 14-day span, two full weeks

        val office = NepaliDateConverter.workingDaysBetween(start, end, NepaliCalendarPolicy.Default)
        val school = NepaliDateConverter.workingDaysBetween(
            start, end, NepaliCalendarPolicy(weeklyOffDays = setOf(saturday, sunday))
        )

        assertEquals(12, office, "two Saturdays off")
        assertEquals(10, school, "two Saturdays and two Sundays off")
    }

    @Test
    fun holidayOnAWeeklyOffDay_isNotCountedTwice() {
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 1, 15)
        val sat = firstDayOfWeek(saturday)
        val withSaturdayNamed = NepaliCalendarPolicy(
            provider = ListProvider(listOf(entry(sat, "Dashain", NepaliEventKind.Religious)))
        )

        assertEquals(
            NepaliDateConverter.workingDaysBetween(start, end, NepaliCalendarPolicy.Default),
            NepaliDateConverter.workingDaysBetween(start, end, withSaturdayNamed)
        )
    }

    @Test
    fun nextWorkingDayAndAddWorkingDays_agreeWithTheProviderOverloads() {
        val from = SimpleDate(2082, 1, 1)
        val policy = NepaliCalendarPolicy(weeklyOffDays = setOf(saturday, sunday))

        assertEquals(
            NepaliDateConverter.nextWorkingDay(from, NoOpEventProvider, setOf(saturday, sunday)),
            NepaliDateConverter.nextWorkingDay(from, policy)
        )
        assertEquals(
            NepaliDateConverter.addWorkingDays(from, 5, NoOpEventProvider, setOf(saturday, sunday)),
            NepaliDateConverter.addWorkingDays(from, 5, policy)
        )
    }

    @Test
    fun aWeeklySetWrittenToAnotherConventionIsRejected() {
        // Sunday is 1 here and 0 in JavaScript. A set written the other way round would close
        // nothing at all, so it fails at construction instead of at the end of the month.
        assertFailsWith<IllegalArgumentException> {
            NepaliCalendarPolicy(weeklyOffDays = setOf(0, 6))
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliCalendarPolicy(weeklyOffDays = setOf(8))
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliCalendarPolicy(weeklyOffDays = setOf(-1))
        }
    }

    @Test
    fun everyDayOfTheWeekIsAcceptedAndNothingElseIsNeeded() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = (1..7).toSet())

        for (dayOfWeek in 1..7) {
            assertTrue(policy.isWeeklyOff(dayOfWeek), "day $dayOfWeek should be closed")
        }
        assertFalse(policy.isWeeklyOff(0), "a number outside the week is never a day off")
    }

    @Test
    fun anEntryReportedUnderTheWrongYear_isNotDrawnOnThatDay() {
        // The SPI asks a provider to answer for the year it is given. One that ignores the year is
        // still safe here, because a date is matched whole rather than by day and month.
        val wrongYear = object : NepaliEventProvider {
            override fun events(year: Int): Set<NepaliCalendarEvent> =
                setOf(entry(SimpleDate(2081, 1, 1), "Last year", NepaliEventKind.Religious))
        }
        val policy = NepaliCalendarPolicy(provider = wrongYear)

        assertTrue(policy.eventsOn(SimpleDate(2082, 1, 1)).isEmpty())
        assertEquals(listOf("Last year"), policy.eventsOn(SimpleDate(2081, 1, 1)).map { it.name })
    }

    @Test
    fun theSameEntryTwice_countsOnce() {
        val date = SimpleDate(2082, 1, 1)
        val duplicated = entry(date, "New Year", NepaliEventKind.GovernmentPublic)
        val policy = NepaliCalendarPolicy(provider = ListProvider(listOf(duplicated, duplicated)))

        assertEquals(1, policy.eventsOn(date).size, "a set collapses an identical entry")
    }

    @Test
    fun aProviderThatOnlyAnswersClosesOn_namesNothingAndStillClosesTheDay() {
        // A calendar has to list what it draws, so a status reads events(year) and this provider
        // gives it nothing. Whether the day is worked is a different question, asked of the
        // provider itself, and every answer that counts working days has to agree on it.
        val date = SimpleDate(2082, 1, 1)
        val predicateOnly = object : NepaliEventProvider {
            override fun events(year: Int): Set<NepaliCalendarEvent> = emptySet()
            override fun closesOn(date: SimpleDate): Boolean = true
        }
        val policy = NepaliCalendarPolicy(weeklyOffDays = emptySet(), provider = predicateOnly)

        assertTrue(policy.eventsOn(date).isEmpty())
        assertFalse(policy.statusOf(date).isNonWorking, "nothing to draw and nothing to name")
        assertTrue(policy.isNonWorkingDay(date), "the policy asks the provider, not the status")
        assertEquals(
            0,
            NepaliDateConverter.workingDaysBetween(date, SimpleDate(2082, 1, 8), policy),
            "the arithmetic sees the same closure"
        )
    }

    @Test
    fun isNonWorkingDay_answersForBothReasonsAndLeavesAnObservanceWorked() {
        val closed = SimpleDate(2082, 1, 1)
        val observed = SimpleDate(2082, 1, 2)
        val plain = SimpleDate(2082, 1, 3)
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = setOf(saturday),
            provider = ListProvider(
                listOf(
                    entry(closed, "Bida", NepaliEventKind.GovernmentPublic),
                    entry(observed, "Programme", NepaliEventKind.Observance)
                )
            )
        )

        assertTrue(policy.isNonWorkingDay(closed))
        assertFalse(policy.isNonWorkingDay(observed), "a programme leaves the day worked")
        assertEquals(
            NepaliDateConverter.getNepaliCalendar(plain.year, plain.month, plain.dayOfMonth)
                .dayOfWeek == saturday,
            policy.isNonWorkingDay(plain),
            "an unnamed day is closed only when the week says so"
        )
    }

    @Test
    fun isNonWorkingDay_agreesWithTheStatusForAListedEvent() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = setOf(saturday),
            provider = ListProvider(
                listOf(
                    entry(SimpleDate(2082, 1, 1), "Bida", NepaliEventKind.GovernmentPublic),
                    entry(SimpleDate(2082, 1, 5), "Programme", NepaliEventKind.Observance)
                )
            )
        )

        for (dayOfMonth in 1..NepaliDateConverter.getNepaliMonthCalendar(2082, 1).totalDaysInMonth) {
            val date = SimpleDate(2082, 1, dayOfMonth)
            assertEquals(
                policy.statusOf(date).isNonWorking,
                policy.isNonWorkingDay(date),
                "day $dayOfMonth"
            )
        }
    }

    @Test
    fun asSelectableDates_narrowsARuleItIsComposedUnder() {
        val today = SimpleDate(2082, 1, 10)
        val policy = NepaliCalendarPolicy(weeklyOffDays = setOf(saturday))
        val rule = NepaliDateConverter.AfterDateSelectable(today, includeDate = true)
            .excludingWeekends(policy.weeklyOffDays)

        fun calendarOf(date: SimpleDate) =
            NepaliDateConverter.getNepaliCalendar(date.year, date.month, date.dayOfMonth)

        assertFalse(rule.isSelectableDate(calendarOf(SimpleDate(2082, 1, 9))), "before the anchor")
        assertTrue(rule.isSelectableDate(calendarOf(today)) == (calendarOf(today).dayOfWeek != saturday))
    }

    @Test
    fun aWorkingDayStatusCarriesNothing() {
        val status = NepaliDayStatus.Working

        assertFalse(status.isWeeklyOff)
        assertFalse(status.isNonWorking)
        assertNull(status.primaryKind)
        assertTrue(status.names.isEmpty())
        assertTrue(status.events.isEmpty())
    }

    @Test
    fun anObservanceOnlyDay_isStillAWorkingDayEverywhere() {
        // The one rule, read by three call sites: the status, the arithmetic, and the picker rule.
        val workday = firstDayOfWeek(2)
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = ListProvider(
                listOf(entry(workday, "World Health Day", NepaliEventKind.Observance))
            )
        )

        val status = policy.statusOf(workday)

        assertEquals(1, status.events.size, "it is still named")
        assertFalse(status.isNonWorking, "but the office is open")
        assertTrue(status.closures.isEmpty())
        assertEquals(
            1,
            NepaliDateConverter.workingDaysBetween(
                workday, offsetDay(workday, 1), policy
            ),
            "and the day is counted as worked"
        )
        assertTrue(
            policy.asSelectableDates().isSelectableDate(
                NepaliDateConverter.getNepaliCalendar(workday.year, workday.month, workday.dayOfMonth)
            ),
            "and it stays selectable"
        )
    }

    @Test
    fun anEventDecidesForItselfWhetherItCloses() {
        // A regional holiday closes one district and not the next, and a school programme closes
        // nothing at all, so the entry overrides what its kind usually means.
        val workday = firstDayOfWeek(2)
        val openRegional = NepaliCalendarEvent(
            date = workday,
            name = "Local jatra, observed elsewhere",
            kind = NepaliEventKind.Regional,
            closesOffices = false
        )
        val closingProgramme = NepaliCalendarEvent(
            date = workday,
            name = "Annual programme",
            kind = NepaliEventKind.Observance,
            closesOffices = true
        )

        assertFalse(
            NepaliCalendarPolicy(emptySet(), ListProvider(listOf(openRegional)))
                .statusOf(workday).isNonWorking
        )
        assertTrue(
            NepaliCalendarPolicy(emptySet(), ListProvider(listOf(closingProgramme)))
                .statusOf(workday).isNonWorking
        )
    }

    @Test
    fun anEventCarriesItsIdAndPayloadBackUntouched() {
        val workday = firstDayOfWeek(2)
        val payload = """{"images":["a.png"],"note":"तिहार"}"""
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        date = workday,
                        name = "Tihar",
                        kind = NepaliEventKind.Religious,
                        id = "evt-42",
                        payload = payload
                    )
                )
            )
        )

        val event = policy.eventsOn(workday).single()

        assertEquals("evt-42", event.id)
        assertEquals(payload, event.payload, "the library never parses it, so it comes back whole")
    }

    @Test
    fun monthStatusWalksTheWeekWithoutConvertingEveryDay() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = setOf(saturday),
            provider = ListProvider(
                listOf(entry(SimpleDate(2082, 1, 5), "Something", NepaliEventKind.Religious))
            )
        )

        val month = policy.monthStatus(2082, 1)

        assertEquals(NepaliDateConverter.getTotalDaysInNepaliMonth(2082, 1), month.size)
        for ((index, status) in month.withIndex()) {
            val date = SimpleDate(2082, 1, index + 1)
            assertEquals(
                policy.statusOf(date),
                status,
                "day ${index + 1} must agree with statusOf"
            )
        }
        assertEquals(listOf("Something"), month[4].names)
    }

    @Test
    fun onlyAnObservance_leavesTheOfficeOpen() {
        assertTrue(NepaliEventKind.GovernmentPublic.closesOfficesByDefault)
        assertTrue(NepaliEventKind.Religious.closesOfficesByDefault)
        assertTrue(NepaliEventKind.Regional.closesOfficesByDefault)
        assertFalse(NepaliEventKind.Observance.closesOfficesByDefault)
    }

    @Test
    fun kindPriority_ordersClosuresFirstAndObservancesLast() {
        val ordered = NepaliEventKind.entries.sortedBy { it.priority }

        assertEquals(
            listOf(
                NepaliEventKind.GovernmentPublic,
                NepaliEventKind.Religious,
                NepaliEventKind.Regional,
                NepaliEventKind.Observance
            ),
            ordered
        )
    }

    @Test
    fun twoPoliciesOverTheSameWeekAndProvider_areEqual() {
        val provider = ListProvider(
            listOf(NepaliCalendarEvent(SimpleDate(2082, 1, 1), "New year", NepaliEventKind.GovernmentPublic))
        )

        val one = NepaliCalendarPolicy(setOf(saturday, sunday), provider)
        val other = NepaliCalendarPolicy(setOf(sunday, saturday), provider)

        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
    }

    @Test
    fun policiesDifferingInTheirWeekOrTheirProvider_areNotEqual() {
        val provider = ListProvider(emptyList())
        val base = NepaliCalendarPolicy(setOf(saturday), provider)

        assertTrue(base != NepaliCalendarPolicy(setOf(sunday), provider))
        assertTrue(base != NepaliCalendarPolicy(setOf(saturday), NoOpEventProvider))
        assertEquals(base, NepaliCalendarPolicy(NepaliWeekend.Default, provider))
    }
}
