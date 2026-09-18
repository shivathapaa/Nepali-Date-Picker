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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The published module is what consumers register, so every type it claims to cover has to resolve
 * through it without naming a serializer by hand.
 */
class SerializersModuleTests {

    @Serializable
    private data class PickerSnapshot(
        @Contextual val date: SimpleDate,
        @Contextual val time: SimpleTime,
        @Contextual val calendar: CustomCalendar,
        @Contextual val nepaliMonth: NepaliMonthCalendar,
        @Contextual val month: MonthCalendar,
        @Contextual val calendarSystem: CalendarSystem
    )

    private val json = Json { serializersModule = NepaliDatePickerSerializersModule }

    private val snapshot = PickerSnapshot(
        date = SimpleDate(2083, 6, 1),
        time = SimpleTime(9, 30, 0, 0),
        calendar = NepaliCalendarDefaults.startingNepaliCalendar,
        nepaliMonth = NepaliMonthCalendar(
            year = 2083, month = 4, totalDaysInMonth = 31,
            firstDayOfMonth = 6, lastDayOfMonth = 1
        ),
        month = MonthCalendar(
            calendarSystem = CalendarSystem.GREGORIAN,
            year = 2026, month = 9, totalDaysInMonth = 30,
            firstDayOfMonth = 3, lastDayOfMonth = 4
        ),
        calendarSystem = CalendarSystem.GREGORIAN
    )

    @Test
    fun everyPublishedTypeHasAContextualBinding() {
        listOf(
            SimpleDate::class,
            SimpleTime::class,
            CustomCalendar::class,
            NepaliMonthCalendar::class,
            MonthCalendar::class,
            CalendarSystem::class
        ).forEach { type ->
            assertNotNull(
                NepaliDatePickerSerializersModule.getContextual(type),
                "no contextual serializer registered for $type"
            )
        }
    }

    @Test
    fun aSnapshotOfEveryTypeRoundTripsThroughTheModule() {
        val encoded = json.encodeToString(snapshot)
        assertEquals(snapshot, json.decodeFromString<PickerSnapshot>(encoded))
    }
}
