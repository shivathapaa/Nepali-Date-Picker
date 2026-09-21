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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the holiday wrapper npm consumers see. The rules themselves are covered by `commonTest`;
 * this guards the wrapper's own work: string-to-kind coercion, array mapping, and that a policy
 * built from JS arrays answers the way the Kotlin one does.
 */
class NepaliHolidayJsTest {

    private val constitutionDay =
        createEvent(2082, 6, 3, "Constitution Day", "governmentPublic")
    private val localJatra = createEvent(2082, 6, 3, "Local jatra", "regional")
    private val healthDay = createEvent(2082, 6, 5, "World Health Day", "observance")

    private fun officePolicy() =
        createCalendarPolicy(arrayOf(7), arrayOf(constitutionDay, localJatra, healthDay))

    @Test
    fun coercesKindStringsBothWays() {
        assertEquals("governmentPublic", createEvent(2082, 1, 1, "x", "public").kind)
        assertEquals("governmentPublic", createEvent(2082, 1, 1, "x", "government-public").kind)
        assertEquals("religious", createEvent(2082, 1, 1, "x", "FESTIVAL").kind)
        assertEquals("regional", createEvent(2082, 1, 1, "x", "local").kind)
        assertEquals("observance", createEvent(2082, 1, 1, "x", "observance").kind)
    }

    @Test
    fun anUnrecognizedKindClaimsTheLeast() {
        assertEquals("observance", createEvent(2082, 1, 1, "x", "bank-holiday").kind)
    }

    @Test
    fun exposesTheWeeklyRuleItWasBuiltWith() {
        assertContentEquals(arrayOf(7), officePolicy().weeklyOffDays)
        assertContentEquals(
            arrayOf(1, 7),
            createCalendarPolicy(arrayOf(7, 1), emptyArray()).weeklyOffDays
        )
        assertTrue(createCalendarPolicy(arrayOf(7), emptyArray()).isWeeklyOff(7))
        assertFalse(createCalendarPolicy(arrayOf(7), emptyArray()).isWeeklyOff(1))
    }

    @Test
    fun rejectsAWeekWrittenToTheJavaScriptConvention() {
        // JavaScript numbers Sunday 0; this library numbers it 1, and a silent mismatch would close
        // nothing at all.
        assertFails { createCalendarPolicy(arrayOf(0, 6), emptyArray()) }
    }

    @Test
    fun statusOfMapsEveryField() {
        val status = officePolicy().statusOf(2082, 6, 3)

        assertTrue(status.isNonWorking)
        assertEquals("governmentPublic", status.primaryKind, "a closure outranks a regional day")
        assertContentEquals(arrayOf("Constitution Day", "Local jatra"), status.names)
        assertEquals(2, status.events.size)
        assertEquals("Constitution Day", status.events[0].name)
        assertEquals(6, status.events[0].month)
    }

    @Test
    fun aPlainWorkingDayCarriesNothing() {
        val policy = officePolicy()
        // The first day of the month that is neither a Saturday nor one of the three holidays.
        var day = 1
        while (getBsCalendar(2082, 6, day).dayOfWeek == 7 || policy.eventsOn(2082, 6, day).isNotEmpty()) {
            day++
        }

        val status = policy.statusOf(2082, 6, day)

        assertFalse(status.isNonWorking)
        assertFalse(status.isWeeklyOff)
        assertNull(status.primaryKind)
        assertTrue(status.names.isEmpty())
    }

    @Test
    fun aWeeklyOffDayIsClosedButUnnamed() {
        // Find the first Saturday of the month through the conversion facade itself.
        var saturday = 1
        while (getBsCalendar(2082, 6, saturday).dayOfWeek != 7) saturday++

        val status = createCalendarPolicy(arrayOf(7), emptyArray()).statusOf(2082, 6, saturday)

        assertTrue(status.isWeeklyOff)
        assertTrue(status.isNonWorking)
        assertNull(status.primaryKind)
        assertTrue(status.events.isEmpty())
    }

    @Test
    fun listsADayAndAMonth() {
        val policy = officePolicy()

        assertEquals(2, policy.eventsOn(2082, 6, 3).size)
        assertTrue(policy.eventsOn(2082, 6, 4).isEmpty())
        assertContentEquals(
            arrayOf("Constitution Day", "Local jatra", "World Health Day"),
            policy.eventsIn(2082, 6).map { it.name }.toTypedArray()
        )
        assertTrue(policy.eventsIn(2082, 7).isEmpty())
        assertTrue(policy.eventsIn(2083, 6).isEmpty(), "another year is another list")
    }

    @Test
    fun countsAndWalksWorkingDays() {
        val everyDayOpen = createCalendarPolicy(emptyArray(), emptyArray())
        val office = createCalendarPolicy(arrayOf(7), emptyArray())
        val school = createCalendarPolicy(arrayOf(7, 1), emptyArray())

        assertEquals(14, everyDayOpen.workingDaysBetween(2082, 1, 1, 2082, 1, 15))
        assertEquals(12, office.workingDaysBetween(2082, 1, 1, 2082, 1, 15))
        assertEquals(10, school.workingDaysBetween(2082, 1, 1, 2082, 1, 15))

        val next = office.nextWorkingDay(2082, 1, 1)
        assertTrue(next.dayOfWeek != 7)

        val walked = everyDayOpen.addWorkingDays(2082, 1, 1, 5)
        assertEquals(2082, walked.year)
        assertEquals(6, walked.dayOfMonth)
        assertEquals(2082, everyDayOpen.addWorkingDays(2082, 1, 1, 0).year)
    }

    @Test
    fun aHolidayIsSkippedWhenWalking() {
        val policy = createCalendarPolicy(
            emptyArray(),
            arrayOf(createEvent(2082, 1, 2, "Closed", "governmentPublic"))
        )

        assertEquals(3, policy.addWorkingDays(2082, 1, 1, 1).dayOfMonth)
    }

    @Test
    fun anEmptyPolicyIsUsableAndSaysNothingIsClosed() {
        val empty = createCalendarPolicy(emptyArray(), emptyArray())

        assertFalse(empty.isNonWorkingDay(2082, 1, 1))
        assertTrue(empty.eventsIn(2082, 1).isEmpty())
        assertTrue(empty.weeklyOffDays.isEmpty())
    }

    @Test
    fun aKindDecidesWhetherAPlainEventClosesTheDay() {
        assertTrue(createEvent(2082, 1, 1, "Bida", "governmentPublic").closesOffices)
        assertTrue(createEvent(2082, 1, 1, "Dashain", "religious").closesOffices)
        assertTrue(createEvent(2082, 1, 1, "Jatra", "regional").closesOffices)
        assertFalse(createEvent(2082, 1, 1, "Programme", "observance").closesOffices)
    }

    @Test
    fun aDetailedEventOverridesItsKindAndCarriesItsOwnData() {
        val payload = """{"images":["a.png"]}"""
        val event = createDetailedEvent(
            2082, 6, 3, "Annual programme", "religious",
            closesOffices = false, id = "evt-42", payload = payload
        )

        assertFalse(event.closesOffices, "the event has the final say, not its kind")
        assertEquals("evt-42", event.id)
        assertEquals(payload, event.payload)
    }

    @Test
    fun theExtraFieldsSurviveThePolicyRoundTrip() {
        val policy = createCalendarPolicy(
            emptyArray(),
            arrayOf(
                createDetailedEvent(
                    2082, 6, 3, "Tihar", "religious",
                    closesOffices = true, id = "evt-7", payload = "{}"
                )
            )
        )

        val event = policy.eventsOn(2082, 6, 3).single()

        assertEquals("evt-7", event.id)
        assertEquals("{}", event.payload)
        assertTrue(event.closesOffices)
        assertTrue(policy.isNonWorkingDay(2082, 6, 3))
    }

    @Test
    fun anEventThatDoesNotCloseLeavesTheDayWorked() {
        val policy = createCalendarPolicy(
            emptyArray(),
            arrayOf(
                createDetailedEvent(
                    2082, 6, 3, "Standup", "observance",
                    closesOffices = false, id = null, payload = null
                )
            )
        )

        val status = policy.statusOf(2082, 6, 3)

        assertEquals(1, status.events.size, "still named")
        assertTrue(status.closures.isEmpty(), "but nothing shuts")
        assertFalse(status.isNonWorking)
        assertFalse(policy.isNonWorkingDay(2082, 6, 3))
    }

    @Test
    fun monthStatusAnswersEveryDayInOrder() {
        val policy = createCalendarPolicy(
            arrayOf(7),
            arrayOf(createEvent(2082, 6, 3, "Constitution Day", "governmentPublic"))
        )

        val month = policy.monthStatus(2082, 6)

        assertEquals(getTotalDaysInBsMonth(2082, 6), month.size)
        assertContentEquals(arrayOf("Constitution Day"), month[2].names)
        for (day in 1..month.size) {
            assertEquals(
                policy.statusOf(2082, 6, day).isNonWorking,
                month[day - 1].isNonWorking,
                "day $day must agree with statusOf"
            )
        }
    }

    @Test
    fun monthStatusOfAnUncoveredYearIsStillAFullMonth() {
        val policy = createCalendarPolicy(arrayOf(7), emptyArray())

        val month = policy.monthStatus(2090, 12)

        assertEquals(getTotalDaysInBsMonth(2090, 12), month.size)
        assertTrue(month.all { it.events.isEmpty() })
    }

    @Test
    fun aDateOutsideTheSupportedRangeFails() {
        assertFails { officePolicy().statusOf(2101, 1, 1) }
    }
}
