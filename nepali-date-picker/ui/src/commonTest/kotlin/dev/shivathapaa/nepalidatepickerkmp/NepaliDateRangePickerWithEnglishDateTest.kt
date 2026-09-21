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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.formatSecondary
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.secondaryMonthLabel
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val DualLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
private val English = NepaliDatePickerLang.ENGLISH

/** The range picker that pairs every day with its counterpart in the other calendar. */
class NepaliDateRangePickerWithEnglishDateTest {

    private val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)

    /**
     * A dual-date cell draws two numbers, so matching on text alone can find the small Gregorian one
     * first. The full spoken date belongs to the cell and names exactly one day, in both calendars.
     */
    private fun descriptionOf(year: Int, month: Int, dayOfMonth: Int): String {
        val day = adapter.daysIn(adapter.monthOf(year, month), withSecondary = true)[dayOfMonth - 1]
        val displayed = adapter.format(
            day.displayed,
            DualLocale.copy(dateFormat = NepaliDateFormatStyle.FULL)
        )
        val secondary = assertNotNull(day.secondary).let {
            adapter.formatSecondary(
                secondaryDate = it,
                calendarModel = NepaliCalendarModel(DualLocale),
                locale = DualLocale.copy(dateFormat = NepaliDateFormatStyle.LONG)
            )
        }
        return "$displayed, $secondary"
    }

    @Test
    fun itRendersTheDaysOfTheDisplayedMonth() = runComposeUiTest {
        setContent {
            NepaliDateRangePickerWithEnglishDate(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 4),
                    locale = DualLocale
                ),
                englishDateLocale = DualLocale
            )
        }
        onNodeWithText(English.dateRangePickerTitle).assertExists()
        onAllNodesWithText("15").onFirst().assertExists()
    }

    @Test
    fun everyCellCarriesTheOtherCalendarSNumberToo() = runComposeUiTest {
        val month = adapter.monthOf(2083, 4)
        val secondary = assertNotNull(
            adapter.secondaryMonthLabel(
                days = adapter.daysIn(month, withSecondary = true),
                calendarModel = NepaliCalendarModel(DualLocale),
                language = NepaliDatePickerLang.ENGLISH
            )
        )

        setContent {
            NepaliDateRangePickerWithEnglishDate(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 4),
                    locale = DualLocale
                ),
                englishDateLocale = DualLocale
            )
        }

        // The second line under the year button names the Gregorian months Shrawan 2083 straddles.
        onNodeWithText(secondary).assertExists()
    }

    @Test
    fun aCellSpeaksBothOfItsDates() = runComposeUiTest {
        val day = adapter.daysIn(adapter.monthOf(2083, 4), withSecondary = true)[9]
        val gregorian = assertNotNull(day.secondary)
        setContent {
            NepaliDateRangePickerWithEnglishDate(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 4),
                    locale = DualLocale
                ),
                englishDateLocale = DualLocale
            )
        }

        // The small number is drawn but never spoken on its own, so the description carries it.
        onNodeWithContentDescription(descriptionOf(2083, 4, 10)).assertExists()
        assertTrue(
            descriptionOf(2083, 4, 10).contains(gregorian.dayOfMonth.toString()),
            "the Gregorian half of the cell has to reach a screen reader"
        )
    }

    @Test
    fun pickingBothEndsBuildsTheRange() = runComposeUiTest {
        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialDisplayedMonth = SimpleDate(2083, 4),
                locale = DualLocale
            )
            NepaliDateRangePickerWithEnglishDate(state = state, englishDateLocale = DualLocale)
        }

        onNodeWithContentDescription(descriptionOf(2083, 4, 10)).performClick()
        onNodeWithContentDescription(descriptionOf(2083, 4, 20)).performClick()

        runOnIdle {
            assertEquals(SimpleDate(2083, 4, 10), state.selectedStartNepaliDate?.toSimpleDate())
            assertEquals(SimpleDate(2083, 4, 20), state.selectedEndNepaliDate?.toSimpleDate())
            // The Gregorian halves are what this variant adds over the plain range picker.
            assertNotNull(state.selectedStartEnglishDate)
            assertNotNull(state.selectedEndEnglishDate)
        }
    }

    @Test
    fun itCanFillTheGridWithTheNeighbouringMonths() = runComposeUiTest {
        setContent {
            NepaliDateRangePickerWithEnglishDate(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 4),
                    locale = DualLocale
                ),
                englishDateLocale = DualLocale,
                showMonthsVertically = false,
                showAdjacentMonthDays = true
            )
        }
        // Shrawan 2083 borrows Asar 32, a day number nothing else in the grid reaches.
        onAllNodesWithText("32").onFirst().assertExists()
    }

    @Test
    fun theCalendarSwitchFlipsWhichCalendarLeads() = runComposeUiTest {
        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialSelectedStartNepaliDate = SimpleDate(2083, 6, 1),
                initialDisplayedMonth = SimpleDate(2083, 6),
                locale = DualLocale
            )
            NepaliDateRangePickerWithEnglishDate(
                state = state,
                englishDateLocale = DualLocale,
                showMonthsVertically = false,
                showCalendarSystemToggle = true
            )
        }

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        runOnIdle {
            assertEquals(CalendarSystem.GREGORIAN, state.displayedCalendarSystem)
            // Switching the display never moves the selection.
            assertEquals(SimpleDate(2083, 6, 1), state.selectedStartNepaliDate?.toSimpleDate())
        }
        onNodeWithText(English.englishDateRangePickerTitle).assertExists()
    }

    @Test
    fun aVerticalListStacksEveryMonthOfTheRange() = runComposeUiTest {
        setContent {
            NepaliDateRangePickerWithEnglishDate(
                state = rememberNepaliDateRangePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 4),
                    yearRange = 2083..2083,
                    locale = DualLocale
                ),
                englishDateLocale = DualLocale,
                showMonthsVertically = true
            )
        }
        // A vertical list shows month headers rather than the year button and arrows.
        val header = adapter.monthName(4, NepaliDatePickerLang.ENGLISH, NameFormat.FULL)
        assertTrue(
            onAllNodesWithText(header, substring = true).fetchSemanticsNodes().isNotEmpty(),
            "expected the stacked months to name themselves, looked for \"$header\""
        )
    }
}
