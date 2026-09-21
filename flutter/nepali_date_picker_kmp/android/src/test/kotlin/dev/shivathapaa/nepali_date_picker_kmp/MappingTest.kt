// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the value conversions between the wire types and the engine's own:
 * every field survives the crossing, and a policy rebuilt from the same rows
 * stays equal to its twin so a remembered picker policy does not churn.
 */
internal class MappingTest {

    @Test
    fun aCalendarSurvivesTheRoundTripFieldForField() {
        val calendar = CustomCalendar(
            year = 2082,
            month = 6,
            dayOfMonth = 4,
            era = 2,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 1,
            dayOfWeek = 6,
            dayOfYear = 160,
            weekOfMonth = 1,
            weekOfYear = 24
        )

        assertEquals(calendar, calendar.toDto().toCore())
    }

    @Test
    fun aTimeKeepsItsNanosecondsBothWays() {
        val time = SimpleTime(hour = 16, minute = 30, second = 15, nanosecond = 500)

        assertEquals(time, time.toDto().toCore())
    }

    @Test
    fun aYearRangeCrossesAsItsEnds() {
        val range = (2080..2090).toDto()

        assertEquals(2080L, range.first)
        assertEquals(2090L, range.last)
    }

    @Test
    fun anEventKeepsItsIdentityAndPayload() {
        val event = NepaliCalendarEvent(
            date = dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate(2082, 6, 24),
            name = "Vijaya Dashami",
            kind = NepaliEventKind.GovernmentPublic,
            closesOffices = true,
            id = "dashami",
            payload = "{\"note\":\"tenth day\"}"
        )

        val round = event.toDto().toCore()
        assertEquals(event.date, round.date)
        assertEquals("Vijaya Dashami", round.name)
        assertEquals(NepaliEventKind.GovernmentPublic, round.kind)
        assertTrue(round.closesOffices)
        assertEquals("dashami", round.id)
        assertEquals("{\"note\":\"tenth day\"}", round.payload)
    }

    @Test
    fun aPolicyBuiltFromTheSameRowsEqualsItsTwin() {
        val rows = listOf(
            EventDto(
                year = 2082,
                month = 6,
                dayOfMonth = 24,
                name = "Vijaya Dashami",
                kind = EventKindDto.GOVERNMENT_PUBLIC,
                closesOffices = true,
                id = null,
                payload = null
            )
        )
        val first = PolicyDto(weeklyOffDays = listOf(7), events = rows).toCore()
        val second = PolicyDto(weeklyOffDays = listOf(7), events = rows).toCore()

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertEquals(setOf(7), first.weeklyOffDays)
        assertEquals(1, first.provider.events(2082).size)
        assertTrue(first.provider.events(2081).isEmpty())
    }

    @Test
    fun aPolicyGroupsItsEventsByTheYearTheyFallIn() {
        val policy = PolicyDto(
            weeklyOffDays = emptyList(),
            events = listOf(
                EventDto(2082, 6, 24, "Dashami", EventKindDto.RELIGIOUS, true, null, null),
                EventDto(2083, 6, 24, "Dashami", EventKindDto.RELIGIOUS, true, null, null)
            )
        ).toCore()

        assertEquals(1, policy.provider.events(2082).size)
        assertEquals(1, policy.provider.events(2083).size)
    }

    @Test
    fun anAbsentSelectableRuleStaysAbsent() {
        assertNull((null as SelectableDto?).toCore())
        assertNull(
            SelectableDto(
                minDate = null,
                maxDate = null,
                includeMinDate = false,
                includeMaxDate = false,
                excludeWeekend = null,
                excludeClosuresOf = null
            ).toCore()
        )
    }
}
