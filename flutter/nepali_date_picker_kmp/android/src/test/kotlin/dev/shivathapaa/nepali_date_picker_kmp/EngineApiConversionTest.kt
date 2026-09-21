// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Runs the conversion half of the engine host against the real engine, with
 * the golden vectors the Kotlin, npm and Python suites already hold.
 *
 * A mismatch here is a wiring defect in the bridge, never a calendar one: the
 * same table answers all four ecosystems.
 */
internal class EngineApiConversionTest {

    private val api = EngineApiImpl()

    private fun date(year: Long, month: Long, dayOfMonth: Long) =
        DateDto(year = year, month = month, dayOfMonth = dayOfMonth)

    @Test
    fun theYearRangesMatchThePublishedBounds() {
        assertEquals(1970L, api.getBsYearRange().first)
        assertEquals(2100L, api.getBsYearRange().last)
        assertEquals(1913L, api.getAdYearRange().first)
        assertEquals(2043L, api.getAdYearRange().last)
    }

    @Test
    fun theGregorianRangeCoversTheBikramSambatYearsItWasAskedFor() {
        val range = api.getAdYearRangeForBsYears(2080, 2090)

        assertTrue(range.first <= api.convertBsToAd(2080, 1, 1).year)
        assertTrue(range.last >= api.convertBsToAd(2090, 12, 1).year)
    }

    @Test
    fun theAnchorConvertsBothWays() {
        val bs = api.convertAdToBs(1913, 4, 13)
        assertEquals(1970L, bs.year)
        assertEquals(1L, bs.month)
        assertEquals(1L, bs.dayOfMonth)
        assertEquals(2L, bs.era)
        assertEquals(1L, bs.dayOfWeek, "1970-01-01 BS was a Sunday")

        val ad = api.convertBsToAd(1970, 1, 1)
        assertEquals(1913L, ad.year)
        assertEquals(4L, ad.month)
        assertEquals(13L, ad.dayOfMonth)
        assertEquals(1L, ad.era)
    }

    @Test
    fun theKnownNewYearVectorsHold() {
        val vectors = listOf(
            Vector(2077, 1, 1, 2020, 4, 13),
            Vector(2081, 1, 1, 2024, 4, 13),
            Vector(2082, 1, 1, 2025, 4, 14)
        )
        for (vector in vectors) {
            val ad = api.convertBsToAd(vector.bsYear, vector.bsMonth, vector.bsDay)
            assertEquals(
                listOf(vector.adYear, vector.adMonth, vector.adDay),
                listOf(ad.year, ad.month, ad.dayOfMonth)
            )
            val bs = api.convertAdToBs(vector.adYear, vector.adMonth, vector.adDay)
            assertEquals(
                listOf(vector.bsYear, vector.bsMonth, vector.bsDay),
                listOf(bs.year, bs.month, bs.dayOfMonth)
            )
        }
    }

    @Test
    fun theConvertibilityEdgeSitsJustBeforeTheAnchor() {
        assertTrue(api.isAdDateConvertible(1913, 4, 13))
        assertFalse(api.isAdDateConvertible(1913, 4, 12))
    }

    @Test
    fun todayAndNowComeFromTheKathmanduClock() {
        assertEquals(2L, api.getTodayBs().era)
        assertEquals(1L, api.getTodayAd().era)
        assertTrue(api.getTodayBs().year in 1970..2100)
        assertTrue(api.getCurrentTime().hour in 0..23)
        assertTrue(api.getCurrentTime().minute in 0..59)
    }

    @Test
    fun aDescribedCalendarAgreesWithItsMonth() {
        val calendar = api.getBsCalendar(2082, 6, 4)

        assertEquals(2L, calendar.era)
        assertEquals(api.getTotalDaysInBsMonth(2082, 6), calendar.totalDaysInMonth)
        assertTrue(calendar.dayOfWeek in 1..7)
        assertEquals(1L, api.getAdCalendar(2025, 9, 20).era)
    }

    @Test
    fun monthMetadataMatchesTheTable() {
        val bsMonth = api.getBsMonth(2082, 6)
        assertEquals(api.getTotalDaysInBsMonth(2082, 6), bsMonth.totalDaysInMonth)
        assertTrue(bsMonth.daysFromStartOfWeekToFirstOfMonth in 0..6)

        assertEquals(30L, api.getAdMonth(2025, 9).totalDaysInMonth)
        assertEquals(29L, api.getTotalDaysInAdMonth(2024, 2))
        assertEquals(28L, api.getTotalDaysInAdMonth(2025, 2))
    }

    @Test
    fun theAnchorMonthKeepsItsUnconvertibleDaysNull() {
        val days = api.getBsCalendarsInAdMonth(1913, 4)

        assertEquals(30, days.size)
        assertTrue(days.take(12).all { it == null })
        val anchor = assertNotNull(days[12])
        assertEquals(1970L, anchor.year)
        assertEquals(1L, anchor.dayOfMonth)
    }

    @Test
    fun everyDayOfABikramSambatMonthRoundTrips() {
        val days = api.getAdCalendarsInBsMonth(2082, 6)

        assertEquals(api.getTotalDaysInBsMonth(2082, 6).toInt(), days.size)
        days.forEachIndexed { index, day ->
            val back = api.convertAdToBs(day.year, day.month, day.dayOfMonth)
            assertEquals(
                listOf(2082L, 6L, index + 1L),
                listOf(back.year, back.month, back.dayOfMonth)
            )
        }
    }

    @Test
    fun dayArithmeticIsReversible() {
        val forward = api.addDaysToBsDate(2082, 6, 4, 30)
        val back = api.addDaysToBsDate(forward.year, forward.month, forward.dayOfMonth, -30)

        assertEquals(listOf(2082L, 6L, 4L), listOf(back.year, back.month, back.dayOfMonth))
    }

    @Test
    fun daysBetweenAreEndExclusive() {
        assertEquals(11L, api.getBsDaysBetween(date(2082, 6, 1), date(2082, 6, 12)))
        assertEquals(12L, api.getAdDaysBetween(date(2025, 9, 1), date(2025, 9, 13)))
        assertEquals(0L, api.getBsDaysBetween(date(2082, 6, 1), date(2082, 6, 1)))
    }

    @Test
    fun comparisonOrdersDatesInTime() {
        assertTrue(api.compareBsDates(date(2082, 6, 1), date(2082, 6, 12)) < 0)
        assertEquals(0L, api.compareBsDates(date(2082, 6, 1), date(2082, 6, 1)))
        assertTrue(api.compareBsDates(date(2082, 7, 1), date(2082, 6, 12)) > 0)
    }

    @Test
    fun aDateOutsideTheTableIsRefusedByTheEngine() {
        assertFailsWithIllegalArgument { api.convertBsToAd(1969, 1, 1) }
        assertFailsWithIllegalArgument { api.convertBsToAd(2082, 13, 1) }
        assertFailsWithIllegalArgument { api.getBsCalendar(2101, 1, 1) }
    }

    @Test
    fun theWireParsersAnswerNullRatherThanThrowOnJunk() {
        assertNull(api.wireParseDate("not a date", DatePatternDto.YYYY_DASH_MM_DASH_DD))
        assertNull(api.wireParseTime("not a time"))
    }

    private data class Vector(
        val bsYear: Long,
        val bsMonth: Long,
        val bsDay: Long,
        val adYear: Long,
        val adMonth: Long,
        val adDay: Long
    )

    private fun assertFailsWithIllegalArgument(block: () -> Unit) {
        val failed = runCatching(block).exceptionOrNull()
        assertTrue(
            failed is IllegalArgumentException || failed is IllegalStateException,
            "expected the engine to refuse the input, got $failed"
        )
    }
}
