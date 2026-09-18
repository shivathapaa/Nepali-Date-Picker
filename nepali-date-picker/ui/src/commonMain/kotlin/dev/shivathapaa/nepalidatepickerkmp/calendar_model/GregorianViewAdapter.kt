/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
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
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/**
 * The [CalendarViewAdapter] that puts the Gregorian calendar on screen.
 *
 * Every cell still resolves to a Bikram Sambat [CalendarDay.canonical], so selection stays in that
 * calendar whichever one is displayed. Days before the conversion anchor (English 1913-04-13) have no
 * canonical date and are therefore inert.
 */
@Immutable
internal class GregorianViewAdapter(
    private val calendarModel: NepaliCalendarModel,
    override val yearRange: IntRange
) : CalendarViewAdapter {

    override val calendarSystem = CalendarSystem.GREGORIAN

    override val locale: NepaliDateLocale get() = calendarModel.locale

    override fun monthAt(index: Int): MonthCalendar =
        monthOf(yearRange.first + index / MonthsInYear, index % MonthsInYear + 1)

    override fun monthOf(year: Int, month: Int): MonthCalendar =
        calendarModel.getEnglishMonth(
            englishYear = year.coerceIn(yearRange),
            englishMonth = month.coerceIn(1, MonthsInYear)
        )

    override fun monthContaining(canonicalDate: SimpleDate): MonthCalendar {
        // The date can arrive straight from a caller, since the wheel picker has no state holder to
        // coerce it first, so it is pulled inside the table rather than left to throw.
        val inTable = calendarModel.coerceIntoConversionTable(canonicalDate)
        val english = calendarModel.convertToEnglishDate(
            nepaliYYYY = inTable.year,
            nepaliMM = inTable.month,
            nepaliDD = inTable.dayOfMonth
        )
        // The Bikram Sambat table outlives the convertible English range at both ends, so a date at
        // the very edge can map to a year the pager does not cover. Clamp to the nearest edge month
        // rather than producing an index the pager cannot scroll to.
        return when {
            english.year < yearRange.first -> monthOf(yearRange.first, 1)
            english.year > yearRange.last -> monthOf(yearRange.last, MonthsInYear)
            else -> monthOf(english.year, english.month)
        }
    }

    // The Bikram Sambat half is the canonical date here, so it is resolved either way and
    // [withSecondary] costs nothing extra.
    override fun daysIn(month: MonthCalendar, withSecondary: Boolean): List<CalendarDay> {
        val englishDays = calendarModel.getEnglishCalendarsInMonth(month.year, month.month)
        val nepaliDays = calendarModel.getNepaliCalendarsInEnglishMonth(month.year, month.month)
        return List(month.totalDaysInMonth) { index ->
            val nepaliDay = nepaliDays.getOrNull(index)
            CalendarDay(
                displayed = englishDays[index],
                canonical = nepaliDay,
                secondary = nepaliDay
            )
        }
    }

    override fun monthName(month: Int, language: NepaliDatePickerLang, format: NameFormat): String =
        calendarModel.getEnglishMonthName(month = month, language = language, format = format)

    override fun canonicalYearsIn(year: Int): List<Int> {
        // Bikram Sambat is a fixed 56 or 57 years ahead: its new year falls in mid-April, so one
        // Gregorian year always ends one Bikram Sambat year and starts the next. The offset is
        // derived from the conversion anchors rather than hardcoded.
        val offset = NepaliCalendarDefaults.NepaliYearRange.first -
                NepaliCalendarDefaults.EnglishYearRange.first
        return listOf(year + offset - 1, year + offset)
            .filter { it in NepaliCalendarDefaults.NepaliYearRange }
    }

    override fun format(displayedDate: CustomCalendar, locale: NepaliDateLocale): String =
        calendarModel.formatEnglishDate(
            year = displayedDate.year,
            month = displayedDate.month,
            dayOfMonth = displayedDate.dayOfMonth,
            dayOfWeek = displayedDate.dayOfWeek,
            locale = locale
        )

    override fun parse(dateString: String): CustomCalendar? =
        calendarModel.parseEnglish(dateString)

    override fun calendarOf(date: SimpleDate): CustomCalendar? =
        runCatching { calendarModel.getEnglishCalendar(date) }.getOrNull()

    override fun toCanonical(displayedDate: CustomCalendar): CustomCalendar? {
        if (!calendarModel.isEnglishDateConvertible(
                displayedDate.year, displayedDate.month, displayedDate.dayOfMonth
            )
        ) {
            return null
        }
        return calendarModel.convertToNepaliCalendar(
            englishYYYY = displayedDate.year,
            englishMM = displayedDate.month,
            englishDD = displayedDate.dayOfMonth
        )
    }

    override fun fromCanonical(canonicalDate: CustomCalendar): CustomCalendar? = runCatching {
        calendarModel.convertToEnglishDate(
            nepaliYYYY = canonicalDate.year,
            nepaliMM = canonicalDate.month,
            nepaliDD = canonicalDate.dayOfMonth
        )
    }.getOrNull()

    override fun dateFieldLabel(language: NepaliDatePickerLang): String = language.englishDate
}
