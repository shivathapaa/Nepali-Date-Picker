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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * [KSerializer] for [NepaliCalendarEvent], written as
 * `{"date": "2082-01-01", "name": "...", "kind": "..."}`.
 *
 * The date reuses [SimpleDateSerializer]'s string form and the kind reuses
 * [NepaliEventKindSerializer]'s name, so a cached event list reads the same way as every other date
 * the library persists. Those three are required: an entry without a date has nowhere to be drawn,
 * and one without a kind has no colour.
 *
 * `closesOffices` is written only when it disagrees with what the kind usually means, and `id` and
 * `payload` only when they are set, so the common entry stays three fields wide. Reading back fills
 * each of them the way the constructor would.
 */
object NepaliCalendarEventSerializer : KSerializer<NepaliCalendarEvent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.NepaliCalendarEvent") {
            element<String>("date")
            element<String>("name")
            element<String>("kind")
            element<Boolean>("closesOffices", isOptional = true)
            element<String?>("id", isOptional = true)
            element<String?>("payload", isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: NepaliCalendarEvent) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, SimpleDateSerializer, value.date)
            encodeStringElement(descriptor, 1, value.name)
            encodeSerializableElement(descriptor, 2, NepaliEventKindSerializer, value.kind)
            if (value.closesOffices != value.kind.closesOfficesByDefault) {
                encodeBooleanElement(descriptor, 3, value.closesOffices)
            }
            value.id?.let { encodeStringElement(descriptor, 4, it) }
            value.payload?.let { encodeStringElement(descriptor, 5, it) }
        }
    }

    override fun deserialize(decoder: Decoder): NepaliCalendarEvent = decoder.decodeStructure(descriptor) {
        var date: SimpleDate? = null
        var name: String? = null
        var kind: NepaliEventKind? = null
        var closesOffices: Boolean? = null
        var id: String? = null
        var payload: String? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> date = decodeSerializableElement(descriptor, 0, SimpleDateSerializer)
                1 -> name = decodeStringElement(descriptor, 1)
                2 -> kind = decodeSerializableElement(descriptor, 2, NepaliEventKindSerializer)
                3 -> closesOffices = decodeBooleanElement(descriptor, 3)
                4 -> id = decodeStringElement(descriptor, 4)
                5 -> payload = decodeStringElement(descriptor, 5)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw SerializationException("Unexpected index $index for NepaliCalendarEvent")
            }
        }

        val resolvedKind = kind
            ?: throw SerializationException("NepaliCalendarEvent is missing its kind")
        NepaliCalendarEvent(
            date = date ?: throw SerializationException("NepaliCalendarEvent is missing its date"),
            name = name ?: throw SerializationException("NepaliCalendarEvent is missing its name"),
            kind = resolvedKind,
            closesOffices = closesOffices ?: resolvedKind.closesOfficesByDefault,
            id = id,
            payload = payload
        )
    }
}
