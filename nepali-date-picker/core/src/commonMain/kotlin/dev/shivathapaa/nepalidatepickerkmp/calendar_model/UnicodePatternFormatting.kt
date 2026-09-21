/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthName
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.data.defaultDigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.nepaliDayPeriod

/**
 * The tokens [NepaliCalendarModel]'s `formatXByUnicodePattern` functions recognise in a pattern,
 * what each one expands to, and the matcher that finds them.
 */

/** Every token [dateFormatReplacements] produces, in the order the alternation prefers them. */
internal val DateFormatTokens = listOf(
    "yyyy", "yy", "MMMM", "MMM", "MM", "M", "dd", "d", "D",
    "EEEEE", "EEEE", "E", "ee", "e", "w"
)

/** Every token [timeFormatReplacements] produces. */
internal val TimeFormatTokens = listOf(
    "HH", "H", "hh", "h", "mm", "m", "ss", "s",
    "SSSS", "SSS", "SS", "S", "a", "A"
)

internal val dateTokenRegex: Regex = tokenRegex(DateFormatTokens)
internal val timeTokenRegex: Regex = tokenRegex(TimeFormatTokens)
internal val dateTimeTokenRegex: Regex = tokenRegex(DateFormatTokens + TimeFormatTokens)

/**
 * Longest token first, so `yyyy` wins over `yy` and `EEEE` over `E`. Regex alternation takes the
 * leftmost branch that matches, which makes the sort the precedence rule.
 */
private fun tokenRegex(tokens: List<String>): Regex =
    Regex(
        tokens.sortedByDescending { it.length }
            .joinToString(separator = "|") { Regex.escape(it) }
    )

/**
 * Replaces every token [tokenRegex] finds in [unicodePattern] with its entry in [replacements].
 *
 * Anything the regex does not match is literal, and a pattern is read once rather than rescanned,
 * so a replacement that happens to contain a token is left alone.
 */
internal fun replaceUnicodePatternTokens(
    unicodePattern: String,
    replacements: Map<String, String>,
    tokenRegex: Regex
): String = tokenRegex.replace(unicodePattern) { matchResult ->
    replacements[matchResult.value] ?: matchResult.value
}

/**
 * What each of [TimeFormatTokens] stands for at [time], with digits in [language]'s own script.
 *
 * `a` and `A` give the day period in lower and upper case; in Nepali both give the same word.
 */
internal fun timeFormatReplacements(
    time: SimpleTime,
    language: NepaliDatePickerLang
): Map<String, String> {
    val hour = time.hour
    val hour24 = hour.toString()
    val hour12 = when {
        hour == 0 -> "12"
        hour > 12 -> (hour - 12).toString()
        else -> hour.toString()
    }
    val hour242Digit = hour.toString().padStart(2, '0')
    val hour122Digit = hour12.padStart(2, '0')

    val minute = time.minute.toString()
    val minute2Digit = time.minute.toString().padStart(2, '0')
    val second = time.second.toString()
    val second2Digit = time.second.toString().padStart(2, '0')

    val nanoStr = time.nanosecond.toString().take(1)
    val nanoStr2Digit = time.nanosecond.toString().padStart(2, '0').take(2)
    val nanoStr3Digit = time.nanosecond.toString().padStart(3, '0').take(3)
    val nanoStr4Digit = time.nanosecond.toString().padStart(4, '0').take(4)

    val amPm = when (language) {
        NepaliDatePickerLang.NEPALI -> nepaliDayPeriod(hour)
        else -> if (hour < 12) "AM" else "PM"
    }

    val amPmLowerCase = when (language) {
        NepaliDatePickerLang.NEPALI -> amPm
        else -> amPm.lowercase()
    }

    val digits = language.defaultDigitScript()
    return mapOf(
        "HH" to digits.localize(hour242Digit),
        "H" to digits.localize(hour24),
        "hh" to digits.localize(hour122Digit),
        "h" to digits.localize(hour12),
        "mm" to digits.localize(minute2Digit),
        "m" to digits.localize(minute),
        "ss" to digits.localize(second2Digit),
        "s" to digits.localize(second),
        "SSSS" to digits.localize(nanoStr4Digit),
        "SSS" to digits.localize(nanoStr3Digit),
        "SS" to digits.localize(nanoStr2Digit),
        "S" to digits.localize(nanoStr),
        "a" to amPmLowerCase,
        "A" to amPm
    )
}

/**
 * What each of [DateFormatTokens] stands for at [calendar], with digits in [language]'s own script.
 *
 * [onGetMonthNames] is called with a zero-based month and supplies the name list to read, which is
 * what lets one set of tokens serve either calendar.
 */
internal fun dateFormatReplacements(
    calendar: CustomCalendar,
    language: NepaliDatePickerLang,
    onGetMonthNames: (Int) -> NepaliMonthName
): Map<String, String> = with(calendar) {
    val yearStr = year.toString()
    val shortYear = yearStr.takeLast(2)
    val monthStr = month.toString()
    val monthStr2Digit = month.toString().padStart(2, '0')
    val dayStr = dayOfMonth.toString()
    val dayStr2Digit = dayOfMonth.toString().padStart(2, '0')
    val weekday = language.weekdays[dayOfWeek - 1]
    val weekdayStr = dayOfWeek.toString()
    val weekdayStr2Digit = weekdayStr.padStart(2, '0')
    val weekOfTheYear = weekOfYear.toString()
    val dayOfTheYear = dayOfYear.toString()

    val monthName = onGetMonthNames(month - 1)

    val digits = language.defaultDigitScript()
    mapOf(
        "yyyy" to digits.localize(yearStr),
        "yy" to digits.localize(shortYear),
        "MMMM" to monthName.full,
        "MMM" to monthName.short,
        "MM" to digits.localize(monthStr2Digit),
        "M" to digits.localize(monthStr),
        "dd" to digits.localize(dayStr2Digit),
        "d" to digits.localize(dayStr),
        "D" to digits.localize(dayOfTheYear),
        "EEEEE" to weekday.short,
        "EEEE" to weekday.full,
        "E" to weekday.medium,
        "ee" to digits.localize(weekdayStr2Digit),
        "e" to digits.localize(weekdayStr),
        "w" to digits.localize(weekOfTheYear)
    )
}
