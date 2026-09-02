/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConversionBoundaryTests {

    // Anchor boundary (Nepali 1970-1-1 ⇔ English 1913-4-13)

    @Test
    fun convertNepaliToEnglish_minNepaliAnchor_returnsMinEnglishAnchor() {
        val result = NepaliDateConverter.convertNepaliToEnglish(1970, 1, 1)
        assertEquals(1913, result.year)
        assertEquals(4, result.month)
        assertEquals(13, result.dayOfMonth)
        assertEquals(1, result.era)
    }

    @Test
    fun convertEnglishToNepali_minEnglishAnchor_returnsMinNepaliAnchor() {
        val result = NepaliDateConverter.convertEnglishToNepali(1913, 4, 13)
        assertEquals(1970, result.year)
        assertEquals(1, result.month)
        assertEquals(1, result.dayOfMonth)
        assertEquals(2, result.era)
    }

    @Test
    fun convertNepaliToEnglish_oneDayAfterAnchor_advancesEnglishByOne() {
        val result = NepaliDateConverter.convertNepaliToEnglish(1970, 1, 2)
        assertEquals(1913, result.year)
        assertEquals(4, result.month)
        assertEquals(14, result.dayOfMonth)
    }

    // Year range boundaries

    @Test
    fun nepaliYearRange_constantsMatchExpected() {
        assertEquals(1970, NepaliCalendarDefaults.NepaliYearRange.first)
        assertEquals(2100, NepaliCalendarDefaults.NepaliYearRange.last)
    }

    @Test
    fun englishYearRange_constantsMatchExpected() {
        assertEquals(1913, NepaliCalendarDefaults.EnglishYearRange.first)
        assertEquals(2043, NepaliCalendarDefaults.EnglishYearRange.last)
    }

    @Test
    fun firstDayOfWeek_isSundayOne() {
        assertEquals(1, NepaliCalendarDefaults.FIRST_DAY_OF_WEEK)
    }

    // Out-of-range Nepali year throws

    @Test
    fun convertNepaliToEnglish_yearBelowRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertNepaliToEnglish(1969, 1, 1)
        }
    }

    @Test
    fun convertNepaliToEnglish_yearAboveRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertNepaliToEnglish(2101, 1, 1)
        }
    }

    @Test
    fun convertNepaliToEnglish_monthBelowOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertNepaliToEnglish(2080, 0, 1)
        }
    }

    @Test
    fun convertNepaliToEnglish_monthAboveTwelve_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertNepaliToEnglish(2080, 13, 1)
        }
    }

    @Test
    fun convertNepaliToEnglish_dayBelowOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertNepaliToEnglish(2080, 1, 0)
        }
    }

    @Test
    fun convertNepaliToEnglish_dayAboveThirtyTwo_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertNepaliToEnglish(2080, 1, 33)
        }
    }

    // Out-of-range English year throws

    @Test
    fun convertEnglishToNepali_yearBelowRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(1912, 12, 31)
        }
    }

    @Test
    fun convertEnglishToNepali_yearAboveRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(2044, 1, 1)
        }
    }

    @Test
    fun convertEnglishToNepali_monthBelowOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(2024, 0, 1)
        }
    }

    @Test
    fun convertEnglishToNepali_monthAboveTwelve_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(2024, 13, 1)
        }
    }

    @Test
    fun convertEnglishToNepali_dayBelowOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(2024, 1, 0)
        }
    }

    @Test
    fun convertEnglishToNepali_dayAboveThirtyOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(2024, 1, 32)
        }
    }

    // Pre-anchor English dates are rejected (previously a silent-wrong loophole)
    // English 1913 dates before the 1913-04-13 anchor have no Nepali equivalent
    // (they would map below Nepali 1970). They used to pass the year-only range
    // check and, because the day-walk ran `repeat(negativeDiff)` zero times,
    // silently returned the anchor (Nepali 1970-01-01). They now throw.

    @Test
    fun convertEnglishToNepali_beforeAnchorInSameYear_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(1913, 1, 1)
        }
    }

    @Test
    fun convertEnglishToNepali_oneDayBeforeAnchor_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.convertEnglishToNepali(1913, 4, 12)
        }
    }

    @Test
    fun convertEnglishToNepali_exactlyOnAnchor_stillSucceeds() {
        val result = NepaliDateConverter.convertEnglishToNepali(1913, 4, 13)
        assertEquals(1970, result.year)
        assertEquals(1, result.month)
        assertEquals(1, result.dayOfMonth)
    }

    // Round trip

    @Test
    fun roundTrip_englishToNepaliAndBack_returnsOriginal_yearStart() {
        val nep = NepaliDateConverter.convertEnglishToNepali(2024, 1, 1)
        val back = NepaliDateConverter.convertNepaliToEnglish(nep.year, nep.month, nep.dayOfMonth)
        assertEquals(2024, back.year)
        assertEquals(1, back.month)
        assertEquals(1, back.dayOfMonth)
    }

    @Test
    fun roundTrip_englishToNepaliAndBack_returnsOriginal_yearEnd() {
        val nep = NepaliDateConverter.convertEnglishToNepali(2024, 12, 31)
        val back = NepaliDateConverter.convertNepaliToEnglish(nep.year, nep.month, nep.dayOfMonth)
        assertEquals(2024, back.year)
        assertEquals(12, back.month)
        assertEquals(31, back.dayOfMonth)
    }

    @Test
    fun roundTrip_englishToNepaliAndBack_returnsOriginal_leapDay() {
        val nep = NepaliDateConverter.convertEnglishToNepali(2024, 2, 29)
        val back = NepaliDateConverter.convertNepaliToEnglish(nep.year, nep.month, nep.dayOfMonth)
        assertEquals(2024, back.year)
        assertEquals(2, back.month)
        assertEquals(29, back.dayOfMonth)
    }

    @Test
    fun roundTrip_nepaliToEnglishAndBack_returnsOriginal_arbitrary() {
        val eng = NepaliDateConverter.convertNepaliToEnglish(2081, 6, 12)
        val back = NepaliDateConverter.convertEnglishToNepali(eng.year, eng.month, eng.dayOfMonth)
        assertEquals(2081, back.year)
        assertEquals(6, back.month)
        assertEquals(12, back.dayOfMonth)
    }

    @Test
    fun roundTrip_nepaliToEnglishAndBack_returnsOriginal_yearBoundary() {
        // Last day of Nepali year 2081 → next day should be 2082-1-1.
        val eng = NepaliDateConverter.convertNepaliToEnglish(2081, 12, 30)
        val back = NepaliDateConverter.convertEnglishToNepali(eng.year, eng.month, eng.dayOfMonth)
        assertEquals(2081, back.year)
        assertEquals(12, back.month)
        assertEquals(30, back.dayOfMonth)
    }

    @Test
    fun convertNepaliToEnglish_maxNepaliInRange_succeeds() {
        // 2100-12-30 lands outside EnglishYearRange (2044) but the converter
        // itself only validates the Nepali input, so this should still succeed.
        val result = NepaliDateConverter.convertNepaliToEnglish(2100, 12, 30)
        assertTrue(result.year > 2043)
    }
}
