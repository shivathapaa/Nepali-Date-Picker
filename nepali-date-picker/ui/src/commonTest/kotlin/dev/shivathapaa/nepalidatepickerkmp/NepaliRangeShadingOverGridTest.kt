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

import androidx.compose.ui.unit.IntOffset
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.CalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.monthGrid
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Where the range picker paints its selection band once the grid can hold the neighbouring months.
 *
 * With the fill off the coordinates have to be exactly what they were before, since the band is
 * drawn over a grid that has not changed. With it on the band reaches onto the borrowed days, so a
 * range running past the month edge does not stop dead beside a day that is plainly inside it.
 */
class NepaliRangeShadingOverGridTest {

    private val calendarModel = NepaliCalendarModel(TestLocale)

    private fun rangeInfo(
        adapter: CalendarViewAdapter,
        month: MonthCalendar,
        start: CustomCalendar,
        end: CustomCalendar,
        withAdjacentDays: Boolean
    ): NepaliSelectedRangeInfo? = NepaliSelectedRangeInfo.calculateRangeInfo(
        cells = adapter.monthGrid(month, withAdjacentDays = withAdjacentDays),
        startNepaliCalendar = start,
        endNepaliCalendar = end,
        compareDates = calendarModel::compareDates
    )

    private fun CalendarViewAdapter.dateOf(year: Int, month: Int, dayOfMonth: Int): CustomCalendar =
        assertNotNull(calendarOf(dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate(year, month, dayOfMonth)))

    private fun slotOf(coordinates: IntOffset): Int =
        coordinates.y * NepaliDaysInWeek + coordinates.x

    @Test
    fun aRangeInsideTheMonth_landsOnItsOwnDays() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val leading = shrawan.daysFromStartOfWeekToFirstOfMonth

        for (withAdjacentDays in listOf(false, true)) {
            val info = assertNotNull(
                rangeInfo(
                    adapter, shrawan,
                    adapter.dateOf(2083, 4, 8), adapter.dateOf(2083, 4, 20),
                    withAdjacentDays
                )
            )
            assertEquals(leading + 7, slotOf(info.gridStartCoordinates))
            assertEquals(leading + 19, slotOf(info.gridEndCoordinates))
            assertTrue(info.firstIsSelectionStart)
            assertTrue(info.lastIsSelectionEnd)
        }
    }

    @Test
    fun aRangeRunningPastTheMonthEnd_stopsAtTheLastOwnDayWithoutTheFill() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val leading = shrawan.daysFromStartOfWeekToFirstOfMonth

        val info = assertNotNull(
            rangeInfo(
                adapter, shrawan,
                adapter.dateOf(2083, 4, 25), adapter.dateOf(2083, 5, 4),
                withAdjacentDays = false
            )
        )
        assertEquals(leading + 24, slotOf(info.gridStartCoordinates))
        assertEquals(
            leading + shrawan.totalDaysInMonth - 1,
            slotOf(info.gridEndCoordinates),
            "with no borrowed days the band can only reach the month's last cell"
        )
        assertTrue(info.firstIsSelectionStart)
        assertTrue(!info.lastIsSelectionEnd, "the range continues past this grid")
    }

    @Test
    fun aRangeRunningPastTheMonthEnd_reachesTheBorrowedDaysWithTheFill() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val leading = shrawan.daysFromStartOfWeekToFirstOfMonth

        val info = assertNotNull(
            rangeInfo(
                adapter, shrawan,
                adapter.dateOf(2083, 4, 25), adapter.dateOf(2083, 5, 4),
                withAdjacentDays = true
            )
        )
        // Bhadra 4 is the fourth borrowed cell after Shrawan's last day.
        assertEquals(leading + shrawan.totalDaysInMonth + 3, slotOf(info.gridEndCoordinates))
        assertTrue(
            info.lastIsSelectionEnd,
            "the range's end is inside this grid now, so the band gets its rounded cap"
        )
    }

    @Test
    fun aRangeStartingBeforeTheMonth_reachesTheBorrowedLeadingDays() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val leading = shrawan.daysFromStartOfWeekToFirstOfMonth
        val start = adapter.dateOf(2083, 3, 30)
        val end = adapter.dateOf(2083, 4, 6)

        val without = assertNotNull(rangeInfo(adapter, shrawan, start, end, false))
        assertEquals(leading, slotOf(without.gridStartCoordinates), "clipped to the month's day 1")
        assertTrue(!without.firstIsSelectionStart)

        val with = assertNotNull(rangeInfo(adapter, shrawan, start, end, true))
        // Asar has 32 days, so Asar 30 is the third of the five borrowed leading cells.
        assertEquals(leading - 3, slotOf(with.gridStartCoordinates))
        assertTrue(with.firstIsSelectionStart)
    }

    @Test
    fun aRangeSpanningTheWholeGrid_getsNoRoundedCapAtEitherEnd() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)

        for (withAdjacentDays in listOf(false, true)) {
            val info = assertNotNull(
                rangeInfo(
                    adapter, shrawan,
                    adapter.dateOf(2083, 1, 1), adapter.dateOf(2083, 12, 1),
                    withAdjacentDays
                )
            )
            assertTrue(!info.firstIsSelectionStart)
            assertTrue(!info.lastIsSelectionEnd)
        }
    }

    @Test
    fun aRangeThatMissesTheMonthEntirely_paintsNothing() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)

        for (withAdjacentDays in listOf(false, true)) {
            assertNull(
                rangeInfo(
                    adapter, shrawan,
                    adapter.dateOf(2083, 8, 1), adapter.dateOf(2083, 8, 20),
                    withAdjacentDays
                )
            )
            assertNull(
                rangeInfo(
                    adapter, shrawan,
                    adapter.dateOf(2083, 1, 1), adapter.dateOf(2083, 1, 20),
                    withAdjacentDays
                )
            )
        }
    }

    @Test
    fun aRangeTouchingOnlyABorrowedDay_paintsNothingWithoutTheFill() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val start = adapter.dateOf(2083, 5, 2)
        val end = adapter.dateOf(2083, 5, 5)

        assertNull(
            rangeInfo(adapter, shrawan, start, end, withAdjacentDays = false),
            "the range lies wholly in the next month, which this grid does not draw"
        )
        val with = assertNotNull(rangeInfo(adapter, shrawan, start, end, withAdjacentDays = true))
        assertTrue(with.firstIsSelectionStart && with.lastIsSelectionEnd)
    }

    @Test
    fun aSingleDayRange_paintsOneCell() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val day = adapter.dateOf(2083, 4, 12)

        for (withAdjacentDays in listOf(false, true)) {
            val info = assertNotNull(rangeInfo(adapter, shrawan, day, day, withAdjacentDays))
            assertEquals(info.gridStartCoordinates, info.gridEndCoordinates)
        }
    }

    @Test
    fun theCoordinatesStayPutAcrossEveryMonth_whenTheFillIsOff() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val start = adapter.dateOf(2083, 4, 10)
        val end = adapter.dateOf(2083, 6, 10)

        for (month in adapter.monthsOf(2083..2083)) {
            val info = rangeInfo(adapter, month, start, end, withAdjacentDays = false) ?: continue
            val leading = month.daysFromStartOfWeekToFirstOfMonth
            assertTrue(
                slotOf(info.gridStartCoordinates) >= leading,
                "$month must not shade a leading blank"
            )
            assertTrue(
                slotOf(info.gridEndCoordinates) <= leading + month.totalDaysInMonth - 1,
                "$month must not shade a trailing blank"
            )
        }
    }

    @Test
    fun aGregorianGrid_shadesByPositionNotByDayNumber() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val september = adapter.monthOf(2026, 9)
        val leading = september.daysFromStartOfWeekToFirstOfMonth
        // AD 2026-09-17 is BS 2083-06-01, the day the reference screenshot opens on.
        val start = assertNotNull(adapter.toCanonical(adapter.dateOf(2026, 9, 17)))
        val end = assertNotNull(adapter.toCanonical(adapter.dateOf(2026, 9, 20)))

        val info = assertNotNull(
            rangeInfo(adapter, september, start, end, withAdjacentDays = true)
        )
        assertEquals(leading + 16, slotOf(info.gridStartCoordinates))
        assertEquals(leading + 19, slotOf(info.gridEndCoordinates))
    }

    @Test
    fun aGregorianRangeCrossingIntoOctober_reachesTheBorrowedDays() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val september = adapter.monthOf(2026, 9)
        val leading = september.daysFromStartOfWeekToFirstOfMonth
        val start = assertNotNull(adapter.toCanonical(adapter.dateOf(2026, 9, 28)))
        val end = assertNotNull(adapter.toCanonical(adapter.dateOf(2026, 10, 2)))

        val without = assertNotNull(rangeInfo(adapter, september, start, end, false))
        assertEquals(leading + 29, slotOf(without.gridEndCoordinates))
        assertTrue(!without.lastIsSelectionEnd)

        val with = assertNotNull(rangeInfo(adapter, september, start, end, true))
        assertEquals(leading + 31, slotOf(with.gridEndCoordinates), "October 2 is the second borrowed cell")
        assertTrue(with.lastIsSelectionEnd)
    }
}
