/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat

private val ButtonGap = 8.dp

/** How many of a month's days the listings print before they are cut short. */
private const val PreviewDays = 4

private const val MonthStep = 1

/**
 * The month as a unit. A picker needs to know how long a month is, which weekday it opens on and
 * what the other calendar's days line up with, and all of that is answerable without drawing
 * anything. This screen is those queries with a stepper over them, plus the four methods
 * [NepaliCalendarModel] carries that the [NepaliDateConverter] facade does not.
 */
@Composable
fun MonthQueriesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val calendarModel = remember { NepaliCalendarModel() }
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val todayEnglish = remember { NepaliDateConverter.todayEnglishSimpleDate }

        // Held as two ints so the stepper's position survives a configuration change.
        var year by rememberSaveable { mutableIntStateOf(today.year) }
        var month by rememberSaveable { mutableIntStateOf(today.month) }
        val nepaliMonth = remember(year, month) {
            NepaliDateConverter.getNepaliMonthCalendar(year, month)
        }

        DemoSection(
            "A Bikram Sambat month",
            "getNepaliMonthCalendar answers the three facts a grid is built from: how many days the " +
                    "month has, and which weekday its first and last days fall on. Step through the " +
                    "months and watch the length change, since a Bikram Sambat month runs 29 to 32 " +
                    "days and the pattern differs year to year."
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(ButtonGap)) {
                OutlinedButton(
                    onClick = {
                        val stepped = steppedMonth(calendarModel, nepaliMonth, -MonthStep)
                        year = stepped.year
                        month = stepped.month
                    }
                ) { Text("Previous") }
                OutlinedButton(
                    onClick = {
                        val stepped = steppedMonth(calendarModel, nepaliMonth, MonthStep)
                        year = stepped.year
                        month = stepped.month
                    }
                ) { Text("Next") }
                OutlinedButton(
                    onClick = {
                        year = today.year
                        month = today.month
                    }
                ) { Text("Today") }
            }
            LabeledValue(
                "Month",
                "${nepaliMonth.year} ${NepaliDateConverter.getMonthName(nepaliMonth.month, NameFormat.FULL)}"
            )
            LabeledValue("Days in month", nepaliMonth.totalDaysInMonth.toString())
            LabeledValue(
                "Opens on",
                NepaliDateConverter.getWeekdayName(nepaliMonth.firstDayOfMonth, NameFormat.FULL)
            )
            LabeledValue(
                "Closes on",
                NepaliDateConverter.getWeekdayName(nepaliMonth.lastDayOfMonth, NameFormat.FULL)
            )
            LabeledValue(
                "getTotalDaysInNepaliMonth",
                NepaliDateConverter.getTotalDaysInNepaliMonth(year, month).toString()
            )
        }

        DemoSection(
            "The Gregorian counterpart",
            "getEnglishMonthCalendar is the same shape for the other calendar, and it carries the " +
                    "calendar system it describes, so a value passed around says which month it is."
        ) {
            val englishMonth = remember(todayEnglish) {
                NepaliDateConverter.getEnglishMonthCalendar(todayEnglish.year, todayEnglish.month)
            }
            LabeledValue(
                "Month",
                "${englishMonth.year} ${NepaliDateConverter.getEnglishMonthName(englishMonth.month, NameFormat.FULL)}"
            )
            LabeledValue("Calendar system", englishMonth.calendarSystem.name)
            LabeledValue("Days in month", englishMonth.totalDaysInMonth.toString())
            LabeledValue(
                "Opens on",
                NepaliDateConverter.getWeekdayName(englishMonth.firstDayOfMonth, NameFormat.FULL)
            )
            LabeledValue(
                "getTotalDaysInEnglishMonth",
                NepaliDateConverter.getTotalDaysInEnglishMonth(todayEnglish.year, todayEnglish.month).toString()
            )
        }

        DemoSection(
            "Every day of a Bikram Sambat month, in Gregorian",
            "getEnglishCalendarsInNepaliMonth walks the month day by day and hands back the " +
                    "Gregorian day each one lands on. This is what the dual-date grid is drawn from, " +
                    "and it is also how a month that straddles two Gregorian months is detected."
        ) {
            val paired = remember(year, month) {
                NepaliDateConverter.getEnglishCalendarsInNepaliMonth(year, month)
            }
            LabeledValue("Entries", paired.size.toString())
            paired.take(PreviewDays).forEachIndexed { index, english ->
                LabeledValue("BS day ${index + 1}", english.formatted())
            }
            LabeledValue("BS day ${paired.size}", paired.last().formatted())
            LabeledValue(
                "Gregorian months crossed",
                paired.map { it.month }.distinct().joinToString()
            )
        }

        DemoSection(
            "Every day of a Gregorian month, in Bikram Sambat",
            "getNepaliCalendarsInEnglishMonth is the reverse, and its list is nullable: a Gregorian " +
                    "day outside the supported range has no Bikram Sambat equivalent, and comes back " +
                    "as null rather than as a guess."
        ) {
            val paired = remember(todayEnglish) {
                NepaliDateConverter.getNepaliCalendarsInEnglishMonth(
                    todayEnglish.year,
                    todayEnglish.month
                )
            }
            LabeledValue("Entries", paired.size.toString())
            LabeledValue("Convertible", paired.count { it != null }.toString())
            paired.take(PreviewDays).forEachIndexed { index, nepali ->
                LabeledValue("AD day ${index + 1}", nepali?.formatted() ?: "no equivalent")
            }
        }

        DemoSection(
            "Every day of a Gregorian month, as itself",
            "getEnglishCalendarsInMonth fills in the weekday and month length for each day, which " +
                    "is the same detail the Bikram Sambat listing carries and what a Gregorian-first " +
                    "grid needs."
        ) {
            val days = remember(todayEnglish) {
                NepaliDateConverter.getEnglishCalendarsInMonth(todayEnglish.year, todayEnglish.month)
            }
            LabeledValue("Entries", days.size.toString())
            days.take(PreviewDays).forEach { day ->
                LabeledValue(
                    day.formatted(),
                    NepaliDateConverter.getWeekdayName(day.dayOfWeek, NameFormat.MEDIUM)
                )
            }
        }

        DemoSection(
            "One day, in full",
            "getNepaliCalendar and getEnglishCalendar return a day with its weekday, its month's " +
                    "length and its day of the year filled in, which is more than a bare year, month " +
                    "and day triple carries."
        ) {
            val nepaliDay = remember(today) {
                NepaliDateConverter.getNepaliCalendar(today.year, today.month, today.dayOfMonth)
            }
            val englishDay = remember(todayEnglish) {
                NepaliDateConverter.getEnglishCalendar(
                    todayEnglish.year,
                    todayEnglish.month,
                    todayEnglish.dayOfMonth
                )
            }
            LabeledValue("BS date", nepaliDay.formatted())
            LabeledValue(
                "BS weekday",
                NepaliDateConverter.getWeekdayName(nepaliDay.dayOfWeek, NameFormat.FULL)
            )
            LabeledValue("BS day of year", nepaliDay.dayOfYear.toString())
            LabeledValue("BS era", nepaliDay.era.toString())
            LabeledValue("AD date", englishDay.formatted())
            LabeledValue("AD day of year", englishDay.dayOfYear.toString())
            LabeledValue("AD week of year", englishDay.weekOfYear.toString())
        }

        DemoSection(
            "Parsing a compact string",
            "NepaliCalendarModel.parse and parseEnglish read the eight-digit yyyyMMdd form a query " +
                    "string or a stored key comes in, and return null for text that is not a date. " +
                    "A syntactically valid day that does not exist comes back with its month length " +
                    "as -1, so \"not a date\" and \"not a real day\" stay tellable apart."
        ) {
            var typed by rememberSaveable {
                mutableStateOf("${today.year}${today.month.padded()}${today.dayOfMonth.padded()}")
            }
            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("yyyyMMdd") },
                singleLine = true
            )
            val parsed = calendarModel.parse(typed)
            LabeledValue("parse (BS)", parsed?.formatted() ?: "not a date")
            LabeledValue(
                "Days in its month",
                parsed?.totalDaysInMonth?.takeIf { it > 0 }?.toString() ?: "not a real day"
            )
            LabeledValue(
                "parseEnglish (AD)",
                calendarModel.parseEnglish(typed)?.formatted() ?: "not a date"
            )
        }

        DemoSection(
            "Month arithmetic",
            "plusNepaliMonths and minusNepaliMonths move a month at a time rather than a day at a " +
                    "time, which is what paging a grid needs: the day would have nowhere to land in " +
                    "a shorter month, so only the month travels."
        ) {
            listOf(1, 3, 6, 12).forEach { step ->
                val ahead = calendarModel.plusNepaliMonths(nepaliMonth, step)
                LabeledValue(
                    "+$step months",
                    "${ahead.year}/${ahead.month}, ${ahead.totalDaysInMonth} days"
                )
            }
            listOf(1, 6).forEach { step ->
                val back = calendarModel.minusNepaliMonths(nepaliMonth, step)
                LabeledValue(
                    "-$step months",
                    "${back.year}/${back.month}, ${back.totalDaysInMonth} days"
                )
            }
            Text(
                text = "A model built with a locale formats in that locale, so an app can hold one " +
                        "model rather than passing a locale into every call. Stepping past either " +
                        "end of the supported range is an error rather than a clamp, so the buttons " +
                        "above stop themselves there.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** A month or day number as the two digits the compact parse format expects. */
private fun Int.padded(): String = toString().padStart(2, '0')
