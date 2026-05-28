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

import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * [KSerializer] for [CustomCalendar] — full struct form. All eleven fields are
 * serialized; defaults from the data class are honored on the way back in so older
 * payloads missing `dayOfWeekInMonth` / `dayOfWeek` / `dayOfYear` / `weekOfMonth` /
 * `weekOfYear` still decode (those fields default to -1).
 *
 * No string form is provided — [CustomCalendar] is a fully-detailed calendar record,
 * not just a date. Use [SimpleDateSerializer] if you only need year/month/day.
 */
object CustomCalendarSerializer : KSerializer<CustomCalendar> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.CustomCalendar") {
            element<Int>("year")
            element<Int>("month")
            element<Int>("dayOfMonth")
            element<Int>("era")
            element<Int>("firstDayOfMonth")
            element<Int>("lastDayOfMonth")
            element<Int>("totalDaysInMonth")
            element<Int>("dayOfWeekInMonth", isOptional = true)
            element<Int>("dayOfWeek", isOptional = true)
            element<Int>("dayOfYear", isOptional = true)
            element<Int>("weekOfMonth", isOptional = true)
            element<Int>("weekOfYear", isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: CustomCalendar) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.year)
            encodeIntElement(descriptor, 1, value.month)
            encodeIntElement(descriptor, 2, value.dayOfMonth)
            encodeIntElement(descriptor, 3, value.era)
            encodeIntElement(descriptor, 4, value.firstDayOfMonth)
            encodeIntElement(descriptor, 5, value.lastDayOfMonth)
            encodeIntElement(descriptor, 6, value.totalDaysInMonth)
            encodeIntElement(descriptor, 7, value.dayOfWeekInMonth)
            encodeIntElement(descriptor, 8, value.dayOfWeek)
            encodeIntElement(descriptor, 9, value.dayOfYear)
            encodeIntElement(descriptor, 10, value.weekOfMonth)
            encodeIntElement(descriptor, 11, value.weekOfYear)
        }
    }

    override fun deserialize(decoder: Decoder): CustomCalendar {
        var year = 0; var month = 0; var dayOfMonth = 0; var era = 0
        var firstDayOfMonth = 0; var lastDayOfMonth = 0; var totalDaysInMonth = 0
        var dayOfWeekInMonth = -1; var dayOfWeek = -1; var dayOfYear = -1
        var weekOfMonth = -1; var weekOfYear = -1
        var bits = 0 // bits 0..6 must all be set

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val idx = decodeElementIndex(descriptor)) {
                    0 -> { year = decodeIntElement(descriptor, 0); bits = bits or (1 shl 0) }
                    1 -> { month = decodeIntElement(descriptor, 1); bits = bits or (1 shl 1) }
                    2 -> { dayOfMonth = decodeIntElement(descriptor, 2); bits = bits or (1 shl 2) }
                    3 -> { era = decodeIntElement(descriptor, 3); bits = bits or (1 shl 3) }
                    4 -> { firstDayOfMonth = decodeIntElement(descriptor, 4); bits = bits or (1 shl 4) }
                    5 -> { lastDayOfMonth = decodeIntElement(descriptor, 5); bits = bits or (1 shl 5) }
                    6 -> { totalDaysInMonth = decodeIntElement(descriptor, 6); bits = bits or (1 shl 6) }
                    7 -> dayOfWeekInMonth = decodeIntElement(descriptor, 7)
                    8 -> dayOfWeek = decodeIntElement(descriptor, 8)
                    9 -> dayOfYear = decodeIntElement(descriptor, 9)
                    10 -> weekOfMonth = decodeIntElement(descriptor, 10)
                    11 -> weekOfYear = decodeIntElement(descriptor, 11)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index $idx for CustomCalendar")
                }
            }
        }
        // Seven required fields — bits 0..6.
        if (bits != 0b111_1111) {
            throw SerializationException(
                "CustomCalendar missing required fields (got bitmask 0b${bits.toString(2)}; expected 0b1111111 for year, month, dayOfMonth, era, firstDayOfMonth, lastDayOfMonth, totalDaysInMonth)"
            )
        }
        return CustomCalendar(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            era = era,
            firstDayOfMonth = firstDayOfMonth,
            lastDayOfMonth = lastDayOfMonth,
            totalDaysInMonth = totalDaysInMonth,
            dayOfWeekInMonth = dayOfWeekInMonth,
            dayOfWeek = dayOfWeek,
            dayOfYear = dayOfYear,
            weekOfMonth = weekOfMonth,
            weekOfYear = weekOfYear,
        )
    }
}

/**
 * [KSerializer] for [NepaliMonthCalendar] — struct form with five required fields
 * and one optional (`daysFromStartOfWeekToFirstOfMonth`, which defaults to
 * `firstDayOfMonth - 1`).
 */
object NepaliMonthCalendarSerializer : KSerializer<NepaliMonthCalendar> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.NepaliMonthCalendar") {
            element<Int>("year")
            element<Int>("month")
            element<Int>("totalDaysInMonth")
            element<Int>("firstDayOfMonth")
            element<Int>("lastDayOfMonth")
            element<Int>("daysFromStartOfWeekToFirstOfMonth", isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: NepaliMonthCalendar) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.year)
            encodeIntElement(descriptor, 1, value.month)
            encodeIntElement(descriptor, 2, value.totalDaysInMonth)
            encodeIntElement(descriptor, 3, value.firstDayOfMonth)
            encodeIntElement(descriptor, 4, value.lastDayOfMonth)
            encodeIntElement(descriptor, 5, value.daysFromStartOfWeekToFirstOfMonth)
        }
    }

    override fun deserialize(decoder: Decoder): NepaliMonthCalendar {
        var year = 0; var month = 0; var totalDaysInMonth = 0
        var firstDayOfMonth = 0; var lastDayOfMonth = 0
        var explicitDaysFrom: Int? = null
        var bits = 0

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val idx = decodeElementIndex(descriptor)) {
                    0 -> { year = decodeIntElement(descriptor, 0); bits = bits or 1 }
                    1 -> { month = decodeIntElement(descriptor, 1); bits = bits or 2 }
                    2 -> { totalDaysInMonth = decodeIntElement(descriptor, 2); bits = bits or 4 }
                    3 -> { firstDayOfMonth = decodeIntElement(descriptor, 3); bits = bits or 8 }
                    4 -> { lastDayOfMonth = decodeIntElement(descriptor, 4); bits = bits or 16 }
                    5 -> explicitDaysFrom = decodeIntElement(descriptor, 5)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index $idx for NepaliMonthCalendar")
                }
            }
        }
        if (bits != 0b11111) {
            throw SerializationException(
                "NepaliMonthCalendar missing required fields (got bitmask 0b${bits.toString(2)}; expected year/month/totalDaysInMonth/firstDayOfMonth/lastDayOfMonth)"
            )
        }
        return NepaliMonthCalendar(
            year = year,
            month = month,
            totalDaysInMonth = totalDaysInMonth,
            firstDayOfMonth = firstDayOfMonth,
            lastDayOfMonth = lastDayOfMonth,
            daysFromStartOfWeekToFirstOfMonth = explicitDaysFrom ?: (firstDayOfMonth - 1),
        )
    }
}

private inline fun <reified T> kotlinx.serialization.descriptors.ClassSerialDescriptorBuilder.element(
    name: String,
    isOptional: Boolean = false,
) = element(name, kotlinx.serialization.serializer<T>().descriptor, isOptional = isOptional)
