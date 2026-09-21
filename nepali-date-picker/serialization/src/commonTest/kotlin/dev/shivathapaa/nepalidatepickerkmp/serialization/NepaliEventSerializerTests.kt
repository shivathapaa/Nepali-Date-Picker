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

import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A holiday list is fetched once and cached, so it has to survive the round trip exactly, including
 * the Devanagari names and the punctuation a festival name carries.
 */
class HolidaySerializerTests {

    private val json = Json { serializersModule = NepaliDatePickerSerializersModule }

    @Serializable
    private data class CachedYear(
        val year: Int,
        val events: List<@Contextual NepaliCalendarEvent>
    )

    @Serializable
    private data class CachedStatus(@Contextual val status: NepaliDayStatus)

    private fun entry(name: String, kind: NepaliEventKind = NepaliEventKind.Religious) =
        NepaliCalendarEvent(SimpleDate(2082, 6, 3), name, kind)

    // NepaliEventKind

    @Test
    fun everyKindRoundTripsUnderItsOwnName() {
        for (kind in NepaliEventKind.entries) {
            val encoded = json.encodeToString(NepaliEventKindSerializer, kind)
            assertEquals("\"${kind.name}\"", encoded)
            assertEquals(kind, json.decodeFromString(NepaliEventKindSerializer, encoded))
        }
    }

    @Test
    fun anUnknownKindIsRejectedRatherThanGuessed() {
        val failure = assertFailsWith<SerializationException> {
            json.decodeFromString(NepaliEventKindSerializer, "\"BankHoliday\"")
        }
        assertContains(failure.message.orEmpty(), "BankHoliday")
    }

    @Test
    fun theOrdinalIsNotTheWireForm() {
        // Writing the ordinal would make adding a kind rewrite what stored files mean.
        assertFailsWith<SerializationException> {
            json.decodeFromString(NepaliEventKindSerializer, "\"0\"")
        }
    }

    // NepaliCalendarEvent

    @Test
    fun anEntryRoundTripsThroughItsJsonForm() {
        val original = entry("Dashain", NepaliEventKind.Religious)
        val encoded = json.encodeToString(NepaliCalendarEventSerializer, original)

        assertEquals("""{"date":"2082-06-03","name":"Dashain","kind":"Religious"}""", encoded)
        assertEquals(original, json.decodeFromString(NepaliCalendarEventSerializer, encoded))
    }

    @Test
    fun namesKeepTheirScriptAndPunctuation() {
        val awkward = listOf(
            entry("विजया दशमी"),
            entry("Dashain, day two"),
            entry("Id-ul-Fitr \"Ramadan\""),
            entry("")
        )

        val encoded = json.encodeToString(ListSerializer(NepaliCalendarEventSerializer), awkward)

        assertEquals(awkward, json.decodeFromString(ListSerializer(NepaliCalendarEventSerializer), encoded))
    }

    @Test
    fun aMissingFieldIsAnError() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(NepaliCalendarEventSerializer, """{"date":"2082-06-03","name":"x"}""")
        }
        assertFailsWith<SerializationException> {
            json.decodeFromString(NepaliCalendarEventSerializer, """{"name":"x","kind":"Religious"}""")
        }
    }

    @Test
    fun aMalformedDateIsAnError() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(
                NepaliCalendarEventSerializer,
                """{"date":"2082-13-40","name":"x","kind":"Religious"}"""
            )
        }
    }

    @Test
    fun theClosureFlagAndTheAppsOwnFieldsRoundTrip() {
        val detailed = NepaliCalendarEvent(
            date = SimpleDate(2082, 6, 3),
            name = "Annual programme",
            kind = NepaliEventKind.Religious,
            closesOffices = false,
            id = "evt-42",
            payload = """{"images":["a.png"],"note":"तिहार"}"""
        )

        val decoded = json.decodeFromString(
            NepaliCalendarEventSerializer,
            json.encodeToString(NepaliCalendarEventSerializer, detailed)
        )

        assertEquals(detailed, decoded)
        assertFalse(decoded.closesOffices, "the flag is stored, not re-derived from the kind")
        assertEquals("evt-42", decoded.id)
        assertEquals(detailed.payload, decoded.payload)
    }

    @Test
    fun anEntryWithoutTheOptionalFieldsReadsBackWithItsKindDefaults() {
        val encoded = """{"date":"2082-06-03","name":"Dashain","kind":"Religious"}"""

        val decoded = json.decodeFromString(NepaliCalendarEventSerializer, encoded)

        assertTrue(decoded.closesOffices, "a religious holiday closes the day unless told otherwise")
        assertNull(decoded.id)
        assertNull(decoded.payload)
    }

    @Test
    fun anObservanceWithoutTheFlagStaysOpen() {
        val decoded = json.decodeFromString(
            NepaliCalendarEventSerializer,
            """{"date":"2082-06-03","name":"World Health Day","kind":"Observance"}"""
        )

        assertFalse(decoded.closesOffices)
    }

    @Test
    fun aPayloadKeepsItsOwnPunctuationAndScript() {
        val awkward = listOf(
            entry("A").copy(payload = """{"a":"b, c","d":"\"quoted\""}"""),
            entry("B").copy(payload = "विजया दशमी"),
            entry("C").copy(payload = ""),
            entry("D").copy(id = "")
        )

        val encoded = json.encodeToString(ListSerializer(NepaliCalendarEventSerializer), awkward)

        assertEquals(
            awkward,
            json.decodeFromString(ListSerializer(NepaliCalendarEventSerializer), encoded)
        )
    }

    // NepaliDayStatus

    @Test
    fun aStatusOfNonClosingEventsStaysAWorkingDay() {
        val status = NepaliDayStatus(
            isWeeklyOff = false,
            events = listOf(entry("Programme", NepaliEventKind.Observance))
        )

        val decoded = json.decodeFromString(
            NepaliDayStatusSerializer,
            json.encodeToString(NepaliDayStatusSerializer, status)
        )

        assertFalse(decoded.isNonWorking, "derived from the flag, not from the list being non-empty")
        assertTrue(decoded.closures.isEmpty())
        assertEquals(1, decoded.events.size)
    }

    @Test
    fun aStatusRoundTripsAndRebuildsItsDerivedAnswers() {
        val status = NepaliDayStatus(
            isWeeklyOff = true,
            events = listOf(entry("Constitution Day", NepaliEventKind.GovernmentPublic))
        )

        val decoded = json.decodeFromString(
            NepaliDayStatusSerializer,
            json.encodeToString(NepaliDayStatusSerializer, status)
        )

        assertEquals(status, decoded)
        assertTrue(decoded.isNonWorking)
        assertEquals(NepaliEventKind.GovernmentPublic, decoded.primaryKind)
        assertEquals(listOf("Constitution Day"), decoded.names)
    }

    @Test
    fun aWorkingDayIsTheEmptyStatus() {
        val encoded = json.encodeToString(NepaliDayStatusSerializer, NepaliDayStatus.Working)

        assertEquals("""{"isWeeklyOff":false,"events":[]}""", encoded)
        assertEquals(
            NepaliDayStatus.Working,
            json.decodeFromString(NepaliDayStatusSerializer, encoded)
        )
    }

    @Test
    fun anOmittedHolidayListReadsAsEmpty() {
        val decoded = json.decodeFromString(NepaliDayStatusSerializer, """{"isWeeklyOff":true}""")

        assertTrue(decoded.isWeeklyOff)
        assertTrue(decoded.events.isEmpty())
        assertTrue(decoded.isNonWorking)
    }

    // Through the published module

    @Test
    fun theModuleResolvesTheHolidayTypesContextually() {
        val cached = CachedYear(
            year = 2082,
            events = listOf(
                entry("New Year", NepaliEventKind.GovernmentPublic),
                entry("Local jatra", NepaliEventKind.Regional)
            )
        )

        val decoded = json.decodeFromString<CachedYear>(json.encodeToString(cached))

        assertEquals(cached, decoded)
    }

    @Test
    fun theModuleResolvesADayStatusContextually() {
        val cached = CachedStatus(NepaliDayStatus(isWeeklyOff = true, events = emptyList()))

        assertEquals(cached, json.decodeFromString<CachedStatus>(json.encodeToString(cached)))
    }

    // A span, which is a list of ordinary events

    @Test
    fun aSpanRoundTripsAsTheDaysItExpandedTo() {
        val span = NepaliCalendarEvent(
            date = SimpleDate(2082, 6, 17),
            name = "Dashain",
            kind = NepaliEventKind.Religious,
            id = "dashain-2082",
            payload = """{"district":"all"}"""
        ).spanningDays(10)

        val decoded = json.decodeFromString(
            ListSerializer(NepaliCalendarEventSerializer),
            json.encodeToString(ListSerializer(NepaliCalendarEventSerializer), span)
        )

        assertEquals(span, decoded)
        assertTrue(decoded.all { it.id == "dashain-2082" }, "the id that folds the days back survives")
        assertEquals(10, decoded.map { it.date }.toSet().size)
    }

    @Test
    fun aSpanKeepsAClosureFlagThatContradictsItsKind() {
        val span = NepaliCalendarEvent(
            date = SimpleDate(2082, 6, 17),
            name = "Sports week",
            kind = NepaliEventKind.Religious,
            closesOffices = false
        ).spanningDays(4)

        val encoded = json.encodeToString(ListSerializer(NepaliCalendarEventSerializer), span)
        val decoded = json.decodeFromString(ListSerializer(NepaliCalendarEventSerializer), encoded)

        assertContains(
            encoded,
            "closesOffices",
            message = "a flag that differs from its kind has to be written"
        )
        assertTrue(decoded.none { it.closesOffices })
    }

    @Test
    fun aSpanAcrossTheYearEndCachesUnderBothYears() {
        val span = NepaliCalendarEvent(
            date = SimpleDate(2082, 12, 29),
            name = "Year end break",
            kind = NepaliEventKind.GovernmentPublic,
            id = "break-2082"
        ).spanningThrough(SimpleDate(2083, 1, 2))

        val cached = span.groupBy { it.date.year }
            .map { (year, entries) -> CachedYear(year, entries) }
            .map { json.decodeFromString<CachedYear>(json.encodeToString(it)) }

        assertEquals(listOf(2082, 2083), cached.map { it.year })
        assertEquals(span.size, cached.sumOf { it.events.size })
    }
}
