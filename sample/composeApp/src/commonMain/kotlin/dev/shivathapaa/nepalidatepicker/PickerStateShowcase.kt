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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

private val ButtonGap = 8.dp

/** Years either side of today the narrowed-range demo offers. */
private const val YearWindow = 2

/** How far the "move the grid" buttons step, in Bikram Sambat months. */
private const val MonthStep = 1

/**
 * The state object as an API rather than a parameter. Every picker in this sample is driven by a
 * state, and that state is readable and writable from outside the picker: the app can move the grid,
 * set or clear the selection, switch between the calendar and the typed input, and read back both
 * calendars' view of the same day.
 */
@Composable
fun PickerStateShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }

        DemoSection(
            "Opening in typed input",
            "initialDisplayMode decides which half of the picker the user lands on. The pencil and " +
                    "calendar icons flip it afterwards, and displayMode reports and accepts the same " +
                    "value, so a screen can restore whichever mode the user left on."
        ) {
            val state = rememberNepaliDatePickerState(initialDisplayMode = DisplayMode.Input)
            NepaliDatePicker(state = state)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ButtonGap)) {
                OutlinedButton(onClick = { state.displayMode = DisplayMode.Picker }) { Text("Calendar") }
                OutlinedButton(onClick = { state.displayMode = DisplayMode.Input }) { Text("Typed input") }
            }
            LabeledValue("displayMode", state.displayMode.toString())
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "A narrowed year range",
            "yearRange bounds the year list and clamps the grid. A five-year window is the shape a " +
                    "booking screen wants; a birth-date screen would open the other end of the range."
        ) {
            val window = remember(today) { (today.year - YearWindow)..(today.year + YearWindow) }
            val state = rememberNepaliDatePickerState(yearRange = window)
            NepaliDatePicker(state = state)
            LabeledValue("yearRange", "${state.yearRange.first} to ${state.yearRange.last}")
            LabeledValue(
                "englishYearRange",
                "${state.englishYearRange.first} to ${state.englishYearRange.last}"
            )
        }

        DemoSection(
            "A selection in one month, the grid in another",
            "initialDisplayedMonth is separate from initialSelectedDate, so a picker can hold last " +
                    "year's date while opening on this month. Left alone it follows the selection."
        ) {
            val state = rememberNepaliDatePickerState(
                initialSelectedDate = remember(today) { SimpleDate(today.year - 1, 1, 15) },
                initialDisplayedMonth = today
            )
            NepaliDatePicker(state = state)
            LabeledValue("Selected", state.selectedDate.readout() ?: "none")
            LabeledValue(
                "Displayed month",
                "${state.displayedMonth.year}/${state.displayedMonth.month}"
            )
        }

        DemoSection(
            "Driving the picker from outside",
            "selectedDate and displayedMonth are both writable. The buttons below are the app's own " +
                    "chrome doing what a tap on the grid would do, which is how a \"today\" shortcut " +
                    "or a restored draft reaches the picker."
        ) {
            val state = rememberNepaliDatePickerState()
            val calendarModel = remember { NepaliCalendarModel() }
            NepaliDatePicker(state = state)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ButtonGap)) {
                OutlinedButton(
                    onClick = {
                        state.selectedDate = NepaliDateConverter.getNepaliCalendar(
                            today.year, today.month, today.dayOfMonth
                        )
                        state.displayedMonth =
                            NepaliDateConverter.getNepaliMonthCalendar(today.year, today.month)
                    }
                ) { Text("Today") }
                OutlinedButton(onClick = { state.selectedDate = null }) { Text("Clear") }
                OutlinedButton(
                    onClick = {
                        state.displayedMonth =
                            steppedMonth(calendarModel, state.displayedMonth, MonthStep)
                    }
                ) { Text("Next month") }
                OutlinedButton(
                    onClick = {
                        state.displayedMonth =
                            steppedMonth(calendarModel, state.displayedMonth, -MonthStep)
                    }
                ) { Text("Previous month") }
            }
            LabeledValue("Selected", state.selectedDate.readout() ?: "none")
            LabeledValue(
                "Displayed month",
                "${state.displayedMonth.year}/${state.displayedMonth.month}"
            )
        }

        DemoSection(
            "The same day in both calendars",
            "selectedDate is always Bikram Sambat; selectedEnglishDate is the Gregorian day it " +
                    "falls on, kept in step by the state itself. displayedMonthCalendar reports the " +
                    "month exactly as drawn, so it follows the switch while displayedMonth does not."
        ) {
            val state = rememberNepaliDatePickerState(
                initialSelectedDate = today,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(
                state = state,
                secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale,
                showCalendarSystemToggle = true
            )
            LabeledValue("selectedDate (BS)", state.selectedDate.readout() ?: "none")
            LabeledValue("selectedEnglishDate (AD)", state.selectedEnglishDate.readout() ?: "none")
            LabeledValue("displayedCalendarSystem", state.displayedCalendarSystem.name)
            LabeledValue(
                "displayedMonth (BS)",
                "${state.displayedMonth.year}/${state.displayedMonth.month}"
            )
            LabeledValue(
                "displayedMonthCalendar",
                "${state.displayedMonthCalendar.year}/${state.displayedMonthCalendar.month}" +
                        " (${state.displayedMonthCalendar.calendarSystem.name})"
            )
        }

        DemoSection(
            "Setting a range in one call",
            "A range picker takes its two ends together through setSelection, because a half-written " +
                    "range is a state the picker itself never produces. Passing nulls clears it."
        ) {
            val state = rememberNepaliDateRangePickerState(
                locale = NepaliDatePickerDefaults.DefaultRangePickerLocale
            )
            NepaliDateRangePicker(state = state, showMonthsVertically = false)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ButtonGap)) {
                OutlinedButton(
                    onClick = {
                        val start = offsetDate(today, 1)
                        val end = offsetDate(today, 7)
                        state.setSelection(
                            startNepaliDate = NepaliDateConverter.getNepaliCalendar(
                                start.year, start.month, start.dayOfMonth
                            ),
                            endNepaliDate = NepaliDateConverter.getNepaliCalendar(
                                end.year, end.month, end.dayOfMonth
                            )
                        )
                    }
                ) { Text("Next week") }
                OutlinedButton(
                    onClick = { state.setSelection(startNepaliDate = null, endNepaliDate = null) }
                ) { Text("Clear") }
            }
            LabeledValue("Start (BS)", state.selectedStartNepaliDate.readout() ?: "none")
            LabeledValue("End (BS)", state.selectedEndNepaliDate.readout() ?: "none")
            LabeledValue("Start (AD)", state.selectedStartEnglishDate.readout() ?: "none")
            LabeledValue("End (AD)", state.selectedEndEnglishDate.readout() ?: "none")
        }

        DemoSection(
            "The rule and the locale travel with the state",
            "nepaliSelectableDates and locale are read back off the state, so a screen that received " +
                    "a state from elsewhere can describe the rules it is working under without being " +
                    "told them a second time."
        ) {
            val selectable = remember(today) {
                NepaliDateConverter.AfterDateSelectable(today, includeDate = true)
            }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            val yesterday = remember(today) { offsetDate(today, -1) }
            NepaliDatePicker(state = state)
            LabeledValue("Language", state.locale.language.name)
            LabeledValue("Date format", state.locale.dateFormat.name)
            LabeledValue(
                "Yesterday selectable",
                state.nepaliSelectableDates.isSelectableDate(
                    NepaliDateConverter.getNepaliCalendar(
                        yesterday.year, yesterday.month, yesterday.dayOfMonth
                    )
                ).toString()
            )
        }
    }
}
