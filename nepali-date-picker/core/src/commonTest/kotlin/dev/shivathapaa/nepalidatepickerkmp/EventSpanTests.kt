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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Expanding an event that runs longer than a day: one entry per day, everything but the date
 * unchanged, the right year on each side of a boundary, and a day the calendar does not have
 * refused rather than rolled forward.
 */
class EventSpanTests {

    private val dashain = NepaliCalendarEvent(
        date = SimpleDate(2082, 6, 17),
        name = "Dashain",
        kind = NepaliEventKind.Religious,
        id = "dashain-2082",
        payload = """{"district":"all"}"""
    )

    private fun daysBetween(start: SimpleDate, end: SimpleDate): Int =
        NepaliDateConverter.getNepaliDaysInBetween(start, end)

    private fun lastDayOf(year: Int, month: Int): Int =
        NepaliDateConverter.getNepaliMonthCalendar(year, month).totalDaysInMonth

    // spanningDays

    @Test
    fun spanningDays_ofOne_isTheEventItself() {
        val span = dashain.spanningDays(1)

        assertEquals(listOf(dashain), span)
        assertSame(dashain, span.single(), "a span of one costs no conversion and no copy")
    }

    @Test
    fun spanningDays_givesOneEntryPerDay_inDateOrder() {
        val span = dashain.spanningDays(10)

        assertEquals(10, span.size)
        assertEquals((17..26).map { SimpleDate(2082, 6, it) }, span.map { it.date })
        assertEquals(span.map { it.date }.sorted(), span.map { it.date })
    }

    @Test
    fun spanningDays_carriesEverythingButTheDate() {
        val span = dashain.spanningDays(5)

        assertTrue(span.all { it.name == dashain.name }, "a span is one event named once")
        assertTrue(span.all { it.kind == dashain.kind })
        assertTrue(span.all { it.id == dashain.id })
        assertTrue(span.all { it.payload == dashain.payload })
        assertEquals(5, span.map { it.date }.toSet().size, "every entry lands on its own day")
        assertEquals(SimpleDate(2082, 6, 17), dashain.date, "the event it was built from is unchanged")
    }

    @Test
    fun spanningDays_keepsAClosureFlagThatContradictsItsKind() {
        val programme = NepaliCalendarEvent(
            date = SimpleDate(2082, 6, 17),
            name = "Sports week",
            kind = NepaliEventKind.Religious,
            closesOffices = false
        )

        assertTrue(
            programme.spanningDays(7).none { it.closesOffices },
            "expanding must not re-read the kind default"
        )
    }

    @Test
    fun spanningDays_crossesAMonthBoundary() {
        val endOfAsoj = lastDayOf(2082, 6)
        val event = dashain.copy(date = SimpleDate(2082, 6, endOfAsoj - 1))

        val span = event.spanningDays(4)

        assertEquals(
            listOf(
                SimpleDate(2082, 6, endOfAsoj - 1),
                SimpleDate(2082, 6, endOfAsoj),
                SimpleDate(2082, 7, 1),
                SimpleDate(2082, 7, 2)
            ),
            span.map { it.date }
        )
    }

    @Test
    fun spanningDays_crossesAYearBoundaryIntoTheNextYear() {
        val endOfChaitra = lastDayOf(2082, 12)
        val event = dashain.copy(date = SimpleDate(2082, 12, endOfChaitra - 1), name = "Year end break")

        val span = event.spanningDays(4)

        assertEquals(
            listOf(
                SimpleDate(2082, 12, endOfChaitra - 1),
                SimpleDate(2082, 12, endOfChaitra),
                SimpleDate(2083, 1, 1),
                SimpleDate(2083, 1, 2)
            ),
            span.map { it.date }
        )
        assertEquals(
            mapOf(2082 to 2, 2083 to 2),
            span.groupingBy { it.date.year }.eachCount(),
            "each entry has to fall in the year a provider would report it under"
        )
    }

    @Test
    fun spanningDays_runsPastAWholeYear() {
        val span = dashain.spanningDays(400)

        assertEquals(400, span.map { it.date }.toSet().size)
        assertEquals(399, daysBetween(dashain.date, span.last().date))
        assertTrue(span.last().date.year > dashain.date.year)
    }

    @Test
    fun spanningDays_startsAtTheFirstSupportedDay() {
        val firstYear = NepaliCalendarDefaults.NepaliYearRange.first
        val span = dashain.copy(date = SimpleDate(firstYear, 1, 1)).spanningDays(5)

        assertEquals((1..5).map { SimpleDate(firstYear, 1, it) }, span.map { it.date })
    }

    @Test
    fun spanningDays_rejectsALengthBelowOne() {
        assertFailsWith<IllegalArgumentException> { dashain.spanningDays(0) }
        assertFailsWith<IllegalArgumentException> { dashain.spanningDays(-3) }
    }

    @Test
    fun spanningDays_rejectsASpanRunningPastTheSupportedRange() {
        val lastYear = NepaliCalendarDefaults.NepaliYearRange.last
        val lastDay = dashain.copy(date = SimpleDate(lastYear, 12, lastDayOf(lastYear, 12)))

        assertEquals(1, lastDay.spanningDays(1).size, "the last supported day is still a span of one")
        assertFailsWith<IllegalArgumentException> { lastDay.spanningDays(2) }
    }

    @Test
    fun spanningDays_takesTheEventsOwnDateAsGiven() {
        // Every API here treats an event's date as the caller's to state, so a start the month does
        // not reach is walked from rather than refused. Only the end of a span is checked, because
        // that is what decides how many entries come back.
        val past = dashain.copy(date = SimpleDate(2082, 6, lastDayOf(2082, 6) + 1))

        assertEquals(listOf(past), past.spanningDays(1))
        assertEquals(2, past.spanningDays(2).size)
    }

    @Test
    fun spanningDays_agreesWithTheConverterAcrossTheRange() {
        for (year in NepaliCalendarDefaults.NepaliYearRange.first..2099 step 7) {
            for (month in listOf(1, 6, 12)) {
                val start = SimpleDate(year, month, lastDayOf(year, month) - 2)
                val span = dashain.copy(date = start).spanningDays(9)

                assertEquals(9, span.map { it.date }.toSet().size, "$start")
                assertEquals(8, daysBetween(start, span.last().date), "$start")
            }
        }
    }

    // spanningThrough

    @Test
    fun spanningThrough_includesBothEnds() {
        val span = dashain.spanningThrough(SimpleDate(2082, 6, 26))

        assertEquals(10, span.size)
        assertEquals(dashain.date, span.first().date)
        assertEquals(SimpleDate(2082, 6, 26), span.last().date)
    }

    @Test
    fun spanningThrough_ofTheSameDay_isTheEventItself() {
        assertEquals(listOf(dashain), dashain.spanningThrough(dashain.date))
    }

    @Test
    fun spanningThrough_matchesTheDayCountItSpells() {
        val end = SimpleDate(2083, 1, 2)
        val event = dashain.copy(date = SimpleDate(2082, 12, lastDayOf(2082, 12) - 1))

        assertEquals(event.spanningDays(daysBetween(event.date, end) + 1), event.spanningThrough(end))
    }

    @Test
    fun spanningThrough_crossesMoreThanOneYear() {
        val end = SimpleDate(2084, 1, 5)

        val span = dashain.spanningThrough(end)

        assertEquals(setOf(2082, 2083, 2084), span.map { it.date.year }.toSet())
        assertEquals(end, span.last().date)
        assertEquals(daysBetween(dashain.date, end) + 1, span.size)
    }

    @Test
    fun spanningThrough_acceptsTheLastDayOfAThirtyTwoDayMonth() {
        val longMonth = firstMonthOfThirtyTwoDays()
        val event = dashain.copy(date = SimpleDate(longMonth.year, longMonth.month, 30))

        val span = event.spanningThrough(longMonth)

        assertEquals(3, span.size)
        assertEquals(longMonth, span.last().date)
    }

    @Test
    fun spanningThrough_rejectsAnEndBeforeTheStart() {
        assertFailsWith<IllegalArgumentException> {
            dashain.spanningThrough(SimpleDate(2082, 6, 16))
        }
        assertFailsWith<IllegalArgumentException> {
            dashain.spanningThrough(SimpleDate(2081, 6, 17))
        }
    }

    @Test
    fun spanningThrough_rejectsADayTheCalendarDoesNotHave() {
        val impossible = SimpleDate(2082, 6, lastDayOf(2082, 6) + 1)

        assertFailsWith<IllegalArgumentException> { dashain.spanningThrough(impossible) }
    }

    @Test
    fun spanningThrough_rejectsAMonthTheCalendarDoesNotHave() {
        assertFailsWith<IllegalArgumentException> { dashain.spanningThrough(SimpleDate(2082, 13, 1)) }
        assertFailsWith<IllegalArgumentException> { dashain.spanningThrough(SimpleDate(2082, 0, 1)) }
    }

    @Test
    fun spanningThrough_rejectsAnEndOutsideTheSupportedRange() {
        val pastTheTable = NepaliCalendarDefaults.NepaliYearRange.last + 1

        assertFailsWith<IllegalArgumentException> {
            dashain.spanningThrough(SimpleDate(pastTheTable, 1, 1))
        }
    }

    /** The last day of the first month in the table that runs to 32 days, which not every year has. */
    private fun firstMonthOfThirtyTwoDays(): SimpleDate {
        for (year in 2080..2090) {
            for (month in 1..12) {
                if (lastDayOf(year, month) == 32) return SimpleDate(year, month, 32)
            }
        }
        fail("no 32-day month between 2080 and 2090")
    }
}
