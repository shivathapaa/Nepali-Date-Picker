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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MonthGridCellCount
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.monthGrid
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Geometry of the six-by-seven month grid: which slot holds which day, and where the fill stops.
 *
 * The screenshot-derived expectations are spelled out literally, so a change to the fill rule has to
 * be a deliberate edit rather than a silently passing refactor. The sweeping invariants live in
 * [NepaliMonthGridInvariantsTest].
 */
class NepaliMonthGridTest {

    @Test
    fun grid_isAlwaysSixRowsOfSeven() {
        for (system in CalendarSystem.entries) {
            val adapter = adapterFor(system)
            val month = adapter.monthOf(adapter.yearRange.first + 5, 6)
            assertEquals(MonthGridCellCount, adapter.monthGrid(month).size)
            assertEquals(
                MonthGridCellCount,
                adapter.monthGrid(month, withAdjacentDays = true).size,
                "turning the fill on must not change the number of slots"
            )
        }
    }

    @Test
    fun grid_withoutAdjacentDays_holdsOnlyTheMonth() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)
        val cells = adapter.monthGrid(shrawan)
        val leading = shrawan.daysFromStartOfWeekToFirstOfMonth

        cells.forEachIndexed { slot, cell ->
            val insideMonth = slot >= leading && slot < leading + shrawan.totalDaysInMonth
            if (insideMonth) {
                val filled = assertNotNull(cell, "slot $slot belongs to the month")
                assertEquals(0, filled.monthOffset)
                assertEquals(slot - leading + 1, filled.day.displayed.dayOfMonth)
            } else {
                assertNull(cell, "slot $slot is outside the month and must stay empty")
            }
        }
    }

    @Test
    fun turningTheFillOn_leavesEveryOwnMonthSlotAlone() {
        for (system in CalendarSystem.entries) {
            val adapter = adapterFor(system)
            for (month in adapter.monthsOf(adapter.yearRange.first + 3..adapter.yearRange.first + 5)) {
                val plain = adapter.monthGrid(month)
                val filled = adapter.monthGrid(month, withAdjacentDays = true)
                plain.forEachIndexed { slot, cell ->
                    if (cell != null) {
                        assertEquals(cell, filled[slot], "slot $slot of $month must not move")
                    }
                }
            }
        }
    }

    @Test
    fun shrawan2083_fillsEveryCellFromBothNeighbours() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val shrawan = adapter.monthOf(2083, 4)

        // The reference screenshot: Shrawan 1 falls on a Friday, so five days of Asar lead the grid.
        assertEquals(5, shrawan.daysFromStartOfWeekToFirstOfMonth)
        assertEquals(31, shrawan.totalDaysInMonth)
        assertEquals(32, adapter.monthOf(2083, 3).totalDaysInMonth)

        val numbers = adapter.gridNumbers(shrawan, withAdjacentDays = true)
        assertEquals(listOf(28, 29, 30, 31, 32), numbers.take(5))
        assertEquals((1..31).toList(), numbers.subList(5, 36))
        assertEquals(listOf(1, 2, 3, 4, 5, 6), numbers.takeLast(6))
        assertTrue(numbers.none { it == null }, "every one of the 42 cells is filled")
    }

    @Test
    fun september2026_fillsFiveRowsAndLeavesTheSixthEmpty() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val september = adapter.monthOf(2026, 9)

        // The reference screenshot: 30 and 31 of August lead, 1 to 3 of October trail.
        assertEquals(2, september.daysFromStartOfWeekToFirstOfMonth)
        assertEquals(30, september.totalDaysInMonth)

        val numbers = adapter.gridNumbers(september, withAdjacentDays = true)
        assertEquals(listOf(30, 31), numbers.take(2))
        assertEquals((1..30).toList(), numbers.subList(2, 32))
        assertEquals(listOf(1, 2, 3), numbers.subList(32, 35))
        assertTrue(
            numbers.drop(35).all { it == null },
            "the sixth row holds no day of September, so it stays empty"
        )
    }

    @Test
    fun adjacentCells_carryTheirOwnMonthAndOffset() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val cells = adapter.monthGrid(adapter.monthOf(2083, 4), withAdjacentDays = true)

        val leadingLast = assertNotNull(cells[4])
        assertEquals(-1, leadingLast.monthOffset)
        assertEquals(2083, leadingLast.day.displayed.year)
        assertEquals(3, leadingLast.day.displayed.month)
        assertEquals(32, leadingLast.day.displayed.dayOfMonth)

        val trailingFirst = assertNotNull(cells[36])
        assertEquals(1, trailingFirst.monthOffset)
        assertEquals(2083, trailingFirst.day.displayed.year)
        assertEquals(5, trailingFirst.day.displayed.month)
        assertEquals(1, trailingFirst.day.displayed.dayOfMonth)
    }

    @Test
    fun chaitra_borrowsBaisakhOfTheFollowingYear() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val chaitra = adapter.monthOf(2081, 12)
        val cells = adapter.monthGrid(chaitra, withAdjacentDays = true)
        val trailing = chaitra.expectedTrailing()
        assertTrue(trailing > 0, "this Chaitra does not end on a week boundary")

        val lastOwnSlot = chaitra.daysFromStartOfWeekToFirstOfMonth + chaitra.totalDaysInMonth - 1
        for (offset in 0 until trailing) {
            val cell = assertNotNull(cells[lastOwnSlot + 1 + offset])
            assertEquals(1, cell.monthOffset)
            assertEquals(2082, cell.day.displayed.year, "the year rolls forward")
            assertEquals(1, cell.day.displayed.month, "into Baisakh")
            assertEquals(offset + 1, cell.day.displayed.dayOfMonth)
        }
    }

    @Test
    fun baisakh_borrowsChaitraOfThePrecedingYear() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val baisakh = adapter.monthOf(2082, 1)
        val leading = baisakh.daysFromStartOfWeekToFirstOfMonth
        assertTrue(leading > 0, "this Baisakh does not start on a Sunday")

        val cells = adapter.monthGrid(baisakh, withAdjacentDays = true)
        val previousLength = adapter.monthOf(2081, 12).totalDaysInMonth
        for (slot in 0 until leading) {
            val cell = assertNotNull(cells[slot])
            assertEquals(-1, cell.monthOffset)
            assertEquals(2081, cell.day.displayed.year, "the year rolls back")
            assertEquals(12, cell.day.displayed.month, "into Chaitra")
            assertEquals(previousLength - leading + slot + 1, cell.day.displayed.dayOfMonth)
        }
    }

    @Test
    fun december_borrowsJanuaryOfTheFollowingYear() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val december = adapter.monthOf(2025, 12)
        val cells = adapter.monthGrid(december, withAdjacentDays = true)
        val lastOwnSlot = december.daysFromStartOfWeekToFirstOfMonth + december.totalDaysInMonth - 1
        assertTrue(december.expectedTrailing() > 0)

        val trailingFirst = assertNotNull(cells[lastOwnSlot + 1])
        assertEquals(2026, trailingFirst.day.displayed.year)
        assertEquals(1, trailingFirst.day.displayed.month)
        assertEquals(1, trailingFirst.day.displayed.dayOfMonth)
    }

    @Test
    fun january_borrowsDecemberOfThePrecedingYear() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val january = adapter.monthOf(2026, 1)
        val leading = january.daysFromStartOfWeekToFirstOfMonth
        assertTrue(leading > 0, "this January does not start on a Sunday")

        val cells = adapter.monthGrid(january, withAdjacentDays = true)
        val leadingLast = assertNotNull(cells[leading - 1])
        assertEquals(2025, leadingLast.day.displayed.year)
        assertEquals(12, leadingLast.day.displayed.month)
        assertEquals(31, leadingLast.day.displayed.dayOfMonth)
    }

    @Test
    fun aMonthStartingOnSunday_borrowsNothingBackward() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val month = adapter.monthsOf(2080..2090).first { it.daysFromStartOfWeekToFirstOfMonth == 0 }
        val cells = adapter.monthGrid(month, withAdjacentDays = true)

        val first = assertNotNull(cells[0], "day 1 sits in the very first slot")
        assertEquals(0, first.monthOffset)
        assertEquals(1, first.day.displayed.dayOfMonth)
    }

    @Test
    fun aMonthEndingOnSaturday_borrowsNothingForward() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val month = adapter.monthsOf(2080..2090).first { it.expectedTrailing() == 0 }
        val cells = adapter.monthGrid(month, withAdjacentDays = true)
        val lastOwnSlot = month.daysFromStartOfWeekToFirstOfMonth + month.totalDaysInMonth - 1

        assertNotNull(cells[lastOwnSlot])
        assertTrue(
            cells.drop(lastOwnSlot + 1).all { it == null },
            "the last occupied row is already full, so nothing is borrowed"
        )
    }

    @Test
    fun aThirtyTwoDayMonth_fillsCorrectly() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val month = adapter.monthsOf(2080..2090).first { it.totalDaysInMonth == 32 }
        val numbers = adapter.gridNumbers(month, withAdjacentDays = true)
        val leading = month.daysFromStartOfWeekToFirstOfMonth

        assertEquals((1..32).toList(), numbers.subList(leading, leading + 32))
    }

    @Test
    fun aTwentyNineDayMonth_fillsCorrectly() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val month = adapter.monthsOf(2080..2090).first { it.totalDaysInMonth == 29 }
        val numbers = adapter.gridNumbers(month, withAdjacentDays = true)
        val leading = month.daysFromStartOfWeekToFirstOfMonth

        assertEquals((1..29).toList(), numbers.subList(leading, leading + 29))
    }

    @Test
    fun leapFebruary_fillsTwentyNineDays() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        val february = adapter.monthOf(2024, 2)
        assertEquals(29, february.totalDaysInMonth, "2024 is a leap year")

        val numbers = adapter.gridNumbers(february, withAdjacentDays = true)
        val leading = february.daysFromStartOfWeekToFirstOfMonth
        assertEquals((1..29).toList(), numbers.subList(leading, leading + 29))
    }

    @Test
    fun theFirstAndLastDayOfAYear_sitWhereTheMonthPutsThem() {
        val adapter = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val baisakh = adapter.monthOf(2083, 1)
        val chaitra = adapter.monthOf(2083, MonthsInTestYear)

        val yearStart = assertNotNull(
            adapter.monthGrid(baisakh, withAdjacentDays = true)[
                baisakh.daysFromStartOfWeekToFirstOfMonth
            ]
        )
        assertEquals(0, yearStart.monthOffset)
        assertEquals(1, yearStart.day.displayed.month)
        assertEquals(1, yearStart.day.displayed.dayOfMonth)

        val yearEndSlot =
            chaitra.daysFromStartOfWeekToFirstOfMonth + chaitra.totalDaysInMonth - 1
        val yearEnd = assertNotNull(
            adapter.monthGrid(chaitra, withAdjacentDays = true)[yearEndSlot]
        )
        assertEquals(0, yearEnd.monthOffset)
        assertEquals(MonthsInTestYear, yearEnd.day.displayed.month)
        assertEquals(chaitra.totalDaysInMonth, yearEnd.day.displayed.dayOfMonth)
    }

    @Test
    fun aGridIsAPureFunctionOfItsArguments() {
        // The pickers cache the grid with `remember`, so building it twice has to agree.
        for (system in CalendarSystem.entries) {
            val adapter = adapterFor(system)
            val month = adapter.monthOf(adapter.yearRange.first + 7, 11)
            for (withSecondary in listOf(false, true)) {
                assertEquals(
                    adapter.monthGrid(month, withSecondary, withAdjacentDays = true),
                    adapter.monthGrid(month, withSecondary, withAdjacentDays = true)
                )
            }
        }
    }

    @Test
    fun theSecondaryNumberOfABorrowedDay_isTheSameDayInTheOtherCalendar() {
        val bikramSambat = adapterFor(CalendarSystem.BIKRAM_SAMBAT)
        val gregorian = adapterFor(CalendarSystem.GREGORIAN)
        val cells = bikramSambat.monthGrid(
            month = bikramSambat.monthOf(2083, 4),
            withSecondary = true,
            withAdjacentDays = true
        )

        for ((slot, cell) in cells.filledSlots()) {
            val canonical = assertNotNull(cell.day.canonical)
            assertEquals(
                gregorian.fromCanonical(canonical),
                cell.day.secondary,
                "slot $slot draws the wrong small number"
            )
        }
    }

    @Test
    fun aGregorianMonthEntirelyBeforeTheAnchor_hasNoSelectableOwnDays() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        // Bikram Sambat 1970-01-01 is AD 1913-04-13, so March 1913 converts nowhere.
        val march = adapter.monthOf(1913, 3)
        val cells = adapter.monthGrid(march, withAdjacentDays = true)

        val ownDays = cells.filledSlots().filter { it.value.monthOffset == 0 }
        assertTrue(ownDays.isNotEmpty(), "the month is still drawn")
        assertTrue(
            ownDays.all { it.value.day.canonical == null },
            "every day of March 1913 is inert"
        )
        // April is borrowed forward, and its second week is past the anchor, so part of it selects.
        val borrowedForward = cells.filledSlots().filter { it.value.monthOffset == 1 }
        assertTrue(borrowedForward.isNotEmpty(), "March 1913 does borrow from April")
    }

    @Test
    fun aFebruaryFillingWholeWeeksExactly_borrowsNothingAtAll() {
        val adapter = adapterFor(CalendarSystem.GREGORIAN)
        // February 2015 has 28 days and starts on a Sunday, so it is four clean rows on its own.
        val february = adapter.monthOf(2015, 2)
        assertEquals(28, february.totalDaysInMonth)
        assertEquals(0, february.daysFromStartOfWeekToFirstOfMonth)

        val cells = adapter.monthGrid(february, withAdjacentDays = true)
        assertEquals((1..28).toList(), cells.take(28).map { assertNotNull(it).day.displayed.dayOfMonth })
        assertTrue(
            cells.drop(28).all { it == null },
            "two whole rows stay empty rather than borrowing a week of March"
        )
    }
}
