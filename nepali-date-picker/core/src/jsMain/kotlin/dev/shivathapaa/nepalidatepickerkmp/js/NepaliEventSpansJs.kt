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

@file:OptIn(ExperimentalJsExport::class)

package dev.shivathapaa.nepalidatepickerkmp.js

import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough

/**
 * Expanding an event that runs longer than a day, for the JavaScript facade.
 *
 * An event covers one day, so a span is an array of entries rather than a range. Hand the array
 * straight to [createCalendarPolicy]: a span crossing Chaitra into Baisakh yields entries in both
 * years, and each one is reported by the year that asks for it.
 */

/**
 * [event] repeated on each of [days] consecutive days, starting on its own date.
 *
 * Every entry keeps the name, kind, `closesOffices`, `id` and `payload` of [event] and differs only
 * in its date, so give the event an `id` through [createDetailedEvent] when the days have to be
 * recognized as one thing again.
 *
 * ```js
 * const dashain = createDetailedEvent(2082, 6, 17, 'Dashain', 'religious', true, 'dashain-2082', null)
 * const policy = createCalendarPolicy([7], expandEventDays(dashain, 10))
 * ```
 *
 * @param days how many days the span covers, counting the first. `1` returns [event] alone.
 * @throws IllegalArgumentException if [days] is below 1, or if the span runs past the supported
 *   Bikram Sambat range.
 */
@JsExport
fun expandEventDays(event: NepaliEvent, days: Int): Array<NepaliEvent> =
    event.toCore().spanningDays(days).map { it.toJs() }.toTypedArray()

/**
 * [event] repeated on each day from its own date through the given end date, both ends included,
 * which is how a published holiday list or a leave request usually states a span.
 *
 * ```js
 * const leave = createDetailedEvent(2082, 6, 17, 'Annual leave', 'observance', false, 'leave-42', null)
 * const days = expandEventThrough(leave, 2082, 6, 26)
 * ```
 *
 * @throws IllegalArgumentException if the end falls before [event]'s own date, if it is not a day
 *   its month has, or if the span runs past the supported Bikram Sambat range.
 */
@JsExport
fun expandEventThrough(
    event: NepaliEvent,
    endYear: Int,
    endMonth: Int,
    endDayOfMonth: Int
): Array<NepaliEvent> = event.toCore()
    .spanningThrough(SimpleDate(endYear, endMonth, endDayOfMonth))
    .map { it.toJs() }
    .toTypedArray()
