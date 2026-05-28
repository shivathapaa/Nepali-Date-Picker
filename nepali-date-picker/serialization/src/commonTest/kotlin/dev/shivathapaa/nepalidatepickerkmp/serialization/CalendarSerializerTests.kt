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
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalendarSerializerTests {

    private val json = Json

    // ── CustomCalendar ─────────────────────────────────────────────────────

    @Test
    fun customCalendar_roundTrip_fullData() {
        val original = NepaliCalendarDefaults.startingNepaliCalendar
        val encoded = json.encodeToString(CustomCalendarSerializer, original)
        val decoded = json.decodeFromString(CustomCalendarSerializer, encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun customCalendar_roundTrip_minimalPayloadUsesDefaults() {
        // Only the 7 required fields — optional fields should default to -1.
        val payload = """
            {
              "year": 2082, "month": 2, "dayOfMonth": 14, "era": 2,
              "firstDayOfMonth": 1, "lastDayOfMonth": 3, "totalDaysInMonth": 31
            }
        """.trimIndent()
        val decoded = json.decodeFromString(CustomCalendarSerializer, payload)
        val expected = CustomCalendar(
            year = 2082, month = 2, dayOfMonth = 14, era = 2,
            firstDayOfMonth = 1, lastDayOfMonth = 3, totalDaysInMonth = 31,
            // optional → -1
        )
        assertEquals(expected, decoded)
    }

    @Test
    fun customCalendar_deserialize_rejectsMissingRequiredField() {
        // Missing `totalDaysInMonth` — bit 6.
        val payload = """
            {
              "year": 2082, "month": 2, "dayOfMonth": 14, "era": 2,
              "firstDayOfMonth": 1, "lastDayOfMonth": 3
            }
        """.trimIndent()
        assertFailsWith<SerializationException> {
            json.decodeFromString(CustomCalendarSerializer, payload)
        }
    }

    // ── NepaliMonthCalendar ────────────────────────────────────────────────

    @Test
    fun monthCalendar_roundTrip() {
        val original = NepaliMonthCalendar(
            year = 2082, month = 2, totalDaysInMonth = 31,
            firstDayOfMonth = 4, lastDayOfMonth = 6
        )
        val encoded = json.encodeToString(NepaliMonthCalendarSerializer, original)
        val decoded = json.decodeFromString(NepaliMonthCalendarSerializer, encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun monthCalendar_deserialize_defaultDaysFromStartIsFirstDayMinusOne() {
        // `daysFromStartOfWeekToFirstOfMonth` omitted — defaults to firstDayOfMonth - 1.
        val payload = """
            {
              "year": 2082, "month": 2, "totalDaysInMonth": 31,
              "firstDayOfMonth": 4, "lastDayOfMonth": 6
            }
        """.trimIndent()
        val decoded = json.decodeFromString(NepaliMonthCalendarSerializer, payload)
        assertEquals(3, decoded.daysFromStartOfWeekToFirstOfMonth)
    }

    @Test
    fun monthCalendar_deserialize_explicitDaysFromStartUsedWhenPresent() {
        val payload = """
            {
              "year": 2082, "month": 2, "totalDaysInMonth": 31,
              "firstDayOfMonth": 4, "lastDayOfMonth": 6,
              "daysFromStartOfWeekToFirstOfMonth": 99
            }
        """.trimIndent()
        val decoded = json.decodeFromString(NepaliMonthCalendarSerializer, payload)
        assertEquals(99, decoded.daysFromStartOfWeekToFirstOfMonth)
    }

    @Test
    fun monthCalendar_deserialize_rejectsMissingRequiredField() {
        val payload = """{"year": 2082, "month": 2, "totalDaysInMonth": 31, "firstDayOfMonth": 4}"""
        assertFailsWith<SerializationException> {
            json.decodeFromString(NepaliMonthCalendarSerializer, payload)
        }
    }
}
