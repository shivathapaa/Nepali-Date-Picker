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
import androidx.compose.ui.graphics.Color

/**
 * What a [NepaliDayDecorator] adds to one day cell.
 *
 * Colors left as [Color.Unspecified] keep whatever the picker's
 * [NepaliDatePickerColors] would have used, so a decoration can mark a day with dots alone, recolor
 * its number alone, or both. None of it changes the cell's size or its touch target.
 *
 * A decoration never overrides selection: on a selected day, and on a day inside a selected range,
 * the theme's container and content colors win and only the indicators survive, recolored to stay
 * legible on that container. On a disabled day the colors are dropped as well and the indicators
 * fade with the rest of the cell.
 *
 * @property contentColor color of the day number.
 * @property containerColor color of the disc behind the day number.
 * @property indicators one color per dot drawn under the day number, in order. Three are drawn at
 *   most, two in a dual-date grid where the second calendar's number already occupies a corner, so
 *   a longer list reads as "this many or more".
 * @property contentDescription text appended to the date a screen reader announces for the cell,
 *   which is what keeps the marking from being color-only. Use the event's own name.
 */
@Immutable
data class NepaliDayDecoration(
    val contentColor: Color = Color.Unspecified,
    val containerColor: Color = Color.Unspecified,
    val indicators: List<Color> = emptyList(),
    val contentDescription: String? = null
)
