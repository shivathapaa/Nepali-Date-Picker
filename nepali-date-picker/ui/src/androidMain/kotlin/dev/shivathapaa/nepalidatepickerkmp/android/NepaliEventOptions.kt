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

package dev.shivathapaa.nepalidatepickerkmp.android

import androidx.compose.runtime.Composable
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedEventConfig
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedEventEntry
import dev.shivathapaa.nepalidatepickerkmp.embed.toDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend

/**
 * One thing on one day, as a View-based caller describes it: a public holiday, a festival, a
 * programme, a meeting. Mirrors the iOS `NepaliEventInfo` field for field.
 *
 * @property closesOffices whether the institution is shut for this. A public holiday is `true`; a
 * school programme or a meeting is `false`, and the day stays a working day. Left `null`, the
 * [kind]'s own convention applies.
 * @property colorArgb the color to draw this in as `0xAARRGGBB`, or `0` to take the palette slot
 * its [kind] maps to.
 * @property indicate whether the day gets a dot for this. Leave it off for something that only
 * gives the day its colour, such as a public holiday, and on for an app's own events, so the dots
 * keep meaning "something is scheduled".
 * @property id an identifier the app can correlate back to its own record, carried through
 * untouched and handed back when the entry is tapped. Days of one span that share it are one row of
 * a month's list rather than several.
 * @property payload anything else the app wants back with it, as an opaque string: JSON, a URL, an
 * identifier list. The library never parses it.
 */
class NepaliEventInfo(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val name: String,
    val kind: NepaliEventKind,
    closesOffices: Boolean? = null,
    val colorArgb: Int = 0,
    val indicate: Boolean = false,
    val id: String? = null,
    val payload: String? = null
) {
    val closesOffices: Boolean = closesOffices ?: kind.closesOfficesByDefault
}

/**
 * What the pickers should mark, for a View-based caller. Mirrors the iOS `NepaliEventOptions`
 * field for field, so one description themes both hosts.
 *
 * Colors are `0xAARRGGBB` integers and `0` means "keep the slot the theme resolved". The defaults
 * restate the Compose display style: a closed day is a colored number and nothing else, which
 * leaves the dots to the events that ask for one.
 *
 * Marking never blocks. To refuse the days it marks, build the same policy with
 * `NepaliCalendarPolicy` and pass `policy.asSelectableDates()` through a factory's
 * `selectableDates` parameter.
 *
 * @property weeklyOffDays weekdays the institution never opens, 1 for Sunday through 7 for
 * Saturday. Nepal's office week is `[7]`; a school closed Saturday and Sunday is `[7, 1]`.
 * A number outside 1..7 is dropped rather than throwing, matching the iOS host.
 */
class NepaliEventOptions(
    val weeklyOffDays: List<Int> = NepaliWeekend.Default.toList(),
    val events: List<NepaliEventInfo> = emptyList(),
    val markWeeklyOff: Boolean = true,
    val markEvents: Boolean = true,
    val tintContainer: Boolean = false,
    val indicateWeeklyOff: Boolean = false,
    val describeEvents: Boolean = true,
    val weeklyOffColorArgb: Int = 0,
    val publicHolidayColorArgb: Int = 0,
    val religiousColorArgb: Int = 0,
    val regionalColorArgb: Int = 0,
    val observanceColorArgb: Int = 0,
    val markedContainerColorArgb: Int = 0
)

/** These options in the shape both platform hosts share. */
internal fun NepaliEventOptions.toEmbeddedConfig(): EmbeddedEventConfig = EmbeddedEventConfig(
    weeklyOffDays = weeklyOffDays,
    events = events.map {
        EmbeddedEventEntry(
            year = it.year,
            month = it.month,
            dayOfMonth = it.dayOfMonth,
            name = it.name,
            kind = it.kind,
            closesOffices = it.closesOffices,
            colorArgb = it.colorArgb,
            indicate = it.indicate,
            id = it.id,
            payload = it.payload
        )
    },
    markWeeklyOff = markWeeklyOff,
    markEvents = markEvents,
    tintContainer = tintContainer,
    indicateWeeklyOff = indicateWeeklyOff,
    describeEvents = describeEvents,
    weeklyOffColorArgb = weeklyOffColorArgb,
    publicHolidayColorArgb = publicHolidayColorArgb,
    religiousColorArgb = religiousColorArgb,
    regionalColorArgb = regionalColorArgb,
    observanceColorArgb = observanceColorArgb,
    markedContainerColorArgb = markedContainerColorArgb
)

/**
 * The decorator these options describe, or `null` when there is nothing to draw.
 *
 * The status leads and the dots follow, so a day that is both a holiday and a meeting keeps the
 * holiday's colour and the meeting's dot, exactly as the Compose side composes the two.
 */
@Composable
internal fun NepaliEventOptions?.toDecorator(): NepaliDayDecorator? =
    this?.toEmbeddedConfig().toDayDecorator()
