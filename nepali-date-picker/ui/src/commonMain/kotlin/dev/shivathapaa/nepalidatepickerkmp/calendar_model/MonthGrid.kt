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
import dev.shivathapaa.nepalidatepickerkmp.NepaliDaysInWeek
import dev.shivathapaa.nepalidatepickerkmp.NepaliMaxCalendarRows
import dev.shivathapaa.nepalidatepickerkmp.numberOfMonthsInRange
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar

/**
 * One occupied slot of a month grid.
 *
 * @property day the day drawn in the slot, resolved in every calendar the cell has to speak.
 * @property monthOffset how far the day's month sits from the grid's month: `-1` for the previous
 *   month, `0` for the grid's own month, `1` for the next. Anything non-zero is drawn dimmed and,
 *   when tapped, moves the pager by that many months.
 */
@Immutable
internal data class MonthGridCell(
    val day: CalendarDay,
    val monthOffset: Int
)

/** Number of slots in a month grid: six rows of seven. */
internal const val MonthGridCellCount = NepaliMaxCalendarRows * NepaliDaysInWeek

/**
 * The fixed six-by-seven grid for [month]: [MonthGridCellCount] slots in cell order, `null` wherever
 * nothing is drawn.
 *
 * [withSecondary] fills in [CalendarDay.secondary] for the dual-date cell, and costs a conversion
 * pass per month involved.
 *
 * [withAdjacentDays] fills the slots around [month] with its neighbours' days, so a grid reads like
 * a wall calendar instead of trailing off into blanks. The fill stops at the end of the last row that
 * holds a day of [month], which leaves a short month's final row empty rather than showing a whole
 * extra week. Slots stay `null` at the edges of the adapter's year range, where there is no
 * neighbouring month to borrow from.
 */
internal fun CalendarViewAdapter.monthGrid(
    month: MonthCalendar,
    withSecondary: Boolean = false,
    withAdjacentDays: Boolean = false
): List<MonthGridCell?> {
    val leading = month.daysFromStartOfWeekToFirstOfMonth
    val ownDays = daysIn(month, withSecondary)
    val cells = MutableList<MonthGridCell?>(MonthGridCellCount) { null }
    ownDays.forEachIndexed { index, day -> cells[leading + index] = MonthGridCell(day, 0) }

    if (!withAdjacentDays) return cells

    val monthIndex = month.indexIn(yearRange)
    val lastMonthIndex = numberOfMonthsInRange(yearRange) - 1

    // monthAt clamps into the year range, so these guards are what stop the first and last months of
    // the range from borrowing days from themselves.
    if (leading > 0 && monthIndex > 0) {
        val previousDays = daysIn(monthAt(monthIndex - 1), withSecondary)
        for (slotsBack in 1..leading) {
            val day = previousDays.getOrNull(previousDays.size - slotsBack) ?: break
            cells[leading - slotsBack] = MonthGridCell(day, -1)
        }
    }

    val lastOwnCell = leading + ownDays.size - 1
    val trailing = NepaliDaysInWeek - 1 - lastOwnCell % NepaliDaysInWeek
    if (trailing > 0 && monthIndex < lastMonthIndex) {
        val nextDays = daysIn(monthAt(monthIndex + 1), withSecondary)
        for (slotsForward in 0 until trailing) {
            val day = nextDays.getOrNull(slotsForward) ?: break
            cells[lastOwnCell + 1 + slotsForward] = MonthGridCell(day, 1)
        }
    }

    return cells
}
