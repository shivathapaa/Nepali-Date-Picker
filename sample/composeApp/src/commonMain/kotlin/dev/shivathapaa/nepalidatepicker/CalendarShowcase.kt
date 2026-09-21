/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendar
import dev.shivathapaa.nepalidatepickerkmp.NepaliDaySummary
import dev.shivathapaa.nepalidatepickerkmp.NepaliMonthEventList
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventDisplayStyle
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.plus
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliCalendarState

/**
 * The calendar as a screen rather than a dialog: a grid that fills its width, the day it is
 * showing written out, and the month listed underneath. All three read the same policy, so none of
 * them can disagree about what is on a day.
 */
@Composable
fun CalendarShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        // Ten events on this month, every one carrying the app's own record as JSON, plus the
        // national closures the other tabs use.
        val richEvents = remember(today) { richSampleEvents(today) }
        val policy = remember(today, richEvents) {
            NepaliCalendarPolicy(
                provider = SampleEventProvider(today) + richEvents.asEventProvider()
            )
        }

        DemoSection(
            "A calendar, its day and its month",
            "One state drives all three, with both calendars' numbers in every cell. Tapping a day " +
                    "picks it, which the summary follows and which highlights that day's lines in " +
                    "the list."
        ) {
            val state = rememberNepaliCalendarState()
            var tapped by remember { mutableStateOf<NepaliCalendarEvent?>(null) }

            NepaliCalendar(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                policy = policy
            )
            NepaliDaySummary(state = state, policy = policy)
            NepaliMonthEventList(
                state = state,
                policy = policy,
                onEventClick = { event -> tapped = event }
            )
            tapped?.let { SampleEventCard(event = it) }
        }

        DemoSection(
            "What an event can carry",
            "The library hands an event back exactly as it was given, id and payload untouched. " +
                    "This card is the sample reading its own JSON out of that payload: a colour, an " +
                    "icon, a description, tags, a venue and image links it could fetch."
        ) {
            val state = rememberNepaliCalendarState()
            var tapped by remember { mutableStateOf(richEvents.first()) }

            NepaliMonthEventList(
                state = state,
                policy = policy,
                onEventClick = { event -> tapped = event }
            )
            SampleEventCard(event = tapped)
            SelectedText("Payload: ${tapped.payload ?: "none"}")
        }

        DemoSection(
            "One calendar at a time",
            "Without a secondary locale a cell carries one number, and the switch turns the whole " +
                    "grid Gregorian without changing what is picked."
        ) {
            val state = rememberNepaliCalendarState()
            NepaliCalendar(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                policy = policy,
                secondaryDateLocale = null,
                showCalendarSystemToggle = true
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Dots on the grid",
            "Holidays are told apart by colour by default, which leaves the dots free. Ask for a " +
                    "kind to be dotted and the cell draws them the way every picker does."
        ) {
            val state = rememberNepaliCalendarState()
            NepaliCalendar(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                policy = policy,
                eventDisplayStyle = NepaliDatePickerDefaults.eventDisplayStyle(
                    indicateKinds = setOf(
                        NepaliEventKind.Religious,
                        NepaliEventKind.Observance,
                        NepaliEventKind.Regional
                    )
                )
            )
        }

        DemoSection(
            "A school's week",
            "The same calendar under a different institution: Saturday and Sunday closed, and the " +
                    "month's own days borrowed from a second provider."
        ) {
            val schoolPolicy = remember(today) {
                NepaliCalendarPolicy(
                    weeklyOffDays = setOf(Saturday, Sunday),
                    provider = SampleEventProvider(today) + SampleSchoolEvents(today)
                )
            }
            val state = rememberNepaliCalendarState()
            NepaliCalendar(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                policy = schoolPolicy
            )
            NepaliDaySummary(state = state, policy = schoolPolicy)
        }
    }
}

