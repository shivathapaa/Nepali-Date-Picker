/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.CalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.isInSelectedRange
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.monthGrid
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Which day cells of a range picker count as inside the selection.
 *
 * The answer drives the in-range content colour, so a predicate that is true everywhere paints the
 * whole grid as selected. The endpoints of the selection are what a day is measured against, not
 * the bounds of the conversion table, which every day in the table trivially satisfies.
 */
class SelectedRangeMembershipTest {

    private val calendarModel = NepaliCalendarModel(TestLocale)

    private fun inRange(
        day: CustomCalendar?,
        start: CustomCalendar?,
        end: CustomCalendar?
    ): Boolean = isInSelectedRange(day, start, end) { date, year, month, dayOfMonth ->
        calendarModel.compareDates(date, year, month, dayOfMonth)
    }

    private fun CalendarViewAdapter.dateOf(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): CustomCalendar = assertNotNull(calendarOf(SimpleDate(year, month, dayOfMonth)))

    @Test
    fun aDayInsideTheRange_isInRange() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2083, 4, 8)
        val end = adapter.dateOf(2083, 4, 20)
        assertTrue(inRange(adapter.dateOf(2083, 4, 14), start, end))
    }

    @Test
    fun bothEndpoints_areInRange() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2083, 4, 8)
        val end = adapter.dateOf(2083, 4, 20)
        assertTrue(inRange(start, start, end))
        assertTrue(inRange(end, start, end))
    }

    @Test
    fun aDayOutsideTheRange_isNotInRange() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2083, 4, 8)
        val end = adapter.dateOf(2083, 4, 20)
        assertFalse(inRange(adapter.dateOf(2083, 4, 7), start, end))
        assertFalse(inRange(adapter.dateOf(2083, 4, 21), start, end))
        assertFalse(inRange(adapter.dateOf(2083, 3, 25), start, end))
        assertFalse(inRange(adapter.dateOf(2083, 5, 2), start, end))
    }

    @Test
    fun aSingleDayRange_containsOnlyThatDay() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val day = adapter.dateOf(2083, 4, 8)
        assertTrue(inRange(day, day, day))
        assertFalse(inRange(adapter.dateOf(2083, 4, 9), day, day))
    }

    @Test
    fun aHalfFinishedSelection_containsNothing() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2083, 4, 8)
        assertFalse(inRange(adapter.dateOf(2083, 4, 14), start, null))
        assertFalse(inRange(adapter.dateOf(2083, 4, 14), null, start))
        assertFalse(inRange(adapter.dateOf(2083, 4, 14), null, null))
    }

    @Test
    fun aCellWithNoBikramSambatDate_isNeverInRange() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2083, 4, 8)
        val end = adapter.dateOf(2083, 4, 20)
        assertFalse(inRange(null, start, end))
    }

    @Test
    fun aRangeSpanningYears_holdsItsInteriorAndRejectsEitherSide() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2082, 12, 20)
        val end = adapter.dateOf(2083, 1, 10)
        assertTrue(inRange(adapter.dateOf(2082, 12, 29), start, end))
        assertTrue(inRange(adapter.dateOf(2083, 1, 1), start, end))
        assertFalse(inRange(adapter.dateOf(2082, 12, 19), start, end))
        assertFalse(inRange(adapter.dateOf(2083, 1, 11), start, end))
    }

    /**
     * The bug this replaced: comparing against the conversion table's first and last date makes
     * every day in the table in-range, so a range picker paints its whole grid as selected.
     */
    @Test
    fun theWholeGridIsNotInRange_whenTheSelectionCoversOnlyPartOfTheMonth() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val month = adapter.monthOf(2083, 4)
        val start = adapter.dateOf(2083, 4, 8)
        val end = adapter.dateOf(2083, 4, 20)

        val cells = adapter.monthGrid(month, withAdjacentDays = true)
        val drawnDays = cells.mapNotNull { it?.day?.canonical }
        val inRangeCount = drawnDays.count { inRange(it, start, end) }

        assertEquals(13, inRangeCount, "Baisakh 8 through 20 inclusive is 13 days")
        assertTrue(
            inRangeCount < drawnDays.size,
            "A partial selection must leave some drawn day out of range"
        )
    }

    /**
     * The grid the range picker measures is the same grid it draws, which is only safe if the
     * dual-date pass leaves every cell's Bikram Sambat date alone.
     */
    @Test
    fun theDualDatePass_doesNotChangeWhichDaysAreInRange() {
        for (calendarSystem in CalendarSystem.entries) {
            val adapter = adapterFor(calendarSystem)
            val month = if (calendarSystem == CalendarSystem.BIKRAM_SAMBAT) {
                adapter.monthOf(2083, 4)
            } else {
                adapter.monthOf(2026, 9)
            }
            val plain = adapter.monthGrid(month, withAdjacentDays = true)
            val dual = adapter.monthGrid(month, withSecondary = true, withAdjacentDays = true)
            assertEquals(
                plain.map { it?.day?.canonical },
                dual.map { it?.day?.canonical },
                "$calendarSystem: the secondary pass moved a canonical date"
            )
            assertEquals(
                plain.map { it?.monthOffset },
                dual.map { it?.monthOffset },
                "$calendarSystem: the secondary pass moved a cell"
            )
        }
    }

    @Test
    fun aGregorianGrid_measuresTheSameBikramSambatRange() {
        val gregorian = adapterFor(CalendarSystem.GREGORIAN)
        val bikramSambat = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = bikramSambat.dateOf(2083, 4, 8)
        val end = bikramSambat.dateOf(2083, 4, 20)

        val august = gregorian.monthOf(2026, 8)
        val canonicalDays = gregorian.monthGrid(august, withAdjacentDays = true)
            .mapNotNull { it?.day?.canonical }
        val selected = canonicalDays.filter { inRange(it, start, end) }

        assertTrue(selected.isNotEmpty(), "The range overlaps this Gregorian month")
        selected.forEach { day ->
            assertEquals(2083, day.year)
            assertEquals(4, day.month)
            assertTrue(day.dayOfMonth in 8..20, "Shrawan ${day.dayOfMonth} is outside 8..20")
        }
    }
}
