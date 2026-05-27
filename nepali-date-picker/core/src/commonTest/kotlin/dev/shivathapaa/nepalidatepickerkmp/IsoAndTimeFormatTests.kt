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
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IsoAndTimeFormatTests {

    // ── ISO formatting ───────────────────────────────────────────────────────

    @Test
    fun formatNepaliDateTimeToIso_kathmanduTimeZone_shiftsTo_UTC() {
        // Asia/Kathmandu = UTC+5:45 → 14:45 KTM = 09:00 UTC.
        val nepaliDate = SimpleDate(2081, 5, 24)
        val time = SimpleTime(14, 45, 15, 0)
        val iso = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(nepaliDate, time)
        assertEquals("2024-09-09T09:00:15Z", iso)
    }

    @Test
    fun formatEnglishDateNepaliTimeToIso_kathmanduTimeZone_shiftsTo_UTC() {
        val englishDate = SimpleDate(2024, 9, 9)
        val time = SimpleTime(14, 45, 15, 0)
        val iso = NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(englishDate, time)
        assertEquals("2024-09-09T09:00:15Z", iso)
    }

    @Test
    fun formatNepaliAndEnglishToIso_sameInstant_returnsSameString() {
        val time = SimpleTime(10, 0, 0, 0)
        val nepaliIso = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(
            SimpleDate(2081, 5, 24), time
        )
        val englishIso = NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(
            SimpleDate(2024, 9, 9), time
        )
        assertEquals(nepaliIso, englishIso)
    }

    @Test
    fun getNepaliDateTimeFromIso_basicUtc_returnsKathmanduLocalNepaliCalendar() {
        val dt = NepaliDateConverter.getNepaliDateTimeFromIsoFormat("2024-09-09T09:00:15Z")
        assertEquals(2081, dt.customCalendar.year)
        assertEquals(5, dt.customCalendar.month)
        assertEquals(24, dt.customCalendar.dayOfMonth)
        assertEquals(14, dt.simpleTime.hour)
        assertEquals(45, dt.simpleTime.minute)
        assertEquals(15, dt.simpleTime.second)
    }

    @Test
    fun getNepaliDateTimeFromIso_offsetWithMinutes_appliesOffsetCorrectly() {
        // 2024-09-09T05:15:00+00:00 → 11:00 KTM same day.
        val dt = NepaliDateConverter.getNepaliDateTimeFromIsoFormat("2024-09-09T05:15:00Z")
        assertEquals(11, dt.simpleTime.hour)
        assertEquals(0, dt.simpleTime.minute)
    }

    @Test
    fun getNepaliDateTimeFromIso_acrossDateLine_shiftsCalendarForward() {
        // 2024-09-09T19:00:00Z → 2024-09-10T00:45 KTM.
        val dt = NepaliDateConverter.getNepaliDateTimeFromIsoFormat("2024-09-09T19:00:00Z")
        assertEquals(0, dt.simpleTime.hour)
        assertEquals(45, dt.simpleTime.minute)
        // Nepali date should advance with the English date (still 2081-5-25).
        val expected = NepaliDateConverter.convertEnglishToNepali(2024, 9, 10)
        assertEquals(expected.dayOfMonth, dt.customCalendar.dayOfMonth)
    }

    @Test
    fun getEnglishDateNepaliTimeFromIso_basicUtc_returnsEnglishCalendar() {
        val dt = NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat("2024-09-09T09:00:15Z")
        assertEquals(2024, dt.customCalendar.year)
        assertEquals(9, dt.customCalendar.month)
        assertEquals(9, dt.customCalendar.dayOfMonth)
        assertEquals(14, dt.simpleTime.hour)
    }

    @Test
    fun getNepaliDateTimeFromIso_invalidIsoString_throws() {
        assertFailsWith<Exception> {
            NepaliDateConverter.getNepaliDateTimeFromIsoFormat("not-an-iso-date")
        }
    }

    @Test
    fun isoRoundTrip_nepaliToIsoToNepali_preservesDateAndTime() {
        val nepDate = SimpleDate(2081, 5, 24)
        val time = SimpleTime(14, 45, 15, 0)
        val iso = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(nepDate, time)
        val recovered = NepaliDateConverter.getNepaliDateTimeFromIsoFormat(iso)
        assertEquals(nepDate.year, recovered.customCalendar.year)
        assertEquals(nepDate.month, recovered.customCalendar.month)
        assertEquals(nepDate.dayOfMonth, recovered.customCalendar.dayOfMonth)
        assertEquals(time.hour, recovered.simpleTime.hour)
        assertEquals(time.minute, recovered.simpleTime.minute)
        assertEquals(time.second, recovered.simpleTime.second)
    }

    // ── 12-hour / 24-hour formatting ─────────────────────────────────────────

    @Test
    fun getFormattedTimeInEnglish_midnight12HourFormat_returnsTwelveAM() {
        val midnight = SimpleTime(0, 0, 0, 0)
        assertEquals("12:00 AM", NepaliDateConverter.getFormattedTimeInEnglish(midnight))
    }

    @Test
    fun getFormattedTimeInEnglish_noon12HourFormat_returnsTwelvePM() {
        val noon = SimpleTime(12, 0, 0, 0)
        assertEquals("12:00 PM", NepaliDateConverter.getFormattedTimeInEnglish(noon))
    }

    @Test
    fun getFormattedTimeInEnglish_lateEvening12HourFormat() {
        val late = SimpleTime(23, 5, 0, 0)
        assertEquals("11:05 PM", NepaliDateConverter.getFormattedTimeInEnglish(late))
    }

    @Test
    fun getFormattedTimeInEnglish_24HourFormat_doesNotConvert() {
        val late = SimpleTime(23, 5, 0, 0)
        assertEquals("23:05", NepaliDateConverter.getFormattedTimeInEnglish(late, use12HourFormat = false))
    }

    @Test
    fun getFormattedTimeInNepali_morning_returnsBihana() {
        val morning = SimpleTime(5, 30, 0, 0)
        // 3..11 maps to बिहान per the (corrected) docstring.
        assertEquals("बिहान ५ : ३०", NepaliDateConverter.getFormattedTimeInNepali(morning))
    }

    @Test
    fun getFormattedTimeInNepali_noon_returnsDiunso() {
        val noon = SimpleTime(12, 0, 0, 0)
        assertEquals("दिउँसो १२ : ००", NepaliDateConverter.getFormattedTimeInNepali(noon))
    }

    @Test
    fun getFormattedTimeInNepali_evening_returnsSaanjh() {
        val evening = SimpleTime(18, 15, 0, 0)
        assertEquals("साँझ ६ : १५", NepaliDateConverter.getFormattedTimeInNepali(evening))
    }

    @Test
    fun getFormattedTimeInNepali_lateNight_returnsRaati() {
        val night = SimpleTime(2, 30, 0, 0)
        assertEquals("राति २ : ३०", NepaliDateConverter.getFormattedTimeInNepali(night))
    }

    @Test
    fun getFormattedTimeInNepali_midnightTwelveHourFormat_returnsTwelveRaati() {
        val midnight = SimpleTime(0, 0, 0, 0)
        assertTrue(NepaliDateConverter.getFormattedTimeInNepali(midnight).startsWith("राति"))
    }

    @Test
    fun getFormattedTimeInNepali_24HourFormat_omitsTimeOfDay() {
        val time = SimpleTime(16, 30, 0, 0)
        assertEquals("१६ : ३०",
            NepaliDateConverter.getFormattedTimeInNepali(time, use12HourFormat = false))
    }

    // ── formatTimeByUnicodePattern edge cases ────────────────────────────────

    @Test
    fun formatTimeByUnicodePattern_midnightTokens_returnPaddedZero() {
        val midnight = SimpleTime(0, 0, 0, 0)
        assertEquals("00", NepaliDateConverter.formatTimeByUnicodePattern("HH", midnight, NepaliDatePickerLang.ENGLISH))
        assertEquals("12", NepaliDateConverter.formatTimeByUnicodePattern("hh", midnight, NepaliDatePickerLang.ENGLISH))
    }

    @Test
    fun formatTimeByUnicodePattern_noonTokens_returnTwelve() {
        val noon = SimpleTime(12, 0, 0, 0)
        assertEquals("12", NepaliDateConverter.formatTimeByUnicodePattern("hh", noon, NepaliDatePickerLang.ENGLISH))
        assertEquals("PM", NepaliDateConverter.formatTimeByUnicodePattern("A", noon, NepaliDatePickerLang.ENGLISH))
    }

    @Test
    fun formatTimeByUnicodePattern_unknownTokenPassesThrough() {
        val time = SimpleTime(10, 30, 0, 0)
        val out = NepaliDateConverter.formatTimeByUnicodePattern("Q[hh:mm]Q", time, NepaliDatePickerLang.ENGLISH)
        assertEquals("Q[10:30]Q", out)
    }

    @Test
    fun formatTimeByUnicodePattern_emptyPattern_returnsEmpty() {
        val time = SimpleTime(10, 30, 0, 0)
        assertEquals("", NepaliDateConverter.formatTimeByUnicodePattern("", time, NepaliDatePickerLang.ENGLISH))
    }
}
