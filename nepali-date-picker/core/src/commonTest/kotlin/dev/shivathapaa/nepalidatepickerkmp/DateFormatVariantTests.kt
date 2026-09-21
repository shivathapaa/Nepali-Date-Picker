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
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.defaultDigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every combination the two long-form date formatters accept.
 *
 * The Bikram Sambat and Gregorian formatters share one body and differ only in which month-name
 * list they read, so the sweeps here run both over the whole space of styles, name formats and
 * languages, and [bothFormatters_differOnlyInTheMonthName] states that difference directly.
 */
class DateFormatVariantTests {

    // 2081-05-24 Bhadra, a Monday, is 2024-09-09 September, the same Monday.
    private val nepali = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
    private val english = NepaliDateConverter.getEnglishCalendar(2024, 9, 9)

    private fun locale(
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH,
        style: NepaliDateFormatStyle = NepaliDateFormatStyle.LONG,
        weekDayName: NameFormat = NameFormat.FULL,
        monthName: NameFormat = NameFormat.FULL
    ) = NepaliDateLocale(language, style, weekDayName, monthName)

    private fun formatNepali(locale: NepaliDateLocale) =
        NepaliDateConverter.formatNepaliDate(
            nepali.year, nepali.month, nepali.dayOfMonth, nepali.dayOfWeek, locale
        )

    private fun formatEnglish(locale: NepaliDateLocale) =
        NepaliDateConverter.formatEnglishDate(
            english.year, english.month, english.dayOfMonth, english.dayOfWeek, locale
        )

    @Test
    fun everyStyle_bikramSambat_english() {
        val expected = mapOf(
            NepaliDateFormatStyle.FULL to "Monday, Bhadra 24, 2081",
            NepaliDateFormatStyle.LONG to "Bhadra 24, 2081",
            NepaliDateFormatStyle.MEDIUM to "2081 Bhadra 24",
            NepaliDateFormatStyle.SHORT_MDY to "05/24/2081",
            NepaliDateFormatStyle.SHORT_YMD to "2081/05/24",
            NepaliDateFormatStyle.COMPACT_MDY to "05/24/81",
            NepaliDateFormatStyle.COMPACT_YMD to "81/05/24"
        )
        expected.forEach { (style, value) ->
            assertEquals(value, formatNepali(locale(style = style)), "Style $style")
        }
    }

    @Test
    fun everyStyle_gregorian_english() {
        val expected = mapOf(
            NepaliDateFormatStyle.FULL to "Monday, September 9, 2024",
            NepaliDateFormatStyle.LONG to "September 9, 2024",
            NepaliDateFormatStyle.MEDIUM to "2024 September 9",
            NepaliDateFormatStyle.SHORT_MDY to "09/09/2024",
            NepaliDateFormatStyle.SHORT_YMD to "2024/09/09",
            NepaliDateFormatStyle.COMPACT_MDY to "09/09/24",
            NepaliDateFormatStyle.COMPACT_YMD to "24/09/09"
        )
        expected.forEach { (style, value) ->
            assertEquals(value, formatEnglish(locale(style = style)), "Style $style")
        }
    }

    @Test
    fun everyStyle_bikramSambat_nepali() {
        val nepaliLang = NepaliDatePickerLang.NEPALI
        val expected = mapOf(
            NepaliDateFormatStyle.FULL to "सोमबार, भदौ २४, २०८१",
            NepaliDateFormatStyle.LONG to "भदौ २४, २०८१",
            NepaliDateFormatStyle.MEDIUM to "२०८१ भदौ २४",
            NepaliDateFormatStyle.SHORT_MDY to "०५/२४/२०८१",
            NepaliDateFormatStyle.SHORT_YMD to "२०८१/०५/२४",
            NepaliDateFormatStyle.COMPACT_MDY to "०५/२४/८१",
            NepaliDateFormatStyle.COMPACT_YMD to "८१/०५/२४"
        )
        expected.forEach { (style, value) ->
            assertEquals(
                value, formatNepali(locale(language = nepaliLang, style = style)), "Style $style"
            )
        }
    }

    @Test
    fun weekdayNameFormat_onlyAffectsTheStyleThatShowsAWeekday() {
        assertEquals(
            "Monday, Bhadra 24, 2081",
            formatNepali(locale(style = NepaliDateFormatStyle.FULL, weekDayName = NameFormat.FULL))
        )
        assertEquals(
            "Mon, Bhadra 24, 2081",
            formatNepali(locale(style = NepaliDateFormatStyle.FULL, weekDayName = NameFormat.MEDIUM))
        )
        assertEquals(
            "M, Bhadra 24, 2081",
            formatNepali(locale(style = NepaliDateFormatStyle.FULL, weekDayName = NameFormat.SHORT))
        )
        NameFormat.entries.forEach { weekDayName ->
            assertEquals(
                "Bhadra 24, 2081",
                formatNepali(
                    locale(style = NepaliDateFormatStyle.LONG, weekDayName = weekDayName)
                ),
                "LONG must not show a weekday whatever $weekDayName asks for"
            )
        }
    }

    @Test
    fun monthNameFormat_shortAbbreviates_everythingElseIsFull() {
        assertEquals(
            "Bha 24, 2081",
            formatNepali(locale(monthName = NameFormat.SHORT))
        )
        assertEquals(
            "Bhadra 24, 2081",
            formatNepali(locale(monthName = NameFormat.MEDIUM))
        )
        assertEquals(
            "Bhadra 24, 2081",
            formatNepali(locale(monthName = NameFormat.FULL))
        )
        assertEquals("Sep 9, 2024", formatEnglish(locale(monthName = NameFormat.SHORT)))
    }

    @Test
    fun numericStyles_zeroPadTheDay_andNamedStylesDoNot() {
        val ninth = NepaliDateConverter.getNepaliCalendar(2081, 5, 9)
        fun format(style: NepaliDateFormatStyle) = NepaliDateConverter.formatNepaliDate(
            ninth.year, ninth.month, ninth.dayOfMonth, ninth.dayOfWeek, locale(style = style)
        )
        assertEquals("Bhadra 9, 2081", format(NepaliDateFormatStyle.LONG))
        assertEquals("2081/05/09", format(NepaliDateFormatStyle.SHORT_YMD))
        assertEquals("81/05/09", format(NepaliDateFormatStyle.COMPACT_YMD))
        assertEquals("05/09/81", format(NepaliDateFormatStyle.COMPACT_MDY))
    }

    /**
     * The one difference between the two formatters. Swapping the month name in the Bikram Sambat
     * output for the Gregorian one has to reproduce the Gregorian output exactly, for every style,
     * every name format and every language.
     */
    @Test
    fun bothFormatters_differOnlyInTheMonthName() {
        for (language in NepaliDatePickerLang.entries) {
            for (style in NepaliDateFormatStyle.entries) {
                for (weekDayName in NameFormat.entries) {
                    for (monthName in NameFormat.entries) {
                        val locale = locale(language, style, weekDayName, monthName)
                        val bikramSambatOutput = NepaliDateConverter.formatNepaliDate(
                            // Same numbers on both sides, so only the name list can differ.
                            year = 2081,
                            month = 9,
                            dayOfMonth = 9,
                            dayOfWeek = 2,
                            locale = locale
                        )
                        val gregorianOutput = NepaliDateConverter.formatEnglishDate(
                            year = 2081,
                            month = 9,
                            dayOfMonth = 9,
                            dayOfWeek = 2,
                            locale = locale
                        )
                        // Anything other than SHORT renders the full name.
                        val nameFormat =
                            if (monthName == NameFormat.SHORT) NameFormat.SHORT else NameFormat.FULL
                        val bikramSambatMonth = NepaliDateConverter.getMonthName(
                            month = 9, format = nameFormat, language = language
                        )
                        val gregorianMonth = NepaliDateConverter.getEnglishMonthName(
                            month = 9, format = nameFormat, language = language
                        )
                        assertEquals(
                            gregorianOutput,
                            bikramSambatOutput.replace(bikramSambatMonth, gregorianMonth),
                            "$language / $style / weekday $weekDayName / month $monthName"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun everyCombination_producesNonEmptyOutput_forEveryMonthAndWeekday() {
        for (language in NepaliDatePickerLang.entries) {
            for (style in NepaliDateFormatStyle.entries) {
                for (month in 1..12) {
                    for (dayOfWeek in 1..7) {
                        val locale = locale(language = language, style = style)
                        assertTrue(
                            NepaliDateConverter.formatNepaliDate(2081, month, 15, dayOfWeek, locale)
                                .isNotEmpty(),
                            "Bikram Sambat $language/$style/$month/$dayOfWeek produced nothing"
                        )
                        assertTrue(
                            NepaliDateConverter.formatEnglishDate(2024, month, 15, dayOfWeek, locale)
                                .isNotEmpty(),
                            "Gregorian $language/$style/$month/$dayOfWeek produced nothing"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun explicitLatinDigits_renderNepaliMonthNamesWithLatinNumerals() {
        val nepaliNamesLatinDigits = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.LONG,
            digitScript = DigitScript.LATIN
        )
        assertEquals("भदौ 24, 2081", formatNepali(nepaliNamesLatinDigits))
        assertEquals(
            "सेप्टेम्बर 9, 2024",
            formatEnglish(nepaliNamesLatinDigits)
        )
    }

    @Test
    fun explicitDevanagariDigits_renderEnglishMonthNamesWithDevanagariNumerals() {
        val englishNamesDevanagariDigits = NepaliDateLocale(
            language = NepaliDatePickerLang.ENGLISH,
            dateFormat = NepaliDateFormatStyle.LONG,
            digitScript = DigitScript.DEVANAGARI
        )
        assertEquals("Bhadra २४, २०८१", formatNepali(englishNamesDevanagariDigits))
        assertEquals("September ९, २०२४", formatEnglish(englishNamesDevanagariDigits))
    }

    @Test
    fun anExplicitScript_reachesEveryNumericFieldOfEveryStyle() {
        val expected = mapOf(
            NepaliDateFormatStyle.FULL to "सोमबार, भदौ 24, 2081",
            NepaliDateFormatStyle.LONG to "भदौ 24, 2081",
            NepaliDateFormatStyle.MEDIUM to "2081 भदौ 24",
            NepaliDateFormatStyle.SHORT_MDY to "05/24/2081",
            NepaliDateFormatStyle.SHORT_YMD to "2081/05/24",
            NepaliDateFormatStyle.COMPACT_MDY to "05/24/81",
            NepaliDateFormatStyle.COMPACT_YMD to "81/05/24"
        )
        expected.forEach { (style, value) ->
            val locale = NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI,
                dateFormat = style,
                digitScript = DigitScript.LATIN
            )
            assertEquals(value, formatNepali(locale), "Style $style")
        }
    }

    /**
     * An unset script still follows the language, which is what every locale built without one
     * relies on. Asserted against the same expectations as [everyStyle_bikramSambat_nepali] so the
     * override cannot quietly change the default.
     */
    @Test
    fun noExplicitScript_stillFollowsTheLanguage() {
        for (language in NepaliDatePickerLang.entries) {
            for (style in NepaliDateFormatStyle.entries) {
                val implicit = NepaliDateLocale(language = language, dateFormat = style)
                val explicit = implicit.copy(digitScript = language.defaultDigitScript())
                assertEquals(
                    formatNepali(explicit), formatNepali(implicit),
                    "Bikram Sambat $language/$style"
                )
                assertEquals(
                    formatEnglish(explicit), formatEnglish(implicit),
                    "Gregorian $language/$style"
                )
            }
        }
    }

    @Test
    fun validatingOverload_rejectsADayTheMonthDoesNotHave() {
        val totalDays = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 5)
        val impossible = nepali.copy(dayOfMonth = totalDays + 1)
        val failure = runCatching {
            NepaliDateConverter.formatNepaliDate(impossible, locale())
        }
        assertTrue(failure.isFailure, "A day past the month length should not format")
    }
}
