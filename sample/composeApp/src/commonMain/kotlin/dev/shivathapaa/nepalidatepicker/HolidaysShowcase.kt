/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateTextField
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.excludingClosures
import dev.shivathapaa.nepalidatepickerkmp.event.excludingWeekends
import dev.shivathapaa.nepalidatepickerkmp.event.filtered
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.plus
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

private const val UpcomingDays = 30
private const val WorkingDaySpan = 30
private const val WorkingDayStep = 5
private val KindGap = 8.dp

/**
 * When an institution is closed, and what that does to selection and to counting. The Events tab
 * covers how a closed day is drawn; this one is about the data and the rules behind it.
 */
@Composable
fun HolidaysShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val provider = remember(today) { SampleEventProvider(today) }
        val officePolicy = remember(provider) { NepaliCalendarPolicy(provider = provider) }
        val schoolPolicy = remember(provider) {
            NepaliCalendarPolicy(weeklyOffDays = setOf(Saturday, Sunday), provider = provider)
        }

        DemoSection(
            "The data is yours, and so is the rendering",
            "A NepaliEventProvider is the only holiday input, and the library draws none of it. " +
                    "Names, kinds and colors below are this screen's own; a closed day reaches a " +
                    "picker as a coloured cell and, if a rule says so, a disabled one."
        ) {
            val upcoming = remember(provider, today) { provider.entriesWithin(today, UpcomingDays) }
            if (upcoming.isEmpty()) {
                Text("Nothing in the next $UpcomingDays days", style = MaterialTheme.typography.bodySmall)
            }
            upcoming.forEach { HolidayRow(it) }
        }

        DemoSection(
            "One policy, one rule",
            "asSelectableDates turns a policy into a picker rule: its weekly off days and its " +
                    "holidays both stop being selectable. This is the office week, Saturday off."
        ) {
            val selectable = remember(officePolicy) { officePolicy.asSelectableDates() }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "The same rule for a school",
            "Saturday and Sunday off, same holidays, same call. The weekly rule lives with the " +
                    "policy, so a school states it once and both the calendar and the arithmetic " +
                    "below follow it."
        ) {
            val selectable = remember(schoolPolicy) { schoolPolicy.asSelectableDates() }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Holidays without the week",
            "weeklyOffDays = emptySet() keeps the holidays and leaves every weekday selectable, " +
                    "for a screen that has its own opinion about weekends."
        ) {
            val selectable = remember(provider) {
                NepaliCalendarPolicy(weeklyOffDays = emptySet(), provider = provider)
                    .asSelectableDates()
            }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Only the days offices close",
            "filtered narrows a shared list to the kinds a screen cares about, so the observance " +
                    "and the regional day stay selectable while the closures do not."
        ) {
            val selectable = remember(provider) {
                NepaliCalendarPolicy(
                    provider = provider.filtered { it.kind == NepaliEventKind.GovernmentPublic }
                ).asSelectableDates()
            }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "An institution's own list on top of the national one",
            "plus merges two providers, which is how a school adds its exam break and founders day " +
                    "to the national holidays without either list knowing about the other."
        ) {
            val merged = remember(provider, today) {
                NepaliCalendarPolicy(
                    weeklyOffDays = setOf(Saturday, Sunday),
                    provider = provider + SampleSchoolEvents(today)
                )
            }
            val upcoming = remember(merged, today) {
                merged.provider.entriesWithin(today, UpcomingDays)
            }
            upcoming.forEach { HolidayRow(it) }
            val state = rememberNepaliDatePickerState(
                nepaliSelectableDates = remember(merged) { merged.asSelectableDates() }
            )
            NepaliDatePicker(state = state)
        }

        DemoSection(
            "Booking from today onwards",
            "The wrappers a policy is built from stay available, so a booking screen can start " +
                    "from a built-in factory and narrow it the same way."
        ) {
            val selectable = remember(today, officePolicy) {
                NepaliDateConverter.AfterDateSelectable(today, includeDate = true)
                    .excludingWeekends(officePolicy.weeklyOffDays)
                    .excludingClosures(officePolicy.provider)
            }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Leave range over working days",
            "The same rule drives the range picker: a closed day can neither start nor end a range."
        ) {
            val selectable = remember(officePolicy) { officePolicy.asSelectableDates() }
            val state = rememberNepaliDateRangePickerState(nepaliSelectableDates = selectable)
            NepaliDateRangePicker(state = state, showMonthsVertically = false)
            val start = state.selectedStartNepaliDate.readout()
            val end = state.selectedEndNepaliDate.readout()
            SelectedText(start?.let { "Range: $it to ${end ?: "..."}" })
        }

        DemoSection(
            "Docked field over working days",
            "Any surface built on a picker state carries the rule with it, so the dropdown greys " +
                    "out the same days."
        ) {
            val selectable = remember(officePolicy) { officePolicy.asSelectableDates() }
            val state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable)
            NepaliDatePickerDocked(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Working day") }
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Typed dates judged by the same rule",
            "A text field runs the predicate over what is typed and reports null when it fails, so " +
                    "a holiday entered by hand never reaches the form."
        ) {
            val selectable = remember(officePolicy) { officePolicy.asSelectableDates() }
            var value by rememberSimpleDateState()
            var touched by remember { mutableStateOf(false) }
            val showError = touched && value == null
            NepaliDateTextField(
                value = value,
                onValueChange = {
                    touched = true
                    value = it
                },
                modifier = Modifier.fillMaxWidth(),
                selectableDates = selectable,
                isError = showError,
                label = { Text("Working day") },
                supportingText = {
                    Text(
                        if (showError) "Enter a complete date that is neither a Saturday nor a holiday"
                        else "Format: YYYY/MM/DD"
                    )
                }
            )
            SelectedText(value.readout()?.let { "Accepted: $it" })
        }

        DemoSection(
            "Counting and walking working days",
            "The converter takes the same policy, so a school and an office count the same span " +
                    "differently without either stating its week twice."
        ) {
            val spanEnd = remember(today) { offsetDate(today, WorkingDaySpan) }
            val plainWeek = remember { NepaliCalendarPolicy(provider = NoOpEventProvider) }
            LabeledValue(
                "Working days in $WorkingDaySpan, no holidays",
                NepaliDateConverter.workingDaysBetween(today, spanEnd, plainWeek).toString()
            )
            LabeledValue(
                "Working days in $WorkingDaySpan, office",
                NepaliDateConverter.workingDaysBetween(today, spanEnd, officePolicy).toString()
            )
            LabeledValue(
                "Working days in $WorkingDaySpan, school",
                NepaliDateConverter.workingDaysBetween(today, spanEnd, schoolPolicy).toString()
            )
            LabeledValue(
                "Next working day, office",
                NepaliDateConverter.nextWorkingDay(today, officePolicy).formatted()
            )
            LabeledValue(
                "+$WorkingDayStep working days, office",
                NepaliDateConverter.addWorkingDays(today, WorkingDayStep, officePolicy).formatted()
            )
            LabeledValue(
                "+$WorkingDayStep working days, school",
                NepaliDateConverter.addWorkingDays(today, WorkingDayStep, schoolPolicy).formatted()
            )
            LabeledValue(
                "-$WorkingDayStep working days, office",
                NepaliDateConverter.addWorkingDays(today, -WorkingDayStep, officePolicy).formatted()
            )
        }
    }
}

/** One holiday line coloured by kind, the rendering the library leaves to the app. */
@Composable
private fun HolidayRow(entry: NepaliCalendarEvent) {
    val kindColor = when (entry.kind) {
        NepaliEventKind.GovernmentPublic -> MaterialTheme.colorScheme.error
        NepaliEventKind.Religious -> MaterialTheme.colorScheme.primary
        NepaliEventKind.Regional -> MaterialTheme.colorScheme.tertiary
        NepaliEventKind.Observance -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(entry.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(KindGap)) {
            Text(entry.kind.name, style = MaterialTheme.typography.bodySmall, color = kindColor)
            Text(entry.date.formatted(), style = MaterialTheme.typography.bodySmall)
        }
    }
}
