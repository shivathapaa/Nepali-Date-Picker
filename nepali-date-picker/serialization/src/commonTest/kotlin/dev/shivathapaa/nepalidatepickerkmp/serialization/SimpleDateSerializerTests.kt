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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimpleDateSerializerTests {

    private val json = Json

    // String form (default)

    @Test
    fun string_serialize() {
        val encoded = json.encodeToString(SimpleDateSerializer, SimpleDate(2082, 2, 14))
        assertEquals("\"2082-02-14\"", encoded)
    }

    @Test
    fun string_serialize_padsSingleDigitMonthAndDay() {
        val encoded = json.encodeToString(SimpleDateSerializer, SimpleDate(2082, 1, 5))
        assertEquals("\"2082-01-05\"", encoded)
    }

    @Test
    fun string_roundTrip() {
        val original = SimpleDate(2082, 2, 14)
        val encoded = json.encodeToString(SimpleDateSerializer, original)
        val decoded = json.decodeFromString(SimpleDateSerializer, encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun string_deserialize_rejectsWrongShape() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleDateSerializer, "\"2082/02/14\"") // slash instead of dash
        }
    }

    @Test
    fun string_deserialize_rejectsMonthOutOfRange() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleDateSerializer, "\"2082-13-01\"")
        }
    }

    @Test
    fun string_deserialize_rejectsDayOutOfRange() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleDateSerializer, "\"2082-02-33\"")
        }
    }

    @Test
    fun string_deserialize_rejectsNonNumeric() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleDateSerializer, "\"2082-02-xx\"")
        }
    }

    @Test
    fun string_deserialize_acceptsDay32_byDesign() {
        // Some BS months have 32 days. Same envelope-only validation as NepaliDateFormatter.parse.
        val parsed = json.decodeFromString(SimpleDateSerializer, "\"2082-02-32\"")
        assertEquals(SimpleDate(2082, 2, 32), parsed)
    }

    // Struct form

    @Test
    fun struct_serialize() {
        val encoded = json.encodeToString(SimpleDateStructSerializer, SimpleDate(2082, 2, 14))
        assertEquals("""{"year":2082,"month":2,"dayOfMonth":14}""", encoded)
    }

    @Test
    fun struct_roundTrip() {
        val original = SimpleDate(2082, 2, 14)
        val encoded = json.encodeToString(SimpleDateStructSerializer, original)
        val decoded = json.decodeFromString(SimpleDateStructSerializer, encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun struct_deserialize_rejectsMissingField() {
        assertFailsWith<SerializationException> {
            // Lenient enough that this throws - required fields must all be present.
            Json { isLenient = true }.decodeFromString(
                SimpleDateStructSerializer,
                """{"year":2082,"month":2}"""
            )
        }
    }

    @Test
    fun struct_deserialize_rejectsMonthOutOfRange() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(
                SimpleDateStructSerializer,
                """{"year":2082,"month":13,"dayOfMonth":1}"""
            )
        }
    }
}
