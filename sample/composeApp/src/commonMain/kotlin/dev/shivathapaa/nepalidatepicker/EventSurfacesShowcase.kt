/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerFullScreenDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.then
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

/**
 * The same marking on every surface that takes it. A decorator is one parameter, so a grid, a
 * dropdown, a text field's dialog and a full-screen dialog all draw the days the same way, and a
 * product never has to keep two ideas of what a holiday looks like.
 */
@Composable
fun EventSurfacesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val provider = remember(today) { SampleEventProvider(today) }
        val policy = remember(provider) { NepaliCalendarPolicy(provider = provider) }
        val events = remember(today) { sampleEvents(today) }
        val markerColors = NepaliDatePickerDefaults.markerColors()
        val marks = NepaliDatePickerDefaults.eventDecorator(policy = policy)
            .then(
                NepaliDatePickerDefaults.dayDecorator(
                    markers = remember(events, markerColors) { events.markers(markerColors) },
                    descriptions = remember(events) { events.titles() }
                )
            )

        DemoSection(
            "The calendar grid",
            "NepaliDatePicker, the surface every other one is built from."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = marks)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "The dual-date grid",
            "NepaliDatePickerWithEnglishDate pairs each cell with its Gregorian day, so the dots " +
                    "move to the opposite corner and a cell draws two rather than three."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerWithEnglishDate(state = state, dayDecorator = marks)
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "The range picker, horizontally",
            "Inside a chosen range the shading leads and the marking follows it, the same way " +
                    "selection does on a single grid."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                dayDecorator = marks
            )
            RangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout())
        }

        DemoSection(
            "The range picker, vertically",
            "The scrolling layout carries the decorator through every month it renders, not only " +
                    "the one in view when it opened. It needs a bounded height inside this scroll."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(
                state = state,
                modifier = Modifier.height(VerticalMonthsHeight),
                showMonthsVertically = true,
                dayDecorator = marks
            )
            RangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout())
        }

        DemoSection(
            "The dual-date range picker",
            "NepaliDateRangePickerWithEnglishDate, the pairing and the range together. Months are " +
                    "stacked by default, so this one takes a bounded height as well."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePickerWithEnglishDate(
                state = state,
                modifier = Modifier.height(VerticalMonthsHeight),
                dayDecorator = marks
            )
            RangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout())
        }

        DemoSection(
            "The docked field",
            "NepaliDatePickerDocked drops its calendar under the field, marked the same way."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerDocked(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pick a date") },
                dayDecorator = marks
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "The text field with a calendar button",
            "NepaliDateField types or picks. The dialog behind the trailing icon is a full picker, " +
                    "so it takes the decorator too."
        ) {
            var value by rememberSimpleDateState()
            NepaliDateField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                dayDecorator = marks
            )
            SelectedText(value.readout()?.let { "Typed or picked: $it" })
        }

        DemoSection(
            "The range text field",
            "NepaliDateRangeField is the same pair of inputs over a range picker, marked alike."
        ) {
            var start by rememberSimpleDateState()
            var end by rememberSimpleDateState()
            NepaliDateRangeField(
                startValue = start,
                endValue = end,
                onRangeChange = { newStart, newEnd ->
                    start = newStart
                    end = newEnd
                },
                modifier = Modifier.fillMaxWidth(),
                dayDecorator = marks
            )
            RangeReadout(start.readout(), end.readout())
        }

        DemoSection(
            "In a dialog",
            "A dialog hosts whatever picker it is given, so the marking is the picker's own " +
                    "parameter and the dialog needs to know nothing about it."
        ) {
            var open by rememberSaveable { mutableStateOf(false) }
            val state = rememberNepaliDatePickerState()
            Button(onClick = { open = true }) { Text("Open dialog") }
            if (open) {
                NepaliDatePickerDialog(
                    confirmButton = {
                        TextButton(onClick = { open = false }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { open = false }) { Text("Cancel") }
                    },
                    onDismissRequest = { open = false }
                ) {
                    NepaliDatePicker(state = state, dayDecorator = marks)
                }
            }
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "In a full-screen dialog",
            "The full-screen host is the same arrangement on a small screen, and the dual-date " +
                    "grid inside it keeps its two dots."
        ) {
            var open by rememberSaveable { mutableStateOf(false) }
            val state = rememberNepaliDatePickerState()
            Button(onClick = { open = true }) { Text("Open full screen") }
            if (open) {
                NepaliDatePickerFullScreenDialog(
                    confirmButton = {
                        TextButton(onClick = { open = false }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { open = false }) { Text("Cancel") }
                    },
                    onDismissRequest = { open = false }
                ) {
                    NepaliDatePickerWithEnglishDate(state = state, dayDecorator = marks)
                }
            }
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }
    }
}

/** The "start to end" line every range surface prints under itself. */
@Composable
private fun RangeReadout(start: String?, end: String?) {
    SelectedText(start?.let { "Range: $it to ${end ?: "..."}" })
}
