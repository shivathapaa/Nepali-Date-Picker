/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package sample.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.holiday.HolidayEntry
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.NoOpHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.holiday.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.holiday.workingDaysBetween

/** The Compose-free core: conversions, formatting, time, ISO, working days, and digit scripts. */
@Composable
fun UtilitiesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val todayBs = remember { NepaliDateConverter.todayNepaliCalendar }
        val todayAd = remember { NepaliDateConverter.todayEnglishSimpleDate }
        val time = remember { NepaliDateConverter.currentTime }

        DemoSection("Today", "The current date in both calendars, plus the wall-clock time.") {
            LabeledValue("Bikram Sambat", todayBs.text())
            LabeledValue("Gregorian", todayAd.text())
            LabeledValue("Time", "${time.hour}:${time.minute}:${time.second}")
        }

        DemoSection("Conversions", "Convert freely between Bikram Sambat and Gregorian.") {
            val adToBs = remember { NepaliDateConverter.convertEnglishToNepali(2024, 3, 21) }
            val bsToAd = remember { NepaliDateConverter.convertNepaliToEnglish(2081, 1, 1) }
            LabeledValue("2024-03-21 AD", adToBs.text() + " BS")
            LabeledValue("2081-01-01 BS", bsToAd.text() + " AD")
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

        DemoSection("Formatting", "Render a date through the locale, in English and Nepali.") {
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
        }

        DemoSection("Time formatting", "12-hour and 24-hour clocks in English and Nepali.") {
            LabeledValue("English 12h", NepaliDateConverter.getFormattedTimeInEnglish(time, use12HourFormat = true))
            LabeledValue("English 24h", NepaliDateConverter.getFormattedTimeInEnglish(time, use12HourFormat = false))
            LabeledValue("Nepali 12h", NepaliDateConverter.getFormattedTimeInNepali(time, use12HourFormat = true))
            LabeledValue("Nepali 24h", NepaliDateConverter.getFormattedTimeInNepali(time, use12HourFormat = false))
        }

        DemoSection("ISO 8601", "Serialize a BS date and time to ISO, then parse it back.") {
            val iso = remember(todayBs, time) {
                NepaliDateConverter.formatNepaliDateTimeToIsoFormat(todayBs.toSimpleDate(), time)
            }
            val parsed = remember(iso) { NepaliDateConverter.getNepaliDateTimeFromIsoFormat(iso) }
            LabeledValue("ISO string", iso)
            LabeledValue("Parsed back (BS)", parsed.customCalendar.text())
        }

        DemoSection("Working days", "Weekend and holiday-aware arithmetic (Nepali single-day weekend).") {
            val holidays = remember(todayBs) {
                setOf(offsetDate(todayBs.toSimpleDate(), 2), offsetDate(todayBs.toSimpleDate(), 3))
            }
            val holidayProvider = remember(holidays) {
                object : NepaliHolidayProvider {
                    override fun holidays(year: Int): Set<HolidayEntry> = emptySet()
                    override fun isHoliday(date: SimpleDate): Boolean = date in holidays
                }
            }
            val from = todayBs.toSimpleDate()
            LabeledValue(
                "Working days over 30 days",
                NepaliDateConverter.workingDaysBetween(from, offsetDate(from, 30), NoOpHolidayProvider).toString()
            )
            LabeledValue("Next working day", NepaliDateConverter.nextWorkingDay(from, NoOpHolidayProvider).text())
            LabeledValue("+5 working days", NepaliDateConverter.addWorkingDays(from, 5, NoOpHolidayProvider).text())
            LabeledValue(
                "+5 working days, with holidays",
                NepaliDateConverter.addWorkingDays(from, 5, holidayProvider).text()
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
                        "2081 to Devanagari" to "2081".convertToNepaliNumber(),
                        "Explicit Devanagari" to "1234567890".localizeDigits(DigitScript.DEVANAGARI),
                        "२०८१ to Latin" to "२०८१".toLatinDigits()
                    )
                }
            }
            demos.forEach { (label, value) -> LabeledValue(label, value) }
        }
    }
}

private fun offsetDate(from: SimpleDate, days: Int): SimpleDate =
    NepaliDateConverter
        .getNepaliCalendarAfterAdditionOrSubtraction(from.year, from.month, from.dayOfMonth, days)
        .toSimpleDate()

private fun SimpleDate.text(): String = "$year/$month/$dayOfMonth"

private fun CustomCalendar.text(): String = "$year/$month/$dayOfMonth"
