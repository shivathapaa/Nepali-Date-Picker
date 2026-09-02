/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:Suppress("DEPRECATION") // intentionally exercises the deprecated convertTo*Number / localizeNumber alias - see DigitScriptTests for new API coverage

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.convertToEnglishNumber
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.convertToNepaliNumber
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.localizeNumber
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParseAndLocaleTests {

    private val model = NepaliCalendarModel()

    // NepaliCalendarModel.parse

    @Test
    fun parse_validEightCharDateString_returnsCalendar() {
        val cal = model.parse("20810524")
        assertNotNull(cal)
        assertEquals(2081, cal.year)
        assertEquals(5, cal.month)
        assertEquals(24, cal.dayOfMonth)
    }

    @Test
    fun parse_lengthNotEight_returnsNull() {
        assertNull(model.parse(""))
        assertNull(model.parse("2081052"))
        assertNull(model.parse("208105244"))
    }

    @Test
    fun parse_nonNumericContent_returnsNull() {
        assertNull(model.parse("20XX0524"))
        assertNull(model.parse("abcdefgh"))
    }

    @Test
    fun parse_monthOutOfRange_returnsNull() {
        assertNull(model.parse("20811324")) // month 13
        assertNull(model.parse("20810024")) // month 0
    }

    @Test
    fun parse_dayOutOfRange_returnsNull() {
        assertNull(model.parse("20810500")) // day 0
        assertNull(model.parse("20810533")) // day 33
    }

    @Test
    fun parse_validRangesButLogicallyInvalidDay_returnsFallbackCalendar() {
        // 2081-5 has 31 days; day 32 passes the 1..32 gate, but getNepaliCalendar
        // rejects it → parse falls back to a stub CustomCalendar with era=2 and
        // -1 sentinel fields per the documented behaviour.
        val cal = model.parse("20810532")
        assertNotNull(cal)
        assertEquals(2081, cal.year)
        assertEquals(5, cal.month)
        assertEquals(32, cal.dayOfMonth)
        assertEquals(2, cal.era)
        assertEquals(-1, cal.firstDayOfMonth)
    }

    // Number localisation

    @Test
    fun convertToNepaliNumber_emptyString_returnsEmpty() {
        assertEquals("", "".convertToNepaliNumber())
    }

    @Test
    fun convertToNepaliNumber_pureDigits_convertsAll() {
        assertEquals("०१२३४५६७८९", "0123456789".convertToNepaliNumber())
    }

    @Test
    fun convertToNepaliNumber_mixedWithLetters_keepsLettersIntact() {
        assertEquals("२०२४-०६-२१", "2024-06-21".convertToNepaliNumber())
        assertEquals("v२.०.०", "v2.0.0".convertToNepaliNumber())
    }

    @Test
    fun convertToEnglishNumber_emptyString_returnsEmpty() {
        assertEquals("", "".convertToEnglishNumber())
    }

    @Test
    fun convertToEnglishNumber_pureNepaliDigits_convertsAll() {
        assertEquals("0123456789", "०१२३४५६७८९".convertToEnglishNumber())
    }

    @Test
    fun convertToEnglishNumber_mixedDigits_convertsOnlyNepali() {
        assertEquals("2024-06-21", "२०२४-०६-२१".convertToEnglishNumber())
    }

    @Test
    fun convertToEnglishNumber_alreadyEnglish_returnsUnchanged() {
        assertEquals("2024-06-21", "2024-06-21".convertToEnglishNumber())
    }

    @Test
    fun localizeNumber_englishLocale_returnsInputUnchanged() {
        assertEquals("2024", "2024".localizeNumber(NepaliDatePickerLang.ENGLISH))
    }

    @Test
    fun localizeNumber_nepaliLocale_returnsConverted() {
        assertEquals("२०२४", "2024".localizeNumber(NepaliDatePickerLang.NEPALI))
    }

    @Test
    fun convertNumber_roundTrip_isIdempotent() {
        val original = "2081/05/24 09:45 AM"
        val nepali = original.convertToNepaliNumber()
        val back = nepali.convertToEnglishNumber()
        assertEquals(original, back)
    }

    // getWeekdayName

    @Test
    fun getWeekdayName_eachDayEnglishFull() {
        val expected = listOf(
            "Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"
        )
        for (day in 1..7) {
            assertEquals(expected[day - 1],
                NepaliDateConverter.getWeekdayName(day, NameFormat.FULL, NepaliDatePickerLang.ENGLISH))
        }
    }

    @Test
    fun getWeekdayName_eachDayEnglishShortMedium() {
        assertEquals("S", NepaliDateConverter.getWeekdayName(1, NameFormat.SHORT))
        assertEquals("Sun", NepaliDateConverter.getWeekdayName(1, NameFormat.MEDIUM))
    }

    @Test
    fun getWeekdayName_eachDayNepali() {
        val expectedFull = listOf(
            "आईतबार", "सोमबार", "मंगलबार", "बुधबार",
            "बिहिबार", "शुक्रबार", "शनिबार"
        )
        for (day in 1..7) {
            assertEquals(expectedFull[day - 1],
                NepaliDateConverter.getWeekdayName(day, NameFormat.FULL, NepaliDatePickerLang.NEPALI))
        }
    }

    @Test
    fun getWeekdayName_zeroOrAboveSeven_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getWeekdayName(0)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getWeekdayName(8)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getWeekdayName(-1)
        }
    }

    // getMonthName (Nepali months)

    @Test
    fun getMonthName_allTwelveInEnglish() {
        val expected = listOf(
            "Baisakh", "Jestha", "Asar", "Shrawn", "Bhadra", "Asoj",
            "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
        )
        for (m in 1..12) {
            assertEquals(expected[m - 1],
                NepaliDateConverter.getMonthName(m, NameFormat.FULL, NepaliDatePickerLang.ENGLISH))
        }
    }

    @Test
    fun getMonthName_allTwelveInNepali() {
        val expected = listOf(
            "बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज",
            "कार्तिक", "मंसिर", "पौष", "माघ", "फाल्गुन", "चैत"
        )
        for (m in 1..12) {
            assertEquals(expected[m - 1],
                NepaliDateConverter.getMonthName(m, NameFormat.FULL, NepaliDatePickerLang.NEPALI))
        }
    }

    @Test
    fun getMonthName_shortFormat_returnsAbbreviated() {
        assertEquals("Bai",
            NepaliDateConverter.getMonthName(1, NameFormat.SHORT, NepaliDatePickerLang.ENGLISH))
        assertEquals("Chai",
            NepaliDateConverter.getMonthName(12, NameFormat.SHORT, NepaliDatePickerLang.ENGLISH))
    }

    @Test
    fun getMonthName_outOfRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getMonthName(0)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getMonthName(13)
        }
    }

    // getEnglishMonthName

    @Test
    fun getEnglishMonthName_allTwelveInEnglish() {
        val expected = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        for (m in 1..12) {
            assertEquals(expected[m - 1],
                NepaliDateConverter.getEnglishMonthName(m, NameFormat.FULL, NepaliDatePickerLang.ENGLISH))
        }
    }

    @Test
    fun getEnglishMonthName_allTwelveInNepali() {
        val expected = listOf(
            "जनवरी", "फेब्रुअरी", "मार्च", "अप्रिल", "मे", "जुन",
            "जुलाई", "अगस्ट", "सेप्टेम्बर", "अक्टोबर", "नोभेम्बर", "डिसेम्बर"
        )
        for (m in 1..12) {
            assertEquals(expected[m - 1],
                NepaliDateConverter.getEnglishMonthName(m, NameFormat.FULL, NepaliDatePickerLang.NEPALI))
        }
    }

    @Test
    fun getEnglishMonthName_outOfRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishMonthName(0)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishMonthName(13)
        }
    }

    // replaceDelimiter

    @Test
    fun replaceDelimiter_emptyString_returnsEmpty() {
        assertEquals("", NepaliDateConverter.replaceDelimiter("", "-"))
    }

    @Test
    fun replaceDelimiter_noOccurrences_returnsUnchanged() {
        assertEquals("hello", NepaliDateConverter.replaceDelimiter("hello", "-"))
    }

    @Test
    fun replaceDelimiter_multipleSlashes_replacesAll() {
        assertEquals("2024-06-21",
            NepaliDateConverter.replaceDelimiter("2024/06/21", "-"))
    }

    @Test
    fun replaceDelimiter_customOldDelimiter_works() {
        assertEquals("a b c",
            NepaliDateConverter.replaceDelimiter("a:b:c", " ", oldDelimiter = ":"))
    }
}
