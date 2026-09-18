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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [KSerializer] for [CalendarSystem], written as its `era` number (1 = Gregorian, 2 = Bikram Sambat).
 *
 * The era is the form [CustomCalendarSerializer] already writes, so a persisted calendar system and a
 * persisted calendar agree on what "1" means. It is also stable against reordering the enum, which the
 * name and the ordinal are not.
 */
object CalendarSystemSerializer : KSerializer<CalendarSystem> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "dev.shivathapaa.nepalidatepickerkmp.CalendarSystem",
        PrimitiveKind.INT
    )

    override fun serialize(encoder: Encoder, value: CalendarSystem) {
        encoder.encodeInt(value.era)
    }

    override fun deserialize(decoder: Decoder): CalendarSystem {
        val era = decoder.decodeInt()
        return CalendarSystem.fromEra(era)
            ?: throw SerializationException("Unknown CalendarSystem era $era; expected 1 (AD) or 2 (BS)")
    }
}
