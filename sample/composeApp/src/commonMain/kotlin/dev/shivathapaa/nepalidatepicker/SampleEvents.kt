/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.ui.graphics.Color
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/** What an event is to this sample. A real app has its own model, and the library never sees it. */
data class SampleEvent(
    val date: SimpleDate,
    val title: String,
    val kind: SampleEventKind
)

/** The categories this sample colors its events by, borrowing slots from the marker palette. */
enum class SampleEventKind { Work, Personal, Festival }

/**
 * The sample's stand-in event store: four days near [from] carrying one, two, three and four
 * events. The last is there to show the cap, since a cell draws three dots at most however many
 * events land on it.
 */
fun sampleEvents(from: SimpleDate): List<SampleEvent> = listOf(
    SampleEvent(offsetDate(from, 1), "Standup", SampleEventKind.Work),
    SampleEvent(offsetDate(from, 4), "Standup", SampleEventKind.Work),
    SampleEvent(offsetDate(from, 4), "Aama's birthday", SampleEventKind.Personal),
    SampleEvent(offsetDate(from, 6), "Sprint review", SampleEventKind.Work),
    SampleEvent(offsetDate(from, 6), "Dentist", SampleEventKind.Personal),
    SampleEvent(offsetDate(from, 6), "Puja at home", SampleEventKind.Festival),
    SampleEvent(offsetDate(from, 8), "Standup", SampleEventKind.Work),
    SampleEvent(offsetDate(from, 8), "Design review", SampleEventKind.Work),
    SampleEvent(offsetDate(from, 8), "Bank errand", SampleEventKind.Personal),
    SampleEvent(offsetDate(from, 8), "Friend's wedding", SampleEventKind.Festival)
)

/**
 * The colors this sample paints [SampleEventKind] in, taken from the library's marker palette so
 * they follow the theme into dark mode along with everything else.
 */
fun SampleEventKind.color(colors: NepaliDayMarkerColors): Color = when (this) {
    SampleEventKind.Work -> colors.eventColor
    SampleEventKind.Personal -> colors.personalColor
    SampleEventKind.Festival -> colors.religiousColor
}

/** One dot per event, keyed by date, which is what `dayDecorator` takes. */
fun List<SampleEvent>.markers(colors: NepaliDayMarkerColors): Map<SimpleDate, List<Color>> =
    groupBy { it.date }.mapValues { (_, events) -> events.map { it.kind.color(colors) } }

/** The titles of a day's events joined, for the screen reader to announce after the date. */
fun List<SampleEvent>.titles(): Map<SimpleDate, String> =
    groupBy { it.date }.mapValues { (_, events) -> events.joinToString { it.title } }

/** The events on [date], in the order the store holds them. */
fun List<SampleEvent>.on(date: SimpleDate): List<SampleEvent> = filter { it.date == date }

