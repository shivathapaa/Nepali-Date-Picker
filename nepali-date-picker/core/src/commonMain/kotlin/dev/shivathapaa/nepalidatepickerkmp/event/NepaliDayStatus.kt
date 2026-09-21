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

package dev.shivathapaa.nepalidatepickerkmp.event

import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable

/**
 * What one day is, as far as a [NepaliCalendarPolicy] is concerned: whether the week makes it a day
 * off, and what is named on it.
 *
 * The two are kept apart because they answer different questions. A weekly off day repeats
 * fifty-two times a year and carries no name worth drawing; an event is a fact about that date
 * alone. A day can be both, and a day that is both is still one day off, so [isNonWorking] is a
 * single answer rather than a count.
 *
 * @property isWeeklyOff whether the day falls on one of the policy's weekly off days.
 * @property events everything named on the day, strongest [NepaliEventKind] first. Empty when the
 *   day carries nothing, which is the usual case even for a weekly off day.
 */
@Immutable
data class NepaliDayStatus(
    val isWeeklyOff: Boolean,
    val events: List<NepaliCalendarEvent>
) {
    /**
     * True when the institution is shut, whether the week says so or an event does.
     *
     * Only an event that [closes][NepaliCalendarEvent.closesOffices] counts, so a working day
     * carrying a programme or a meeting stays a working day, and the working-day helpers agree.
     */
    val isNonWorking: Boolean get() = isWeeklyOff || events.any { it.closesOffices }

    /**
     * The kind that describes the day best, or `null` when nothing is named on it. A day that is
     * only a weekly off day has no kind, which is what separates "it is Saturday" from "it is
     * Dashain, which happens to be a Saturday".
     */
    val primaryKind: NepaliEventKind? get() = events.firstOrNull()?.kind

    /** The names of the day's events, strongest first, for whatever the app draws or announces. */
    val names: List<String> get() = events.map { it.name }

    /** The events that actually shut the institution, which is the subset the arithmetic counts. */
    val closures: List<NepaliCalendarEvent> get() = events.filter { it.closesOffices }

    companion object {
        /** An ordinary working day: no weekly rule, nothing named. */
        val Working: NepaliDayStatus = NepaliDayStatus(isWeeklyOff = false, events = emptyList())
    }
}
