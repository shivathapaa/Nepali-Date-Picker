/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.daysInMonthMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Month geometry for the whole Bikram Sambat table, and the year bounds around it.
 *
 * The day-count table reaches one year further back than the supported range, so the year checks
 * here are what stop a month lookup from answering for a year no picker can page to.
 */
class NepaliMonthDetailsTests {

    @Test
    fun dayCountTable_reachesOneYearBelowTheSupportedRange() {
        assertTrue(
            daysInMonthMap.containsKey(NepaliCalendarDefaults.NepaliYearRange.first - 1),
            "The 1969 row is what the year guards below have to exclude"
        )
    }

    @Test
    fun getNepaliMonthCalendar_yearBelowSupportedRange_throwsEvenThoughTheTableHasTheRow() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliMonthCalendar(
                NepaliCalendarDefaults.NepaliYearRange.first - 1, 1
            )
        }
    }

    @Test
    fun getNepaliMonthCalendar_yearAboveSupportedRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliMonthCalendar(
                NepaliCalendarDefaults.NepaliYearRange.last + 1, 1
            )
        }
    }

    @Test
    fun getNepaliCalendar_yearBelowSupportedRange_throwsEvenThoughTheTableHasTheRow() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendar(
                NepaliCalendarDefaults.NepaliYearRange.first - 1, 1, 1
            )
        }
    }

    @Test
    fun getNepaliCalendar_yearAboveSupportedRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendar(
                NepaliCalendarDefaults.NepaliYearRange.last + 1, 1, 1
            )
        }
    }

    @Test
    fun getNepaliMonthCalendar_edgesOfTheSupportedRange_succeed() {
        val first = NepaliDateConverter.getNepaliMonthCalendar(
            NepaliCalendarDefaults.NepaliYearRange.first, 1
        )
        assertEquals(NepaliCalendarDefaults.NepaliYearRange.first, first.year)
        assertEquals(1, first.firstDayOfMonth)

        val last = NepaliDateConverter.getNepaliMonthCalendar(
            NepaliCalendarDefaults.NepaliYearRange.last, 12
        )
        assertEquals(NepaliCalendarDefaults.NepaliYearRange.last, last.year)
        assertEquals(12, last.month)
    }

    /**
     * Every month of every supported year, checked against the day-by-day walk rather than against
     * itself: the month's own geometry has to be the geometry its first and last days report.
     */
    @Test
    fun everyMonthInRange_geometryMatchesItsOwnFirstAndLastDay() {
        for (year in NepaliCalendarDefaults.NepaliYearRange) {
            for (month in 1..12) {
                val monthCalendar = NepaliDateConverter.getNepaliMonthCalendar(year, month)
                val totalDays = NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)

                assertEquals(
                    totalDays, monthCalendar.totalDaysInMonth,
                    "$year-$month total days disagree with the table"
                )
                assertTrue(
                    monthCalendar.firstDayOfMonth in 1..7,
                    "$year-$month firstDayOfMonth=${monthCalendar.firstDayOfMonth}"
                )
                assertTrue(
                    monthCalendar.lastDayOfMonth in 1..7,
                    "$year-$month lastDayOfMonth=${monthCalendar.lastDayOfMonth}"
                )
                assertEquals(
                    monthCalendar.firstDayOfMonth - 1,
                    monthCalendar.daysFromStartOfWeekToFirstOfMonth,
                    "$year-$month leading blank count disagrees with its first weekday"
                )

                val firstDay = NepaliDateConverter.getNepaliCalendar(year, month, 1)
                assertEquals(
                    monthCalendar.firstDayOfMonth, firstDay.dayOfWeek,
                    "$year-$month day 1 weekday disagrees with the month"
                )
                val lastDay = NepaliDateConverter.getNepaliCalendar(year, month, totalDays)
                assertEquals(
                    monthCalendar.lastDayOfMonth, lastDay.dayOfWeek,
                    "$year-$month last day weekday disagrees with the month"
                )
            }
        }
    }

    /**
     * Walking a day at a time across the whole table has to advance the weekday by exactly one,
     * which is the invariant the month anchors exist to preserve.
     */
    @Test
    fun weekdayAdvancesByOne_acrossEveryMonthBoundaryInRange() {
        var previous: Int? = null
        for (year in NepaliCalendarDefaults.NepaliYearRange) {
            for (month in 1..12) {
                val monthCalendar = NepaliDateConverter.getNepaliMonthCalendar(year, month)
                previous?.let { last ->
                    assertEquals(
                        (last % 7) + 1, monthCalendar.firstDayOfMonth,
                        "Weekday chain breaks entering $year-$month"
                    )
                }
                previous = monthCalendar.lastDayOfMonth
            }
        }
    }

    @Test
    fun getNepaliCalendar_dayPastTheMonthLength_throws() {
        for (year in listOf(1970, 2000, 2082, 2100)) {
            for (month in 1..12) {
                val totalDays = NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)
                assertFailsWith<IllegalArgumentException>(
                    "$year-$month should reject day ${totalDays + 1}"
                ) {
                    NepaliDateConverter.getNepaliCalendar(year, month, totalDays + 1)
                }
            }
        }
    }

    /**
     * A day number below one is not a day of the month, so it is rejected rather than carried into
     * a calendar whose derived fields would then describe nothing.
     */
    @Test
    fun getNepaliCalendar_dayBelowOne_throws() {
        for (dayOfMonth in listOf(0, -1, -5, -32)) {
            assertFailsWith<IllegalArgumentException>("Day $dayOfMonth should be rejected") {
                NepaliDateConverter.getNepaliCalendar(2081, 5, dayOfMonth)
            }
        }
    }

    @Test
    fun getNepaliCalendar_dayBelowOne_throwsForEveryMonthOfASampleOfYears() {
        for (year in listOf(1970, 2000, 2082, 2100)) {
            for (month in 1..12) {
                assertFailsWith<IllegalArgumentException>("$year-$month should reject day 0") {
                    NepaliDateConverter.getNepaliCalendar(year, month, 0)
                }
            }
        }
    }

    /** Both calendars answer the same way at the bottom of a month. */
    @Test
    fun bothCalendars_rejectADayBelowOne() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliCalendar(2081, 5, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishCalendar(2024, 5, 0)
        }
    }

    @Test
    fun getNepaliCalendar_lastDayOfEveryMonthLength_isAccepted() {
        for (year in NepaliCalendarDefaults.NepaliYearRange) {
            for (month in 1..12) {
                val totalDays = NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)
                val calendar = NepaliDateConverter.getNepaliCalendar(year, month, totalDays)
                assertEquals(totalDays, calendar.dayOfMonth)
                assertEquals(totalDays, calendar.totalDaysInMonth)
            }
        }
    }

    @Test
    fun getNepaliDaysInBetween_spansTheWholeTable_andIsAntisymmetric() {
        val start = SimpleDate(NepaliCalendarDefaults.NepaliYearRange.first, 1, 1)
        val endYear = NepaliCalendarDefaults.NepaliYearRange.last
        val end = SimpleDate(endYear, 12, NepaliDateConverter.getTotalDaysInNepaliMonth(endYear, 12))

        val forward = NepaliDateConverter.getNepaliDaysInBetween(start, end)
        val expected = NepaliCalendarDefaults.NepaliYearRange.sumOf { year ->
            (1..12).sumOf { NepaliDateConverter.getTotalDaysInNepaliMonth(year, it) }
        } - 1
        assertEquals(expected, forward)
        assertEquals(-forward, NepaliDateConverter.getNepaliDaysInBetween(end, start))
    }
}
