/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DateArithmeticTests {

    private val calendarModel = NepaliCalendarModel()

    // ── getNepaliCalendarAfterAdditionOrSubtraction ───────────────────────────

    @Test
    fun addZeroDays_returnsSameCalendar() {
        val expected = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 5, 24, 0
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun addOneDay_advancesByOne() {
        val expected = NepaliDateConverter.getNepaliCalendar(2081, 5, 25)
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 5, 24, 1
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun subtractOneDay_movesBackByOne() {
        val expected = NepaliDateConverter.getNepaliCalendar(2081, 5, 23)
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 5, 24, -1
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun addDays_crossesIntoNextMonth() {
        // 2081 month 5 has 31 days, so +10 from day 25 ⇒ month 6 day 4.
        val daysInMonth5 = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 5)
        assertEquals(31, daysInMonth5)
        val expected = NepaliDateConverter.getNepaliCalendar(2081, 6, 4)
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 5, 25, 10
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun subtractDays_crossesIntoPreviousMonth() {
        val expected = NepaliDateConverter.getNepaliCalendar(2081, 4, 25)
        val daysInMonth4 = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 4)
        // Subtracting from day 5 of month 5 by 12 should land at month 4 day (totalDays4 - 7).
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 5, 5, -(5 + (daysInMonth4 - 25))
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun addDays_crossesYearBoundary() {
        // Last day of Nepali year 2081 + 1 should land on 2082-1-1.
        val last = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 12)
        val expected = NepaliDateConverter.getNepaliCalendar(2082, 1, 1)
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 12, last, 1
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun subtractDays_crossesYearBoundary() {
        // 2082-1-1 minus 1 day should land on the last day of 2081-12.
        val lastOfPrevYear = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 12)
        val expected = NepaliDateConverter.getNepaliCalendar(2081, 12, lastOfPrevYear)
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2082, 1, 1, -1
        )
        assertEquals(expected, adjusted)
    }

    @Test
    fun addDays_largePositiveSpanningMultipleYears() {
        // Should match the same calendar reached via convertEnglishToNepali round-trip.
        val englishStart = NepaliDateConverter.convertNepaliToEnglish(2081, 5, 24)
        val expectedEnglishDays = 1000
        val adjusted = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 5, 24, expectedEnglishDays
        )
        val computedEnglish = NepaliDateConverter.convertNepaliToEnglish(
            adjusted.year, adjusted.month, adjusted.dayOfMonth
        )
        val diff = NepaliDateConverter.getEnglishDaysInBetween(
            SimpleDate(englishStart.year, englishStart.month, englishStart.dayOfMonth),
            SimpleDate(computedEnglish.year, computedEnglish.month, computedEnglish.dayOfMonth)
        )
        assertEquals(expectedEnglishDays, diff)
    }

    @Test
    fun addDays_invalidYearThrows() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(1900, 1, 1, 5)
        }
    }

    @Test
    fun addDays_invalidMonthThrows() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2081, 13, 1, 5)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2081, 0, 1, 5)
        }
    }

    // ── plus / minus months ──────────────────────────────────────────────────

    @Test
    fun plusNepaliMonths_zeroMonths_returnsSameMonth() {
        val start = NepaliDateConverter.getNepaliCalendar(2081, 5, 1).toNepaliMonthCalendar()
        val result = calendarModel.plusNepaliMonths(start, 0)
        assertEquals(start, result)
    }

    @Test
    fun plusNepaliMonths_thirteenMonths_crossesYearAndKeepsMonthOne() {
        val start = NepaliDateConverter.getNepaliCalendar(2081, 1, 1).toNepaliMonthCalendar()
        val result = calendarModel.plusNepaliMonths(start, 13)
        assertEquals(2082, result.year)
        assertEquals(2, result.month)
    }

    @Test
    fun minusNepaliMonths_oneMonthFromJanuary_landsInPreviousYearMonthTwelve() {
        val start = NepaliDateConverter.getNepaliCalendar(2081, 1, 1).toNepaliMonthCalendar()
        val result = calendarModel.minusNepaliMonths(start, 1)
        assertEquals(2080, result.year)
        assertEquals(12, result.month)
    }

    @Test
    fun plusNepaliMonths_twelveMonths_landsOnSameMonthNextYear() {
        val start = NepaliDateConverter.getNepaliCalendar(2081, 7, 1).toNepaliMonthCalendar()
        val result = calendarModel.plusNepaliMonths(start, 12)
        assertEquals(2082, result.year)
        assertEquals(7, result.month)
    }

    @Test
    fun plusNepaliMonths_negativeCountEquivalentToMinus() {
        val start = NepaliDateConverter.getNepaliCalendar(2081, 5, 1).toNepaliMonthCalendar()
        val viaPlus = calendarModel.plusNepaliMonths(start, -3)
        val viaMinus = calendarModel.minusNepaliMonths(start, 3)
        assertEquals(viaMinus, viaPlus)
    }

    // ── days between ─────────────────────────────────────────────────────────

    @Test
    fun nepaliDaysInBetween_sameDay_returnsZero() {
        val d = SimpleDate(2081, 5, 24)
        assertEquals(0, NepaliDateConverter.getNepaliDaysInBetween(d, d))
    }

    @Test
    fun nepaliDaysInBetween_consecutiveDays_returnsOne() {
        val start = SimpleDate(2081, 5, 24)
        val end = SimpleDate(2081, 5, 25)
        assertEquals(1, NepaliDateConverter.getNepaliDaysInBetween(start, end))
        assertEquals(-1, NepaliDateConverter.getNepaliDaysInBetween(end, start))
    }

    @Test
    fun nepaliDaysInBetween_acrossYearBoundary_matchesEnglishDaysInBetween() {
        val nepStart = SimpleDate(2080, 12, 1)
        val nepEnd = SimpleDate(2081, 2, 1)
        val englishStart = NepaliDateConverter.convertNepaliToEnglish(
            nepStart.year, nepStart.month, nepStart.dayOfMonth
        )
        val englishEnd = NepaliDateConverter.convertNepaliToEnglish(
            nepEnd.year, nepEnd.month, nepEnd.dayOfMonth
        )
        val englishDiff = NepaliDateConverter.getEnglishDaysInBetween(
            SimpleDate(englishStart.year, englishStart.month, englishStart.dayOfMonth),
            SimpleDate(englishEnd.year, englishEnd.month, englishEnd.dayOfMonth)
        )
        assertEquals(
            englishDiff,
            NepaliDateConverter.getNepaliDaysInBetween(nepStart, nepEnd)
        )
    }

    @Test
    fun englishDaysInBetween_acrossLeapYear_includesExtraDay() {
        val start = SimpleDate(2023, 3, 1)
        val end = SimpleDate(2024, 3, 1)
        // Crossing Feb 29, 2024 → 366 days, not 365.
        assertEquals(366, NepaliDateConverter.getEnglishDaysInBetween(start, end))
    }

    @Test
    fun nepaliDaysInBetween_invalidStartYear_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliDaysInBetween(
                SimpleDate(1969, 1, 1),
                SimpleDate(2081, 1, 1)
            )
        }
    }

    @Test
    fun nepaliDaysInBetween_invalidEndYear_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliDaysInBetween(
                SimpleDate(2081, 1, 1),
                SimpleDate(2101, 1, 1)
            )
        }
    }

    @Test
    fun nepaliDaysInBetween_swapStartEnd_negatesResult() {
        val start = SimpleDate(2070, 3, 15)
        val end = SimpleDate(2081, 6, 12)
        val forward = NepaliDateConverter.getNepaliDaysInBetween(start, end)
        val backward = NepaliDateConverter.getNepaliDaysInBetween(end, start)
        assertEquals(forward, -backward)
        assertTrue(forward > 0)
    }
}
