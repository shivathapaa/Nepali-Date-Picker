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

import dev.shivathapaa.nepalidatepickerkmp.data.NepaliTimeFormatter
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
 *
 * The string itself is defined by [NepaliTimeFormatter] in the `-core` artifact, which every
 * published target carries. Swift and JavaScript consumers reach the same wire form through it
 * without depending on `kotlinx-serialization`.
 */
object SimpleTimeSerializer : KSerializer<SimpleTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.SimpleTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SimpleTime) {
        encoder.encodeString(NepaliTimeFormatter.format(value))
    }

    override fun deserialize(decoder: Decoder): SimpleTime {
        val raw = decoder.decodeString()
        return NepaliTimeFormatter.parse(raw)
            ?: throw SerializationException(
                "Invalid SimpleTime '$raw' - expected 'HH:mm:ss[.nnnnnnnnn]' with hour in 0..23, " +
                    "minute and second in 0..59, and a fractional part in 0..999999999"
            )
    }
}
