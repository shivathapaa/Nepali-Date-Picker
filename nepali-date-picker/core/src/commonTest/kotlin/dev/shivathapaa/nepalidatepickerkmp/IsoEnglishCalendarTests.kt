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
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Reading a Gregorian calendar out of an ISO timestamp.
 *
 * The result is a Gregorian date and a Nepal-time clock reading, neither of which needs the Bikram
 * Sambat table, so the supported span here is wider than
 * [NepaliCalendarDefaults.EnglishYearRange]. Its Bikram Sambat counterpart still needs the table
 * and still refuses timestamps outside it.
 */
class IsoEnglishCalendarTests {

    @Test
    fun englishCalendarFromIso_yearPastTheConversionTable_stillResolves() {
        val beyond = NepaliCalendarDefaults.EnglishYearRange.last + 7
        val dateTime = NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(
            "$beyond-06-15T00:00:00Z"
        )
        assertEquals(beyond, dateTime.customCalendar.year)
        assertEquals(6, dateTime.customCalendar.month)
        assertEquals(15, dateTime.customCalendar.dayOfMonth)
        // 00:00Z is 05:45 in Kathmandu.
        assertEquals(5, dateTime.simpleTime.hour)
        assertEquals(45, dateTime.simpleTime.minute)
    }

    @Test
    fun englishCalendarFromIso_yearBeforeTheConversionTable_stillResolves() {
        val before = NepaliCalendarDefaults.EnglishYearRange.first - 13
        val dateTime = NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(
            "$before-03-01T12:00:00Z"
        )
        assertEquals(before, dateTime.customCalendar.year)
        assertEquals(3, dateTime.customCalendar.month)
        assertEquals(1, dateTime.customCalendar.dayOfMonth)
        assertEquals(17, dateTime.simpleTime.hour)
        assertEquals(45, dateTime.simpleTime.minute)
    }

    @Test
    fun englishCalendarFromIso_outsideTheTable_carriesEveryDerivedField() {
        val beyond = NepaliCalendarDefaults.EnglishYearRange.last + 7
        val fromIso = NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(
            "$beyond-06-15T00:00:00Z"
        ).customCalendar
        val direct = NepaliDateConverter.getEnglishCalendar(beyond, 6, 15)
        assertEquals(direct, fromIso)
        assertTrue(fromIso.dayOfWeek in 1..7)
        assertTrue(fromIso.weekOfYear > 0)
        assertTrue(fromIso.dayOfYear > 0)
    }

    @Test
    fun nepaliCalendarFromIso_yearPastTheConversionTable_stillThrows() {
        val beyond = NepaliCalendarDefaults.EnglishYearRange.last + 7
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getNepaliDateTimeFromIsoFormat("$beyond-06-15T00:00:00Z")
        }
    }

    @Test
    fun englishCalendarFromIso_insideTheTable_matchesTheBikramSambatRoundTrip() {
        val samples = listOf(
            "1913-04-13T06:00:00Z",
            "2024-09-09T09:00:15Z",
            "2024-02-29T23:30:00Z",
            "2043-12-31T18:14:59Z"
        )
        samples.forEach { iso ->
            val english =
                NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(iso).customCalendar
            val nepali = NepaliDateConverter.getNepaliDateTimeFromIsoFormat(iso).customCalendar
            val roundTripped = NepaliDateConverter.convertNepaliToEnglish(
                nepali.year, nepali.month, nepali.dayOfMonth
            )
            assertEquals(roundTripped, english, "ISO $iso")
        }
    }

    @Test
    fun englishCalendarFromIso_leapDayAcrossTheDateLine_advancesCorrectly() {
        // 2024-02-29T19:00Z is 2024-03-01T00:45 in Kathmandu.
        val dateTime =
            NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat("2024-02-29T19:00:00Z")
        assertEquals(2024, dateTime.customCalendar.year)
        assertEquals(3, dateTime.customCalendar.month)
        assertEquals(1, dateTime.customCalendar.dayOfMonth)
        assertEquals(0, dateTime.simpleTime.hour)
        assertEquals(45, dateTime.simpleTime.minute)
    }

    @Test
    fun englishCalendarFromIso_invalidString_throws() {
        assertFailsWith<Exception> {
            NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat("not-an-iso-date")
        }
    }
}
