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
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.daysInMonthMap
import dev.shivathapaa.nepalidatepickerkmp.data.englishDateMap
import dev.shivathapaa.nepalidatepickerkmp.data.nepaliDateMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

@Immutable
internal object DateConverters {

    private val minNepaliYear = NepaliDatePickerDefaults.NepaliYearRange.first
    private val maxNepaliYear = NepaliDatePickerDefaults.NepaliYearRange.last
    private val minEnglishYear = NepaliDatePickerDefaults.EnglishYearRange.first
    private val maxEnglishYear = NepaliDatePickerDefaults.EnglishYearRange.last

    fun getTotalDaysInNepaliMonth(nepaliYYYY: Int, nepaliMM: Int): Int {
        return daysInMonthMap.getValue(nepaliYYYY)[nepaliMM]
    }

    fun convertToNepaliCalendar(
        englishYYYY: Int,
        englishMM: Int,
        englishDD: Int
    ): CustomCalendar {
        // Check if the input date is within the conversion range
        require(isEnglishDateInConversionRange(englishYYYY, englishMM, englishDD)) {
            throw IllegalArgumentException("Out of Range: English year $englishYYYY is out of range to convert.")
        }

        // Initialize the starting English and Nepali dates
        val (startingEnglishDate, startingNepaliCalendar) = initializeStartingDates(
            englishYYYY,
            false
        )

        val englishLocalDate = LocalDate(
            year = startingEnglishDate.year,
            monthNumber = startingEnglishDate.month,
            dayOfMonth = startingEnglishDate.dayOfMonth
        )

        val newEnglishDate =
            LocalDate(year = englishYYYY, monthNumber = englishMM, dayOfMonth = englishDD)

        // Calculate the total number of days between the base date and the target date
        val totalDaysDifference = calculateEnglishDaysDifference(
            englishLocalDate, newEnglishDate
        )

        // Initialize the Nepali date with the starting values
        var (nepaliYYYY, nepaliMM, nepaliDD) = Triple(
            startingNepaliCalendar.year,
            startingNepaliCalendar.month,
            startingNepaliCalendar.dayOfMonth
        )
        var dayOfWeek = startingNepaliCalendar.dayOfWeek

        // Counters for day of year, week of year, and week of month
        var dayOfYear = startingNepaliCalendar.dayOfYear
        var weekOfYear = startingNepaliCalendar.weekOfYear
        var weekOfMonth = startingNepaliCalendar.weekOfMonth

        var firstDayOfMonth: Int = startingNepaliCalendar.firstDayOfMonth
        var lastDayOfMonth: Int = startingNepaliCalendar.lastDayOfMonth

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

        return CustomCalendar(
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
        nepaliMM: Int,
        nepaliDD: Int
    ): CustomCalendar {
        // Check if the input Nepali date is within the conversion range
        require(isNepaliCalendarInConversionRange(nepaliYYYY, nepaliMM, nepaliDD)) {
            throw IllegalArgumentException("Out of Range: Nepali year $nepaliYYYY is out of range to convert.")
        }

        // Initialize the starting English and Nepali dates
        val (startingEnglishDate, startingNepaliCalendar) = initializeStartingDates(
            nepaliYYYY,
            true
        )

        // Calculate the total number of Nepali days from the starting date to the target date
        val totalNepDaysCount =
            calculateTotalNepaliDaysCount(startingNepaliCalendar, nepaliYYYY, nepaliMM, nepaliDD)

        // Initialize the English date with the starting values
        var (englishYYYY, englishMM, englishDD) = Triple(
            startingEnglishDate.year,
            startingEnglishDate.month,
            startingEnglishDate.dayOfMonth
        )

        var dayOfWeek = startingEnglishDate.dayOfWeek
        var dayOfYear = startingEnglishDate.dayOfYear
        var weekOfYear = startingEnglishDate.weekOfYear
        var weekOfMonth = startingEnglishDate.weekOfMonth

        var firstDayOfMonth: Int = startingEnglishDate.firstDayOfMonth
        var lastDayOfMonth: Int = startingEnglishDate.lastDayOfMonth

        var totalDaysInMonth = getTotalDaysInEnglishMonth(englishYYYY, englishMM)

        // Loop through the total Nepali days to calculate the corresponding English date
        repeat(totalNepDaysCount) {
            englishDD++
            dayOfWeek++
            dayOfYear++

            if (dayOfWeek > 7) {
                dayOfWeek = 1
                weekOfYear++
                weekOfMonth++
            }

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

                totalDaysInMonth = getTotalDaysInEnglishMonth(englishYYYY, englishMM)
                firstDayOfMonth = dayOfWeek // The first day of the new month
            }

            val remainingDaysOfTheMonth = totalDaysInMonth - englishDD
            lastDayOfMonth = (dayOfWeek + remainingDaysOfTheMonth) % 7

            // If the result is 0, it is mapped to 7 (Saturday)
            if (lastDayOfMonth == 0) lastDayOfMonth = 7
        }

        return CustomCalendar(
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

    private fun initializeStartingDates(targetYear: Int, isNepaliDate: Boolean)
            : Pair<CustomCalendar, CustomCalendar> {
        val referenceDate =
            if (isNepaliDate) nepaliDateMap[targetYear - 1]
            else englishDateMap[targetYear - 1]

        val startingEnglishDate =
            referenceDate?.englishDate ?: NepaliDatePickerDefaults.startingEnglishCalendar
        val startingNepaliCalendar =
            referenceDate?.nepaliDate ?: NepaliDatePickerDefaults.startingNepaliCalendar

        return Pair(startingEnglishDate, startingNepaliCalendar)
    }

    private fun calculateEnglishDaysDifference(
        startingDate: LocalDate, targetDate: LocalDate
    ): Int {
        return (startingDate.daysUntil(targetDate))
    }

    private fun calculateTotalNepaliDaysCount(
        startingNepaliCalendar: CustomCalendar, nepaliYYYY: Int, nepaliMM: Int, nepaliDD: Int
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

    fun getNepaliMonth(
        nepaliYear: Int, nepaliMonth: Int, addedMonthsCount: Int
    ): NepaliMonthCalendar {
        // Normalize the month and adjust the year accordingly
        val (newYear, newMonth) = adjustYearAndMonth(nepaliYear, nepaliMonth + addedMonthsCount)

        // Calculate and return the month details
        return calculateNepaliMonthDetails(newYear, newMonth)
    }

    fun getNepaliCalendar(simpleNepaliDate: SimpleDate): CustomCalendar {
        return getCustomCalendarUsingDayMonthYear(
            dayOfMonth = simpleNepaliDate.dayOfMonth,
            month = simpleNepaliDate.month,
            year = simpleNepaliDate.year,
            adjustMonth = false
        )
    }

    /**
     * Calculates the total number of days between two [SimpleDate] objects in the Nepali calendar.
     *
     * @param startDate The starting date.
     * @param endDate The ending date.
     * @return The number of days between the two dates. Returns -1 if either date is invalid,
     *         and throws IllegalArgumentException if the year is not found in [daysInMonthMap].
     */
    fun nepaliDaysInBetween(startDate: SimpleDate, endDate: SimpleDate): Int {
        val referenceYear: Int

        if (startDate.year > endDate.year) {
            return -nepaliDaysInBetween(endDate, startDate)
        } else {
            referenceYear = startDate.year
        }

        if (!isNepaliCalendarInConversionRange(
                startDate.year,
                startDate.month,
                startDate.dayOfMonth
            )
            || !isNepaliCalendarInConversionRange(endDate.year, endDate.month, endDate.dayOfMonth)
        ) {
            throw IllegalArgumentException(
                "Out of Range: Nepali start year ${startDate.year} or end year " +
                        "${startDate.year} is out of range to compare. Check range value from NepaliDatePickerDefaults."
            )
        }

        val startOffset = calculateDayOffset(
            referenceYear,
            startDate.year,
            startDate.month
        ) + startDate.dayOfMonth
        val endOffset =
            calculateDayOffset(referenceYear, endDate.year, endDate.month) + endDate.dayOfMonth

        return endOffset - startOffset
    }

    private fun getCustomCalendarUsingDayMonthYear(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        adjustMonth: Boolean
    ): CustomCalendar {
        // Normalize the month and adjust the year accordingly
//        val (newYear, newMonth) = adjustYearAndMonth(year, month)

        val newMonthDetails = calculateNepaliMonthDetails(year, month)

        // Adjust the day of the month if it exceeds the total days in the new month
        val newDayOfMonth = if (adjustMonth) {
            minOf(dayOfMonth, newMonthDetails.totalDaysInMonth)
        } else {
            if (dayOfMonth > newMonthDetails.totalDaysInMonth) {
                throw IllegalArgumentException("Day of Month $dayOfMonth is out of bound. There is no $dayOfMonth in $month month.")
            } else {
                dayOfMonth
            }
        }

        // Calculate the day of the week
        val normalizedDayOfWeek =
            ((newMonthDetails.firstDayOfMonth + newDayOfMonth - 1) % 7).let { if (it == 0) 7 else it }

        val totalDayInYear = calculateDayOfYear(year, month, newDayOfMonth)

        // Return the updated CustomCalendar
        return CustomCalendar(
            year = year,
            month = month,
            dayOfMonth = newDayOfMonth,
            totalDaysInMonth = newMonthDetails.totalDaysInMonth,
            firstDayOfMonth = newMonthDetails.firstDayOfMonth,
            lastDayOfMonth = newMonthDetails.lastDayOfMonth,
            dayOfWeekInMonth = (newDayOfMonth - 1) / 7 + 1,
            dayOfWeek = normalizedDayOfWeek,
            era = 2,
            dayOfYear = totalDayInYear,
            weekOfMonth = calculateWeekOfMonth(
                dayOfMonth = dayOfMonth, firstDayOfMonth = newMonthDetails.firstDayOfMonth
            ),
            // Todo: Simplify logic
            weekOfYear = calculateWeekOfYear(
                dayOfYear = totalDayInYear,
                firstDayOfYear = calculateNepaliMonthDetails(year, 1).firstDayOfMonth
            )
        )
    }

    fun adjustNepaliDateForDayAdjustments(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        daysToAdjust: Int
    ): CustomCalendar {
        val totalDaysInCurrentMonth = daysInMonthMap[year]?.get(month)
            ?: throw IllegalArgumentException("Out of range: Invalid year $year or month $month passed.")

        if (dayOfMonth + daysToAdjust in 1..totalDaysInCurrentMonth) {
            val newDay = dayOfMonth + daysToAdjust
            return getCustomCalendarUsingDayMonthYear(year, month, newDay, true)
        }

        return if (daysToAdjust > 0) { // next month
            val remainingDays = daysToAdjust - (totalDaysInCurrentMonth - dayOfMonth + 1)
            val newMonth = month + 1
            val newYear = if (newMonth > 12) year + 1 else year
            adjustNepaliDateForDayAdjustments(
                year = newYear,
                month = if (newMonth > 12) 1 else newMonth,
                dayOfMonth = 1,  // first day of the next month
                daysToAdjust = remainingDays
            )
        } else { // previous month
            val newMonth = month - 1
            val newYear = if (newMonth < 1) year - 1 else year
            val daysInPreviousMonth =
                daysInMonthMap[newYear]?.get(if (newMonth < 1) 12 else newMonth)
                    ?: throw IllegalArgumentException("Out of range: Year $newYear or month $newMonth invalid due to daysToAdjust $daysToAdjust.")
            adjustNepaliDateForDayAdjustments(
                year = newYear,
                month = if (newMonth < 1) 12 else newMonth,
                dayOfMonth = daysInPreviousMonth,  // last day of the previous month
                daysToAdjust = daysToAdjust + dayOfMonth
            )
        }
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

        val startingNepaliCalendar = nepaliDateMap[nepaliYear - 1]?.nepaliDate
            ?: NepaliDatePickerDefaults.startingNepaliCalendar

        // Calculate the offset in days from the starting date to the target date
        val dayOffset =
            calculateDayOffset(startingNepaliCalendar.year, nepaliYear, nepaliMonth)

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
     * Works perfectly for recent tests and edge cases.
     *
     * Todo: Add more tests covering more edge cases for this. Still have some doubts regarding this.
     */
    private fun calculateWeekOfYear(dayOfYear: Int, firstDayOfYear: Int): Int {
        val totalDaysPassed = dayOfYear + (firstDayOfYear - 1)

        return if (totalDaysPassed % 7 == 0) {
            totalDaysPassed / 7
        } else {
            (totalDaysPassed / 7) + 1
        }
    }

    /**
     * Calculates the total number of days in a given month of the Gregorian calendar (English calendar).
     *
     * @param year The year (e.g., 2024).
     * @param month The month (1 for January, 2 for February, ..., 12 for December).
     * @return The total number of days in the specified month.
     * @throws IllegalArgumentException If the provided month is invalid (outside the range of 1 to 12).
     */
    internal fun getTotalDaysInEnglishMonth(year: Int, month: Int): Int {
        require(month in 1..12) { "Invalid month: $month. Month must be between 1 and 12." }

        val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val daysInMonthOfLeapYear = intArrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        return if (isEnglishLeapYear(year)) daysInMonthOfLeapYear[month] else daysInMonth[month]
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