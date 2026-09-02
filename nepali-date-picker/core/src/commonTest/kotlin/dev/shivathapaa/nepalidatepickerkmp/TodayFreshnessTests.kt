/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTime::class)

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Guards against the "frozen today" regression: `NepaliCalendarModel` used to capture the
 * clock in a construction-time `val`, and because `NepaliDateConverter` is an `object`
 * holding one model, `today*` stayed pinned to the process-start date. These read the wall
 * clock live, so a long-lived Gradle daemon (or app process) would fail the first assertion
 * if the freeze ever came back.
 */
class TodayFreshnessTests {

    // Nepal Standard Time — fixed +05:45, matching NepaliCalendarModel.
    private val nepalOffset = FixedOffsetTimeZone(UtcOffset(hours = 5, minutes = 45))

    @Test
    fun todayEnglishSimpleDate_reflectsCurrentClock() {
        val now = Clock.System.now().toLocalDateTime(nepalOffset)
        val expected = SimpleDate(now.year, now.month.number, now.day)
        assertEquals(expected, NepaliDateConverter.todayEnglishSimpleDate)
    }

    @Test
    fun todayNepaliSimpleDate_matchesConversionOfTodayEnglish() {
        val eng = NepaliDateConverter.todayEnglishSimpleDate
        val nepaliFromEnglish = NepaliDateConverter
            .convertEnglishToNepali(eng.year, eng.month, eng.dayOfMonth)
            .toSimpleDate()
        assertEquals(nepaliFromEnglish, NepaliDateConverter.todayNepaliSimpleDate)
    }
}
