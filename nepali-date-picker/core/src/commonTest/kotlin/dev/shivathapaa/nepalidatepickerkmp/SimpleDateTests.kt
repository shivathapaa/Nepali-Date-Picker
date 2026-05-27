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

import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleDateTests {

    @Test
    fun simpleDate_compareTo_equalDates_returnsZero() {
        val a = SimpleDate(2080, 5, 15)
        val b = SimpleDate(2080, 5, 15)
        assertEquals(0, a.compareTo(b))
        assertTrue(a == b)
    }

    @Test
    fun simpleDate_compareTo_differentDay_sameMonthYear_returnsSignedDayDiff() {
        val earlier = SimpleDate(2080, 5, 15)
        val later = SimpleDate(2080, 5, 20)
        assertTrue(earlier < later)
        assertTrue(later > earlier)
        assertEquals(-5, earlier.compareTo(later))
        assertEquals(5, later.compareTo(earlier))
    }

    @Test
    fun simpleDate_compareTo_differentMonth_sameYear_returnsSignedMonthDiff() {
        val earlier = SimpleDate(2080, 3, 30)
        val later = SimpleDate(2080, 5, 1)
        assertTrue(earlier < later)
        assertTrue(later > earlier)
    }

    @Test
    fun simpleDate_compareTo_differentYear_returnsSignedYearDiff() {
        val earlier = SimpleDate(2079, 12, 30)
        val later = SimpleDate(2080, 1, 1)
        assertTrue(earlier < later)
        assertTrue(later > earlier)
    }

    @Test
    fun simpleDate_compareTo_lessEqualGreaterEqual_workCorrectly() {
        val a = SimpleDate(2080, 5, 15)
        val b = SimpleDate(2080, 5, 15)
        val c = SimpleDate(2080, 5, 16)
        assertTrue(a <= b)
        assertTrue(a >= b)
        assertTrue(a <= c)
        assertTrue(c >= a)
    }

    @Test
    fun simpleDate_sortable_listSortsByChronologicalOrder() {
        val unsorted = listOf(
            SimpleDate(2081, 1, 1),
            SimpleDate(2080, 12, 30),
            SimpleDate(2080, 1, 1),
            SimpleDate(2080, 12, 1),
            SimpleDate(2079, 6, 15)
        )
        val sorted = unsorted.sorted()
        val expected = listOf(
            SimpleDate(2079, 6, 15),
            SimpleDate(2080, 1, 1),
            SimpleDate(2080, 12, 1),
            SimpleDate(2080, 12, 30),
            SimpleDate(2081, 1, 1)
        )
        assertEquals(expected, sorted)
    }

    @Test
    fun simpleDate_indexIn_startOfRange_returnsZero() {
        val date = SimpleDate(2080, 1, 1)
        assertEquals(0, date.indexIn(2080..2100))
    }

    @Test
    fun simpleDate_indexIn_endOfFirstYear_returnsEleven() {
        val date = SimpleDate(2080, 12, 1)
        assertEquals(11, date.indexIn(2080..2100))
    }

    @Test
    fun simpleDate_indexIn_startOfSecondYear_returnsTwelve() {
        val date = SimpleDate(2081, 1, 1)
        assertEquals(12, date.indexIn(2080..2100))
    }

    @Test
    fun simpleDate_indexIn_arbitraryOffset_returnsCorrectIndex() {
        val date = SimpleDate(2083, 7, 15)
        assertEquals(3 * 12 + 7 - 1, date.indexIn(2080..2100))
    }

    @Test
    fun simpleDate_indexIn_yearBelowRange_returnsNegativeIndex() {
        val date = SimpleDate(2079, 1, 1)
        assertEquals(-12, date.indexIn(2080..2100))
    }

    @Test
    fun simpleDate_defaultDayOfMonth_isOne() {
        val date = SimpleDate(2080, 5)
        assertEquals(1, date.dayOfMonth)
    }

    @Test
    fun nepaliMonthCalendar_indexIn_returnsSameFormulaAsSimpleDate() {
        val month = NepaliMonthCalendar(
            year = 2083,
            month = 7,
            totalDaysInMonth = 30,
            firstDayOfMonth = 1,
            lastDayOfMonth = 2
        )
        assertEquals(3 * 12 + 7 - 1, month.indexIn(2080..2100))
    }

    @Test
    fun nepaliMonthCalendar_daysFromStartOfWeekToFirstOfMonth_defaultsToFirstDayMinusOne() {
        val month = NepaliMonthCalendar(
            year = 2080,
            month = 1,
            totalDaysInMonth = 31,
            firstDayOfMonth = 4,
            lastDayOfMonth = 6
        )
        assertEquals(3, month.daysFromStartOfWeekToFirstOfMonth)
    }
}
