/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.serialization.NepaliDatePickerSerializersModule
import dev.shivathapaa.nepalidatepickerkmp.serialization.SimpleDateSerializer
import dev.shivathapaa.nepalidatepickerkmp.serialization.SimpleDateStructSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** How many days the demo festival covers, so the cached list has more than one entry. */
private const val FestivalDays = 3

/**
 * Every published type as JSON. `:core` carries no serialization dependency, so the serializers live
 * in their own artifact and a consumer opts in by adding it. Registering the module is the whole
 * setup: after that a date, a time, a month, a holiday or a whole day's status round-trips like any
 * other field of a request body or a cache entry.
 */
@Composable
fun SerializationShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliCalendar }
        val time = remember { NepaliDateConverter.currentTime }

        DemoSection(
            "Registering the module",
            "One Json instance, configured once. NepaliDatePickerSerializersModule binds a " +
                    "serializer for every type the library publishes, so a field marked @Contextual " +
                    "resolves without the class naming a serializer itself."
        ) {
            CodeBlock(
                """
                val json = Json {
                    serializersModule = NepaliDatePickerSerializersModule
                }

                @Serializable
                data class Booking(
                    @Contextual val date: SimpleDate,
                    @Contextual val time: SimpleTime
                )
                """.trimIndent()
            )
        }

        DemoSection(
            "A booking, out and back",
            "A record holding a date and a time, encoded and decoded through the module. A date is " +
                    "a compact string by default rather than an object, which keeps a stored payload " +
                    "small and a query parameter readable."
        ) {
            val booking = remember(today, time) {
                Booking(
                    reference = "NDP-2081-0042",
                    date = today.toSimpleDate(),
                    time = time,
                    calendar = today,
                    system = CalendarSystem.BIKRAM_SAMBAT
                )
            }
            val encoded = remember(booking) { sampleJson.encodeToString(booking) }
            val decoded = remember(encoded) { sampleJson.decodeFromString<Booking>(encoded) }
            CodeBlock(encoded)
            LabeledValue("Round-trips intact", (decoded == booking).toString())
            LabeledValue("Decoded date", decoded.date.formatted())
            LabeledValue("Decoded weekday", decoded.calendar.dayOfWeek.toString())
        }

        DemoSection(
            "A date in either shape",
            "SimpleDateSerializer writes the compact string the module registers by default. " +
                    "SimpleDateStructSerializer writes the object form instead, for an API whose " +
                    "schema already has three fields. Both read either form's own output back."
        ) {
            val date = remember(today) { today.toSimpleDate() }
            val asString = remember(date) { sampleJson.encodeToString(SimpleDateSerializer, date) }
            val asObject = remember(date) { sampleJson.encodeToString(SimpleDateStructSerializer, date) }
            LabeledValue("String form", asString)
            CodeBlock(asObject)
            LabeledValue(
                "Both decode to",
                sampleJson.decodeFromString(SimpleDateSerializer, asString).formatted()
            )
        }

        DemoSection(
            "Months and calendar systems",
            "A month calendar is what a grid is built from, so caching one saves recomputing it. " +
                    "MonthCalendar carries its calendar system, and that enum has a serializer of " +
                    "its own so the stored form does not depend on the ordinal."
        ) {
            val months = remember(today) {
                MonthSnapshot(
                    nepali = NepaliDateConverter.getNepaliMonthCalendar(today.year, today.month),
                    english = NepaliDateConverter.getEnglishMonthCalendar(
                        NepaliDateConverter.todayEnglishSimpleDate.year,
                        NepaliDateConverter.todayEnglishSimpleDate.month
                    ),
                    system = CalendarSystem.GREGORIAN
                )
            }
            val encoded = remember(months) { sampleJson.encodeToString(months) }
            CodeBlock(encoded)
            LabeledValue(
                "Round-trips intact",
                (sampleJson.decodeFromString<MonthSnapshot>(encoded) == months).toString()
            )
        }

        DemoSection(
            "A fetched holiday list",
            "This is the case the artifact exists for: a provider that fetches its year from a " +
                    "server has to cache it, and a holiday carries a kind, a closes-offices flag, an " +
                    "id and an opaque payload the library never reads. All of it survives the trip."
        ) {
            val events = remember(today) { sampleHolidays(today.toSimpleDate()) }
            val encoded = remember(events) { sampleJson.encodeToString(HolidayYear(today.year, events)) }
            val decoded = remember(encoded) { sampleJson.decodeFromString<HolidayYear>(encoded) }
            CodeBlock(encoded)
            LabeledValue("Entries", decoded.events.size.toString())
            LabeledValue("Round-trips intact", (decoded.events == events).toString())
            LabeledValue("Payload kept", decoded.events.first().payload ?: "none")
        }

        DemoSection(
            "A span cached as its days",
            "An event that runs longer than a day expands into one entry per day, each sharing the " +
                    "id it came from. Serializing the expansion means a cache and a calendar agree " +
                    "on exactly which days are covered, including any that fall in the next year."
        ) {
            val span = remember(today) {
                NepaliCalendarEvent(
                    date = offsetDate(today.toSimpleDate(), 2),
                    name = "Chhath (demo)",
                    kind = NepaliEventKind.Religious,
                    id = "chhath-demo"
                ).spanningDays(FestivalDays)
            }
            val encoded = remember(span) { sampleJson.encodeToString(HolidayYear(today.year, span)) }
            CodeBlock(encoded)
            LabeledValue(
                "Shared id",
                sampleJson.decodeFromString<HolidayYear>(encoded).events.first().id ?: "none"
            )
        }

        DemoSection(
            "A whole day's status",
            "NepaliDayStatus is what a policy answers a day with, and it serializes too, so a " +
                    "screen can be handed a precomputed day rather than a policy and a provider. " +
                    "isNonWorking and primaryKind are derived on read, not stored."
        ) {
            val events = remember(today) { sampleHolidays(today.toSimpleDate()) }
            val policy = remember(events) {
                NepaliCalendarPolicy(provider = events.asEventProvider())
            }
            val status = remember(policy, events) { policy.statusOf(events.first().date) }
            val encoded = remember(status) { sampleJson.encodeToString(DaySnapshot(status)) }
            val decoded = remember(encoded) { sampleJson.decodeFromString<DaySnapshot>(encoded) }
            CodeBlock(encoded)
            LabeledValue("Weekly off", decoded.status.isWeeklyOff.toString())
            LabeledValue("Non-working", decoded.status.isNonWorking.toString())
            LabeledValue("Strongest kind", decoded.status.primaryKind?.name ?: "none")
        }

        DemoSection(
            "Saving what the picker produced",
            "The loop a form actually runs: pick a day, write it down, read it back. Nothing here " +
                    "knows about Compose, so the same two calls work in a repository or a worker."
        ) {
            val state = rememberNepaliDatePickerState(initialSelectedDate = today.toSimpleDate())
            NepaliDatePicker(state = state)
            val selected = state.selectedDate
            if (selected == null) {
                Text("Pick a day to serialize it", style = MaterialTheme.typography.bodySmall)
            } else {
                val encoded = sampleJson.encodeToString(Draft(selected.toSimpleDate(), selected))
                val restored = sampleJson.decodeFromString<Draft>(encoded)
                CodeBlock(encoded)
                LabeledValue("Restored date", restored.date.formatted())
                LabeledValue("Restored weekday", restored.calendar.dayOfWeek.toString())
                LabeledValue(
                    "Matches the picker",
                    (restored.calendar == selected).toString()
                )
            }
        }

        DemoSection(
            "The boundary calendars, as JSON",
            "Constants serialize like anything else, which makes them easy to pin in a contract " +
                    "test on the other side of a network call."
        ) {
            CodeBlock(
                sampleJson.encodeToString(
                    Bounds(
                        first = NepaliCalendarDefaults.startingNepaliCalendar,
                        last = NepaliCalendarDefaults.endNepaliCalendar,
                        minEnglish = NepaliCalendarDefaults.minConvertibleEnglishDate,
                        maxEnglish = NepaliCalendarDefaults.maxConvertibleEnglishDate
                    )
                )
            )
        }
    }
}

/** JSON as it is meant to be read: monospaced, and scrollable rather than wrapped. */
@Composable
private fun CodeBlock(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
    )
}
