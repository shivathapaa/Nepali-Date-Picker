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
 * Parse and format [SimpleTime] as the library's canonical time-of-day wire string,
 * `HH:mm:ss` with an optional nine-digit fractional part.
 *
 * This is the shape a [SimpleTime] takes on the wire everywhere the library is published. The
 * optional `nepali-date-picker-serialization` artifact's `SimpleTimeSerializer` emits and accepts
 * exactly these strings, so a Kotlin backend, a Swift client and a JavaScript client agree on the
 * payload without any of them depending on `kotlinx-serialization`.
 *
 * Times are always read as `Asia/Kathmandu`, the zone every [SimpleTime] in this library is
 * anchored to. No zone offset is written or accepted.
 *
 * For display output (12-hour clocks, Devanagari digits, locale-aware wording) use
 * `NepaliDateConverter.getFormattedTimeInEnglish`, `getFormattedTimeInNepali`, or
 * `NepaliDateConverter.formatTimeByUnicodePattern`. This formatter is for persistence and
 * transport, and is fixed to Latin digits and 24-hour form on purpose.
 *
 * @see NepaliDateFormatter for the matching `YYYY-MM-DD` date wire form.
 */
@Immutable
object NepaliTimeFormatter {

    private val HourRange = 0..23
    private val MinuteRange = 0..59
    private val SecondRange = 0..59
    private val NanosecondRange = 0..999_999_999

    private const val ClockFieldCount = 3
    private const val ClockFieldWidth = 2
    private const val NanosecondWidth = 9
    private const val ClockDelimiter = ':'
    private const val FractionDelimiter = '.'

    /**
     * Format [time] as `HH:mm:ss`, appending `.nnnnnnnnn` only when [SimpleTime.nanosecond] is
     * non-zero, so whole-second payloads stay compact.
     *
     * Examples: `"09:30:00"`, `"23:59:59.123456789"`, `"00:00:00.000000007"`.
     *
     * No range check, pass any [SimpleTime] and the result reflects it. A value outside the
     * ranges [parse] accepts will not round-trip.
     */
    fun format(time: SimpleTime): String {
        val hour = time.hour.toString().padStart(ClockFieldWidth, '0')
        val minute = time.minute.toString().padStart(ClockFieldWidth, '0')
        val second = time.second.toString().padStart(ClockFieldWidth, '0')
        val clock = "$hour$ClockDelimiter$minute$ClockDelimiter$second"
        if (time.nanosecond == 0) return clock
        return "$clock$FractionDelimiter${time.nanosecond.toString().padStart(NanosecondWidth, '0')}"
    }

    /**
     * Parse [input] as `HH:mm:ss` or `HH:mm:ss.nnnnnnnnn`, returning `null` when it is not a time
     * this formatter would have produced.
     *
     * Returns `null` when:
     * - the clock part does not hold exactly three `:`-separated fields,
     * - any field or the fractional part is empty or holds anything but digits, a sign included,
     * - hour is outside `0..23`, or minute or second is outside `0..59`,
     * - the fractional part is outside `0..999999999`.
     *
     * The fractional part is read as a plain count of nanoseconds, so `".7"` means seven
     * nanoseconds, not seven tenths of a second. Round-tripping through [format] returns the
     * zero-padded nine-digit form.
     *
     * Field widths are not enforced, so `"9:30:00"` parses as `09:30:00`. Any Unicode decimal
     * digit is accepted, so Devanagari input such as `"०९:३०:००"` reads the same as its Latin
     * form, and the two may be mixed. [format] always writes Latin digits. Leading and trailing
     * whitespace is not trimmed, call `String.trim()` first when the input may carry any.
     */
    fun parse(input: String): SimpleTime? {
        val fractionIndex = input.indexOf(FractionDelimiter)
        val clock = if (fractionIndex == -1) input else input.substring(0, fractionIndex)
        val fraction = if (fractionIndex == -1) "0" else input.substring(fractionIndex + 1)

        val fields = clock.split(ClockDelimiter)
        if (fields.size != ClockFieldCount) return null

        val hour = fields[0].toDigitsOrNull() ?: return null
        val minute = fields[1].toDigitsOrNull() ?: return null
        val second = fields[2].toDigitsOrNull() ?: return null
        val nanosecond = fraction.toDigitsOrNull() ?: return null

        if (hour !in HourRange) return null
        if (minute !in MinuteRange) return null
        if (second !in SecondRange) return null
        if (nanosecond !in NanosecondRange) return null

        return SimpleTime(hour, minute, second, nanosecond)
    }

    /**
     * This field's value, or `null` when it is empty or carries anything but digits. A sign is not
     * a digit, so `"+09"` reads as nothing. Every Unicode decimal digit counts.
     */
    private fun String.toDigitsOrNull(): Int? {
        if (isEmpty() || any { !it.isDigit() }) return null
        return toIntOrNull()
    }
}
