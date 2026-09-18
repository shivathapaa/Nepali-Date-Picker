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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * What a tap on a borrowed day does, and what the grid shows once the fill is on.
 *
 * Shrawan 2083 is the month under test throughout: it is the reference screenshot's month, it needs
 * all six rows, and its neighbours are Asar (32 days, so "32" can only be a borrowed number) and
 * Bhadra.
 */
class NepaliAdjacentMonthDaysTest {

    private val shrawan = SimpleDate(2083, 4, 15)

    @Test
    fun withoutTheFlag_theNeighboursAreNotShown() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                )
            )
        }

        // 32 is Asar's last day. Shrawan has 31, so the number can only come from the neighbour.
        onAllNodesWithText("32").assertCountEquals(0)
    }

    @Test
    fun withTheFlag_theNeighboursAreShown() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText("32").onFirst().assertIsDisplayed()
    }

    @Test
    fun clickingALeadingDay_selectsItAndPagesBack() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = shrawan,
                locale = TestLocale
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        runOnIdle { assertEquals(4, state.displayedMonthCalendar.month) }
        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().performClick()

        runOnIdle {
            assertEquals(borrowed.toSimpleDate(), state.selectedDate?.toSimpleDate())
            assertEquals(2083, state.displayedMonthCalendar.year)
            assertEquals(3, state.displayedMonthCalendar.month)
        }
    }

    @Test
    fun clickingATrailingDay_selectsItAndPagesForward() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.lastBorrowedDay(adapter.monthOf(2083, 4))
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = shrawan,
                locale = TestLocale
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        // Shrawan's grid ends with the head of Bhadra, so its last cell is the neighbour's.
        onAllNodesWithText(borrowed.dayOfMonth.toString()).onLast().performClick()

        runOnIdle {
            assertEquals(borrowed.toSimpleDate(), state.selectedDate?.toSimpleDate())
            assertEquals(5, state.displayedMonthCalendar.month)
        }
    }

    @Test
    fun clickingAnOwnMonthDay_selectsWithoutPaging() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = shrawan,
                locale = TestLocale
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        // Shrawan's grid borrows Asar 28 to 32 and Bhadra 1 to 6, so 15 can only be its own.
        onAllNodesWithText("15").onFirst().performClick()

        runOnIdle {
            assertEquals(SimpleDate(2083, 4, 15), state.selectedDate?.toSimpleDate())
            assertEquals(4, state.displayedMonthCalendar.month, "the grid stays where it was")
        }
    }

    @Test
    fun clickingATrailingDayOfChaitra_pagesIntoTheNextYear() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        // Chaitra 2081 does not end on a week boundary, so it borrows from Baisakh 2082.
        val borrowed = adapter.lastBorrowedDay(adapter.monthOf(2081, 12))
        assertEquals(2082, borrowed.year)
        assertEquals(1, borrowed.month)

        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2081, 12, 1),
                locale = TestLocale
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onLast().performClick()

        runOnIdle {
            assertEquals(borrowed.toSimpleDate(), state.selectedDate?.toSimpleDate())
            assertEquals(2082, state.displayedMonthCalendar.year)
            assertEquals(1, state.displayedMonthCalendar.month)
        }
    }

    @Test
    fun clickingALeadingDayOfBaisakh_pagesIntoThePreviousYear() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2082, 1))
        assertEquals(2081, borrowed.year)
        assertEquals(12, borrowed.month)

        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2082, 1, 1),
                locale = TestLocale
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().performClick()

        runOnIdle {
            assertEquals(borrowed.toSimpleDate(), state.selectedDate?.toSimpleDate())
            assertEquals(2081, state.displayedMonthCalendar.year)
            assertEquals(12, state.displayedMonthCalendar.month)
        }
    }

    @Test
    fun anUnselectableAdjacentDay_neitherSelectsNorPages() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = shrawan,
                locale = TestLocale,
                nepaliSelectableDates = object : NepaliSelectableDates {
                    override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
                        customCalendar.month == 4
                }
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        onAllNodesWithText("32").onFirst().performClick()

        runOnIdle {
            assertNull(state.selectedDate, "a disabled cell does not select")
            assertEquals(4, state.displayedMonthCalendar.month, "and does not page either")
        }
    }

    @Test
    fun atTheEdgeOfTheYearRange_thereIsNothingToBorrow() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 1, 1),
                    yearRange = 2083..2084,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        // Baisakh 2083 is the first month of this range, and Baisakh has 31 days, so a "32" on
        // screen could only have been borrowed from a Chaitra the picker does not cover.
        onAllNodesWithText("32").assertCountEquals(0)
    }

    @Test
    fun theGregorianGrid_pagesTheSameWay() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2026, 9))
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2083, 6, 1),
                locale = TestLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(state = state, showAdjacentMonthDays = true)
        }

        // BS 2083-06-01 is AD 2026-09-17, so the grid opens on September and borrows from August.
        runOnIdle { assertEquals(9, state.displayedMonthCalendar.month) }
        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().performClick()

        runOnIdle {
            assertEquals(8, state.displayedMonthCalendar.month)
            assertEquals(2026, state.displayedMonthCalendar.year)
            assertEquals(borrowed, state.selectedEnglishDate)
        }
    }

    @Test
    fun theDualDateGrid_showsBothNumbersOnABorrowedDay() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale,
                showAdjacentMonthDays = true
            )
        }

        // Asar 32 of 2083 is AD 2026-07-16, so the borrowed cell carries a small 16 as well.
        onAllNodesWithText("32").onFirst().assertIsDisplayed()
        onAllNodesWithText("16").onFirst().assertIsDisplayed()
    }

    @Test
    fun theRangePicker_pagesFromABorrowedDayToo() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialDisplayedMonth = shrawan,
                locale = TestLocale
            )
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().performClick()

        runOnIdle {
            assertEquals(borrowed.toSimpleDate(), state.selectedStartNepaliDate?.toSimpleDate())
            assertEquals(3, state.displayedMonthCalendar.month)
        }
    }

    @Test
    fun aBorrowedDayThatIsTheSelection_readsAsSelected() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = borrowed.toSimpleDate(),
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().assertIsSelected()
        // The same number elsewhere in the grid is a different day and stays unselected.
        onAllNodesWithText("15").onFirst().assertIsNotSelected()
    }

    @Test
    fun switchingCalendar_keepsTheFillAndTheSelection() = runComposeUiTest {
        val english = NepaliDatePickerLang.ENGLISH
        val gregorian = adapterFor(CalendarSystem.GREGORIAN)
        val borrowedInSeptember = gregorian.firstBorrowedDay(gregorian.monthOf(2026, 9))

        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                locale = TestLocale
            )
            NepaliDatePicker(
                state = state,
                showCalendarSystemToggle = true,
                showAdjacentMonthDays = true
            )
        }

        val before = runOnIdle { state.selectedDate }
        onNodeWithContentDescription(english.switchToGregorianContentDescription).performClick()

        runOnIdle {
            assertEquals(CalendarSystem.GREGORIAN, state.displayedCalendarSystem)
            assertEquals(before, state.selectedDate, "the selection survives the switch")
            assertEquals(9, state.displayedMonthCalendar.month)
        }
        // BS 2083-06-01 is AD 2026-09-17, so the Gregorian grid opens on September and still fills.
        onAllNodesWithText(borrowedInSeptember.dayOfMonth.toString()).onFirst().assertIsDisplayed()

        onNodeWithContentDescription(english.switchToBikramSambatContentDescription).performClick()
        runOnIdle {
            assertEquals(CalendarSystem.BIKRAM_SAMBAT, state.displayedCalendarSystem)
            assertEquals(before, state.selectedDate)
            assertEquals(6, state.displayedMonthCalendar.month, "and the month does not drift")
        }
    }

    @Test
    fun switchingCalendarRepeatedly_doesNotDriftTheMonth() = runComposeUiTest {
        val english = NepaliDatePickerLang.ENGLISH
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = shrawan,
                locale = TestLocale
            )
            NepaliDatePicker(
                state = state,
                showCalendarSystemToggle = true,
                showAdjacentMonthDays = true
            )
        }

        val firstBikramSambatMonth = runOnIdle { state.displayedMonthCalendar }
        repeat(3) {
            onNodeWithContentDescription(english.switchToGregorianContentDescription).performClick()
            onNodeWithContentDescription(english.switchToBikramSambatContentDescription)
                .performClick()
        }

        runOnIdle {
            assertEquals(
                firstBikramSambatMonth,
                state.displayedMonthCalendar,
                "the borrowed days must not move the anchor the switch reads"
            )
        }
    }

    @Test
    fun theVerticalRangePicker_fillsItsMonthsToo() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        setContent {
            NepaliDateRangePicker(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showMonthsVertically = true,
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().assertIsDisplayed()
    }

    @Test
    fun theEnglishDateVariant_forwardsTheFlag() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        setContent {
            NepaliDatePickerWithEnglishDate(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().assertIsDisplayed()
    }

    @Test
    fun theDockedPicker_forwardsTheFlag() = runComposeUiTest {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        setContent {
            NepaliDatePickerDocked(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        onNodeWithContentDescription(
            NepaliDatePickerLang.ENGLISH.switchToCalendarModeContentDescription
        ).performClick()
        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst().assertIsDisplayed()
    }

    @Test
    fun aBorrowedDay_announcesThatItMovesTheGrid() = runComposeUiTest {
        val english = NepaliDatePickerLang.ENGLISH
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val borrowed = adapter.firstBorrowedDay(adapter.monthOf(2083, 4))
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst()
            .assertContentDescriptionContains(
                english.adjacentMonthDayContentDescription,
                substring = true
            )
        // The month name is still in there, so the listener knows which month it moves to.
        onAllNodesWithText(borrowed.dayOfMonth.toString()).onFirst()
            .assertContentDescriptionContains("Asar", substring = true)
    }

    @Test
    fun anOwnMonthDay_saysNothingAboutMovingTheGrid() = runComposeUiTest {
        val english = NepaliDatePickerLang.ENGLISH
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showAdjacentMonthDays = true
            )
        }

        onAllNodesWithText("15").onFirst().assert(
            hasContentDescription(
                english.adjacentMonthDayContentDescription,
                substring = true
            ).not()
        )
    }

    @Test
    fun theRangePicker_withoutTheFlag_showsNoNeighbours() = runComposeUiTest {
        setContent {
            NepaliDateRangePicker(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = shrawan,
                    locale = TestLocale
                ),
                showMonthsVertically = false
            )
        }

        onAllNodesWithText("32").assertCountEquals(0)
    }
}
