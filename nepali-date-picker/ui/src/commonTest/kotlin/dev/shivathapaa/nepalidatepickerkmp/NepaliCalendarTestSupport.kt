/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider

/** The fixtures the calendar tests share: one locale, one month, and events stated by hand. */
internal val CalendarTestLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)

/** A month with a full six-row shape in the middle of the supported range. */
internal val CalendarTestMonth = SimpleDate(2082, 6, 1)

/** A provider over a fixed list, equal to another built from the same events. */
internal data class ListEventProvider(
    private val entries: List<NepaliCalendarEvent>
) : NepaliEventProvider {
    override fun events(year: Int): Set<NepaliCalendarEvent> =
        entries.filterTo(mutableSetOf()) { it.date.year == year }
}

/** The events on [date], as a provider. */
internal fun eventsProviderOf(vararg entries: NepaliCalendarEvent): NepaliEventProvider =
    ListEventProvider(entries.toList())
