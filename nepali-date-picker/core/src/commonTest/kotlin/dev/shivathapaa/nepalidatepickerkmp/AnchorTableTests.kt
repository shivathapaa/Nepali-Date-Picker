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
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.ReferenceDate
import dev.shivathapaa.nepalidatepickerkmp.data.englishDateMap
import dev.shivathapaa.nepalidatepickerkmp.data.nepaliDateMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * The per-year conversion anchors. Both maps hold the same set of English/Bikram Sambat pairs, one
 * keyed by English year and one by Bikram Sambat year, and the day-walk converters start from them,
 * so a wrong or missing entry silently shifts a whole year of conversions.
 *
 * The digests below cover every field of every pair, so any change to an anchor has to be deliberate.
 */
class AnchorTableTests {

    private fun CustomCalendar.fingerprint(): String =
        "$year|$month|$dayOfMonth|$era|$firstDayOfMonth|$lastDayOfMonth|$totalDaysInMonth|" +
                "$dayOfWeekInMonth|$dayOfWeek|$dayOfYear|$weekOfMonth|$weekOfYear"

    private fun Map<Int, ReferenceDate>.fingerprint(): String =
        entries.joinToString(";") { (key, value) ->
            "$key>${value.englishDate.fingerprint()}>${value.nepaliDate.fingerprint()}"
        }

    /**
     * FNV-1a over the fingerprint. Long arithmetic wraps identically on every target, so the digest
     * is the same number on JVM, native, JS and Wasm.
     */
    private fun String.digest(): Long {
        var hash = FNV_OFFSET_BASIS
        for (byte in encodeToByteArray()) {
            hash = (hash xor (byte.toLong() and 0xFF)) * FNV_PRIME
        }
        return hash
    }

    @Test
    fun englishDateMap_coversEveryEnglishYearFromFirstAnchorToRangeEnd() {
        assertEquals(130, englishDateMap.size)
        assertEquals((1914..2043).toList(), englishDateMap.keys.toList())
        englishDateMap.forEach { (year, reference) ->
            assertEquals(year, reference.englishDate.year, "englishDateMap key must be its own year")
        }
    }

    @Test
    fun nepaliDateMap_coversEveryNepaliYearFromFirstAnchorToRangeEnd() {
        assertEquals(130, nepaliDateMap.size)
        assertEquals((1971..2100).toList(), nepaliDateMap.keys.toList())
        nepaliDateMap.forEach { (year, reference) ->
            assertEquals(year, reference.nepaliDate.year, "nepaliDateMap key must be its own year")
        }
    }

    @Test
    fun bothMaps_holdTheSamePairsUnderDifferentKeys() {
        englishDateMap.forEach { (englishYear, reference) ->
            val byNepaliYear = nepaliDateMap[reference.nepaliDate.year]
            assertNotNull(
                byNepaliYear,
                "English anchor $englishYear has no counterpart keyed by Bikram Sambat year"
            )
            assertSame(
                reference, byNepaliYear,
                "The two anchor maps must re-key one set of pairs, not duplicate them"
            )
        }
        assertEquals(englishDateMap.size, nepaliDateMap.size)
    }

    @Test
    fun nepaliDateMap_matchesItsExpectedDigest() {
        val fingerprint = nepaliDateMap.fingerprint()
        assertEquals(130, fingerprint.split(";").size)
        assertEquals(
            expectedNepaliAnchorDigest, fingerprint.digest(),
            "An anchor moved. everyAnchorPair_agreesWithTheConverterAcrossTheFullRange names which."
        )
    }

    @Test
    fun englishDateMap_matchesItsExpectedDigest() {
        val fingerprint = englishDateMap.fingerprint()
        assertEquals(130, fingerprint.split(";").size)
        assertEquals(
            expectedEnglishAnchorDigest, fingerprint.digest(),
            "An anchor moved. everyAnchorPair_agreesWithTheConverterAcrossTheFullRange names which."
        )
    }

    /**
     * Each anchor is one year past its predecessor, so walking the converter from the previous
     * anchor has to land exactly on it. This checks the anchor values themselves rather than the
     * shape of the maps: a corrupted entry survives every structural assertion above but breaks here.
     */
    @Test
    fun everyAnchorPair_agreesWithTheConverterAcrossTheFullRange() {
        englishDateMap.forEach { (englishYear, reference) ->
            val english = reference.englishDate
            val nepali = reference.nepaliDate
            assertEquals(
                nepali,
                NepaliDateConverter.convertEnglishToNepali(
                    english.year, english.month, english.dayOfMonth
                ),
                "Anchor $englishYear: English to Bikram Sambat disagrees with the converter"
            )
            assertEquals(
                english,
                NepaliDateConverter.convertNepaliToEnglish(
                    nepali.year, nepali.month, nepali.dayOfMonth
                ),
                "Anchor $englishYear: Bikram Sambat to English disagrees with the converter"
            )
        }
    }

    @Test
    fun everyAnchor_isTheFirstDayOfItsNepaliYear() {
        nepaliDateMap.forEach { (year, reference) ->
            assertEquals(1, reference.nepaliDate.month, "Anchor $year is not in Baisakh")
            assertEquals(1, reference.nepaliDate.dayOfMonth, "Anchor $year is not day 1")
            assertEquals(1, reference.nepaliDate.dayOfYear, "Anchor $year is not day 1 of the year")
        }
    }

    private companion object {
        const val FNV_OFFSET_BASIS: Long = -3750763034362895579L
        const val FNV_PRIME: Long = 1099511628211L

        /** Digest of the 130 pairs as `nepaliDateMap` presents them. */
        const val expectedNepaliAnchorDigest: Long = -6431168786330472778L

        /** Digest of the same 130 pairs as `englishDateMap` presents them. */
        const val expectedEnglishAnchorDigest: Long = -258393609491842188L
    }
}
