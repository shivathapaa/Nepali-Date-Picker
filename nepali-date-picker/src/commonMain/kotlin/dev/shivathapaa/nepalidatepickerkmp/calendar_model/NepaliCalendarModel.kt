package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Immutable
internal class NepaliCalendarModel(val locale: NepaliDateLocale = NepaliDateLocale()) {

    private val dateConverter = DateConverters

    val today
        get(): NepaliCalendar {
            return getNepaliDateInstance()
        }

    private fun getNepaliDateInstance(): NepaliCalendar {
        val now = Clock.System.todayIn(TimeZone.of("Asia/Kathmandu"))
        return DateConverters.convertToNepaliCalendar(
            englishYYYY = now.year,
            englishMM = now.monthNumber,
            englishDD = now.dayOfMonth
        )
    }

    fun convertToNepaliCalendar(englishYYYY: Int, englishMM: Int, englishDD: Int): NepaliCalendar {
        return DateConverters.convertToNepaliCalendar(
            englishYYYY = englishYYYY, englishMM = englishMM, englishDD = englishDD
        )
    }

    fun convertToEnglishDate(nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int): NepaliCalendar {
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

    fun getNepaliMonth(simpleNepaliDate: SimpleDate): NepaliCalendar {
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
        fromNepaliCalendar: NepaliCalendar, subtractedMonthsCount: Int
    ): NepaliMonthCalendar {
        return DateConverters.getNepaliMonth(
            nepaliYear = fromNepaliCalendar.year,
            nepaliMonth = fromNepaliCalendar.month,
            addedMonthsCount = -subtractedMonthsCount
        )
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