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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MonthGridCell
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.calendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.monthGrid
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang

internal val TestLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)

internal val FullNepaliYearRange = NepaliCalendarDefaults.NepaliYearRange

internal fun adapterFor(
    calendarSystem: CalendarSystem,
    yearRange: IntRange = FullNepaliYearRange
): CalendarViewAdapter = calendarViewAdapter(
    calendarSystem = calendarSystem,
    calendarModel = NepaliCalendarModel(TestLocale),
    nepaliYearRange = yearRange
)

/** Day numbers of each slot, `null` where the grid draws nothing. */
internal fun CalendarViewAdapter.gridNumbers(
    month: MonthCalendar,
    withAdjacentDays: Boolean
): List<Int?> = monthGrid(month, withAdjacentDays = withAdjacentDays)
    .map { it?.day?.displayed?.dayOfMonth }

/** Slots a grid actually draws something in, paired with their cell. */
internal fun List<MonthGridCell?>.filledSlots(): List<IndexedValue<MonthGridCell>> =
    withIndex().mapNotNull { (slot, cell) -> cell?.let { IndexedValue(slot, it) } }

/** The number of trailing slots this month's fill rule should reach, before range clamping. */
internal fun MonthCalendar.expectedTrailing(): Int {
    val lastOwnSlot = daysFromStartOfWeekToFirstOfMonth + totalDaysInMonth - 1
    return NepaliDaysInWeek - 1 - lastOwnSlot % NepaliDaysInWeek
}

/** Whether [later] is the very next day after [earlier] in this adapter's calendar. */
internal fun CalendarViewAdapter.isDayAfter(
    earlier: CustomCalendar,
    later: CustomCalendar
): Boolean {
    if (earlier.year == later.year && earlier.month == later.month) {
        return later.dayOfMonth == earlier.dayOfMonth + 1
    }
    val earlierMonth = monthOf(earlier.year, earlier.month)
    if (earlier.dayOfMonth != earlierMonth.totalDaysInMonth || later.dayOfMonth != 1) return false
    return if (earlier.month == MonthsInTestYear) {
        later.year == earlier.year + 1 && later.month == 1
    } else {
        later.year == earlier.year && later.month == earlier.month + 1
    }
}

/** Every month of [years] in this adapter's calendar. */
internal fun CalendarViewAdapter.monthsOf(years: IntRange): List<MonthCalendar> =
    years.flatMap { year -> (1..MonthsInTestYear).map { monthOf(year, it) } }

/**
 * The day drawn in a grid's very first slot, which is the earliest day the previous month lends.
 *
 * A grid's first slot is also the first node in the semantics tree, so a UI test can find this cell
 * by its number with `onFirst()` no matter which numbers the rest of the grid repeats.
 */
internal fun CalendarViewAdapter.firstBorrowedDay(month: MonthCalendar): CustomCalendar {
    val leading = month.daysFromStartOfWeekToFirstOfMonth
    require(leading > 0) { "$month starts the week, so it borrows nothing backward" }
    return assertFilled(monthGrid(month, withAdjacentDays = true)[0])
}

/**
 * The day drawn in a grid's last filled slot, which is the latest day the next month lends.
 *
 * Mirror of [firstBorrowedDay]: the last filled slot is the last node in the semantics tree, so a UI
 * test can reach it with `onLast()`.
 */
internal fun CalendarViewAdapter.lastBorrowedDay(month: MonthCalendar): CustomCalendar {
    require(month.expectedTrailing() > 0) { "$month ends the week, so it borrows nothing forward" }
    return assertFilled(monthGrid(month, withAdjacentDays = true).filledSlots().last().value)
}

private fun assertFilled(cell: MonthGridCell?): CustomCalendar =
    requireNotNull(cell) { "the slot under test is empty" }.day.displayed

internal const val MonthsInTestYear = 12
