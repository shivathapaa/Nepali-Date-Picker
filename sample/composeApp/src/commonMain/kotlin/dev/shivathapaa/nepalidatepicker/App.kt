/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * One demo screen of the showcase. The screen takes a [Modifier] and fills whatever it is given, so
 * every entry hosts the same way whether it is opened from the index or previewed on its own.
 */
private class ShowcaseEntry(
    val title: String,
    val summary: String,
    val screen: @Composable (Modifier) -> Unit
)

/** A titled run of entries in the index, so related screens sit together. */
private class ShowcaseGroup(val title: String, val entries: List<ShowcaseEntry>)

private val showcaseGroups = listOf(
    ShowcaseGroup(
        "Pickers",
        listOf(
            ShowcaseEntry(
                "Calendar pickers",
                "Single, localized, dual-date and range grids, and the B.S. / A.D. switch"
            ) { PickersShowcase(it) },
            ShowcaseEntry(
                "Wheel and docked",
                "The scrolling wheel and the compact field with a dropdown calendar"
            ) { WheelDockedShowcase(it) },
            ShowcaseEntry(
                "Dialogs",
                "Modal and full-screen hosts around the same pickers"
            ) { DialogsShowcase(it) },
            ShowcaseEntry(
                "Picker state",
                "Display mode, year range, and driving the selection and the month from outside"
            ) { PickerStateShowcase(it) },
            ShowcaseEntry(
                "State outside composition",
                "NepaliDatePickerState held in a state holder and shared by two surfaces"
            ) { HoistedStateShowcase(it) },
            ShowcaseEntry(
                "Customization",
                "Colors, chrome toggles, header slots, locale and layout flags"
            ) { CustomizationShowcase(it) }
        )
    ),
    ShowcaseGroup(
        "Fields",
        listOf(
            ShowcaseEntry(
                "Text fields",
                "Typed entry, picker-backed fields, styling and validation"
            ) { TextFieldsShowcase(it) },
            ShowcaseEntry(
                "Input patterns",
                "Every NepaliDateFormatter pattern, formatting and parsing on their own"
            ) { InputPatternsShowcase(it) },
            ShowcaseEntry(
                "Selectable dates",
                "Windows, custom rules and a restricted year list"
            ) { SelectableDatesShowcase(it) }
        )
    ),
    ShowcaseGroup(
        "Calendar",
        listOf(
            ShowcaseEntry(
                "Month calendar",
                "A grid that fills its width, the picked day written out, the month listed under it"
            ) { CalendarShowcase(it) }
        )
    ),
    ShowcaseGroup(
        "Events",
        listOf(
            ShowcaseEntry(
                "Calendar policy",
                "An office week against a school week, merged providers, working-day arithmetic"
            ) { HolidaysShowcase(it) },
            ShowcaseEntry(
                "Marking days",
                "One colour for a closed day, dots for events, and layering the two"
            ) { EventsShowcase(it) },
            ShowcaseEntry(
                "Event spans",
                "An event that runs longer than a day, across a month and a year end"
            ) { EventSpansShowcase(it) },
            ShowcaseEntry(
                "Event styles",
                "Every display-style switch live, the marker palette, a decorator by hand"
            ) { EventStylesShowcase(it) },
            ShowcaseEntry(
                "Event surfaces",
                "The same marking on every surface that takes a decorator"
            ) { EventSurfacesShowcase(it) }
        )
    ),
    ShowcaseGroup(
        "Engine",
        listOf(
            ShowcaseEntry(
                "Conversions and arithmetic",
                "Today, B.S. and A.D. conversion, day counts, comparison, working days"
            ) { UtilitiesShowcase(it) },
            ShowcaseEntry(
                "Formatting and names",
                "Every format style in both calendars, the month and weekday names, digit scripts"
            ) { FormattingShowcase(it) },
            ShowcaseEntry(
                "Patterns, clocks and ISO",
                "The five Unicode-pattern formatters, the two clocks, and ISO 8601 both ways round"
            ) { PatternsAndIsoShowcase(it) },
            ShowcaseEntry(
                "Month queries",
                "Month details, cross-calendar month listings, parsing and month arithmetic"
            ) { MonthQueriesShowcase(it) },
            ShowcaseEntry(
                "Range and boundaries",
                "The supported year ranges, the boundary calendars and what falls outside them"
            ) { RangeBoundariesShowcase(it) },
            ShowcaseEntry(
                "Serialization",
                "Every published type round-tripped through JSON with the serializers module"
            ) { SerializationShowcase(it) }
        )
    )
)

private val allEntries = showcaseGroups.flatMap { it.entries }

@Composable
fun App() {
    var palette by rememberSaveable { mutableStateOf(SamplePalette.Default) }
    var themeMode by rememberSaveable { mutableStateOf(SampleThemeMode.System) }

    SampleTheme(palette = palette, mode = themeMode) {
        var openTitle by rememberSaveable { mutableStateOf<String?>(null) }
        val open = allEntries.firstOrNull { it.title == openTitle }

        val indexState = rememberLazyListState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(open?.title ?: "Nepali Date Picker") },
                    navigationIcon = {
                        if (open != null) {
                            IconButton(onClick = { openTitle = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        SampleThemeMenu(
                            palette = palette,
                            mode = themeMode,
                            onPaletteChange = { palette = it },
                            onModeChange = { themeMode = it }
                        )
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (open == null) {
                    ShowcaseIndex(state = indexState, onOpen = { openTitle = it })
                } else {
                    open.screen(Modifier)
                }
            }
        }
    }
}

/** The landing list: every screen in the showcase, grouped and summarized in one line each. */
@Composable
private fun ShowcaseIndex(state: LazyListState, onOpen: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
        showcaseGroups.forEach { group ->
            item(key = "header-${group.title}") {
                Text(
                    text = group.title.uppercase(),
                    modifier = Modifier.padding(start = IndexPadding, top = GroupGap, bottom = LabelGap),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(group.entries, key = { it.title }) { entry ->
                ListItem(
                    headlineContent = { Text(entry.title) },
                    supportingContent = { Text(entry.summary) },
                    modifier = Modifier.clickable { onOpen(entry.title) }
                )
                HorizontalDivider()
            }
        }
    }
}

private val IndexPadding = 16.dp
private val GroupGap = 20.dp
private val LabelGap = 4.dp
