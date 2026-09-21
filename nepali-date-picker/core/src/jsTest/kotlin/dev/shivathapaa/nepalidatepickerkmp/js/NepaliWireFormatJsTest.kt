/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp.js

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Exercises the wire-format wrapper npm consumers see. The format rules themselves are covered by
 * `commonTest`; this guards the wrapper's own responsibilities: pattern-name coercion, the nullable
 * parse result, and that format and parse are inverses across the exported boundary.
 */
class NepaliWireFormatJsTest {

    @Test
    fun formatsTheCanonicalDateWireForm() {
        assertEquals("2082-02-14", formatBsDateText(2082, 2, 14, "yyyy-mm-dd", "latin"))
    }

    @Test
    fun formatsEverySupportedPattern() {
        assertEquals("2082/02/14", formatBsDateText(2082, 2, 14, "yyyy/mm/dd", null))
        assertEquals("14/02/2082", formatBsDateText(2082, 2, 14, "dd/mm/yyyy", null))
        assertEquals("14-02-2082", formatBsDateText(2082, 2, 14, "dd-mm-yyyy", null))
    }

    @Test
    fun formatsDevanagariDigitsOnRequest() {
        assertEquals("२०८२-०२-१४", formatBsDateText(2082, 2, 14, "yyyy-mm-dd", "devanagari"))
    }

    @Test
    fun unrecognizedPatternFallsBackToTheWireForm() {
        assertEquals("2082-02-14", formatBsDateText(2082, 2, 14, "not-a-pattern", null))
    }

    @Test
    fun parsesTheCanonicalDateWireForm() {
        val parts = assertNotNull(parseBsDateText("2082-02-14", "yyyy-mm-dd"))
        assertEquals(2082, parts.year)
        assertEquals(2, parts.month)
        assertEquals(14, parts.dayOfMonth)
    }

    @Test
    fun parsesDayFirstPatternsAndDevanagariInput() {
        val dayFirst = assertNotNull(parseBsDateText("14/02/2082", "dd/mm/yyyy"))
        assertEquals(2082, dayFirst.year)
        assertEquals(14, dayFirst.dayOfMonth)

        val devanagari = assertNotNull(parseBsDateText("२०८२-०२-१४", "yyyy-mm-dd"))
        assertEquals(2082, devanagari.year)
        assertEquals(2, devanagari.month)
        assertEquals(14, devanagari.dayOfMonth)
    }

    @Test
    fun parseReturnsNullOnMismatch() {
        assertNull(parseBsDateText("2082/02/14", "yyyy-mm-dd"))
        assertNull(parseBsDateText("2082-2-14", "yyyy-mm-dd"))
        assertNull(parseBsDateText("2082-13-14", "yyyy-mm-dd"))
        assertNull(parseBsDateText("2082-02-33", "yyyy-mm-dd"))
        assertNull(parseBsDateText("", "yyyy-mm-dd"))
    }

    @Test
    fun dateRoundTripsThroughEveryPattern() {
        for (pattern in listOf("yyyy-mm-dd", "yyyy/mm/dd", "dd/mm/yyyy", "dd-mm-yyyy")) {
            val text = formatBsDateText(2081, 12, 30, pattern, null)
            val parts = assertNotNull(parseBsDateText(text, pattern), "round trip failed for $pattern")
            assertEquals(2081, parts.year)
            assertEquals(12, parts.month)
            assertEquals(30, parts.dayOfMonth)
        }
    }

    @Test
    fun formatsTheTimeWireForm() {
        assertEquals("09:30:00", formatTimeOfDay(9, 30, 0, 0))
        assertEquals("23:59:59.123456789", formatTimeOfDay(23, 59, 59, 123_456_789))
        assertEquals("00:00:00.000000007", formatTimeOfDay(0, 0, 0, 7))
    }

    @Test
    fun parsesTheTimeWireForm() {
        val time = assertNotNull(parseTimeOfDay("23:59:59.123456789"))
        assertEquals(23, time.hour)
        assertEquals(59, time.minute)
        assertEquals(59, time.second)
        assertEquals(123_456_789, time.nanosecond)
    }

    @Test
    fun parseTimeReturnsNullOnMismatch() {
        assertNull(parseTimeOfDay("12:30"))
        assertNull(parseTimeOfDay("24:00:00"))
        assertNull(parseTimeOfDay("12:60:00"))
        assertNull(parseTimeOfDay("12:00:00.1000000000"))
    }

    @Test
    fun timeRoundTrips() {
        val time = assertNotNull(parseTimeOfDay(formatTimeOfDay(15, 22, 7, 999_999_999)))
        assertEquals(15, time.hour)
        assertEquals(22, time.minute)
        assertEquals(7, time.second)
        assertEquals(999_999_999, time.nanosecond)
    }

    @Test
    fun timestampHelpersStayDistinctFromTheTimeOfDayForm() {
        assertEquals("2024-09-09T09:00:15Z", bsDateTimeToIso(2081, 5, 24, 14, 45, 15, 0))
        assertEquals("14:45:15", formatTimeOfDay(14, 45, 15, 0))
    }
}
