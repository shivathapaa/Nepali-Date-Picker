/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp.js

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The span wrappers npm consumers see. The expansion rules are covered by `commonTest`; this guards
 * the wrapper's own work: that every field survives the trip through the JavaScript shape, and that
 * the array it returns is what [createCalendarPolicy] wants.
 */
class NepaliEventSpansJsTest {

    private val dashain = createDetailedEvent(
        year = 2082,
        month = 6,
        dayOfMonth = 17,
        name = "Dashain",
        kind = "religious",
        closesOffices = true,
        id = "dashain-2082",
        payload = """{"district":"all"}"""
    )

    private val sportsWeek = createDetailedEvent(
        2082, 6, 17, "Sports week", "observance", false, "sports-2082", null
    )

    @Test
    fun expandEventDays_givesOneEntryPerDay() {
        val span = expandEventDays(dashain, 10)

        assertEquals(10, span.size)
        assertContentEquals((17..26).toList(), span.map { it.dayOfMonth })
        assertTrue(span.all { it.year == 2082 && it.month == 6 })
    }

    @Test
    fun expandEventDays_carriesEveryFieldThrough() {
        val span = expandEventDays(dashain, 4)

        assertTrue(span.all { it.name == "Dashain" })
        assertTrue(span.all { it.kind == "religious" })
        assertTrue(span.all { it.closesOffices })
        assertTrue(span.all { it.id == "dashain-2082" })
        assertTrue(span.all { it.payload == """{"district":"all"}""" })
    }

    @Test
    fun expandEventDays_keepsAClosureFlagThatContradictsItsKind() {
        assertTrue(expandEventDays(sportsWeek, 5).none { it.closesOffices })
    }

    @Test
    fun expandEventDays_ofOne_isTheEventItself() {
        val span = expandEventDays(dashain, 1)

        assertEquals(1, span.size)
        assertEquals(17, span.single().dayOfMonth)
    }

    @Test
    fun expandEventDays_crossesTheYearEnd() {
        val yearEnd = createEvent(2082, 12, getTotalDaysInBsMonth(2082, 12), "Year end", "religious")

        val span = expandEventDays(yearEnd, 3)

        assertContentEquals(listOf(2082, 2083, 2083), span.map { it.year })
        assertContentEquals(listOf(12, 1, 1), span.map { it.month })
    }

    @Test
    fun expandEventDays_keepsTheKindTheStringCoercedTo() {
        val coerced = createEvent(2082, 6, 17, "Sarkari bida", "public")

        val span = expandEventDays(coerced, 3)

        assertTrue(span.all { it.kind == "governmentPublic" }, "the kind is resolved once, then carried")
        assertTrue(span.all { it.closesOffices })
    }

    @Test
    fun expandEventDays_ofAnUnknownKind_staysAnObservance() {
        val span = expandEventDays(createEvent(2082, 6, 17, "Bank holiday", "bank-holiday"), 3)

        assertTrue(span.all { it.kind == "observance" })
        assertFalse(span.any { it.closesOffices })
    }

    @Test
    fun expandEventDays_rejectsALengthBelowOne() {
        assertFails { expandEventDays(dashain, 0) }
        assertFails { expandEventDays(dashain, -2) }
    }

    @Test
    fun expandEventDays_rejectsASpanRunningPastTheSupportedRange() {
        val lastYear = getBsYearRange().last
        val lastDay = createEvent(
            lastYear, 12, getTotalDaysInBsMonth(lastYear, 12), "Past the table", "religious"
        )

        assertEquals(1, expandEventDays(lastDay, 1).size)
        assertFails { expandEventDays(lastDay, 2) }
    }

    @Test
    fun expandEventThrough_crossesTheYearEnd() {
        val yearEnd = createDetailedEvent(
            2082, 12, getTotalDaysInBsMonth(2082, 12) - 1, "Year end break", "governmentPublic",
            true, "break-2082", null
        )

        val span = expandEventThrough(yearEnd, 2083, 1, 2)

        assertEquals(4, span.size)
        assertContentEquals(listOf(2082, 2082, 2083, 2083), span.map { it.year })
        assertTrue(span.all { it.id == "break-2082" })
    }

    @Test
    fun expandEventThrough_rejectsAnEndOutsideTheSupportedRange() {
        assertFails { expandEventThrough(dashain, getBsYearRange().last + 1, 1, 1) }
    }

    @Test
    fun expandEventThrough_includesBothEnds() {
        val span = expandEventThrough(dashain, 2082, 6, 26)

        assertEquals(10, span.size)
        assertEquals(17, span.first().dayOfMonth)
        assertEquals(26, span.last().dayOfMonth)
    }

    @Test
    fun expandEventThrough_ofTheSameDay_isTheEventItself() {
        assertEquals(1, expandEventThrough(dashain, 2082, 6, 17).size)
    }

    @Test
    fun expandEventThrough_rejectsAnEndBeforeTheStart() {
        assertFails { expandEventThrough(dashain, 2082, 6, 16) }
    }

    @Test
    fun expandEventThrough_rejectsADayTheMonthDoesNotHave() {
        val pastTheMonth = getTotalDaysInBsMonth(2082, 6) + 1

        assertFails { expandEventThrough(dashain, 2082, 6, pastTheMonth) }
    }

    @Test
    fun aSpan_buildsAPolicyThatMarksEveryDayOfIt() {
        val policy = createCalendarPolicy(arrayOf(7), expandEventDays(dashain, 5))

        for (day in 17..21) {
            assertContentEquals(arrayOf("Dashain"), policy.statusOf(2082, 6, day).names)
            assertTrue(policy.isNonWorkingDay(2082, 6, day), "day $day")
        }
        assertEquals(0, policy.eventsOn(2082, 6, 22).size)
        assertEquals(5, policy.eventsIn(2082, 6).size)
    }

    @Test
    fun aSpanOfObservances_marksItsDaysWithoutClosingThem() {
        val policy = createCalendarPolicy(emptyArray(), expandEventDays(sportsWeek, 5))

        assertContentEquals(arrayOf("Sports week"), policy.statusOf(2082, 6, 19).names)
        assertFalse(policy.isNonWorkingDay(2082, 6, 19))
        assertEquals(5, policy.workingDaysBetween(2082, 6, 17, 2082, 6, 22))
    }

    @Test
    fun aClosingSpan_isSkippedByTheWorkingDayArithmetic() {
        val policy = createCalendarPolicy(emptyArray(), expandEventDays(dashain, 5))

        assertEquals(0, policy.workingDaysBetween(2082, 6, 17, 2082, 6, 22))
        assertEquals(22, policy.nextWorkingDay(2082, 6, 17).dayOfMonth)
        assertEquals(22, policy.addWorkingDays(2082, 6, 16, 1).dayOfMonth)
    }

    @Test
    fun aSpanAcrossTheYearEnd_isReportedByBothYears() {
        val yearEnd = createDetailedEvent(
            2082, 12, getTotalDaysInBsMonth(2082, 12), "Year end break", "religious",
            true, "break-2082", null
        )
        val policy = createCalendarPolicy(arrayOf(7), expandEventThrough(yearEnd, 2083, 1, 3))

        assertEquals(1, policy.eventsIn(2082, 12).size)
        assertEquals(3, policy.eventsIn(2083, 1).size)
        assertTrue(policy.isNonWorkingDay(2083, 1, 1))
    }

    @Test
    fun aSpan_showsUpInTheMonthWalk() {
        val policy = createCalendarPolicy(arrayOf(7), expandEventDays(dashain, 3))

        val marked = policy.monthStatus(2082, 6)
            .mapIndexedNotNull { index, status -> (index + 1).takeIf { status.events.isNotEmpty() } }

        assertContentEquals(listOf(17, 18, 19), marked)
    }
}
