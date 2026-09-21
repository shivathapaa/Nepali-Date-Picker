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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.EventLinePadding
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventLine
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus

/**
 * What one day is, written out: whether the institution is shut, why, and everything named on it.
 *
 * The verdict leads, since that is what a reader looks for first, and the weekly rule and each event
 * follow as their own lines. A day that is open and carries nothing says so rather than showing an
 * empty space.
 *
 * Renders as a plain column that wraps its content, so it sits under a [NepaliCalendar] inside a
 * screen's own scrolling container.
 *
 * @param date the day to write out, Bikram Sambat.
 * @param modifier the [Modifier] applied to the summary.
 * @param policy the institution the day is read against. Give the calendar the same one.
 * @param colors the [NepaliDatePickerColors] the summary themes itself from.
 * @param markerColors the palette an event's dot is drawn in, one slot per kind.
 * @param locale the language the verdict and the weekly rule are written in.
 *
 * Example usage:
 * ```
 * NepaliDaySummary(date = SimpleDate(2082, 6, 17), policy = officePolicy)
 * ```
 *
 * @see NepaliCalendar
 * @see NepaliMonthEventList
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDaySummary(
    date: SimpleDate,
    modifier: Modifier = Modifier,
    policy: NepaliCalendarPolicy = NepaliCalendarPolicy.Default,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    markerColors: NepaliDayMarkerColors = NepaliDatePickerDefaults.markerColors(),
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale
) {
    val status = remember(date, policy) { policy.statusOf(date) }
    DayStatusLines(
        status = status,
        markerColors = markerColors,
        colors = colors,
        locale = locale,
        modifier = modifier
    )
}

/**
 * The day picked on a calendar, written out. Follows [state], and says so when nothing is picked
 * yet.
 *
 * The locale comes from the calendar's own, so the summary reads in the language the grid does.
 *
 * @param state the calendar this summary follows. Share the one the grid uses.
 * @param policy the institution the day is read against. Give the calendar the same one.
 *
 * Example usage:
 * ```
 * val state = rememberNepaliCalendarState()
 * val policy = remember { NepaliCalendarPolicy(provider = myEvents) }
 *
 * NepaliCalendar(state = state, policy = policy)
 * NepaliDaySummary(state = state, policy = policy)
 * ```
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDaySummary(
    state: NepaliCalendarState,
    modifier: Modifier = Modifier,
    policy: NepaliCalendarPolicy = NepaliCalendarPolicy.Default,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    markerColors: NepaliDayMarkerColors = NepaliDatePickerDefaults.markerColors()
) {
    val selectedDate = state.selectedDate?.toSimpleDate()
    if (selectedDate == null) {
        Text(
            text = state.locale.language.selectDateText,
            modifier = modifier.padding(vertical = EventLinePadding),
            style = MaterialTheme.typography.bodySmall
        )
        return
    }
    NepaliDaySummary(
        date = selectedDate,
        modifier = modifier,
        policy = policy,
        colors = colors,
        markerColors = markerColors,
        locale = state.locale
    )
}

/** The verdict, the weekly rule and the day's events, in that order. */
@Composable
private fun DayStatusLines(
    status: NepaliDayStatus,
    markerColors: NepaliDayMarkerColors,
    colors: NepaliDatePickerColors,
    locale: NepaliDateLocale,
    modifier: Modifier = Modifier
) {
    val language = locale.language
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (status.isNonWorking) language.closedText else language.workingDayText,
            modifier = Modifier.padding(vertical = EventLinePadding),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (status.isNonWorking) {
                markerColors.weeklyOffColor
            } else {
                colors.dayContentColor
            }
        )
        if (status.isWeeklyOff) {
            NepaliEventLine(
                label = language.weeklyOffText,
                dotColor = markerColors.weeklyOffColor
            )
        }
        status.events.forEach { event ->
            NepaliEventLine(
                label = event.name,
                dotColor = markerColors.colorFor(event.kind)
            )
        }
        if (!status.isWeeklyOff && status.events.isEmpty()) {
            Text(
                text = language.noEventsOnDayText,
                modifier = Modifier.padding(vertical = EventLinePadding),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
