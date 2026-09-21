/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepicker

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState

/** Bikram Sambat month numbers the supported range opens and closes on. */
private const val FirstMonth = 1
private const val LastMonth = 12

/**
 * Where the calendar stops. Conversion is table-driven, so the library knows exactly how far it can
 * go in either direction, and it says so rather than guessing past the end. Everything here is a
 * constant or a pure function: a screen can check a date against the supported range before it ever
 * builds a picker.
 */
@Composable
fun RangeBoundariesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        DemoSection(
            "The supported years",
            "Extending either range means extending the conversion table, so these are facts about " +
                    "the build rather than settings. A picker's own yearRange can narrow them but " +
                    "never widen them."
        ) {
            LabeledValue(
                "NepaliYearRange",
                "${NepaliCalendarDefaults.NepaliYearRange.first} to ${NepaliCalendarDefaults.NepaliYearRange.last}"
            )
            LabeledValue(
                "EnglishYearRange",
                "${NepaliCalendarDefaults.EnglishYearRange.first} to ${NepaliCalendarDefaults.EnglishYearRange.last}"
            )
            LabeledValue(
                "GregorianYearRange",
                "${NepaliCalendarDefaults.GregorianYearRange.first} to ${NepaliCalendarDefaults.GregorianYearRange.last}"
            )
            LabeledValue("First day of the week", NepaliCalendarDefaults.FIRST_DAY_OF_WEEK.toString())
        }

        DemoSection(
            "The anchor days",
            "The two calendars are tied together at one pair of days, and every conversion is a day " +
                    "count from there. The Bikram Sambat year opens in Baisakh, which is mid-April, " +
                    "so the two ranges do not start together."
        ) {
            val bsStart = NepaliCalendarDefaults.startingNepaliCalendar
            val adStart = NepaliCalendarDefaults.startingEnglishCalendar
            val bsEnd = NepaliCalendarDefaults.endNepaliCalendar
            LabeledValue("First BS day", bsStart.formatted())
            LabeledValue(
                "Its weekday",
                NepaliDateConverter.getWeekdayName(bsStart.dayOfWeek, NameFormat.FULL)
            )
            LabeledValue("The same day in AD", adStart.formatted())
            LabeledValue("Last BS day", bsEnd.formatted())
            LabeledValue("Its day of the year", bsEnd.dayOfYear.toString())
        }

        DemoSection(
            "The convertible window is narrower than the year range",
            "A Gregorian year at either end is only partly covered, because the Bikram Sambat year " +
                    "it maps into starts in the middle of it. These two dates are the real bounds a " +
                    "Gregorian-first screen has to respect."
        ) {
            val min = NepaliCalendarDefaults.minConvertibleEnglishDate
            val max = NepaliCalendarDefaults.maxConvertibleEnglishDate
            LabeledValue("minConvertibleEnglishDate", min.formatted())
            LabeledValue("maxConvertibleEnglishDate", max.formatted())
            LabeledValue(
                "${min.year}/1/1 convertible",
                NepaliDateConverter.isEnglishDateConvertible(min.year, 1, 1).toString()
            )
            LabeledValue(
                "The day before the anchor",
                NepaliDateConverter
                    .isEnglishDateConvertible(min.year, min.month, min.dayOfMonth - 1)
                    .toString()
            )
            LabeledValue(
                "The anchor itself",
                NepaliDateConverter
                    .isEnglishDateConvertible(min.year, min.month, min.dayOfMonth)
                    .toString()
            )
            LabeledValue(
                "The last convertible day",
                NepaliDateConverter
                    .isEnglishDateConvertible(max.year, max.month, max.dayOfMonth)
                    .toString()
            )
            LabeledValue(
                "One year past the end",
                NepaliDateConverter.isEnglishDateConvertible(max.year + 1, 1, 1).toString()
            )
        }

        DemoSection(
            "Narrowing the range moves the Gregorian one with it",
            "gregorianYearRangeFor answers which English years a picker should offer for a given " +
                    "Bikram Sambat range, so both calendars page over the same span of real days. " +
                    "The result is clamped, because the tail of the Bikram Sambat range reaches into " +
                    "an English year the converter will not take."
        ) {
            val samples = remember {
                listOf(
                    NepaliCalendarDefaults.NepaliYearRange,
                    2080..2085,
                    2000..2010,
                    NepaliCalendarDefaults.NepaliYearRange.last..NepaliCalendarDefaults.NepaliYearRange.last
                )
            }
            samples.forEach { range ->
                val gregorian = NepaliCalendarDefaults.gregorianYearRangeFor(range)
                LabeledValue(
                    "BS ${range.first}..${range.last}",
                    "AD ${gregorian.first}..${gregorian.last}"
                )
            }
        }

        DemoSection(
            "The first supported month",
            "A picker pinned to the opening of the range. There is nothing before Baisakh 1 of " +
                    "${NepaliCalendarDefaults.NepaliYearRange.first}, so the arrow back is inert and " +
                    "the year list starts here."
        ) {
            val firstYear = NepaliCalendarDefaults.NepaliYearRange.first
            val state = rememberNepaliDatePickerState(
                initialDisplayedMonth = remember { SimpleDate(firstYear, FirstMonth, 1) }
            )
            NepaliDatePicker(state = state)
            LabeledValue(
                "Displayed month",
                "${state.displayedMonth.year}/${state.displayedMonth.month}"
            )
        }

        DemoSection(
            "The last supported month",
            "The other end. Chaitra of ${NepaliCalendarDefaults.NepaliYearRange.last} is the last " +
                    "month the table describes, so the grid stops there."
        ) {
            val lastYear = NepaliCalendarDefaults.NepaliYearRange.last
            val state = rememberNepaliDatePickerState(
                initialDisplayedMonth = remember { SimpleDate(lastYear, LastMonth, 1) }
            )
            NepaliDatePicker(state = state)
            LabeledValue(
                "Displayed month",
                "${state.displayedMonth.year}/${state.displayedMonth.month}"
            )
            LabeledValue(
                "Days in it",
                NepaliDateConverter.getTotalDaysInNepaliMonth(lastYear, LastMonth).toString()
            )
        }

        DemoSection(
            "A Gregorian grid at the very start",
            "Opened on the anchor month with the switch on. The days of " +
                    "${NepaliCalendarDefaults.minConvertibleEnglishDate.year} before the anchor have " +
                    "no Bikram Sambat equivalent, so they are drawn disabled rather than left out: " +
                    "the month keeps its shape and the user can see why those days are unavailable."
        ) {
            val min = NepaliCalendarDefaults.minConvertibleEnglishDate
            val state = rememberNepaliDatePickerState(
                initialDisplayedMonth = remember(min) {
                    SimpleDate(NepaliCalendarDefaults.NepaliYearRange.first, FirstMonth, 1)
                },
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(
                state = state,
                secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale,
                showCalendarSystemToggle = true,
                showAdjacentMonthDays = true
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Out-of-range values are coerced, not rejected",
            "A state given a displayed month or a selected date outside its range does not throw. " +
                    "The month is clamped into the range and an impossible selection resolves to no " +
                    "selection, so a restored draft from an older build cannot crash a screen."
        ) {
            val state = rememberNepaliDatePickerState(
                initialSelectedDate = remember { SimpleDate(1900, 1, 1) },
                initialDisplayedMonth = remember { SimpleDate(1900, 1, 1) },
                yearRange = remember { 2080..2085 }
            )
            NepaliDatePicker(state = state)
            LabeledValue("Asked for", "1900/1/1")
            LabeledValue("Selection", state.selectedDate.readout() ?: "none")
            LabeledValue(
                "Displayed month",
                "${state.displayedMonth.year}/${state.displayedMonth.month}"
            )
            Text(
                text = "The same holds for a date past the end of the range, and for a day number " +
                        "that does not exist in the month it names.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
