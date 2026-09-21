/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeTextField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateTextField
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate

private val NepaliLocale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)

/**
 * The shape typed dates come in. A pattern fixes the field order and the separator for both the mask
 * the user types into and the text the field writes back, and the same four patterns are available
 * without a field at all through [NepaliDateFormatter], which is what a screen parsing a date out of
 * a query string or a CSV row reaches for.
 */
@Composable
fun InputPatternsShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliCalendar.toSimpleDate() }

        DemoSection(
            "Every pattern, as a field",
            "The placeholder shows the mask, and typing is constrained to it. All four accept Latin " +
                    "and Devanagari digits whichever one they print in."
        ) {
            Pattern.entries.forEach { pattern ->
                var value by rememberSimpleDateState()
                NepaliDateTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(),
                    dateFormat = pattern,
                    label = { Text(pattern.literal) }
                )
                SelectedText(value.readout()?.let { "Parsed: $it" })
                VerticalGap()
            }
        }

        DemoSection(
            "The same date written out in each",
            "NepaliDateFormatter.format takes a date, a pattern and a digit script, so the text a " +
                    "field would show can be produced without a field. The second column is the " +
                    "same call asking for Devanagari digits."
        ) {
            Pattern.entries.forEach { pattern ->
                LabeledValue(
                    pattern.literal,
                    NepaliDateFormatter.format(today, pattern)
                )
            }
            VerticalGap()
            Pattern.entries.forEach { pattern ->
                LabeledValue(
                    "${pattern.literal} (Devanagari)",
                    NepaliDateFormatter.format(today, pattern, DigitScript.DEVANAGARI)
                )
            }
        }

        DemoSection(
            "Parsing text back",
            "NepaliDateFormatter.parse is the inverse, and returns null rather than throwing on " +
                    "anything that is not a complete date in the pattern it was given. Type below " +
                    "and watch each pattern accept or reject the same string."
        ) {
            var typed by rememberSaveable { mutableStateOf(NepaliDateFormatter.format(today, Pattern.YYYY_SLASH_MM_SLASH_DD)) }
            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Text to parse") },
                singleLine = true
            )
            Pattern.entries.forEach { pattern ->
                LabeledValue(
                    pattern.literal,
                    NepaliDateFormatter.parse(typed, pattern)?.formatted() ?: "no match"
                )
            }
            Text(
                text = "A month outside 1 to 12 or a day outside 1 to 32 is rejected here. Whether " +
                        "the day actually exists in that month is the calendar's question, not the " +
                        "formatter's, so pair this with a selectable-date rule when it matters.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        DemoSection(
            "Changing the separator afterwards",
            "replaceDelimiter rewrites a formatted string without re-parsing it, which is the cheap " +
                    "way to move between the slash a field prints and the dash an API expects. It " +
                    "works on any string, so a formatted time takes it too."
        ) {
            val slashed = remember(today) {
                NepaliDateFormatter.format(today, Pattern.YYYY_SLASH_MM_SLASH_DD)
            }
            val time = remember { NepaliDateConverter.currentTime }
            LabeledValue("As printed", slashed)
            LabeledValue("Dashed", NepaliDateConverter.replaceDelimiter(slashed, "-"))
            LabeledValue("Dotted", NepaliDateConverter.replaceDelimiter(slashed, "."))
            LabeledValue(
                "Devanagari, dashed",
                NepaliDateConverter.replaceDelimiter(
                    NepaliDateFormatter.format(today, Pattern.YYYY_SLASH_MM_SLASH_DD, DigitScript.DEVANAGARI),
                    "-"
                )
            )
            LabeledValue(
                "A time, spaced",
                NepaliDateConverter.replaceDelimiter(
                    NepaliDateConverter.getFormattedTimeInEnglish(time),
                    newDelimiter = " ",
                    oldDelimiter = ":"
                )
            )
        }

        DemoSection(
            "A day-first field in Nepali",
            "The pattern and the locale are separate knobs: the pattern decides the order and the " +
                    "separator, the locale decides the digits and the language around it."
        ) {
            var value by rememberSimpleDateState(initial = today)
            NepaliDateTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                dateFormat = Pattern.DD_DASH_MM_DASH_YYYY,
                locale = NepaliLocale,
                label = { Text("मिति") }
            )
            SelectedText(value.readout()?.let { "Parsed: $it" })
        }

        DemoSection(
            "A range typed in one pattern",
            "The pair takes the same pattern for both ends, so a form never asks a user to switch " +
                    "shape halfway through a range."
        ) {
            var start by rememberSimpleDateState()
            var end by rememberSimpleDateState()
            NepaliDateRangeTextField(
                startValue = start,
                endValue = end,
                onRangeChange = { newStart, newEnd ->
                    start = newStart
                    end = newEnd
                },
                modifier = Modifier.fillMaxWidth(),
                dateFormat = Pattern.YYYY_DASH_MM_DASH_DD
            )
            SelectedText(rangeLine(start, end))
        }
    }
}

private fun rangeLine(start: SimpleDate?, end: SimpleDate?): String? =
    if (start == null && end == null) null
    else "Range: ${start.readout() ?: "..."} to ${end.readout() ?: "..."}"
