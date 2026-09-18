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
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
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
 * [KSerializer] for [MonthCalendar] - struct form with the five geometry fields plus the calendar the
 * month is expressed in, written as an `era` number through [CalendarSystemSerializer].
 *
 * `era` is optional and defaults to Bikram Sambat, so a payload written by
 * [NepaliMonthCalendarSerializer] decodes here as a Bikram Sambat month. That payload's
 * `daysFromStartOfWeekToFirstOfMonth` is accepted and discarded: [MonthCalendar] derives it from
 * `firstDayOfMonth`, so it is never written out and never read back.
 */
object MonthCalendarSerializer : KSerializer<MonthCalendar> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("dev.shivathapaa.nepalidatepickerkmp.MonthCalendar") {
            element<Int>("era", isOptional = true)
            element<Int>("year")
            element<Int>("month")
            element<Int>("totalDaysInMonth")
            element<Int>("firstDayOfMonth")
            element<Int>("lastDayOfMonth")
            element<Int>("daysFromStartOfWeekToFirstOfMonth", isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: MonthCalendar) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.calendarSystem.era)
            encodeIntElement(descriptor, 1, value.year)
            encodeIntElement(descriptor, 2, value.month)
            encodeIntElement(descriptor, 3, value.totalDaysInMonth)
            encodeIntElement(descriptor, 4, value.firstDayOfMonth)
            encodeIntElement(descriptor, 5, value.lastDayOfMonth)
        }
    }

    override fun deserialize(decoder: Decoder): MonthCalendar {
        var era = CalendarSystem.BIKRAM_SAMBAT.era
        var year = 0; var month = 0; var totalDaysInMonth = 0
        var firstDayOfMonth = 0; var lastDayOfMonth = 0
        var bits = 0

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val idx = decodeElementIndex(descriptor)) {
                    0 -> era = decodeIntElement(descriptor, 0)
                    1 -> { year = decodeIntElement(descriptor, 1); bits = bits or 1 }
                    2 -> { month = decodeIntElement(descriptor, 2); bits = bits or 2 }
                    3 -> { totalDaysInMonth = decodeIntElement(descriptor, 3); bits = bits or 4 }
                    4 -> { firstDayOfMonth = decodeIntElement(descriptor, 4); bits = bits or 8 }
                    5 -> { lastDayOfMonth = decodeIntElement(descriptor, 5); bits = bits or 16 }
                    6 -> decodeIntElement(descriptor, 6)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index $idx for MonthCalendar")
                }
            }
        }
        if (bits != 0b11111) {
            throw SerializationException(
                "MonthCalendar missing required fields (got bitmask 0b${bits.toString(2)}; expected year/month/totalDaysInMonth/firstDayOfMonth/lastDayOfMonth)"
            )
        }
        val calendarSystem = CalendarSystem.fromEra(era)
            ?: throw SerializationException("Unknown MonthCalendar era $era; expected 1 (AD) or 2 (BS)")
        return MonthCalendar(
            calendarSystem = calendarSystem,
            year = year,
            month = month,
            totalDaysInMonth = totalDaysInMonth,
            firstDayOfMonth = firstDayOfMonth,
            lastDayOfMonth = lastDayOfMonth
        )
    }
}
