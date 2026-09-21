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
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.takeOrElse
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar

/**
 * One day of a month grid as the picker is about to draw it.
 *
 * @property date the Bikram Sambat date of the cell. Selection, the selectable-date rules and this
 *   are always Bikram Sambat, whichever calendar the grid displays, so a decorator keyed by Bikram
 *   Sambat keeps working when the user flips to the Gregorian view.
 * @property displayedDate the date whose day number the cell actually draws. The same value as
 *   [date] in a Bikram Sambat grid, the Gregorian equivalent in a Gregorian one.
 * @property isToday whether [date] is the current date.
 * @property isSelected whether [date] is the selected date, or an endpoint of the selected range.
 * @property isInRange whether [date] falls between the two endpoints of a selected range.
 * @property isEnabled whether the picker's [dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates]
 *   allows the day to be picked.
 * @property isAdjacentMonth whether the cell belongs to a neighbouring month, drawn faded at the
 *   edges of a grid with `showAdjacentMonthDays` on.
 */
@Immutable
data class NepaliDayInfo(
    val date: CustomCalendar,
    val displayedDate: CustomCalendar,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isInRange: Boolean,
    val isEnabled: Boolean,
    val isAdjacentMonth: Boolean
)

/**
 * Decides how a day is drawn beyond what the theme already says, which is what puts holidays,
 * festivals and an app's own events on the calendar.
 *
 * The picker calls this once per visible cell, so keep it a lookup rather than a computation: read
 * from a map or a memoized source, as [NepaliDatePickerDefaults.eventDecorator] and
 * [NepaliDatePickerDefaults.dayDecorator] do. Hoist the decorator itself into a `remember` too, so
 * a new lambda identity on every recomposition does not defeat the grid's own caching.
 *
 * [decorate] runs outside composition, so read the theme before building the decorator rather than
 * inside it: `MaterialTheme.colorScheme` is not available in the lambda.
 *
 * Example:
 * ```
 * val eventColor = MaterialTheme.colorScheme.primary
 * val decorator = remember(events, eventColor) {
 *     NepaliDayDecorator { day ->
 *         val onThatDay = events[day.date.toSimpleDate()] ?: return@NepaliDayDecorator null
 *         NepaliDayDecoration(
 *             indicators = onThatDay.map { eventColor },
 *             contentDescription = onThatDay.joinToString { it.title }
 *         )
 *     }
 * }
 * NepaliDatePicker(state = state, dayDecorator = decorator)
 * ```
 */
@Stable
fun interface NepaliDayDecorator {

    /** How to draw [day], or `null` to leave the cell exactly as the theme draws it. */
    fun decorate(day: NepaliDayInfo): NepaliDayDecoration?
}

/**
 * Lays [other]'s marks over this decorator's, which is how a day that is both a holiday and a
 * working day with two meetings on it gets drawn.
 *
 * This decorator leads: a colour it specifies is kept and [other]'s is dropped, so a holiday rule
 * placed first keeps naming the day whatever else is scheduled on it. Indicators from both are
 * drawn in order, still capped by the cell, and both descriptions are announced. A day neither
 * decorates stays untouched.
 *
 * ```
 * dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy)
 *     .then(NepaliDatePickerDefaults.dayDecorator(markers = myEvents))
 * ```
 */
fun NepaliDayDecorator.then(other: NepaliDayDecorator): NepaliDayDecorator {
    val first = this
    return NepaliDayDecorator { day ->
        val leading = first.decorate(day)
        val trailing = other.decorate(day)
        when {
            leading == null -> trailing
            trailing == null -> leading
            else -> NepaliDayDecoration(
                contentColor = leading.contentColor.takeOrElse { trailing.contentColor },
                containerColor = leading.containerColor.takeOrElse { trailing.containerColor },
                indicators = leading.indicators + trailing.indicators,
                contentDescription = listOfNotNull(
                    leading.contentDescription?.takeIf { it.isNotEmpty() },
                    trailing.contentDescription?.takeIf { it.isNotEmpty() }
                ).joinToString().takeIf { it.isNotEmpty() }
            )
        }
    }
}
