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
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlinx.serialization.modules.SerializersModule

/**
 * Ready-to-register [SerializersModule] for all four data types.
 *
 * Wires the default serializers - [SimpleDateSerializer] (string form),
 * [SimpleTimeSerializer], [CustomCalendarSerializer], [NepaliMonthCalendarSerializer].
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
}
