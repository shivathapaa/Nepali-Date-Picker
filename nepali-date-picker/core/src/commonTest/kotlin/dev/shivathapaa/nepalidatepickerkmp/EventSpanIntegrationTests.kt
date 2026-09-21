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
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.filtered
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.plus
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A span read back through the rest of the event API: it has to mark, count, block and compose
 * exactly as the same days written out one by one would.
 */
class EventSpanIntegrationTests {

    private val festival = NepaliCalendarEvent(
        date = SimpleDate(2082, 6, 17),
        name = "Dashain",
        kind = NepaliEventKind.Religious,
        id = "dashain-2082"
    )

    private val programme = NepaliCalendarEvent(
        date = SimpleDate(2082, 6, 17),
        name = "Sports week",
        kind = NepaliEventKind.Observance,
        id = "sports-2082"
    )

    /** Asoj 17 through 21, the window the tests below count over. */
    private val windowStart = SimpleDate(2082, 6, 17)
    private val windowEnd = SimpleDate(2082, 6, 22)

    private val openWeek = NepaliCalendarPolicy(weeklyOffDays = emptySet(), provider = NoOpEventProvider)

    // Marking

    @Test
    fun aSpan_marksEveryDayItCoversAndNothingElse() {
        val policy = NepaliCalendarPolicy(provider = festival.spanningDays(5).asProvider())

        for (day in 17..21) {
            assertEquals(
                listOf("Dashain"),
                policy.eventsOn(SimpleDate(2082, 6, day)).map { it.name },
                "day $day"
            )
            assertTrue(policy.isNonWorkingDay(SimpleDate(2082, 6, day)), "day $day")
        }
        assertTrue(policy.eventsOn(SimpleDate(2082, 6, 22)).isEmpty())
        assertFalse(policy.isNonWorkingDay(SimpleDate(2082, 6, 22)))
    }

    @Test
    fun aSpan_showsUpInTheMonthWalk() {
        val policy = NepaliCalendarPolicy(provider = festival.spanningDays(3).asProvider())

        val marked = policy.monthStatus(2082, 6)
            .mapIndexedNotNull { index, status -> (index + 1).takeIf { status.events.isNotEmpty() } }

        assertEquals(listOf(17, 18, 19), marked)
    }

    @Test
    fun aSpan_acrossTheYearEnd_isReportedByBothYears() {
        val span = festival.copy(date = SimpleDate(2082, 12, lastDayOf(2082, 12)))
            .spanningThrough(SimpleDate(2083, 1, 3))
        val policy = NepaliCalendarPolicy(provider = span.asProvider())

        assertEquals(1, policy.eventsIn(2082, 12).size)
        assertEquals(3, policy.eventsIn(2083, 1).size)
        assertTrue(policy.isNonWorkingDay(SimpleDate(2083, 1, 1)))
    }

    @Test
    fun aSpan_collapsesBackToOneRowByItsId() {
        val month = festival.spanningDays(10) +
                NepaliCalendarEvent(SimpleDate(2082, 6, 3), "Sprint review", NepaliEventKind.Observance)
        val policy = NepaliCalendarPolicy(provider = month.asProvider())

        val agenda = policy.eventsIn(2082, 6).distinctBy { it.id ?: it.name }

        assertEquals(listOf("Sprint review", "Dashain"), agenda.map { it.name })
    }

    @Test
    fun aSpan_expandedTwice_dedupesThroughTheProviderSet() {
        val twice = festival.spanningDays(4) + festival.spanningDays(4)

        assertEquals(4, twice.toSet().size, "the same span stated twice is one span")
        assertEquals(4, NepaliCalendarPolicy(provider = twice.asProvider()).eventsIn(2082, 6).size)
    }

    // Counting

    @Test
    fun aClosingSpan_removesItsDaysFromTheWorkingCount() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = festival.spanningDays(5).asProvider()
        )

        assertEquals(5, NepaliDateConverter.workingDaysBetween(windowStart, windowEnd, openWeek))
        assertEquals(0, NepaliDateConverter.workingDaysBetween(windowStart, windowEnd, policy))
    }

    @Test
    fun anObservanceSpan_leavesTheWorkingCountAlone() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = programme.spanningDays(5).asProvider()
        )

        assertEquals(
            NepaliDateConverter.workingDaysBetween(windowStart, windowEnd, openWeek),
            NepaliDateConverter.workingDaysBetween(windowStart, windowEnd, policy),
            "a week of programmes is still a week of work"
        )
    }

    @Test
    fun aSpanThatIsAlsoAWeeklyOffDay_isCountedOnce() {
        val policy = NepaliCalendarPolicy(provider = festival.spanningDays(5).asProvider())
        val saturdaysInWindow = (17..21).count { dayOfWeek(SimpleDate(2082, 6, it)) == 7 }

        assertTrue(saturdaysInWindow > 0, "the window has to overlap the weekly rule to test this")
        assertEquals(0, NepaliDateConverter.workingDaysBetween(windowStart, windowEnd, policy))
    }

    @Test
    fun nextWorkingDay_stepsOverAWholeSpan() {
        val policy = NepaliCalendarPolicy(provider = festival.spanningDays(5).asProvider())

        val landed = NepaliDateConverter.nextWorkingDay(windowStart, policy)

        assertTrue(landed >= SimpleDate(2082, 6, 22), "landed on $landed, inside the span")
        assertFalse(policy.isNonWorkingDay(landed))
    }

    @Test
    fun addWorkingDays_countsPastASpan() {
        val policy = NepaliCalendarPolicy(provider = festival.spanningDays(5).asProvider())
        val from = SimpleDate(2082, 6, 16)

        val landed = NepaliDateConverter.addWorkingDays(from, 1, policy)

        assertTrue(landed >= SimpleDate(2082, 6, 22), "landed on $landed, inside the span")
        assertFalse(policy.isNonWorkingDay(landed))
    }

    // Blocking and composing

    @Test
    fun asSelectableDates_blocksEveryDayOfAClosingSpan() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = festival.spanningDays(5).asProvider()
        )
        val rule = policy.asSelectableDates()

        for (day in 17..21) {
            assertFalse(rule.isSelectableDate(calendarOf(2082, 6, day)), "day $day")
        }
        assertTrue(rule.isSelectableDate(calendarOf(2082, 6, 22)))
        assertTrue(rule.isSelectableYear(2082))
    }

    @Test
    fun asSelectableDates_leavesAnObservanceSpanSelectable() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = programme.spanningDays(5).asProvider()
        )
        val rule = policy.asSelectableDates()

        assertTrue((17..21).all { rule.isSelectableDate(calendarOf(2082, 6, it)) })
    }

    @Test
    fun filtered_dropsAWholeSpanAtOnce() {
        val both = (festival.spanningDays(5) + programme.spanningDays(5)).asProvider()

        val closuresOnly = both.filtered { it.closesOffices }.events(2082)

        assertEquals(5, closuresOnly.size)
        assertEquals(setOf("Dashain"), closuresOnly.map { it.name }.toSet())
    }

    @Test
    fun plus_keepsTwoSpansThatCoverTheSameDays() {
        val merged = festival.spanningDays(5).asProvider() + programme.spanningDays(5).asProvider()
        val policy = NepaliCalendarPolicy(provider = merged)

        assertEquals(
            listOf("Dashain", "Sports week"),
            policy.eventsOn(SimpleDate(2082, 6, 19)).map { it.name },
            "the stronger kind is named first"
        )
        assertTrue(policy.isNonWorkingDay(SimpleDate(2082, 6, 19)), "one of the two closes the day")
    }

    private fun List<NepaliCalendarEvent>.asProvider(): NepaliEventProvider {
        val byYear = groupBy { it.date.year }.mapValues { (_, entries) -> entries.toSet() }
        return object : NepaliEventProvider {
            override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
        }
    }

    private fun lastDayOf(year: Int, month: Int): Int =
        NepaliDateConverter.getNepaliMonthCalendar(year, month).totalDaysInMonth

    private fun dayOfWeek(date: SimpleDate): Int =
        NepaliDateConverter.getNepaliCalendar(date.year, date.month, date.dayOfMonth).dayOfWeek

    private fun calendarOf(year: Int, month: Int, dayOfMonth: Int) =
        NepaliDateConverter.getNepaliCalendar(year, month, dayOfMonth)
}
