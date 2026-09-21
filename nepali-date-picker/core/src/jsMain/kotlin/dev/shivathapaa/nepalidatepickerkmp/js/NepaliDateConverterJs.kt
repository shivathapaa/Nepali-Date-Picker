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

package dev.shivathapaa.nepalidatepickerkmp.js

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.CustomDateTime
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime

/**
 * JavaScript / TypeScript facade over the shared Bikram Sambat conversion engine.
 *
 * This file is the only JS-specific glue in `:core`. Every calendar rule, conversion table, and
 * formatting routine still lives in `commonMain` and is exercised identically here, so the npm
 * package (`@nepali-date-picker/core`) and the JVM / native / Compose artifacts always agree.
 *
 * The wrapper deliberately trades Kotlin idioms for JS ones: enums are passed as plain lowercase
 * strings, results are flat exported classes rather than Kotlin data classes, and there are no
 * default parameters (JavaScript has no named arguments, so every parameter is explicit). Consumers
 * import named functions, e.g. `import { convertAdToBs } from "@nepali-date-picker/core"`.
 */

/**
 * A fully described calendar date in either the Bikram Sambat or Gregorian system.
 *
 * Mirrors the shared `CustomCalendar`. [era] is `1` for AD (Gregorian) and `2` for BS (Bikram
 * Sambat). Weekday and month indices are 1-based (1 = Sunday, 1 = Baisakh / January). Fields that
 * a producing function does not compute are `-1`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NepaliDate internal constructor(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val era: Int,
    val firstDayOfMonth: Int,
    val lastDayOfMonth: Int,
    val totalDaysInMonth: Int,
    val dayOfWeekInMonth: Int,
    val dayOfWeek: Int,
    val dayOfYear: Int,
    val weekOfMonth: Int,
    val weekOfYear: Int
)

/**
 * Month-level metadata used to lay out a Bikram Sambat month grid.
 *
 * Mirrors the shared `NepaliMonthCalendar`. [daysFromStartOfWeekToFirstOfMonth] is the number of
 * empty leading cells (Sunday-based) before day 1 when rendering a calendar.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NepaliMonthInfo internal constructor(
    val year: Int,
    val month: Int,
    val totalDaysInMonth: Int,
    val firstDayOfMonth: Int,
    val lastDayOfMonth: Int,
    val daysFromStartOfWeekToFirstOfMonth: Int
)

/**
 * A time of day in the `Asia/Kathmandu` zone. Mirrors the shared `SimpleTime`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NepaliTime internal constructor(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nanosecond: Int
)

/**
 * A [NepaliDate] paired with a [NepaliTime]. Mirrors the shared `CustomDateTime`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NepaliDateTime internal constructor(
    val calendar: NepaliDate,
    val time: NepaliTime
)

/**
 * An inclusive year range. Mirrors a Kotlin `IntRange`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class YearRange internal constructor(val first: Int, val last: Int)

private fun CustomCalendar.toJs(): NepaliDate = NepaliDate(
    year = year,
    month = month,
    dayOfMonth = dayOfMonth,
    era = era,
    firstDayOfMonth = firstDayOfMonth,
    lastDayOfMonth = lastDayOfMonth,
    totalDaysInMonth = totalDaysInMonth,
    dayOfWeekInMonth = dayOfWeekInMonth,
    dayOfWeek = dayOfWeek,
    dayOfYear = dayOfYear,
    weekOfMonth = weekOfMonth,
    weekOfYear = weekOfYear
)

private fun NepaliMonthCalendar.toJs(): NepaliMonthInfo = NepaliMonthInfo(
    year = year,
    month = month,
    totalDaysInMonth = totalDaysInMonth,
    firstDayOfMonth = firstDayOfMonth,
    lastDayOfMonth = lastDayOfMonth,
    daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth
)

private fun MonthCalendar.toJs(): NepaliMonthInfo = NepaliMonthInfo(
    year = year,
    month = month,
    totalDaysInMonth = totalDaysInMonth,
    firstDayOfMonth = firstDayOfMonth,
    lastDayOfMonth = lastDayOfMonth,
    daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth
)

private fun SimpleTime.toJs(): NepaliTime = NepaliTime(hour, minute, second, nanosecond)

private fun CustomDateTime.toJs(): NepaliDateTime = NepaliDateTime(customCalendar.toJs(), simpleTime.toJs())

private fun lang(value: String): NepaliDatePickerLang = when (value.lowercase()) {
    "ne", "nepali", "np" -> NepaliDatePickerLang.NEPALI
    else -> NepaliDatePickerLang.ENGLISH
}

private fun nameFormat(value: String): NameFormat = when (value.lowercase()) {
    "short" -> NameFormat.SHORT
    "medium" -> NameFormat.MEDIUM
    else -> NameFormat.FULL
}

private fun dateFormatStyle(value: String): NepaliDateFormatStyle = when (value.lowercase()) {
    "full" -> NepaliDateFormatStyle.FULL
    "long" -> NepaliDateFormatStyle.LONG
    "medium" -> NepaliDateFormatStyle.MEDIUM
    "short_mdy", "short-mdy" -> NepaliDateFormatStyle.SHORT_MDY
    "short_ymd", "short-ymd" -> NepaliDateFormatStyle.SHORT_YMD
    "compact_mdy", "compact-mdy" -> NepaliDateFormatStyle.COMPACT_MDY
    "compact_ymd", "compact-ymd" -> NepaliDateFormatStyle.COMPACT_YMD
    else -> NepaliDateFormatStyle.LONG
}

internal fun digitScript(value: String?): DigitScript? = when (value?.lowercase()) {
    null, "", "auto" -> null
    "latin", "english", "en" -> DigitScript.LATIN
    "devanagari", "nepali", "ne" -> DigitScript.DEVANAGARI
    else -> null
}

private fun locale(
    language: String,
    dateFormat: String,
    weekDayName: String,
    monthName: String,
    digitScript: String?
): NepaliDateLocale = NepaliDateLocale(
    language = lang(language),
    dateFormat = dateFormatStyle(dateFormat),
    weekDayName = nameFormat(weekDayName),
    monthName = nameFormat(monthName),
    digitScript = digitScript(digitScript)
)

/** Inclusive supported Bikram Sambat year range. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getBsYearRange(): YearRange =
    YearRange(NepaliCalendarDefaults.NepaliYearRange.first, NepaliCalendarDefaults.NepaliYearRange.last)

/** Inclusive supported Gregorian year range. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdYearRange(): YearRange =
    YearRange(NepaliCalendarDefaults.EnglishYearRange.first, NepaliCalendarDefaults.EnglishYearRange.last)

/** Today in the Bikram Sambat calendar, in the `Asia/Kathmandu` zone. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getTodayBs(): NepaliDate = NepaliDateConverter.todayNepaliCalendar.toJs()

/** Today in the Gregorian calendar, in the `Asia/Kathmandu` zone. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getTodayAd(): NepaliDate = NepaliDateConverter.todayEnglishCalendar.toJs()

/** The current wall-clock time in the `Asia/Kathmandu` zone. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getCurrentTime(): NepaliTime = NepaliDateConverter.currentTime.toJs()

/** Convert a Gregorian (AD) date to its Bikram Sambat (BS) equivalent. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun convertAdToBs(year: Int, month: Int, dayOfMonth: Int): NepaliDate =
    NepaliDateConverter.convertEnglishToNepali(year, month, dayOfMonth).toJs()

/** Convert a Bikram Sambat (BS) date to its Gregorian (AD) equivalent. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun convertBsToAd(year: Int, month: Int, dayOfMonth: Int): NepaliDate =
    NepaliDateConverter.convertNepaliToEnglish(year, month, dayOfMonth).toJs()

/** The fully described Bikram Sambat calendar for a BS year / month / day. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getBsCalendar(year: Int, month: Int, dayOfMonth: Int): NepaliDate =
    NepaliDateConverter.getNepaliCalendar(year, month, dayOfMonth).toJs()

/** The fully described Gregorian calendar for an AD year / month / day. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdCalendar(year: Int, month: Int, dayOfMonth: Int): NepaliDate =
    NepaliDateConverter.getEnglishCalendar(year, month, dayOfMonth).toJs()

/** Month-grid metadata for a Bikram Sambat year / month. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getBsMonth(year: Int, month: Int): NepaliMonthInfo =
    NepaliDateConverter.getNepaliMonthCalendar(year, month).toJs()

/** Month-grid metadata for a Gregorian year / month. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdMonth(year: Int, month: Int): NepaliMonthInfo =
    NepaliDateConverter.getEnglishMonthCalendar(year, month).toJs()

/**
 * Every day of a Gregorian month as its Bikram Sambat equivalent, in day order.
 *
 * Converts the whole month in one pass, so prefer this over calling [convertAdToBs] per day when
 * rendering a Gregorian grid that also shows Bikram Sambat dates. Days before the conversion anchor
 * (AD 1913-04-13) come back as `null`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getBsCalendarsInAdMonth(year: Int, month: Int): Array<NepaliDate?> =
    NepaliDateConverter.getNepaliCalendarsInEnglishMonth(year, month)
        .map { it?.toJs() }
        .toTypedArray()

/**
 * Every day of a Bikram Sambat month as its Gregorian equivalent, in day order.
 *
 * The mirror of [getBsCalendarsInAdMonth], and likewise a single conversion pass.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdCalendarsInBsMonth(year: Int, month: Int): Array<NepaliDate> =
    NepaliDateConverter.getEnglishCalendarsInNepaliMonth(year, month)
        .map { it.toJs() }
        .toTypedArray()

/**
 * Whether a Gregorian date has a Bikram Sambat equivalent.
 *
 * [getAdYearRange] alone is not a sufficient check: the calendars start mid-year relative to each
 * other, so 1913-01-01 through 1913-04-12 sit inside the year range yet cannot be converted.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun isAdDateConvertible(year: Int, month: Int, dayOfMonth: Int): Boolean =
    NepaliDateConverter.isEnglishDateConvertible(year, month, dayOfMonth)

/**
 * The Gregorian years a Gregorian-first calendar should offer for a Bikram Sambat year range, so
 * both calendars cover the same span of real days.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdYearRangeForBsYears(first: Int, last: Int): YearRange =
    NepaliCalendarDefaults.gregorianYearRangeFor(IntRange(first, last))
        .let { YearRange(it.first, it.last) }

/** Total days in a Bikram Sambat month. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getTotalDaysInBsMonth(year: Int, month: Int): Int =
    NepaliDateConverter.getTotalDaysInNepaliMonth(year, month)

/** Total days in a Gregorian month. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getTotalDaysInAdMonth(year: Int, month: Int): Int =
    NepaliDateConverter.getTotalDaysInEnglishMonth(year, month)

/** Add (or subtract, with a negative value) days to a Bikram Sambat date. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun addDaysToBsDate(year: Int, month: Int, dayOfMonth: Int, daysToAdjust: Int): NepaliDate =
    NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(year, month, dayOfMonth, daysToAdjust).toJs()

/**
 * Days between two Bikram Sambat dates (end exclusive). Add 1 to include the end date.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getBsDaysBetween(
    startYear: Int, startMonth: Int, startDay: Int,
    endYear: Int, endMonth: Int, endDay: Int
): Int = NepaliDateConverter.getNepaliDaysInBetween(
    SimpleDate(startYear, startMonth, startDay),
    SimpleDate(endYear, endMonth, endDay)
)

/**
 * Days between two Gregorian dates (end exclusive). Add 1 to include the end date.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdDaysBetween(
    startYear: Int, startMonth: Int, startDay: Int,
    endYear: Int, endMonth: Int, endDay: Int
): Int = NepaliDateConverter.getEnglishDaysInBetween(
    SimpleDate(startYear, startMonth, startDay),
    SimpleDate(endYear, endMonth, endDay)
)

/**
 * Compare two Bikram Sambat dates. Negative if the first is earlier, `0` if equal, positive if later.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun compareBsDates(
    year1: Int, month1: Int, day1: Int,
    year2: Int, month2: Int, day2: Int
): Int = NepaliDateConverter.compareDates(SimpleDate(year1, month1, day1), year2, month2, day2)

/**
 * Weekday name (1 = Sunday) in the requested [format] (`short` / `medium` / `full`) and [language]
 * (`en` / `ne`).
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getWeekdayName(dayOfWeek: Int, format: String, language: String): String =
    NepaliDateConverter.getWeekdayName(dayOfWeek, nameFormat(format), lang(language))

/** Bikram Sambat month name (1 = Baisakh) in the requested [format] and [language]. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getBsMonthName(month: Int, format: String, language: String): String =
    NepaliDateConverter.getMonthName(month, nameFormat(format), lang(language))

/** Gregorian month name (1 = January) in the requested [format] and [language]. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun getAdMonthName(month: Int, format: String, language: String): String =
    NepaliDateConverter.getEnglishMonthName(month, nameFormat(format), lang(language))

/**
 * Format a Bikram Sambat date with a locale preset.
 *
 * @param dateFormat one of `full`, `long`, `medium`, `short_mdy`, `short_ymd`, `compact_mdy`, `compact_ymd`.
 * @param digitScript `latin`, `devanagari`, or `null`/`auto` to follow [language].
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatBsDate(
    year: Int, month: Int, dayOfMonth: Int, dayOfWeek: Int,
    language: String, dateFormat: String, weekDayName: String, monthName: String, digitScript: String?
): String = NepaliDateConverter.formatNepaliDate(
    year, month, dayOfMonth, dayOfWeek,
    locale(language, dateFormat, weekDayName, monthName, digitScript)
)

/** Format a Gregorian date with a locale preset. See [formatBsDate] for parameter meanings. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatAdDate(
    year: Int, month: Int, dayOfMonth: Int, dayOfWeek: Int,
    language: String, dateFormat: String, weekDayName: String, monthName: String, digitScript: String?
): String = NepaliDateConverter.formatEnglishDate(
    year, month, dayOfMonth, dayOfWeek,
    locale(language, dateFormat, weekDayName, monthName, digitScript)
)

/**
 * Format a Bikram Sambat date with a Unicode-style pattern (e.g. `"yyyy-MM-dd EEEE"`). The full
 * calendar for the date is resolved internally, so day-of-year (`D`) and week-of-year (`w`) work.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatBsDateByPattern(pattern: String, year: Int, month: Int, dayOfMonth: Int, language: String): String =
    NepaliDateConverter.formatNepaliDateByUnicodePattern(
        pattern,
        NepaliDateConverter.getNepaliCalendar(year, month, dayOfMonth),
        lang(language)
    )

/**
 * Format a Gregorian date with a Unicode-style pattern. The full Gregorian calendar is resolved
 * internally via a round-trip conversion, so day-of-year (`D`) and week-of-year (`w`) work.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatAdDateByPattern(pattern: String, year: Int, month: Int, dayOfMonth: Int, language: String): String {
    val bs = NepaliDateConverter.convertEnglishToNepali(year, month, dayOfMonth)
    val ad = NepaliDateConverter.convertNepaliToEnglish(bs.year, bs.month, bs.dayOfMonth)
    return NepaliDateConverter.formatEnglishDateByUnicodePattern(pattern, ad, lang(language))
}

/** Format a time of day in English, 12-hour (e.g. `"4:30 PM"`) or 24-hour (e.g. `"16:30"`). */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatTimeEnglish(hour: Int, minute: Int, second: Int, nanosecond: Int, use12HourFormat: Boolean): String =
    NepaliDateConverter.getFormattedTimeInEnglish(SimpleTime(hour, minute, second, nanosecond), use12HourFormat)

/** Format a time of day in Nepali (e.g. `"दिउँसो ४ : ३०"`). */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatTimeNepali(hour: Int, minute: Int, second: Int, nanosecond: Int, use12HourFormat: Boolean): String =
    NepaliDateConverter.getFormattedTimeInNepali(SimpleTime(hour, minute, second, nanosecond), use12HourFormat)

/** Convert a Bikram Sambat date and time to an ISO 8601 UTC timestamp (e.g. `"2024-09-09T09:00:15Z"`). */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun bsDateTimeToIso(
    year: Int, month: Int, dayOfMonth: Int,
    hour: Int, minute: Int, second: Int, nanosecond: Int
): String = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(
    SimpleDate(year, month, dayOfMonth),
    SimpleTime(hour, minute, second, nanosecond)
)

/** Convert a Gregorian date and Nepali time to an ISO 8601 UTC timestamp. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun adDateTimeToIso(
    year: Int, month: Int, dayOfMonth: Int,
    hour: Int, minute: Int, second: Int, nanosecond: Int
): String = NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(
    SimpleDate(year, month, dayOfMonth),
    SimpleTime(hour, minute, second, nanosecond)
)

/** Parse an ISO 8601 timestamp into a Bikram Sambat date and Nepali time. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun bsDateTimeFromIso(isoDateTime: String): NepaliDateTime =
    NepaliDateConverter.getNepaliDateTimeFromIsoFormat(isoDateTime).toJs()

/** Parse an ISO 8601 timestamp into a Gregorian date and Nepali time. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun adDateTimeFromIso(isoDateTime: String): NepaliDateTime =
    NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime).toJs()

/**
 * Localize the ASCII digits in [text] to a numeral [script] (`latin` / `devanagari`). Non-digit
 * characters pass through unchanged.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun localizeDigits(text: String, script: String): String =
    with(NepaliDateConverter) { text.localizeDigits(digitScript(script) ?: DigitScript.LATIN) }

/** Convert digits in any supported non-Latin script back to ASCII `0-9`. */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun toLatinDigits(text: String): String = with(NepaliDateConverter) { text.toLatinDigits() }
