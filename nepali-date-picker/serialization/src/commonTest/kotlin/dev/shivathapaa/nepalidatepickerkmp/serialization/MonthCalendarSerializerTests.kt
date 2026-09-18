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

import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toMonthCalendar
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MonthCalendarSerializerTests {

    private val json = Json

    // CalendarSystem

    @Test
    fun calendarSystem_roundTripsBothWays() {
        CalendarSystem.entries.forEach { system ->
            val encoded = json.encodeToString(CalendarSystemSerializer, system)
            assertEquals(system, json.decodeFromString(CalendarSystemSerializer, encoded))
        }
    }

    @Test
    fun calendarSystem_isWrittenAsItsEra() {
        assertEquals("1", json.encodeToString(CalendarSystemSerializer, CalendarSystem.GREGORIAN))
        assertEquals("2", json.encodeToString(CalendarSystemSerializer, CalendarSystem.BIKRAM_SAMBAT))
    }

    @Test
    fun calendarSystem_rejectsAnUnknownEra() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(CalendarSystemSerializer, "3")
        }
    }

    // MonthCalendar

    @Test
    fun monthCalendar_roundTripsInEitherCalendar() {
        val bikramSambat = MonthCalendar(
            calendarSystem = CalendarSystem.BIKRAM_SAMBAT,
            year = 2083, month = 4, totalDaysInMonth = 31,
            firstDayOfMonth = 6, lastDayOfMonth = 1
        )
        val gregorian = MonthCalendar(
            calendarSystem = CalendarSystem.GREGORIAN,
            year = 2026, month = 9, totalDaysInMonth = 30,
            firstDayOfMonth = 3, lastDayOfMonth = 4
        )
        listOf(bikramSambat, gregorian).forEach { original ->
            val encoded = json.encodeToString(MonthCalendarSerializer, original)
            assertEquals(original, json.decodeFromString(MonthCalendarSerializer, encoded))
        }
    }

    @Test
    fun monthCalendar_keepsTheDerivedLeadingCellCount() {
        val original = MonthCalendar(
            calendarSystem = CalendarSystem.BIKRAM_SAMBAT,
            year = 2083, month = 4, totalDaysInMonth = 31,
            firstDayOfMonth = 6, lastDayOfMonth = 1
        )
        val decoded = json.decodeFromString(
            MonthCalendarSerializer,
            json.encodeToString(MonthCalendarSerializer, original)
        )
        assertEquals(5, decoded.daysFromStartOfWeekToFirstOfMonth)
    }

    @Test
    fun monthCalendar_writesTheCalendarAsAnEra() {
        val encoded = json.encodeToString(
            MonthCalendarSerializer,
            MonthCalendar(
                calendarSystem = CalendarSystem.GREGORIAN,
                year = 2026, month = 9, totalDaysInMonth = 30,
                firstDayOfMonth = 3, lastDayOfMonth = 4
            )
        )
        assertTrue(encoded.contains("\"era\":1"), "expected an era of 1 in $encoded")
    }

    @Test
    fun monthCalendar_readsAPayloadWithNoEraAsBikramSambat() {
        // The shape NepaliMonthCalendarSerializer writes, which has no calendar of its own.
        val payload = """
            {
              "year": 2082, "month": 2, "totalDaysInMonth": 31,
              "firstDayOfMonth": 4, "lastDayOfMonth": 6
            }
        """.trimIndent()
        val decoded = json.decodeFromString(MonthCalendarSerializer, payload)
        assertEquals(CalendarSystem.BIKRAM_SAMBAT, decoded.calendarSystem)
        assertEquals(2082, decoded.year)
    }

    @Test
    fun monthCalendar_decodesWhatTheNepaliMonthCalendarSerializerWrote() {
        val nepaliMonth = NepaliMonthCalendar(
            year = 2082, month = 2, totalDaysInMonth = 31,
            firstDayOfMonth = 4, lastDayOfMonth = 6
        )
        val encoded = json.encodeToString(NepaliMonthCalendarSerializer, nepaliMonth)
        assertEquals(
            nepaliMonth.toMonthCalendar(),
            json.decodeFromString(MonthCalendarSerializer, encoded)
        )
    }

    @Test
    fun monthCalendar_rejectsMissingRequiredField() {
        val payload = """{"era": 2, "year": 2082, "month": 2, "totalDaysInMonth": 31, "firstDayOfMonth": 4}"""
        assertFailsWith<SerializationException> {
            json.decodeFromString(MonthCalendarSerializer, payload)
        }
    }

    @Test
    fun monthCalendar_rejectsAnUnknownEra() {
        val payload = """
            {
              "era": 7, "year": 2082, "month": 2, "totalDaysInMonth": 31,
              "firstDayOfMonth": 4, "lastDayOfMonth": 6
            }
        """.trimIndent()
        assertFailsWith<SerializationException> {
            json.decodeFromString(MonthCalendarSerializer, payload)
        }
    }
}
