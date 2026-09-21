/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.serialization.NepaliDatePickerSerializersModule
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The one Json instance [SerializationShowcase] encodes through. */
internal val sampleJson = Json {
    serializersModule = NepaliDatePickerSerializersModule
    prettyPrint = true
    encodeDefaults = true
}

@Serializable
internal data class Booking(
    val reference: String,
    @Contextual val date: SimpleDate,
    @Contextual val time: SimpleTime,
    @Contextual val calendar: CustomCalendar,
    @Contextual val system: CalendarSystem
)

@Serializable
internal data class MonthSnapshot(
    @Contextual val nepali: NepaliMonthCalendar,
    @Contextual val english: MonthCalendar,
    @Contextual val system: CalendarSystem
)

@Serializable
internal data class HolidayYear(
    val year: Int,
    val events: List<@Contextual NepaliCalendarEvent>
)

@Serializable
internal data class DaySnapshot(@Contextual val status: NepaliDayStatus)

@Serializable
internal data class Draft(
    @Contextual val date: SimpleDate,
    @Contextual val calendar: CustomCalendar
)

@Serializable
internal data class Bounds(
    @Contextual val first: CustomCalendar,
    @Contextual val last: CustomCalendar,
    @Contextual val minEnglish: SimpleDate,
    @Contextual val maxEnglish: SimpleDate
)

/** Three named days near [from], one of them carrying an app-specific payload. */
internal fun sampleHolidays(from: SimpleDate): List<NepaliCalendarEvent> = listOf(
    NepaliCalendarEvent(
        date = offsetDate(from, 1),
        name = "Constitution Day (demo)",
        kind = NepaliEventKind.GovernmentPublic,
        id = "constitution-day",
        payload = """{"source":"cache","region":"national"}"""
    ),
    NepaliCalendarEvent(
        date = offsetDate(from, 4),
        name = "Local jatra (demo)",
        kind = NepaliEventKind.Regional,
        id = "local-jatra"
    ),
    NepaliCalendarEvent(
        date = offsetDate(from, 6),
        name = "World Health Day (demo)",
        kind = NepaliEventKind.Observance,
        id = "world-health-day"
    )
)
