// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Runs the policy half of the engine host against the real engine: a policy
 * that crosses as plain data has to answer the same way one built in Kotlin
 * would, and the working-day helpers have to agree with it.
 */
internal class EngineApiPolicyTest {

    private val api = EngineApiImpl()

    private val dashami = EventDto(
        year = 2082,
        month = 6,
        dayOfMonth = 24,
        name = "Vijaya Dashami",
        kind = EventKindDto.GOVERNMENT_PUBLIC,
        closesOffices = true,
        id = "dashami",
        payload = null
    )

    private val office = PolicyDto(weeklyOffDays = listOf(7), events = listOf(dashami))

    private fun date(dayOfMonth: Long) =
        DateDto(year = 2082, month = 6, dayOfMonth = dayOfMonth)

    private val daysInMonth = api.getTotalDaysInBsMonth(2082, 6)

    @Test
    fun aHolidayClosesTheDayAndNamesItself() {
        val status = api.statusOf(office, date(24))

        assertTrue(status.isNonWorking)
        assertFalse(status.isWeeklyOff)
        assertEquals(EventKindDto.GOVERNMENT_PUBLIC, status.primaryKind)
        assertContains(status.names, "Vijaya Dashami")
        assertEquals("dashami", status.events.single().id)
        assertEquals("Vijaya Dashami", status.closures.single().name)
    }

    @Test
    fun aMonthOfStatusesCoversEveryDayAndMarksTheWeeklyOffs() {
        val statuses = api.monthStatus(office, 2082, 6)

        assertEquals(daysInMonth.toInt(), statuses.size)
        assertTrue(statuses.count { it.isWeeklyOff } >= 4, "a month holds at least four Saturdays")
        assertTrue(statuses[23].isNonWorking, "the 24th is the holiday")
    }

    @Test
    fun eventsAreFoundOnTheirDayAndInTheirMonth() {
        assertEquals("Vijaya Dashami", api.eventsOn(office, date(24)).single().name)
        assertTrue(api.eventsOn(office, date(23)).isEmpty())
        assertEquals(1, api.eventsIn(office, 2082, 6).size)
        assertTrue(api.eventsIn(office, 2082, 7).isEmpty())
    }

    @Test
    fun theClosedDayQueryAgreesWithTheStatus() {
        assertTrue(api.isNonWorkingDay(office, date(24)))

        val working = (1L..daysInMonth).first { !api.isNonWorkingDay(office, date(it)) }
        assertFalse(api.statusOf(office, date(working)).isNonWorking)
    }

    @Test
    fun workingDaysBetweenCountsWhatTheDayQueryReports() {
        val expected = (1L until 15L).count { !api.isNonWorkingDay(office, date(it)) }

        assertEquals(expected.toLong(), api.workingDaysBetween(office, date(1), date(15)))
        assertEquals(0L, api.workingDaysBetween(office, date(1), date(1)))
    }

    @Test
    fun theNextWorkingDayStepsPastTheHoliday() {
        val next = api.nextWorkingDay(office, date(24))

        assertTrue(next.dayOfMonth > 24)
        assertFalse(api.isNonWorkingDay(office, date(next.dayOfMonth)))
    }

    @Test
    fun workingDayArithmeticFollowsTheWorkdayConvention() {
        val stays = api.addWorkingDays(office, date(24), 0)
        assertEquals(24L, stays.dayOfMonth)

        val forward = api.addWorkingDays(office, date(24), 3)
        assertTrue(forward.dayOfMonth > 24)

        val back = api.addWorkingDays(office, date(24), -3)
        assertTrue(back.dayOfMonth < 24)
    }

    @Test
    fun aPolicyWithoutAWeekendOnlyClosesForItsEvents() {
        val alwaysOpen = PolicyDto(weeklyOffDays = emptyList(), events = listOf(dashami))
        val statuses = api.monthStatus(alwaysOpen, 2082, 6)

        assertTrue(statuses.none { it.isWeeklyOff })
        assertEquals(1, statuses.count { it.isNonWorking })
    }

    @Test
    fun anObservanceMarksTheDayWithoutClosingIt() {
        val meeting = dashami.copy(
            dayOfMonth = 9,
            name = "Staff meeting",
            kind = EventKindDto.OBSERVANCE,
            closesOffices = false,
            id = "meeting"
        )
        val schedule = PolicyDto(weeklyOffDays = emptyList(), events = listOf(meeting))
        val status = api.statusOf(schedule, date(9))

        assertFalse(status.isNonWorking)
        assertContains(status.names, "Staff meeting")
        assertTrue(status.closures.isEmpty())
    }

    @Test
    fun aSpanRepeatsTheEventOnConsecutiveDays() {
        val span = api.eventSpanningDays(dashami, 3)

        assertEquals(3, span.size)
        assertEquals(listOf(24L, 25L, 26L), span.map { it.dayOfMonth })
        assertTrue(span.all { it.name == "Vijaya Dashami" && it.id == "dashami" })
    }

    @Test
    fun aSpanThroughADateIncludesBothEnds() {
        val span = api.eventSpanningThrough(dashami, date(26))

        assertEquals(3, span.size)
        assertEquals(24L, span.first().dayOfMonth)
        assertEquals(26L, span.last().dayOfMonth)
    }
}
