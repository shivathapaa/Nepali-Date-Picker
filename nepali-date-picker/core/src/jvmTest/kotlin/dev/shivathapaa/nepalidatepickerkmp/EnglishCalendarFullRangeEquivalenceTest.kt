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
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The exhaustive form of [EnglishCalendarEquivalenceTests]: every convertible English date, each
 * read both by converting a Bikram Sambat date back and by reading the Gregorian date directly.
 *
 * JVM only. Two day-walks per date over the whole range overruns the per-test timeout the
 * Kotlin/JS test runner allows, so the shared source set carries the boundaries and a stride and
 * this carries the proof that nothing in between disagrees.
 */
class EnglishCalendarFullRangeEquivalenceTest {

    @Test
    fun getEnglishCalendar_matchesRoundTripThroughBikramSambat_acrossFullRange() {
        var checked = 0
        for (year in NepaliCalendarDefaults.EnglishYearRange) {
            for (month in 1..12) {
                val totalDays = NepaliDateConverter.getTotalDaysInEnglishMonth(year, month)
                for (dayOfMonth in 1..totalDays) {
                    if (!NepaliDateConverter.isEnglishDateConvertible(year, month, dayOfMonth)) {
                        continue
                    }
                    val nepali =
                        NepaliDateConverter.convertEnglishToNepali(year, month, dayOfMonth)
                    val viaRoundTrip = NepaliDateConverter.convertNepaliToEnglish(
                        nepali.year, nepali.month, nepali.dayOfMonth
                    )
                    val direct = NepaliDateConverter.getEnglishCalendar(year, month, dayOfMonth)
                    assertEquals(
                        viaRoundTrip, direct,
                        "English $year-$month-$dayOfMonth disagrees between the round trip and " +
                                "the direct read"
                    )
                    checked++
                }
            }
        }
        // Every day of 1913..2043 except the 102 that precede the 1913-04-13 anchor.
        assertEquals(47745, checked, "Coverage of the convertible English range changed")
    }
}
