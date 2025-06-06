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
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.CustomDateTime
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

@Immutable
object NepaliDateConverter {
    private val calendarModel = NepaliCalendarModel()

    /**
     * @return [CustomCalendar] with today's date in Nepali calendar
     */
    @Deprecated(
        message = "Use todayNepaliSimpleDate or todayNepaliCalendar instead",
        replaceWith = ReplaceWith(expression = "NepaliDateConverter.todayNepaliCalendar"),
        level = DeprecationLevel.ERROR
    )
    val todayNepaliDate
        get() = calendarModel.todayNepaliSimpleDate

    /**
     * @return [SimpleDate] with today's date in Nepali calendar
     */
    val todayNepaliSimpleDate: SimpleDate
        get() = calendarModel.todayNepaliSimpleDate

    /**
     * @return [CustomCalendar] with today's date in Nepali calendar
     */
    val todayNepaliCalendar: CustomCalendar
        get() = calendarModel.todayNepaliCalendar

    /**
     * Strictly adjusted to `Asia/Kathmandu` TimeZone.
     *
     * @return the current date in the English calendar as a [SimpleDate].
     */
    val todayEnglishSimpleDate: SimpleDate
        get() = calendarModel.todayEnglishSimpleDate

    /**
     * Strictly adjusted to `Asia/Kathmandu` TimeZone.
     *
     * @return the current date in the English calendar as a [CustomCalendar].
     */
    val todayEnglishCalendar: CustomCalendar
        get() = calendarModel.todayEnglishCalendar

    /**
     * Strictly adjusted to `Asia/Kathmandu` TimeZone.
     *
     * @return the current date in the English calendar as a [SimpleDate].
     */
    @Deprecated(
        "Use todayEnglishSimpleDate or todayEnglishCalendar instead",
        replaceWith = ReplaceWith(expression = "NepaliDateConverter.todayEnglishSimpleDate"),
        level = DeprecationLevel.ERROR
    )
    val todayEnglishDate: SimpleDate
        get() = calendarModel.todayEnglishSimpleDate

    /**
     * Gets the current time in Kathmandu.
     *
     * This property returns a [SimpleTime] object representing the current hour, minute, second,
     * and nanosecond in the `Asia/Kathmandu` time zone. The time is calculated fresh each time
     * this property is accessed, so you always get the most up-to-date time.
     *
     * Get formatted time string using [getFormattedTimeInNepali] and [getFormattedTimeInEnglish]
     *
     * @return A [SimpleTime] object representing the current time.
     */
    val currentTime: SimpleTime
        get() = calendarModel.currentTime

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
     * Returns the Nepali calendar representation for a specific Nepali date (year, month, day).
     * This function retrieves the `CustomCalendar` object that holds detailed information about
     * the Nepali date including the total days in the month, the day of the week, and other
     * necessary calendar data.
     *
     * @param nepaliYYYY The Nepali year (e.g., 2078).
     * @param nepaliMM The Nepali month (1-based index, i.e., 1 for Baisakh, 12 for Chaitra).
     * @param nepaliDD The day of the month (1-based index).
     * @return A `CustomCalendar` object containing calendar information for the specified Nepali date.
     * @throws IllegalArgumentException if the year, month, or day provided is invalid.
     */
    fun getNepaliCalendar(nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int): CustomCalendar {
        return calendarModel.getNepaliCalendar(SimpleDate(nepaliYYYY, nepaliMM, nepaliDD))
    }

    /**
     * Adjusts a given Nepali date by adding or subtracting number of days.
     *
     * This function calculates the resulting Nepali date after adjusting the provided year, month,
     * and day by a given number of days (positive or negative). It handles all months
     * and years calculations according to the day adjustment, ensuring correct calculation of Nepali
     * calendar.
     *
     * @param year The Nepali year (e.g., 2081).
     * @param month The Nepali month (1 to 12).
     * @param dayOfMonth The Nepali day of the month (1 to 32, depending on the month).
     * @param daysToAdjust The number of days to adjust by. Positive values will add days, and
     * negative values will subtract days. i.e, 15, -20, 366, -454,etc.
     * @return A [CustomCalendar] instance representing the adjusted Nepali date.
     *
     * @throws IllegalArgumentException if the provided date is invalid.
     *
     * Example usage:
     * ```
     * // Add 10 days to Nepali date 2081-03-15
     * val adjustedDate = getNepaliCalendarAfterAdditionOrSubtraction(2081, 3, 15, 10)
     * println(adjustedDate) // Output: CustomCalendar(year=2081, month=3, dayOfMonth=25, ...)
     *
     * // Subtract 5 days from Nepali date 2081-03-15
     * val adjustedDate = getNepaliCalendarAfterAdditionOrSubtraction(2081, 3, 15, -5)
     * println(adjustedDate) // Output: CustomCalendar(year=2081, month=3, dayOfMonth=10, ...)
     *
     * // Add 50 days, crossing over to the next month/year
     * val adjustedDate = getNepaliCalendarAfterAdditionOrSubtraction(2081, 11, 15, 50)
     * println(adjustedDate) // Output: CustomCalendar(year=2082, month=1, dayOfMonth=5, ...)
     * ```
     */
    fun getNepaliCalendarAfterAdditionOrSubtraction(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        daysToAdjust: Int
    ): CustomCalendar {
        return calendarModel.addOrSubtractDaysToSimpleDate(year, month, dayOfMonth, daysToAdjust)
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
        return calendarModel.getNepaliMonth(nepaliYear, nepaliMonth)
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
     * This function returns total days in a English month.
     *
     * @param year takes value between [NepaliDatePickerDefaults.EnglishYearRange]
     * @param month takes value between 1 to 12
     *
     * @return total days in a month
     */
    fun getTotalDaysInEnglishMonth(year: Int, month: Int): Int {
        return calendarModel.getTotalDaysInEnglishMonth(year, month)
    }

    /**
     * Calculates the total number of days between two [SimpleDate] objects in the Nepali calendar.
     *
     * The end date is not added. You can add 1 to its returned value to include the end date too.
     *
     * @param startDate The starting date.
     * @param endDate The ending date.
     * @return The number of days between the two dates. Throws exception if the year is not in
     * range [NepaliDatePickerDefaults.NepaliYearRange], or returns -1 if either date is invalid.
     */
    fun getNepaliDaysInBetween(startDate: SimpleDate, endDate: SimpleDate): Int {
        return calendarModel.nepaliDaysInBetween(startDate, endDate)
    }

    /**
     * Calculates the total number of days between two [SimpleDate] objects in the English calendar.
     *
     * The end date is not added. You can add 1 to its returned value to include the end date too.
     *
     * @param startDate The starting date.
     * @param endDate The ending date.
     * @return The number of days between the two dates.
     */
    fun getEnglishDaysInBetween(startDate: SimpleDate, endDate: SimpleDate): Int {
        val startLocalDate = LocalDate(startDate.year, startDate.month, startDate.dayOfMonth)
        val endLocalDate = LocalDate(endDate.year, endDate.month, endDate.dayOfMonth)

        return startLocalDate.daysUntil(endLocalDate)
    }

    /**
     * Formats a Nepali date based on the specified user preferences. (Without date range validation)
     *
     * @param year takes an integer value of year.
     * @param month takes an integer value of month which takes value between 1 to 12.
     * @param dayOfMonth takes an integer value of day of month which takes value between 1 to 32.
     * @param dayOfWeek takes an integer value of day of week which takes value between 1 to 7.
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
     * val locale = NepaliDateLocale(
     *     language = NepaliDatePickerLang.ENGLISH,
     *     dateFormat = NepaliDateFormatStyle.FULL,
     *     weekDayName = NameFormat.FULL,
     *     monthName = NameFormat.FULL
     * )
     * val formattedDate = formatNepaliDate(year = 2080, month = 3, dayOfMonth = 15, dayOfWeek = 2, locale)
     * // formattedDate: "Tuesday, Asar 15, 2080"
     * ```
     */
    fun formatNepaliDate(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        dayOfWeek: Int,
        locale: NepaliDateLocale
    ): String {
        return calendarModel.formatNepaliDate(year, month, dayOfMonth, dayOfWeek, locale)
    }

    /**
     * Overload function of above [formatNepaliDate] function without date range validation.
     *
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
     * val customCalendar = CustomCalendar(year = 2080, month = 3, dayOfMonth = 15, dayOfWeek = 2, ....)
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
     * Formats a English date based on the specified user preferences.
     *
     * @param year takes an integer value of year.
     * @param month takes an integer value of month.
     * @param dayOfMonth takes an integer value of day.
     * @param dayOfWeek takes an integer value of day of week.
     * @param locale The [NepaliDateLocale] specifying the user's preferred date format,
     * weekday name format, language, and month name format.
     *
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
     *     language = NepaliDatePickerLang.ENGLISH,
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
        return calendarModel.formatEnglishDate(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            dayOfWeek = dayOfWeek,
            locale = locale
        )
    }


    /**
     * Overload function of above [formatEnglishDate] function without date range validation.
     *
     * Formats a English date based on the specified user preferences.
     *
     * @param customCalendar The [CustomCalendar] containing the year, month, day, and weekday.
     * @param locale The [NepaliDateLocale] specifying the user's preferred language,date format,
     * weekday name format, language, and month name format.
     *
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
     *     language = NepaliDatePickerLang.ENGLISH,
     *     dateFormat = NepaliDateFormatStyle.FULL,
     *     weekDayName = NameFormat.FULL,
     *     monthName = NameFormat.FULL
     * )
     * val formattedDate = formatEnglishDate(englishDate, locale)
     * // formattedDate: "Monday, March 11, 2024"
     * ```
     */
    fun formatEnglishDate(
        customCalendar: CustomCalendar,
        locale: NepaliDateLocale
    ): String {
        return calendarModel.formatEnglishDate(
            year = customCalendar.year,
            month = customCalendar.month,
            dayOfMonth = customCalendar.dayOfMonth,
            dayOfWeek = customCalendar.dayOfWeek,
            locale = locale
        )
    }

    /**
     * Formats a time based on the specified Unicode pattern and language preferences.
     *
     * @param time A [SimpleTime] that holds the time details (hour, minute, second, nanosecond).
     * @param unicodePattern A string specifying the pattern to use for formatting the date and time. The pattern supports
     *        various placeholders like "HH", "hh", "mm", "ss", "SSS", "a", etc.
     * @param language The [NepaliDatePickerLang] specifying the language for formatting the date and time.
     *
     * @return A string representing the formatted time according to the provided Unicode pattern and language preferences.
     *
     * The function supports the following placeholders in the [unicodePattern]:
     * - `HH`: Hour in 24-hour format (00–23) (e.g., "08" or "०८")
     * - `H`: Hour in 24-hour format, no padding (e.g., "8" or "८")
     * - `hh`: Hour in 12-hour format (01–12) (e.g., "01" or "०१")
     * - `h`: Hour in 12-hour format, no padding (e.g., "1" or "१")
     * - `mm`: Minutes, two-digit (e.g., "05" or "०५")
     * - `m`: Minutes, no padding (e.g., "5" or "५")
     * - `ss`: Seconds, two-digit (e.g., "09" or "०९")
     * - `s`: Seconds, no padding (e.g., "9" or "९")
     *
     * - `SSSS`: Four-digit nanosecond precision (e.g., "1234" or "१२३४")
     * - `SSS`: Millisecond precision (e.g., "123" or "१२३")
     * - `SS`: Two-digit fractional seconds (e.g., "12" or "१२")
     * - `S`: One-digit fractional second (e.g., "1" or "१")
     *
     * - `a`: Lowercase AM/PM or localized period (e.g., "am" or "बिहान")
     * - `A`: Uppercase AM/PM or localized period (e.g., "AM" or "साँझ")
     *
     * Example usage:
     * ```
     * val time = SimpleTime(hour = 15, minute = 30, second = 45, nanosecond = 123)
     * val formattedTime = formatTimeByUnicodePattern(
     *     calendar,
     *     time,
     *     "hh:mm:ss.SSS A"
     * )
     * // formattedTime: "03:30:45.123 PM"
     * ```
     */
    fun formatTimeByUnicodePattern(
        unicodePattern: String,
        time: SimpleTime,
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
    ): String = calendarModel.formatTimeByUnicodePattern(
        unicodePattern = unicodePattern,
        time = time,
        language = language
    )

    /**
     * Formats an English date based on the specified Unicode pattern and language preferences.
     *
     * @param calendar A [CustomCalendar] representing the date.
     * @param unicodePattern A string specifying the pattern to use for formatting the date and time. The pattern supports
     *        various placeholders like "yyyy", "MM", "dd", "D", "w", "E", etc.
     * @param language The [NepaliDatePickerLang] specifying the language for formatting the date and time.
     *
     * @return A string representing the formatted date according to the provided Unicode pattern and language preferences.
     *
     * The function supports the following placeholders in the [unicodePattern]:
     * - `yyyy`: Four-digit year (e.g., "2025" or "२०२५")
     * - `yy`: Two-digit year (e.g., "25" or "२५")
     * - `MMMM`: Full month name (e.g., "January" or "जनवरी")
     * - `MMM`: Abbreviated month name (e.g., "Jan" or "जन")
     * - `MM`: Two-digit month (e.g., "01" or "०१")
     * - `M`: Month number, no padding (e.g., "1" or "१")
     * - `dd`: Two-digit day of month (e.g., "04" or "०४")
     * - `d`: Day of month, no padding (e.g., "4" or "४")
     * - `D`: Day of the year (1–366) (e.g., "123" or "१२३")
     * - `w`: Week of the year (e.g., "23" or "२३")
     *
     * - `EEEE`: Full weekday name (e.g., "Monday" or "सोमबार")
     * - `E`: Medium weekday name (e.g., "Mon" or "सोम")
     * - `EEEEE`: Short weekday name, if defined (e.g., "M" or "स")
     * - `ee`: Two-digit day of week (e.g., "02" or "०२")
     * - `e`: Day of week, no padding (e.g., "2" or "२")
     *
     * Example usage:
     * ```
     * val calendar = CustomCalendar(year = 2025, month = 5, dayOfMonth = 14, dayOfWeek = 3,...)
     * val formattedDateTime = formatEnglishDateByUnicodePattern(
     *     "yyyy-MM-dd EEEE",
     *     calendar,
     *     NepaliDatePickerLang.ENGLISH
     * )
     * // formattedDateTime: "2025-05-14 Tuesday"
     * ```
     */
    fun formatEnglishDateByUnicodePattern(
        unicodePattern: String,
        calendar: CustomCalendar,
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
    ): String = calendarModel.formatEnglishDateByUnicodePattern(
        unicodePattern = unicodePattern,
        calendar = calendar,
        language = language
    )

    /**
     * Formats a Nepali date based on the specified Unicode pattern and language preferences.
     *
     * @param calendar A [CustomCalendar] representing the date.
     * @param unicodePattern A string specifying the pattern to use for formatting the date and time. The pattern supports
     *        various placeholders like "yyyy", "MM", "dd", "D", "E", "ee", etc.
     * @param language The [NepaliDatePickerLang] specifying the language for formatting the date and time. Defaults to
     *        [NepaliDatePickerLang.NEPALI], which uses Nepali month and weekday names.
     *
     * @return A string representing the formatted date and time according to the provided Unicode pattern and language preferences.
     *
     * The function supports the following placeholders in the [unicodePattern]:
     * - `yyyy`: Four-digit year (e.g., "2025" or "२०२५")
     * - `yy`: Two-digit year (e.g., "25" or "२५")
     * - `MMMM`: Full month name (e.g., "Baisakh" or "बैशाख")
     * - `MMM`: Abbreviated month name (e.g., "Bai" or "बै")
     * - `MM`: Two-digit month (e.g., "01" or "०१")
     * - `M`: Month number, no padding (e.g., "1" or "१")
     * - `dd`: Two-digit day of month (e.g., "04" or "०४")
     * - `d`: Day of month, no padding (e.g., "4" or "४")
     * - `D`: Day of the year (1–366) (e.g., "123" or "१२३")
     * - `w`: Week of the year (e.g., "23" or "२३")
     *
     * - `EEEE`: Full weekday name (e.g., "Monday" or "सोमबार")
     * - `E`: Medium weekday name (e.g., "Mon" or "सोम")
     * - `EEEEE`: Short weekday name, if defined (e.g., "M" or "स")
     * - `ee`: Two-digit day of week (e.g., "02" or "०२")
     * - `e`: Day of week, no padding (e.g., "2" or "२")
     *
     * Example usage:
     * ```
     * val calendar = CustomCalendar(year = 2025, month = 5, dayOfMonth = 14, dayOfWeek = 3, ...)
     * val formattedDateTime = formatNepaliDateByUnicodePattern(
     *     "yyyy-MM-dd EEEE",
     *     calendar,
     *     NepaliDatePickerLang.ENGLISH
     * )
     * // formattedDateTime: "2025-05-14 Tuesday"
     * ```
     */
    fun formatNepaliDateByUnicodePattern(
        unicodePattern: String,
        calendar: CustomCalendar,
        language: NepaliDatePickerLang = NepaliDatePickerLang.NEPALI
    ): String = calendarModel.formatNepaliDateByUnicodePattern(
        unicodePattern = unicodePattern,
        calendar = calendar,
        language = language
    )

    /**
     * Formats an English date and time based on the specified Unicode pattern and language preferences.
     *
     * @param calendar A [CustomCalendar] representing the date.
     * @param time An optional [SimpleTime] that holds the time details (hour, minute, second, nanosecond).
     *        If not provided, the time part will be omitted in the formatted string.
     * @param unicodePattern A string specifying the pattern to use for formatting the date and time. The pattern supports
     *        various placeholders like "yyyy", "MM", "dd", "hh", "mm", "ss", etc.
     * @param language The [NepaliDatePickerLang] specifying the language for formatting the date and time. Defaults to
     *        [NepaliDatePickerLang.ENGLISH], which uses English month and weekday names.
     *
     * @return A string representing the formatted date and time according to the provided Unicode pattern and language preferences.
     *
     * The function supports the following placeholders in the [unicodePattern]:
     * - `yyyy`: Four-digit year (e.g., "2025" or "२०२५")
     * - `yy`: Two-digit year (e.g., "25" or "२५")
     * - `MMMM`: Full month name (e.g., "January" or "जनवरी")
     * - `MMM`: Abbreviated month name (e.g., "Jan" or "जन")
     * - `MM`: Two-digit month (e.g., "01" or "०१")
     * - `M`: Month number, no padding (e.g., "1" or "१")
     * - `dd`: Two-digit day of month (e.g., "04" or "०४")
     * - `d`: Day of month, no padding (e.g., "4" or "४")
     * - `D`: Day of the year (1–366) (e.g., "123" or "१२३")
     * - `w`: Week of the year (e.g., "23" or "२३")
     *
     * - `EEEE`: Full weekday name (e.g., "Monday" or "सोमबार")
     * - `E`: Medium weekday name (e.g., "Mon" or "सोम")
     * - `EEEEE`: Short weekday name, if defined (e.g., "M" or "स")
     * - `ee`: Two-digit day of week (e.g., "02" or "०२")
     * - `e`: Day of week, no padding (e.g., "2" or "२")
     *
     * - `HH`: Hour in 24-hour format (00–23) (e.g., "08" or "०८")
     * - `H`: Hour in 24-hour format, no padding (e.g., "8" or "८")
     * - `hh`: Hour in 12-hour format (01–12) (e.g., "01" or "०१")
     * - `h`: Hour in 12-hour format, no padding (e.g., "1" or "१")
     * - `mm`: Minutes, two-digit (e.g., "05" or "०५")
     * - `m`: Minutes, no padding (e.g., "5" or "५")
     * - `ss`: Seconds, two-digit (e.g., "09" or "०९")
     * - `s`: Seconds, no padding (e.g., "9" or "९")
     *
     * - `SSSS`: Four-digit nanosecond precision (e.g., "1234" or "१२३४")
     * - `SSS`: Millisecond precision (e.g., "123" or "१२३")
     * - `SS`: Two-digit fractional seconds (e.g., "12" or "१२")
     * - `S`: One-digit fractional second (e.g., "1" or "१")
     *
     * - `a`: Lowercase AM/PM or localized period (e.g., "am" or "बिहान")
     * - `A`: Uppercase AM/PM or localized period (e.g., "AM" or "साँझ")
     *
     * Example usage:
     * ```
     * val calendar = CustomCalendar(year = 2025, month = 5, dayOfMonth = 14, dayOfWeek = 3,...)
     * val time = SimpleTime(hour = 15, minute = 30, second = 45, nanosecond = 123)
     * val formattedDateTime = formatEnglishDateTimeByUnicodePattern(
     *     "yyyy-MM-dd EEEE hh:mm:ss a",
     *     calendar,
     *     time
     * )
     * // formattedDateTime: "2025-05-14 Tuesday 03:30:45 PM"
     * ```
     */
    fun formatEnglishDateTimeByUnicodePattern(
        unicodePattern: String,
        calendar: CustomCalendar,
        time: SimpleTime? = null,
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
    ): String {
        return calendarModel.formatEnglishDateTimeByUnicodePattern(
            unicodePattern = unicodePattern,
            calendar = calendar,
            time = time,
            language = language
        )
    }

    /**
     * Formats a Nepali date and time based on the specified Unicode pattern and language preferences.
     *
     * @param calendar A [CustomCalendar] representing the date.
     * @param time An optional [SimpleTime] that holds the time details (hour, minute, second, nanosecond).
     *        If not provided, the time part will be omitted in the formatted string.
     * @param unicodePattern A string specifying the pattern to use for formatting the date and time. The pattern supports
     *        various placeholders like "yyyy", "MM", "dd", "hh", "mm", "ss", etc.
     * @param language The [NepaliDatePickerLang] specifying the language for formatting the date and time. Defaults to
     *        [NepaliDatePickerLang.NEPALI], which uses Nepali month and weekday names.
     *
     * @return A string representing the formatted date and time according to the provided Unicode pattern and language preferences.
     *
     * The function supports the following placeholders in the [unicodePattern]:
     * - `yyyy`: Four-digit year (e.g., "2025" or "२०२५")
     * - `yy`: Two-digit year (e.g., "25" or "२५")
     * - `MMMM`: Full month name (e.g., "Baisakh" or "बैशाख")
     * - `MMM`: Abbreviated month name (e.g., "Bai" or "बै")
     * - `MM`: Two-digit month (e.g., "01" or "०१")
     * - `M`: Month number, no padding (e.g., "1" or "१")
     * - `dd`: Two-digit day of month (e.g., "04" or "०४")
     * - `d`: Day of month, no padding (e.g., "4" or "४")
     * - `D`: Day of the year (1–366) (e.g., "123" or "१२३")
     * - `w`: Week of the year (e.g., "23" or "२३")
     *
     * - `EEEE`: Full weekday name (e.g., "Monday" or "सोमबार")
     * - `E`: Medium weekday name (e.g., "M" or "सो")
     * - `EEEEE`: Small weekday name, if defined (e.g., "M" or "स")
     * - `ee`: Two-digit day of week (e.g., "02" or "०२")
     * - `e`: Day of week, no padding (e.g., "2" or "२")
     *
     * - `HH`: Hour in 24-hour format (00–23) (e.g., "08" or "०८")
     * - `H`: Hour in 24-hour format, no padding (e.g., "8" or "८")
     * - `hh`: Hour in 12-hour format (01–12) (e.g., "01" or "०१")
     * - `h`: Hour in 12-hour format, no padding (e.g., "1" or "१")
     * - `mm`: Minutes, two-digit (e.g., "05" or "०५")
     * - `m`: Minutes, no padding (e.g., "5" or "५")
     * - `ss`: Seconds, two-digit (e.g., "09" or "०९")
     * - `s`: Seconds, no padding (e.g., "9" or "९")
     *
     * - `SSSS`: Four-digit nanosecond precision (e.g., "1234" or "१२३४")
     * - `SSS`: Millisecond precision (e.g., "123" or "१२३")
     * - `SS`: Two-digit fractional seconds (e.g., "12" or "१२")
     * - `S`: One-digit fractional second (e.g., "1" or "१")
     *
     * - `a`: Lowercase AM/PM or localized period (e.g., "am" or "बिहान")
     * - `A`: Uppercase AM/PM or localized period (e.g., "AM" or "साँझ")
     *
     * Example usage:
     * ```
     * val calendar = CustomCalendar(year = 2025, month = 5, dayOfMonth = 14, dayOfWeek = 3)
     * val time = SimpleTime(hour = 15, minute = 30, second = 45, nanosecond = 123)
     * val formattedDateTime = formatNepaliDateTimeByUnicodePattern(
     *     "yyyy-MM-dd EEEE hh:mm:ss.SSS a"
     *     calendar,
     *     time,
     *     NepaliDatePickerLang.ENGLISH
     * )
     * // formattedDateTime: "2025-05-14 Tuesday 03:30:45.123 am"
     * ```
     */
    fun formatNepaliDateTimeByUnicodePattern(
        unicodePattern: String,
        calendar: CustomCalendar,
        time: SimpleTime? = null,
        language: NepaliDatePickerLang = NepaliDatePickerLang.NEPALI
    ): String {
        return calendarModel.formatNepaliDateTimeByUnicodePattern(
            unicodePattern = unicodePattern,
            calendar = calendar,
            time = time,
            language = language
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
     * @param month takes value between 1 to 12.
     * @param format gives name of month either in short or full name.
     * @param language gives name of `Nepali` months in english or nepali.
     *
     * [NameFormat.MEDIUM] & [NameFormat.FULL] gives full name of month i.e. January, February, etc.
     * [NameFormat.SHORT] gives short name of month i.e. Jan, Feb, etc.]
     *
     * Use [NepaliDatePickerLang.ENGLISH] for english name of Nepali month.
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
        language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
    ): String {
        return calendarModel.getEnglishMonthName(month, language, format)
    }

    /**
     * Formats a [SimpleTime] object into a time string in English.
     *
     * @param simpleTime The [SimpleTime] object to format.
     * @param use12HourFormat If true, the time will be formatted in 12-hour format (e.g., "09:45 AM").
     * If false, the time will be formatted in 24-hour format (e.g., "21:45").
     *
     * @return A formatted time string in English.
     *
     * ```
     * val time = SimpleTime(16, 30, 0, 0)
     * val englishTime12Hour = getFormattedTimeInEnglish(time) // Output: "4:30 PM"
     * val englishTime24Hour = getFormattedTimeInEnglish(time, false) // Output: "16:30"
     * ```
     */
    fun getFormattedTimeInEnglish(simpleTime: SimpleTime, use12HourFormat: Boolean = true): String {
        return if (use12HourFormat) {
            val amPm = if (simpleTime.hour < 12) "AM" else "PM"
            val standardHour =
                if (simpleTime.hour == 0) 12 else if (simpleTime.hour > 12) simpleTime.hour - 12 else simpleTime.hour

            "$standardHour:${simpleTime.minute.toString().padStart(2, '0')} $amPm"
        } else {
            "${simpleTime.hour}:${simpleTime.minute.toString().padStart(2, '0')}"
        }
    }

    /**
     * Formats a [SimpleTime] object into a Nepali time string.
     *
     * This function generates a formatted time string in Nepali.
     *
     * The time of day is determined using hour values to Nepali names:
     * * 4-10: "बिहान"
     * * 11-15: "दिउँसो"
     * * 16-19: "साँझ"
     * * Other: "राति"
     *
     * The hour is displayed in 12-hour format if `use12HourFormat` is true, otherwise in 24-hour format.
     * Minutes are always displayed with two digits (e.g., 05, 30).
     * Digits are converted to Nepali using the [convertToNepaliNumber] function.
     *
     * @param simpleTime The [SimpleTime] object to format.
     * @param use12HourFormat If true (default), the time will be formatted in 12-hour format including the time of day.
     *                        If false, the time will be formatted in 24-hour format.
     *
     * @return A formatted time string in Nepali.
     *
     * ```
     * val time = SimpleTime(16, 30, 0, 0)
     * val nepaliTime12Hour = getFormattedTimeInNepali(time) // Output: "दिउँसो ४ : ३०"
     * val nepaliTime24Hour = getFormattedTimeInNepali(time, false) // Output: "१६ : ३०"
     * ```
     */
    fun getFormattedTimeInNepali(simpleTime: SimpleTime, use12HourFormat: Boolean = true): String {
        val hourOfDay =
            when (simpleTime.hour) {
                in 3..11 -> "बिहान"
                in 12..16 -> "दिउँसो"
                in 17..19 -> "साँझ"
                else -> "राति"
            }

        val hour =
            if (use12HourFormat && simpleTime.hour > 12) simpleTime.hour - 12 else simpleTime.hour
        val formattedHour = if (use12HourFormat && simpleTime.hour == 0) 12 else hour

        return if (use12HourFormat) {
            "$hourOfDay ${
                formattedHour.toString().convertToNepaliNumber()
            } : ${simpleTime.minute.toString().padStart(2, '0').convertToNepaliNumber()}"
        } else "${formattedHour.toString().convertToNepaliNumber()} : ${
            simpleTime.minute.toString().padStart(2, '0').convertToNepaliNumber()
        }"
    }

    /**
     * Converts a Nepali (Bikram Sambat) date and time to ISO 8601 UTC format.
     *
     * The Nepali date (Bikram Sambat) is first converted to the equivalent Gregorian (English) date,
     * and then the time is appended to produce a complete timestamp in UTC.
     *
     * @param nepaliDate The date in the Nepali Bikram Sambat calendar as a [SimpleDate].
     * @param time The time of day as a [SimpleTime], default is the current time.
     *
     * @return A string in ISO 8601 format representing the UTC date and time.
     *         The result is in the format `YYYY-MM-DDTHH:mm:ssZ` (Zulu time).
     *
     * Example:
     * ```
     * val nepaliDate = SimpleDate(2081, 5, 24)  // Bikram Sambat date
     * val time = SimpleTime(14, 45, 15, 0)
     * val isoFormat = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(nepaliDate, time)
     * println(isoFormat)  // Outputs: "2024-09-09T09:00:15Z"
     * ```
     */
    fun formatNepaliDateTimeToIsoFormat(
        nepaliDate: SimpleDate,
        time: SimpleTime = currentTime
    ): String {
        return calendarModel.formatNepaliDateTimeToIsoFormat(nepaliDate, time)
    }

    /**
     * Converts an English (Gregorian) date and time to ISO 8601 UTC format.
     *
     * @param englishDate The date in the Gregorian calendar (English date) as a [SimpleDate].
     * @param time The `Nepali` time of day as a [SimpleTime], default is the current time.
     *
     * @return A string in ISO 8601 format representing the UTC date and time.
     *         The result is in the format `YYYY-MM-DDTHH:mm:ssZ` (Zulu time).
     *
     * Example:
     * ```
     * val englishDate = SimpleDate(2024, 9, 9)
     * val time = SimpleTime(14, 45, 15, 0)
     * val isoFormat = NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(englishDate, time)
     * println(isoFormat)  // Outputs: "2024-09-09T09:00:15Z"
     * ```
     */
    fun formatEnglishDateNepaliTimeToIsoFormat(
        englishDate: SimpleDate,
        time: SimpleTime = currentTime
    ): String {
        return calendarModel.formatEnglishDateNepaliTimeToIsoFormat(englishDate, time)
    }

    /**
     * Converts ISO 8601 UTC format to [CustomDateTime] which represents the Nepali [CustomCalendar] and Nepali [SimpleTime].
     *
     * @param isoDateTime The ISO 8601 format string
     * Examples of the ISO 8601 format dateTime:
     * - `2020-08-30T18:43:00Z`
     * - `2020-08-30T18:43:00.50Z`
     * - `2020-08-30T18:43:00.123456789Z`
     * - `2020-08-30T18:40:00+03:00`
     * - `2020-08-30T18:40:00+03:30:20`
     * * `2020-01-01T23:59:59.123456789+01`
     * * `+02020-01-31T23:59:59Z`
     *
     *
     * @return [CustomDateTime] which represents the Nepali [CustomCalendar] and Nepali [SimpleTime]
     *
     * @throws IllegalArgumentException if the isoDateTime text cannot be parsed or the date boundaries are exceeded.
     *
     * Example:
     * ```
     * val customNepaliDateTime = NepaliDateConverter.getNepaliDateTimeFromIsoFormat("2024-09-09T09:00:15Z)
     * println(customNepaliDateTime)  // Outputs: CustomDateTime(customCalendar = CustomCalendar(..), simpleTime = SimpleTime(..))
     * ```
     */
    fun getNepaliDateTimeFromIsoFormat(isoDateTime: String): CustomDateTime {
        return calendarModel.getNepaliDateTimeFromIsoFormat(isoDateTime)
    }

    /**
     * Converts ISO 8601 UTC format to [CustomDateTime] which represents the English [CustomCalendar] and Nepali [SimpleTime].
     *
     * @param isoDateTime The ISO 8601 format string
     * Examples of the ISO 8601 format dateTime:
     * - `2020-08-30T18:43:00Z`
     * - `2020-08-30T18:43:00.50Z`
     * - `2020-08-30T18:43:00.123456789Z`
     * - `2020-08-30T18:40:00+03:00`
     * - `2020-08-30T18:40:00+03:30:20`
     * * `2020-01-01T23:59:59.123456789+01`
     * * `+02020-01-31T23:59:59Z`
     *
     * @return [CustomDateTime] which represents the English [CustomCalendar] and Nepali [SimpleTime]
     *
     * @throws IllegalArgumentException if the isoDateTime text cannot be parsed or the date boundaries are exceeded.
     *
     * Example:
     * ```
     * val customEnglishDateTime = NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat("2024-09-09T09:00:15Z)
     * println(customEnglishDateTime)  // Outputs: CustomDateTime(customCalendar = CustomCalendar(..), simpleTime = SimpleTime(..))
     * ```
     */
    fun getEnglishDateNepaliTimeFromIsoFormat(isoDateTime: String): CustomDateTime {
        return calendarModel.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime)
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
                    calendarModel.compareDates(
                        customCalendar,
                        simpleDate.year,
                        simpleDate.month,
                        simpleDate.dayOfMonth
                    ) <= 0
                } else {
                    calendarModel.compareDates(
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
                    calendarModel.compareDates(
                        customCalendar,
                        simpleDate.year,
                        simpleDate.month,
                        simpleDate.dayOfMonth
                    ) >= 0
                } else {
                    calendarModel.compareDates(
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
                val compareMin = calendarModel.compareDates(
                    customCalendar,
                    minDate.year,
                    minDate.month,
                    minDate.dayOfMonth
                )
                val compareMax = calendarModel.compareDates(
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
        return calendarModel.compareDates(
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
        return calendarModel.compareDates(
            dateToCompareFrom,
            dateToCompareTo.year,
            dateToCompareFrom.month,
            dateToCompareFrom.dayOfMonth
        )
    }

    /**
     * Compares a [SimpleDate] instance with a date represented by the given year, month, and dayOfMonth.
     *
     * Returns a negative integer if the [SimpleDate] is before the given date, zero if they are equal,
     * and a positive integer if the [SimpleDate] is after the given date.
     *
     * @param simpleDate The [SimpleDate] instance to compare.
     * @param year The year of the date to compare with.
     * @param month The month of the date to compare with.
     * @param dayOfMonth The day of the month of the date to compare with.
     * @return A negative integer, zero, or a positive integer as described above.
     */
    fun compareDates(
        simpleDate: SimpleDate,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): Int {
        return calendarModel.compareDates(
            simpleDate,
            year,
            month,
            dayOfMonth
        )
    }

    /**
     * Replaces the delimiter in a date string formatted with the original delimiter.
     *
     * This function can handle a variety of date formats and will replace the existing
     * delimiter (`/`) with the specified new delimiter (e.g., `-`, or `.`).
     * The function works best with date formats or any strings such as:
     * - SHORT_MDY: 06/21/2024
     * - SHORT_YMD: 2024/06/21
     * - COMPACT_MDY: 06/21/24
     * - COMPACT_YMD: 24/06/21
     *
     * Example usage:
     * ```
     * val originalDate = "2024/06/21"
     * val newDelimiter = "-"
     * val formattedDate = NepaliDateConverter.replaceDelimiter(originalDate, newDelimiter)
     * // formattedDate: "2024-06-21"
     *
     * val originalDate = "२०२४/०६/२१"
     * val newDelimiter = "-"
     * val formattedDate = NepaliDateConverter.replaceDelimiter(originalDate, newDelimiter)
     * // formattedDate: "२०२४-०६-२१"
     *
     * val originalTime = "09:45 AM"
     * val newDelimiterSpace = " "
     * val oldDelimiter = ":"
     * val formattedTimeWithSpace = NepaliDateConverter.replaceDelimiter(originalTime, newDelimiterSpace, oldDelimiter)
     * // formattedTimeWithSpace: "09 45 AM"
     * ```
     *
     * @param dateString The date string containing the original delimiter (e.g., "2024/06/21").
     * @param newDelimiter The new delimiter to replace the old one (e.g., "-").
     * @return The date string with the delimiter replaced (e.g., "2024-06-21").
     */
    fun replaceDelimiter(
        dateString: String,
        newDelimiter: String,
        oldDelimiter: String = "/"
    ): String {
        return dateString.replace(oldDelimiter, newDelimiter)
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