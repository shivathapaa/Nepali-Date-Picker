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
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals

// Todo: Extend test to year 2082 above after data correction
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
            calendarModel.getNepaliMonth(simpleNepaliDate = today.toSimpleDate())
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
            dayOfWeek = 1,
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
            fromNepaliCalendar = nepaliCustomCalendar.toNepaliMonthCalendar(), subtractedMonthsCount = 4
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
            dayOfWeek = 1,
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
}