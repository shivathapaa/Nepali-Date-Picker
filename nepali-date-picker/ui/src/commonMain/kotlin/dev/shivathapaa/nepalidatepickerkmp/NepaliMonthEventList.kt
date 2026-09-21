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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.EventLinePadding
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventLine
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventRow
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.collapseEventRows
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy

/**
 * Everything named in the month a [NepaliCalendar] is showing, written out in date order.
 *
 * A festival that runs ten days is one line rather than ten: entries sharing an
 * [NepaliCalendarEvent.id] over consecutive days are gathered into a single row carrying the range
 * they cover. Entries without an id stay one row per day, since nothing says those days are the same
 * thing.
 *
 * Follows [state], so the list changes as the calendar is paged, and the rows covering the picked
 * day are highlighted, which is what ties the list to the grid without either one driving the other.
 * Renders as a plain column that wraps its content, so it sits inside a screen's own scrolling
 * container beside the calendar rather than scrolling on its own.
 *
 * @param state the calendar this list follows. Share the one the grid uses.
 * @param modifier the [Modifier] applied to the list.
 * @param policy the institution whose events are listed. Give the grid the same one, or the two will
 *   disagree about what is on a day.
 * @param colors the [NepaliDatePickerColors] the list themes itself from; the picked day's rows take
 *   the in-range colours, the quieter half of the selection palette.
 * @param markerColors the palette a row's dot is drawn in, one slot per kind of event.
 * @param onEventClick invoked with the event a tapped row speaks for, which for a span is its first
 *   entry in the month. Its [NepaliCalendarEvent.id] and [NepaliCalendarEvent.payload] are what an
 *   app correlates back to its own record. `null` leaves rows untappable.
 * @param emptyContent what to draw for a month with nothing named. Defaults to a single quiet line.
 *
 * Example usage:
 * ```
 * val state = rememberNepaliCalendarState()
 * val policy = remember { NepaliCalendarPolicy(provider = myEvents) }
 *
 * NepaliCalendar(state = state, policy = policy)
 * NepaliMonthEventList(
 *     state = state,
 *     policy = policy,
 *     onEventClick = { event -> openDetail(event.id) }
 * )
 * ```
 *
 * @see NepaliCalendar
 * @see NepaliDaySummary
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliMonthEventList(
    state: NepaliCalendarState,
    modifier: Modifier = Modifier,
    policy: NepaliCalendarPolicy = NepaliCalendarPolicy.Default,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    markerColors: NepaliDayMarkerColors = NepaliDatePickerDefaults.markerColors(),
    onEventClick: ((NepaliCalendarEvent) -> Unit)? = null,
    emptyContent: @Composable () -> Unit = {
        Text(
            text = state.locale.language.noEventsInMonthText,
            modifier = Modifier.padding(vertical = EventLinePadding),
            style = MaterialTheme.typography.bodySmall
        )
    }
) {
    val calendarModel = remember(state.locale) { NepaliCalendarModel(state.locale) }
    val month = state.displayedMonth
    val rows = remember(month, policy) {
        collapseEventRows(policy.eventsIn(month.year, month.month))
    }
    val selectedDate = state.selectedDate?.toSimpleDate()

    Column(modifier = modifier.fillMaxWidth()) {
        if (rows.isEmpty()) {
            emptyContent()
            return@Column
        }
        rows.forEach { row ->
            val isSelected = selectedDate != null && row.covers(selectedDate)
            NepaliEventLine(
                label = row.event.name,
                dotColor = markerColors.colorFor(row.event.kind),
                modifier = Modifier
                    .clip(RoundedCornerShape(EventRowCornerRadius))
                    .background(
                        if (isSelected) {
                            colors.dayInSelectionRangeContainerColor
                        } else {
                            Color.Transparent
                        }
                    )
                    .then(
                        if (onEventClick == null) {
                            Modifier
                        } else {
                            Modifier.clickable { onEventClick(row.event) }
                        }
                    )
                    .padding(horizontal = EventRowHorizontalPadding),
                supportingText = row.supportingText(calendarModel, state),
                leadingText = calendarModel.localizeNumber(
                    stringToLocalize = row.firstDate.dayOfMonth.toString(),
                    locale = state.locale.language
                ),
                supportingLeadingText = calendarModel.weekdayNameOf(row.firstDate, state)
            )
        }
    }
}

/**
 * The second line of a row: the days a span covers, or the Gregorian date a single day falls on,
 * which is how a Nepali calendar's reader places it against the other calendar.
 */
@Composable
private fun NepaliEventRow.supportingText(
    calendarModel: NepaliCalendarModel,
    state: NepaliCalendarState
): String {
    val language = state.locale.language
    if (isSpan) {
        val first = calendarModel.localizeNumber(firstDate.dayOfMonth.toString(), language)
        val last = calendarModel.localizeNumber(lastDate.dayOfMonth.toString(), language)
        val monthName = calendarModel.getNepaliMonthName(
            monthOfYear = lastDate.month,
            format = NameFormat.SHORT,
            language = language
        )
        return "$first - $last $monthName"
    }
    val english = calendarModel.convertToEnglishDate(
        nepaliYYYY = firstDate.year,
        nepaliMM = firstDate.month,
        nepaliDD = firstDate.dayOfMonth
    )
    return calendarModel.formatEnglishDate(
        year = english.year,
        month = english.month,
        dayOfMonth = english.dayOfMonth,
        dayOfWeek = english.dayOfWeek,
        locale = state.locale.copy(dateFormat = NepaliDateFormatStyle.LONG)
    )
}

/** The weekday name under a row's day number. */
private fun NepaliCalendarModel.weekdayNameOf(
    date: SimpleDate,
    state: NepaliCalendarState
): String = NepaliDateConverter.getWeekdayName(
    dayOfWeek = getNepaliCalendar(date).dayOfWeek,
    format = NameFormat.MEDIUM,
    language = state.locale.language
)

/** Corner rounding of a highlighted row. */
private val EventRowCornerRadius = 8.dp

/** Space a row keeps from the edges of the list. */
private val EventRowHorizontalPadding = 8.dp
