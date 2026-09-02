/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp.serialization

import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * Default [KSerializer] for [SimpleDate] - encodes as the string `"YYYY-MM-DD"`.
 *
 * Wire format example: `"2082-02-14"`.
 *
 * Year always pads to 4 digits, month and day to 2. Parses any year width >= 1 on
 * the way back in (so historical Bikram Sambat years that take 4 digits, plus any
 * one-off `"99-01-01"` test fixture, all round-trip).
 *
 * The date is always interpreted as Bikram Sambat - no era tag in the wire form
 * because the library doesn't model AD dates as `SimpleDate`.
 *
 * @see SimpleDateStructSerializer for a `{year, month, dayOfMonth}` JSON-object form.
 */
object SimpleDateSerializer : KSerializer<SimpleDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.SimpleDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SimpleDate) {
        encoder.encodeString(formatIso(value))
    }

    override fun deserialize(decoder: Decoder): SimpleDate {
        val raw = decoder.decodeString()
        return parseIso(raw)
            ?: throw SerializationException(
                "Invalid SimpleDate '$raw' - expected 'YYYY-MM-DD' with month in 1..12 and day in 1..32"
            )
    }

    internal fun formatIso(value: SimpleDate): String {
        val year = value.year.toString().padStart(4, '0')
        val month = value.month.toString().padStart(2, '0')
        val day = value.dayOfMonth.toString().padStart(2, '0')
        return "$year-$month-$day"
    }

    internal fun parseIso(raw: String): SimpleDate? {
        val parts = raw.split('-')
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..32) return null
        return SimpleDate(year, month, day)
    }
}

/**
 * Alternative [KSerializer] for [SimpleDate] - encodes as a JSON object
 * `{"year": …, "month": …, "dayOfMonth": …}`.
 *
 * Use this when downstream tooling (JSON Schema, BigQuery, GraphQL codegen, …) prefers
 * field access over string parsing. Register with `@Serializable(SimpleDateStructSerializer::class)`
 * or via a `SerializersModule`.
 */
object SimpleDateStructSerializer : KSerializer<SimpleDate> {
    override val descriptor: SerialDescriptor =
        kotlinx.serialization.descriptors.buildClassSerialDescriptor(
            "dev.shivathapaa.nepalidatepickerkmp.SimpleDate"
        ) {
            element<Int>("year")
            element<Int>("month")
            element<Int>("dayOfMonth")
        }

    override fun serialize(encoder: Encoder, value: SimpleDate) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.year)
            encodeIntElement(descriptor, 1, value.month)
            encodeIntElement(descriptor, 2, value.dayOfMonth)
        }
    }

    override fun deserialize(decoder: Decoder): SimpleDate {
        var year = 0
        var month = 0
        var day = 0
        var seenYear = false
        var seenMonth = false
        var seenDay = false
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val idx = decodeElementIndex(descriptor)) {
                    0 -> { year = decodeIntElement(descriptor, 0); seenYear = true }
                    1 -> { month = decodeIntElement(descriptor, 1); seenMonth = true }
                    2 -> { day = decodeIntElement(descriptor, 2); seenDay = true }
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index $idx for SimpleDate")
                }
            }
        }
        if (!seenYear || !seenMonth || !seenDay) {
            throw SerializationException("SimpleDate requires year, month, dayOfMonth")
        }
        if (month !in 1..12) throw SerializationException("SimpleDate.month must be in 1..12, got $month")
        if (day !in 1..32) throw SerializationException("SimpleDate.dayOfMonth must be in 1..32, got $day")
        return SimpleDate(year, month, day)
    }
}

// element<T> helper for buildClassSerialDescriptor - pulled out for readability.
private inline fun <reified T> kotlinx.serialization.descriptors.ClassSerialDescriptorBuilder.element(
    name: String,
) = element(name, kotlinx.serialization.serializer<T>().descriptor)
