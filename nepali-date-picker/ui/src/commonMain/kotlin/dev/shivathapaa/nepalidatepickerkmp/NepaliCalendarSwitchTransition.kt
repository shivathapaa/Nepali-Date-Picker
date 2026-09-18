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

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp

/**
 * Fades and scales this content in every time [calendarKey] changes, for the `B.S.` / `A.D.` switch.
 *
 * The content itself swaps in one frame and then arrives, rather than cross-dissolving with what it
 * replaced. That is deliberate: the calendar body owns a month pager, a year overlay and the snapshot
 * flow that reports the visible month, all of which read a displayed month that has *already* moved
 * to the new calendar by the time a switch is rendered. Keeping the outgoing calendar composed
 * alongside the new one hands its adapter a month from the other calendar, which is not merely wrong
 * but unrepresentable: Bikram Sambat has no Poush 30, so the converter throws.
 *
 * A fade with a slight scale rather than a slide, because paging months is already a horizontal slide
 * and the calendar/typed-input toggle is a vertical one; a third direction would read as one of those
 * instead of as what a calendar switch is, the same day written another way. This is also exactly
 * what the web components do, so the two platforms move the same way.
 *
 * Nothing here needs an opt-out. The first composition does not animate, a [calendarKey] that never
 * changes never animates, and the duration is driven by the composition's `MotionDurationScale`,
 * which follows the platform's own "remove animations" setting.
 */
@Composable
internal fun Modifier.nepaliCalendarSwitchAppearance(calendarKey: Any?): Modifier {
    val progress = remember { Animatable(1f) }
    // A picker should not fade in merely because it appeared; only a change earns the motion.
    var seenKey by remember { mutableStateOf(calendarKey) }

    LaunchedEffect(calendarKey) {
        if (calendarKey == seenKey) return@LaunchedEffect
        seenKey = calendarKey
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = CalendarSwitchDurationMillis,
                easing = CalendarSwitchEasing
            )
        )
    }

    // Read inside the layer block, so every frame stays in the draw phase: the animation moves the
    // calendar without recomposing the grid behind it.
    return graphicsLayer {
        val shown = progress.value
        alpha = shown
        val scale = lerp(CalendarSwitchInitialScale, 1f, shown)
        scaleX = scale
        scaleY = scale
    }
}

/** How long the arriving calendar takes to settle. */
private const val CalendarSwitchDurationMillis = 220

/** How small the arriving calendar starts, so it settles into place rather than appearing flat. */
private const val CalendarSwitchInitialScale = 0.94f

/** Decelerating, so the calendar arrives quickly and comes to rest gently. */
private val CalendarSwitchEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
