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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach

/** Diameter of one indicator dot. */
internal val DayIndicatorSize = 4.dp

/** Gap between two indicator dots. */
internal val DayIndicatorSpacing = 2.dp

/** Distance from the dots to the edge of the day cell they sit against. */
internal val DayIndicatorPadding = 4.dp

/** Dots drawn on a cell that holds one day number. */
internal const val MaxDayIndicators = 3

/**
 * Dots drawn on a dual-date cell, where the second calendar's number already takes the bottom-end
 * corner and the dots move to the bottom-start one.
 */
internal const val MaxDualDateDayIndicators = 2

/** The colors one day cell is finally drawn in, after the theme and a decoration are reconciled. */
@Immutable
internal data class NepaliDayVisuals(
    val containerColor: Color,
    val contentColor: Color,
    val indicators: List<Color>
)

/**
 * Reconciles [decoration] with the colors the theme already resolved for the cell.
 *
 * Selection wins: on a selected day and on a day inside a selected range the theme's container and
 * content colors are kept, and the indicators are repainted in [selectedIndicatorColor] or
 * [inRangeIndicatorColor] so they stay legible on those containers. A disabled day keeps the
 * theme's colors too and fades its indicators, which leaves the day visibly marked without
 * suggesting it can be picked. Everywhere else, including today, a color the decoration specifies
 * replaces the theme's, and one left as [Color.Unspecified] falls through to it.
 *
 * At most [maxIndicators] dots survive, so a day carrying more events than the cell can show reads
 * as "this many or more" rather than overflowing.
 */
internal fun resolveDayVisuals(
    decoration: NepaliDayDecoration?,
    themeContainerColor: Color,
    themeContentColor: Color,
    selectedIndicatorColor: Color,
    inRangeIndicatorColor: Color,
    isSelected: Boolean,
    isInRange: Boolean,
    isEnabled: Boolean,
    maxIndicators: Int = MaxDayIndicators
): NepaliDayVisuals {
    if (decoration == null) {
        return NepaliDayVisuals(themeContainerColor, themeContentColor, emptyList())
    }

    val shown = if (decoration.indicators.size > maxIndicators) {
        decoration.indicators.subList(0, maxIndicators)
    } else {
        decoration.indicators
    }
    val indicators = when {
        isSelected -> shown.map { selectedIndicatorColor }
        isInRange -> shown.map { inRangeIndicatorColor }
        !isEnabled -> shown.map { it.copy(alpha = it.alpha * DisabledAlpha) }
        else -> shown
    }

    val keepsThemeColors = isSelected || isInRange || !isEnabled
    return NepaliDayVisuals(
        containerColor = if (keepsThemeColors) {
            themeContainerColor
        } else {
            decoration.containerColor.takeOrElse { themeContainerColor }
        },
        contentColor = if (keepsThemeColors) {
            themeContentColor
        } else {
            decoration.contentColor.takeOrElse { themeContentColor }
        },
        indicators = indicators
    )
}

/**
 * The row of dots under a day number. Drawn inside the cell that is already laid out, so it changes
 * neither the size of the grid nor the touch target of a day.
 */
@Composable
internal fun NepaliDayIndicators(indicators: List<Color>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DayIndicatorSpacing)
    ) {
        indicators.fastForEach { color ->
            Spacer(modifier = Modifier.size(DayIndicatorSize).background(color, CircleShape))
        }
    }
}
