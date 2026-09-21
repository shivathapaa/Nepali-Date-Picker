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

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendar
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendarState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDaySummary
import dev.shivathapaa.nepalidatepickerkmp.NepaliMonthEventList
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.embed.toCalendarMarks
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import platform.UIKit.UIViewController

/**
 * Hosts the browsable [NepaliCalendar] for a Swift caller: a month grid that fills its frame, pages
 * month by month and marks the days [events] names.
 *
 * Composable functions cannot be called from Swift, so this factory is the iOS entry point to the
 * calendar. Wrap the returned controller in a `UIViewControllerRepresentable` to embed it in
 * SwiftUI, and size the frame from [onHeightChange].
 *
 * The grid draws at a fixed height whatever the month's shape. Asking for the day's summary or the
 * month's events stacks them under the grid inside this one controller, since two controllers cannot
 * share a selection, which is why the height is reported rather than assumed.
 *
 * @param initialSelectedDate the day picked when the calendar first appears, or `null` for none.
 * @param locale language, date format and digit script the calendar renders with.
 * @param yearRangeStart first Bikram Sambat year the calendar pages over.
 * @param yearRangeEnd last Bikram Sambat year the calendar pages over.
 * @param options appearance and behaviour knobs, or `null` for the library defaults.
 * @param events the institution's weekly rule and the days it names, or `null` for Nepal's usual
 * office week with nothing named.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onDaySelected invoked with the tapped day and what the policy says about it: whether the
 * day is a weekly off, whether the institution is shut, and everything named on it.
 * @param onEventTapped invoked with the event behind a tapped line of the stacked month list, which
 * is never called while [NepaliCalendarViewOptions.showMonthEvents] is off.
 */
fun NepaliCalendarViewController(
    initialSelectedDate: SimpleDate?,
    locale: NepaliDateLocale,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    options: NepaliCalendarViewOptions?,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit,
    onDaySelected: (CustomCalendar, NepaliDayStatus) -> Unit,
    onEventTapped: (NepaliCalendarEvent) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliCalendarViewOptions()
    val marks = events?.toEmbeddedConfig().toCalendarMarks()
    val state = remember {
        NepaliCalendarState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            locale = locale,
            initialCalendarSystem = opts.initialCalendarSystem
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        NepaliCalendar(
            state = state,
            policy = marks.policy,
            secondaryDateLocale = opts.secondaryLocaleOrNull(locale),
            showTodayButton = opts.showTodayButton,
            showCalendarSystemToggle = opts.showCalendarSystemToggle,
            showAdjacentMonthDays = opts.showAdjacentMonthDays,
            markerColors = marks.markerColors,
            eventDisplayStyle = marks.style,
            dayDecorator = marks.dotDecorator,
            onDayClick = onDaySelected
        )
        if (opts.showDaySummary) {
            NepaliDaySummary(
                state = state,
                modifier = Modifier.padding(HostedListPadding),
                policy = marks.policy,
                markerColors = marks.markerColors
            )
        }
        if (opts.showMonthEvents) {
            NepaliMonthEventList(
                state = state,
                modifier = Modifier.padding(HostedListPadding),
                policy = marks.policy,
                markerColors = marks.markerColors,
                onEventClick = onEventTapped
            )
        }
    }
}

/**
 * Hosts [NepaliMonthEventList] on its own: everything named in one Bikram Sambat month, in date
 * order, with a festival that runs several days written as a single line.
 *
 * Use this when the month's events belong somewhere else on the screen than the grid. A calendar and
 * a list in two controllers cannot share a selection, so the list shows no picked day; ask
 * [NepaliCalendarViewController] for a stacked list when that matters.
 *
 * @param year the Bikram Sambat year to list.
 * @param month the Bikram Sambat month to list, 1 for Baisakh through 12 for Chaitra.
 * @param onEventTapped invoked with the event behind a tapped line.
 */
fun NepaliMonthEventListViewController(
    year: Int,
    month: Int,
    locale: NepaliDateLocale,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit,
    onEventTapped: (NepaliCalendarEvent) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val marks = events?.toEmbeddedConfig().toCalendarMarks()
    val state = remember {
        NepaliCalendarState(
            initialDisplayedMonth = SimpleDate(year, month, 1),
            locale = locale
        )
    }

    NepaliMonthEventList(
        state = state,
        modifier = Modifier.padding(HostedListPadding),
        policy = marks.policy,
        markerColors = marks.markerColors,
        onEventClick = onEventTapped
    )
}

/**
 * Hosts [NepaliDaySummary] on its own: whether the institution is shut on one Bikram Sambat day, why,
 * and everything named on it.
 *
 * @param date the day to write out, Bikram Sambat.
 */
fun NepaliDaySummaryViewController(
    date: SimpleDate,
    locale: NepaliDateLocale,
    events: NepaliEventOptions?,
    onHeightChange: (Float) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val marks = events?.toEmbeddedConfig().toCalendarMarks()

    NepaliDaySummary(
        date = date,
        modifier = Modifier.padding(HostedListPadding),
        policy = marks.policy,
        markerColors = marks.markerColors,
        locale = locale
    )
}

/** Space a hosted list keeps from the edges of the view it fills. */
private val HostedListPadding = 8.dp
