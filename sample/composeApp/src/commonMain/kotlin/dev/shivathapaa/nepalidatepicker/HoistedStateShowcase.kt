/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class, ExperimentalLayoutApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerState
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

private val ButtonGap = 8.dp

/**
 * The state built outside composition. `rememberNepaliDatePickerState` is the right call inside a
 * composable, but the state itself is a plain object: a view model or any other holder that outlives
 * the composition can build one with the `NepaliDatePickerState` function and hand it down. Two
 * pickers given the same instance are two views of one selection, not two selections.
 */
@Composable
fun HoistedStateShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }

        // The kind of object a view model would own. It is built once, survives every recomposition
        // below, and knows nothing about Compose.
        val holder = remember(today) { BookingFormState(today) }

        DemoSection(
            "One state, two surfaces",
            "The grid and the dropdown below share a single NepaliDatePickerState built outside " +
                    "composition. Picking in either moves the other, because there is only one " +
                    "selection between them."
        ) {
            NepaliDatePicker(state = holder.dateState)
            VerticalGap()
            NepaliDatePickerDocked(
                state = holder.dateState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("The same date") }
            )
            SelectedText(holder.dateState.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "The holder decides, not the picker",
            "Because the holder owns the state, the app's own logic can read and write it without " +
                    "a picker on screen. These buttons are the holder's methods, the shape a view " +
                    "model would expose to a screen."
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ButtonGap)) {
                OutlinedButton(onClick = holder::selectToday) { Text("Today") }
                OutlinedButton(onClick = holder::selectNextWeek) { Text("A week out") }
                OutlinedButton(onClick = holder::clear) { Text("Clear") }
                OutlinedButton(onClick = holder::toggleMode) { Text("Flip input mode") }
            }
            LabeledValue("Selected", holder.dateState.selectedDate.readout() ?: "none")
            LabeledValue("Mode", holder.dateState.displayMode.toString())
            LabeledValue("Summary", holder.summary())
        }

        DemoSection(
            "A range state held the same way",
            "NepaliDateRangePickerState has its own non-composable factory, so a leave-request form " +
                    "can build the whole thing in its holder and only then show a picker."
        ) {
            NepaliDateRangePicker(state = holder.rangeState, showMonthsVertically = false)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ButtonGap)) {
                OutlinedButton(onClick = holder::selectWorkWeek) { Text("Next five days") }
                OutlinedButton(onClick = holder::clearRange) { Text("Clear") }
            }
            LabeledValue("Start", holder.rangeState.selectedStartNepaliDate.readout() ?: "none")
            LabeledValue("End", holder.rangeState.selectedEndNepaliDate.readout() ?: "none")
            LabeledValue("Nights", holder.nights()?.toString() ?: "incomplete")
        }

        Text(
            text = "A state built this way is not saved across configuration changes on its own. " +
                    "Hold it somewhere that survives them, the way a view model does, or use " +
                    "rememberNepaliDatePickerState when the composition is the right owner.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * A stand-in for the state holder a real screen would have: it owns both picker states, exposes the
 * operations the screen offers, and derives whatever the screen prints. No Compose types are needed
 * for any of it.
 */
private class BookingFormState(private val today: SimpleDate) {

    val dateState: NepaliDatePickerState = NepaliDatePickerState(
        initialSelectedDate = today,
        locale = NepaliDatePickerDefaults.DefaultLocale
    )

    val rangeState: NepaliDateRangePickerState = NepaliDateRangePickerState(
        initialDisplayedMonth = today,
        locale = NepaliDatePickerDefaults.DefaultRangePickerLocale
    )

    fun selectToday() {
        dateState.selectedDate = calendarOf(today)
    }

    fun selectNextWeek() {
        dateState.selectedDate = calendarOf(offsetDate(today, DaysInWeek))
    }

    fun clear() {
        dateState.selectedDate = null
    }

    fun toggleMode() {
        dateState.displayMode =
            if (dateState.displayMode == DisplayMode.Picker) DisplayMode.Input else DisplayMode.Picker
    }

    fun selectWorkWeek() {
        rangeState.setSelection(
            startNepaliDate = calendarOf(offsetDate(today, 1)),
            endNepaliDate = calendarOf(offsetDate(today, DaysInWeek - 2))
        )
    }

    fun clearRange() {
        rangeState.setSelection(startNepaliDate = null, endNepaliDate = null)
    }

    /** What the screen would submit, formatted through the locale the state already carries. */
    fun summary(): String {
        val selected = dateState.selectedDate ?: return "nothing chosen"
        return NepaliDateConverter.formatNepaliDate(selected, dateState.locale)
    }

    /** Nights between the two ends of the range, or null while only one end is chosen. */
    fun nights(): Int? {
        val start = rangeState.selectedStartNepaliDate ?: return null
        val end = rangeState.selectedEndNepaliDate ?: return null
        return NepaliDateConverter.getNepaliDaysInBetween(
            SimpleDate(start.year, start.month, start.dayOfMonth),
            SimpleDate(end.year, end.month, end.dayOfMonth)
        )
    }

    private fun calendarOf(date: SimpleDate): CustomCalendar =
        NepaliDateConverter.getNepaliCalendar(date.year, date.month, date.dayOfMonth)
}

private const val DaysInWeek = 7
