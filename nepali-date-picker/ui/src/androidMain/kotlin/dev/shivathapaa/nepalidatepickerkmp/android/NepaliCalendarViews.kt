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

package dev.shivathapaa.nepalidatepickerkmp.android

import android.content.Context
import android.view.View
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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.embed.toCalendarMarks
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus

/**
 * The browsable month calendar as a plain [View], for hosts that cannot compose: a Flutter platform
 * view or an XML layout. Mirrors the iOS `NepaliCalendarViewController` parameter for parameter.
 *
 * The grid fills the width it is given and draws at a fixed height whatever the month's shape.
 * [onHeightChange] reports that height in density independent points, for hosts that size the view
 * themselves.
 *
 * @param initialSelectedDate the day picked when the calendar first appears, or `null` for none.
 * @param locale language, date format and digit script the calendar renders with.
 * @param yearRange the Bikram Sambat years the calendar pages over.
 * @param showSecondaryDates whether every cell also shows the same day in the other calendar.
 * @param secondaryDateLocale the language and digits those second numbers are written in, or `null`
 *   to use the calendar's own locale.
 * @param events the institution's weekly rule and the days it names, or `null` for Nepal's usual
 *   office week with nothing named.
 * @param showDaySummary whether the picked day is written out under the grid.
 * @param showMonthEvents whether the month's events are listed under the grid, below the day's
 *   summary when both are shown. Both stack inside this one view, since two views cannot share a
 *   selection.
 * @param onDaySelected invoked with the tapped day and what the policy says about it.
 * @param onEventTapped invoked with the event behind a tapped line of the stacked month list.
 */
fun NepaliCalendarView(
    context: Context,
    initialSelectedDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    showTodayButton: Boolean = true,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = true,
    showSecondaryDates: Boolean = true,
    secondaryDateLocale: NepaliDateLocale? = null,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showDaySummary: Boolean = false,
    showMonthEvents: Boolean = false,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onEventTapped: (NepaliCalendarEvent) -> Unit = {},
    onDaySelected: (CustomCalendar, NepaliDayStatus) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    val marks = events?.toEmbeddedConfig().toCalendarMarks()
    val state = remember {
        NepaliCalendarState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialSelectedDate,
            yearRange = yearRange,
            locale = locale,
            initialCalendarSystem = initialCalendarSystem
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        NepaliCalendar(
            state = state,
            policy = marks.policy,
            secondaryDateLocale = if (showSecondaryDates) secondaryDateLocale ?: locale else null,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays,
            markerColors = marks.markerColors,
            eventDisplayStyle = marks.style,
            dayDecorator = marks.dotDecorator,
            onDayClick = onDaySelected
        )
        if (showDaySummary) {
            NepaliDaySummary(
                state = state,
                modifier = Modifier.padding(HostedListPadding),
                policy = marks.policy,
                markerColors = marks.markerColors
            )
        }
        if (showMonthEvents) {
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
 * The month's event list as a plain [View]. Mirrors the iOS `NepaliMonthEventListViewController`.
 *
 * A grid and a list in two views cannot share a selection, so this one shows no picked day; ask
 * [NepaliCalendarView] for a stacked list when that matters.
 *
 * @param year the Bikram Sambat year to list.
 * @param month the Bikram Sambat month to list, 1 for Baisakh through 12 for Chaitra.
 * @param onEventTapped invoked with the event behind a tapped line.
 */
fun NepaliMonthEventListView(
    context: Context,
    year: Int,
    month: Int,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onEventTapped: (NepaliCalendarEvent) -> Unit = {}
): View = nepaliPickerView(context, onHeightChange) {
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
 * One day written out as a plain [View]: whether the institution is shut, why, and what is named on
 * it. Mirrors the iOS `NepaliDaySummaryViewController`.
 *
 * @param date the day to write out, Bikram Sambat.
 */
fun NepaliDaySummaryView(
    context: Context,
    date: SimpleDate,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {}
): View = nepaliPickerView(context, onHeightChange) {
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
