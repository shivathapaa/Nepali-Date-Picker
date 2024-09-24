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

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.getNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals

class NepaliDateConverterTest {
    private val calendarModel = NepaliCalendarModel()

    @Test
    fun nepaliToEnglishDateConverter_NepaliNewYear2079_GetEnglishYear20220414() {
        val toTestEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = 2079, nepaliMM = 1, nepaliDD = 1
        )

        val correctEnglishCustomCalendar = CustomCalendar(
            year = 2022,
            month = 4,
            dayOfMonth = 14,
            era = 1,
            firstDayOfMonth = 6,
            lastDayOfMonth = 7,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 2,
            dayOfWeek = 5,
            dayOfYear = 104,
            weekOfMonth = 3,
            weekOfYear = 16
        )

        assertEquals(correctEnglishCustomCalendar, toTestEnglishCalendar)
    }

    @Test
    fun nepaliToEnglishDateConverter_NepaliNewYear2082_GetEnglishYear20250414() {
        val toTestEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = 2082, nepaliMM = 1, nepaliDD = 1
        )

        val correctEnglishCustomCalendar = CustomCalendar(
            year = 2025,
            month = 4,
            dayOfMonth = 14,
            era = 1,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 2,
            dayOfWeek = 2,
            dayOfYear = 104,
            weekOfMonth = 3,
            weekOfYear = 16
        )

        assertEquals(correctEnglishCustomCalendar, toTestEnglishCalendar)
    }

    @Test
    fun nepaliToEnglishDateConverter_NepaliYear21001112_GetEnglishYear20440224() {
        val toTestEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = 2100, nepaliMM = 11, nepaliDD = 12
        )

        val correctEnglishCustomCalendar = CustomCalendar(
            year = 2044,
            month = 2,
            dayOfMonth = 24,
            era = 1,
            firstDayOfMonth = 2,
            lastDayOfMonth = 2,
            totalDaysInMonth = 29,
            dayOfWeekInMonth = 4,
            dayOfWeek = 4,
            dayOfYear = 55,
            weekOfMonth = 4,
            weekOfYear = 9
        )

        assertEquals(correctEnglishCustomCalendar, toTestEnglishCalendar)
    }

    @Test
    fun nepaliToEnglishDateConverter_NepaliYear20870921_GetEnglishYear20310105() {
        val toTestEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = 2087, nepaliMM = 9, nepaliDD = 21
        )

        val correctEnglishCustomCalendar = CustomCalendar(
            year = 2031,
            month = 1,
            dayOfMonth = 5,
            era = 1,
            firstDayOfMonth = 4,
            lastDayOfMonth = 6,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 5,
            weekOfMonth = 2,
            weekOfYear = 2
        )

        assertEquals(correctEnglishCustomCalendar, toTestEnglishCalendar)
    }

    @Test
    fun nepaliToEnglishDateConverter_NepaliYear20970317_GetEnglishYear20400701() {
        val toTestEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = 2097, nepaliMM = 3, nepaliDD = 17
        )

        val correctEnglishCustomCalendar = CustomCalendar(
            year = 2040,
            month = 7,
            dayOfMonth = 1,
            era = 1,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 183,
            weekOfMonth = 1,
            weekOfYear = 27
        )

        assertEquals(correctEnglishCustomCalendar, toTestEnglishCalendar)
    }

    @Test
    fun nepaliToEnglishDateConverter_NepaliYear20340629_GetEnglishYear19771015() {
        val toTestEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = 2034, nepaliMM = 6, nepaliDD = 29
        )

        val correctEnglishCustomCalendar = CustomCalendar(
            year = 1977,
            month = 10,
            dayOfMonth = 15,
            era = 1,
            firstDayOfMonth = 7,
            lastDayOfMonth = 2,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 3,
            dayOfWeek = 7,
            dayOfYear = 288,
            weekOfMonth = 3,
            weekOfYear = 42
        )

        assertEquals(correctEnglishCustomCalendar, toTestEnglishCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishYear20000624_GetNepaliYear20570310() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2000, englishMM = 6, englishDD = 24
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2057,
            month = 3,
            dayOfMonth = 10,
            era = 2,
            firstDayOfMonth = 5,
            lastDayOfMonth = 7,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 2,
            dayOfWeek = 7,
            dayOfYear = 73,
            weekOfMonth = 2,
            weekOfYear = 11
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishYear19770513_GetNepaliYear20540131() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 1997, englishMM = 5, englishDD = 13
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2054,
            month = 1,
            dayOfMonth = 31,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 5,
            dayOfWeek = 3,
            dayOfYear = 31,
            weekOfMonth = 5,
            weekOfYear = 5
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishYear19140808_GetNepaliYear19710424() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 1914, englishMM = 8, englishDD = 8
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 1971,
            month = 4,
            dayOfMonth = 24,
            era = 2,
            firstDayOfMonth = 5,
            lastDayOfMonth = 7,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 4,
            dayOfWeek = 7,
            dayOfYear = 118,
            weekOfMonth = 4,
            weekOfYear = 17
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishYear20250615_GetNepaliYear20820301() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2025, englishMM = 6, englishDD = 15
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2082,
            month = 3,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 63,
            weekOfMonth = 1,
            weekOfYear = 10
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishLeapYear20240229_GetNepaliYear20801117() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2024, englishMM = 2, englishDD = 29
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2080,
            month = 11,
            dayOfMonth = 17,
            era = 2,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 3,
            dayOfWeek = 5,
            dayOfYear = 322,
            weekOfMonth = 3,
            weekOfYear = 47
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishYear20320605_GetNepaliYear20890223() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2032, englishMM = 6, englishDD = 5
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2089,
            month = 2,
            dayOfMonth = 23,
            era = 2,
            firstDayOfMonth = 6,
            lastDayOfMonth = 2,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 4,
            dayOfWeek = 7,
            dayOfYear = 53,
            weekOfMonth = 4,
            weekOfYear = 8
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishYear20431014_GetNepaliYear20890223() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2043, englishMM = 10, englishDD = 14
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2100,
            month = 6,
            dayOfMonth = 27,
            era = 2,
            firstDayOfMonth = 6,
            lastDayOfMonth = 7,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 4,
            dayOfWeek = 4,
            dayOfYear = 184,
            weekOfMonth = 5,
            weekOfYear = 27
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    /*
    // Limit english year up to 2043 only
        @Test
        fun englishToNepaliDateConverter_EnglishYear20440412_GetNepaliYear21001230() {
            val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
                englishYYYY = 2044, englishMM = 4, englishDD = 12
            )

            val correctNepaliCustomCalendar = CustomCalendar(
                year = 2100,
                month = 12,
                dayOfMonth = 30,
                era = 2,
                firstDayOfMonth = 2,
                lastDayOfMonth = 4,
                totalDaysInMonth = 31,
                dayOfWeekInMonth = 5,
                dayOfWeek = 3,
                dayOfYear = 365,
                weekOfMonth = 5,
                weekOfYear = 53
            )

            assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
        }
    */

    @Test
    fun englishToNepaliDateConverter_EnglishAfterLeapYear20240302_GetNepaliYear20801119() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2024, englishMM = 3, englishDD = 2
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2080,
            month = 11,
            dayOfMonth = 19,
            era = 2,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 3,
            dayOfWeek = 7,
            dayOfYear = 324,
            weekOfMonth = 3,
            weekOfYear = 47
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun englishToNepaliDateConverter_EnglishAfterLeapYearFirstDayOfWeek20240303_GetNepaliYear20801120() {
        val toTestNepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2024, englishMM = 3, englishDD = 3
        )

        val correctNepaliCustomCalendar = CustomCalendar(
            year = 2080,
            month = 11,
            dayOfMonth = 20,
            era = 2,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4,
            totalDaysInMonth = 30,
            dayOfWeekInMonth = 3,
            dayOfWeek = 1,
            dayOfYear = 325,
            weekOfMonth = 4,
            weekOfYear = 48
        )

        assertEquals(correctNepaliCustomCalendar, toTestNepaliCalendar)
    }

    @Test
    fun dateConversion_convertToEnglish_GetSameFromConvertToNepali() {
        val nepaliCalendar = NepaliDateConverter.convertEnglishToNepali(
            englishYYYY = 2021, englishMM = 2, englishDD = 28
        )

        val englishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = nepaliCalendar.year,
            nepaliMM = nepaliCalendar.month,
            nepaliDD = nepaliCalendar.dayOfMonth
        )

        assertEquals(2021, englishCalendar.year)
        assertEquals(2, englishCalendar.month)
        assertEquals(28, englishCalendar.dayOfMonth)
    }

// Use Kotlin DateTime to get Instance of today's date
//    @Test
//    fun dateConversion_todayNepaliInstance_GetSameFromConvertToNepali() {
//        val today = DateConverter.todayNepaliDate
//
//        val nepaliCalendar =
//            DateConverter.convertEnglishToNepali(
//                englishYYYY = 2021,
//                englishMM = 8,
//                englishDD = 14
//            )
//
//        assertEquals(today, nepaliCalendar)
//    }

    @Test
    fun getNepaliMonth_todayNepaliDate_GetSameCustomMonthAndNepaliCalendarProperties() {
        val today = calendarModel.today

        val nepaliMonthFromSimpleDate =
            calendarModel.getNepaliCalendar(simpleNepaliDate = today.toSimpleDate())
        val nepaliMonthFromYearAndMonth =
            calendarModel.getNepaliMonth(nepaliYear = today.year, nepaliMonth = today.month)

        assertEquals(today.toNepaliMonthCalendar(), nepaliMonthFromYearAndMonth)
        assertEquals(nepaliMonthFromYearAndMonth, nepaliMonthFromSimpleDate.toNepaliMonthCalendar())
    }

    @Test
    fun getNepaliMonth_addOneMonthTo208203_GetNepaliCalendarOfDate208104() {
        val nepaliCustomCalendar = CustomCalendar(
            year = 2082,
            month = 3,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 63,
            weekOfMonth = 1,
            weekOfYear = 10
        )

        val customCalenderAfterOneMonthAdded = calendarModel.plusNepaliMonths(
            fromNepaliCalendar = nepaliCustomCalendar.toNepaliMonthCalendar(), addedMonthsCount = 1
        )

        val correctCustomCalendarAfterAddition = CustomCalendar(
            year = 2082,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 4,
            lastDayOfMonth = 7,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 1,
            dayOfWeek = 4,
            dayOfYear = 94,
            weekOfMonth = 1,
            weekOfYear = 14
        )

        assertEquals(correctCustomCalendarAfterAddition.year, customCalenderAfterOneMonthAdded.year)
        assertEquals(
            correctCustomCalendarAfterAddition.month, customCalenderAfterOneMonthAdded.month
        )
        assertEquals(
            correctCustomCalendarAfterAddition.firstDayOfMonth,
            customCalenderAfterOneMonthAdded.firstDayOfMonth
        )
        assertEquals(
            correctCustomCalendarAfterAddition.lastDayOfMonth,
            customCalenderAfterOneMonthAdded.lastDayOfMonth
        )
        assertEquals(
            correctCustomCalendarAfterAddition.totalDaysInMonth,
            customCalenderAfterOneMonthAdded.totalDaysInMonth
        )
    }

    @Test
    fun getNepaliMonth_addTwentyFourMonthTo207904_GetNepaliCalendarOfDate208104() {
        val nepaliCustomCalendar = CustomCalendar(
            year = 2079,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 95,
            weekOfMonth = 1,
            weekOfYear = 15
        )

        val customCalenderAfterOneMonthAdded = calendarModel.plusNepaliMonths(
            fromNepaliCalendar = nepaliCustomCalendar.toNepaliMonthCalendar(), addedMonthsCount = 24
        )

        val correctCustomCalendarAfterAddition = CustomCalendar(
            year = 2081,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 3,
            lastDayOfMonth = 6,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 1,
            dayOfWeek = 3,
            dayOfYear = 95,
            weekOfMonth = 1,
            weekOfYear = 15
        )

        assertEquals(correctCustomCalendarAfterAddition.year, customCalenderAfterOneMonthAdded.year)
        assertEquals(
            correctCustomCalendarAfterAddition.month, customCalenderAfterOneMonthAdded.month
        )
        assertEquals(
            correctCustomCalendarAfterAddition.firstDayOfMonth,
            customCalenderAfterOneMonthAdded.firstDayOfMonth
        )
        assertEquals(
            correctCustomCalendarAfterAddition.lastDayOfMonth,
            customCalenderAfterOneMonthAdded.lastDayOfMonth
        )
        assertEquals(
            correctCustomCalendarAfterAddition.totalDaysInMonth,
            customCalenderAfterOneMonthAdded.totalDaysInMonth
        )
    }

    @Test
    fun getNepaliMonth_subtractFourMonthTo208204_GetNepaliCalendarOfDate208112() {
        val nepaliCustomCalendar = CustomCalendar(
            year = 2082,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 4,
            lastDayOfMonth = 7,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 1,
            dayOfWeek = 4,
            dayOfYear = 94,
            weekOfMonth = 1,
            weekOfYear = 14
        )

        val customCalenderAfterFourMonthSubtraction = calendarModel.minusNepaliMonths(
            fromNepaliCalendar = nepaliCustomCalendar.toNepaliMonthCalendar(),
            subtractedMonthsCount = 4
        )

        val correctCustomCalendarAfterSubtraction = CustomCalendar(
            year = 2081,
            month = 12,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 6,
            lastDayOfMonth = 1,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 6,
            dayOfYear = 336,
            weekOfMonth = 1,
            weekOfYear = 49
        )

        assertEquals(
            correctCustomCalendarAfterSubtraction.year, customCalenderAfterFourMonthSubtraction.year
        )
        assertEquals(
            correctCustomCalendarAfterSubtraction.month,
            customCalenderAfterFourMonthSubtraction.month
        )
        assertEquals(
            correctCustomCalendarAfterSubtraction.firstDayOfMonth,
            customCalenderAfterFourMonthSubtraction.firstDayOfMonth
        )
        assertEquals(
            correctCustomCalendarAfterSubtraction.lastDayOfMonth,
            customCalenderAfterFourMonthSubtraction.lastDayOfMonth
        )
        assertEquals(
            correctCustomCalendarAfterSubtraction.totalDaysInMonth,
            customCalenderAfterFourMonthSubtraction.totalDaysInMonth
        )
    }

    @Test
    fun calculateFirstAndLastDayOfMonth_fromNepaliCalendar_getFirstLast() {
        val customCalendarOf2079 = CustomCalendar(
            year = 2079,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 95,
            weekOfMonth = 1,
            weekOfYear = 15
        )

        val customCalendarOf2082 = CustomCalendar(
            year = 2082,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 4,
            lastDayOfMonth = 7,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 1,
            dayOfWeek = 4,
            dayOfYear = 94,
            weekOfMonth = 1,
            weekOfYear = 14
        )

        val calculatedFirstAndLastDayOf2079 = getNepaliMonthCalendar(
            nepaliYear = customCalendarOf2079.year, nepaliMonth = customCalendarOf2079.month
        )

        val calculatedFirstAndLastDayOf2082 = getNepaliMonthCalendar(
            nepaliYear = customCalendarOf2082.year, nepaliMonth = customCalendarOf2082.month
        )

        assertEquals(customCalendarOf2079.year, calculatedFirstAndLastDayOf2079.year)
        assertEquals(customCalendarOf2079.month, calculatedFirstAndLastDayOf2079.month)
        assertEquals(
            customCalendarOf2079.firstDayOfMonth, calculatedFirstAndLastDayOf2079.firstDayOfMonth
        )
        assertEquals(
            customCalendarOf2079.lastDayOfMonth, calculatedFirstAndLastDayOf2079.lastDayOfMonth
        )
        assertEquals(
            customCalendarOf2079.totalDaysInMonth, calculatedFirstAndLastDayOf2079.totalDaysInMonth
        )
        assertEquals(
            (customCalendarOf2079.firstDayOfMonth - 1),
            calculatedFirstAndLastDayOf2079.daysFromStartOfWeekToFirstOfMonth
        )

        assertEquals(customCalendarOf2082.year, calculatedFirstAndLastDayOf2082.year)
        assertEquals(customCalendarOf2082.month, calculatedFirstAndLastDayOf2082.month)
        assertEquals(
            customCalendarOf2082.firstDayOfMonth, calculatedFirstAndLastDayOf2082.firstDayOfMonth
        )
        assertEquals(
            customCalendarOf2082.lastDayOfMonth, calculatedFirstAndLastDayOf2082.lastDayOfMonth
        )
        assertEquals(
            customCalendarOf2082.totalDaysInMonth, calculatedFirstAndLastDayOf2082.totalDaysInMonth
        )
        assertEquals(
            (customCalendarOf2082.firstDayOfMonth - 1),
            calculatedFirstAndLastDayOf2082.daysFromStartOfWeekToFirstOfMonth
        )
    }

    @Test
    fun calculatePositiveEnglishDaysInBetween_fromStartDate20160216ToEndDate20210327_get1866DaysDifference() {
        val startDate = SimpleDate(2016, 2, 16)
        val endDate = SimpleDate(2021, 3, 27)

        val daysBetween = NepaliDateConverter.getEnglishDaysInBetween(startDate, endDate)

        assertEquals(1866, daysBetween)
    }

    @Test
    fun calculatePositiveEnglishDaysInBetween_fromStartDate19801231ToEndDate20240308_get15773DaysDifference() {
        val startDate = SimpleDate(1980, 12, 31)
        val endDate = SimpleDate(2024, 3, 8)

        val daysBetween = NepaliDateConverter.getEnglishDaysInBetween(startDate, endDate)

        assertEquals(15773, daysBetween)
    }

    @Test
    fun calculateNegativeEnglishDaysInBetween_fromStartDate20240308ToEndDate19801231_getNegative15773DaysDifference() {
        val endDate = SimpleDate(1980, 12, 31)
        val startDate = SimpleDate(2024, 3, 8)

        val daysBetween = NepaliDateConverter.getEnglishDaysInBetween(startDate, endDate)

        assertEquals(-15773, daysBetween)
    }

    @Test
    fun calculatePositiveNepaliDaysInBetween_fromStartDate19801231ToEndDate20810524_get36674DaysDifference() {
        val startDate = SimpleDate(1980, 12, 31)
        val endDate = SimpleDate(2081, 5, 24)

        val daysBetween = NepaliDateConverter.getNepaliDaysInBetween(startDate, endDate)

        assertEquals(36675, daysBetween)
    }

    @Test
    fun calculateNegativeNepaliDaysInBetween_fromStartDate20810524_ToEndDate19801231_getNegative36674DaysDifference() {
        val endDate = SimpleDate(1980, 12, 31)
        val startDate = SimpleDate(2081, 5, 24)

        val daysBetween = NepaliDateConverter.getNepaliDaysInBetween(startDate, endDate)

        assertEquals(-36675, daysBetween)
    }

    @Test
    fun compareBothEnglishAndNepaliDaysInBetween_useRelativeDateForBothEnglishAndNepali_getSameDaysDifference() {
        val englishStartDate = SimpleDate(1998, 4, 12)
        val englishEndDate = SimpleDate(2024, 9, 21)
        val nepaliStartDate = SimpleDate(2054, 12, 30)
        val nepaliEndDate = SimpleDate(2081, 6, 5)

        val expectedDaysDifference = 9659

        val nepaliDaysBetween = NepaliDateConverter.getNepaliDaysInBetween(nepaliStartDate, nepaliEndDate)
        val englishDaysBetween = NepaliDateConverter.getEnglishDaysInBetween(englishStartDate, englishEndDate)

        assertEquals(expectedDaysDifference, nepaliDaysBetween)
        assertEquals(expectedDaysDifference, englishDaysBetween)
        assertEquals(nepaliDaysBetween, englishDaysBetween)
    }

    @Test
    fun compareCalculatedNepaliCalendarUsingDayMonthYearWithTodayAndAnyStartEndMonthDate_UseTodayAndRandomCalendar_GetSameNepaliCalendar() {
        val today = NepaliDateConverter.todayNepaliDate
        val calculatedNepaliCalendarFromToday = NepaliDateConverter.getNepaliCalendar(today.year, today.month, today.dayOfMonth)

        assertEquals(today, calculatedNepaliCalendarFromToday)

        val customStartCalendarOf2079 = CustomCalendar(
            year = 2079,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 1,
            dayOfWeek = 1,
            dayOfYear = 95,
            weekOfMonth = 1,
            weekOfYear = 15
        )
        val calculatedNepaliCalendarOf2079Start = NepaliDateConverter.getNepaliCalendar(customStartCalendarOf2079.year, customStartCalendarOf2079.month, customStartCalendarOf2079.dayOfMonth)

        assertEquals(customStartCalendarOf2079, calculatedNepaliCalendarOf2079Start)

        val customStartCalendarOf2082 = CustomCalendar(
            year = 2082,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 4,
            lastDayOfMonth = 7,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 1,
            dayOfWeek = 4,
            dayOfYear = 94,
            weekOfMonth = 1,
            weekOfYear = 14
        )
        val calculatedNepaliCalendarOf2082Start = NepaliDateConverter.getNepaliCalendar(customStartCalendarOf2082.year, customStartCalendarOf2082.month, customStartCalendarOf2082.dayOfMonth)

        assertEquals(customStartCalendarOf2082, calculatedNepaliCalendarOf2082Start)

        val customEndCalendarOf2054 = CustomCalendar(
            year = 2054,
            month = 1,
            dayOfMonth = 31,
            era = 2,
            firstDayOfMonth = 1,
            lastDayOfMonth = 3,
            totalDaysInMonth = 31,
            dayOfWeekInMonth = 5,
            dayOfWeek = 3,
            dayOfYear = 31,
            weekOfMonth = 5,
            weekOfYear = 5
        )
        val calculatedNepaliCalendarOf2054End = NepaliDateConverter.getNepaliCalendar(customEndCalendarOf2054.year, customEndCalendarOf2054.month, customEndCalendarOf2054.dayOfMonth)

        assertEquals(customEndCalendarOf2054, calculatedNepaliCalendarOf2054End)
    }

    @Test
    fun compareNepaliCalendarAfterDayAdjustments_UseRandomDatesUsingGetCalendar_GetSameNepaliCalendar() {
        val today = NepaliDateConverter.todayNepaliDate
        val getTodayAdjustedCalendar = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(today.year, today.month, today.dayOfMonth, 0)
        assertEquals(today, getTodayAdjustedCalendar)

        val customStartCalendarOf2082 = CustomCalendar(
            year = 2082,
            month = 4,
            dayOfMonth = 1,
            era = 2,
            firstDayOfMonth = 4,
            lastDayOfMonth = 7,
            totalDaysInMonth = 32,
            dayOfWeekInMonth = 1,
            dayOfWeek = 4,
            dayOfYear = 94,
            weekOfMonth = 1,
            weekOfYear = 14
        )

        val adjustedCalendarOf2082 = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(customStartCalendarOf2082.year, customStartCalendarOf2082.month, 20, -19)

        assertEquals(customStartCalendarOf2082, adjustedCalendarOf2082)

        val getNepaliCalendar2081 = NepaliDateConverter.getNepaliCalendar(2081, 5, 28)
        val adjustedNepaliCalendar2081 = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2081, 6, 9, -12)

        assertEquals(getNepaliCalendar2081, adjustedNepaliCalendar2081)

        val getNepaliCalendar2084 = NepaliDateConverter.getNepaliCalendar(2084, 1, 2)
        val adjustedNepaliCalendar2083 = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2083, 12, 25, 7)

        assertEquals(getNepaliCalendar2084, adjustedNepaliCalendar2083)
    }
}