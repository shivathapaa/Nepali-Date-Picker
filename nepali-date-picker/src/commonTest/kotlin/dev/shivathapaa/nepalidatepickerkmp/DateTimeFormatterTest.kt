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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.convertToNepaliNumber
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.localizeNumber
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeFormatterTest {

    @Test
    fun formatNepaliDate_RandomDate_GetFormattedDateInStringByLocale() {
        val englishLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.ENGLISH,
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.FULL,
            monthName = NameFormat.FULL
        )

        val nepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.MEDIUM,
            monthName = NameFormat.FULL
        )

        val fullFormatNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.FULL,
            monthName = NameFormat.FULL
        )

        val toTestFormattedDateInEnglish = NepaliDateConverter.formatNepaliDate(
            year = 2080,
            month = 3,
            dayOfMonth = 15,
            dayOfWeek = 3,
            englishLocale
        )
        val formattedDateInEnglish = "Tuesday, Asar 15, 2080"

        val toTestFormattedDateInNepali = NepaliDateConverter.formatNepaliDate(
            year = 2080,
            month = 6,
            dayOfMonth = 2,
            dayOfWeek = 5,
            nepaliLocale
        )
        val formattedDateInNepali = "२०८० असोज २"

        val toTestFullFormattedDateInNepali = NepaliDateConverter.formatNepaliDate(
            year = 2080,
            month = 2,
            dayOfMonth = 2,
            dayOfWeek = 5,
            fullFormatNepaliLocale
        )
        val fullFormattedDateInNepali = "बिहिबार, जेठ २, २०८०"


        assertEquals(formattedDateInEnglish, toTestFormattedDateInEnglish)
        assertEquals(formattedDateInNepali, toTestFormattedDateInNepali)
        assertEquals(fullFormattedDateInNepali, toTestFullFormattedDateInNepali)
    }

    @Test
    fun formatNepaliDateUsingOtherHelperFunctionOfNames_CustomCalendarDate_GetFormattedDateInString() {
        val customCalendar = NepaliDateConverter.todayNepaliCalendar

        val weekDayNameInEnglish = NepaliDateConverter.getWeekdayName(
            dayOfWeek = customCalendar.dayOfWeek,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.ENGLISH
        )
        val fullWeekDayNameInNepali = NepaliDateConverter.getWeekdayName(
            dayOfWeek = customCalendar.dayOfWeek,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.NEPALI
        )
        val mediumWeekDayNameInNepali = NepaliDateConverter.getWeekdayName(
            dayOfWeek = customCalendar.dayOfWeek,
            format = NameFormat.MEDIUM,
            language = NepaliDatePickerLang.NEPALI
        )

        val monthNameInEnglish = NepaliDateConverter.getMonthName(
            month = customCalendar.month,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.ENGLISH
        )
        val monthNameInNepali = NepaliDateConverter.getMonthName(
            month = customCalendar.month,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.NEPALI
        )
        val shortMonthNameInNepali = NepaliDateConverter.getMonthName(
            month = customCalendar.month,
            format = NameFormat.SHORT,
            language = NepaliDatePickerLang.NEPALI
        )

        val dayInNepali = customCalendar.dayOfMonth.toString().convertToNepaliNumber()
        val monthInNepali = customCalendar.month.toString().convertToNepaliNumber()
        val yearInNepali =
            customCalendar.year.toString().localizeNumber(NepaliDatePickerLang.NEPALI)

        val englishLocale = NepaliDateLocale()

        val correctFormattedDateInEnglish =
            "$monthNameInEnglish ${customCalendar.dayOfMonth}, ${customCalendar.year}"
        val toTestFormattedDateInEnglish =
            NepaliDateConverter.formatNepaliDate(customCalendar, englishLocale)

        assertEquals(correctFormattedDateInEnglish, toTestFormattedDateInEnglish)

        val fullNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.FULL
        )
        val correctFullNepaliFormattedDate =
            "$fullWeekDayNameInNepali, $monthNameInNepali $dayInNepali, $yearInNepali"
        val toTestFullNepaliFormattedDate =
            NepaliDateConverter.formatNepaliDate(customCalendar, fullNepaliLocale)

        assertEquals(correctFullNepaliFormattedDate, toTestFullNepaliFormattedDate)

        val mixNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.MEDIUM,
            monthName = NameFormat.SHORT
        )
        val correctMixNepaliFormattedDate =
            "$mediumWeekDayNameInNepali, $shortMonthNameInNepali $dayInNepali, $yearInNepali"
        val toTestMixNepaliFormattedDate =
            NepaliDateConverter.formatNepaliDate(customCalendar, mixNepaliLocale)

        assertEquals(correctMixNepaliFormattedDate, toTestMixNepaliFormattedDate)

        val dayInNepaliWithLeadingZero =
            customCalendar.dayOfMonth.toString().padStart(2, '0').convertToNepaliNumber()
        val monthInNepaliWithLeadingZero = customCalendar.month.toString().padStart(2, '0')
            .localizeNumber(NepaliDatePickerLang.NEPALI)

        val compatYear = yearInNepali.slice(2..3)
//         val compatYear = yearInNepali.takeLast(2)
        val compatNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.COMPACT_YMD
        )
        val correctCompatNepaliFormattedDate =
            "$compatYear/$monthInNepaliWithLeadingZero/$dayInNepaliWithLeadingZero"
        val toTestCompatNepaliFormattedDate =
            NepaliDateConverter.formatNepaliDate(customCalendar, compatNepaliLocale)

        assertEquals(correctCompatNepaliFormattedDate, toTestCompatNepaliFormattedDate)
    }

    @Test
    fun formatEnglishDate_RandomDate_GetFormattedDateInStringByLocale() {
        val englishLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.ENGLISH,
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.FULL,
            monthName = NameFormat.FULL
        )

        val nepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.MEDIUM,
            monthName = NameFormat.FULL
        )

        val fullFormatNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.FULL,
            monthName = NameFormat.FULL
        )

        val toTestFormattedDateInEnglish = NepaliDateConverter.formatEnglishDate(
            year = 2024,
            month = 3,
            dayOfMonth = 15,
            dayOfWeek = 3,
            englishLocale
        )
        val formattedDateInEnglish = "Tuesday, March 15, 2024"

        val toTestFormattedDateInNepali = NepaliDateConverter.formatEnglishDate(
            year = 2024,
            month = 6,
            dayOfMonth = 2,
            dayOfWeek = 5,
            nepaliLocale
        )
        val formattedDateInNepali = "२०२४ जुन २"

        val toTestFullFormattedDateInNepali = NepaliDateConverter.formatEnglishDate(
            year = 2024,
            month = 2,
            dayOfMonth = 2,
            dayOfWeek = 5,
            fullFormatNepaliLocale
        )
        val fullFormattedDateInNepali = "बिहिबार, फेब्रुअरी २, २०२४"


        assertEquals(formattedDateInEnglish, toTestFormattedDateInEnglish)
        assertEquals(formattedDateInNepali, toTestFormattedDateInNepali)
        assertEquals(fullFormattedDateInNepali, toTestFullFormattedDateInNepali)
    }

    @Test
    fun formatEnglishDateUsingOtherHelperFunctionOfNames_CustomCalendarDate_GetFormattedDateInString() {
        val todayNepaliDate = NepaliDateConverter.todayNepaliCalendar
        val customCalendar = NepaliDateConverter.convertNepaliToEnglish(
            todayNepaliDate.year,
            todayNepaliDate.month,
            todayNepaliDate.dayOfMonth
        )

        val weekDayNameInEnglish = NepaliDateConverter.getWeekdayName(
            dayOfWeek = customCalendar.dayOfWeek,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.ENGLISH
        )
        val fullWeekDayNameInNepali = NepaliDateConverter.getWeekdayName(
            dayOfWeek = customCalendar.dayOfWeek,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.NEPALI
        )
        val mediumWeekDayNameInNepali = NepaliDateConverter.getWeekdayName(
            dayOfWeek = customCalendar.dayOfWeek,
            format = NameFormat.MEDIUM,
            language = NepaliDatePickerLang.NEPALI
        )

        val monthNameInEnglish = NepaliDateConverter.getEnglishMonthName(
            month = customCalendar.month,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.ENGLISH
        )
        val monthNameInNepali = NepaliDateConverter.getEnglishMonthName(
            month = customCalendar.month,
            format = NameFormat.FULL,
            language = NepaliDatePickerLang.NEPALI
        )
        val shortMonthNameInNepali = NepaliDateConverter.getEnglishMonthName(
            month = customCalendar.month,
            format = NameFormat.SHORT,
            language = NepaliDatePickerLang.NEPALI
        )

        val dayInNepali = customCalendar.dayOfMonth.toString().convertToNepaliNumber()
        val monthInNepali = customCalendar.month.toString().convertToNepaliNumber()
        val yearInNepali =
            customCalendar.year.toString().localizeNumber(NepaliDatePickerLang.NEPALI)

        val englishLocale = NepaliDateLocale()

        val correctFormattedDateInEnglish =
            "$monthNameInEnglish ${customCalendar.dayOfMonth}, ${customCalendar.year}"
        val toTestFormattedDateInEnglish =
            NepaliDateConverter.formatEnglishDate(customCalendar, englishLocale)

        assertEquals(correctFormattedDateInEnglish, toTestFormattedDateInEnglish)

        val fullNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.FULL
        )
        val correctFullNepaliFormattedDate =
            "$fullWeekDayNameInNepali, $monthNameInNepali $dayInNepali, $yearInNepali"
        val toTestFullNepaliFormattedDate =
            NepaliDateConverter.formatEnglishDate(customCalendar, fullNepaliLocale)

        assertEquals(correctFullNepaliFormattedDate, toTestFullNepaliFormattedDate)

        val mixNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.MEDIUM,
            monthName = NameFormat.SHORT
        )
        val correctMixNepaliFormattedDate =
            "$mediumWeekDayNameInNepali, $shortMonthNameInNepali $dayInNepali, $yearInNepali"
        val toTestMixNepaliFormattedDate =
            NepaliDateConverter.formatEnglishDate(customCalendar, mixNepaliLocale)

        assertEquals(correctMixNepaliFormattedDate, toTestMixNepaliFormattedDate)

        val dayInNepaliWithLeadingZero =
            customCalendar.dayOfMonth.toString().padStart(2, '0').convertToNepaliNumber()
        val monthInNepaliWithLeadingZero = customCalendar.month.toString().padStart(2, '0')
            .localizeNumber(NepaliDatePickerLang.NEPALI)

        val compatYear = yearInNepali.slice(2..3)
//         val compatYear = yearInNepali.takeLast(2)
        val compatNepaliLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.COMPACT_YMD
        )
        val correctCompatNepaliFormattedDate =
            "$compatYear/$monthInNepaliWithLeadingZero/$dayInNepaliWithLeadingZero"
        val toTestCompatNepaliFormattedDate =
            NepaliDateConverter.formatEnglishDate(customCalendar, compatNepaliLocale)

        assertEquals(correctCompatNepaliFormattedDate, toTestCompatNepaliFormattedDate)
    }

    @Test
    fun formatTimeInEnglish_RandomSimpleTime_GetFormattedTimeInEnglish() {
        val time = SimpleTime(16, 30, 48, 22)

        val time12Hour = NepaliDateConverter.getFormattedTimeInEnglish(time)
        val correct12HourFormattedTime = "4:30 PM"
        assertEquals(correct12HourFormattedTime, time12Hour)

        val time24Hour = NepaliDateConverter.getFormattedTimeInEnglish(time, false)
        val correct24HourFormattedTime = "16:30"
        assertEquals(correct24HourFormattedTime, time24Hour)
    }

    @Test
    fun formatTimeInNepali_RandomSimpleTime_GetFormattedTimeInEnglish() {
        val time = SimpleTime(16, 30, 48, 22)

        val time12Hour = NepaliDateConverter.getFormattedTimeInNepali(time)
        val correct12HourFormattedTime = "दिउँसो ४ : ३०"
        assertEquals(correct12HourFormattedTime, time12Hour)

        val time24Hour = NepaliDateConverter.getFormattedTimeInNepali(time, false)
        val correct24HourFormattedTime = "१६ : ३०"
        assertEquals(correct24HourFormattedTime, time24Hour)
    }

    @Test
    fun formatTimeToIsoFormat_RandomDates_GetFormattedTimeInIsoFormat() {
        val nepaliDate = SimpleDate(2081, 5, 24)
        val time = SimpleTime(14, 45, 15, 0)

        val nepaliDateIsoFormat =
            NepaliDateConverter.formatNepaliDateTimeToIsoFormat(nepaliDate, time)
        val correctNepaliDateIsoFormat = "2024-09-09T09:00:15Z"
        assertEquals(correctNepaliDateIsoFormat, nepaliDateIsoFormat)

        val englishDate = SimpleDate(2024, 9, 9)
        val englishDateIsoFormat =
            NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(englishDate, time)
        val correctEnglishDateIsoFormat = "2024-09-09T09:00:15Z"
        assertEquals(correctEnglishDateIsoFormat, englishDateIsoFormat)

        assertEquals(nepaliDateIsoFormat, englishDateIsoFormat)
    }

    @Test
    fun convertIsoFormatToNepaliCalendarAndNepaliTime_ISO8601DateTime_GetNepaliCalendarAndNepaliTime() {
        val customDateTimeFromIso =
            NepaliDateConverter.getNepaliDateTimeFromIsoFormat(isoDateTime = "2024-09-09T09:00:15Z")
        val correctNepaliCalendar =
            NepaliDateConverter.getNepaliCalendar(nepaliYYYY = 2081, nepaliMM = 5, nepaliDD = 24)
        val correctNepaliTime = SimpleTime(14, 45, 15, 0)
        assertEquals(correctNepaliCalendar, customDateTimeFromIso.customCalendar)
        assertEquals(correctNepaliTime, customDateTimeFromIso.simpleTime)

        val customDateTimeFromIso2 =
            NepaliDateConverter.getNepaliDateTimeFromIsoFormat(isoDateTime = "2020-08-30T18:43:00.123456789Z")
        val correctNepaliCalendar2 =
            NepaliDateConverter.getNepaliCalendar(nepaliYYYY = 2077, nepaliMM = 5, nepaliDD = 15)
        val correctNepaliTime2 = SimpleTime(0, 28, 0, 123456789)
        assertEquals(correctNepaliCalendar2, customDateTimeFromIso2.customCalendar)
        assertEquals(correctNepaliTime2, customDateTimeFromIso2.simpleTime)
    }

    @Test
    fun convertIsoFormatToEnglishCalendarAndNepaliTime_ISO8601DateTime_GetEnglishCalendarAndNepaliTime() {
        val customDateTimeFromIso =
            NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime = "2024-09-09T09:00:15Z")
        val correctNepaliCalendar =
            NepaliDateConverter.getNepaliCalendar(nepaliYYYY = 2081, nepaliMM = 5, nepaliDD = 24)
        val correctEnglishCalendar = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = correctNepaliCalendar.year,
            nepaliMM = correctNepaliCalendar.month,
            nepaliDD = correctNepaliCalendar.dayOfMonth
        )
        val correctNepaliTime = SimpleTime(14, 45, 15, 0)
        assertEquals(correctEnglishCalendar, customDateTimeFromIso.customCalendar)
        assertEquals(correctNepaliTime, customDateTimeFromIso.simpleTime)

        val customDateTimeFromIso2 =
            NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime = "2020-01-01T23:59:59.123456789+01")
        val correctNepaliCalendar2 =
            NepaliDateConverter.getNepaliCalendar(nepaliYYYY = 2076, nepaliMM = 9, nepaliDD = 17)
        val correctEnglishCalendar2 = NepaliDateConverter.convertNepaliToEnglish(
            nepaliYYYY = correctNepaliCalendar2.year,
            nepaliMM = correctNepaliCalendar2.month,
            nepaliDD = correctNepaliCalendar2.dayOfMonth
        )
        val correctNepaliTime2 = SimpleTime(4, 44, 59, 123456789)
        assertEquals(correctEnglishCalendar2, customDateTimeFromIso2.customCalendar)
        assertEquals(correctNepaliTime2, customDateTimeFromIso2.simpleTime)
    }

    @Test
    fun formatAndCompareTimeToIsoFormat_TodayEnglishAndNepaliDate_GetSameFormattedTimeInIsoFormat() {
        val time = SimpleTime(14, 30, 15, 0)

        val nepaliDate = NepaliDateConverter.todayNepaliCalendar.toSimpleDate()
        val nepaliDateIsoFormat =
            NepaliDateConverter.formatNepaliDateTimeToIsoFormat(nepaliDate, time)

        val englishDate = NepaliDateConverter.todayEnglishSimpleDate
        val englishDateIsoFormat =
            NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(englishDate, time)

        assertEquals(nepaliDateIsoFormat, englishDateIsoFormat)
    }

    @Test
    fun replaceDelimitersOfString_RandomDateStringWithDelimiter_GetReplacedStringWithDelimiter() {
        val originalDate = "2024/06/21"
        val newDelimiter = "-"
        val formattedDate = NepaliDateConverter.replaceDelimiter(originalDate, newDelimiter)
        val correctFormattedDate = "2024-06-21"
        assertEquals(correctFormattedDate, formattedDate)

        val originalNepaliDate = "२०२४/०६/२१"
        val formattedNepaliDate =
            NepaliDateConverter.replaceDelimiter(originalNepaliDate, newDelimiter)
        val correctFormattedNepaliDate = "२०२४-०६-२१"
        assertEquals(correctFormattedNepaliDate, formattedNepaliDate)

        val originalTime = "09:45 AM"
        val newDelimiterSpace = " "
        val oldDelimiter = ":"
        val formattedTimeWithSpace =
            NepaliDateConverter.replaceDelimiter(originalTime, newDelimiterSpace, oldDelimiter)
        val correctedFormattedTimeWithSpace = "09 45 AM"
        assertEquals(correctedFormattedTimeWithSpace, formattedTimeWithSpace)
    }
}