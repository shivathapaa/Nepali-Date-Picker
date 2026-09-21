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

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarMonth
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarWeekDays
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventDisplayStyle
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.rememberCalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.secondaryMonthLabel
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import kotlinx.coroutines.launch

/**
 * A browsable month calendar: a wall patro that fills the width it is given, pages month by month,
 * and names the days an institution is closed for.
 *
 * Unlike [NepaliDatePicker], which asks for a date and is sized for a dialog, this fills its
 * container and shows both calendars' numbers by default, which is how a Nepali calendar is read.
 * It draws at a fixed height, six week rows whatever the month's shape, and never scrolls
 * vertically, so it sits as one block inside a screen's own scrolling content. Give it a bounded
 * width; an unbounded one, a horizontal scroller or a `Row` without a weight, has nothing for the
 * month pager to fill.
 *
 * Events are first-class here: [policy] decides which days are closed and what is named on them, and
 * the grid marks them without any further wiring. The policy is read once per visible month rather
 * than once per day, so a provider answering from a memoized source keeps paging cheap. Pair the
 * calendar with [NepaliMonthEventList] and [NepaliDaySummary] over the same state and policy to
 * write those days out.
 *
 * @param state what is shown and what is picked. See [rememberNepaliCalendarState].
 * @param modifier the [Modifier] applied to the calendar.
 * @param policy the institution the calendar describes: the days of the week it never opens, and the
 *   events it keeps. Defaults to Nepal's usual office week, Saturday off, with no events.
 * @param secondaryDateLocale pairs every cell with the same day in the other calendar, in this
 *   locale's language and digits. On by default, since a Nepali calendar is usually read against the
 *   Gregorian one; pass `null` for a single number per cell.
 * @param showTodayButton whether the header offers a button back to today's month.
 * @param showCalendarSystemToggle whether the header carries the `B.S.` / `A.D.` switch, which drives
 *   [NepaliCalendarState.displayedCalendarSystem].
 * @param showAdjacentMonthDays whether the empty slots around the month are filled with its
 *   neighbours' days, drawn faded. On by default, which is what makes the grid read as a wall
 *   calendar. Tapping one picks that day and moves the calendar to its month.
 * @param colors the [NepaliDatePickerColors] the calendar themes itself from.
 * @param markerColors the palette a marked day is drawn in, one slot per kind of event.
 * @param eventDisplayStyle how much of a day's status is drawn: its colour, its dots, its disc, and
 *   what a screen reader is told.
 * @param dayDecorator an app's own marks, laid over the policy's. The policy's colour leads and the
 *   dots of both are drawn, so a public holiday that also carries two meetings keeps the holiday's
 *   colour and gains the meetings' dots. Hoist it in a `remember`, as [NepaliDayDecorator] asks.
 * @param onDayClick invoked after a tapped day becomes the selection, with the day and what [policy]
 *   says about it. A day with no Bikram Sambat date, which only happens before the conversion
 *   anchor, is inert and never calls this.
 *
 * Example usage:
 * ```
 * val state = rememberNepaliCalendarState()
 * val policy = remember { NepaliCalendarPolicy(provider = myEvents) }
 *
 * LazyColumn {
 *     item { NepaliCalendar(state = state, policy = policy) }
 *     item { NepaliDaySummary(state = state, policy = policy) }
 *     item { NepaliMonthEventList(state = state, policy = policy) }
 * }
 * ```
 *
 * @see NepaliMonthEventList
 * @see NepaliDaySummary
 * @see NepaliDatePicker
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliCalendar(
    state: NepaliCalendarState,
    modifier: Modifier = Modifier,
    policy: NepaliCalendarPolicy = NepaliCalendarPolicy.Default,
    secondaryDateLocale: NepaliDateLocale? = NepaliDatePickerDefaults.DefaultLocale,
    showTodayButton: Boolean = true,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = true,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    markerColors: NepaliDayMarkerColors = NepaliDatePickerDefaults.markerColors(),
    eventDisplayStyle: NepaliEventDisplayStyle = NepaliEventDisplayStyle.Default,
    dayDecorator: NepaliDayDecorator? = null,
    onDayClick: ((day: CustomCalendar, status: NepaliDayStatus) -> Unit)? = null
) {
    val calendarModel = remember(state.locale) { NepaliCalendarModel(state.locale) }
    // `today` reads the wall clock; remember it so it isn't recomputed on every recomposition.
    val today = remember(calendarModel) { calendarModel.todayNepaliSimpleDate }
    val adapter = rememberCalendarViewAdapter(
        calendarSystem = state.displayedCalendarSystem,
        calendarModel = calendarModel,
        nepaliYearRange = state.yearRange
    )

    val displayedMonth = state.displayedMonthCalendar
    val displayedMonthIndex = remember(displayedMonth, adapter) {
        displayedMonth.indexIn(adapter.yearRange)
    }
    val todayMonth = remember(today, adapter) { adapter.monthContaining(today) }
    val todayMonthIndex = remember(todayMonth, adapter) { todayMonth.indexIn(adapter.yearRange) }

    // A calendar switch changes both how many months the pager holds and what an index means, so the
    // list state cannot be carried over; `key` rebuilds it at the new calendar's index.
    val monthsListState = key(state.displayedCalendarSystem) {
        rememberLazyListState(initialFirstVisibleItemIndex = displayedMonthIndex)
    }
    val coroutineScope = rememberCoroutineScope()
    var yearPickerVisible by rememberSaveable { mutableStateOf(false) }

    val language = state.locale.language
    val monthName = adapter.monthName(displayedMonth.month, language, NameFormat.FULL)
    val year = calendarModel.localizeNumber(displayedMonth.year.toString(), language)
    val secondaryMonthLabel = remember(displayedMonth, adapter, secondaryDateLocale) {
        adapter.secondaryMonthLabel(
            days = adapter.daysIn(displayedMonth, withSecondary = true),
            calendarModel = calendarModel,
            language = secondaryDateLocale?.language ?: language
        )
    }

    Column(modifier = modifier.fillMaxWidth().background(colors.containerColor)) {
        // The switch takes a line of its own above the month row. The year button, TODAY and the
        // two arrows already fill that row at a phone's width, so a switch beside them would be
        // squeezed against the edge.
        if (showCalendarSystemToggle) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(
                        start = CalendarHorizontalPadding,
                        end = CalendarHorizontalPadding,
                        top = CalendarToggleRowTopPadding
                    ),
                horizontalArrangement = Arrangement.End
            ) {
                NepaliCalendarSystemToggle(
                    calendarSystem = state.displayedCalendarSystem,
                    onCalendarSystemChange = { state.displayedCalendarSystem = it },
                    language = language,
                    colors = colors
                )
            }
        }
        NepaliMonthsNavigation(
            modifier = Modifier.padding(horizontal = CalendarHorizontalPadding),
            isToday = displayedMonthIndex == todayMonthIndex,
            todayText = language.today,
            nextAvailable = monthsListState.canScrollForward,
            previousAvailable = monthsListState.canScrollBackward,
            yearPickerVisible = yearPickerVisible,
            yearPickerText = "$monthName $year",
            yearPickerSubtitle = secondaryMonthLabel,
            showTodayButton = showTodayButton,
            onNextClicked = {
                coroutineScope.scrollMonthsTo(
                    monthsListState, monthsListState.firstVisibleItemIndex + 1
                )
            },
            onPreviousClicked = {
                coroutineScope.scrollMonthsTo(
                    monthsListState, monthsListState.firstVisibleItemIndex - 1
                )
            },
            onTodayClicked = {
                coroutineScope.launch { monthsListState.scrollToItem(todayMonthIndex) }
            },
            onYearPickerButtonClicked = { yearPickerVisible = !yearPickerVisible },
            colors = colors,
            previousMonthContentDescription = language.previousMonthContentDescription,
            nextMonthContentDescription = language.nextMonthContentDescription
        )
        Box {
            Column(modifier = Modifier.padding(horizontal = CalendarHorizontalPadding)) {
                NepaliCalendarWeekDays(
                    colors = colors,
                    language = language,
                    weekDayFormat = state.locale.weekDayName
                )
                LazyRow(
                    state = monthsListState,
                    flingBehavior = rememberCustomSnapFlingBehavior(lazyListState = monthsListState)
                ) {
                    items(
                        count = numberOfMonthsInRange(adapter.yearRange),
                        key = { index: Int -> index }
                    ) { index ->
                        val monthCalendar = remember(index, adapter) { adapter.monthAt(index) }
                        Box(modifier = Modifier.fillParentMaxWidth()) {
                            NepaliCalendarMonth(
                                monthCalendar = monthCalendar,
                                adapter = adapter,
                                calendarModel = calendarModel,
                                today = today,
                                selectedDate = state.selectedDate,
                                policy = policy,
                                markerColors = markerColors,
                                eventDisplayStyle = eventDisplayStyle,
                                dayDecorator = dayDecorator,
                                colors = colors,
                                secondaryDateLocale = secondaryDateLocale,
                                showAdjacentMonthDays = showAdjacentMonthDays,
                                onDateSelectionChange = { state.selectedDate = it },
                                onDayClick = onDayClick,
                                onNavigateToMonth = { monthIndex ->
                                    coroutineScope.scrollMonthsTo(monthsListState, monthIndex)
                                }
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = yearPickerVisible,
                modifier = Modifier.clipToBounds(),
                enter = expandVertically() + fadeIn(initialAlpha = YearPickerInitialAlpha),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    NepaliYearPicker(
                        // The overlay covers the weekday row and the grid exactly, which is what
                        // keeps the calendar the same height whether or not it is open.
                        modifier = Modifier.requiredHeight(
                            RecommendedSizeForAccessibility * (NepaliMaxCalendarRows + 1) -
                                    YearPickerDividerAllowance
                        ).padding(horizontal = CalendarHorizontalPadding),
                        currentYear = todayMonth.year,
                        displayedYear = displayedMonth.year,
                        onYearSelected = { selectedYear ->
                            yearPickerVisible = false
                            coroutineScope.launch {
                                monthsListState.scrollToItem(
                                    (selectedYear - adapter.yearRange.first) * NepaliMonthsInYear +
                                            displayedMonth.month - 1
                                )
                            }
                        },
                        nepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
                        calendarModel = calendarModel,
                        adapter = adapter,
                        colors = colors
                    )
                    HorizontalDivider(color = colors.dividerColor)
                }
            }
        }
    }

    LaunchedEffect(monthsListState, adapter) {
        updateDisplayedMonth(
            lazyListState = monthsListState,
            adapter = adapter,
            onDisplayedMonthChange = { month -> state.displayedMonthCalendar = month }
        )
    }
}

/** Distance from the grid to the edges of the calendar. */
internal val CalendarHorizontalPadding = 8.dp

/** Space above the row the calendar switch sits on, so it clears whatever is drawn over it. */
private val CalendarToggleRowTopPadding = 8.dp

/** Height the year overlay gives back to the divider drawn under it. */
private val YearPickerDividerAllowance = 1.dp

/** How visible the year overlay already is when it starts expanding. */
private const val YearPickerInitialAlpha = 0.6f
