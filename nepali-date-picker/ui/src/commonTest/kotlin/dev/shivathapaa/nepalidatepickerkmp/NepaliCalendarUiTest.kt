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
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Width a calendar is drawn at in these tests, wide enough for seven columns of day numbers. */
private val CalendarTestWidth = 360.dp

/**
 * What the calendar puts on screen and what a tap on it does: the month it opens on, the day it
 * picks, the status it reports, and the way it moves between months.
 */
class NepaliCalendarUiTest {

    private val language = CalendarTestLocale.language

    /** The Gregorian half of a dual-date cell's description. */
    private fun secondaryDescriptionOf(date: SimpleDate): String {
        val english = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = date.year,
            nepaliMM = date.month,
            nepaliDD = date.dayOfMonth
        )
        return NepaliDateConverter.formatEnglishDate(
            customCalendar = english,
            locale = CalendarTestLocale.copy(dateFormat = NepaliDateFormatStyle.LONG)
        )
    }

    /** The full date a cell announces, which is how a test finds one day among forty-two. */
    private fun descriptionOf(date: SimpleDate): String = NepaliDateConverter.formatNepaliDate(
        customCalendar = NepaliDateConverter.getNepaliCalendar(
            nepaliYYYY = date.year,
            nepaliMM = date.month,
            nepaliDD = date.dayOfMonth
        ),
        locale = CalendarTestLocale.copy(dateFormat = NepaliDateFormatStyle.FULL)
    )

    @Test
    fun calendar_opensOnTheMonthItIsGiven() = runComposeUiTest {
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null
            )
        }

        onNodeWithText("Asoj 2082").assertExists()
    }

    @Test
    fun tappingADay_picksItAndReportsItsStatus() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        var clicked: Pair<CustomCalendar, NepaliDayStatus>? = null
        // Asoj 2082 starts on a Wednesday, so its first Saturday is the fourth.
        val saturday = SimpleDate(2082, 6, 4)
        setContent {
            state = rememberNepaliCalendarState(
                initialDisplayedMonth = CalendarTestMonth,
                locale = CalendarTestLocale
            )
            NepaliCalendar(
                state = state,
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null,
                onDayClick = { day, status -> clicked = day to status }
            )
        }

        onNodeWithContentDescription(descriptionOf(saturday), substring = true).performClick()

        runOnIdle {
            assertEquals(saturday, state.selectedDate?.toSimpleDate())
            val reported = assertNotNull(clicked)
            assertEquals(saturday, reported.first.toSimpleDate())
            // The default policy keeps Nepal's office week, so its Saturdays are days off.
            assertTrue(reported.second.isWeeklyOff)
            assertTrue(reported.second.events.isEmpty())
        }
    }

    @Test
    fun aWorkingDay_reportsNeitherAWeeklyOffNorAnEvent() = runComposeUiTest {
        var status: NepaliDayStatus? = null
        val sunday = SimpleDate(2082, 6, 5)
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null,
                onDayClick = { _, dayStatus -> status = dayStatus }
            )
        }

        onNodeWithContentDescription(descriptionOf(sunday), substring = true).performClick()

        runOnIdle {
            val reported = assertNotNull(status)
            assertTrue(!reported.isWeeklyOff)
            assertTrue(!reported.isNonWorking)
        }
    }

    @Test
    fun theNextMonthArrow_movesTheCalendarOn() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        setContent {
            state = rememberNepaliCalendarState(
                initialDisplayedMonth = CalendarTestMonth,
                locale = CalendarTestLocale
            )
            NepaliCalendar(
                state = state,
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null
            )
        }

        onNodeWithContentDescription(language.nextMonthContentDescription).performClick()

        runOnIdle { assertEquals(7, state.displayedMonth.month) }
    }

    @Test
    fun theTodayButton_returnsToThisMonth() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        val today = NepaliDateConverter.todayNepaliSimpleDate
        setContent {
            state = rememberNepaliCalendarState(
                initialDisplayedMonth = SimpleDate(today.year - 1, today.month, 1),
                locale = CalendarTestLocale
            )
            NepaliCalendar(
                state = state,
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null
            )
        }

        onNodeWithText(language.today).performClick()

        runOnIdle {
            assertEquals(today.year, state.displayedMonth.year)
            assertEquals(today.month, state.displayedMonth.month)
        }
    }

    @Test
    fun tappingANeighbouringMonthsDay_picksItAndFollowsIt() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        // Asoj 2082 opens on a Wednesday, so the three days before it come from Bhadra.
        val previousMonthDay = SimpleDate(2082, 5, 31)
        setContent {
            state = rememberNepaliCalendarState(
                initialDisplayedMonth = CalendarTestMonth,
                locale = CalendarTestLocale
            )
            NepaliCalendar(
                state = state,
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null,
                showAdjacentMonthDays = true
            )
        }

        onNodeWithContentDescription(descriptionOf(previousMonthDay), substring = true)
            .performClick()

        runOnIdle {
            assertEquals(previousMonthDay, state.selectedDate?.toSimpleDate())
            assertEquals(5, state.displayedMonth.month)
        }
    }

    @Test
    fun aDualDateCell_announcesBothDates() = runComposeUiTest {
        val day = SimpleDate(2082, 6, 5)
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth)
            )
        }

        onNodeWithContentDescription(secondaryDescriptionOf(day), substring = true).assertExists()
    }

    @Test
    fun withoutASecondaryLocale_aCellAnnouncesOneDateOnly() = runComposeUiTest {
        val day = SimpleDate(2082, 6, 5)
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null
            )
        }

        onNodeWithContentDescription(descriptionOf(day), substring = true).assertExists()
        onAllNodesWithContentDescription(secondaryDescriptionOf(day), substring = true)
            .assertCountEquals(0)
    }

    @Test
    fun theCalendarSwitch_movesTheGridToTheOtherCalendar() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        setContent {
            state = rememberNepaliCalendarState(
                initialDisplayedMonth = CalendarTestMonth,
                locale = CalendarTestLocale
            )
            NepaliCalendar(
                state = state,
                modifier = Modifier.width(CalendarTestWidth),
                showCalendarSystemToggle = true
            )
        }

        onNodeWithContentDescription(language.switchToGregorianContentDescription).performClick()

        runOnIdle {
            assertEquals(CalendarSystem.GREGORIAN, state.displayedCalendarSystem)
            assertEquals(
                CalendarSystem.GREGORIAN,
                state.displayedMonthCalendar.calendarSystem
            )
        }
    }

    @Test
    fun theCalendarSwitch_staysClearOfTheMonthRow() = runComposeUiTest {
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                showCalendarSystemToggle = true
            )
        }

        val toggle = onNodeWithContentDescription(language.switchToGregorianContentDescription)
            .getUnclippedBoundsInRoot()
        val nextMonth = onNodeWithContentDescription(language.nextMonthContentDescription)
            .getUnclippedBoundsInRoot()

        // The switch sits on its own line above the month row, whole rather than squeezed against
        // the arrows, and inside the calendar's own width.
        assertTrue(
            toggle.bottom <= nextMonth.top,
            "The switch overlaps the month row: it ends at ${toggle.bottom} and the row starts at ${nextMonth.top}"
        )
        assertTrue(
            toggle.right <= CalendarTestWidth,
            "The switch runs past the calendar's edge, ending at ${toggle.right}"
        )
        assertTrue(
            (toggle.right - toggle.left).value > 0f,
            "The switch was measured with no width at all"
        )
    }

    @Test
    fun aPolicyWithoutAWeeklyRule_leavesSaturdayAWorkingDay() = runComposeUiTest {
        var status: NepaliDayStatus? = null
        val saturday = SimpleDate(2082, 6, 4)
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                policy = NepaliCalendarPolicy(weeklyOffDays = emptySet()),
                secondaryDateLocale = null,
                onDayClick = { _, dayStatus -> status = dayStatus }
            )
        }

        onNodeWithContentDescription(descriptionOf(saturday), substring = true).performClick()

        runOnIdle { assertTrue(!assertNotNull(status).isNonWorking) }
    }
}
