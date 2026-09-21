/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween

/** The Compose-free core: conversions, formatting, time, ISO, working days, and digit scripts. */
@Composable
fun UtilitiesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val todayBs = remember { NepaliDateConverter.todayNepaliCalendar }
        val todayAd = remember { NepaliDateConverter.todayEnglishSimpleDate }
        val time = remember { NepaliDateConverter.currentTime }

        DemoSection("Today", "The current date in both calendars, plus the wall-clock time.") {
            LabeledValue("Bikram Sambat", todayBs.formatted())
            LabeledValue("Gregorian", todayAd.formatted())
            LabeledValue("Time", "${time.hour}:${time.minute}:${time.second}")
        }

        DemoSection("Conversions", "Convert freely between Bikram Sambat and Gregorian.") {
            val adToBs = remember { NepaliDateConverter.convertEnglishToNepali(2024, 3, 21) }
            val bsToAd = remember { NepaliDateConverter.convertNepaliToEnglish(2081, 1, 1) }
            LabeledValue("2024-03-21 AD", adToBs.formatted() + " BS")
            LabeledValue("2081-01-01 BS", bsToAd.formatted() + " AD")
        }

        DemoSection("Spans and month lengths", "Day counts between dates and the length of a month.") {
            val bsDays = remember {
                NepaliDateConverter.getNepaliDaysInBetween(SimpleDate(2081, 1, 1), SimpleDate(2081, 12, 30))
            }
            val adDays = remember {
                NepaliDateConverter.getEnglishDaysInBetween(SimpleDate(2024, 1, 1), SimpleDate(2024, 12, 31))
            }
            LabeledValue("Days in BS 2081", bsDays.toString())
            LabeledValue("Days in AD 2024", adDays.toString())
            LabeledValue("Days in BS 2081-01", NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 1).toString())
            LabeledValue("Days in AD 2024-02", NepaliDateConverter.getTotalDaysInEnglishMonth(2024, 2).toString())
        }

        DemoSection(
            "Formatting",
            "Render a date through the locale, in English and Nepali. The Formatting screen takes " +
                    "every style, the Unicode patterns and the clocks one at a time."
        ) {
            val english = remember(todayBs) {
                NepaliDateConverter.formatNepaliDate(todayBs, NepaliDateLocale(dateFormat = NepaliDateFormatStyle.FULL))
            }
            val nepali = remember(todayBs) {
                NepaliDateConverter.formatNepaliDate(
                    todayBs,
                    NepaliDateLocale(language = NepaliDatePickerLang.NEPALI, dateFormat = NepaliDateFormatStyle.FULL)
                )
            }
            LabeledValue("English", english)
            LabeledValue("Nepali", nepali)
            LabeledValue("Time", NepaliDateConverter.getFormattedTimeInEnglish(time))
        }

        DemoSection("Working days", "Weekend and holiday-aware arithmetic (Nepali single-day weekend).") {
            val holidays = remember(todayBs) {
                setOf(offsetDate(todayBs.toSimpleDate(), 2), offsetDate(todayBs.toSimpleDate(), 3))
            }
            val holidayProvider = remember(holidays) {
                object : NepaliEventProvider {
                    override fun events(year: Int): Set<NepaliCalendarEvent> = emptySet()
                    override fun closesOn(date: SimpleDate): Boolean = date in holidays
                }
            }
            val from = todayBs.toSimpleDate()
            LabeledValue(
                "Working days over 30 days",
                NepaliDateConverter.workingDaysBetween(from, offsetDate(from, 30), NoOpEventProvider).toString()
            )
            LabeledValue("Next working day", NepaliDateConverter.nextWorkingDay(from, NoOpEventProvider).formatted())
            LabeledValue("+5 working days", NepaliDateConverter.addWorkingDays(from, 5, NoOpEventProvider).formatted())
            LabeledValue(
                "+5 working days, with holidays",
                NepaliDateConverter.addWorkingDays(from, 5, holidayProvider).formatted()
            )
        }

        DemoSection("Comparisons", "compareDates returns the sign of the difference.") {
            val later = remember(todayBs) { offsetDate(todayBs.toSimpleDate(), 10) }
            val cmp = remember(todayBs, later) { NepaliDateConverter.compareDates(todayBs, later) }
            LabeledValue("today vs +10 days", "$cmp (${if (cmp < 0) "earlier" else "later or equal"})")
        }

        DemoSection("Digit scripts", "Localize any numeric string between Latin and Devanagari.") {
            val demos = remember {
                with(NepaliDateConverter) {
                    listOf(
                        "2081 to Devanagari" to "2081".localizeDigits(DigitScript.DEVANAGARI),
                        "A whole date" to "2081/06/12".localizeDigits(DigitScript.DEVANAGARI),
                        "२०८१ to Latin" to "२०८१".toLatinDigits()
                    )
                }
            }
            demos.forEach { (label, value) -> LabeledValue(label, value) }
        }
    }
}

