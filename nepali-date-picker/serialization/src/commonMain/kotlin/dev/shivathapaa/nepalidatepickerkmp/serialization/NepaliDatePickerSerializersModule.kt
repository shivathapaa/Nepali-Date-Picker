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
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import kotlinx.serialization.modules.SerializersModule

/**
 * Ready-to-register [SerializersModule] for every data type the library publishes.
 *
 * Wires the default serializers - [SimpleDateSerializer] (string form),
 * [SimpleTimeSerializer], [CustomCalendarSerializer], [NepaliMonthCalendarSerializer],
 * [MonthCalendarSerializer], [CalendarSystemSerializer], and the holiday trio
 * [NepaliCalendarEventSerializer], [NepaliEventKindSerializer] and [NepaliDayStatusSerializer], so a fetched
 * holiday list caches the same way a date does.
 *
 * Usage with kotlinx-serialization-json:
 * ```
 * val json = Json {
 *     serializersModule = NepaliDatePickerSerializersModule
 * }
 * ```
 *
 * If you prefer the JSON-object form of [SimpleDate], replace this module's binding
 * with [SimpleDateStructSerializer] in your own [SerializersModule] copy.
 */
val NepaliDatePickerSerializersModule: SerializersModule = SerializersModule {
    contextual(SimpleDate::class, SimpleDateSerializer)
    contextual(SimpleTime::class, SimpleTimeSerializer)
    contextual(CustomCalendar::class, CustomCalendarSerializer)
    contextual(NepaliMonthCalendar::class, NepaliMonthCalendarSerializer)
    contextual(MonthCalendar::class, MonthCalendarSerializer)
    contextual(CalendarSystem::class, CalendarSystemSerializer)
    contextual(NepaliCalendarEvent::class, NepaliCalendarEventSerializer)
    contextual(NepaliEventKind::class, NepaliEventKindSerializer)
    contextual(NepaliDayStatus::class, NepaliDayStatusSerializer)
}
