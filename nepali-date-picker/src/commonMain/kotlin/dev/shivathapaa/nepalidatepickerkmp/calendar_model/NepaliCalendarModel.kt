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
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.data.englishMonths
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Immutable
internal class NepaliCalendarModel(val locale: NepaliDateLocale = NepaliDateLocale()) {
    private val timeZone = TimeZone.of(zoneId = "Asia/Kathmandu")
    private val localEnglishDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)

    val today
        get(): CustomCalendar {
            return getNepaliDateInstance()
        }

    val todayEnglish
        get(): SimpleDate = SimpleDate(
            year = localEnglishDateTime.year,
            month = localEnglishDateTime.monthNumber,
            dayOfMonth = localEnglishDateTime.dayOfMonth
        )

    val currentTime
        get(): SimpleTime {
            val nowTime: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)

            return SimpleTime(
                hour = nowTime.hour,
                minute = nowTime.minute,
                second = nowTime.second,
                nanosecond = nowTime.nanosecond
            )
        }

    private fun getNepaliDateInstance(): CustomCalendar {
        return DateConverters.convertToNepaliCalendar(
            englishYYYY = localEnglishDateTime.year,
            englishMM = localEnglishDateTime.monthNumber,
            englishDD = localEnglishDateTime.dayOfMonth
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

    fun getNepaliMonth(nepaliYear: Int, nepaliMonth: Int): NepaliMonthCalendar {
        return DateConverters.calculateNepaliMonthDetails(nepaliYear, nepaliMonth)
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

    fun nepaliDaysInBetween(startDate: SimpleDate, endDate: SimpleDate): Int {
        return DateConverters.nepaliDaysInBetween(startDate, endDate)
    }

    /**
     * Formats a Nepali date based on the specified user preferences. (Without validation)
     *
     * @param year takes an integer value of year.
     * @param month takes an integer value of month.
     * @param dayOfMonth takes an integer value of day.
     * @param dayOfWeek takes an integer value of day of week.
     * @param locale The [NepaliDateLocale] specifying the user's preferred language,date format,
     * weekday name format, and month name format.
     * @return A string representing the formatted date according to the user's preferences.
     *
     */
    fun formatNepaliDate(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        dayOfWeek: Int,
        locale: NepaliDateLocale
    ): String {
        val showMonthName = locale.dateFormat in listOf(
            NepaliDateFormatStyle.FULL,
            NepaliDateFormatStyle.LONG,
            NepaliDateFormatStyle.MEDIUM
        )

        val weekday = locale.language.weekdays[dayOfWeek - 1]
        val localizedMonth = locale.language.months[month - 1]

        val weekdayName = when (locale.weekDayName) {
            NameFormat.FULL -> weekday.full
            NameFormat.MEDIUM -> weekday.medium
            NameFormat.SHORT -> weekday.short
        }

        val monthName = when (locale.monthName) {
            NameFormat.SHORT -> localizedMonth.short
            else -> localizedMonth.full
        }

        val day = localizeNumber(
            if (showMonthName) dayOfMonth.toString()
            else dayOfMonth.toString().padStart(2, '0'),
            locale.language
        )

        val monthNum =
            localizeNumber(month.toString().padStart(2, '0'), locale.language)
        val localizedYear = localizeNumber(year.toString(), locale.language)
        val shortYear = localizedYear.takeLast(2)

        return when (locale.dateFormat) {
            NepaliDateFormatStyle.FULL -> "$weekdayName, $monthName $day, $localizedYear"
            NepaliDateFormatStyle.LONG -> "$monthName $day, $localizedYear"
            NepaliDateFormatStyle.MEDIUM -> "$localizedYear $monthName $day"
            NepaliDateFormatStyle.SHORT_MDY -> "$monthNum/$day/$localizedYear"
            NepaliDateFormatStyle.SHORT_YMD -> "$localizedYear/$monthNum/$day"
            NepaliDateFormatStyle.COMPACT_MDY -> "$monthNum/$day/$shortYear"
            NepaliDateFormatStyle.COMPACT_YMD -> "$shortYear/$monthNum/$day"
        }
    }

    /**
     * Overload function of above [formatNepaliDate] function without date range validation.
     *
     * Formats a Nepali date based on the specified user preferences with date range validation.
     *
     * @param customCalendar The [CustomCalendar] containing the year, month, day, and weekday.
     * @param locale The [NepaliDateLocale] specifying the user's preferred language,date format,
     * weekday name format, and month name format.
     * @return A string representing the formatted date according to the user's preferences.
     *
     * The function supports the following date format styles:
     * - FULL: Monday, Asar 21, 2024 or, सोमबार, असार २१, २०७४
     * - LONG: Asar 21, 2024 or, असार २१, २०७४
     * - MEDIUM: 2024 Asar 21 or, २०७४ असार २१
     * - SHORT_MDY: 06/21/2024 or, ०६/२१/२०७४
     * - SHORT_YMD: 2024/06/21 or, २०७४/०६/२१
     * - COMPACT_MDY: 06/21/24 or, ०६/२१/२४
     * - COMPACT_YMD: 24/06/21 or, २४/०६/२१
     *
     * The function uses the specified weekday and month name formats to match the user's
     * preferred language and format style.
     *
     * Example usage:
     * ```
     * val nepaliDate = CustomCalendar(year = 2080, month = 3, dayOfMonth = 15, dayOfWeek = 2, ....)
     * val locale = NepaliDateLocale(
     *     language = NepaliDatePickerLang.ENGLISH,
     *     dateFormat = NepaliDateFormatStyle.FULL,
     *     weekDayName = NameFormat.FULL,
     *     monthName = NameFormat.FULL
     * )
     * val formattedDate = formatNepaliDate(nepaliDate, locale)
     * // formattedDate: "Monday, Asar 11, 2080"
     * ```
     */
    fun formatNepaliDate(customCalendar: CustomCalendar, locale: NepaliDateLocale): String {
        validateNepaliDate(customCalendar.year, customCalendar.month, customCalendar.dayOfMonth)

        return formatNepaliDate(
            year = customCalendar.year,
            month = customCalendar.month,
            dayOfMonth = customCalendar.dayOfMonth,
            dayOfWeek = customCalendar.dayOfWeek,
            locale = locale
        )
    }

    /**
     * Formats a English date based on the specified user preferences.
     *
     * @param year takes an integer value of year.
     * @param month takes an integer value of month.
     * @param dayOfMonth takes an integer value of day.
     * @param dayOfWeek takes an integer value of day of week.
     * @param locale The [NepaliDateLocale] specifying the user's preferred date format,
     * weekday name format, and month name format.
     *
     * Note: [NepaliDatePickerLang] will be dummy for this case.
     *
     * @return A string representing the formatted date according to the user's preferences.
     *
     * The function supports the following date format styles:
     * - FULL: Monday, March 21, 2024
     * - LONG: March 21, 2024
     * - MEDIUM: 2024 March 21
     * - SHORT_MDY: 06/21/2024
     * - SHORT_YMD: 2024/06/21
     * - COMPACT_MDY: 06/21/24
     * - COMPACT_YMD: 24/06/21
     *
     * The function uses the specified weekday and month name formats to match the user's
     * preferred format style.
     *
     * Example usage:
     * ```
     * val englishDate = CustomCalendar(year = 2024, month = 3, dayOfMonth = 11, dayOfWeek = 2, ....)
     * val locale = NepaliDateLocale(
     *     language = NepaliDatePickerLang.ENGLISH, // This is dummy, no need to pass for this case.
     *     dateFormat = NepaliDateFormatStyle.FULL,
     *     weekDayName = NameFormat.FULL,
     *     monthName = NameFormat.FULL
     * )
     * val formattedDate = formatEnglishDate(englishDate, locale)
     * // formattedDate: "Monday, March 11, 2024"
     * ```
     */
    fun formatEnglishDate(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        dayOfWeek: Int,
        locale: NepaliDateLocale
    ): String {
        val language = NepaliDatePickerLang.ENGLISH

        val showMonthName = locale.dateFormat in listOf(
            NepaliDateFormatStyle.FULL,
            NepaliDateFormatStyle.LONG,
            NepaliDateFormatStyle.MEDIUM
        )

        val weekday = language.weekdays[dayOfWeek - 1]
        val monthNames = englishMonths[month - 1]

        val weekdayName = when (locale.weekDayName) {
            NameFormat.FULL -> weekday.full
            NameFormat.MEDIUM -> weekday.medium
            NameFormat.SHORT -> weekday.short
        }

        val monthName = when (locale.monthName) {
            NameFormat.SHORT -> monthNames.short
            else -> monthNames.full
        }

        val day = if (showMonthName) dayOfMonth.toString()
        else dayOfMonth.toString().padStart(2, '0')

        val monthNum = month.toString().padStart(2, '0')
        val fullYear = year.toString()
        val shortYear = fullYear.takeLast(2)

        return when (locale.dateFormat) {
            NepaliDateFormatStyle.FULL -> "$weekdayName, $monthName $day, $fullYear"
            NepaliDateFormatStyle.LONG -> "$monthName $day, $fullYear"
            NepaliDateFormatStyle.MEDIUM -> "$fullYear $monthName $day"
            NepaliDateFormatStyle.SHORT_MDY -> "$monthNum/$day/$fullYear"
            NepaliDateFormatStyle.SHORT_YMD -> "$fullYear/$monthNum/$day"
            NepaliDateFormatStyle.COMPACT_MDY -> "$monthNum/$day/$shortYear"
            NepaliDateFormatStyle.COMPACT_YMD -> "$shortYear/$monthNum/$day"
        }
    }

    fun formatEnglishDateNepaliTimeToIsoFormat(englishDate: SimpleDate, time: SimpleTime): String {
        val localDateTime = LocalDateTime(
            englishDate.year,
            englishDate.month,
            englishDate.dayOfMonth,
            time.hour,
            time.minute,
            time.second,
            time.nanosecond
        )

        return localDateTime.toInstant(timeZone).toString()
    }

    fun formatNepaliDateTimeToIsoFormat(nepaliDate: SimpleDate, time: SimpleTime): String {
        val convertedEnglishDate =
            convertToEnglishDate(nepaliDate.year, nepaliDate.month, nepaliDate.dayOfMonth)

        val localDateTime = LocalDateTime(
            convertedEnglishDate.year,
            convertedEnglishDate.month,
            convertedEnglishDate.dayOfMonth,
            time.hour,
            time.minute,
            time.second,
            time.nanosecond
        )

        return localDateTime.toInstant(timeZone).toString()
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
            else -> month.full
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