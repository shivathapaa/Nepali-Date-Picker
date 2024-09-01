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

import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.englishMonths

@Immutable
object NepaliDateConverter {
    private val calendarModel = NepaliCalendarModel()

    /**
     * @return [CustomCalendar] with today's date in Nepali calendar
     */
    val todayNepaliDate
        get() = calendarModel.today

    /**
     * This function converts english date to nepali date.
     *
     * @param englishYYYY year in english calendar which takes value between [NepaliDatePickerDefaults.EnglishYearRange]
     * @param englishMM month in english calendar which takes value between 1 to 12
     * @param englishDD day in english calendar which takes value between 1 to 31
     */
    fun convertEnglishToNepali(englishYYYY: Int, englishMM: Int, englishDD: Int): CustomCalendar {
        return calendarModel.convertToNepaliCalendar(
            englishYYYY = englishYYYY, englishMM = englishMM, englishDD = englishDD
        )
    }

    /**
     * This function converts nepali date to english date.
     *
     * @param nepaliYYYY year in nepali calendar which takes value between [NepaliDatePickerDefaults.NepaliYearRange]
     * @param nepaliMM month in nepali calendar which takes value between 1 to 12
     * @param nepaliDD day in nepali calendar which takes value between 1 to 32
     */
    fun convertNepaliToEnglish(nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int): CustomCalendar {
        return calendarModel.convertToEnglishDate(
            nepaliYYYY = nepaliYYYY, nepaliMM = nepaliMM, nepaliDD = nepaliDD
        )
    }

    /**
     * This function returns total days in a Nepali month.
     *
     * @param year takes value between [NepaliDatePickerDefaults.NepaliYearRange]
     * @param month takes value between 1 to 12
     *
     * @return total days in a month
     */
    fun getTotalDaysInNepaliMonth(year: Int, month: Int): Int {
        return calendarModel.getTotalDaysInNepaliMonth(year, month)
    }

    /**
     * This function gives particular Nepali month details in Nepali calendar.
     *
     * @param nepaliYear takes value between [NepaliDatePickerDefaults.NepaliYearRange]
     * @param nepaliMonth takes value between 1 to 12
     *
     * @return [NepaliMonthCalendar] using [nepaliYear] and [nepaliMonth]
     */
    fun getNepaliMonthCalendar(
        nepaliYear: Int, nepaliMonth: Int
    ): NepaliMonthCalendar {
        return calendarModel.calculateFirstAndLastDayOfNepaliMonth(nepaliYear, nepaliMonth)
    }

    /**
     * Allows selection of dates before a given date.
     *
     * @param simpleDate The date before which selection is allowed.
     * @param includeDate Whether to include the given date in the selectable dates. (if [SimpleDate] is of Today then whether to include today or not)
     */
    fun BeforeDateSelectable(
        simpleDate: SimpleDate,
        includeDate: Boolean = false
    ): NepaliSelectableDates =
        object : NepaliSelectableDates {
            override fun isSelectableDate(customCalendar: CustomCalendar): Boolean {
                return if (includeDate) {
                    compareDates(
                        customCalendar,
                        simpleDate.year,
                        simpleDate.month,
                        simpleDate.dayOfMonth
                    ) <= 0
                } else {
                    compareDates(
                        customCalendar,
                        simpleDate.year,
                        simpleDate.month,
                        simpleDate.dayOfMonth
                    ) < 0
                }
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= simpleDate.year
            }
        }

    /**
     * Allows selection of dates after a given date.
     *
     * @param simpleDate The date after which selection is allowed.
     * @param includeDate Whether to include the given date in the selectable dates. (if [SimpleDate] is of Today then whether to include today or not)
     */
    fun AfterDateSelectable(
        simpleDate: SimpleDate,
        includeDate: Boolean = false
    ): NepaliSelectableDates =
        object : NepaliSelectableDates {
            override fun isSelectableDate(customCalendar: CustomCalendar): Boolean {
                return if (includeDate) {
                    compareDates(
                        customCalendar,
                        simpleDate.year,
                        simpleDate.month,
                        simpleDate.dayOfMonth
                    ) >= 0
                } else {
                    compareDates(
                        customCalendar,
                        simpleDate.year,
                        simpleDate.month,
                        simpleDate.dayOfMonth
                    ) > 0
                }
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= simpleDate.year
            }
        }

    /**
     * Creates a [NepaliSelectableDates] object that represents a selectable date range.
     *
     * Remember, [minDate] and [maxDate] should make sense i.e., [minDate] should be less than or equal to [maxDate]
     *
     * @param minDate The minimum selectable [SimpleDate] date.
     * @param maxDate The maximum selectable [SimpleDate] date.
     * @param includeMinDate Whether the `minDate` is included in the selectable range. (if [minDate] is of Today then whether to include today or not)
     * @param includeMaxDate Whether the `maxDate` is included in the selectable range. (if [maxDate] is of Today then whether to include today or not)
     *
     * @return A [NepaliSelectableDates] object that allows selecting dates within the specified range.
     */
    fun DateRangeSelectable(
        minDate: SimpleDate,
        maxDate: SimpleDate,
        includeMinDate: Boolean = false,
        includeMaxDate: Boolean = false
    ): NepaliSelectableDates =
        object : NepaliSelectableDates {
            override fun isSelectableDate(customCalendar: CustomCalendar): Boolean {
                val compareMin = compareDates(
                    customCalendar,
                    minDate.year,
                    minDate.month,
                    minDate.dayOfMonth
                )
                val compareMax = compareDates(
                    customCalendar,
                    maxDate.year,
                    maxDate.month,
                    maxDate.dayOfMonth
                )

                return when {
                    includeMinDate && includeMaxDate -> compareMin >= 0 && compareMax <= 0
                    includeMinDate && !includeMaxDate -> compareMin >= 0 && compareMax < 0
                    !includeMinDate && includeMaxDate -> compareMin > 0 && compareMax <= 0
                    else -> compareMin > 0 && compareMax < 0
                }
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in minDate.year..maxDate.year
            }
        }

    /**
     * Compares a [CustomCalendar] instance with a date represented by the given year, month, and dayOfMonth.
     *
     * Returns a negative integer if the [calendar] is before the given date, zero if they are equal,
     * and a positive integer if the [calendar] is after the given date.
     *
     * @param calendar The [CustomCalendar] instance to compare.
     * @param year The year of the date to compare with.
     * @param month The month of the date to compare with.
     * @param dayOfMonth The day of the month of the date to compare with.
     * @return A negative integer, zero, or a positive integer as described above.
     */
    private fun compareDates(
        calendar: CustomCalendar,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): Int {
        return when {
            calendar.year != year -> calendar.year - year
            calendar.month != month -> calendar.month - month
            else -> calendar.dayOfMonth - dayOfMonth
        }
    }

    // Two overload functions [compareDates] can be simplified to one using an interface but,
    // I don't want complexity in outer level. Also, these functions are unlikely to change overtime.

    /**
     * Compares a [CustomCalendar] instance with a date represented by the given [SimpleDate].
     *
     * Returns a negative integer if the [dateToCompareFrom] is before the [dateToCompareTo], zero if they are equal,
     * and a positive integer if the [dateToCompareFrom] is after the [dateToCompareTo].
     *
     * @param dateToCompareFrom The [CustomCalendar] instance to compare.
     * @param dateToCompareTo The [SimpleDate] representing the date to compare to.
     * @return A negative integer, zero, or a positive integer as described above.
     */
    fun compareDates(dateToCompareFrom: CustomCalendar, dateToCompareTo: SimpleDate): Int {
        return compareDates(
            dateToCompareFrom,
            dateToCompareTo.year,
            dateToCompareFrom.month,
            dateToCompareFrom.dayOfMonth
        )
    }

    /**
     * Overloaded function of above [compareDates] function.
     *
     * Compares two [CustomCalendar] instances to determine their chronological order.
     *
     * Returns a negative integer if [dateToCompareFrom] is before [dateToCompareTo], zero if they are equal,
     * and a positive integer if [dateToCompareFrom] is after [dateToCompareTo].
     *
     * @param dateToCompareFrom The first date in the comparison.
     * @param dateToCompareTo The second date, which [dateToCompareFrom] is compared to.
     * @return A negative integer, zero, or a positive integer as described above.
     */
    fun compareDates(
        dateToCompareFrom: CustomCalendar,
        dateToCompareTo: CustomCalendar
    ): Int {
        return compareDates(
            dateToCompareFrom,
            dateToCompareTo.year,
            dateToCompareFrom.month,
            dateToCompareFrom.dayOfMonth
        )
    }

    /**
     * @param dayOfWeek takes value between 1 to 7.
     * @param format gives name of day either in short or medium or full name. i.e. Sunday, Mon, T, आईतबार, आईत, आ, etc.
     * @param language gives name of days in english or nepali. i.e. Sunday, Monday, etc. or आईतबार, सोमबार, etc.
     */
    fun getWeekdayName(
        dayOfWeek: Int,
        format: NameFormat = NameFormat.FULL,
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
    ): String {
        if (dayOfWeek !in 1..7) {
            throw IllegalArgumentException("Invalid dayOfWeek value:$dayOfWeek. Must be between 1 and 7.")
        }

        val weekdays = language.weekdays[dayOfWeek - 1]
        return when (format) {
            NameFormat.SHORT -> weekdays.short
            NameFormat.MEDIUM -> weekdays.medium
            NameFormat.FULL -> weekdays.full
        }
    }

    /**
     * Formats a Nepali date based on the specified user preferences. (With date validation)
     *
     * @param customCalendar The [CustomCalendar] containing the year, month, day, and weekday.
     * @param locale The [NepaliDateLocale] specifying the user's preferred language,date format,
     * weekday name format, and month name format.
     * @return A string representing the formatted date according to the user's preferences.
     *
     * The function supports the following date format styles:
     * - FULL: Monday, Asar 21, 2024  or,  सोमबार, असार २१, २०२४
     * - LONG: Asar 21, 2024  or,  असार २१, २०२४
     * - MEDIUM: 2024 Asar 21  or,  २०२४ असार २१
     * - SHORT_MDY: 06/21/2024  or,  ०६/२१/२०२४
     * - SHORT_YMD: 2024/06/21  or,  २०२४/०६/२१
     * - COMPACT_MDY: 06/21/24  or,  ०६/२१/२४
     * - COMPACT_YMD: 24/06/21  or,  २४/०६/२१
     *
     * The function uses the specified weekday and month name formats to match the preferred
     * language and format style.
     *
     * Example usage:
     * ```
     * val customCalendar = CustomCalendar(year = 2080, month = 3, day = 15, weekday = 2, ....)
     * val locale = NepaliDateLocale(
     *     language = NepaliDatePickerLang.ENGLISH,
     *     dateFormat = NepaliDateFormatStyle.FULL,
     *     weekDayName = NameFormat.FULL,
     *     monthName = NameFormat.FULL
     * )
     * val formattedDate = formatNepaliDate(customCalendar, locale)
     * // formattedDate: "Tuesday, Asar 15, 2080"
     * ```
     */
    fun formatNepaliDate(customCalendar: CustomCalendar, locale: NepaliDateLocale): String {
        return calendarModel.formatNepaliDate(customCalendar, locale)
    }

    /**
     * Overload function of above [formatNepaliDate] function without date validation.
     * Formats a Nepali date based on the specified user preferences. (Without validation)
     *
     * @param year takes an integer value of year.
     * @param month takes an integer value of month which takes value between 1 to 12.
     * @param dayOfMonth takes an integer value of day of month which takes value between 1 to 32.
     * @param dayOfWeek takes an integer value of day of week which takes value between 1 to 7.
     * @param locale The [NepaliDateLocale] specifying the user's preferred language,date format,
     * weekday name format, and month name format.
     * @return A string representing the formatted date according to the user's preferences.
     *
     */
    fun formatNepaliDate(
        year: Int,
        @IntRange(from = 1, to = 12) month: Int,
        @IntRange(from = 1, to = 32) dayOfMonth: Int,
        @IntRange(from = 1, to = 7) dayOfWeek: Int,
        locale: NepaliDateLocale
    ): String {
        return calendarModel.formatNepaliDate(year, month, dayOfMonth, dayOfWeek, locale)
    }

    /**
     * @param month takes value between 1 to 12.
     * @param format gives name of month either in short or full name.
     * @param language gives name of `Nepali` months in english or nepali.
     *
     * [NameFormat.MEDIUM] & [NameFormat.FULL] gives full name of month i.e. January, February, etc.
     * [NameFormat.SHORT] gives short name of month i.e. Jan, Feb, etc.]
     *
     * Use [NepaliDatePickerLang.ENGLISH] for english l
     */
    fun getMonthName(
        month: Int,
        format: NameFormat = NameFormat.FULL,
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
    ): String {
        return calendarModel.getNepaliMonthName(
            monthOfYear = month, format = format, language = language
        )
    }

    /**
     * @param month takes value between 1 to 12.
     * @param format gives name of `English` month either in short or full name.
     *
     * [NameFormat.MEDIUM] & [NameFormat.FULL] gives full name of month i.e. January, February, etc.
     * [NameFormat.SHORT] gives short name of month i.e. Jan, Feb, etc.
     */
    fun getEnglishMonthName(
        month: Int,
        format: NameFormat = NameFormat.FULL,
    ): String {
        if (month !in 1..12) {
            throw IllegalArgumentException("Invalid monthOfYear value: $month. Must be between 1 and 12.")
        }

        val monthIndex = englishMonths[month - 1]

        return when (format) {
            NameFormat.SHORT -> monthIndex.short
            NameFormat.MEDIUM -> monthIndex.full
            NameFormat.FULL -> monthIndex.full
        }
    }

    /**
     * This function localizes [String] to either English or Nepali, i.e.
     * converts english numbers of string to nepali numbers, and vice versa.
     *
     * @param locale takes value from [NepaliDatePickerLang] i.e. [NepaliDatePickerLang.ENGLISH] or [NepaliDatePickerLang.NEPALI]
     *
     * @return [String] with English numbers converted to Nepali numbers, or vice versa.
     */
    fun String.localizeNumber(locale: NepaliDatePickerLang): String =
        calendarModel.localizeNumber(stringToLocalize = this, locale = locale)

    /**
     * This function converts [String] to Nepali, i.e. converts english numbers of string to nepali numbers.
     */
    fun String.convertToNepaliNumber(): String =
        calendarModel.localizeNumbersToNepali(englishString = this)

    /**
     * This function converts [String] to English, i.e. converts nepali numbers of string to english numbers.
     */
    fun String.convertToEnglishNumber(): String =
        calendarModel.localizeNumberToEnglish(nepaliString = this)

}