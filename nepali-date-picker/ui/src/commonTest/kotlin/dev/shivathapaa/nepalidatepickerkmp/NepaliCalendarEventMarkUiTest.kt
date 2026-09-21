/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTestApi::class, ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventDisplayStyle
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val CalendarTestWidth = 360.dp
private val EventDay = SimpleDate(2082, 6, 10)

/**
 * What a policy's events do to the grid: the names a screen reader hears, the dots a style asks for,
 * and what a day carrying more of them than its cell draws still says.
 */
class NepaliCalendarEventMarkUiTest {

    private fun policyWith(vararg events: NepaliCalendarEvent) =
        NepaliCalendarPolicy(provider = eventsProviderOf(*events))

    private val festival = NepaliCalendarEvent(
        date = EventDay,
        name = "Ghatasthapana",
        kind = NepaliEventKind.Religious
    )

    @Test
    fun anEventsName_reachesTheCellDescription() = runComposeUiTest {
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = policyWith(festival),
                secondaryDateLocale = null
            )
        }

        onNodeWithContentDescription(festival.name, substring = true).assertExists()
    }

    @Test
    fun anEventsNameStaysUnspoken_whenTheStyleDoesNotDescribeIt() = runComposeUiTest {
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = policyWith(festival),
                secondaryDateLocale = null,
                eventDisplayStyle = NepaliEventDisplayStyle.Default.copy(describe = false)
            )
        }

        onAllNodesWithText(festival.name, substring = true).assertCountEquals(0)
    }

    @Test
    fun aDayCarryingMoreEventsThanItsDotsCanShow_stillAnnouncesThemAll() = runComposeUiTest {
        val crowdedDay = (1..4).map { index ->
            NepaliCalendarEvent(
                date = EventDay,
                name = "Programme $index",
                kind = NepaliEventKind.Observance,
                id = "programme-$index"
            )
        }
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = policyWith(*crowdedDay.toTypedArray()),
                secondaryDateLocale = null,
                eventDisplayStyle = NepaliEventDisplayStyle.Default.copy(
                    indicateKinds = setOf(NepaliEventKind.Observance)
                )
            )
        }

        // The cell draws three dots at most, exactly as a picker does, and the names it cannot
        // draw are still spoken.
        crowdedDay.forEach { event ->
            onNodeWithContentDescription(event.name, substring = true).assertExists()
        }
    }

    @Test
    fun anAppsOwnMarks_areSpokenAfterThePolicys() = runComposeUiTest {
        setContent {
            val ownMarks = NepaliDatePickerDefaults.dayDecorator(
                markers = emptyMap(),
                descriptions = mapOf(EventDay to "Team offsite")
            )
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = policyWith(festival),
                secondaryDateLocale = null,
                dayDecorator = ownMarks
            )
        }

        onNodeWithContentDescription("${festival.name}, Team offsite", substring = true)
            .assertExists()
    }

    @Test
    fun aClosingEvent_makesTheDayNonWorking() = runComposeUiTest {
        var status: NepaliDayStatus? = null
        val closure = NepaliCalendarEvent(
            date = EventDay,
            name = "Offices closed",
            kind = NepaliEventKind.GovernmentPublic
        )
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = policyWith(closure),
                secondaryDateLocale = null,
                onDayClick = { _, dayStatus -> status = dayStatus }
            )
        }

        onNodeWithContentDescription(closure.name, substring = true).performClick()

        runOnIdle {
            val reported = assertNotNull(status)
            assertTrue(reported.isNonWorking)
            assertEquals(NepaliEventKind.GovernmentPublic, reported.primaryKind)
        }
    }

    @Test
    fun anObservance_leavesTheDayWorking() = runComposeUiTest {
        var status: NepaliDayStatus? = null
        val programme = NepaliCalendarEvent(
            date = EventDay,
            name = "School programme",
            kind = NepaliEventKind.Observance
        )
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = policyWith(programme),
                secondaryDateLocale = null,
                onDayClick = { _, dayStatus -> status = dayStatus }
            )
        }

        onNodeWithContentDescription(programme.name, substring = true).performClick()

        runOnIdle {
            val reported = assertNotNull(status)
            assertTrue(!reported.isNonWorking)
            assertEquals(listOf(programme.name), reported.names)
        }
    }
}
