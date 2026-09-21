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

import dev.shivathapaa.nepalidatepickerkmp.data.NepaliTimeFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NepaliTimeFormatterTests {

    // format

    @Test
    fun format_dropsZeroNanoseconds() {
        assertEquals("09:30:00", NepaliTimeFormatter.format(SimpleTime(9, 30, 0, 0)))
    }

    @Test
    fun format_includesNonZeroNanoseconds() {
        assertEquals("23:59:59.123456789", NepaliTimeFormatter.format(SimpleTime(23, 59, 59, 123_456_789)))
    }

    @Test
    fun format_padsFractionToNineDigits() {
        assertEquals("00:00:00.000000007", NepaliTimeFormatter.format(SimpleTime(0, 0, 0, 7)))
    }

    @Test
    fun format_padsSingleDigitClockFields() {
        assertEquals("01:02:03", NepaliTimeFormatter.format(SimpleTime(1, 2, 3, 0)))
    }

    // parse

    @Test
    fun parse_wholeSeconds() {
        assertEquals(SimpleTime(9, 30, 0, 0), NepaliTimeFormatter.parse("09:30:00"))
    }

    @Test
    fun parse_fullPrecision() {
        assertEquals(SimpleTime(23, 59, 59, 123_456_789), NepaliTimeFormatter.parse("23:59:59.123456789"))
    }

    @Test
    fun parse_acceptsUnpaddedClockFields() {
        assertEquals(SimpleTime(9, 5, 3, 0), NepaliTimeFormatter.parse("9:5:3"))
    }

    @Test
    fun parse_readsFractionAsNanosecondCount() {
        assertEquals(SimpleTime(0, 0, 0, 7), NepaliTimeFormatter.parse("00:00:00.7"))
    }

    @Test
    fun parse_rejectsHourOutOfRange() {
        assertNull(NepaliTimeFormatter.parse("24:00:00"))
    }

    @Test
    fun parse_rejectsMinuteOutOfRange() {
        assertNull(NepaliTimeFormatter.parse("12:60:00"))
    }

    @Test
    fun parse_rejectsSecondOutOfRange() {
        assertNull(NepaliTimeFormatter.parse("12:00:60"))
    }

    @Test
    fun parse_rejectsFractionOutOfRange() {
        assertNull(NepaliTimeFormatter.parse("12:00:00.1000000000"))
    }

    @Test
    fun parse_rejectsMissingSecondField() {
        assertNull(NepaliTimeFormatter.parse("12:30"))
    }

    @Test
    fun parse_rejectsExtraField() {
        assertNull(NepaliTimeFormatter.parse("12:30:00:00"))
    }

    @Test
    fun parse_rejectsNonNumericField() {
        assertNull(NepaliTimeFormatter.parse("ab:30:00"))
    }

    @Test
    fun parse_rejectsEmptyFraction() {
        assertNull(NepaliTimeFormatter.parse("12:30:00."))
    }

    @Test
    fun parse_rejectsNegativeField() {
        assertNull(NepaliTimeFormatter.parse("-1:30:00"))
    }

    @Test
    fun parse_rejectsSurroundingWhitespace() {
        assertNull(NepaliTimeFormatter.parse(" 09:30:00"))
    }

    @Test
    fun parse_acceptsDevanagariDigits() {
        assertEquals(SimpleTime(9, 30, 0, 0), NepaliTimeFormatter.parse("०९:३०:००"))
    }

    @Test
    fun parse_acceptsMixedDigitScripts() {
        assertEquals(SimpleTime(9, 30, 0, 0), NepaliTimeFormatter.parse("०९:30:00"))
    }

    // round trip

    @Test
    fun roundTrip_coversEveryHourAndBoundaryFraction() {
        val fractions = listOf(0, 1, 7, 999_999_999)
        for (hour in 0..23) {
            for (fraction in fractions) {
                val original = SimpleTime(hour, 59, 59, fraction)
                assertEquals(original, NepaliTimeFormatter.parse(NepaliTimeFormatter.format(original)))
            }
        }
    }

    @Test
    fun roundTrip_midnightAndEndOfDay() {
        val bounds = listOf(SimpleTime(0, 0, 0, 0), SimpleTime(23, 59, 59, 999_999_999))
        for (time in bounds) {
            assertEquals(time, NepaliTimeFormatter.parse(NepaliTimeFormatter.format(time)))
        }
    }

    @Test
    fun parse_rejectsASignedField() {
        val signed = listOf(
            "+09:30:00",
            "-09:30:00",
            "09:+30:00",
            "09:30:+00",
            "09:30:00.+7",
            "09:30:00.-7"
        )
        for (input in signed) {
            assertNull(NepaliTimeFormatter.parse(input), "'$input' is not a time format would write")
        }
    }

    @Test
    fun parse_rejectsAnEmptyField() {
        val empty = listOf(":30:00", "09::00", "09:30:", "09:30:00.", "::")
        for (input in empty) {
            assertNull(NepaliTimeFormatter.parse(input), "'$input' has a field with no digits")
        }
    }
}
