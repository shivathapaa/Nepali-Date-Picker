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
import androidx.compose.runtime.Immutable
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventDisplayStyle
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy

/**
 * What a hosted calendar needs to mark its days, in the four pieces the calendar takes.
 *
 * A picker receives its marks as one decorator, but a calendar takes the policy itself, so that the
 * days it draws, the status it reports on a tap and the events its lists write out all come from one
 * source. The per-event dots a host asks for stay a decorator, laid over the policy's own marks.
 *
 * @property policy the institution the calendar describes.
 * @property markerColors the palette a marked day is drawn in.
 * @property style how much of a day's status is drawn.
 * @property dotDecorator the host's own dots, or `null` when it asked for none.
 */
@Immutable
internal class EmbeddedCalendarMarks(
    val policy: NepaliCalendarPolicy,
    val markerColors: NepaliDayMarkerColors,
    val style: NepaliEventDisplayStyle,
    val dotDecorator: NepaliDayDecorator?
)

/**
 * The marks this configuration describes. A host that passed no events gets the plain weekly rule,
 * Saturday off, so a calendar always knows what a closed day is even before any data is wired.
 */
@Composable
internal fun EmbeddedEventConfig?.toCalendarMarks(): EmbeddedCalendarMarks {
    val config = this
    val colors = config?.toMarkerColors(NepaliDatePickerDefaults.markerColors())
        ?: NepaliDatePickerDefaults.markerColors()
    val dots = config?.dotMarkers(colors).orEmpty()
    return EmbeddedCalendarMarks(
        policy = config?.toPolicy() ?: NepaliCalendarPolicy.Default,
        markerColors = colors,
        style = config?.toStyle() ?: NepaliEventDisplayStyle.Default,
        dotDecorator = if (dots.isEmpty()) {
            null
        } else {
            NepaliDatePickerDefaults.dayDecorator(markers = dots)
        }
    )
}
