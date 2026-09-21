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

import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliTimeFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime

/**
 * Fixed-pattern date and time text, the shape the library persists and transmits.
 *
 * `formatBsDateText` / `parseBsDateText` cover the `YYYY-MM-DD` date form and the three other
 * text-field patterns; `formatTimeOfDay` / `parseTimeOfDay` cover the `HH:mm:ss` time form. All
 * four sit on the same `:core` primitives (`NepaliDateFormatter`, `NepaliTimeFormatter`) that the
 * Kotlin, Android and Swift builds use, and on the same strings the optional
 * `nepali-date-picker-serialization` artifact writes, so a payload produced by a Kotlin backend
 * reads identically here.
 *
 * These are not the timestamp helpers. `bsDateTimeToIso` and `bsDateTimeFromIso` deal in absolute
 * UTC instants such as `"2024-09-09T09:00:15Z"`; the functions here deal in a calendar date or a
 * wall-clock time on its own, with no zone and no conversion.
 */

/**
 * The year, month and day recovered by [parseBsDateText], with no calendar lookup performed.
 *
 * Only the three fields the text carried. Pass them to `getBsCalendar` for the full [NepaliDate]
 * with weekday, week-of-year and month-length metadata filled in.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NepaliDateParts internal constructor(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
)

/**
 * Map a lowercase pattern name onto the fixed-width text patterns `:core` supports.
 *
 * Unrecognized names fall back to `YYYY-MM-DD`, the canonical wire form.
 */
private fun datePattern(value: String): NepaliDateFormatter.Pattern = when (value.lowercase()) {
    "yyyy/mm/dd" -> NepaliDateFormatter.Pattern.YYYY_SLASH_MM_SLASH_DD
    "dd/mm/yyyy" -> NepaliDateFormatter.Pattern.DD_SLASH_MM_SLASH_YYYY
    "dd-mm-yyyy" -> NepaliDateFormatter.Pattern.DD_DASH_MM_DASH_YYYY
    else -> NepaliDateFormatter.Pattern.YYYY_DASH_MM_DASH_DD
}

/**
 * Format a Bikram Sambat date as fixed-width text, zero-padded to ten characters.
 *
 * @param pattern one of `yyyy-mm-dd` (the default and the canonical wire form), `yyyy/mm/dd`,
 *   `dd/mm/yyyy`, `dd-mm-yyyy`. Anything else is read as `yyyy-mm-dd`.
 * @param digitScript `latin` (the default), or `devanagari` to write Devanagari numerals. Pass
 *   `null` or `auto` for `latin`. Only `latin` is valid on the wire.
 *
 * No range check against the calendar; any year, month and day are formatted as given.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatBsDateText(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    pattern: String,
    digitScript: String?
): String = NepaliDateFormatter.format(
    SimpleDate(year, month, dayOfMonth),
    datePattern(pattern),
    digitScript(digitScript) ?: DigitScript.LATIN
)

/**
 * Parse fixed-width date text back into its year, month and day, returning `null` when [text] is
 * not that pattern.
 *
 * Latin and Devanagari numerals are both accepted. Returns `null` when the length is not ten
 * characters, a delimiter is in the wrong place, a digit slot holds something else, the month is
 * outside `1..12`, or the day is outside `1..32`. Day 32 is allowed because some Bikram Sambat
 * months run that long; checking the day against the real month length is the caller's job, via
 * `getTotalDaysInBsMonth`.
 *
 * @param pattern as in [formatBsDateText].
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun parseBsDateText(text: String, pattern: String): NepaliDateParts? {
    val date = NepaliDateFormatter.parse(text, datePattern(pattern)) ?: return null
    return NepaliDateParts(date.year, date.month, date.dayOfMonth)
}

/**
 * Format a time of day as `HH:mm:ss`, gaining a `.nnnnnnnnn` fractional part only when
 * [nanosecond] is non-zero.
 *
 * Examples: `"09:30:00"`, `"23:59:59.123456789"`. Always Latin digits and 24-hour form. For
 * display output use `formatTimeEnglish` or `formatTimeNepali` instead.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun formatTimeOfDay(hour: Int, minute: Int, second: Int, nanosecond: Int): String =
    NepaliTimeFormatter.format(SimpleTime(hour, minute, second, nanosecond))

/**
 * Parse `HH:mm:ss` or `HH:mm:ss.nnnnnnnnn` text into a time of day, returning `null` when [text]
 * is not one of those.
 *
 * Returns `null` when the clock part does not hold three `:`-separated fields, a field is empty or
 * holds anything but digits, the hour is outside `0..23`, a minute or second is outside `0..59`, or
 * the fractional part is outside `0..999999999`. Field widths are not enforced, so `"9:30:00"` reads
 * as `09:30:00`. Any Unicode decimal digit is accepted, so Devanagari input reads the same as Latin.
 *
 * The fractional part counts nanoseconds, so `".7"` is seven nanoseconds, not seven tenths of a
 * second.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun parseTimeOfDay(text: String): NepaliTime? {
    val time = NepaliTimeFormatter.parse(text) ?: return null
    return NepaliTime(time.hour, time.minute, time.second, time.nanosecond)
}
