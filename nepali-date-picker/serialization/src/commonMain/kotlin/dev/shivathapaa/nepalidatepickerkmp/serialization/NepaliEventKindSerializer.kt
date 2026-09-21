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

import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [KSerializer] for [NepaliEventKind], written as its name, for example `"GovernmentPublic"`.
 *
 * The name rather than the ordinal, so adding a kind later cannot silently rewrite what an already
 * stored file means. A name the library does not know fails with a [SerializationException] rather
 * than falling back to a kind, since guessing would decide whether a day closes an office.
 */
object NepaliEventKindSerializer : KSerializer<NepaliEventKind> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "dev.shivathapaa.nepalidatepickerkmp.NepaliEventKind",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: NepaliEventKind) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): NepaliEventKind {
        val raw = decoder.decodeString()
        return NepaliEventKind.entries.firstOrNull { it.name == raw }
            ?: throw SerializationException(
                "Unknown NepaliEventKind '$raw'; expected one of ${NepaliEventKind.entries.joinToString { it.name }}"
            )
    }
}
