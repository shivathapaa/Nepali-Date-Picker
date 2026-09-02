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

import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimpleTimeSerializerTests {

    private val json = Json

    @Test
    fun serialize_dropsZeroNanoseconds() {
        val encoded = json.encodeToString(SimpleTimeSerializer, SimpleTime(9, 30, 0, 0))
        assertEquals("\"09:30:00\"", encoded)
    }

    @Test
    fun serialize_includesNonZeroNanoseconds() {
        val encoded = json.encodeToString(SimpleTimeSerializer, SimpleTime(23, 59, 59, 123_456_789))
        assertEquals("\"23:59:59.123456789\"", encoded)
    }

    @Test
    fun serialize_padsSubSecondToNineDigits() {
        val encoded = json.encodeToString(SimpleTimeSerializer, SimpleTime(0, 0, 0, 7))
        assertEquals("\"00:00:00.000000007\"", encoded)
    }

    @Test
    fun roundTrip_zeroNano() {
        val original = SimpleTime(9, 30, 0, 0)
        val encoded = json.encodeToString(SimpleTimeSerializer, original)
        assertEquals(original, json.decodeFromString(SimpleTimeSerializer, encoded))
    }

    @Test
    fun roundTrip_fullPrecision() {
        val original = SimpleTime(15, 22, 7, 999_999_999)
        val encoded = json.encodeToString(SimpleTimeSerializer, original)
        assertEquals(original, json.decodeFromString(SimpleTimeSerializer, encoded))
    }

    @Test
    fun deserialize_rejectsHourOutOfRange() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleTimeSerializer, "\"24:00:00\"")
        }
    }

    @Test
    fun deserialize_rejectsMinuteOutOfRange() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleTimeSerializer, "\"12:60:00\"")
        }
    }

    @Test
    fun deserialize_rejectsMalformed() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(SimpleTimeSerializer, "\"12:30\"")
        }
    }
}
