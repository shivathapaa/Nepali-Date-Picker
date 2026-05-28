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

import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NepaliDateFormatterTests {

    private val sample = SimpleDate(2082, 2, 14)

    // ── format ──────────────────────────────────────────────────────────────

    @Test
    fun format_yyyySlashLatin() {
        assertEquals("2082/02/14", NepaliDateFormatter.format(sample, Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun format_yyyyDashLatin() {
        assertEquals("2082-02-14", NepaliDateFormatter.format(sample, Pattern.YYYY_DASH_MM_DASH_DD))
    }

    @Test
    fun format_ddSlashLatin() {
        assertEquals("14/02/2082", NepaliDateFormatter.format(sample, Pattern.DD_SLASH_MM_SLASH_YYYY))
    }

    @Test
    fun format_ddDashLatin() {
        assertEquals("14-02-2082", NepaliDateFormatter.format(sample, Pattern.DD_DASH_MM_DASH_YYYY))
    }

    @Test
    fun format_yyyySlashDevanagari() {
        assertEquals(
            "२०८२/०२/१४",
            NepaliDateFormatter.format(sample, Pattern.YYYY_SLASH_MM_SLASH_DD, DigitScript.DEVANAGARI)
        )
    }

    @Test
    fun format_singleDigitMonthAndDay_areZeroPadded() {
        val d = SimpleDate(2082, 1, 5)
        assertEquals("2082/01/05", NepaliDateFormatter.format(d, Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    // ── parse — happy path ─────────────────────────────────────────────────

    @Test
    fun parse_yyyySlashLatin_roundTrip() {
        assertEquals(sample, NepaliDateFormatter.parse("2082/02/14", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun parse_yyyyDashLatin_roundTrip() {
        assertEquals(sample, NepaliDateFormatter.parse("2082-02-14", Pattern.YYYY_DASH_MM_DASH_DD))
    }

    @Test
    fun parse_ddSlashLatin_roundTrip() {
        assertEquals(sample, NepaliDateFormatter.parse("14/02/2082", Pattern.DD_SLASH_MM_SLASH_YYYY))
    }

    @Test
    fun parse_ddDashLatin_roundTrip() {
        assertEquals(sample, NepaliDateFormatter.parse("14-02-2082", Pattern.DD_DASH_MM_DASH_YYYY))
    }

    @Test
    fun parse_yyyySlashDevanagari_roundTrip() {
        assertEquals(sample, NepaliDateFormatter.parse("२०८२/०२/१४", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun parse_ddDashDevanagari_roundTrip() {
        assertEquals(sample, NepaliDateFormatter.parse("१४-०२-२०८२", Pattern.DD_DASH_MM_DASH_YYYY))
    }

    @Test
    fun parse_mixedLatinAndDevanagariDigits_succeeds() {
        // Pasted-from-clipboard-half-IME edge case.
        assertEquals(sample, NepaliDateFormatter.parse("2082/०२/14", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    // ── parse — rejection ──────────────────────────────────────────────────

    @Test
    fun parse_partialInput_returnsNull() {
        assertNull(NepaliDateFormatter.parse("2082/02/1", Pattern.YYYY_SLASH_MM_SLASH_DD))
        assertNull(NepaliDateFormatter.parse("", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun parse_wrongDelimiter_returnsNull() {
        assertNull(NepaliDateFormatter.parse("2082-02-14", Pattern.YYYY_SLASH_MM_SLASH_DD))
        assertNull(NepaliDateFormatter.parse("2082/02/14", Pattern.YYYY_DASH_MM_DASH_DD))
    }

    @Test
    fun parse_nonDigitInDigitSlot_returnsNull() {
        assertNull(NepaliDateFormatter.parse("20a2/02/14", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun parse_monthOutOfRange_returnsNull() {
        assertNull(NepaliDateFormatter.parse("2082/13/14", Pattern.YYYY_SLASH_MM_SLASH_DD))
        assertNull(NepaliDateFormatter.parse("2082/00/14", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun parse_dayOutOfRange_returnsNull() {
        assertNull(NepaliDateFormatter.parse("2082/02/33", Pattern.YYYY_SLASH_MM_SLASH_DD))
        assertNull(NepaliDateFormatter.parse("2082/02/00", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    @Test
    fun parse_day32_allowed_byDesign() {
        // Some BS months have 32 days. Tighter "totalDaysInMonth" check is the
        // caller's responsibility via NepaliSelectableDates + getNepaliCalendar.
        val parsed = NepaliDateFormatter.parse("2082/02/32", Pattern.YYYY_SLASH_MM_SLASH_DD)
        assertNotNull(parsed)
        assertEquals(32, parsed.dayOfMonth)
    }

    @Test
    fun parse_extraTrailingChar_returnsNull() {
        assertNull(NepaliDateFormatter.parse("2082/02/14 ", Pattern.YYYY_SLASH_MM_SLASH_DD))
    }

    // ── round-trip across all patterns ─────────────────────────────────────

    @Test
    fun formatAndParse_roundTripAllPatterns() {
        val dates = listOf(
            SimpleDate(2082, 1, 1),
            SimpleDate(2082, 12, 31),
            SimpleDate(2099, 6, 15),
            SimpleDate(1992, 4, 1)
        )
        for (date in dates) {
            for (pattern in Pattern.entries) {
                val rendered = NepaliDateFormatter.format(date, pattern)
                val parsed = NepaliDateFormatter.parse(rendered, pattern)
                assertEquals(date, parsed, "round-trip failed for $date with $pattern")
            }
        }
    }

    @Test
    fun formatAndParse_devanagariRoundTrip() {
        val rendered = NepaliDateFormatter.format(sample, Pattern.YYYY_SLASH_MM_SLASH_DD, DigitScript.DEVANAGARI)
        assertEquals("२०८२/०२/१४", rendered)
        assertEquals(sample, NepaliDateFormatter.parse(rendered, Pattern.YYYY_SLASH_MM_SLASH_DD))
    }
}
