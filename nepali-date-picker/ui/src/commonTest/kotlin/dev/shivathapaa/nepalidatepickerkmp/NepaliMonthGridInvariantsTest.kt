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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MonthGridCellCount
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.monthGrid
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Properties every month grid has to satisfy, checked across whole spans of years in both calendars
 * rather than on hand-picked months. A single off-by-one anywhere in the fill breaks one of these.
 */
class NepaliMonthGridInvariantsTest {

    @Test
    fun everyFilledSlot_sitsInItsWeekdayColumn() {
        for ((adapter, years) in sweptCalendars()) {
            for (month in adapter.monthsOf(years)) {
                for ((slot, cell) in adapter.monthGrid(month, withAdjacentDays = true).filledSlots()) {
                    assertEquals(
                        slot % NepaliDaysInWeek + 1,
                        cell.day.displayed.dayOfWeek,
                        "slot $slot of $month holds ${cell.day.displayed}, which is in the wrong column"
                    )
                }
            }
        }
    }

    @Test
    fun filledSlots_areOneUnbrokenRunOfConsecutiveDays() {
        for ((adapter, years) in sweptCalendars()) {
            for (month in adapter.monthsOf(years)) {
                val filled = adapter.monthGrid(month, withAdjacentDays = true).filledSlots()

                assertEquals(
                    (filled.first().index..filled.last().index).toList(),
                    filled.map { it.index },
                    "the filled slots of $month have a hole in them"
                )
                for ((earlier, later) in filled.zipWithNext()) {
                    assertTrue(
                        adapter.isDayAfter(earlier.value.day.displayed, later.value.day.displayed),
                        "$month jumps from ${earlier.value.day.displayed} to ${later.value.day.displayed}"
                    )
                }
            }
        }
    }

    @Test
    fun theFillStops_atTheEndOfTheLastRowHoldingTheMonth() {
        for ((adapter, years) in sweptCalendars()) {
            val lastMonthIndex = numberOfMonthsInRange(adapter.yearRange) - 1
            for (month in adapter.monthsOf(years)) {
                val cells = adapter.monthGrid(month, withAdjacentDays = true)
                val lastOwnSlot =
                    month.daysFromStartOfWeekToFirstOfMonth + month.totalDaysInMonth - 1
                val lastFilledSlot = cells.filledSlots().last().index

                val borrowedForward = lastFilledSlot - lastOwnSlot
                val atRangeEnd = month.indexIn(adapter.yearRange) == lastMonthIndex
                if (atRangeEnd) {
                    assertEquals(0, borrowedForward, "$month has no later month to borrow from")
                } else {
                    assertEquals(
                        month.expectedTrailing(),
                        borrowedForward,
                        "$month borrowed the wrong number of days forward"
                    )
                    assertTrue(
                        lastFilledSlot % NepaliDaysInWeek == NepaliDaysInWeek - 1 ||
                                lastFilledSlot == MonthGridCellCount - 1,
                        "$month stops mid-row at slot $lastFilledSlot"
                    )
                }
            }
        }
    }

    @Test
    fun monthOffset_matchesTheActualDistanceFromTheDisplayedMonth() {
        for ((adapter, years) in sweptCalendars()) {
            for (month in adapter.monthsOf(years)) {
                for ((_, cell) in adapter.monthGrid(month, withAdjacentDays = true).filledSlots()) {
                    val displayed = cell.day.displayed
                    val distance = (displayed.year - month.year) * MonthsInTestYear +
                            (displayed.month - month.month)
                    assertEquals(
                        distance,
                        cell.monthOffset,
                        "$month labelled $displayed with the wrong offset"
                    )
                    assertTrue(cell.monthOffset in -1..1, "a grid never reaches past one month")
                }
            }
        }
    }

    @Test
    fun everySelectableCell_roundTripsThroughItsCanonicalDate() {
        for ((adapter, years) in sweptCalendars()) {
            for (month in adapter.monthsOf(years)) {
                for ((_, cell) in adapter.monthGrid(month, withAdjacentDays = true).filledSlots()) {
                    val canonical = cell.day.canonical ?: continue
                    assertEquals(
                        cell.day.displayed,
                        assertNotNull(adapter.fromCanonical(canonical)),
                        "a cell must select the day it draws"
                    )
                }
            }
        }
    }

    @Test
    fun theSecondaryNumber_isResolvedForNeighboursToo() {
        for ((adapter, years) in sweptCalendars()) {
            for (month in adapter.monthsOf(years.first..years.first + 1)) {
                val cells = adapter.monthGrid(
                    month = month,
                    withSecondary = true,
                    withAdjacentDays = true
                )
                for ((slot, cell) in cells.filledSlots()) {
                    if (cell.day.canonical == null) continue
                    assertNotNull(
                        cell.day.secondary,
                        "slot $slot of $month has no small number to draw"
                    )
                }
            }
        }
    }

    @Test
    fun theFirstMonthOfTheRange_borrowsNothingBackward() {
        for (system in CalendarSystem.entries) {
            for (yearRange in listOf(FullNepaliYearRange, 2081..2084, 2090..2090)) {
                val adapter = adapterFor(system, yearRange)
                val first = adapter.monthOf(adapter.yearRange.first, 1)
                val cells = adapter.monthGrid(first, withAdjacentDays = true)

                assertTrue(
                    cells.take(first.daysFromStartOfWeekToFirstOfMonth).all { it == null },
                    "$first is the start of the range, so there is nothing to borrow"
                )
            }
        }
    }

    @Test
    fun theLastMonthOfTheRange_borrowsNothingForward() {
        for (system in CalendarSystem.entries) {
            for (yearRange in listOf(FullNepaliYearRange, 2081..2084, 2090..2090)) {
                val adapter = adapterFor(system, yearRange)
                val last = adapter.monthOf(adapter.yearRange.last, MonthsInTestYear)
                val cells = adapter.monthGrid(last, withAdjacentDays = true)
                val lastOwnSlot = last.daysFromStartOfWeekToFirstOfMonth + last.totalDaysInMonth - 1

                assertNotNull(cells[lastOwnSlot], "the month's own last day is still drawn")
                assertTrue(
                    cells.drop(lastOwnSlot + 1).all { it == null },
                    "$last is the end of the range, so there is nothing to borrow"
                )
            }
        }
    }

    @Test
    fun aSingleYearRange_borrowsNothingAtEitherEnd() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT, yearRange = 2083..2083)
        val baisakh = adapter.monthOf(2083, 1)
        val chaitra = adapter.monthOf(2083, MonthsInTestYear)

        val leadingCells = adapter.monthGrid(baisakh, withAdjacentDays = true)
            .take(baisakh.daysFromStartOfWeekToFirstOfMonth)
        assertTrue(leadingCells.all { it == null })

        val lastOwnSlot = chaitra.daysFromStartOfWeekToFirstOfMonth + chaitra.totalDaysInMonth - 1
        assertTrue(
            adapter.monthGrid(chaitra, withAdjacentDays = true).drop(lastOwnSlot + 1)
                .all { it == null }
        )
        // The months in between still borrow across, so the clamp is at the range edge only.
        val middle = adapter.monthOf(2083, 6)
        assertTrue(
            adapter.monthGrid(middle, withAdjacentDays = true).filledSlots()
                .any { it.value.monthOffset != 0 }
        )
    }

    @Test
    fun gregorianGridAtTheConversionAnchor_drawsUnconvertibleDaysButLeavesThemInert() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val april1913 = adapter.monthOf(adapter.yearRange.first, 4)
        assertEquals(1913, april1913.year, "the Gregorian range starts at the conversion anchor")

        val cells = adapter.monthGrid(april1913, withAdjacentDays = true)
        // Bikram Sambat 1970-01-01 is AD 1913-04-13, so nothing earlier converts. The grid also
        // borrows from May, which is past the anchor and therefore not part of this check.
        val beforeAnchor = cells.filledSlots()
            .map { it.value.day }
            .filter { it.displayed.month < 4 || (it.displayed.month == 4 && it.displayed.dayOfMonth < 13) }

        assertTrue(beforeAnchor.isNotEmpty(), "April 1913 does hold days before the anchor")
        assertTrue(
            beforeAnchor.all { it.canonical == null },
            "a day with no Bikram Sambat equivalent stays inert"
        )
        assertTrue(
            cells.filledSlots().any { it.value.day.canonical != null },
            "the rest of the month is still selectable"
        )
    }

    @Test
    fun gregorianGridPastTheConversionTable_leavesTheTailInert() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val last = adapter.monthOf(adapter.yearRange.last, MonthsInTestYear)
        val cells = adapter.monthGrid(last, withAdjacentDays = true)

        assertTrue(
            cells.filledSlots().isNotEmpty(),
            "the last Gregorian month of the range still draws"
        )
        // Whatever converts, converts; whatever does not is inert rather than crashing the grid.
        for ((_, cell) in cells.filledSlots()) {
            val canonical = cell.day.canonical ?: continue
            assertTrue(canonical.year in FullNepaliYearRange)
        }
    }

    /** The calendars and year spans every invariant above is checked over. */
    private fun sweptCalendars(): List<Pair<CalendarViewAdapter, IntRange>> =
        listOf(
            adapterFor(CalendarSystem.BIKRAM_SAMBAT) to 2080..2085,
            adapterFor(CalendarSystem.GREGORIAN) to 2023..2028
        )
}
