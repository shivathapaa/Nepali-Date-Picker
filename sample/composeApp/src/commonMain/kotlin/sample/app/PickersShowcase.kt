/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package sample.app

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendarSystemToggle
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

private val NepaliLocale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)

/** The calendar-grid pickers: single, localized, dual-date, and range in both month layouts. */
@Composable
fun PickersShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        DemoSection(
            "Date picker",
            "The default single-date picker. Toggle the pencil for text input."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Date picker in Nepali",
            "Same picker with Nepali language and Devanagari digits."
        ) {
            val state = rememberNepaliDatePickerState(locale = NepaliLocale)
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Date picker with English date",
            "Each cell shows the Bikram Sambat day paired with its Gregorian day."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerWithEnglishDate(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Switchable B.S. / A.D. picker",
            "One grid, either calendar. The switch changes only what is displayed: the same day " +
                    "stays selected, and the selection is always reported in Bikram Sambat."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale,
                showCalendarSystemToggle = true
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Neighbouring months in the empty cells",
            "The blank cells around the month are filled with the days either side of it, drawn " +
                    "faded. Tapping one picks that day and moves the grid to its month."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                showAdjacentMonthDays = true,
                showCalendarSystemToggle = true
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Switchable picker in Nepali",
            "The same switch with Nepali labels (बि.सं. / ई.सं.) and Devanagari digits."
        ) {
            val state = rememberNepaliDatePickerState(locale = NepaliLocale)
            NepaliDatePicker(
                state = state,
                secondaryDateLocale = NepaliLocale,
                showCalendarSystemToggle = true
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Dual-date picker, switchable and filled",
            "Every option at once: both calendars in each cell, a switch for which one leads, and " +
                    "the neighbouring months filling the edges."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerWithEnglishDate(
                state = state,
                showCalendarSystemToggle = true,
                showAdjacentMonthDays = true
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Switch outside the picker",
            "NepaliCalendarSystemToggle on its own, driving the grid from the app's own chrome " +
                    "instead of from inside the picker."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliCalendarSystemToggle(
                calendarSystem = state.displayedCalendarSystem,
                onCalendarSystemChange = { state.displayedCalendarSystem = it }
            )
            VerticalGap()
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Gregorian-first picker",
            "Opens on the Gregorian grid with no switch, for a screen that is English-first."
        ) {
            val state = rememberNepaliDatePickerState(
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(
                state = state,
                secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Date range picker (horizontal months)",
            "Start and end selection with months paged horizontally."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(state = state, showMonthsVertically = false)
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }

        DemoSection(
            "Date range picker (vertical months)",
            "Months stacked in a scrolling list. It needs a bounded height inside this scroll."
        ) {
            val state = rememberNepaliDateRangePickerState()
            // The vertical layout is a LazyColumn; an unbounded height inside this outer scroll crashes it.
            NepaliDateRangePicker(
                state = state,
                modifier = Modifier.height(480.dp),
                showMonthsVertically = true
            )
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }

        DemoSection(
            "Date range picker (vertical months, filled)",
            "With the edges filled, consecutive months share the days on the boundary, so a range " +
                    "crossing it is shaded in both, the way a wall calendar reads."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(
                state = state,
                modifier = Modifier.height(480.dp),
                showMonthsVertically = true,
                showAdjacentMonthDays = true
            )
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }

        DemoSection(
            "Date range picker with English date",
            "Dual Bikram Sambat and Gregorian range selection."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePickerWithEnglishDate(state = state, showMonthsVertically = false)
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }

        DemoSection(
            "Dual-date range picker, switchable and filled",
            "The range variant with every option on: both calendars per cell, the switch, and the " +
                    "filled edges."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePickerWithEnglishDate(
                state = state,
                showMonthsVertically = false,
                showCalendarSystemToggle = true,
                showAdjacentMonthDays = true
            )
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }

        DemoSection(
            "Switchable range picker",
            "The range highlight follows whichever calendar is on screen; the range itself stays " +
                    "the same two Bikram Sambat dates."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale,
                showCalendarSystemToggle = true,
                showAdjacentMonthDays = true
            )
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }
    }
}

private fun rangeReadout(start: String?, end: String?): String? =
    if (start == null && end == null) null else "Range: ${start ?: "..."} to ${end ?: "..."}"
