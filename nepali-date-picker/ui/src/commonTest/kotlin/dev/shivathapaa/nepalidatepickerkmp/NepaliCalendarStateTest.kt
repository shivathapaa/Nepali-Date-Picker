/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.runtime.saveable.SaverScope
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val EnglishLocale = NepaliDateLocale()

class NepaliCalendarStateTest {

    private fun stateOf(
        selected: SimpleDate? = null,
        displayed: SimpleDate? = selected,
        yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) = NepaliCalendarStateImpl(
        initialSelectedDate = selected,
        initialDisplayedMonth = displayed,
        yearRange = yearRange,
        locale = EnglishLocale,
        initialCalendarSystem = calendarSystem
    )

    @Test
    fun initialSelection_outsideTheYearRange_readsAsNoSelection() {
        val state = stateOf(selected = SimpleDate(2000, 5, 12), yearRange = 2080..2090)

        assertNull(state.selectedDate)
    }

    @Test
    fun initialSelection_thatIsNotARealDay_readsAsNoSelection() {
        val state = stateOf(selected = SimpleDate(2082, 13, 45))

        assertNull(state.selectedDate)
    }

    @Test
    fun assigningAnOutOfRangeDate_clearsTheSelection() {
        val state = stateOf(selected = SimpleDate(2082, 6, 3), yearRange = 2080..2090)
        val outOfRange = state.selectedDate?.copy(year = 2099)

        state.selectedDate = outOfRange

        assertNull(state.selectedDate)
    }

    @Test
    fun displayedMonth_isClampedIntoTheYearRange() {
        val state = stateOf(displayed = SimpleDate(2099, 3, 1), yearRange = 2080..2085)

        assertTrue(state.displayedMonth.year in 2080..2085)
    }

    @Test
    fun selectedEnglishDate_followsTheSelection() {
        val state = stateOf(selected = SimpleDate(2082, 6, 3))

        val english = state.selectedEnglishDate

        assertEquals(CalendarSystem.GREGORIAN.era, english?.era)
        state.selectedDate = null
        assertNull(state.selectedEnglishDate)
    }

    @Test
    fun switchingCalendar_keepsTheSelectionAndShowsItsMonth() {
        val selected = SimpleDate(2082, 6, 3)
        val state = stateOf(selected = selected)
        val before = state.selectedDate

        state.displayedCalendarSystem = CalendarSystem.GREGORIAN

        assertEquals(before, state.selectedDate)
        assertEquals(CalendarSystem.GREGORIAN, state.displayedMonthCalendar.calendarSystem)
        // The Gregorian month on screen is the one the selected Bikram Sambat day falls in.
        val englishDate = state.selectedEnglishDate
        assertEquals(englishDate?.year, state.displayedMonthCalendar.year)
        assertEquals(englishDate?.month, state.displayedMonthCalendar.month)
    }

    @Test
    fun switchingCalendarBackAndForth_staysOnTheSameMonth() {
        val state = stateOf(displayed = SimpleDate(2082, 6, 1))
        val before = state.displayedMonth

        state.displayedCalendarSystem = CalendarSystem.GREGORIAN
        state.displayedCalendarSystem = CalendarSystem.BIKRAM_SAMBAT

        assertEquals(before, state.displayedMonth)
    }

    @Test
    fun saver_roundTripsSelectionMonthAndCalendar() {
        val state = stateOf(selected = SimpleDate(2082, 6, 3), yearRange = 2080..2090)
        state.displayedCalendarSystem = CalendarSystem.GREGORIAN
        val saver = NepaliCalendarStateImpl.Saver(EnglishLocale)

        val saved = with(saver) { SaverScope { true }.save(state) }
        val restored = saver.restore(requireNotNull(saved))

        assertEquals(state.selectedDate?.toSimpleDate(), restored?.selectedDate?.toSimpleDate())
        assertEquals(state.displayedMonth, restored?.displayedMonth)
        assertEquals(CalendarSystem.GREGORIAN, restored?.displayedCalendarSystem)
        assertEquals(2080..2090, restored?.yearRange)
    }

    @Test
    fun saver_writesOnlySelectionMonthAndRange() {
        val state = stateOf(selected = SimpleDate(2082, 6, 3))
        val saver = NepaliCalendarStateImpl.Saver(EnglishLocale)

        val saved = with(saver) { SaverScope { true }.save(state) } as List<*>

        assertEquals(6, saved.size)
        assertEquals(listOf("2082,6,3", 2082, 6, CalendarSystem.BIKRAM_SAMBAT.era), saved.take(4))
    }

    @Test
    fun saver_restoresAGregorianViewOnTheSameGregorianMonth() {
        val state = stateOf(selected = SimpleDate(2082, 6, 3))
        state.displayedCalendarSystem = CalendarSystem.GREGORIAN
        val saver = NepaliCalendarStateImpl.Saver(EnglishLocale)

        val saved = with(saver) { SaverScope { true }.save(state) }
        val restored = saver.restore(requireNotNull(saved))

        assertEquals(state.displayedMonthCalendar, restored?.displayedMonthCalendar)
        assertEquals(state.displayedMonth, restored?.displayedMonth)
    }

    @Test
    fun saver_restoresAnEmptySelection() {
        val state = stateOf()
        val saver = NepaliCalendarStateImpl.Saver(EnglishLocale)

        val saved = with(saver) { SaverScope { true }.save(state) }
        val restored = saver.restore(requireNotNull(saved))

        assertNull(restored?.selectedDate)
        assertEquals(state.displayedMonth, restored?.displayedMonth)
    }
}
