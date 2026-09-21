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
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind

/**
 * How much of a day's holiday status is drawn, one switch per channel.
 *
 * The defaults say a closed day is a colored number and nothing else. That leaves the dots to mean
 * one thing only, events, so a Saturday carrying a wedding reads as a red number with one dot rather
 * than as two competing marks. Turn the switches on when a product wants more: a public-holiday dot,
 * a tinted disc, or a grid where the weekly rule is not drawn at all.
 *
 * @property colorWeeklyOff whether a weekly off day takes [NepaliDayMarkerColors.weeklyOffColor].
 * @property colorEvents whether a named holiday takes the color of its [NepaliEventKind].
 * @property tintContainer whether a day with any status also takes a tinted disc behind the number,
 *   from [NepaliDayMarkerColors.markedContainerColor].
 * @property indicateWeeklyOff whether a weekly off day draws a dot. Off by default: it would repeat
 *   fifty-two times a year and say nothing the color has not already said.
 * @property indicateKinds which kinds of named holiday draw a dot. Empty by default, so holidays are
 *   told apart from events by color rather than by competing for the same three dot slots.
 * @property describe whether the day's holiday names are appended to what a screen reader announces
 *   for the cell, which is what keeps the marking from being carried by color alone.
 */
@Immutable
class NepaliEventDisplayStyle(
    val colorWeeklyOff: Boolean = true,
    val colorEvents: Boolean = true,
    val tintContainer: Boolean = false,
    val indicateWeeklyOff: Boolean = false,
    val indicateKinds: Set<NepaliEventKind> = emptySet(),
    val describe: Boolean = true
) {
    /** A copy with some switches changed; the rest keep the values they already have. */
    fun copy(
        colorWeeklyOff: Boolean = this.colorWeeklyOff,
        colorEvents: Boolean = this.colorEvents,
        tintContainer: Boolean = this.tintContainer,
        indicateWeeklyOff: Boolean = this.indicateWeeklyOff,
        indicateKinds: Set<NepaliEventKind> = this.indicateKinds,
        describe: Boolean = this.describe
    ) = NepaliEventDisplayStyle(
        colorWeeklyOff = colorWeeklyOff,
        colorEvents = colorEvents,
        tintContainer = tintContainer,
        indicateWeeklyOff = indicateWeeklyOff,
        indicateKinds = indicateKinds,
        describe = describe
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NepaliEventDisplayStyle) return false
        return colorWeeklyOff == other.colorWeeklyOff &&
                colorEvents == other.colorEvents &&
                tintContainer == other.tintContainer &&
                indicateWeeklyOff == other.indicateWeeklyOff &&
                indicateKinds == other.indicateKinds &&
                describe == other.describe
    }

    override fun hashCode(): Int {
        var result = colorWeeklyOff.hashCode()
        result = 31 * result + colorEvents.hashCode()
        result = 31 * result + tintContainer.hashCode()
        result = 31 * result + indicateWeeklyOff.hashCode()
        result = 31 * result + indicateKinds.hashCode()
        result = 31 * result + describe.hashCode()
        return result
    }

    companion object {
        /** Colour only, for both the weekly rule and named holidays. Dots stay with events. */
        val Default: NepaliEventDisplayStyle = NepaliEventDisplayStyle()
    }
}
