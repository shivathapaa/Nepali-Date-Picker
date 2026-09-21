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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The library has two ways to reach a Gregorian calendar: reading the date directly, and converting
 * a Bikram Sambat date back. They are documented as the same value and callers mix them freely, so
 * every derived field has to agree.
 *
 * These cover the boundaries and a stride through the range, which is what every target can afford.
 * `EnglishCalendarFullRangeEquivalenceTest` runs the same check over all 47,745 convertible dates
 * on the JVM, where a day-walk per date is cheap enough to do exhaustively.
 */
class EnglishCalendarEquivalenceTests {

    private fun assertBothRoutesAgree(year: Int, month: Int, dayOfMonth: Int) {
        val nepali = NepaliDateConverter.convertEnglishToNepali(year, month, dayOfMonth)
        val viaRoundTrip = NepaliDateConverter.convertNepaliToEnglish(
            nepali.year, nepali.month, nepali.dayOfMonth
        )
        val direct = NepaliDateConverter.getEnglishCalendar(year, month, dayOfMonth)
        assertEquals(
            viaRoundTrip, direct,
            "English $year-$month-$dayOfMonth disagrees between the round trip and the direct read"
        )
    }

    @Test
    fun bothRoutesAgree_atTheConversionAnchor() {
        val anchor = NepaliCalendarDefaults.minConvertibleEnglishDate
        assertBothRoutesAgree(anchor.year, anchor.month, anchor.dayOfMonth)
        assertBothRoutesAgree(anchor.year, anchor.month, anchor.dayOfMonth + 1)
    }

    @Test
    fun bothRoutesAgree_atTheEndOfTheConvertibleRange() {
        val last = NepaliCalendarDefaults.EnglishYearRange.last
        assertBothRoutesAgree(last, 12, 31)
        assertBothRoutesAgree(last, 12, 30)
        assertBothRoutesAgree(last, 1, 1)
    }

    @Test
    fun bothRoutesAgree_onEveryMonthEdgeOfASampleOfYears() {
        var checked = 0
        for (year in NepaliCalendarDefaults.EnglishYearRange step SampleStride) {
            for (month in 1..12) {
                val totalDays = NepaliDateConverter.getTotalDaysInEnglishMonth(year, month)
                listOf(1, 2, totalDays - 1, totalDays).forEach { dayOfMonth ->
                    if (NepaliDateConverter.isEnglishDateConvertible(year, month, dayOfMonth)) {
                        assertBothRoutesAgree(year, month, dayOfMonth)
                        checked++
                    }
                }
            }
        }
        assertTrue(checked > 100, "Expected a meaningful sample, got $checked dates")
    }

    @Test
    fun bothRoutesAgree_onEveryDayOfAFullYear() {
        for (month in 1..12) {
            val totalDays = NepaliDateConverter.getTotalDaysInEnglishMonth(SampleLeapYear, month)
            for (dayOfMonth in 1..totalDays) {
                assertBothRoutesAgree(SampleLeapYear, month, dayOfMonth)
            }
        }
    }

    @Test
    fun bothRoutesAgree_onEveryLeapDayInRange() {
        for (year in NepaliCalendarDefaults.EnglishYearRange) {
            if (NepaliDateConverter.getTotalDaysInEnglishMonth(year, 2) == 29) {
                assertBothRoutesAgree(year, 2, 29)
                assertBothRoutesAgree(year, 3, 1)
            }
        }
    }

    private companion object {
        const val SampleStride = 7
        const val SampleLeapYear = 2024
    }
}
