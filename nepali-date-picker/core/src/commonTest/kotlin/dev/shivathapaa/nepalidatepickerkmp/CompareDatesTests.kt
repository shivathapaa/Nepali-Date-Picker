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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Direct tests against the public [NepaliDateConverter.compareDates] overloads.
 *
 * The (CustomCalendar, SimpleDate) and (CustomCalendar, CustomCalendar) overloads
 * defined in NepaliDateConverter currently forward `dateToCompareFrom.month` and
 * `dateToCompareFrom.dayOfMonth` to the underlying model — comparing the calendar
 * with itself for month/day and only honouring the year. The tests below assert
 * the corrected behaviour and will fail until those two overloads are fixed.
 */
class CompareDatesTests {

    private val model = NepaliCalendarModel()

    // ── SimpleDate / year-month-day overload (model-level, correct) ──────────

    @Test
    fun model_compareDates_simpleDateVsTargetTuple_sameDate_returnsZero() {
        val a = SimpleDate(2080, 5, 15)
        assertEquals(0, model.compareDates(a, 2080, 5, 15))
    }

    @Test
    fun model_compareDates_simpleDateBeforeTarget_returnsNegative() {
        val a = SimpleDate(2080, 5, 15)
        assertTrue(model.compareDates(a, 2080, 5, 16) < 0)
        assertTrue(model.compareDates(a, 2080, 6, 1) < 0)
        assertTrue(model.compareDates(a, 2081, 1, 1) < 0)
    }

    @Test
    fun model_compareDates_simpleDateAfterTarget_returnsPositive() {
        val a = SimpleDate(2080, 5, 15)
        assertTrue(model.compareDates(a, 2080, 5, 14) > 0)
        assertTrue(model.compareDates(a, 2080, 4, 30) > 0)
        assertTrue(model.compareDates(a, 2079, 12, 30) > 0)
    }

    @Test
    fun model_compareDates_calendarVsTargetTuple_sameDate_returnsZero() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        assertEquals(0, model.compareDates(cal, 2081, 5, 24))
    }

    @Test
    fun model_compareDates_calendarBeforeTarget_returnsNegative() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        assertTrue(model.compareDates(cal, 2081, 5, 25) < 0)
        assertTrue(model.compareDates(cal, 2081, 6, 1) < 0)
        assertTrue(model.compareDates(cal, 2082, 1, 1) < 0)
    }

    @Test
    fun model_compareDates_calendarAfterTarget_returnsPositive() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        assertTrue(model.compareDates(cal, 2081, 5, 23) > 0)
        assertTrue(model.compareDates(cal, 2081, 4, 1) > 0)
        assertTrue(model.compareDates(cal, 2080, 12, 30) > 0)
    }

    // ── NepaliDateConverter facade overloads (currently buggy) ───────────────

    @Test
    fun converter_compareDates_calendarVsSimpleDate_sameDate_returnsZero() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        val target = SimpleDate(2081, 5, 24)
        assertEquals(0, NepaliDateConverter.compareDates(cal, target))
    }

    @Test
    fun converter_compareDates_calendarVsSimpleDate_calendarBeforeTarget_returnsNegative() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        val laterSameMonth = SimpleDate(2081, 5, 25)
        val laterDifferentMonth = SimpleDate(2081, 6, 1)
        // Expected: both negative.
        // Currently both return 0 because the facade forwards calendar's own
        // month/dayOfMonth instead of the target's.
        assertTrue(NepaliDateConverter.compareDates(cal, laterSameMonth) < 0,
            "Expected negative for same-month later day — currently broken")
        assertTrue(NepaliDateConverter.compareDates(cal, laterDifferentMonth) < 0,
            "Expected negative for later month — currently broken")
    }

    @Test
    fun converter_compareDates_calendarVsSimpleDate_calendarAfterTarget_returnsPositive() {
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        val earlierSameMonth = SimpleDate(2081, 5, 1)
        val earlierDifferentMonth = SimpleDate(2081, 4, 30)
        assertTrue(NepaliDateConverter.compareDates(cal, earlierSameMonth) > 0,
            "Expected positive for earlier same-month day — currently broken")
        assertTrue(NepaliDateConverter.compareDates(cal, earlierDifferentMonth) > 0,
            "Expected positive for earlier month — currently broken")
    }

    @Test
    fun converter_compareDates_calendarVsSimpleDate_yearDiffStillCorrect() {
        // Year diff IS honoured because the buggy line still passes target year.
        val cal = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
        val nextYear = SimpleDate(2082, 5, 24)
        val prevYear = SimpleDate(2080, 5, 24)
        assertTrue(NepaliDateConverter.compareDates(cal, nextYear) < 0)
        assertTrue(NepaliDateConverter.compareDates(cal, prevYear) > 0)
    }

    @Test
    fun converter_compareDates_calendarVsCalendar_differentMonth_returnsSignedDiff() {
        val earlier = NepaliDateConverter.getNepaliCalendar(2081, 3, 15)
        val later = NepaliDateConverter.getNepaliCalendar(2081, 7, 15)
        assertTrue(NepaliDateConverter.compareDates(earlier, later) < 0,
            "Expected negative for earlier month — currently broken")
        assertTrue(NepaliDateConverter.compareDates(later, earlier) > 0,
            "Expected positive for later month — currently broken")
    }

    @Test
    fun converter_compareDates_simpleDateOverload_returnsSignedDiffForMonthAndDay() {
        val date = SimpleDate(2081, 5, 24)
        // This overload (SimpleDate, year, month, dayOfMonth) forwards correctly.
        assertTrue(NepaliDateConverter.compareDates(date, 2081, 5, 25) < 0)
        assertTrue(NepaliDateConverter.compareDates(date, 2081, 5, 23) > 0)
        assertTrue(NepaliDateConverter.compareDates(date, 2081, 6, 1) < 0)
        assertTrue(NepaliDateConverter.compareDates(date, 2081, 4, 30) > 0)
        assertEquals(0, NepaliDateConverter.compareDates(date, 2081, 5, 24))
    }
}
