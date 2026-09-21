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

import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.daysInMonthMap
import dev.shivathapaa.nepalidatepickerkmp.data.englishDateMap
import dev.shivathapaa.nepalidatepickerkmp.data.nepaliDateMap
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber

@Immutable
internal object DateConverters {

    private val minNepaliYear = NepaliCalendarDefaults.NepaliYearRange.first
    private val maxNepaliYear = NepaliCalendarDefaults.NepaliYearRange.last
    private val minEnglishYear = NepaliCalendarDefaults.EnglishYearRange.first
    private val maxEnglishYear = NepaliCalendarDefaults.EnglishYearRange.last

    // Index 0 is unused so a month number indexes directly.
    private val englishDaysInMonth =
        intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val englishDaysInMonthOfLeapYear =
        intArrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    // Days from Baisakh 1 of [minNepaliYear] to Baisakh 1 of each later supported year, indexed by
    // `year - minNepaliYear`.
    private val cumulativeDaysAtYearStart: IntArray by lazy {
        val counts = IntArray(maxNepaliYear - minNepaliYear + 1)
        var running = 0
        for (year in minNepaliYear..maxNepaliYear) {
            counts[year - minNepaliYear] = running
            running += daysInMonthMap[year]?.sum() ?: 0
        }
        counts
    }

    fun getTotalDaysInNepaliMonth(nepaliYYYY: Int, nepaliMM: Int): Int {
        require(nepaliMM in 1..12) {
            "Invalid month: $nepaliMM. Must be between 1 and 12."
        }
        return nepaliDaysInMonthArray(nepaliYYYY)[nepaliMM]
    }

    /**
     * Returns the `[0, m1..m12]` day-count array for [year], throwing a clear
     * [IllegalArgumentException] (rather than leaking the map's
     * `NoSuchElementException`) when the year falls outside the supported table.
     * Guards the day-walk converters against rolling a year past the table edge.
     */
    private fun nepaliDaysInMonthArray(year: Int): IntArray =
        daysInMonthMap[year] ?: throw IllegalArgumentException(
            "Out of Range: Nepali year $year is out of the supported range " +
                    "$minNepaliYear..$maxNepaliYear."
        )

    /**
     * Day counts for [year] as `[0, m1..m12]`, so a month number indexes directly.
     *
     * @throws IllegalArgumentException when [year] falls outside
     *   [NepaliCalendarDefaults.NepaliYearRange].
     */
    private fun supportedNepaliDaysInMonthArray(year: Int): IntArray {
        if (year !in minNepaliYear..maxNepaliYear) {
            throw IllegalArgumentException(
                "Out of Range: Nepali year $year is out of the supported range " +
                        "$minNepaliYear..$maxNepaliYear."
            )
        }
        return nepaliDaysInMonthArray(year)
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
            month = startingEnglishDate.month,
            day = startingEnglishDate.dayOfMonth
        )

        val newEnglishDate =
            LocalDate(year = englishYYYY, month = englishMM, day = englishDD)

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

        var totalDaysInMonth = nepaliDaysInMonthArray(nepaliYYYY)[nepaliMM]

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
                totalDaysInMonth = nepaliDaysInMonthArray(nepaliYYYY)[nepaliMM]
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

    /**
     * The English and Bikram Sambat pair a conversion counts forward from, taken one year before
     * [targetYear].
     *
     * Every supported year carries its own anchor, so a corrected month length in `daysInMonthMap`
     * changes dates in that year and the one after it, and no year beyond.
     */
    private fun initializeStartingDates(targetYear: Int, isNepaliDate: Boolean)
            : Pair<CustomCalendar, CustomCalendar> {
        val referenceDate =
            if (isNepaliDate) nepaliDateMap[targetYear - 1]
            else englishDateMap[targetYear - 1]

        val startingEnglishDate =
            referenceDate?.englishDate ?: NepaliCalendarDefaults.startingEnglishCalendar
        val startingNepaliCalendar =
            referenceDate?.nepaliDate ?: NepaliCalendarDefaults.startingNepaliCalendar

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
            val daysInYear = nepaliDaysInMonthArray(year)
            for (month in 1..12) {
                totalNepDaysCount += daysInYear[month]
            }
        }

        // Add days for each month in the target year up to the target month
        val daysInTargetYear = nepaliDaysInMonthArray(nepaliYYYY)
        for (month in startingNepaliCalendar.month until nepaliMM) {
            totalNepDaysCount += daysInTargetYear[month]
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
                        "${endDate.year} is out of range to compare. Check range value from NepaliDatePickerDefaults."
            )
        }

        val startOffset = calculateDayOffset(
            startingYear = referenceYear,
            targetYear = startDate.year,
            targetMonth = startDate.month,
            targetYearDaysInMonth = supportedNepaliDaysInMonthArray(startDate.year)
        ) + startDate.dayOfMonth
        val endOffset = calculateDayOffset(
            startingYear = referenceYear,
            targetYear = endDate.year,
            targetMonth = endDate.month,
            targetYearDaysInMonth = supportedNepaliDaysInMonthArray(endDate.year)
        ) + endDate.dayOfMonth

        return endOffset - startOffset
    }

    private fun getCustomCalendarUsingDayMonthYear(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        adjustMonth: Boolean
    ): CustomCalendar {
        val newMonthDetails = calculateNepaliMonthDetails(year, month)

        val newDayOfMonth = if (adjustMonth) {
            dayOfMonth.coerceIn(1, newMonthDetails.totalDaysInMonth)
        } else {
            require(dayOfMonth in 1..newMonthDetails.totalDaysInMonth) {
                "Day of Month $dayOfMonth is out of bound. There is no $dayOfMonth in $month month."
            }
            dayOfMonth
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
                dayOfMonth = newDayOfMonth, firstDayOfMonth = newMonthDetails.firstDayOfMonth
            ),
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
        require(month in 1..12) {
            "Invalid month: $month. Must be between 1 and 12."
        }
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
     * Grid geometry of a Bikram Sambat month: how many days it holds and the weekday its first and
     * last day fall on, counting Sunday as 1.
     *
     * @throws IllegalArgumentException when [nepaliMonth] is outside 1..12 or [nepaliYear] falls
     *   outside [NepaliCalendarDefaults.NepaliYearRange].
     */
    fun calculateNepaliMonthDetails(
        nepaliYear: Int, nepaliMonth: Int
    ): NepaliMonthCalendar {
        require(nepaliMonth in 1..12) {
            "Invalid month: $nepaliMonth. Must be between 1 and 12."
        }
        val daysInMonth = supportedNepaliDaysInMonthArray(nepaliYear)
        val totalDaysInMonth = daysInMonth[nepaliMonth]

        val startingNepaliCalendar = nepaliDateMap[nepaliYear - 1]?.nepaliDate
            ?: NepaliCalendarDefaults.startingNepaliCalendar

        val dayOffset = calculateDayOffset(
            startingYear = startingNepaliCalendar.year,
            targetYear = nepaliYear,
            targetMonth = nepaliMonth,
            targetYearDaysInMonth = daysInMonth
        )

        val firstDayOfMonth = (startingNepaliCalendar.firstDayOfMonth + dayOffset) % 7
        val normalizedFirstDayOfMonth = if (firstDayOfMonth == 0) 7 else firstDayOfMonth

        val normalizedLastDayOfMonth =
            ((normalizedFirstDayOfMonth + totalDaysInMonth - 1) % 7).let { if (it == 0) 7 else it }

        return NepaliMonthCalendar(
            year = nepaliYear,
            month = nepaliMonth,
            firstDayOfMonth = normalizedFirstDayOfMonth,
            totalDaysInMonth = totalDaysInMonth,
            lastDayOfMonth = normalizedLastDayOfMonth
        )
    }

    /**
     * Days from Baisakh 1 of [startingYear] to the first of [targetMonth] in [targetYear]. Both
     * years must already be inside [NepaliCalendarDefaults.NepaliYearRange].
     */
    private fun calculateDayOffset(
        startingYear: Int,
        targetYear: Int,
        targetMonth: Int,
        targetYearDaysInMonth: IntArray
    ): Int {
        val yearOffset = cumulativeDaysAtYearStart[targetYear - minNepaliYear] -
                cumulativeDaysAtYearStart[startingYear - minNepaliYear]

        var monthOffset = 0
        for (month in 1 until targetMonth) {
            monthOffset += targetYearDaysInMonth[month]
        }

        return yearOffset + monthOffset
    }

    /**
     * Helper function to calculate the day of the year
     */
    private fun calculateDayOfYear(year: Int, month: Int, dayOfMonth: Int): Int {
        val daysInMonth = nepaliDaysInMonthArray(year)
        var monthOffset = 0
        for (precedingMonth in 1 until month) {
            monthOffset += daysInMonth[precedingMonth]
        }

        return monthOffset + dayOfMonth
    }

    /**
     * Helper function to calculate the week of the month
     */
    private fun calculateWeekOfMonth(dayOfMonth: Int, firstDayOfMonth: Int): Int {
        val daysBefore = (dayOfMonth - 1) + (firstDayOfMonth - 1)
        return daysBefore / 7 + 1
    }

    /** Week of the year from the day-of-year and the weekday of the first day of the year. */
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

        return if (isEnglishLeapYear(year)) {
            englishDaysInMonthOfLeapYear[month]
        } else {
            englishDaysInMonth[month]
        }
    }

    /**
     * Grid geometry of a Gregorian month.
     *
     * The Gregorian counterpart of [calculateNepaliMonthDetails]. It needs neither a lookup table
     * nor a cache: kotlinx-datetime answers the weekday of the first, and the rest is arithmetic.
     */
    fun calculateEnglishMonthDetails(englishYYYY: Int, englishMM: Int): MonthCalendar {
        val totalDaysInMonth = getTotalDaysInEnglishMonth(englishYYYY, englishMM)
        val firstDayOfMonth = weekdayOf(LocalDate(englishYYYY, englishMM, 1))
        val lastDayOfMonth =
            ((firstDayOfMonth + totalDaysInMonth - 1) % 7).let { if (it == 0) 7 else it }

        return MonthCalendar(
            calendarSystem = CalendarSystem.GREGORIAN,
            year = englishYYYY,
            month = englishMM,
            totalDaysInMonth = totalDaysInMonth,
            firstDayOfMonth = firstDayOfMonth,
            lastDayOfMonth = lastDayOfMonth
        )
    }

    /**
     * A fully populated Gregorian [CustomCalendar] for a date given directly in the Gregorian
     * calendar, without the round trip through Bikram Sambat that [convertToEnglishDate] performs.
     *
     * Derived fields use the same helpers as the Bikram Sambat side, so `dayOfWeek`, `weekOfMonth`
     * and `weekOfYear` follow one definition across both calendars.
     */
    fun getEnglishCalendar(englishYYYY: Int, englishMM: Int, englishDD: Int): CustomCalendar {
        val monthDetails = calculateEnglishMonthDetails(englishYYYY, englishMM)
        require(englishDD in 1..monthDetails.totalDaysInMonth) {
            "Day of Month $englishDD is out of bound. There is no $englishDD in $englishMM month."
        }

        val dayOfYear = englishDayOfYear(englishYYYY, englishMM, englishDD)
        val dayOfWeek = ((monthDetails.firstDayOfMonth + englishDD - 1) % 7)
            .let { if (it == 0) 7 else it }

        return CustomCalendar(
            year = englishYYYY,
            month = englishMM,
            dayOfMonth = englishDD,
            era = CalendarSystem.GREGORIAN.era,
            firstDayOfMonth = monthDetails.firstDayOfMonth,
            lastDayOfMonth = monthDetails.lastDayOfMonth,
            totalDaysInMonth = monthDetails.totalDaysInMonth,
            dayOfWeekInMonth = (englishDD - 1) / 7 + 1,
            dayOfWeek = dayOfWeek,
            dayOfYear = dayOfYear,
            weekOfMonth = calculateWeekOfMonth(englishDD, monthDetails.firstDayOfMonth),
            weekOfYear = calculateWeekOfYear(
                dayOfYear = dayOfYear,
                firstDayOfYear = weekdayOf(LocalDate(englishYYYY, 1, 1))
            )
        )
    }

    /** Every day of a Gregorian month as a [CustomCalendar], in day order. */
    fun englishCalendarsInMonth(englishYYYY: Int, englishMM: Int): List<CustomCalendar> {
        val totalDaysInMonth = getTotalDaysInEnglishMonth(englishYYYY, englishMM)
        val calendars = ArrayList<CustomCalendar>(totalDaysInMonth)
        for (dayOfMonth in 1..totalDaysInMonth) {
            calendars.add(getEnglishCalendar(englishYYYY, englishMM, dayOfMonth))
        }
        return calendars
    }

    /**
     * The Bikram Sambat equivalent of every day of a Gregorian month, in day order, with `null` for
     * days that predate the conversion anchor (English 1913-04-13).
     *
     * The pickers need this whenever Gregorian is the calendar on screen: each cell still resolves
     * to a canonical Bikram Sambat date for selection and for the selectable-date predicate.
     * Calling [convertToNepaliCalendar] per cell would pay its day-walk 28 to 31 times per month.
     * This pays it once, for the month's first convertible day, then advances the date by one and
     * reads each day back through the cached [getNepaliCalendar] path, so every element is
     * identical to the one the Bikram Sambat grid builds for the same date.
     */
    fun nepaliCalendarsInEnglishMonth(englishYYYY: Int, englishMM: Int): List<CustomCalendar?> {
        val totalDaysInMonth = getTotalDaysInEnglishMonth(englishYYYY, englishMM)
        val calendars = ArrayList<CustomCalendar?>(totalDaysInMonth)

        var nepaliDate: SimpleDate? = null
        for (dayOfMonth in 1..totalDaysInMonth) {
            nepaliDate = when {
                nepaliDate != null -> nextNepaliDate(nepaliDate)
                isEnglishDateInConversionRange(englishYYYY, englishMM, dayOfMonth) ->
                    convertToNepaliCalendar(englishYYYY, englishMM, dayOfMonth).toSimpleDate()

                else -> null
            }
            calendars.add(nepaliDate?.let { getNepaliCalendar(it) })
        }
        return calendars
    }

    /**
     * The English equivalent of every day of a Bikram Sambat month, in day order.
     *
     * The mirror of [nepaliCalendarsInEnglishMonth], and the same one-walk-then-advance shape: the
     * dual-date grid needs all 29 to 32 English days of a Bikram Sambat month, and
     * [convertToEnglishDate] would walk from the year anchor for each one.
     *
     * Every day has an English equivalent, so unlike the reverse direction this never yields null.
     * The tail of the supported Bikram Sambat table maps past [NepaliCalendarDefaults.EnglishYearRange],
     * which is expected: that range bounds English *input*, not output.
     */
    fun englishCalendarsInNepaliMonth(nepaliYYYY: Int, nepaliMM: Int): List<CustomCalendar> {
        val totalDaysInMonth = getTotalDaysInNepaliMonth(nepaliYYYY, nepaliMM)
        val calendars = ArrayList<CustomCalendar>(totalDaysInMonth)

        var englishDate = convertToEnglishDate(nepaliYYYY, nepaliMM, 1).toSimpleDate()
        for (dayOfMonth in 1..totalDaysInMonth) {
            if (dayOfMonth > 1) englishDate = nextEnglishDate(englishDate)
            calendars.add(
                getEnglishCalendar(englishDate.year, englishDate.month, englishDate.dayOfMonth)
            )
        }
        return calendars
    }

    /** Whether the given English date has a Bikram Sambat equivalent in the supported table. */
    fun isEnglishDateConvertible(englishYYYY: Int, englishMM: Int, englishDD: Int): Boolean =
        isEnglishDateInConversionRange(englishYYYY, englishMM, englishDD)

    /**
     * The day after [date] in the Bikram Sambat calendar.
     *
     * Throws through [nepaliDaysInMonthArray] when [date] is in the table's last year and the roll
     * would leave it.
     */
    private fun nextNepaliDate(date: SimpleDate): SimpleDate {
        val totalDaysInMonth = nepaliDaysInMonthArray(date.year)[date.month]
        return when {
            date.dayOfMonth < totalDaysInMonth -> date.copy(dayOfMonth = date.dayOfMonth + 1)
            date.month < 12 -> SimpleDate(date.year, date.month + 1, 1)
            else -> SimpleDate(date.year + 1, 1, 1)
        }
    }

    /** The day after [date] in the Gregorian calendar. */
    private fun nextEnglishDate(date: SimpleDate): SimpleDate {
        val totalDaysInMonth = getTotalDaysInEnglishMonth(date.year, date.month)
        return when {
            date.dayOfMonth < totalDaysInMonth -> date.copy(dayOfMonth = date.dayOfMonth + 1)
            date.month < 12 -> SimpleDate(date.year, date.month + 1, 1)
            else -> SimpleDate(date.year + 1, 1, 1)
        }
    }

    private fun englishDayOfYear(year: Int, month: Int, dayOfMonth: Int): Int =
        (1 until month).sumOf { getTotalDaysInEnglishMonth(year, it) } + dayOfMonth

    // kotlinx-datetime numbers weekdays the ISO way (Monday = 1 ... Sunday = 7); this library
    // numbers them from Sunday = 1 so both calendars share one weekday grid.
    private fun weekdayOf(date: LocalDate): Int = date.dayOfWeek.isoDayNumber % 7 + 1

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
        if (englishYYYY !in minEnglishYear..maxEnglishYear) return false
        if (englishMM !in 1..12 || englishDD !in 1..31) return false

        // Reject English dates that fall before the earliest convertible anchor
        // (English 1913-04-13 ≡ Nepali 1970-01-01). Without this guard, dates in
        // 1913-01-01..1913-04-12 pass the year check, then the day-walk runs
        // `repeat(negativeDiff)` zero times and silently returns the anchor
        // (Nepali 1970-01-01) - a wrong result with no error.
        val start = NepaliCalendarDefaults.startingEnglishCalendar
        if (englishYYYY == start.year &&
            (englishMM < start.month ||
                    (englishMM == start.month && englishDD < start.dayOfMonth))
        ) return false

        return true
    }

    /**
     * Helper function to check if the input year is a leap year in English calendar
     */
    private fun isEnglishLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}