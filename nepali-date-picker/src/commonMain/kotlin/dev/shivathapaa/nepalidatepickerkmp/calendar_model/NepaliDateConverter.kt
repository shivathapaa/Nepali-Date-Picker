package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
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