/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp.js

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the JS export wrapper directly (the layer that npm consumers see). The calendar rules
 * themselves are covered by `commonTest`; this guards the wrapper's own responsibilities: DTO
 * mapping, string-to-enum coercion, and that today resolves through the platform time zone.
 */
class NepaliDateConverterJsTest {

    @Test
    fun convertsAndMapsAllDtoFields() {
        val ad = convertBsToAd(2081, 5, 24)
        assertEquals(2024, ad.year)
        assertEquals(9, ad.month)
        assertEquals(9, ad.dayOfMonth)
        assertEquals(1, ad.era)

        val bs = convertAdToBs(2024, 9, 9)
        assertEquals(2081, bs.year)
        assertEquals(5, bs.month)
        assertEquals(24, bs.dayOfMonth)
        assertEquals(2, bs.era)
    }

    @Test
    fun coercesLanguageAndFormatStrings() {
        assertEquals("Asar", getBsMonthName(3, "full", "en"))
        assertEquals("असार", getBsMonthName(3, "full", "ne"))
        assertEquals("Sunday", getWeekdayName(1, "full", "en"))
        assertEquals("Sun", getWeekdayName(1, "medium", "en"))
    }

    @Test
    fun localizesDigitsBothDirections() {
        assertEquals("२०८२/०२/१४", localizeDigits("2082/02/14", "devanagari"))
        assertEquals("2082", toLatinDigits("२०८२"))
    }

    @Test
    fun exposesRangesAndTotals() {
        val range = getBsYearRange()
        assertEquals(1970, range.first)
        assertEquals(2100, range.last)
        assertEquals(31, getTotalDaysInBsMonth(2081, 1))
    }

    @Test
    fun formatsIsoAndParsesBack() {
        assertEquals("2024-09-09T09:00:15Z", bsDateTimeToIso(2081, 5, 24, 14, 45, 15, 0))
        val parsed = bsDateTimeFromIso("2024-09-09T09:00:15Z")
        assertEquals(2081, parsed.calendar.year)
        assertEquals(5, parsed.calendar.month)
        assertEquals(24, parsed.calendar.dayOfMonth)
    }

    @Test
    fun todayResolvesThroughPlatformTimeZone() {
        val bs = getTodayBs()
        val ad = getTodayAd()
        val roundTrip = convertBsToAd(bs.year, bs.month, bs.dayOfMonth)
        assertEquals(ad.year, roundTrip.year)
        assertEquals(ad.month, roundTrip.month)
        assertEquals(ad.dayOfMonth, roundTrip.dayOfMonth)
        assertTrue(bs.year in 1970..2100)
    }
}
