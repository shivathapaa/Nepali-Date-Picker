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
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang

private val English = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
private val Nepali = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)

/**
 * Turning a date into text through a named shape. A format style says how the parts are ordered and
 * spelled, and the locale around it decides the language and the digit script, so one call reads
 * correctly in either language without the caller knowing how each one arranges itself.
 */
@Composable
fun FormattingShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val nepaliToday = remember { NepaliDateConverter.todayNepaliCalendar }
        val englishToday = remember { NepaliDateConverter.todayEnglishCalendar }

        DemoSection(
            "Every format style, both languages",
            "A style names a shape rather than a pattern, so the same value reads correctly in " +
                    "either language without the caller knowing how each one orders its parts."
        ) {
            NepaliDateFormatStyle.entries.forEach { style ->
                LabeledValue(
                    style.name,
                    NepaliDateConverter.formatNepaliDate(nepaliToday, English.copy(dateFormat = style))
                )
            }
            VerticalGap()
            NepaliDateFormatStyle.entries.forEach { style ->
                LabeledValue(
                    "${style.name} (नेपाली)",
                    NepaliDateConverter.formatNepaliDate(nepaliToday, Nepali.copy(dateFormat = style))
                )
            }
        }

        DemoSection(
            "The Gregorian date through the same styles",
            "formatEnglishDate is the Gregorian counterpart, and it takes the same locale, so an " +
                    "English-first screen keeps one formatting call and one set of preferences."
        ) {
            NepaliDateFormatStyle.entries.forEach { style ->
                LabeledValue(
                    style.name,
                    NepaliDateConverter.formatEnglishDate(englishToday, English.copy(dateFormat = style))
                )
            }
            VerticalGap()
            LabeledValue(
                "From loose parts",
                NepaliDateConverter.formatEnglishDate(
                    year = englishToday.year,
                    month = englishToday.month,
                    dayOfMonth = englishToday.dayOfMonth,
                    dayOfWeek = englishToday.dayOfWeek,
                    locale = English.copy(dateFormat = NepaliDateFormatStyle.FULL)
                )
            )
            LabeledValue(
                "In Nepali",
                NepaliDateConverter.formatEnglishDate(
                    englishToday,
                    Nepali.copy(dateFormat = NepaliDateFormatStyle.FULL)
                )
            )
        }

        DemoSection(
            "Names on their own",
            "Month and weekday names without a date around them, for a header row or a dropdown. " +
                    "The Bikram Sambat and Gregorian month lists are separate calls because they " +
                    "are separate months."
        ) {
            NameFormat.entries.forEach { format ->
                LabeledValue(
                    "BS month ${nepaliToday.month}, ${format.name}",
                    NepaliDateConverter.getMonthName(nepaliToday.month, format)
                )
            }
            VerticalGap()
            NameFormat.entries.forEach { format ->
                LabeledValue(
                    "AD month ${englishToday.month}, ${format.name}",
                    NepaliDateConverter.getEnglishMonthName(englishToday.month, format)
                )
            }
            VerticalGap()
            NameFormat.entries.forEach { format ->
                LabeledValue(
                    "Weekday ${nepaliToday.dayOfWeek}, ${format.name}",
                    NepaliDateConverter.getWeekdayName(nepaliToday.dayOfWeek, format)
                )
            }
            LabeledValue(
                "Weekday in Nepali",
                NepaliDateConverter.getWeekdayName(
                    nepaliToday.dayOfWeek,
                    NameFormat.FULL,
                    NepaliDatePickerLang.NEPALI
                )
            )
        }

        DemoSection(
            "Digit scripts",
            "Any numeric string can be moved between Latin and Devanagari. The locale overload reads " +
                    "the script the locale resolved, so UI code that already holds one need not " +
                    "decide again."
        ) {
            val sample = "1234567890"
            with(NepaliDateConverter) {
                LabeledValue("Explicit Devanagari", sample.localizeDigits(DigitScript.DEVANAGARI))
                LabeledValue("Explicit Latin", sample.localizeDigits(DigitScript.LATIN))
                LabeledValue("From a Nepali locale", sample.localizeDigits(Nepali))
                LabeledValue("From an English locale", sample.localizeDigits(English))
                LabeledValue("By language", sample.localizeNumber(NepaliDatePickerLang.NEPALI))
                LabeledValue("Back to Latin", "२०८१/०६/१२".toLatinDigits())
            }
        }
    }
}
