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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.CalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Resolving a wheel position (a year, a month and a day number) to a date.
 *
 * The wheel reads the position directly through [CalendarViewAdapter.calendarOf]. These hold that
 * against printing the three numbers into an eight-digit string and parsing them back, on every
 * position a wheel can rest on, including the ones that resolve to nothing.
 */
class WheelPositionResolutionTest {

    /** Print the position, parse it back, and drop the invalid-day sentinel `parse` returns. */
    private fun CalendarViewAdapter.viaStringRoundTrip(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): CustomCalendar? = parse(
        buildString {
            append(year.toString().padStart(4, '0'))
            append(month.toString().padStart(2, '0'))
            append(dayOfMonth.toString().padStart(2, '0'))
        }
    )?.takeIf { it.totalDaysInMonth > 0 }

    private fun CalendarViewAdapter.direct(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): CustomCalendar? = calendarOf(SimpleDate(year, month, dayOfMonth))

    @Test
    fun bothRoutesAgree_onEveryPositionAWheelCanRestOn() {
        var resolved = 0
        var empty = 0
        for (calendarSystem in CalendarSystem.entries) {
            val adapter = adapterFor(calendarSystem)
            for (year in adapter.yearRange) {
                for (month in 1..MonthsInTestYear) {
                    // A wheel offers as many days as the longest month it can show, so the tail of
                    // the day column is exactly where the two routes could disagree.
                    for (dayOfMonth in 1..32) {
                        val direct = adapter.direct(year, month, dayOfMonth)
                        assertEquals(
                            adapter.viaStringRoundTrip(year, month, dayOfMonth),
                            direct,
                            "$calendarSystem $year-$month-$dayOfMonth"
                        )
                        if (direct == null) empty++ else resolved++
                    }
                }
            }
        }
        assertTrue(resolved > 0 && empty > 0, "Both outcomes have to be exercised")
    }

    @Test
    fun aDayPastTheMonthLength_resolvesToNothing() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val month = adapter.monthOf(2083, 4)
        assertNull(adapter.direct(2083, 4, month.totalDaysInMonth + 1))
        assertEquals(
            month.totalDaysInMonth,
            adapter.direct(2083, 4, month.totalDaysInMonth)?.dayOfMonth
        )
    }

    @Test
    fun aThirtyTwoDayMonth_resolvesItsLastDay() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        // Asar 2082 is one of the 32-day months, the longest the day wheel ever has to offer.
        assertEquals(32, adapter.monthOf(2082, 3).totalDaysInMonth)
        assertEquals(32, adapter.direct(2082, 3, 32)?.dayOfMonth)
    }

    @Test
    fun aGregorianLeapDay_resolvesOnlyInALeapYear() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        assertEquals(29, adapter.direct(2024, 2, 29)?.dayOfMonth)
        assertNull(adapter.direct(2023, 2, 29))
    }

    @Test
    fun aGregorianDayBeforeTheConversionAnchor_resolvesButHasNoBikramSambatDate() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val beforeAnchor = adapter.direct(1913, 1, 1)
        assertEquals(1, beforeAnchor?.dayOfMonth, "The Gregorian day itself is real")
        assertNull(adapter.toCanonical(beforeAnchor!!), "It has no Bikram Sambat equivalent")
    }

    @Test
    fun aDayOnTheConversionAnchor_carriesItsBikramSambatDate() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val anchor = adapter.direct(1913, 4, 13)
        val canonical = adapter.toCanonical(anchor!!)
        assertEquals(1970, canonical?.year)
        assertEquals(1, canonical?.month)
        assertEquals(1, canonical?.dayOfMonth)
    }

    @Test
    fun theEdgesOfEachAdapterRange_resolve() {
        for (calendarSystem in CalendarSystem.entries) {
            val adapter = adapterFor(calendarSystem)
            val firstYear = adapter.yearRange.first
            val lastYear = adapter.yearRange.last
            assertEquals(1, adapter.direct(firstYear, 1, 1)?.dayOfMonth, "$calendarSystem first")
            val lastMonth = adapter.monthOf(lastYear, MonthsInTestYear)
            assertEquals(
                lastMonth.totalDaysInMonth,
                adapter.direct(lastYear, MonthsInTestYear, lastMonth.totalDaysInMonth)?.dayOfMonth,
                "$calendarSystem last"
            )
        }
    }
}
