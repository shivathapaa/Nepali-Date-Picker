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

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate

/**
 * What a [NepaliCalendar] is showing and what is picked on it.
 *
 * A calendar browses rather than asks, so this is deliberately smaller than
 * [NepaliDatePickerState]: there is no typed-input mode, and no selectable-date rule, because a
 * calendar marks days rather than refusing them. Events are not held here either; they arrive with
 * the [dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy] each surface is given, so the
 * same state can drive a grid and a list over different policies.
 *
 * Selection is optional and always Bikram Sambat, whichever calendar is displayed, which is what
 * makes [displayedCalendarSystem] a display choice rather than a conversion.
 */
@Stable
@ExperimentalNepaliDatePickerApi
interface NepaliCalendarState {

    /** The picked day, or `null` when nothing is picked. Always a Bikram Sambat date. */
    var selectedDate: CustomCalendar?

    /** [selectedDate] in the Gregorian calendar, or `null` when nothing is picked. */
    val selectedEnglishDate: CustomCalendar?

    /**
     * The Bikram Sambat month the grid is showing.
     *
     * While [displayedCalendarSystem] is [CalendarSystem.GREGORIAN] the grid pages over Gregorian
     * months, and this reports the Bikram Sambat month holding the first day of the visible one.
     * Use [displayedMonthCalendar] for the month exactly as displayed.
     */
    var displayedMonth: NepaliMonthCalendar

    /** The month the grid is showing, in [displayedCalendarSystem]. */
    var displayedMonthCalendar: MonthCalendar

    /**
     * Which calendar the grid shows. Switching keeps [selectedDate] as it is and re-anchors the grid
     * on the selected day, or on the visible month when nothing is picked.
     */
    var displayedCalendarSystem: CalendarSystem

    /** Bikram Sambat years the calendar pages over. */
    val yearRange: IntRange

    /** The Gregorian years [yearRange] covers, for the Gregorian view of the same span of days. */
    val englishYearRange: IntRange

    /** The language, date format and digit script the calendar renders with. */
    val locale: NepaliDateLocale
}

/**
 * Creates a [NepaliCalendarState] remembered across compositions and restored after configuration
 * changes. Only the selection, the displayed month and the displayed calendar are saved; events
 * belong to the policy a surface is given, and are never stored here.
 *
 * Out-of-range or invalid initial values are coerced rather than rejected: the displayed month is
 * clamped into [yearRange], and an out-of-range or non-existent initial selected date resolves to no
 * selection.
 *
 * @param initialSelectedDate the day picked when the calendar first appears, or `null` for none.
 * @param initialDisplayedMonth the month shown first. Defaults to the month of
 *   [initialSelectedDate], and to the current month when there is no selection.
 * @param yearRange the Bikram Sambat years the calendar pages over.
 * @param locale the language, date format and digit script the calendar renders with.
 * @param initialCalendarSystem the calendar shown first.
 *
 * Example usage:
 * ```
 * val state = rememberNepaliCalendarState()
 * NepaliCalendar(state = state, policy = officePolicy)
 * NepaliMonthEventList(state = state, policy = officePolicy)
 * ```
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun rememberNepaliCalendarState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
): NepaliCalendarState = rememberSaveable(saver = NepaliCalendarStateImpl.Saver(locale)) {
    NepaliCalendarStateImpl(
        initialSelectedDate = initialSelectedDate,
        initialDisplayedMonth = initialDisplayedMonth,
        yearRange = yearRange,
        locale = locale,
        initialCalendarSystem = initialCalendarSystem
    )
}

/**
 * Creates a [NepaliCalendarState] outside composition, which is what a view model or a platform host
 * holds. Prefer [rememberNepaliCalendarState] inside a composition, since only that one is restored
 * after a configuration change.
 *
 * Coerces the same way [rememberNepaliCalendarState] does.
 *
 * @see rememberNepaliCalendarState
 */
@ExperimentalNepaliDatePickerApi
fun NepaliCalendarState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
): NepaliCalendarState = NepaliCalendarStateImpl(
    initialSelectedDate = initialSelectedDate,
    initialDisplayedMonth = initialDisplayedMonth,
    yearRange = yearRange,
    locale = locale,
    initialCalendarSystem = initialCalendarSystem
)

/**
 * The calendar's own state over the shared picker base, which already carries the displayed month in
 * both calendars, the anchor a calendar switch re-centres on, and the year-range clamping.
 */
@Stable
internal class NepaliCalendarStateImpl(
    initialSelectedDate: SimpleDate?,
    initialDisplayedMonth: SimpleDate?,
    yearRange: IntRange,
    locale: NepaliDateLocale,
    initialCalendarSystem: CalendarSystem
) : BaseNepaliDatePickerStateImpl(
    initialDisplayedMonth = initialDisplayedMonth,
    initialCalendarSystem = initialCalendarSystem,
    yearRange = yearRange,
    nepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale = locale
), NepaliCalendarState {

    // An initial date the conversion table cannot answer for, or one outside the range the calendar
    // pages over, reads as no selection rather than failing the whole calendar.
    private var _selectedDate = mutableStateOf(
        initialSelectedDate?.let {
            runCatching { calendarModel.getNepaliCalendar(simpleNepaliDate = it) }
                .getOrNull()
                ?.takeIf { date -> yearRange.contains(date.year) }
        }
    )

    override var selectedDate: CustomCalendar?
        get() = _selectedDate.value
        set(customCalendar) {
            _selectedDate.value = customCalendar?.takeIf { yearRange.contains(it.year) }
        }

    override val selectedEnglishDate: CustomCalendar?
        get() = selectedDate?.let {
            calendarModel.convertToEnglishDate(
                nepaliYYYY = it.year,
                nepaliMM = it.month,
                nepaliDD = it.dayOfMonth
            )
        }

    // Keep the picked day in view across a calendar switch, and fall back to the visible month only
    // when nothing is picked.
    override fun canonicalAnchor(): SimpleDate =
        selectedDate?.toSimpleDate() ?: super.canonicalAnchor()

    override var displayedCalendarSystem: CalendarSystem
        get() = super<BaseNepaliDatePickerStateImpl>.displayedCalendarSystem
        set(value) {
            super<BaseNepaliDatePickerStateImpl>.displayedCalendarSystem = value
        }

    override var displayedMonthCalendar: MonthCalendar
        get() = super<BaseNepaliDatePickerStateImpl>.displayedMonthCalendar
        set(value) {
            super<BaseNepaliDatePickerStateImpl>.displayedMonthCalendar = value
        }

    override val englishYearRange: IntRange
        get() = super<BaseNepaliDatePickerStateImpl>.englishYearRange

    companion object {
        /**
         * Saves what the user is looking at and what they picked, and nothing else. Events are the
         * policy's, so restoring a calendar never resurrects a stale copy of them.
         *
         * The month is stored as it is displayed, calendar and all, rather than as its Bikram
         * Sambat projection: one Gregorian month spans two Bikram Sambat ones, and the projection
         * can only name the first of them, so restoring from it would walk the grid a month
         * backwards every time the screen was rebuilt.
         */
        fun Saver(locale: NepaliDateLocale): Saver<NepaliCalendarStateImpl, Any> = listSaver(
            save = { state ->
                val month = state.displayedMonthCalendar
                listOf(
                    state.selectedDate?.encodeToSimpleDateString(),
                    month.year,
                    month.month,
                    // Stored as the era rather than the ordinal so reordering the enum cannot
                    // silently restore the wrong calendar.
                    month.calendarSystem.era,
                    state.yearRange.first,
                    state.yearRange.last
                )
            },
            restore = { value ->
                val calendarSystem = decodeCalendarSystem(value[3])
                NepaliCalendarStateImpl(
                    initialSelectedDate = decodeSimpleDateFromString(value[0] as? String),
                    initialDisplayedMonth = canonicalFirstDayOf(
                        calendarSystem = calendarSystem,
                        year = value[1] as Int,
                        month = value[2] as Int
                    ),
                    yearRange = IntRange(value[4] as Int, value[5] as Int),
                    locale = locale,
                    initialCalendarSystem = calendarSystem
                )
            }
        )

        /**
         * The Bikram Sambat date the saved month is restored through: day one of the month for a
         * Bikram Sambat month, and the Bikram Sambat day the Gregorian month opens on for a
         * Gregorian one. `null` when the Gregorian month predates the conversion table, which
         * restores the calendar on the current month rather than failing.
         */
        private fun canonicalFirstDayOf(
            calendarSystem: CalendarSystem,
            year: Int,
            month: Int
        ): SimpleDate? = when (calendarSystem) {
            CalendarSystem.BIKRAM_SAMBAT -> SimpleDate(year, month, 1)
            CalendarSystem.GREGORIAN -> runCatching {
                NepaliDateConverter.convertEnglishToNepali(
                    englishYYYY = year,
                    englishMM = month,
                    englishDD = 1
                ).toSimpleDate()
            }.getOrNull()
        }
    }
}
