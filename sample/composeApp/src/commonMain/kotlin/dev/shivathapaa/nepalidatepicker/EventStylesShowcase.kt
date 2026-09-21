/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalLayoutApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.then
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState

private val ChipGap = 8.dp

/**
 * Every knob the marking has: the six switches of a display style, the eight colours of a palette,
 * and a decorator written by hand. The Events tab shows the defaults; this one takes them apart.
 */
@Composable
fun EventStylesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val provider = remember(today) { SampleEventProvider(today) }
        val policy = remember(provider) { NepaliCalendarPolicy(provider = provider) }
        val events = remember(today) { sampleEvents(today) }

        DemoSection(
            "The six switches, live",
            "A display style says what the marking is allowed to draw. Toggle each one and watch " +
                    "the same policy render differently: nothing here changes the data."
        ) {
            var colorWeeklyOff by rememberSaveable { mutableStateOf(true) }
            var colorEvents by rememberSaveable { mutableStateOf(true) }
            var tintContainer by rememberSaveable { mutableStateOf(false) }
            var indicateWeeklyOff by rememberSaveable { mutableStateOf(false) }
            var indicateClosures by rememberSaveable { mutableStateOf(false) }
            var describe by rememberSaveable { mutableStateOf(true) }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
                SwitchChip("colorWeeklyOff", colorWeeklyOff) { colorWeeklyOff = it }
                SwitchChip("colorEvents", colorEvents) { colorEvents = it }
                SwitchChip("tintContainer", tintContainer) { tintContainer = it }
                SwitchChip("indicateWeeklyOff", indicateWeeklyOff) { indicateWeeklyOff = it }
                SwitchChip("indicateKinds", indicateClosures) { indicateClosures = it }
                SwitchChip("describe", describe) { describe = it }
            }

            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = policy,
                    style = NepaliDatePickerDefaults.eventDisplayStyle(
                        colorWeeklyOff = colorWeeklyOff,
                        colorEvents = colorEvents,
                        tintContainer = tintContainer,
                        indicateWeeklyOff = indicateWeeklyOff,
                        indicateKinds = if (indicateClosures) ClosingKinds else emptySet(),
                        describe = describe
                    )
                )
            )
            Text(
                text = "describe = false leaves the day's own label alone, so a screen reader " +
                        "announces the date and nothing about what is on it.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        DemoSection(
            "A palette of your own",
            "Every slot a mark can take is overridable, and an unset slot keeps the theme's own. " +
                    "These are the app's brand colours rather than the Material defaults."
        ) {
            val brandColors = NepaliDatePickerDefaults.markerColors(
                weeklyOffColor = MaterialTheme.colorScheme.tertiary,
                publicHolidayColor = MaterialTheme.colorScheme.error,
                religiousColor = MaterialTheme.colorScheme.primary,
                regionalColor = MaterialTheme.colorScheme.secondary,
                observanceColor = MaterialTheme.colorScheme.onSurfaceVariant,
                eventColor = MaterialTheme.colorScheme.primary,
                personalColor = MaterialTheme.colorScheme.tertiary,
                markedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = policy,
                    colors = brandColors,
                    style = NepaliDatePickerDefaults.eventDisplayStyle(tintContainer = true)
                )
            )
        }

        DemoSection(
            "One kind at a time",
            "indicateKinds opts individual kinds into a dot. Here only the days offices close get " +
                    "one, so a religious festival still colours its day without spending a slot."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(
                    policy = policy,
                    style = NepaliDatePickerDefaults.eventDisplayStyle(
                        indicateKinds = setOf(NepaliEventKind.GovernmentPublic)
                    )
                )
            )
        }

        DemoSection(
            "A decorator written by hand",
            "NepaliDayDecorator is a fun interface over one day at a time, and the day it is given " +
                    "says whether it is today, selected, in a range, enabled, or borrowed from the " +
                    "next month. This one marks today and greys the borrowed cells."
        ) {
            val accent = MaterialTheme.colorScheme.primary
            val muted = MaterialTheme.colorScheme.outlineVariant
            val custom = remember(accent, muted) {
                NepaliDayDecorator { day ->
                    when {
                        day.isToday -> NepaliDayDecoration(
                            indicators = listOf(accent),
                            contentDescription = "Today"
                        )

                        day.isAdjacentMonth -> NepaliDayDecoration(contentColor = muted)
                        else -> null
                    }
                }
            }
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = custom, showAdjacentMonthDays = true)
        }

        DemoSection(
            "Three decorators deep",
            "then layers one over another: the first to answer keeps the colour, and every layer's " +
                    "dots are kept in order until the cell's three are full. Here the policy leads, " +
                    "the app's events follow, and a hand-written layer marks today last."
        ) {
            val markerColors = NepaliDatePickerDefaults.markerColors()
            val accent = MaterialTheme.colorScheme.primary
            val todayMark = remember(accent) {
                NepaliDayDecorator { day ->
                    if (day.isToday) NepaliDayDecoration(indicators = listOf(accent)) else null
                }
            }
            val layered = NepaliDatePickerDefaults.eventDecorator(policy = policy)
                .then(
                    NepaliDatePickerDefaults.dayDecorator(
                        markers = remember(events, markerColors) { events.markers(markerColors) },
                        descriptions = remember(events) { events.titles() }
                    )
                )
                .then(todayMark)
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(state = state, dayDecorator = layered)
            val selected = state.selectedDate?.toSimpleDate()
            SelectedText(selected?.let { "${it.formatted()}: ${events.on(it).size} event(s)" })
        }

        DemoSection(
            "Colour without a policy",
            "dayDecorator takes plain maps, so an app that already holds its days by date needs no " +
                    "provider at all: a content colour, a list of dots, and a label per date."
        ) {
            val markerColors = NepaliDatePickerDefaults.markerColors()
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.dayDecorator(
                    markers = remember(events, markerColors) { events.markers(markerColors) },
                    contentColors = remember(events, markerColors) {
                        events.associate { it.date to markerColors.eventColor }
                    },
                    descriptions = remember(events) { events.titles() }
                )
            )
        }
    }
}

/** The kinds that shut an institution by default, for the indicateKinds switch above. */
private val ClosingKinds = setOf(
    NepaliEventKind.GovernmentPublic,
    NepaliEventKind.Religious,
    NepaliEventKind.Regional
)

/** One switch of a display style, as a chip, so a tap re-renders the grid beside it. */
@Composable
private fun SwitchChip(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    FilterChip(
        selected = checked,
        onClick = { onChange(!checked) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
