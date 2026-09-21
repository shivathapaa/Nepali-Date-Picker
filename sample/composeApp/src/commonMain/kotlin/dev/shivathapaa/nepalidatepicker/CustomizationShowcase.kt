/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

private val EnglishLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)

/** How far the surface bends: colors, chrome toggles, header slots, locale, and layout flags. */
@Composable
fun CustomizationShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        DemoSection(
            "Custom colors",
            "Every part of the calendar is themeable through NepaliDatePickerDefaults.colors()."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                colors = NepaliDatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    headlineContentColor = MaterialTheme.colorScheme.primary,
                    weekdayContentColor = MaterialTheme.colorScheme.primary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        DemoSection(
            "One slot changed, the rest inherited",
            "copy() on a resolved palette keeps every slot the theme already picked and replaces " +
                    "only the named ones, which is the cheaper edit when a product disagrees with " +
                    "the default about one colour rather than about the scheme."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                colors = NepaliDatePickerDefaults.colors().copy(
                    todayDateBorderColor = MaterialTheme.colorScheme.tertiary,
                    todayContentColor = MaterialTheme.colorScheme.tertiary
                )
            )
        }

        DemoSection(
            "Minimal chrome",
            "Hide the mode toggle, the today button, the title, and the headline."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                title = null,
                headline = null,
                showModeToggle = false,
                showTodayButton = false
            )
        }

        DemoSection(
            "Custom title and headline",
            "The title and headline are slots you can fully replace."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePicker(
                state = state,
                title = { Text("Choose your date", style = MaterialTheme.typography.titleMedium) },
                headline = {
                    Text(
                        text = state.selectedDate.readout() ?: "Nothing selected yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                showModeToggle = false
            )
        }

        DemoSection(
            "Nepali language and short names",
            "Language drives Devanagari digits; weekday and month name lengths are independent knobs."
        ) {
            val state = rememberNepaliDatePickerState(
                locale = NepaliDateLocale(
                    language = NepaliDatePickerLang.NEPALI,
                    weekDayName = NameFormat.SHORT,
                    monthName = NameFormat.FULL
                )
            )
            NepaliDatePicker(state = state)
        }

        DemoSection(
            "Range picker layout flags",
            "Horizontal months, no year picker or month navigation, and no mode toggle."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                showModeToggle = false,
                showYearPickerAndMonthNavigation = false
            )
        }

        DemoSection(
            "The dual-date range headline on its own",
            "NepaliDateRangePickerHeadlineWithEnglishDate is the picker's own headline, exported so " +
                    "a screen that hides the built-in one can draw the same two lines somewhere " +
                    "else and keep them in step with the selection."
        ) {
            val state = rememberNepaliDateRangePickerState()
            NepaliDatePickerDefaults.NepaliDateRangePickerHeadlineWithEnglishDate(
                selectedNepaliStartDate = state.selectedStartNepaliDate,
                selectedNepaliEndDate = state.selectedEndNepaliDate,
                selectedEnglishStartDate = state.selectedStartEnglishDate,
                selectedEnglishEndDate = state.selectedEndEnglishDate,
                locale = state.locale,
                englishLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale,
                calendarSystem = state.displayedCalendarSystem
            )
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                title = null,
                headline = null
            )
        }

        DemoSection(
            "The dialog's own surface",
            "NepaliDatePickerDefaults carries the shape and tonal elevation the dialog uses by " +
                    "default, so a screen can lean on them rather than restating a number, or start " +
                    "from them and change one."
        ) {
            var show by remember { mutableStateOf(false) }
            val state = rememberNepaliDatePickerState()
            LabeledValue("Default tonal elevation", "${NepaliDatePickerDefaults.TonalElevation}")
            LabeledValue("Default date format", NepaliDatePickerDefaults.DateFormatStyle.name)
            Button(onClick = { show = true }) { Text("Open with the defaults") }
            if (show) {
                NepaliDatePickerDialog(
                    onDismissRequest = { show = false },
                    confirmButton = {
                        NepaliDatePickerDefaults.DialogButton("Done", { show = false })
                    },
                    shape = NepaliDatePickerDefaults.shape,
                    tonalElevation = NepaliDatePickerDefaults.TonalElevation
                ) {
                    NepaliDatePicker(state = state)
                }
            }
        }

        DemoSection(
            "Date format styles",
            "The same date rendered in every NepaliDateFormatStyle (English)."
        ) {
            val today = remember { NepaliDateConverter.todayNepaliCalendar }
            val lines = remember(today) {
                NepaliDateFormatStyle.entries.map { style ->
                    style.name to NepaliDateConverter.formatNepaliDate(
                        today, EnglishLocale.copy(dateFormat = style)
                    )
                }
            }
            lines.forEach { (label, value) -> LabeledValue(label, value) }
        }

        DemoSection(
            "Digit scripts and name lengths",
            "Digit script and weekday/month name length can be set without changing the language."
        ) {
            val today = remember { NepaliDateConverter.todayNepaliCalendar }
            val previews = remember(today) {
                val base = EnglishLocale.copy(dateFormat = NepaliDateFormatStyle.FULL)
                listOf(
                    "Latin digits" to NepaliDateConverter.formatNepaliDate(today, base.copy(digitScript = DigitScript.LATIN)),
                    "Devanagari digits" to NepaliDateConverter.formatNepaliDate(today, base.copy(digitScript = DigitScript.DEVANAGARI)),
                    "Weekday FULL" to NepaliDateConverter.getWeekdayName(today.dayOfWeek, NameFormat.FULL),
                    "Weekday SHORT" to NepaliDateConverter.getWeekdayName(today.dayOfWeek, NameFormat.SHORT),
                    "Month MEDIUM" to NepaliDateConverter.getMonthName(today.month, NameFormat.MEDIUM),
                    "Month SHORT" to NepaliDateConverter.getMonthName(today.month, NameFormat.SHORT)
                )
            }
            previews.forEach { (label, value) -> LabeledValue(label, value) }
        }
    }
}
