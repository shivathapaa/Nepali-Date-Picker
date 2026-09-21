/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp.serialization

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliTimeFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the string wire forms to the `-core` formatters that Swift and JavaScript consumers use.
 *
 * These serializers and formatters are separate implementations on purpose: the serializers accept
 * a wider range of input than the fixed-width text-field formatters do. What must never drift is
 * the canonical form itself, which is what these tests hold.
 */
class WireFormatParityTests {

    private val json = Json

    @Test
    fun simpleDate_encodesWhatTheCoreFormatterProduces() {
        for (year in NepaliCalendarDefaults.NepaliYearRange) {
            val date = SimpleDate(year, 2, 14)
            val fromFormatter = NepaliDateFormatter.format(date, Pattern.YYYY_DASH_MM_DASH_DD, DigitScript.LATIN)
            assertEquals("\"$fromFormatter\"", json.encodeToString(SimpleDateSerializer, date))
        }
    }

    @Test
    fun simpleDate_decodesWhatTheCoreFormatterAccepts() {
        for (year in NepaliCalendarDefaults.NepaliYearRange) {
            val date = SimpleDate(year, 12, 30)
            val wire = NepaliDateFormatter.format(date, Pattern.YYYY_DASH_MM_DASH_DD, DigitScript.LATIN)
            assertEquals(date, NepaliDateFormatter.parse(wire, Pattern.YYYY_DASH_MM_DASH_DD))
            assertEquals(date, json.decodeFromString(SimpleDateSerializer, "\"$wire\""))
        }
    }

    @Test
    fun simpleDate_agreesOnEveryMonthAndDayBoundary() {
        val boundaries = listOf(1 to 1, 1 to 32, 12 to 1, 12 to 32, 6 to 15)
        for ((month, dayOfMonth) in boundaries) {
            val date = SimpleDate(2082, month, dayOfMonth)
            val fromFormatter = NepaliDateFormatter.format(date, Pattern.YYYY_DASH_MM_DASH_DD, DigitScript.LATIN)
            assertEquals("\"$fromFormatter\"", json.encodeToString(SimpleDateSerializer, date))
            assertEquals(date, NepaliDateFormatter.parse(fromFormatter, Pattern.YYYY_DASH_MM_DASH_DD))
        }
    }

    @Test
    fun simpleTime_encodesWhatTheCoreFormatterProduces() {
        val times = listOf(
            SimpleTime(0, 0, 0, 0),
            SimpleTime(9, 30, 0, 0),
            SimpleTime(23, 59, 59, 999_999_999),
            SimpleTime(0, 0, 0, 7)
        )
        for (time in times) {
            assertEquals(
                "\"${NepaliTimeFormatter.format(time)}\"",
                json.encodeToString(SimpleTimeSerializer, time)
            )
        }
    }

    @Test
    fun simpleTime_decodesWhatTheCoreFormatterAccepts() {
        val wires = listOf("00:00:00", "09:30:00", "23:59:59.999999999", "00:00:00.000000007")
        for (wire in wires) {
            assertEquals(
                NepaliTimeFormatter.parse(wire),
                json.decodeFromString(SimpleTimeSerializer, "\"$wire\"")
            )
        }
    }
}
