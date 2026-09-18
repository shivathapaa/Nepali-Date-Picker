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
import dev.shivathapaa.nepalidatepickerkmp.data.toMonthCalendar

/**
 * The [CalendarViewAdapter] that puts Bikram Sambat on screen, which is what the pickers show unless
 * asked otherwise. Displayed and canonical dates are the same thing here, so every cell converts to
 * itself.
 */
@Immutable
internal class BikramSambatViewAdapter(
    private val calendarModel: NepaliCalendarModel,
    override val yearRange: IntRange
) : CalendarViewAdapter {

    override val calendarSystem = CalendarSystem.BIKRAM_SAMBAT

    override val locale: NepaliDateLocale get() = calendarModel.locale

    override fun monthAt(index: Int): MonthCalendar =
        monthOf(yearRange.first + index / MonthsInYear, index % MonthsInYear + 1)

    override fun monthOf(year: Int, month: Int): MonthCalendar =
        calendarModel.getNepaliMonth(
            nepaliYear = year.coerceIn(yearRange),
            nepaliMonth = month.coerceIn(1, MonthsInYear)
        ).toMonthCalendar()

    override fun monthContaining(canonicalDate: SimpleDate): MonthCalendar =
        monthOf(canonicalDate.year, canonicalDate.month)

    override fun daysIn(month: MonthCalendar, withSecondary: Boolean): List<CalendarDay> {
        val englishDays = if (withSecondary) {
            calendarModel.getEnglishCalendarsInNepaliMonth(month.year, month.month)
        } else {
            emptyList()
        }
        return List(month.totalDaysInMonth) { index ->
            val nepaliDay = calendarModel.getNepaliCalendar(
                SimpleDate(month.year, month.month, index + 1)
            )
            CalendarDay(
                displayed = nepaliDay,
                canonical = nepaliDay,
                secondary = englishDays.getOrNull(index)
            )
        }
    }

    override fun monthName(month: Int, language: NepaliDatePickerLang, format: NameFormat): String =
        calendarModel.getNepaliMonthName(monthOfYear = month, format = format, language = language)

    override fun canonicalYearsIn(year: Int): List<Int> = listOf(year)

    override fun format(displayedDate: CustomCalendar, locale: NepaliDateLocale): String =
        calendarModel.formatNepaliDate(
            year = displayedDate.year,
            month = displayedDate.month,
            dayOfMonth = displayedDate.dayOfMonth,
            dayOfWeek = displayedDate.dayOfWeek,
            locale = locale
        )

    override fun parse(dateString: String): CustomCalendar? = calendarModel.parse(dateString)

    override fun calendarOf(date: SimpleDate): CustomCalendar? =
        runCatching { calendarModel.getNepaliCalendar(date) }.getOrNull()

    override fun toCanonical(displayedDate: CustomCalendar): CustomCalendar = displayedDate

    override fun fromCanonical(canonicalDate: CustomCalendar): CustomCalendar = canonicalDate

    override fun dateFieldLabel(language: NepaliDatePickerLang): String = language.nepaliDate
}
