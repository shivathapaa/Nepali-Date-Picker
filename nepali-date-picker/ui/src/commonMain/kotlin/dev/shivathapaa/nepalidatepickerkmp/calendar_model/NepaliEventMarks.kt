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

import androidx.compose.ui.graphics.Color
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus

/**
 * How one day's status is drawn, which is the single rule every surface marks a day by: the picker's
 * [NepaliDatePickerDefaults.eventDecorator] and the calendar's own per-month marking both end here,
 * so the two can never disagree about what a Saturday carrying a festival looks like.
 *
 * A day takes one colour. A named holiday is coloured by the strongest kind on it, and a day that is
 * only a weekly off day takes [NepaliDayMarkerColors.weeklyOffColor]. A Saturday that is also Dashain
 * is Dashain, because that is the more specific fact about it, but a Saturday carrying only an
 * observance stays a Saturday: a kind that does not close the office cannot make a closed day look
 * open.
 *
 * Returns `null` when [style] leaves nothing to draw, which keeps the cell on the grid's cheap path
 * rather than handing it a decoration that says nothing.
 */
internal fun NepaliDayStatus.toDayDecoration(
    colors: NepaliDayMarkerColors,
    style: NepaliEventDisplayStyle
): NepaliDayDecoration? {
    if (!isWeeklyOff && events.isEmpty()) return null

    val strongest = primaryKind
    // A named closure is the better name for the day, but an event that leaves the doors open never
    // makes a day the institution is shut for look like a working one. The event decides, not its
    // kind: the same kind closes one institution and not another.
    val weeklyRuleLeads = isWeeklyOff && events.none { it.closesOffices }
    val contentColor = when {
        weeklyRuleLeads && style.colorWeeklyOff -> colors.weeklyOffColor
        strongest != null && style.colorEvents -> colors.colorFor(strongest)
        isWeeklyOff && style.colorWeeklyOff -> colors.weeklyOffColor
        else -> Color.Unspecified
    }
    val indicators = buildList {
        events.forEach { if (it.kind in style.indicateKinds) add(colors.colorFor(it.kind)) }
        if (isWeeklyOff && style.indicateWeeklyOff) add(colors.weeklyOffColor)
    }
    val containerColor = if (style.tintContainer) colors.markedContainerColor else Color.Unspecified
    val description = if (style.describe && events.isNotEmpty()) {
        events.joinToString { it.name }
    } else {
        null
    }

    if (contentColor == Color.Unspecified && containerColor == Color.Unspecified &&
        indicators.isEmpty() && description == null
    ) {
        return null
    }
    return NepaliDayDecoration(
        contentColor = contentColor,
        containerColor = containerColor,
        indicators = indicators,
        contentDescription = description
    )
}

/**
 * What every day drawn in [cells] is under [policy], keyed by its Bikram Sambat date.
 *
 * Costs one [NepaliCalendarPolicy.monthStatus] pass per Bikram Sambat month the grid reaches into,
 * which is one for a Bikram Sambat month and two or three for a Gregorian one or a grid filled with
 * its neighbours' days. Asking the policy per cell instead would filter the provider's whole year
 * once for every day on screen.
 *
 * Days with no Bikram Sambat date, the ones before the conversion anchor, are absent from the map:
 * they carry no status and are drawn inert.
 */
internal fun monthStatusByDate(
    cells: List<MonthGridCell?>,
    policy: NepaliCalendarPolicy
): Map<SimpleDate, NepaliDayStatus> {
    val canonicalDates = cells.mapNotNull { it?.day?.canonical?.toSimpleDate() }
    if (canonicalDates.isEmpty()) return emptyMap()

    val statusByMonth = canonicalDates
        .map { it.year to it.month }
        .distinct()
        .associateWith { (year, month) -> policy.monthStatus(year, month) }

    return canonicalDates.associateWith { date ->
        // monthStatus answers in day order from day 1, so the day of the month is its index.
        statusByMonth[date.year to date.month]
            ?.getOrNull(date.dayOfMonth - 1)
            ?: NepaliDayStatus.Working
    }
}
