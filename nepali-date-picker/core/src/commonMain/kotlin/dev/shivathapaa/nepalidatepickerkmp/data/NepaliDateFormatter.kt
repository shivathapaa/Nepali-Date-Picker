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

package dev.shivathapaa.nepalidatepickerkmp.data

import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable

/**
 * Parse and format [SimpleDate] for short numeric text-field input.
 *
 * Use this when you have a raw `YYYY/MM/DD`-style string from a `TextField` and need a
 * [SimpleDate] (or vice versa). For locale-aware long-form output ("Asar 21, 2082"),
 * use `NepaliDateConverter.formatNepaliDate(...)` instead - this formatter is the
 * primitive that backs `NepaliDateTextField` / `NepaliDateField`.
 *
 * Supported [Pattern]s are limited on purpose. A free-form `DateTimeFormatter`-style
 * DSL is out of scope; the constrained surface keeps the masking and validation in
 * the UI layer simple and predictable.
 */
@Immutable
object NepaliDateFormatter {

    /** Supported text-field input/output patterns. */
    enum class Pattern(val literal: String, val delimiter: Char, val yearFirst: Boolean) {
        /** `YYYY/MM/DD` - e.g. `2082/02/14`. */
        YYYY_SLASH_MM_SLASH_DD("YYYY/MM/DD", '/', yearFirst = true),

        /** `YYYY-MM-DD` - ISO-like, e.g. `2082-02-14`. */
        YYYY_DASH_MM_DASH_DD("YYYY-MM-DD", '-', yearFirst = true),

        /** `DD/MM/YYYY` - day-first, e.g. `14/02/2082`. */
        DD_SLASH_MM_SLASH_YYYY("DD/MM/YYYY", '/', yearFirst = false),

        /** `DD-MM-YYYY` - day-first dashed, e.g. `14-02-2082`. */
        DD_DASH_MM_DASH_YYYY("DD-MM-YYYY", '-', yearFirst = false);

        /** Total visible character count when the field is full (always 10). */
        val length: Int get() = literal.length

        /** Number of ASCII digit characters expected (always 8 - `YYYY` + `MM` + `DD`). */
        val digitCount: Int get() = 8
    }

    /**
     * Format [date] as a `Pattern.literal`-shaped string with digits in the given [script].
     *
     * No range or selectable-date checks - pass any [SimpleDate]; the result will reflect it.
     */
    fun format(date: SimpleDate, pattern: Pattern, script: DigitScript = DigitScript.LATIN): String {
        val year = date.year.toString().padStart(4, '0')
        val month = date.month.toString().padStart(2, '0')
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val raw = if (pattern.yearFirst) {
            "$year${pattern.delimiter}$month${pattern.delimiter}$day"
        } else {
            "$day${pattern.delimiter}$month${pattern.delimiter}$year"
        }
        return script.localize(raw)
    }

    /**
     * Parse [input] as the given [pattern]. Accepts Latin digits and any non-Latin
     * digit script registered in [DigitScript] (Devanagari today).
     *
     * Returns `null` when:
     * - length does not match `pattern.length`,
     * - any year/month/day token is not numeric,
     * - month is not in 1..12,
     * - dayOfMonth is not in 1..32 (32 is allowed because some BS months have 32 days;
     *   tighter "real" validation that hits the converter and reports total days in month
     *   is the caller's job - usually via `NepaliSelectableDates` + `NepaliCalendarModel`).
     *
     * Delimiters must match `pattern.delimiter` exactly. Trailing/leading whitespace is
     * NOT trimmed - text fields should call `String.trim()` first if they care.
     */
    fun parse(input: String, pattern: Pattern): SimpleDate? {
        if (input.length != pattern.length) return null

        val (delim1, delim2) = if (pattern.yearFirst) 4 to 7 else 2 to 5
        val normalized = normalizeDigits(input, delim1, delim2) ?: return null
        if (normalized[delim1] != pattern.delimiter || normalized[delim2] != pattern.delimiter) return null

        val (yearStr, monthStr, dayStr) = if (pattern.yearFirst) {
            Triple(normalized.substring(0, 4), normalized.substring(5, 7), normalized.substring(8, 10))
        } else {
            Triple(normalized.substring(6, 10), normalized.substring(3, 5), normalized.substring(0, 2))
        }

        val year = yearStr.toIntOrNull() ?: return null
        val month = monthStr.toIntOrNull() ?: return null
        val day = dayStr.toIntOrNull() ?: return null

        if (month !in 1..12) return null
        if (day !in 1..32) return null
        return SimpleDate(year, month, day)
    }

    /**
     * Convert any non-Latin digits in [input] to Latin in place, **only at the digit slots**.
     * Returns `null` if a slot that should hold a digit holds something else. Delimiter
     * positions ([delim1Index], [delim2Index]) are passed through verbatim.
     */
    private fun normalizeDigits(input: String, delim1Index: Int, delim2Index: Int): String? {
        val builder = StringBuilder(input.length)
        for ((i, char) in input.withIndex()) {
            if (i == delim1Index || i == delim2Index) {
                builder.append(char)
                continue
            }
            val latin = char.toLatinDigitOrSelf()
            if (latin !in '0'..'9') return null
            builder.append(latin)
        }
        return builder.toString()
    }
}

/** Latin digits pass through. Other supported scripts map to '0'..'9'. Non-digits return as-is. */
private fun Char.toLatinDigitOrSelf(): Char = latinDigitOrNull() ?: this
