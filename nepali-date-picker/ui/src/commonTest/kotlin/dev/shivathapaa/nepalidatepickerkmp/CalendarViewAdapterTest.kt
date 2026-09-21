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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.coerceIntoConversionTable
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.formatSecondary
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.secondaryMonthLabel
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The calendar seam itself, exercised without Compose so it also runs on the targets where the UI
 * test harness cannot start.
 */
class CalendarViewAdapterTest {

    private val bikramSambat = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
    private val gregorian = adapterFor(CalendarSystem.GREGORIAN)

    // Year ranges

    @Test
    fun aBikramSambatAdapterKeepsTheRangeItWasGiven() {
        assertEquals(2080..2085, adapterFor(CalendarSystem.BIKRAM_SAMBAT, 2080..2085).yearRange)
    }

    @Test
    fun aRangeReachingPastTheConversionTableIsClampedIntoIt() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT, 1900..2200)
        assertEquals(NepaliCalendarDefaults.NepaliYearRange, adapter.yearRange)
    }

    @Test
    fun aGregorianAdapterDerivesItsRangeFromTheBikramSambatOne() {
        assertEquals(
            NepaliCalendarDefaults.gregorianYearRangeFor(NepaliCalendarDefaults.NepaliYearRange),
            gregorian.yearRange
        )
        assertTrue(
            gregorian.yearRange.first in NepaliCalendarDefaults.EnglishYearRange &&
                    gregorian.yearRange.last in NepaliCalendarDefaults.EnglishYearRange,
            "a Gregorian pager must stay inside the convertible English years"
        )
    }

    @Test
    fun aGregorianAdapterNarrowsWithTheBikramSambatRangeItIsBuiltFrom() {
        // BS 2082-01-01 falls in English 2025 and BS 2083-12-30 in English 2027.
        assertEquals(2025..2027, adapterFor(CalendarSystem.GREGORIAN, 2082..2083).yearRange)
    }

    // Pager positions

    @Test
    fun monthAtAndIndexInAreInversesAcrossBothCalendars() {
        listOf(bikramSambat, gregorian).forEach { adapter ->
            val lastIndex = numberOfMonthsInRange(adapter.yearRange) - 1
            listOf(0, 1, 11, 12, lastIndex / 2, lastIndex - 1, lastIndex).forEach { index ->
                assertEquals(
                    index,
                    adapter.monthAt(index).indexIn(adapter.yearRange),
                    "${adapter.calendarSystem} lost index $index"
                )
            }
        }
    }

    @Test
    fun monthAtKeepsAnIndexOutsideTheRangeInsideTheTable() {
        listOf(bikramSambat, gregorian).forEach { adapter ->
            val lastIndex = numberOfMonthsInRange(adapter.yearRange) - 1
            assertEquals(adapter.monthOf(adapter.yearRange.first, 1), adapter.monthAt(-5))
            val pastTheEnd = adapter.monthAt(lastIndex + 5)
            assertTrue(
                pastTheEnd.year in adapter.yearRange && pastTheEnd.month in 1..12,
                "${adapter.calendarSystem} escaped its range at index ${lastIndex + 5}: $pastTheEnd"
            )
        }
    }

    @Test
    fun monthContainingFollowsTheDateInEitherCalendar() {
        val date = SimpleDate(2083, 6, 1)
        assertEquals(bikramSambat.monthOf(2083, 6), bikramSambat.monthContaining(date))
        // BS 2083-06-01 is AD 2026-09-17.
        assertEquals(gregorian.monthOf(2026, 9), gregorian.monthContaining(date))
    }

    @Test
    fun monthContainingClampsADateThePagerCannotReach() {
        // BS 2100-12 maps into English 2044, past the convertible range, so the Gregorian pager has
        // to land on its own last month rather than produce an index it cannot scroll to.
        val pastTheEnd = SimpleDate(2100, 12, 30)
        assertEquals(gregorian.monthOf(gregorian.yearRange.last, 12), gregorian.monthContaining(pastTheEnd))

        // A year the conversion table has no row for is pulled to its first day, BS 1970-01-01, which
        // is English 1913-04-13, so the grid opens on that April rather than on the pager's first month.
        val beforeTheStart = SimpleDate(1900, 1, 1)
        assertEquals(gregorian.monthOf(1913, 4), gregorian.monthContaining(beforeTheStart))
        assertEquals(bikramSambat.monthOf(1970, 1), bikramSambat.monthContaining(beforeTheStart))
    }

    // Days of a month

    @Test
    fun daysInReturnsOneEntryPerDayOfTheMonth() {
        listOf(bikramSambat.monthOf(2083, 4), gregorian.monthOf(2026, 2)).forEachIndexed { index, month ->
            val adapter = if (index == 0) bikramSambat else gregorian
            assertEquals(month.totalDaysInMonth, adapter.daysIn(month).size)
        }
    }

    @Test
    fun aBikramSambatDayIsItsOwnCanonicalDate() {
        bikramSambat.daysIn(bikramSambat.monthOf(2083, 4)).forEach { day ->
            assertEquals(day.displayed, day.canonical)
        }
    }

    @Test
    fun theSecondaryHalfIsOnlyResolvedWhenAskedFor() {
        val month = bikramSambat.monthOf(2083, 4)
        assertTrue(bikramSambat.daysIn(month).all { it.secondary == null })
        assertTrue(bikramSambat.daysIn(month, withSecondary = true).all { it.secondary != null })
    }

    @Test
    fun aGregorianDayAlwaysCarriesItsBikramSambatHalf() {
        // Without being asked: the Bikram Sambat date is what the cell selects.
        gregorian.daysIn(gregorian.monthOf(2026, 9)).forEach { day ->
            assertEquals(day.canonical, day.secondary)
            assertNotNull(day.canonical)
        }
    }

    @Test
    fun aGregorianDayBeforeTheConversionAnchorHasNoCanonicalDate() {
        // English 1913-04-13 is the earliest convertible day.
        val april1913 = gregorian.daysIn(gregorian.monthOf(1913, 4))
        assertTrue(april1913.take(12).all { it.canonical == null })
        assertNotNull(april1913[12].canonical)
        assertEquals(SimpleDate(1970, 1, 1), april1913[12].canonical?.toSimpleDate())
    }

    // Years the picker offers

    @Test
    fun aBikramSambatYearMapsToItself() {
        assertEquals(listOf(2083), bikramSambat.canonicalYearsIn(2083))
    }

    @Test
    fun aGregorianYearStraddlesTwoBikramSambatYears() {
        assertEquals(listOf(2082, 2083), gregorian.canonicalYearsIn(2026))
    }

    @Test
    fun aGregorianYearAtTheEdgeOnlyOffersTheBikramSambatYearsTheTableHas() {
        // The first convertible English year only reaches into the first Bikram Sambat year.
        assertEquals(
            listOf(NepaliCalendarDefaults.NepaliYearRange.first),
            gregorian.canonicalYearsIn(NepaliCalendarDefaults.EnglishYearRange.first)
        )
    }

    // Parsing and formatting

    @Test
    fun eachAdapterParsesItsOwnCalendar() {
        val nepali = assertNotNull(bikramSambat.parse("20830601"))
        assertEquals(SimpleDate(2083, 6, 1), nepali.toSimpleDate())

        val english = assertNotNull(gregorian.parse("20260917"))
        assertEquals(SimpleDate(2026, 9, 17), english.toSimpleDate())
    }

    @Test
    fun aDayThatDoesNotExistParsesToTheInvalidSentinelRatherThanNull() {
        // April has 30 days, so the 31st is a real-looking string for a day that is not a day. The
        // typed-input validator tells the two apart on totalDaysInMonth.
        val impossible = assertNotNull(gregorian.parse("20260431"))
        assertEquals(-1, impossible.totalDaysInMonth)
    }

    @Test
    fun somethingThatIsNotADateParsesToNull() {
        listOf("", "2083", "not-a-date", "208306011").forEach { input ->
            assertNull(bikramSambat.parse(input), "expected null for \"$input\"")
            assertNull(gregorian.parse(input), "expected null for \"$input\"")
        }
    }

    @Test
    fun calendarOfRejectsADayThatIsNotInItsCalendar() {
        assertNotNull(gregorian.calendarOf(SimpleDate(2026, 2, 28)))
        assertNull(gregorian.calendarOf(SimpleDate(2026, 2, 30)))
        assertNull(bikramSambat.calendarOf(SimpleDate(2083, 13, 1)))
    }

    @Test
    fun formatWritesTheDateInTheAdapterSOwnCalendar() {
        val locale = TestLocale.copy(dateFormat = NepaliDateFormatStyle.FULL)
        val nepaliDay = bikramSambat.daysIn(bikramSambat.monthOf(2083, 6)).first().displayed
        val englishDay = gregorian.daysIn(gregorian.monthOf(2026, 9)).first().displayed

        assertTrue(bikramSambat.format(nepaliDay, locale).contains("2083"))
        assertTrue(gregorian.format(englishDay, locale).contains("2026"))
    }

    @Test
    fun eachAdapterNamesItsOwnMonthsAndFieldLabel() {
        assertEquals("Baisakh", bikramSambat.monthName(1, NepaliDatePickerLang.ENGLISH, NameFormat.FULL))
        assertEquals("January", gregorian.monthName(1, NepaliDatePickerLang.ENGLISH, NameFormat.FULL))
        assertEquals("Nepali Date", bikramSambat.dateFieldLabel(NepaliDatePickerLang.ENGLISH))
        assertEquals("English Date", gregorian.dateFieldLabel(NepaliDatePickerLang.ENGLISH))
    }

    // Moving between the calendars

    @Test
    fun toCanonicalAndFromCanonicalAreInverses() {
        val day = gregorian.daysIn(gregorian.monthOf(2026, 9))[16].displayed
        val canonical = assertNotNull(gregorian.toCanonical(day))
        assertEquals(SimpleDate(2083, 6, 1), canonical.toSimpleDate())
        assertEquals(day.toSimpleDate(), gregorian.fromCanonical(canonical)?.toSimpleDate())
    }

    @Test
    fun aBikramSambatAdapterHandsBothDirectionsBackUntouched() {
        val day = bikramSambat.daysIn(bikramSambat.monthOf(2083, 6)).first().displayed
        assertEquals(day, bikramSambat.toCanonical(day))
        assertEquals(day, bikramSambat.fromCanonical(day))
    }

    @Test
    fun toCanonicalRefusesADayBeforeTheConversionAnchor() {
        val beforeAnchor = assertNotNull(gregorian.calendarOf(SimpleDate(1913, 4, 12)))
        assertNull(gregorian.toCanonical(beforeAnchor))
    }

    @Test
    fun fromCanonicalReachesPastThePagerAtTheEndOfTheTable() {
        // The English year range bounds English input, not output, so the last Bikram Sambat month
        // still converts. It lands in a year no Gregorian pager covers, which is why
        // monthContaining has to clamp rather than trust this result.
        val pastTheEnd = assertNotNull(bikramSambat.calendarOf(SimpleDate(2100, 12, 30)))
        val english = assertNotNull(gregorian.fromCanonical(pastTheEnd))
        assertTrue(
            english.year > gregorian.yearRange.last,
            "expected a year past the pager's last, got ${english.year}"
        )
    }

    // The second month label under the year button

    @Test
    fun theSecondaryLabelNamesTheStraddledMonthsOfTheOtherCalendar() {
        val model = NepaliCalendarModel(TestLocale)
        val month = bikramSambat.monthOf(2083, 6)
        val label = bikramSambat.secondaryMonthLabel(
            days = bikramSambat.daysIn(month, withSecondary = true),
            calendarModel = model,
            language = NepaliDatePickerLang.ENGLISH
        )
        // Asoj 2083 runs from mid-September into mid-October 2026.
        assertEquals("Sep/Oct 2026", label)
    }

    @Test
    fun theSecondaryLabelReadsTheOtherWayRoundInAGregorianGrid() {
        val model = NepaliCalendarModel(TestLocale)
        val month = gregorian.monthOf(2026, 9)
        val label = gregorian.secondaryMonthLabel(
            days = gregorian.daysIn(month, withSecondary = true),
            calendarModel = model,
            language = NepaliDatePickerLang.ENGLISH
        )
        assertNotNull(label)
        assertTrue(label.contains("/"), "September 2026 spans two Bikram Sambat months, got $label")
        assertTrue(label.endsWith("2083"), "expected the Bikram Sambat year, got $label")
    }

    @Test
    fun theSecondaryHalfIsSpokenInTheCalendarThatIsNotOnScreen() {
        val model = NepaliCalendarModel(TestLocale)
        val locale = TestLocale.copy(dateFormat = NepaliDateFormatStyle.LONG)
        val nepaliDay = bikramSambat.daysIn(bikramSambat.monthOf(2083, 6), withSecondary = true)
            .first()
        val englishDay = gregorian.daysIn(gregorian.monthOf(2026, 9), withSecondary = true).first()

        // Each adapter's secondary half reads exactly as the other adapter writes its own dates,
        // which is what keeps the two halves of a cell from being swapped.
        assertEquals(
            gregorian.format(assertNotNull(nepaliDay.secondary), locale),
            bikramSambat.formatSecondary(
                secondaryDate = assertNotNull(nepaliDay.secondary),
                calendarModel = model,
                locale = locale
            )
        )
        assertEquals(
            bikramSambat.format(assertNotNull(englishDay.secondary), locale),
            gregorian.formatSecondary(
                secondaryDate = assertNotNull(englishDay.secondary),
                calendarModel = model,
                locale = locale
            )
        )
    }

    @Test
    fun theSecondaryLabelIsNullWhenNothingConverts() {
        val model = NepaliCalendarModel(TestLocale)
        // Every day of March 1913 predates the conversion anchor.
        val month = gregorian.monthOf(1913, 3)
        assertNull(
            gregorian.secondaryMonthLabel(
                days = gregorian.daysIn(month, withSecondary = true),
                calendarModel = model,
                language = NepaliDatePickerLang.ENGLISH
            )
        )
    }

    // Pulling a caller's date inside the table

    @Test
    fun coerceIntoConversionTableClampsEveryComponent() {
        val model = NepaliCalendarModel(TestLocale)
        assertEquals(
            SimpleDate(NepaliCalendarDefaults.NepaliYearRange.first, 1, 1),
            model.coerceIntoConversionTable(SimpleDate(1900, 0, 0))
        )
        assertEquals(
            SimpleDate(NepaliCalendarDefaults.NepaliYearRange.last, 12, 1),
            model.coerceIntoConversionTable(SimpleDate(2200, 99, 1))
        )
        // Day 32 only exists in some months; Asoj 2083 has 30 days, so it clamps to the 30th.
        val asoj = bikramSambat.monthOf(2083, 6)
        assertEquals(
            SimpleDate(2083, 6, asoj.totalDaysInMonth),
            model.coerceIntoConversionTable(SimpleDate(2083, 6, 32))
        )
    }

    @Test
    fun anInRangeDatePassesThroughUnchanged() {
        val model = NepaliCalendarModel(TestLocale)
        val date = SimpleDate(2083, 6, 1)
        assertEquals(date, model.coerceIntoConversionTable(date))
    }
}
