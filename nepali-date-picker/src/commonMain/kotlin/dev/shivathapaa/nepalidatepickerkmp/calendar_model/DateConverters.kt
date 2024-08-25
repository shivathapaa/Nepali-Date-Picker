package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.daysInMonthMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

@Immutable
internal object DateConverters {

    private val minNepaliYear = NepaliDatePickerDefaults.NepaliYearRange.first
    private val maxNepaliYear = NepaliDatePickerDefaults.NepaliYearRange.last
    private val minEnglishYear = NepaliDatePickerDefaults.EnglishYearRange.first
    private val maxEnglishYear = NepaliDatePickerDefaults.EnglishYearRange.last

    private val startingEnglishCalendar = NepaliDatePickerDefaults.startingEnglishCalendar
    private val startingNepaliCalendar = NepaliDatePickerDefaults.startingNepaliCalendar

    fun getTotalDaysInNepaliMonth(nepaliYYYY: Int, nepaliMM: Int): Int {
        return daysInMonthMap.getValue(nepaliYYYY)[nepaliMM]
    }

    fun convertToNepaliCalendar(
        englishYYYY: Int,
        @IntRange(from = 1, to = 12) englishMM: Int,
        @IntRange(from = 1, to = 31) englishDD: Int
    ): NepaliCalendar {
        // Check if the input date is within the conversion range
        require(isEnglishDateInConversionRange(englishYYYY, englishMM, englishDD)) {
            throw IllegalArgumentException("Out of Range: English year $englishYYYY is out of range to convert.")
        }

        // Initialize the starting English and Nepali dates
        val (startingEnglishDate, startingNepaliCalendar, startingDayOfWeek) = initializeStartingDates()

        val newEnglishDate = LocalDate(englishYYYY, englishMM, englishDD)

        // Calculate the total number of days between the base date and the target date
        val totalDaysDifference =
            calculateEnglishDaysDifference(
                startingEnglishDate,
                newEnglishDate
            )

        // Initialize the Nepali date with the starting values
        var (nepaliYYYY, nepaliMM, nepaliDD) = Triple(
            startingNepaliCalendar.year,
            startingNepaliCalendar.month,
            startingNepaliCalendar.dayOfMonth
        )
        var dayOfWeek = startingDayOfWeek

        // Counters for day of year, week of year, and week of month
        var dayOfYear = 1
        var weekOfYear = 1
        var weekOfMonth = 1

        var firstDayOfMonth: Int = dayOfWeek
        var lastDayOfMonth: Int = -1

        var totalDaysInMonth = daysInMonthMap.getValue(nepaliYYYY)[nepaliMM]

        // Loop through the days to calculate the corresponding Nepali date
        repeat(totalDaysDifference) {

            nepaliDD++
            dayOfYear++
            dayOfWeek++

            if (dayOfWeek > 7) {
                dayOfWeek = 1
                weekOfYear++
                weekOfMonth++
            }

            if (nepaliDD > totalDaysInMonth) {
                nepaliMM++
                nepaliDD = 1

                // If the month exceeds 12, move to the next year and reset weekOfYear, month, and year.
                if (nepaliMM > 12) {
                    nepaliYYYY++
                    nepaliMM = 1
                    dayOfYear = 1
                    weekOfYear = 1
                }

                weekOfMonth = 1 // Reset week of month for a new month
                totalDaysInMonth = daysInMonthMap.getValue(nepaliYYYY)[nepaliMM]
                firstDayOfMonth = dayOfWeek // The first day of the new month
            }

            val remainingDaysOfTheMonth = totalDaysInMonth - nepaliDD
            lastDayOfMonth = (dayOfWeek + remainingDaysOfTheMonth) % 7

            // If the result is 0, it is mapped to 7 (Saturday)
            if (lastDayOfMonth == 0) lastDayOfMonth = 7
        }

        return NepaliCalendar(
            year = nepaliYYYY,
            month = nepaliMM,
            dayOfMonth = nepaliDD,
            dayOfWeekInMonth = (nepaliDD - 1) / 7 + 1,
            dayOfWeek = dayOfWeek,
            weekOfYear = weekOfYear,
            weekOfMonth = weekOfMonth,
            dayOfYear = dayOfYear,
            firstDayOfMonth = firstDayOfMonth,
            lastDayOfMonth = lastDayOfMonth,
            totalDaysInMonth = totalDaysInMonth,
            era = 2 // For Nepali date, the era is always 2 (for this library)
        )
    }

    fun convertToEnglishDate(
        nepaliYYYY: Int,
        @IntRange(from = 1, to = 12) nepaliMM: Int,
        @IntRange(from = 1, to = 32) nepaliDD: Int
    ): NepaliCalendar {
        // Check if the input Nepali date is within the conversion range
        require(isNepaliCalendarInConversionRange(nepaliYYYY, nepaliMM, nepaliDD)) {
            throw IllegalArgumentException("Out of Range: Nepali year $nepaliYYYY is out of range to convert.")
        }

        // Initialize the starting English and Nepali dates
        val (startingEnglishDate, startingNepaliCalendar, startingDayOfWeek) = initializeStartingDates()

        // Calculate the total number of Nepali days from the starting date to the target date
        val totalNepDaysCount =
            calculateTotalNepaliDaysCount(startingNepaliCalendar, nepaliYYYY, nepaliMM, nepaliDD)

        // Arrays holding the number of days in each month for regular and leap years
        val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val daysInMonthOfLeapYear = intArrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        // Initialize the English date with the starting values
        var (englishYYYY, englishMM, englishDD) = Triple(
            startingEnglishDate.year,
            startingEnglishDate.monthNumber,
            startingEnglishDate.dayOfMonth
        )

        var dayOfWeek = startingDayOfWeek
        var dayOfYear = 1
        var weekOfYear = 1
        var weekOfMonth = 1

        var firstDayOfMonth: Int = dayOfWeek
        var lastDayOfMonth: Int = -1

        var totalDaysInMonth =
            if (isEnglishLeapYear(englishYYYY)) daysInMonthOfLeapYear[englishMM] else daysInMonth[englishMM]

        // Loop through the total Nepali days to calculate the corresponding English date
        repeat(totalNepDaysCount) {
            englishDD++
            dayOfWeek++
            dayOfYear++

            if (englishDD > totalDaysInMonth) {
                englishMM++
                englishDD = 1
                weekOfMonth = 1 // Reset week of month for a new month

                if (englishMM > 12) {
                    englishYYYY++
                    englishMM = 1
                    dayOfYear = 1
                    weekOfYear = 1
                }

                totalDaysInMonth =
                    if (isEnglishLeapYear(englishYYYY)) daysInMonthOfLeapYear[englishMM] else daysInMonth[englishMM]
                firstDayOfMonth = dayOfWeek // The first day of the new month
            }

            if (dayOfWeek > 7) {
                dayOfWeek = 1
                weekOfYear++
                weekOfMonth++
            }

            val remainingDaysOfTheMonth = totalDaysInMonth - englishDD
            lastDayOfMonth = (dayOfWeek + remainingDaysOfTheMonth) % 7

            // If the result is 0, it is mapped to 7 (Saturday)
            if (lastDayOfMonth == 0) lastDayOfMonth = 7
        }

        return NepaliCalendar(
            year = englishYYYY,
            month = englishMM,
            dayOfMonth = englishDD,
            firstDayOfMonth = firstDayOfMonth,
            lastDayOfMonth = lastDayOfMonth,
            dayOfWeek = dayOfWeek,
            dayOfWeekInMonth = (englishDD - 1) / 7 + 1,
            dayOfYear = dayOfYear,
            weekOfMonth = weekOfMonth,
            weekOfYear = weekOfYear,
            totalDaysInMonth = totalDaysInMonth,
            era = 1 // For English Date, the era is always 1 (for this library)
        )
    }

    private fun initializeStartingDates(): Triple<LocalDate, NepaliCalendar, Int> {
        val startingEnglishDate = LocalDate(
            startingEnglishCalendar.year,
            startingEnglishCalendar.month,
            startingEnglishCalendar.dayOfMonth
        )
        val startingNepaliCalendar = startingNepaliCalendar
        val startingDayOfWeek = 1

        return Triple(startingEnglishDate, startingNepaliCalendar, startingDayOfWeek)
    }

    private fun calculateEnglishDaysDifference(
        startingDate: LocalDate,
        targetDate: LocalDate
    ): Int {
        return (startingDate.daysUntil(targetDate))
    }

    private fun calculateTotalNepaliDaysCount(
        startingNepaliCalendar: NepaliCalendar, nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int
    ): Int {
        var totalNepDaysCount = 0

        // Add days for full years between the starting year and the target year
        for (year in startingNepaliCalendar.year until nepaliYYYY) {
            val daysInYear = daysInMonthMap.getValue(year)
            for (month in 1..12) {
                totalNepDaysCount += daysInYear[month]
            }
        }

        // Add days for each month in the target year up to the target month
        for (month in startingNepaliCalendar.month until nepaliMM) {
            totalNepDaysCount += daysInMonthMap.getValue(nepaliYYYY)[month]
        }

        // Add the remaining days in the target month
        totalNepDaysCount += nepaliDD - startingNepaliCalendar.dayOfMonth

        return totalNepDaysCount
    }

    // Three overload functions for month details
    fun getNepaliMonth(
        nepaliYear: Int, @IntRange(from = 1, to = 12) nepaliMonth: Int
    ): NepaliMonthCalendar {
        // Calculate and return the month details
        return calculateNepaliMonthDetails(
            nepaliYear = nepaliYear, nepaliMonth = nepaliMonth
        )
    }

    fun getNepaliMonth(
        nepaliYear: Int, @IntRange(from = 1, to = 12) nepaliMonth: Int, addedMonthsCount: Int
    ): NepaliMonthCalendar {
        // Normalize the month and adjust the year accordingly
        val (newYear, newMonth) = adjustYearAndMonth(nepaliYear, nepaliMonth + addedMonthsCount)

        // Calculate and return the month details
        return calculateNepaliMonthDetails(newYear, newMonth)
    }

    fun getNepaliMonth(simpleNepaliDate: SimpleDate): NepaliCalendar {
        return getCustomCalendarUsingDayMonthYear(
            dayOfMonth = simpleNepaliDate.dayOfMonth,
            month = simpleNepaliDate.month,
            year = simpleNepaliDate.year
        )
    }

    private fun getCustomCalendarUsingDayMonthYear(
        year: Int,
        @IntRange(from = 1, to = 12) month: Int,
        @IntRange(from = 1, to = 32) dayOfMonth: Int
    ): NepaliCalendar {
        // Normalize the month and adjust the year accordingly
//        val (newYear, newMonth) = adjustYearAndMonth(year, month)

        // Get the total days in the new month
        val totalDaysInNewMonth = daysInMonthMap[year]?.get(month)
            ?: throw IllegalArgumentException("Invalid year $year or month $month passed.")

        // Adjust the day of the month if it exceeds the total days in the new month
        val newDayOfMonth = minOf(dayOfMonth, totalDaysInNewMonth)

        val newMonthDetails = calculateNepaliMonthDetails(year, month)

        // Calculate the day of the week
        val normalizedDayOfWeek =
            ((newMonthDetails.firstDayOfMonth + newDayOfMonth - 1) % 7).let { if (it == 0) 7 else it }

        val totalDayInYear = calculateDayOfYear(year, month, newDayOfMonth)

        // Return the updated CustomCalendar
        return NepaliCalendar(
            year = year,
            month = month,
            dayOfMonth = newDayOfMonth,
            totalDaysInMonth = totalDaysInNewMonth,
            firstDayOfMonth = newMonthDetails.firstDayOfMonth,
            lastDayOfMonth = newMonthDetails.lastDayOfMonth,
            dayOfWeekInMonth = (newDayOfMonth - 1) / 7 + 1,
            dayOfWeek = normalizedDayOfWeek,
            era = 2,
            dayOfYear = totalDayInYear,
            weekOfMonth = calculateWeekOfMonth(
                dayOfMonth = dayOfMonth, firstDayOfMonth = newMonthDetails.firstDayOfMonth
            ),
            weekOfYear = calculateWeekOfYear(
                dayOfYear = totalDayInYear, firstDayOfMonth = newMonthDetails.firstDayOfMonth
            )
        )
    }

    /**
     * Adjust the year and month to handle month overflow and underflow
     */
    private fun adjustYearAndMonth(year: Int, month: Int): Pair<Int, Int> {
        var adjustedYear = year
        var adjustedMonth = month

        // Handle month overflow and underflow
        while (adjustedMonth > 12) {
            adjustedMonth -= 12
            adjustedYear++
        }
        while (adjustedMonth < 1) {
            adjustedMonth += 12
            adjustedYear--
        }

        return Pair(adjustedYear, adjustedMonth)
    }

    /**
     * Calculate the first and last day of a given Nepali month
     */
    fun calculateNepaliMonthDetails(
        nepaliYear: Int, nepaliMonth: Int
    ): NepaliMonthCalendar {
        val totalDaysInMonth = daysInMonthMap[nepaliYear]?.get(nepaliMonth)
            ?: throw IllegalArgumentException("Invalid year $nepaliYear or month provided $nepaliMonth.")

        val startingNepaliCalendar = startingNepaliCalendar

        // Calculate the offset in days from the starting date to the target date
        val dayOffset = calculateDayOffset(startingNepaliCalendar.year, nepaliYear, nepaliMonth)

        // Calculate the first day of the month by applying the offset to the base day of the week
        val firstDayOfMonth = (startingNepaliCalendar.firstDayOfMonth + dayOffset) % 7
        val normalizedFirstDayOfMonth = if (firstDayOfMonth == 0) 7 else firstDayOfMonth

        // Calculate the last day of the month
        val normalizedLastDayOfMonth =
            ((normalizedFirstDayOfMonth + totalDaysInMonth - 1) % 7).let { if (it == 0) 7 else it }

        // Return the CustomMonth with first and last day details
        return NepaliMonthCalendar(
            year = nepaliYear,
            month = nepaliMonth,
            firstDayOfMonth = normalizedFirstDayOfMonth,
            totalDaysInMonth = totalDaysInMonth,
            lastDayOfMonth = normalizedLastDayOfMonth
        )
    }

    /**
     * Helper function to calculate the day offset from the starting Nepali date to the target Nepali date
     */
    private fun calculateDayOffset(
        startingYear: Int, targetYear: Int, targetMonth: Int
    ): Int {
        // Calculate the offset for years
        val yearOffset = (startingYear until targetYear).sumOf {
            daysInMonthMap[it]?.sum() ?: 0
        }

        // Calculate the offset for months in the target year
        val monthOffset = (1 until targetMonth).sumOf {
            daysInMonthMap[targetYear]?.get(it) ?: 0
        }

        return yearOffset + monthOffset
    }

    /**
     * Helper function to calculate the day of the year
     */
    private fun calculateDayOfYear(year: Int, month: Int, dayOfMonth: Int): Int {
        // Sum the days of the preceding months
        val monthOffset = (1 until month).sumOf {
            daysInMonthMap[year]?.get(it) ?: 0
        }

        // Add the days in the current month
        return monthOffset + dayOfMonth
    }

    /**
     * Helper function to calculate the week of the month
     */
    private fun calculateWeekOfMonth(dayOfMonth: Int, firstDayOfMonth: Int): Int {
        val daysBefore = (dayOfMonth - 1) + (firstDayOfMonth - 1)
        return daysBefore / 7 + 1
    }

    /**
     * INCORRECT RESPONSE: Helper function to calculate the week of the year
     *
     * It requires a bit more complex logic to calculate. This is not implemented yet due to performance issues.
     * This value is not required for the library in `getNepaliMonth()`.
     * Todo: Correct this logic
     */
    private fun calculateWeekOfYear(dayOfYear: Int, firstDayOfMonth: Int): Int {
        val firstDayOfYear = (firstDayOfMonth - (dayOfYear % 7) + 7) % 7
        return (dayOfYear + firstDayOfYear) / 7 + 1
    }

    /**
     * Helper function to check if the input Nepali date is within the conversion range
     */
    private fun isNepaliCalendarInConversionRange(
        nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int
    ): Boolean {
        return nepaliYYYY in minNepaliYear..maxNepaliYear && nepaliMM in 1..12 && nepaliDD in 1..32
    }

    /**
     * Helper function to check if the input English date is within the conversion range
     */
    private fun isEnglishDateInConversionRange(
        englishYYYY: Int, englishMM: Int, englishDD: Int
    ): Boolean {
        return englishYYYY in minEnglishYear..maxEnglishYear && englishMM in 1..12 && englishDD in 1..31
    }

    /**
     * Helper function to check if the input year is a leap year in English calendar
     */
    private fun isEnglishLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}