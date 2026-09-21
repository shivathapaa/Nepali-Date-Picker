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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import kotlin.test.Test
import kotlin.test.assertEquals

private val EnglishLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
private val MarkedDate = SimpleDate(2081, 5, 15)
private const val MarkName = "Constitution Day"
private val MarkColor = Color(0xFFB3261E)
private const val SpanName = "Dashain"
private const val SpanLength = 5

/**
 * The decorator as the grid actually uses it: which cell it lands on when the displayed calendar is
 * not the one it is keyed by, and whether each surface that hosts a grid passes it along.
 */
class NepaliDayDecoratorGridTest {

    /** Marks one Bikram Sambat date, whatever calendar the grid happens to be showing. */
    private fun markOneDay(date: SimpleDate = MarkedDate) = NepaliDayDecorator { day ->
        if (day.date.year == date.year &&
            day.date.month == date.month &&
            day.date.dayOfMonth == date.dayOfMonth
        ) {
            NepaliDayDecoration(
                contentColor = MarkColor,
                indicators = listOf(MarkColor),
                contentDescription = MarkName
            )
        } else {
            null
        }
    }

    @Test
    fun aBikramSambatGridMarksExactlyOneDay() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = MarkedDate,
                    locale = EnglishLocale
                ),
                dayDecorator = markOneDay()
            )
        }

        assertEquals(1, markedCells())
    }

    @Test
    fun aGregorianGridMarksTheSameDay() = runComposeUiTest {
        // The cell draws a Gregorian number, but selection and decoration are both keyed by the
        // canonical Bikram Sambat date, so the mark has to land on the same real day.
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = MarkedDate,
                    locale = EnglishLocale,
                    initialCalendarSystem = CalendarSystem.GREGORIAN
                ),
                dayDecorator = markOneDay()
            )
        }

        assertEquals(1, markedCells(), "the Gregorian grid marks the same day, not another one")
    }

    @Test
    fun theFilledGridMarksABorrowedDayToo() = runComposeUiTest {
        // With the neighbouring months filling the edges, a marked day can appear twice: once in its
        // own month and once as a borrowed cell in the month beside it. Both are the same date.
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                ),
                showAdjacentMonthDays = true,
                dayDecorator = markOneDay(SimpleDate(2081, 5, 1))
            )
        }

        onNodeWithContentDescription(MarkName, substring = true).assertIsDisplayed()
    }

    @Test
    fun changingThePolicyRepaintsTheGrid() = runComposeUiTest {
        var marking by mutableStateOf(true)
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = MarkedDate,
                    locale = EnglishLocale
                ),
                dayDecorator = if (marking) markOneDay() else NepaliDayDecorator { null }
            )
        }

        assertEquals(1, markedCells())

        marking = false
        waitForIdle()

        assertEquals(0, markedCells(), "dropping the decorator clears the mark")
    }

    @Test
    fun theDualDateGridForwardsTheDecorator() = runComposeUiTest {
        setContent {
            NepaliDatePickerWithEnglishDate(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = MarkedDate,
                    locale = EnglishLocale
                ),
                dayDecorator = markOneDay()
            )
        }

        assertEquals(1, markedCells())
    }

    @Test
    fun theRangePickerForwardsTheDecorator() = runComposeUiTest {
        setContent {
            NepaliDateRangePicker(
                state = rememberNepaliDateRangePickerState(
                    initialSelectedStartNepaliDate = MarkedDate,
                    locale = EnglishLocale
                ),
                showMonthsVertically = false,
                dayDecorator = markOneDay()
            )
        }

        assertEquals(1, markedCells())
    }

    @Test
    fun theDockedPickerForwardsTheDecorator() = runComposeUiTest {
        setContent {
            NepaliDatePickerDocked(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = MarkedDate,
                    locale = EnglishLocale
                ),
                dayDecorator = markOneDay()
            )
        }

        onNodeWithContentDescription(
            NepaliDatePickerLang.ENGLISH.switchToCalendarModeContentDescription
        ).performClick()

        assertEquals(1, markedCells())
    }

    @Test
    fun theFieldsDialogForwardsTheDecorator() = runComposeUiTest {
        setContent {
            NepaliDateField(
                value = MarkedDate,
                onValueChange = {},
                locale = EnglishLocale,
                dayDecorator = markOneDay()
            )
        }

        onNodeWithContentDescription(NepaliDatePickerLang.ENGLISH.selectDateText).performClick()

        assertEquals(1, markedCells())
    }

    @Test
    fun aSpanMarksEveryDayItCoversInTheGrid() = runComposeUiTest {
        val span = NepaliCalendarEvent(
            date = SimpleDate(2081, 5, 10),
            name = SpanName,
            kind = NepaliEventKind.Religious
        ).spanningDays(SpanLength)

        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                ),
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = NepaliCalendarPolicy(
                        weeklyOffDays = emptySet(),
                        provider = spanProvider(span)
                    )
                )
            )
        }

        assertEquals(SpanLength, spanCells(), "one cell per day of the span, and no more")
    }

    @Test
    fun aSpanRunningIntoTheNextMonthMarksTheBorrowedCellsToo() = runComposeUiTest {
        // The days past the month end are the interesting half: they are drawn from the next
        // month's data, so the borrowed cells have to resolve to the span's own dates.
        val month = NepaliDateConverter.getNepaliMonthCalendar(2081, 5)
        val daysInThisMonth = 2
        val span = NepaliCalendarEvent(
            date = SimpleDate(2081, 5, month.totalDaysInMonth - (daysInThisMonth - 1)),
            name = SpanName,
            kind = NepaliEventKind.Religious
        ).spanningDays(SpanLength)
        // Only the trailing cells the grid has room for can show a borrowed day.
        val trailingCells = (7 - ((month.firstDayOfMonth - 1 + month.totalDaysInMonth) % 7)) % 7
        val borrowed = minOf(SpanLength - daysInThisMonth, trailingCells)

        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                ),
                showAdjacentMonthDays = true,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = NepaliCalendarPolicy(
                        weeklyOffDays = emptySet(),
                        provider = spanProvider(span)
                    )
                )
            )
        }

        assertEquals(daysInThisMonth + borrowed, spanCells())
    }

    /** A provider over the days one span expanded to, bucketed the way a real one would be. */
    private fun spanProvider(span: List<NepaliCalendarEvent>) = object : NepaliEventProvider {
        private val byYear = span.groupBy { it.date.year }.mapValues { (_, days) -> days.toSet() }

        override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.markedCells(): Int =
        onAllNodesWithContentDescription(MarkName, substring = true).fetchSemanticsNodes().size

    private fun androidx.compose.ui.test.ComposeUiTest.spanCells(): Int =
        onAllNodesWithContentDescription(SpanName, substring = true).fetchSemanticsNodes().size
}
