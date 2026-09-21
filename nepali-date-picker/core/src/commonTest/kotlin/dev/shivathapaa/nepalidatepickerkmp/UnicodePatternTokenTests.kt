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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Token-by-token behaviour of the `*ByUnicodePattern` formatters.
 *
 * The token alternation is compiled once and shared, so these pin what each token expands to, which
 * token wins when one is a prefix of another, and which tokens a given entry point is willing to
 * replace at all. A token added to a replacement map but not to the shared alternation would reach
 * the output untouched, which is what the sweeps at the bottom catch.
 */
class UnicodePatternTokenTests {

    // 2081-05-24 is a Monday (dayOfWeek 2), day 148 of the year, in week 22.
    private val nepaliCalendar = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)

    // 2024-09-09 is the same day read as Gregorian.
    private val englishCalendar = NepaliDateConverter.getEnglishCalendar(2024, 9, 9)

    private val time = SimpleTime(hour = 14, minute = 45, second = 15, nanosecond = 123456789)

    private val english = NepaliDatePickerLang.ENGLISH

    private fun nepaliDate(pattern: String) =
        NepaliDateConverter.formatNepaliDateByUnicodePattern(pattern, nepaliCalendar, english)

    private fun englishDate(pattern: String) =
        NepaliDateConverter.formatEnglishDateByUnicodePattern(pattern, englishCalendar, english)

    private fun timeOnly(pattern: String) =
        NepaliDateConverter.formatTimeByUnicodePattern(pattern, time, english)

    private fun nepaliDateTime(pattern: String, withTime: SimpleTime? = time) =
        NepaliDateConverter.formatNepaliDateTimeByUnicodePattern(
            pattern, nepaliCalendar, withTime, english
        )

    private fun englishDateTime(pattern: String, withTime: SimpleTime? = time) =
        NepaliDateConverter.formatEnglishDateTimeByUnicodePattern(
            pattern, englishCalendar, withTime, english
        )

    @Test
    fun everyDateToken_expandsForABikramSambatCalendar() {
        val expected = mapOf(
            "yyyy" to "2081",
            "yy" to "81",
            "MMMM" to "Bhadra",
            "MMM" to "Bha",
            "MM" to "05",
            "M" to "5",
            "dd" to "24",
            "d" to "24",
            "D" to nepaliCalendar.dayOfYear.toString(),
            "EEEEE" to "M",
            "EEEE" to "Monday",
            "E" to "Mon",
            "ee" to "02",
            "e" to "2",
            "w" to nepaliCalendar.weekOfYear.toString()
        )
        expected.forEach { (token, value) ->
            assertEquals(value, nepaliDate(token), "Date token $token")
        }
    }

    @Test
    fun everyDateToken_expandsForAGregorianCalendar() {
        val expected = mapOf(
            "yyyy" to "2024",
            "yy" to "24",
            "MMMM" to "September",
            "MMM" to "Sep",
            "MM" to "09",
            "M" to "9",
            "dd" to "09",
            "d" to "9",
            "D" to englishCalendar.dayOfYear.toString(),
            "EEEEE" to "M",
            "EEEE" to "Monday",
            "E" to "Mon",
            "ee" to "02",
            "e" to "2",
            "w" to englishCalendar.weekOfYear.toString()
        )
        expected.forEach { (token, value) ->
            assertEquals(value, englishDate(token), "Date token $token")
        }
    }

    @Test
    fun everyTimeToken_expands() {
        val expected = mapOf(
            "HH" to "14",
            "H" to "14",
            "hh" to "02",
            "h" to "2",
            "mm" to "45",
            "m" to "45",
            "ss" to "15",
            "s" to "15",
            "SSSS" to "1234",
            "SSS" to "123",
            "SS" to "12",
            "S" to "1",
            "a" to "pm",
            "A" to "PM"
        )
        expected.forEach { (token, value) ->
            assertEquals(value, timeOnly(token), "Time token $token")
        }
    }

    @Test
    fun longerTokenWins_whenAShorterOneIsItsPrefix() {
        assertEquals("2081", nepaliDate("yyyy"))
        assertEquals("81", nepaliDate("yy"))
        assertEquals("Monday", nepaliDate("EEEE"))
        assertEquals("M", nepaliDate("EEEEE"))
        assertEquals("Mon", nepaliDate("E"))
        assertEquals("Bhadra", nepaliDate("MMMM"))
        assertEquals("Bha", nepaliDate("MMM"))
        assertEquals("1234", timeOnly("SSSS"))
        assertEquals("123", timeOnly("SSS"))
        assertEquals("12", timeOnly("SS"))
        assertEquals("1", timeOnly("S"))
    }

    @Test
    fun adjacentTokens_eachExpandIndependently() {
        assertEquals("20810524", nepaliDate("yyyyMMdd"))
        assertEquals("2081-05-24", nepaliDate("yyyy-MM-dd"))
        assertEquals("144515", timeOnly("HHmmss"))
    }

    @Test
    fun dateOnlyFormatter_leavesTimeTokensAlone() {
        // A date formatter has no time to put there, so the token is not a token to it.
        assertEquals("HH:mm", nepaliDate("HH:mm"))
        assertEquals("HH:mm", englishDate("HH:mm"))
    }

    @Test
    fun timeOnlyFormatter_leavesDateTokensAlone() {
        assertEquals("yyyy", timeOnly("yyyy"))
        assertEquals("dd", timeOnly("dd"))
    }

    @Test
    fun dateTimeFormatter_withNullTime_leavesTimeTokensAlone() {
        assertEquals("2081-05-24 HH:mm", nepaliDateTime("yyyy-MM-dd HH:mm", withTime = null))
        assertEquals("2024-09-09 HH:mm", englishDateTime("yyyy-MM-dd HH:mm", withTime = null))
    }

    @Test
    fun dateTimeFormatter_withTime_replacesBothFamilies() {
        assertEquals("2081-05-24 14:45:15 PM", nepaliDateTime("yyyy-MM-dd HH:mm:ss A"))
        assertEquals("2024-09-09 14:45:15 PM", englishDateTime("yyyy-MM-dd HH:mm:ss A"))
    }

    @Test
    fun unsupportedCharacters_passThroughUnchanged() {
        assertEquals("[2081] (05) {24}", nepaliDate("[yyyy] (MM) {dd}"))
        assertEquals("", nepaliDate(""))
        assertEquals("-- :: ++", timeOnly("-- :: ++"))
    }

    /**
     * A pattern is matched token by token with no notion of literal text, so a single-letter token
     * is replaced wherever it appears, including inside a word. Callers who want literal letters
     * have to keep them out of the pattern.
     */
    @Test
    fun singleLetterTokens_areReplacedInsideOrdinaryWords() {
        // 's' is the seconds token and 'h' the 12-hour token.
        assertEquals("no token15 2ere", timeOnly("no tokens here"))
    }

    @Test
    fun nepaliLanguage_localizesDigitsAndNames() {
        val nepali = NepaliDatePickerLang.NEPALI
        assertEquals(
            "२०८१",
            NepaliDateConverter.formatNepaliDateByUnicodePattern("yyyy", nepaliCalendar, nepali)
        )
        assertEquals(
            "भदौ",
            NepaliDateConverter.formatNepaliDateByUnicodePattern("MMMM", nepaliCalendar, nepali)
        )
        assertEquals(
            "सेप्टेम्बर",
            NepaliDateConverter.formatEnglishDateByUnicodePattern("MMMM", englishCalendar, nepali)
        )
    }

    /**
     * Sweeps over every token the formatters know. A token that the shared alternation does not
     * carry survives into the output as itself, so requiring a change is what detects the drift.
     */
    @Test
    fun everyDateToken_isActuallyReplaced_inEveryDateEntryPoint() {
        DateTokens.forEach { token ->
            assertTrue(nepaliDate(token) != token, "Nepali date formatter ignored token $token")
            assertTrue(englishDate(token) != token, "English date formatter ignored token $token")
            assertTrue(
                nepaliDateTime(token) != token,
                "Nepali date-time formatter ignored date token $token"
            )
            assertTrue(
                englishDateTime(token) != token,
                "English date-time formatter ignored date token $token"
            )
        }
    }

    @Test
    fun everyTimeToken_isActuallyReplaced_inEveryTimeEntryPoint() {
        TimeTokens.forEach { token ->
            assertTrue(timeOnly(token) != token, "Time formatter ignored token $token")
            assertTrue(
                nepaliDateTime(token) != token,
                "Nepali date-time formatter ignored time token $token"
            )
            assertTrue(
                englishDateTime(token) != token,
                "English date-time formatter ignored time token $token"
            )
        }
    }

    @Test
    fun everyTokenConcatenated_leavesNoTokenBehind() {
        val pattern = (DateTokens + TimeTokens).joinToString("|")
        val formatted = nepaliDateTime(pattern)
        assertEquals(
            DateTokens.size + TimeTokens.size - 1,
            formatted.count { it == '|' },
            "Separators must survive so each token maps to exactly one field"
        )
        formatted.split("|").forEachIndexed { index, part ->
            val token = (DateTokens + TimeTokens)[index]
            assertTrue(part.isNotEmpty(), "Token $token expanded to nothing")
            assertTrue(part != token, "Token $token was not replaced")
        }
    }

    private companion object {
        val DateTokens = listOf(
            "yyyy", "yy", "MMMM", "MMM", "MM", "M", "dd", "d", "D",
            "EEEEE", "EEEE", "E", "ee", "e", "w"
        )
        val TimeTokens = listOf(
            "HH", "H", "hh", "h", "mm", "m", "ss", "s",
            "SSSS", "SSS", "SS", "S", "a", "A"
        )
    }
}
