/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
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

import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Immutable
internal class NepaliCalendarModel(val locale: NepaliDateLocale = NepaliDateLocale()) {

    val today
        get(): CustomCalendar {
            return getNepaliDateInstance()
        }

    private fun getNepaliDateInstance(): CustomCalendar {
        val now = Clock.System.todayIn(TimeZone.of("Asia/Kathmandu"))
        return DateConverters.convertToNepaliCalendar(
            englishYYYY = now.year, englishMM = now.monthNumber, englishDD = now.dayOfMonth
        )
    }

    fun convertToNepaliCalendar(englishYYYY: Int, englishMM: Int, englishDD: Int): CustomCalendar {
        return DateConverters.convertToNepaliCalendar(
            englishYYYY = englishYYYY, englishMM = englishMM, englishDD = englishDD
        )
    }

    fun convertToEnglishDate(nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int): CustomCalendar {
        return DateConverters.convertToEnglishDate(
            nepaliYYYY = nepaliYYYY, nepaliMM = nepaliMM, nepaliDD = nepaliDD
        )
    }

    fun getTotalDaysInNepaliMonth(year: Int, month: Int): Int {
        return DateConverters.getTotalDaysInNepaliMonth(year, month)
    }

    fun calculateFirstAndLastDayOfNepaliMonth(
        nepaliYear: Int, nepaliMonth: Int
    ): NepaliMonthCalendar {
        return DateConverters.calculateNepaliMonthDetails(nepaliYear, nepaliMonth)
    }

    fun getNepaliMonth(nepaliYear: Int, nepaliMonth: Int): NepaliMonthCalendar {
        return DateConverters.getNepaliMonth(
            nepaliYear = nepaliYear, nepaliMonth = nepaliMonth
        )
    }

    fun getNepaliMonth(simpleNepaliDate: SimpleDate): CustomCalendar {
        return DateConverters.getNepaliMonth(simpleNepaliDate = simpleNepaliDate)
    }

    // Function to add months to a CustomCalendar
    fun plusNepaliMonths(
        fromNepaliCalendar: NepaliMonthCalendar, addedMonthsCount: Int
    ): NepaliMonthCalendar {
        return DateConverters.getNepaliMonth(
            nepaliYear = fromNepaliCalendar.year,
            nepaliMonth = fromNepaliCalendar.month,
            addedMonthsCount = addedMonthsCount
        )
    }

    // Function to subtract months from a CustomCalendar
    fun minusNepaliMonths(
        fromNepaliCalendar: NepaliMonthCalendar, subtractedMonthsCount: Int
    ): NepaliMonthCalendar {
        return DateConverters.getNepaliMonth(
            nepaliYear = fromNepaliCalendar.year,
            nepaliMonth = fromNepaliCalendar.month,
            addedMonthsCount = -subtractedMonthsCount
        )
    }

    /**
     * Formats a Nepali date based on the specified user preferences.
     *
     * @param customCalendar The [CustomCalendar] containing the year, month, day, and weekday.
     * @param locale The [NepaliDateLocale] specifying the user's preferred language,date format,
     * weekday name format, and month name format.
     * @return A string representing the formatted date according to the user's preferences.
     *
     * The function supports the following date format styles:
     * - FULL: Monday, Asar 21, 2024
     * - LONG: Asar 21, 2024
     * - MEDIUM: 2024 Asar 21
     * - SHORT_MDY: 06/21/2024
     * - SHORT_YMD: 2024/06/21
     * - COMPACT_MDY: 06/21/24
     * - COMPACT_YMD: 24/06/21
     *
     * The function uses the specified weekday and month name formats to match the user's
     * preferred language and format style.
     *
     * Example usage:
     * ```
     * val nepaliDate = CustomCalendar(year = 2080, month = 3, day = 15, weekday = 2, ....)
     * val locale = NepaliDateLocale(
     *     language = NepaliDatePickerLang.ENGLISH,
     *     dateFormat = NepaliDateFormatStyle.FULL,
     *     weekDayName = NameFormat.FULL,
     *     monthName = NameFormat.FULL
     * )
     * val formattedDate = formatNepaliDate(nepaliDate, locale)
     * // formattedDate: "Monday, Asar 15, 2080"
     * ```
     */
    fun formatNepaliDate(customCalendar: CustomCalendar, locale: NepaliDateLocale): String {
        validateNepaliDate(customCalendar.year, customCalendar.month, customCalendar.dayOfMonth)

        val showMonthName = locale.dateFormat in listOf(
            NepaliDateFormatStyle.FULL,
            NepaliDateFormatStyle.LONG,
            NepaliDateFormatStyle.MEDIUM
        )

        val weekday = locale.language.weekdays[customCalendar.dayOfWeek - 1]
        val month = locale.language.months[customCalendar.month - 1]

        val weekdayName = when (locale.weekDayName) {
            NameFormat.FULL -> weekday.full
            NameFormat.MEDIUM -> weekday.medium
            NameFormat.SHORT -> weekday.short
        }

        val monthName = when (locale.monthName) {
            NameFormat.FULL, NameFormat.MEDIUM -> month.full
            NameFormat.SHORT -> month.short
        }

        val day = localizeNumber(
            if (showMonthName) customCalendar.dayOfMonth.toString()
            else customCalendar.dayOfMonth.toString().padStart(2, '0'),
            locale.language
        )

        val monthNum =
            localizeNumber(customCalendar.month.toString().padStart(2, '0'), locale.language)
        val year = localizeNumber(customCalendar.year.toString(), locale.language)
        val shortYear = year.takeLast(2)

        return when (locale.dateFormat) {
            NepaliDateFormatStyle.FULL -> "$weekdayName, $monthName $day, $year"
            NepaliDateFormatStyle.LONG -> "$monthName $day, $year"
            NepaliDateFormatStyle.MEDIUM -> "$year $monthName $day"
            NepaliDateFormatStyle.SHORT_MDY -> "$monthNum/$day/$year"
            NepaliDateFormatStyle.SHORT_YMD -> "$year/$monthNum/$day"
            NepaliDateFormatStyle.COMPACT_MDY -> "$monthNum/$day/$shortYear"
            NepaliDateFormatStyle.COMPACT_YMD -> "$shortYear/$monthNum/$day"
        }
    }

    private fun validateNepaliDate(year: Int, month: Int, day: Int) {
        if (month !in 1..12) {
            throw IllegalArgumentException("Invalid month value: $month. Must be between 1 and 12.")
        }

        val maxDaysInMonth = getTotalDaysInNepaliMonth(year, month)
        if (day !in 1..maxDaysInMonth) {
            throw IllegalArgumentException("Invalid day value: $day. Must be between 1 and $maxDaysInMonth for month $month.")
        }
    }

    fun getNepaliMonthName(
        monthOfYear: Int, format: NameFormat, language: NepaliDatePickerLang = locale.language
    ): String {
        if (monthOfYear !in 1..12) {
            throw IllegalArgumentException("Invalid monthOfYear value: $monthOfYear. Must be between 1 and 12.")
        }
        val month = language.months[monthOfYear - 1]
        return when (format) {
            NameFormat.SHORT -> month.short
            NameFormat.MEDIUM -> month.full
            NameFormat.FULL -> month.full
        }
    }

    fun localizeNumber(stringToLocalize: String, locale: NepaliDatePickerLang): String =
        if (locale == NepaliDatePickerLang.ENGLISH) stringToLocalize else stringToLocalize.convertToNepaliNumber()

    fun localizeNumbersToNepali(englishString: String): String =
        englishString.convertToNepaliNumber()

    fun localizeNumberToEnglish(nepaliString: String): String =
        nepaliString.convertToEnglishNumber()

    private val nepaliDigits = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    private fun String.convertToNepaliNumber(): String {
        val builder = StringBuilder(length)
        for (char in this) {
            builder.append(if (char in '0'..'9') nepaliDigits[char - '0'] else char)
        }
        return builder.toString()
    }

    private val nepaliToEnglishDigits = mapOf(
        '०' to '0',
        '१' to '1',
        '२' to '2',
        '३' to '3',
        '४' to '4',
        '५' to '5',
        '६' to '6',
        '७' to '7',
        '८' to '8',
        '९' to '9'
    )

    private fun String.convertToEnglishNumber(): String {
        val builder = StringBuilder(length)
        for (char in this) {
            builder.append(nepaliToEnglishDigits[char] ?: char)
        }
        return builder.toString()
    }
}