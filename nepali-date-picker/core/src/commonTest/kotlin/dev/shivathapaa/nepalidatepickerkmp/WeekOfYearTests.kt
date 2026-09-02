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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Coverage for the `weekOfYear` field that the private `calculateWeekOfYear` produces.
 *
 * The weekly fields only mean anything if the underlying weekday is right, so the day-of-week and
 * day-of-year are first pinned to sources outside this library (the Gregorian date via
 * kotlinx-datetime, and the raw month-length table). The `weekOfYear` invariants then rest on a
 * trusted weekday rather than on the calendar agreeing with itself.
 */
class WeekOfYearTests {

    // A spread across the supported range, including the boundary years and the years that reach
    // the 54-week maximum, used for the per-day (heavier) checks.
    private val sampleYears = listOf(1970, 1971, 2000, 2050, 2080, 2081, 2082, 2099, 2100)
    private val allYears = NepaliCalendarDefaults.NepaliYearRange

    private fun calendar(year: Int, month: Int, day: Int) =
        NepaliDateConverter.getNepaliCalendar(year, month, day)

    private fun daysInMonth(year: Int, month: Int) =
        NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)

    private inline fun eachDay(year: Int, action: (month: Int, day: Int) -> Unit) {
        for (month in 1..12) for (day in 1..daysInMonth(year, month)) action(month, day)
    }

    /**
     * The oracle the rest of the suite depends on: converting each Bikram Sambat date to its
     * Gregorian date and asking kotlinx-datetime for the weekday must match the calendar's own
     * `dayOfWeek`. The calendar uses 1=Sunday..7=Saturday; ISO uses 1=Monday..7=Sunday.
     */
    @Test
    fun dayOfWeek_matchesGregorianCalendar() {
        for (year in sampleYears) {
            eachDay(year) { month, day ->
                val cal = calendar(year, month, day)
                val gregorian = NepaliDateConverter.convertNepaliToEnglish(year, month, day)
                val isoFromCalendar = if (cal.dayOfWeek == 1) 7 else cal.dayOfWeek - 1
                val isoFromGregorian =
                    LocalDate(gregorian.year, gregorian.month, gregorian.dayOfMonth).dayOfWeek.ordinal + 1
                assertEquals(
                    isoFromGregorian, isoFromCalendar,
                    "weekday mismatch at BS $year/$month/$day " +
                        "(AD ${gregorian.year}/${gregorian.month}/${gregorian.dayOfMonth})"
                )
            }
        }
    }

    /** `dayOfYear` must equal the running sum of the preceding months plus the day. */
    @Test
    fun dayOfYear_matchesRunningMonthLengthSum() {
        for (year in sampleYears) {
            eachDay(year) { month, day ->
                val expected = (1 until month).sumOf { daysInMonth(year, it) } + day
                assertEquals(expected, calendar(year, month, day).dayOfYear, "dayOfYear at $year/$month/$day")
            }
        }
    }

    @Test
    fun baisakhFirst_isAlwaysWeekOne() {
        for (year in allYears) {
            assertEquals(1, calendar(year, 1, 1).weekOfYear, "Baisakh 1, $year should be week 1")
        }
    }

    /**
     * The defining property: as days advance, the week number advances by exactly one when, and
     * only when, the previous day was the last day of the week (Saturday, dayOfWeek 7). This does
     * not reuse the `calculateWeekOfYear` formula, so it independently validates the values.
     */
    @Test
    fun weekOfYear_advancesOnlyAfterSaturday() {
        for (year in sampleYears) {
            var previousWeek = 0
            var previousDayOfWeek = 0
            var isFirstDay = true
            eachDay(year) { month, day ->
                val cal = calendar(year, month, day)
                if (isFirstDay) {
                    assertEquals(1, cal.weekOfYear, "first day of $year should be week 1")
                    isFirstDay = false
                } else {
                    val expected = if (previousDayOfWeek == 7) previousWeek + 1 else previousWeek
                    assertEquals(expected, cal.weekOfYear, "weekOfYear wrong at $year/$month/$day")
                }
                previousWeek = cal.weekOfYear
                previousDayOfWeek = cal.dayOfWeek
            }
        }
    }

    /**
     * Every year ends in week 52, 53, or 54, and 54 is actually reached, so the documented upper
     * bound is neither wrong nor overly conservative. 52 is possible for a 364-day year that starts
     * on a Sunday; 54 for a long year that starts on a Saturday.
     */
    @Test
    fun lastDayOfEveryYearIsWeek52To54_andMaximumIs54() {
        var globalMax = 0
        for (year in allYears) {
            val lastWeek = calendar(year, 12, daysInMonth(year, 12)).weekOfYear
            assertTrue(lastWeek in 52..54, "year $year ends in week $lastWeek, expected 52..54")
            if (lastWeek > globalMax) globalMax = lastWeek
        }
        assertEquals(54, globalMax, "some year should reach week 54 (the documented upper bound)")
    }

    /** Regression lock on the exact formula: ceil((dayOfYear + weekdayOfBaisakh1 - 1) / 7). */
    @Test
    fun weekOfYear_matchesDayOfYearAndYearStartWeekday() {
        for (year in listOf(2080, 2081, 2082)) {
            val startWeekday = calendar(year, 1, 1).dayOfWeek
            eachDay(year) { month, day ->
                val cal = calendar(year, month, day)
                val daysPassed = cal.dayOfYear + (startWeekday - 1)
                val expected = (daysPassed + 6) / 7
                assertEquals(expected, cal.weekOfYear, "weekOfYear formula mismatch at $year/$month/$day")
            }
        }
    }
}
