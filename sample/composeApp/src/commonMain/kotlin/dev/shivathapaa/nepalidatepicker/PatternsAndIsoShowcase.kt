/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate

/** A starting pattern that uses a token from each family, so editing it teaches the whole set. */
private const val StartingDatePattern = "EEEE, dd MMMM yyyy"
private const val StartingTimePattern = "hh:mm a"

/**
 * Writing a date or a time out in a shape you chose yourself. A format style names a shape the
 * library already knows; a Unicode pattern is a string of tokens, which is what an API contract or a
 * report header usually asks for. ISO is the same idea with the shape fixed by the standard.
 */
@Composable
fun PatternsAndIsoShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val nepaliToday = remember { NepaliDateConverter.todayNepaliCalendar }
        val englishToday = remember { NepaliDateConverter.todayEnglishCalendar }
        val time = remember { NepaliDateConverter.currentTime }

        DemoSection(
            "A pattern of your own",
            "Tokens: yyyy yy for the year, MMMM MMM MM M for the month, dd d for the day, D for the " +
                    "day of the year, EEEE E EEEEE for the weekday, ee e for its number, w for the " +
                    "week of the year. Anything else is copied through, and there is no escape " +
                    "syntax, so a literal word made of token letters will be rewritten. Edit the " +
                    "pattern and both calendars re-render."
        ) {
            var pattern by rememberSaveable { mutableStateOf(StartingDatePattern) }
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date pattern") },
                singleLine = true
            )
            LabeledValue(
                "Bikram Sambat",
                NepaliDateConverter.formatNepaliDateByUnicodePattern(
                    unicodePattern = pattern,
                    calendar = nepaliToday,
                    language = NepaliDatePickerLang.ENGLISH
                )
            )
            LabeledValue(
                "Bikram Sambat (नेपाली)",
                NepaliDateConverter.formatNepaliDateByUnicodePattern(
                    unicodePattern = pattern,
                    calendar = nepaliToday
                )
            )
            LabeledValue(
                "Gregorian",
                NepaliDateConverter.formatEnglishDateByUnicodePattern(
                    unicodePattern = pattern,
                    calendar = englishToday
                )
            )
            LabeledValue(
                "Gregorian (नेपाली)",
                NepaliDateConverter.formatEnglishDateByUnicodePattern(
                    unicodePattern = pattern,
                    calendar = englishToday,
                    language = NepaliDatePickerLang.NEPALI
                )
            )
        }

        DemoSection(
            "A pattern for a time",
            "Tokens: HH H for the 24-hour clock, hh h for the 12-hour one, mm m ss s for minutes " +
                    "and seconds, S through SSSS for fractions, a A for the meridiem in either case."
        ) {
            var pattern by rememberSaveable { mutableStateOf(StartingTimePattern) }
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Time pattern") },
                singleLine = true
            )
            LabeledValue(
                "English",
                NepaliDateConverter.formatTimeByUnicodePattern(pattern, time)
            )
            LabeledValue(
                "Nepali",
                NepaliDateConverter.formatTimeByUnicodePattern(
                    unicodePattern = pattern,
                    time = time,
                    language = NepaliDatePickerLang.NEPALI
                )
            )
        }

        DemoSection(
            "Date and time in one pattern",
            "The date-time formatters take both token families at once. Leave the time out and the " +
                    "clock reads from now, which is what a timestamp on a freshly written record wants."
        ) {
            val pattern = "EEEE, dd MMMM yyyy, hh:mm a"
            LabeledValue("Pattern", pattern)
            LabeledValue(
                "Bikram Sambat",
                NepaliDateConverter.formatNepaliDateTimeByUnicodePattern(
                    unicodePattern = pattern,
                    calendar = nepaliToday,
                    time = time,
                    language = NepaliDatePickerLang.ENGLISH
                )
            )
            LabeledValue(
                "Gregorian",
                NepaliDateConverter.formatEnglishDateTimeByUnicodePattern(
                    unicodePattern = pattern,
                    calendar = englishToday,
                    time = time
                )
            )
            LabeledValue(
                "Bikram Sambat, clock from now",
                NepaliDateConverter.formatNepaliDateTimeByUnicodePattern(
                    unicodePattern = "yyyy/MM/dd HH:mm:ss",
                    calendar = nepaliToday,
                    language = NepaliDatePickerLang.ENGLISH
                )
            )
        }

        DemoSection(
            "Clocks",
            "The two ready-made time formats, in both languages and both clock conventions."
        ) {
            LabeledValue("English 12h", NepaliDateConverter.getFormattedTimeInEnglish(time))
            LabeledValue(
                "English 24h",
                NepaliDateConverter.getFormattedTimeInEnglish(time, use12HourFormat = false)
            )
            LabeledValue("Nepali 12h", NepaliDateConverter.getFormattedTimeInNepali(time))
            LabeledValue(
                "Nepali 24h",
                NepaliDateConverter.getFormattedTimeInNepali(time, use12HourFormat = false)
            )
        }

        DemoSection(
            "ISO 8601, both ways round",
            "A Bikram Sambat date carries a Nepali wall clock, and the Gregorian pair carries the " +
                    "same clock against the converted date. Each has a parser that reads its own " +
                    "output back into a calendar and a time."
        ) {
            val nepaliIso = remember(nepaliToday, time) {
                NepaliDateConverter.formatNepaliDateTimeToIsoFormat(nepaliToday.toSimpleDate(), time)
            }
            val englishIso = remember(englishToday, time) {
                NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(
                    englishToday.toSimpleDate(),
                    time
                )
            }
            LabeledValue("BS date, ISO", nepaliIso)
            LabeledValue(
                "Read back (BS)",
                NepaliDateConverter.getNepaliDateTimeFromIsoFormat(nepaliIso).customCalendar.formatted()
            )
            LabeledValue("AD date, ISO", englishIso)
            LabeledValue(
                "Read back (AD)",
                NepaliDateConverter
                    .getEnglishDateNepaliTimeFromIsoFormat(englishIso)
                    .customCalendar
                    .formatted()
            )
            Text(
                text = "Both strings are UTC-anchored, so they sort and compare the way any other " +
                        "ISO timestamp does.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
