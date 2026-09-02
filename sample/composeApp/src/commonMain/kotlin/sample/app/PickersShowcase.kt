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
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
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
            "Date range picker with English date",
            "Dual Bikram Sambat and Gregorian range selection."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePickerWithEnglishDate(state = state, showMonthsVertically = false)
            SelectedText(rangeReadout(state.selectedStartNepaliDate.readout(), state.selectedEndNepaliDate.readout()))
        }
    }
}

private fun rangeReadout(start: String?, end: String?): String? =
    if (start == null && end == null) null else "Range: ${start ?: "..."} to ${end ?: "..."}"
