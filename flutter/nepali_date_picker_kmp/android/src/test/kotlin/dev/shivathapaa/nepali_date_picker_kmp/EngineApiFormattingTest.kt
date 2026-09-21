// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Runs the naming, formatting and timestamp half of the engine host against
 * the real engine: each call has to reach the right table and carry the
 * locale, script and pattern it was given.
 */
internal class EngineApiFormattingTest {

    private val api = EngineApiImpl()

    private val date = DateDto(year = 2082, month = 6, dayOfMonth = 4)
    private val time = TimeDto(hour = 16, minute = 30, second = 15, nanosecond = 0)

    private fun locale(
        language: LangDto = LangDto.ENGLISH,
        style: DateFormatStyleDto = DateFormatStyleDto.LONG,
        script: DigitScriptDto? = null
    ) = LocaleDto(
        language = language,
        dateFormat = style,
        weekDayName = NameFormatDto.FULL,
        monthName = NameFormatDto.FULL,
        digitScript = script
    )

    @Test
    fun weekdayNamesFollowTheSharedOneBasedWeek() {
        assertEquals("Sunday", api.getWeekdayName(1, NameFormatDto.FULL, LangDto.ENGLISH))
        assertEquals("Sun", api.getWeekdayName(1, NameFormatDto.MEDIUM, LangDto.ENGLISH))
        assertEquals("S", api.getWeekdayName(1, NameFormatDto.SHORT, LangDto.ENGLISH))
        assertNotEquals(
            api.getWeekdayName(1, NameFormatDto.FULL, LangDto.ENGLISH),
            api.getWeekdayName(1, NameFormatDto.FULL, LangDto.NEPALI)
        )
    }

    @Test
    fun monthNamesComeFromTheCalendarTheyBelongTo() {
        assertEquals("Baisakh", api.getBsMonthName(1, NameFormatDto.FULL, LangDto.ENGLISH))
        assertEquals("Bai", api.getBsMonthName(1, NameFormatDto.SHORT, LangDto.ENGLISH))
        assertEquals(
            "Baisakh",
            api.getBsMonthName(1, NameFormatDto.MEDIUM, LangDto.ENGLISH),
            "month names carry two lengths, so only SHORT shortens them"
        )
        assertEquals("September", api.getAdMonthName(9, NameFormatDto.FULL, LangDto.ENGLISH))
        assertEquals("Sep", api.getAdMonthName(9, NameFormatDto.SHORT, LangDto.ENGLISH))
    }

    @Test
    fun aLocalePresetRendersTheCalendarItWasGiven() {
        val calendar = api.getBsCalendar(2082, 6, 4)

        val english = api.formatBsDate(calendar, locale())
        assertContains(english, "2082")
        assertContains(english, "Asoj")

        val nepali = api.formatBsDate(calendar, locale(language = LangDto.NEPALI))
        assertContains(nepali, "२०८२")

        val gregorian = api.formatAdDate(api.getAdCalendar(2025, 9, 20), locale())
        assertContains(gregorian, "2025")
    }

    @Test
    fun theDigitScriptOverridesTheLanguagesOwnDefault() {
        val calendar = api.getBsCalendar(2082, 6, 4)

        val latinNepali = api.formatBsDate(
            calendar,
            locale(language = LangDto.NEPALI, script = DigitScriptDto.LATIN)
        )
        assertContains(latinNepali, "2082")
    }

    @Test
    fun patternsRenderTheirTokensInEitherLanguage() {
        assertContains(api.formatBsDateByPattern("yyyy MMMM d", date, LangDto.ENGLISH), "2082")
        assertContains(
            api.formatAdDateByPattern("yyyy MMMM d", DateDto(2025, 9, 20), LangDto.ENGLISH),
            "September"
        )
        assertContains(api.formatTimeByPattern("HH:mm", time, LangDto.ENGLISH), "16")
        assertContains(
            api.formatBsDateTimeByPattern("yyyy MMMM d hh:mm a", date, time, LangDto.ENGLISH),
            "2082"
        )
        assertTrue(
            api.formatBsDateTimeByPattern("yyyy MMMM d", date, null, LangDto.ENGLISH).isNotEmpty()
        )
        assertContains(
            api.formatAdDateTimeByPattern(
                "yyyy MMMM d hh:mm a", DateDto(2025, 9, 20), time, LangDto.ENGLISH
            ),
            "2025"
        )
    }

    @Test
    fun theClockFormsHonourTheHourConvention() {
        assertContains(api.formatTimeEnglish(time, true), "4:30")
        assertContains(api.formatTimeEnglish(time, false), "16:30")
        assertTrue(api.formatTimeNepali(time, true).isNotEmpty())
        assertNotEquals(api.formatTimeNepali(time, true), api.formatTimeNepali(time, false))
    }

    @Test
    fun timestampsSurviveTheRoundTripThroughIso() {
        val iso = api.bsDateTimeToIso(date, time)
        assertTrue(iso.endsWith("Z"), "expected a UTC timestamp, got $iso")

        val parsed = api.bsDateTimeFromIso(iso)
        assertEquals(2082L, parsed.calendar.year)
        assertEquals(6L, parsed.calendar.month)
        assertEquals(4L, parsed.calendar.dayOfMonth)

        val gregorian = api.adDateTimeFromIso(api.adDateTimeToIso(DateDto(2025, 9, 20), time))
        assertEquals(2025L, gregorian.calendar.year)
        assertEquals(1L, gregorian.calendar.era)
    }

    @Test
    fun digitsLocalizeAndComeBack() {
        assertEquals("२०८२", api.localizeDigits("2082", DigitScriptDto.DEVANAGARI))
        assertEquals("2082", api.localizeDigits("2082", DigitScriptDto.LATIN))
        assertEquals("2082", api.toLatinDigits("२०८२"))
    }

    @Test
    fun everyWirePatternFormatsAndParsesBack() {
        for (pattern in DatePatternDto.entries) {
            val text = api.wireFormatDate(date, pattern, DigitScriptDto.LATIN)
            assertEquals(10, text.length, "$pattern should print a ten character date")
            assertEquals(date, api.wireParseDate(text, pattern))
        }
    }

    @Test
    fun aWireDateInDevanagariStillParsesBack() {
        val text = api.wireFormatDate(date, DatePatternDto.YYYY_DASH_MM_DASH_DD, DigitScriptDto.DEVANAGARI)

        assertContains(text, "२०८२")
        assertEquals(date, api.wireParseDate(text, DatePatternDto.YYYY_DASH_MM_DASH_DD))
    }

    @Test
    fun textInTheWrongPatternDoesNotParse() {
        val slashed = api.wireFormatDate(date, DatePatternDto.YYYY_SLASH_MM_SLASH_DD, DigitScriptDto.LATIN)

        assertNull(api.wireParseDate(slashed, DatePatternDto.DD_DASH_MM_DASH_YYYY))
    }

    @Test
    fun aWireTimeFormatsAndParsesBack() {
        val text = api.wireFormatTime(time)

        assertEquals("16:30:15", text)
        assertEquals(time, api.wireParseTime(text))
    }
}
