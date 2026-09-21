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

package dev.shivathapaa.nepalidatepickerkmp.embed

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventDisplayStyle
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.then
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider

/**
 * One thing on one day, as a hosted caller describes it. [colorArgb] is `0xAARRGGBB` and `0`
 * takes the palette slot the [kind] maps to; [indicate] gives the day a dot for this entry.
 *
 * [id] and [payload] are carried through untouched and handed back when the entry is tapped, which
 * is what lets a hosted app find its own record, and what makes a span of days one thing.
 */
internal class EmbeddedEventEntry(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val name: String,
    val kind: NepaliEventKind,
    val closesOffices: Boolean,
    val colorArgb: Int,
    val indicate: Boolean,
    val id: String? = null,
    val payload: String? = null
)

/**
 * What a hosted picker should mark, with the same fields both platform option bags expose.
 * Colors are `0xAARRGGBB` and `0` keeps the slot the theme resolved. Marking never blocks a day;
 * refusing days is the selectable-dates channel's job.
 */
internal class EmbeddedEventConfig(
    val weeklyOffDays: List<Int>,
    val events: List<EmbeddedEventEntry>,
    val markWeeklyOff: Boolean,
    val markEvents: Boolean,
    val tintContainer: Boolean,
    val indicateWeeklyOff: Boolean,
    val describeEvents: Boolean,
    val weeklyOffColorArgb: Int,
    val publicHolidayColorArgb: Int,
    val religiousColorArgb: Int,
    val regionalColorArgb: Int,
    val observanceColorArgb: Int,
    val markedContainerColorArgb: Int
)

/**
 * A provider over a fixed table, equal to another one built from the same events, which keeps the
 * policy and the decorator remembered against it stable while the host hands the options in again.
 */
private data class TableEventProvider(
    private val byYear: Map<Int, Set<NepaliCalendarEvent>>
) : NepaliEventProvider {
    override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
}

/** The policy this configuration describes, for the arithmetic or a selectable-date rule. */
internal fun EmbeddedEventConfig.toPolicy(): NepaliCalendarPolicy = NepaliCalendarPolicy(
    weeklyOffDays = weeklyOffDays.filterTo(mutableSetOf()) { it in 1..7 },
    provider = TableEventProvider(
        events.map { it.toEvent() }
            .groupBy { it.date.year }
            .mapValues { (_, list) -> list.toSet() }
    )
)

/** The switches, mapped one for one onto the Compose style. */
internal fun EmbeddedEventConfig.toStyle(): NepaliEventDisplayStyle = NepaliEventDisplayStyle(
    colorWeeklyOff = markWeeklyOff,
    colorEvents = markEvents,
    tintContainer = tintContainer,
    indicateWeeklyOff = indicateWeeklyOff,
    // Dots are per event here rather than per kind, so the status channel draws none of them.
    indicateKinds = emptySet(),
    describe = describeEvents
)

/** [base] with every non-zero ARGB override applied, and the theme's own slot everywhere else. */
internal fun EmbeddedEventConfig.toMarkerColors(base: NepaliDayMarkerColors): NepaliDayMarkerColors =
    base.copy(
        weeklyOffColor = weeklyOffColorArgb.toColorOrUnspecified(),
        publicHolidayColor = publicHolidayColorArgb.toColorOrUnspecified(),
        religiousColor = religiousColorArgb.toColorOrUnspecified(),
        regionalColor = regionalColorArgb.toColorOrUnspecified(),
        observanceColor = observanceColorArgb.toColorOrUnspecified(),
        markedContainerColor = markedContainerColorArgb.toColorOrUnspecified()
    )

/** One dot per event that asked for one, keyed by date and coloured by its own slot. */
internal fun EmbeddedEventConfig.dotMarkers(colors: NepaliDayMarkerColors): Map<SimpleDate, List<Color>> =
    events.filter { it.indicate }
        .groupBy { SimpleDate(it.year, it.month, it.dayOfMonth) }
        .mapValues { (_, marks) ->
            marks.map { mark ->
                if (mark.colorArgb == 0) colors.colorFor(mark.kind) else Color(mark.colorArgb)
            }
        }

/**
 * The decorator this configuration describes, or `null` when there is nothing to draw.
 *
 * The status leads and the dots follow, so a day that is both a holiday and a meeting keeps the
 * holiday's colour and the meeting's dot, exactly as the Compose side composes the two.
 */
@Composable
internal fun EmbeddedEventConfig?.toDayDecorator(): NepaliDayDecorator? {
    val config = this ?: return null
    val colors = config.toMarkerColors(NepaliDatePickerDefaults.markerColors())
    val status = NepaliDatePickerDefaults.eventDecorator(
        policy = config.toPolicy(),
        colors = colors,
        style = config.toStyle()
    )
    val dots = config.dotMarkers(colors)
    if (dots.isEmpty()) return status
    return status.then(NepaliDatePickerDefaults.dayDecorator(markers = dots))
}

private fun EmbeddedEventEntry.toEvent(): NepaliCalendarEvent = NepaliCalendarEvent(
    date = SimpleDate(year, month, dayOfMonth),
    name = name,
    kind = kind,
    closesOffices = closesOffices,
    id = id,
    payload = payload
)

/** `0` keeps whatever the theme resolved, anything else is an opaque or translucent `0xAARRGGBB`. */
private fun Int.toColorOrUnspecified(): Color =
    if (this == 0) Color.Unspecified else Color(this)
