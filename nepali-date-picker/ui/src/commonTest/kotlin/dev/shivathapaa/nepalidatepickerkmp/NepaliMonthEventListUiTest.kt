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

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * What the month's list writes out: every event of the visible month, a span as one line, and the
 * event an app gets back when a line is tapped.
 */
class NepaliMonthEventListUiTest {

    private val language = CalendarTestLocale.language

    private fun event(day: Int, name: String, id: String? = null) = NepaliCalendarEvent(
        date = SimpleDate(2082, 6, day),
        name = name,
        kind = NepaliEventKind.Religious,
        id = id
    )

    private fun policyOf(events: List<NepaliCalendarEvent>) =
        NepaliCalendarPolicy(provider = ListEventProvider(events))

    @Test
    fun theMonthsEvents_areListedInDayOrder() = runComposeUiTest {
        val policy = policyOf(listOf(event(9, "Ninth"), event(2, "Second")))
        setContent {
            NepaliMonthEventList(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                policy = policy
            )
        }

        onNodeWithText("Second").assertExists()
        onNodeWithText("Ninth").assertExists()
    }

    @Test
    fun aSpan_isOneLineCarryingItsRange() = runComposeUiTest {
        val policy = policyOf(event(17, "Dashain", id = "dashain").spanningDays(4))
        setContent {
            NepaliMonthEventList(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                policy = policy
            )
        }

        onAllNodesWithText("Dashain").assertCountEquals(1)
        onNodeWithText("17 - 20", substring = true).assertExists()
    }

    @Test
    fun tappingALine_deliversTheEventBehindIt() = runComposeUiTest {
        var tapped: NepaliCalendarEvent? = null
        val policy = policyOf(listOf(event(9, "Ghatasthapana", id = "ghatasthapana")))
        setContent {
            NepaliMonthEventList(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                policy = policy,
                onEventClick = { tapped = it }
            )
        }

        onNodeWithText("Ghatasthapana").performClick()

        runOnIdle {
            val event = assertNotNull(tapped)
            assertEquals("ghatasthapana", event.id)
            assertEquals(SimpleDate(2082, 6, 9), event.date)
        }
    }

    @Test
    fun aMonthWithNothingNamed_saysSo() = runComposeUiTest {
        setContent {
            NepaliMonthEventList(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                )
            )
        }

        onNodeWithText(language.noEventsInMonthText).assertExists()
    }

    @Test
    fun anEmptyMonthsOwnContent_replacesTheDefaultLine() = runComposeUiTest {
        setContent {
            NepaliMonthEventList(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                emptyContent = { Text("Nothing planned yet") }
            )
        }

        onNodeWithText("Nothing planned yet").assertExists()
        onAllNodesWithText(language.noEventsInMonthText).assertCountEquals(0)
    }

    @Test
    fun theListFollowsTheCalendar_whenItIsPaged() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        val policy = policyOf(
            listOf(
                event(9, "This month"),
                NepaliCalendarEvent(
                    date = SimpleDate(2082, 7, 9),
                    name = "Next month",
                    kind = NepaliEventKind.Religious
                )
            )
        )
        setContent {
            state = rememberNepaliCalendarState(
                initialDisplayedMonth = CalendarTestMonth,
                locale = CalendarTestLocale
            )
            NepaliMonthEventList(state = state, policy = policy)
        }

        onNodeWithText("This month").assertExists()
        runOnIdle { state.displayedMonth = state.displayedMonth.copy(month = 7) }
        waitForIdle()

        onNodeWithText("Next month").assertExists()
        onAllNodesWithText("This month").assertCountEquals(0)
    }
}
