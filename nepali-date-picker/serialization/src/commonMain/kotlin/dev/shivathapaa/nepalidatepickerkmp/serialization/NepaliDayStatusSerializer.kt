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

import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * [KSerializer] for [NepaliDayStatus], written as `{"isWeeklyOff": false, "events": [...]}`.
 *
 * Only the two facts a policy resolved are stored. `isNonWorking`, `primaryKind` and `names` are
 * derived on the way back out, so a cached status can never disagree with itself. `events` may be
 * omitted and reads as an empty list, which is what an ordinary working day serializes to.
 */
object NepaliDayStatusSerializer : KSerializer<NepaliDayStatus> {
    private val eventListSerializer = ListSerializer(NepaliCalendarEventSerializer)

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.NepaliDayStatus") {
            element<Boolean>("isWeeklyOff")
            // Named with the list serializer's own descriptor: NepaliCalendarEvent has no compiled
            // serializer to resolve reflectively, since its serializer lives in this module.
            element("events", eventListSerializer.descriptor, isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: NepaliDayStatus) {
        encoder.encodeStructure(descriptor) {
            encodeBooleanElement(descriptor, 0, value.isWeeklyOff)
            encodeSerializableElement(descriptor, 1, eventListSerializer, value.events)
        }
    }

    override fun deserialize(decoder: Decoder): NepaliDayStatus = decoder.decodeStructure(descriptor) {
        var isWeeklyOff: Boolean? = null
        var events: List<NepaliCalendarEvent> = emptyList()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> isWeeklyOff = decodeBooleanElement(descriptor, 0)
                1 -> events = decodeSerializableElement(descriptor, 1, eventListSerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw SerializationException("Unexpected index $index for NepaliDayStatus")
            }
        }

        NepaliDayStatus(
            isWeeklyOff = isWeeklyOff
                ?: throw SerializationException("NepaliDayStatus is missing isWeeklyOff"),
            events = events
        )
    }
}
