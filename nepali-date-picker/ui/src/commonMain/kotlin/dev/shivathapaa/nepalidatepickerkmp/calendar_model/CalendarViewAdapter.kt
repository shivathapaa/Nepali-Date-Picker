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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate

/**
 * One cell of a month grid, resolved in every calendar the cell has to speak.
 *
 * @property displayed the date as the active calendar writes it. Drives the large number.
 * @property canonical the Bikram Sambat date this cell selects, or `null` when the displayed day
 *   predates the conversion anchor and therefore cannot be selected. Selection, "today" and the
 *   selectable-date predicate all run on this, so they mean the same thing in either calendar.
 * @property secondary the same day in the other calendar. Drives the small number in the dual-date
 *   cell, and equals [canonical] whenever the active calendar is Gregorian.
 */
@Immutable
internal data class CalendarDay(
    val displayed: CustomCalendar,
    val canonical: CustomCalendar?,
    val secondary: CustomCalendar?
)

/**
 * Everything the month grid, the month pager and the navigation header need from a calendar.
 *
 * The pickers talk to this rather than to [NepaliCalendarModel] directly, so putting a different
 * calendar on screen is a matter of choosing a different implementation. Selection never changes
 * calendar: whatever is displayed, a picked cell is stored as its [CalendarDay.canonical] Bikram
 * Sambat date, which is what makes switching lossless.
 */
@Immutable
internal interface CalendarViewAdapter {

    /** The calendar this adapter puts on screen. */
    val calendarSystem: CalendarSystem

    /** Years the pager and the year picker cover, expressed in [calendarSystem]. */
    val yearRange: IntRange

    /** The locale the underlying [NepaliCalendarModel] was built with. */
    val locale: NepaliDateLocale

    /** The month at [index] in the pager, counting twelve months per year from [yearRange]. */
    fun monthAt(index: Int): MonthCalendar

    /** The month [month] of [year] in [calendarSystem]. */
    fun monthOf(year: Int, month: Int): MonthCalendar

    /**
     * The displayed month holding [canonicalDate], clamped into [yearRange] so the result is always
     * a valid pager position.
     */
    fun monthContaining(canonicalDate: SimpleDate): MonthCalendar

    /**
     * The days of [month], in day order.
     *
     * [withSecondary] fills in [CalendarDay.secondary]. It defaults to off because resolving it
     * costs a conversion pass the single-calendar grid would never read.
     */
    fun daysIn(month: MonthCalendar, withSecondary: Boolean = false): List<CalendarDay>

    /** Localized name of [month] (1-12) in [calendarSystem]. */
    fun monthName(month: Int, language: NepaliDatePickerLang, format: NameFormat): String

    /**
     * The Bikram Sambat years a [calendarSystem] year overlaps.
     *
     * The year picker gates a year on `NepaliSelectableDates.isSelectableYear`, which speaks Bikram
     * Sambat. A Gregorian year straddles two of them, so it stays selectable if either one is.
     */
    fun canonicalYearsIn(year: Int): List<Int>

    /** Formats a date already expressed in [calendarSystem]. */
    fun format(displayedDate: CustomCalendar, locale: NepaliDateLocale): String

    /**
     * Parses an eight-digit `yyyyMMdd` string as a date in [calendarSystem].
     *
     * Returns `null` when the string is not a date at all. A syntactically valid date that does not
     * exist comes back with `totalDaysInMonth = -1`, so the typed-input validator can tell the two
     * apart and say "day is invalid" rather than "not a date".
     */
    fun parse(dateString: String): CustomCalendar?

    /**
     * [date], read in [calendarSystem], as a fully populated [CustomCalendar], or `null` when it is
     * not a real day in this calendar.
     */
    fun calendarOf(date: SimpleDate): CustomCalendar?

    /** The Bikram Sambat date [displayedDate] resolves to, or `null` when it has none. */
    fun toCanonical(displayedDate: CustomCalendar): CustomCalendar?

    /** [canonicalDate] expressed in [calendarSystem], or `null` when it cannot be. */
    fun fromCanonical(canonicalDate: CustomCalendar): CustomCalendar?

    /** Label for a typed date field in [calendarSystem] (for example "Nepali Date"). */
    fun dateFieldLabel(language: NepaliDatePickerLang): String
}

/** Remembers the adapter for [calendarSystem]; [nepaliYearRange] is always the Bikram Sambat range. */
@Composable
internal fun rememberCalendarViewAdapter(
    calendarSystem: CalendarSystem,
    calendarModel: NepaliCalendarModel,
    nepaliYearRange: IntRange
): CalendarViewAdapter = remember(calendarSystem, calendarModel, nepaliYearRange) {
    calendarViewAdapter(calendarSystem, calendarModel, nepaliYearRange)
}

/**
 * The adapter for [calendarSystem]. [nepaliYearRange] is the Bikram Sambat range the picker was
 * configured with; a Gregorian adapter derives its own range from it so both calendars page over
 * the same span of real days.
 */
internal fun calendarViewAdapter(
    calendarSystem: CalendarSystem,
    calendarModel: NepaliCalendarModel,
    nepaliYearRange: IntRange
): CalendarViewAdapter {
    // A caller's range can reach past the conversion table, and every month lookup below would
    // throw on a year the table has no row for. Clamping once here covers the pager, the year
    // picker and the wheel, none of which coerce the range themselves.
    val supportedYearRange = IntRange(
        nepaliYearRange.first.coerceIn(NepaliCalendarDefaults.NepaliYearRange),
        nepaliYearRange.last.coerceIn(NepaliCalendarDefaults.NepaliYearRange)
    )
    return when (calendarSystem) {
        CalendarSystem.BIKRAM_SAMBAT -> BikramSambatViewAdapter(calendarModel, supportedYearRange)
        CalendarSystem.GREGORIAN -> GregorianViewAdapter(
            calendarModel = calendarModel,
            yearRange = NepaliCalendarDefaults.gregorianYearRangeFor(supportedYearRange)
        )
    }
}

/**
 * The month label shown under the year button: the months of the *other* calendar that this month
 * straddles, with their year. Reads "Sep/Oct 2026" under a Bikram Sambat month and
 * "Bhadra/Ashoj 2083" under a Gregorian one.
 *
 * Returns a single month name when the straddled months happen to coincide, and `null` when the
 * month has no counterpart at all (only possible at the very start of the conversion table).
 */
internal fun CalendarViewAdapter.secondaryMonthLabel(
    days: List<CalendarDay>,
    calendarModel: NepaliCalendarModel,
    language: NepaliDatePickerLang,
    format: NameFormat = NameFormat.SHORT
): String? {
    val first = days.firstNotNullOfOrNull { it.secondary } ?: return null
    val last = days.lastOrNull { it.secondary != null }?.secondary ?: first

    // The secondary calendar is whichever one is not on screen.
    val names = when (calendarSystem) {
        CalendarSystem.BIKRAM_SAMBAT -> language.englishMonths
        CalendarSystem.GREGORIAN -> language.months
    }
    val firstName = names[first.month - 1].let { if (format == NameFormat.SHORT) it.short else it.full }
    val lastName = names[last.month - 1].let { if (format == NameFormat.SHORT) it.short else it.full }
    val year = calendarModel.localizeNumber(last.year.toString(), language)

    return if (first.month == last.month) "$firstName $year" else "$firstName/$lastName $year"
}

/** [canonicalDate] pulled inside the supported Bikram Sambat conversion table. */
internal fun NepaliCalendarModel.coerceIntoConversionTable(canonicalDate: SimpleDate): SimpleDate {
    val year = canonicalDate.year.coerceIn(NepaliCalendarDefaults.NepaliYearRange)
    val month = canonicalDate.month.coerceIn(1, MonthsInYear)
    val dayOfMonth = canonicalDate.dayOfMonth
        .coerceIn(1, getTotalDaysInNepaliMonth(year, month))
    return SimpleDate(year, month, dayOfMonth)
}

/** The Bikram Sambat date a cell selects, as a [SimpleDate], or `null` when it has none. */
internal fun CalendarDay.canonicalDate(): SimpleDate? = canonical?.toSimpleDate()

internal const val MonthsInYear = 12
