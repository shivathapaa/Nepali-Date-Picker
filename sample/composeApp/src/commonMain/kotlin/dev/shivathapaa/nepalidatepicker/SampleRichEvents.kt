/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays

/**
 * A month with a real product's worth of events on it: ten of them, every one carrying the app's
 * own record in its payload, and one running four days so a list has a span to collapse.
 *
 * The days are stated as day numbers of [month] rather than offsets from today, so the month a
 * calendar opens on always has something on it, and clamped to the month's length so Chaitra's
 * thirty days never take a thirty-first.
 */
internal fun richSampleEvents(month: SimpleDate): List<NepaliCalendarEvent> {
    val daysInMonth = NepaliDateConverter.getTotalDaysInNepaliMonth(month.year, month.month)
    fun day(dayOfMonth: Int) =
        SimpleDate(month.year, month.month, dayOfMonth.coerceIn(1, daysInMonth))

    val festival = NepaliCalendarEvent(
        date = day(17),
        name = "Indra Jatra",
        kind = NepaliEventKind.Religious,
        id = "indra-jatra",
        payload = SampleEventPayload(
            description = "Eight days of masked dance around Basantapur, with the chariot " +
                    "procession on the first evening.",
            icon = "festival",
            accentArgb = 0xFFD32F2F,
            bannerUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2b/Indra_Jatra_2014.jpg",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/8/8a/Kumari_Jatra.jpg",
            tags = listOf("festival", "public"),
            venue = "Basantapur Durbar Square",
            organizer = "Kathmandu Metropolitan City",
            link = "https://en.wikipedia.org/wiki/Indra_Jatra"
        ).encode()
    ).spanningDays(4)

    return festival + listOf(
        NepaliCalendarEvent(
            date = day(2),
            name = "Sprint planning",
            kind = NepaliEventKind.Observance,
            id = "sprint-planning",
            payload = SampleEventPayload(
                description = "Scope for the fortnight, then estimates. Bring the support queue.",
                icon = "date",
                accentArgb = 0xFF1E88E5,
                tags = listOf("work", "recurring"),
                venue = "Meeting room 3B",
                organizer = "Platform team"
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(4),
            name = "Aama's birthday",
            kind = NepaliEventKind.Observance,
            id = "birthday-aama",
            payload = SampleEventPayload(
                description = "Seventy this year. Cake at home in the evening.",
                icon = "favorite",
                accentArgb = 0xFFEC407A,
                iconUrl = "https://upload.wikimedia.org/wikipedia/commons/4/45/Birthday_cake.jpg",
                tags = listOf("personal", "family")
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(4),
            name = "Dentist",
            kind = NepaliEventKind.Observance,
            id = "dentist",
            payload = SampleEventPayload(
                description = "Six-month check, the one that keeps being postponed.",
                icon = "reminder",
                accentArgb = 0xFF00897B,
                tags = listOf("health"),
                venue = "Lalitpur Dental",
                link = "https://maps.example.com/lalitpur-dental"
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(9),
            name = "Sahakari meeting",
            kind = NepaliEventKind.Observance,
            id = "sahakari",
            payload = SampleEventPayload(
                description = "Monthly savings collection and the year's audit summary.",
                icon = "person",
                accentArgb = 0xFF6D4C41,
                tags = listOf("community"),
                venue = "Ward office",
                organizer = "Tole sudhar samiti"
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(12),
            name = "Kirtipur trek",
            kind = NepaliEventKind.Observance,
            id = "trek",
            payload = SampleEventPayload(
                description = "Leaves at five in the morning, back by noon.",
                icon = "place",
                accentArgb = 0xFF43A047,
                bannerUrl = "https://upload.wikimedia.org/wikipedia/commons/5/5e/Kirtipur_Nepal.jpg",
                tags = listOf("outdoors", "personal"),
                venue = "Kirtipur"
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(21),
            name = "Quarterly review",
            kind = NepaliEventKind.Observance,
            id = "quarterly-review",
            payload = SampleEventPayload(
                description = "Numbers first, then the roadmap for the next quarter.",
                icon = "star",
                accentArgb = 0xFF5E35B1,
                tags = listOf("work"),
                organizer = "Leadership"
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(21),
            name = "Dashain shopping",
            kind = NepaliEventKind.Observance,
            id = "dashain-shopping",
            payload = SampleEventPayload(
                description = "Clothes for the children, then the Asan run for spices.",
                icon = "shopping",
                accentArgb = 0xFFEF6C00,
                tags = listOf("personal", "errand"),
                venue = "Asan"
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(21),
            name = "Hand over the release",
            kind = NepaliEventKind.Observance,
            id = "release-handover",
            payload = SampleEventPayload(
                description = "Tag, notes, and the store submission before the holiday.",
                icon = "share",
                accentArgb = 0xFF3949AB,
                tags = listOf("work", "deadline")
            ).encode()
        ),
        NepaliCalendarEvent(
            date = day(25),
            name = "Blood donation camp",
            kind = NepaliEventKind.Regional,
            id = "blood-donation",
            payload = SampleEventPayload(
                description = "Ward-level camp, walk-ins welcome until four.",
                icon = "favorite",
                accentArgb = 0xFFC62828,
                tags = listOf("community", "health"),
                venue = "Community hall",
                organizer = "Nepal Red Cross",
                link = "https://nrcs.org"
            ).encode()
        )
    )
}
