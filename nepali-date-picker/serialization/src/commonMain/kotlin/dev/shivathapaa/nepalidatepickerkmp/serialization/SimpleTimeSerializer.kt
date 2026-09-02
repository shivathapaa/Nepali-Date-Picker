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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Default [KSerializer] for [SimpleTime] - encodes as the string
 * `"HH:mm:ss.NNNNNNNNN"`, always in Asia/Kathmandu (the timezone all `SimpleTime`s
 * in this library are anchored to). The nanosecond fractional part is omitted when
 * `nanosecond == 0` to keep wire payloads small.
 *
 * Wire format examples:
 *   - `"09:30:00"`
 *   - `"23:59:59.123456789"`
 */
object SimpleTimeSerializer : KSerializer<SimpleTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.SimpleTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SimpleTime) {
        val hh = value.hour.toString().padStart(2, '0')
        val mm = value.minute.toString().padStart(2, '0')
        val ss = value.second.toString().padStart(2, '0')
        val base = "$hh:$mm:$ss"
        val out = if (value.nanosecond == 0) base else "$base.${value.nanosecond.toString().padStart(9, '0')}"
        encoder.encodeString(out)
    }

    override fun deserialize(decoder: Decoder): SimpleTime {
        val raw = decoder.decodeString()
        val (timePart, nanoStr) = when (val dot = raw.indexOf('.')) {
            -1 -> raw to "0"
            else -> raw.substring(0, dot) to raw.substring(dot + 1)
        }
        val parts = timePart.split(':')
        if (parts.size != 3) {
            throw SerializationException("Invalid SimpleTime '$raw' - expected 'HH:mm:ss[.nnnnnnnnn]'")
        }
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        val second = parts[2].toIntOrNull()
        val nanosecond = nanoStr.toIntOrNull()
        if (hour == null || minute == null || second == null || nanosecond == null) {
            throw SerializationException("Invalid SimpleTime '$raw' - non-numeric component")
        }
        if (hour !in 0..23) throw SerializationException("SimpleTime.hour out of 0..23: $hour")
        if (minute !in 0..59) throw SerializationException("SimpleTime.minute out of 0..59: $minute")
        if (second !in 0..59) throw SerializationException("SimpleTime.second out of 0..59: $second")
        if (nanosecond !in 0..999_999_999) {
            throw SerializationException("SimpleTime.nanosecond out of 0..999_999_999: $nanosecond")
        }
        return SimpleTime(hour, minute, second, nanosecond)
    }
}
