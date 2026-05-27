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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalendarPropertiesTests {

    // ── getTotalDaysInEnglishMonth: Gregorian leap-year rule ─────────────────

    @Test
    fun englishMonth_february_commonYear_has28Days() {
        assertEquals(28, NepaliDateConverter.getTotalDaysInEnglishMonth(2023, 2))
    }

    @Test
    fun englishMonth_february_divisibleBy4_has29Days() {
        assertEquals(29, NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 2))
    }

    @Test
    fun englishMonth_february_divisibleBy100ButNot400_has28Days() {
        // 1900 is divisible by 100 but not 400 → common year.
        assertEquals(28, NepaliDateConverter.getTotalDaysInEnglishMonth(1900, 2))
    }

    @Test
    fun englishMonth_february_divisibleBy400_has29Days() {
        assertEquals(29, NepaliDateConverter.getTotalDaysInEnglishMonth(2000, 2))
    }

    @Test
    fun englishMonth_30DayMonths_have30Days() {
        assertEquals(30, NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 4))
        assertEquals(30, NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 6))
        assertEquals(30, NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 9))
        assertEquals(30, NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 11))
    }

    @Test
    fun englishMonth_31DayMonths_have31Days() {
        listOf(1, 3, 5, 7, 8, 10, 12).forEach { month ->
            assertEquals(31, NepaliDateConverter.getTotalDaysInEnglishMonth(2024, month),
                "Month $month should have 31 days")
        }
    }

    @Test
    fun englishMonth_invalidMonth_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 13)
        }
    }

    // ── getTotalDaysInNepaliMonth: BS month length table ─────────────────────

    @Test
    fun nepaliMonth_min_returnsMappedValue() {
        // Year 1970, month 1 → 31 days per the embedded data table.
        assertEquals(31, NepaliDateConverter.getTotalDaysInNepaliMonth(1970, 1))
    }

    @Test
    fun nepaliMonth_max_returnsMappedValue() {
        // Year 2100, month 12 → 31 days.
        assertEquals(31, NepaliDateConverter.getTotalDaysInNepaliMonth(2100, 12))
    }

    @Test
    fun nepaliMonth_2082Month3_has32Days() {
        assertEquals(32, NepaliDateConverter.getTotalDaysInNepaliMonth(2082, 3))
    }

    @Test
    fun nepaliMonth_allMonthsInRange29To32() {
        for (year in 1970..2100) {
            for (month in 1..12) {
                val days = NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)
                assertTrue(days in 29..32,
                    "Year $year month $month has $days days — outside 29..32")
            }
        }
    }

    @Test
    fun nepaliYear_totalDaysAcrossAllMonths_isWithinExpectedRange() {
        for (year in 1970..2100) {
            val total = (1..12).sumOf { NepaliDateConverter.getTotalDaysInNepaliMonth(year, it) }
            assertTrue(total in 364..367,
                "Year $year total days = $total — expected 364..367")
        }
    }

    @Test
    fun nepaliMonth_yearOutsideMap_throws() {
        assertFailsWith<NoSuchElementException> {
            NepaliDateConverter.getTotalDaysInNepaliMonth(1500, 1)
        }
    }

    // ── getNepaliMonthCalendar ────────────────────────────────────────────────

    @Test
    fun getNepaliMonthCalendar_matchesGetNepaliCalendarFirstDay() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 1)
        val month = NepaliDateConverter.getNepaliMonthCalendar(2081, 5)
        assertEquals(cal.year, month.year)
        assertEquals(cal.month, month.month)
        assertEquals(cal.firstDayOfMonth, month.firstDayOfMonth)
        assertEquals(cal.lastDayOfMonth, month.lastDayOfMonth)
        assertEquals(cal.totalDaysInMonth, month.totalDaysInMonth)
    }

    @Test
    fun getNepaliMonthCalendar_firstDayOfMonthInRangeOneToSeven() {
        for (year in 2080..2090) {
            for (month in 1..12) {
                val m = NepaliDateConverter.getNepaliMonthCalendar(year, month)
                assertTrue(m.firstDayOfMonth in 1..7,
                    "Year $year month $month firstDayOfMonth=${m.firstDayOfMonth}")
                assertTrue(m.lastDayOfMonth in 1..7,
                    "Year $year month $month lastDayOfMonth=${m.lastDayOfMonth}")
            }
        }
    }

    @Test
    fun getNepaliMonthCalendar_consecutiveMonths_firstDayChain() {
        // Last day of month N should be one less than first day of month N+1 (mod 7).
        for (year in 2080..2085) {
            for (month in 1..11) {
                val current = NepaliDateConverter.getNepaliMonthCalendar(year, month)
                val next = NepaliDateConverter.getNepaliMonthCalendar(year, month + 1)
                val expectedNextFirst = (current.lastDayOfMonth % 7) + 1
                assertEquals(expectedNextFirst, next.firstDayOfMonth,
                    "Year $year month $month → $month+1 weekday chain broken")
            }
        }
    }

    // ── getNepaliCalendar / dayOfWeek / dayOfYear ────────────────────────────

    @Test
    fun getNepaliCalendar_firstDayOfYear_dayOfYearIsOne() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 1, 1)
        assertEquals(1, cal.dayOfYear)
    }

    @Test
    fun getNepaliCalendar_dayOfYear_increasesByOneEachDay() {
        var previous = NepaliDateConverter.getNepaliCalendar(2081, 1, 1)
        for (i in 1..30) {
            val next = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
                2081, 1, 1, i
            )
            assertEquals(previous.dayOfYear + 1, next.dayOfYear,
                "Day ${previous.dayOfYear} → ${next.dayOfYear} not monotonic")
            previous = next
        }
    }

    @Test
    fun getNepaliCalendar_lastDayOfYear_dayOfYearMatchesTotal() {
        val totalDays = (1..12).sumOf { NepaliDateConverter.getTotalDaysInNepaliMonth(2081, it) }
        val lastMonth12Day = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 12)
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 12, lastMonth12Day)
        assertEquals(totalDays, cal.dayOfYear)
    }

    @Test
    fun getNepaliCalendar_dayOfWeek_walksOneToSevenAndWraps() {
        var cal = NepaliDateConverter.getNepaliCalendar(2081, 1, 1)
        val firstWeekday = cal.dayOfWeek
        for (i in 1..21) {
            cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
                2081, 1, 1, i
            )
            val expected = ((firstWeekday - 1 + i) % 7) + 1
            assertEquals(expected, cal.dayOfWeek,
                "After $i days, weekday should be $expected (firstWeekday=$firstWeekday)")
        }
    }

    @Test
    fun getNepaliCalendar_invalidDayThrows() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendar(2081, 5, 50)
        }
    }

    @Test
    fun getNepaliCalendar_invalidMonth_throws() {
        // Mirrors the inconsistency exposed by DateArithmeticTests: month=13 walks
        // off the IntArray instead of triggering the elvis branch.
        val ex = kotlin.runCatching {
            NepaliDateConverter.getNepaliCalendar(2081, 13, 1)
        }.exceptionOrNull()
        assertTrue(ex != null)
    }
}
