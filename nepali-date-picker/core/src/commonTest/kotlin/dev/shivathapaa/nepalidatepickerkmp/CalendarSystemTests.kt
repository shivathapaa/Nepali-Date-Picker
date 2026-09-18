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
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.calendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.toMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalendarSystemTests {

    // CalendarSystem ⇄ era

    @Test
    fun calendarSystem_eraMatchesLibraryConvention() {
        assertEquals(2, CalendarSystem.BIKRAM_SAMBAT.era)
        assertEquals(1, CalendarSystem.GREGORIAN.era)
    }

    @Test
    fun calendarSystem_fromEra_roundTripsBothSystems() {
        CalendarSystem.entries.forEach { system ->
            assertEquals(system, CalendarSystem.fromEra(system.era))
        }
    }

    @Test
    fun calendarSystem_fromEra_unknownEraIsNull() {
        assertNull(CalendarSystem.fromEra(0))
        assertNull(CalendarSystem.fromEra(3))
    }

    @Test
    fun calendarSystem_oppositeFlips() {
        assertEquals(CalendarSystem.GREGORIAN, CalendarSystem.BIKRAM_SAMBAT.opposite())
        assertEquals(CalendarSystem.BIKRAM_SAMBAT, CalendarSystem.GREGORIAN.opposite())
    }

    @Test
    fun customCalendar_reportsItsOwnSystem() {
        assertEquals(
            CalendarSystem.BIKRAM_SAMBAT,
            NepaliDateConverter.getNepaliCalendar(2082, 6, 1).calendarSystem
        )
        assertEquals(
            CalendarSystem.GREGORIAN,
            NepaliDateConverter.getEnglishCalendar(2026, 9, 17).calendarSystem
        )
    }

    // English month geometry

    @Test
    fun getEnglishMonthCalendar_weekdayOfFirstMatchesKnownDates() {
        // 2026-09-01 is a Tuesday, which this library numbers 3 (Sunday = 1).
        val september2026 = NepaliDateConverter.getEnglishMonthCalendar(2026, 9)
        assertEquals(CalendarSystem.GREGORIAN, september2026.calendarSystem)
        assertEquals(3, september2026.firstDayOfMonth)
        assertEquals(30, september2026.totalDaysInMonth)
        // 2026-09-30 is a Wednesday.
        assertEquals(4, september2026.lastDayOfMonth)

        // 2024-09-01 is a Sunday, the low end of the numbering.
        assertEquals(1, NepaliDateConverter.getEnglishMonthCalendar(2024, 9).firstDayOfMonth)
        // 2024-06-01 is a Saturday, the high end.
        assertEquals(7, NepaliDateConverter.getEnglishMonthCalendar(2024, 6).firstDayOfMonth)
    }

    @Test
    fun getEnglishMonthCalendar_leapAndNonLeapFebruary() {
        assertEquals(29, NepaliDateConverter.getEnglishMonthCalendar(2024, 2).totalDaysInMonth)
        assertEquals(28, NepaliDateConverter.getEnglishMonthCalendar(2026, 2).totalDaysInMonth)
        assertEquals(29, NepaliDateConverter.getEnglishMonthCalendar(2000, 2).totalDaysInMonth)
        assertEquals(28, NepaliDateConverter.getEnglishMonthCalendar(1900, 2).totalDaysInMonth)
    }

    @Test
    fun getEnglishMonthCalendar_everyMonthOfLeapAndNonLeapYearIsSelfConsistent() {
        listOf(2024, 2026).forEach { year ->
            (1..12).forEach { month ->
                val monthCalendar = NepaliDateConverter.getEnglishMonthCalendar(year, month)
                assertTrue(monthCalendar.firstDayOfMonth in 1..7, "$year-$month first")
                assertTrue(monthCalendar.lastDayOfMonth in 1..7, "$year-$month last")
                assertEquals(
                    monthCalendar.firstDayOfMonth - 1,
                    monthCalendar.daysFromStartOfWeekToFirstOfMonth,
                    "$year-$month leading cells"
                )
                // Walking the month forward from the first weekday must land on the last weekday.
                val expectedLast =
                    ((monthCalendar.firstDayOfMonth + monthCalendar.totalDaysInMonth - 1) % 7)
                        .let { if (it == 0) 7 else it }
                assertEquals(expectedLast, monthCalendar.lastDayOfMonth, "$year-$month walk")
            }
        }
    }

    @Test
    fun getEnglishMonthCalendar_invalidMonthThrows() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishMonthCalendar(2026, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishMonthCalendar(2026, 13)
        }
    }

    // English CustomCalendar

    @Test
    fun getEnglishCalendar_matchesConversionFromNepali() {
        // 2026-09-17 ⇔ BS 2083-06-01, the pairing shown in the reference calendars.
        val fromNepali = NepaliDateConverter.convertNepaliToEnglish(2083, 6, 1)
        val direct = NepaliDateConverter.getEnglishCalendar(2026, 9, 17)
        assertEquals(fromNepali, direct)
    }

    @Test
    fun getEnglishCalendar_agreesWithConverterAcrossASampledYear() {
        // Every derived field has to match, otherwise a Gregorian grid would disagree with the
        // Gregorian date the Bikram Sambat grid reports for the same day.
        (1..12).forEach { month ->
            val totalDays = NepaliDateConverter.getTotalDaysInEnglishMonth(2026, month)
            (1..totalDays).forEach { dayOfMonth ->
                val nepali = NepaliDateConverter.convertEnglishToNepali(2026, month, dayOfMonth)
                val viaNepali = NepaliDateConverter.convertNepaliToEnglish(
                    nepali.year, nepali.month, nepali.dayOfMonth
                )
                val direct = NepaliDateConverter.getEnglishCalendar(2026, month, dayOfMonth)
                assertEquals(viaNepali, direct, "2026-$month-$dayOfMonth")
            }
        }
    }

    @Test
    fun getEnglishCalendar_dayOutOfMonthThrows() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishCalendar(2026, 2, 29)
        }
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.getEnglishCalendar(2026, 9, 0)
        }
    }

    @Test
    fun getEnglishCalendarsInMonth_coversEveryDayInOrder() {
        val days = NepaliDateConverter.getEnglishCalendarsInMonth(2026, 9)
        assertEquals(30, days.size)
        days.forEachIndexed { index, calendar ->
            assertEquals(index + 1, calendar.dayOfMonth)
            assertEquals(9, calendar.month)
            assertEquals(2026, calendar.year)
        }
    }

    // Batched English to Nepali

    @Test
    fun getNepaliCalendarsInEnglishMonth_agreesWithPerDayConversion() {
        // The batch path converts once and advances; the reference walks from the year anchor every
        // time. They must not drift. Sampled across a decade rather than the full table for speed.
        (2020..2029).forEach { year ->
            listOf(1, 4, 7, 12).forEach { month ->
                val batched = NepaliDateConverter.getNepaliCalendarsInEnglishMonth(year, month)
                val totalDays = NepaliDateConverter.getTotalDaysInEnglishMonth(year, month)
                assertEquals(totalDays, batched.size, "$year-$month size")

                batched.forEachIndexed { index, actual ->
                    val expected =
                        NepaliDateConverter.convertEnglishToNepali(year, month, index + 1)
                    assertEquals(expected, actual, "$year-$month-${index + 1}")
                }
            }
        }
    }

    @Test
    fun getNepaliCalendarsInEnglishMonth_matchesTheBikramSambatGridsOwnCalendars() {
        // A Gregorian grid resolves each cell to a Bikram Sambat date; that date must be identical
        // to the one the Bikram Sambat grid builds, or the two grids would disagree on selection.
        val batched = NepaliDateConverter.getNepaliCalendarsInEnglishMonth(2026, 9)
        batched.forEach { nepali ->
            assertNotNull(nepali)
            assertEquals(
                NepaliDateConverter.getNepaliCalendar(
                    nepali.year, nepali.month, nepali.dayOfMonth
                ),
                nepali
            )
        }
    }

    @Test
    fun getEnglishCalendarsInNepaliMonth_agreesWithPerDayConversion() {
        (2080..2085).forEach { nepaliYear ->
            listOf(1, 6, 9, 12).forEach { nepaliMonth ->
                val batched =
                    NepaliDateConverter.getEnglishCalendarsInNepaliMonth(nepaliYear, nepaliMonth)
                val totalDays =
                    NepaliDateConverter.getTotalDaysInNepaliMonth(nepaliYear, nepaliMonth)
                assertEquals(totalDays, batched.size, "$nepaliYear-$nepaliMonth size")

                batched.forEachIndexed { index, actual ->
                    val expected = NepaliDateConverter.convertNepaliToEnglish(
                        nepaliYear, nepaliMonth, index + 1
                    )
                    assertEquals(expected, actual, "$nepaliYear-$nepaliMonth-${index + 1}")
                }
            }
        }
    }

    @Test
    fun getEnglishCalendarsInNepaliMonth_worksPastTheEnglishInputRange() {
        // BS 2100-12 maps into English 2044. EnglishYearRange bounds English input, not output, so
        // the last supported Nepali month must still render its English half.
        val lastMonth = NepaliDateConverter.getEnglishCalendarsInNepaliMonth(2100, 12)
        assertEquals(
            NepaliDateConverter.getTotalDaysInNepaliMonth(2100, 12),
            lastMonth.size
        )
        assertTrue(lastMonth.last().year > NepaliCalendarDefaults.EnglishYearRange.last)
    }

    @Test
    fun getNepaliCalendarsInEnglishMonth_crossesTheNepaliYearBoundary() {
        // Mid-April is where the Bikram Sambat year rolls over, so one English month spans two.
        val april2026 = NepaliDateConverter.getNepaliCalendarsInEnglishMonth(2026, 4)
        val years = april2026.mapNotNull { it?.year }.distinct()
        assertEquals(listOf(2082, 2083), years)
    }

    @Test
    fun getNepaliCalendarsInEnglishMonth_nullsOutDaysBeforeTheAnchor() {
        // English 1913-04-13 is the earliest convertible day, so 1..12 have no Nepali equivalent.
        val april1913 = NepaliDateConverter.getNepaliCalendarsInEnglishMonth(1913, 4)
        assertEquals(30, april1913.size)
        (0 until 12).forEach { index ->
            assertNull(april1913[index], "1913-04-${index + 1}")
        }
        val anchor = april1913[12]
        assertNotNull(anchor)
        assertEquals(1970, anchor.year)
        assertEquals(1, anchor.month)
        assertEquals(1, anchor.dayOfMonth)

        assertTrue(NepaliDateConverter.getNepaliCalendarsInEnglishMonth(1913, 1).all { it == null })
    }

    @Test
    fun isEnglishDateConvertible_guardsTheAnchorNotJustTheYear() {
        assertFalse(NepaliDateConverter.isEnglishDateConvertible(1913, 1, 1))
        assertFalse(NepaliDateConverter.isEnglishDateConvertible(1913, 4, 12))
        assertTrue(NepaliDateConverter.isEnglishDateConvertible(1913, 4, 13))
        assertTrue(NepaliDateConverter.isEnglishDateConvertible(2043, 12, 31))
        assertFalse(NepaliDateConverter.isEnglishDateConvertible(2044, 1, 1))
    }

    // Conversion bounds

    @Test
    fun convertibleBounds_matchTheConversionAnchors() {
        val min = NepaliCalendarDefaults.minConvertibleEnglishDate
        assertEquals(1913, min.year)
        assertEquals(4, min.month)
        assertEquals(13, min.dayOfMonth)

        val max = NepaliCalendarDefaults.maxConvertibleEnglishDate
        assertEquals(NepaliCalendarDefaults.EnglishYearRange.last, max.year)
        assertEquals(12, max.month)
        assertEquals(31, max.dayOfMonth)

        assertTrue(
            NepaliDateConverter.isEnglishDateConvertible(min.year, min.month, min.dayOfMonth)
        )
        assertTrue(
            NepaliDateConverter.isEnglishDateConvertible(max.year, max.month, max.dayOfMonth)
        )
    }

    @Test
    fun gregorianYearRangeFor_defaultRangeIsClampedIntoTheEnglishRange() {
        // BS 2100-12 reaches into English 2044, past what the converter accepts.
        assertEquals(
            NepaliCalendarDefaults.EnglishYearRange,
            NepaliCalendarDefaults.gregorianYearRangeFor(NepaliCalendarDefaults.NepaliYearRange)
        )
        assertEquals(
            NepaliCalendarDefaults.EnglishYearRange,
            NepaliCalendarDefaults.GregorianYearRange
        )
    }

    @Test
    fun gregorianYearRangeFor_narrowRangeSpansTheOverlappingEnglishYears() {
        // BS 2082-01-01 falls in English 2025, BS 2083-12-30 in English 2027.
        assertEquals(IntRange(2025, 2027), NepaliCalendarDefaults.gregorianYearRangeFor(2082..2083))
    }

    @Test
    fun gregorianYearRangeFor_emptyRangeStaysEmpty() {
        assertTrue(NepaliCalendarDefaults.gregorianYearRangeFor(IntRange.EMPTY).isEmpty())
    }

    @Test
    fun gregorianYearRangeFor_outOfTableYearsAreClamped() {
        assertEquals(
            NepaliCalendarDefaults.EnglishYearRange,
            NepaliCalendarDefaults.gregorianYearRangeFor(1900..2200)
        )
    }

    // MonthCalendar

    @Test
    fun monthCalendar_indexInCountsTwelveMonthsPerYear() {
        val month = MonthCalendar(
            calendarSystem = CalendarSystem.GREGORIAN,
            year = 2026,
            month = 9,
            totalDaysInMonth = 30,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4
        )
        assertEquals((2026 - 1913) * 12 + 8, month.indexIn(NepaliCalendarDefaults.EnglishYearRange))
    }

    @Test
    fun monthCalendar_roundTripsThroughNepaliMonthCalendar() {
        val nepaliMonth = NepaliDateConverter.getNepaliMonthCalendar(2083, 6)
        val generic = nepaliMonth.toMonthCalendar()
        assertEquals(CalendarSystem.BIKRAM_SAMBAT, generic.calendarSystem)
        assertEquals(nepaliMonth, generic.toNepaliMonthCalendar())
        assertEquals(
            nepaliMonth.indexIn(NepaliCalendarDefaults.NepaliYearRange),
            generic.indexIn(NepaliCalendarDefaults.NepaliYearRange)
        )
    }

    // Round trip across the whole supported table

    @Test
    fun everyNepaliMonthStartRoundTripsThroughGregorian() {
        NepaliCalendarDefaults.NepaliYearRange.forEach { nepaliYear ->
            (1..12).forEach { nepaliMonth ->
                val english = NepaliDateConverter.convertNepaliToEnglish(nepaliYear, nepaliMonth, 1)
                if (english.year !in NepaliCalendarDefaults.EnglishYearRange) return@forEach

                val back = NepaliDateConverter.convertEnglishToNepali(
                    english.year, english.month, english.dayOfMonth
                )
                assertEquals(nepaliYear, back.year, "$nepaliYear-$nepaliMonth year")
                assertEquals(nepaliMonth, back.month, "$nepaliYear-$nepaliMonth month")
                assertEquals(1, back.dayOfMonth, "$nepaliYear-$nepaliMonth day")
            }
        }
    }
}
