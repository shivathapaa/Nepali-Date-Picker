/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendar
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDaySummary
import dev.shivathapaa.nepalidatepickerkmp.NepaliMonthEventList
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.then
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliCalendarState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

/**
 * Drawing a day: the colour that says it is closed, and the dots that say something is scheduled on
 * it. The two are separate channels, which is what lets a Saturday carrying a wedding read as both.
 * What the picker never draws is text: names come back through the policy for the app to render.
 */
@Composable
fun EventsShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val provider = remember(today) { SampleEventProvider(today) }
        val officePolicy = remember(provider) { NepaliCalendarPolicy(provider = provider) }
        val schoolPolicy = remember(provider) {
            NepaliCalendarPolicy(weeklyOffDays = setOf(Saturday, Sunday), provider = provider)
        }
        val markerColors = NepaliDatePickerDefaults.markerColors()
        val officeMarks = NepaliDatePickerDefaults.eventDecorator(policy = officePolicy)
        val events = remember(today) { sampleEvents(today) }
        val eventMarks = NepaliDatePickerDefaults.dayDecorator(
            markers = remember(events, markerColors) { events.markers(markerColors) },
            descriptions = remember(events) { events.titles() }
        )

        DemoSection(
            "The week is a holiday too",
            "A policy carries the days an institution never opens along with the holidays it keeps. " +
                    "Nepal's office week is Saturday off, so every Saturday is coloured, and no dot " +
                    "is spent on something that repeats fifty-two times a year."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = officeMarks)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "A school closes twice a week",
            "The same picker with weeklyOffDays = Saturday and Sunday. Nothing else changes, and " +
                    "the same policy gives the school's working-day arithmetic in the Holidays tab."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy = schoolPolicy)
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "A named closure outranks the week",
            "A day carries one colour. A Saturday that is also Dashain is coloured as Dashain, the " +
                    "more specific fact about it, and several holidays on one day give the colour " +
                    "of the strongest. An observance is the exception: it keeps the office open, so " +
                    "it names the day without making a closed one look open. The weekly colour is " +
                    "the outline here to tell the two apart."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = officePolicy,
                    colors = NepaliDatePickerDefaults.markerColors(
                        weeklyOffColor = MaterialTheme.colorScheme.outline
                    )
                )
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Opt a kind into a dot",
            "Dots are off for holidays by default so they can mean events alone. A product that " +
                    "wants the days offices close to stand out passes indicateKinds."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = officePolicy,
                    style = NepaliDatePickerDefaults.eventDisplayStyle(
                        indicateKinds = setOf(NepaliEventKind.GovernmentPublic)
                    )
                )
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Tint the day instead",
            "tintContainer gives a closed day a disc as well as a coloured number, for a calendar " +
                    "where holidays are the loudest thing on screen."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = officePolicy,
                    style = NepaliDatePickerDefaults.eventDisplayStyle(tintContainer = true)
                )
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Events are the dots",
            "dayDecorator takes plain maps, so anything the app holds by date marks the grid. The " +
                    "last marked day carries four events and still draws three dots, the cell's cap."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = eventMarks)
            val selected = state.selectedDate?.toSimpleDate()
            SelectedText(
                selected?.let { "${it.formatted()}: ${events.on(it).size} event(s)" }
            )
        }

        DemoSection(
            "A holiday and an event on the same day",
            "then lays one decorator over another: the holiday rule leads and keeps the colour, the " +
                    "events keep their dots. A Saturday with a wedding on it is a coloured number " +
                    "with one dot, and neither fact hides the other."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = officeMarks.then(eventMarks))
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "What is on the selected day",
            "The picker prints no text: forty dp holds a number and nothing else. NepaliDaySummary " +
                    "is the day written out, over the same policy the grid is marked from."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = officeMarks.then(eventMarks))
            val selected = state.selectedDate?.toSimpleDate()
            if (selected == null) {
                SelectedText("Pick a day to see what is on it")
            } else {
                NepaliDaySummary(date = selected, policy = officePolicy)
            }
        }

        DemoSection(
            "The whole month written out",
            "NepaliMonthEventList answers a month at a time, so a wall-patro list beside a grid " +
                    "costs one call. See the Calendar tab for the list following a calendar it shares " +
                    "its state with."
        ) {
            val calendarState = rememberNepaliCalendarState()
            NepaliCalendar(
                state = calendarState,
                modifier = Modifier.fillMaxWidth(),
                policy = officePolicy
            )
            NepaliMonthEventList(state = calendarState, policy = officePolicy)
        }

        DemoSection(
            "Dual-date grid",
            "The Gregorian number already sits in the corner of each cell, so dots move to the " +
                    "opposite one and two are drawn rather than three."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerWithEnglishDate(
                state = state,
                dayDecorator = officeMarks.then(eventMarks)
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Range picker",
            "Marks survive in the range picker. Inside a chosen range the shading leads and the " +
                    "dots follow it, the same way selection does."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                dayDecorator = officeMarks
            )
            val start = state.selectedStartNepaliDate.readout()
            val end = state.selectedEndNepaliDate.readout()
            SelectedText(start?.let { "Range: $it to ${end ?: "..."}" })
        }

        DemoSection(
            "Docked field",
            "Any surface built on a picker carries the decorator with it, dropdown included."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerDocked(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pick a date") },
                dayDecorator = officeMarks
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }
    }
}
