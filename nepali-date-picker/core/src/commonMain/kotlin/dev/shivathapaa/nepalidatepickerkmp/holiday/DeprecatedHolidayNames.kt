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

package dev.shivathapaa.nepalidatepickerkmp.holiday

import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.excludingClosures

/**
 * The 3.1.0 holiday package, kept so code written against it still compiles.
 *
 * A holiday turned out to be one kind of event rather than the whole subject: the library also
 * marks festivals, school programmes, deadlines and birthdays, and whether a day is worked is now
 * [NepaliCalendarEvent.closesOffices] rather than something the taxonomy decides. The types were
 * renamed to say so.
 *
 * The types now live in `dev.shivathapaa.nepalidatepickerkmp.event`. Referring to them through the
 * old package and the old names keeps working, `NepaliWeekend` included, which only moved.
 * **Implementing** the old provider does not: an override of `holidays(year)` has to become
 * [NepaliEventProvider.events], because a Kotlin typealias renames the type and not its members.
 */

/** The 3.1.0 location of [NepaliWeekend], which was not renamed. */
@Deprecated(
    message = "Moved to dev.shivathapaa.nepalidatepickerkmp.event.",
    replaceWith = ReplaceWith("NepaliWeekend", "dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend")
)
typealias NepaliWeekend = dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend

@Deprecated(
    message = "A holiday is one kind of calendar event. Renamed to NepaliCalendarEvent, whose " +
            "closesOffices says whether the day is worked.",
    replaceWith = ReplaceWith("NepaliCalendarEvent")
)
typealias HolidayEntry = NepaliCalendarEvent

@Deprecated(
    message = "Renamed to NepaliEventKind. The constants are unchanged.",
    replaceWith = ReplaceWith("NepaliEventKind")
)
typealias HolidayKind = NepaliEventKind

@Deprecated(
    message = "Renamed to NepaliEventProvider. An implementation must rename holidays(year) to " +
            "events(year) and isHoliday(date) to closesOn(date).",
    replaceWith = ReplaceWith("NepaliEventProvider")
)
typealias NepaliHolidayProvider = NepaliEventProvider

@Deprecated(
    message = "Renamed to NepaliCalendarPolicy, since it covers every kind of event and not only " +
            "holidays.",
    replaceWith = ReplaceWith("NepaliCalendarPolicy")
)
typealias NepaliHolidayPolicy = NepaliCalendarPolicy

/** The 3.1.0 name for [NoOpEventProvider]. */
@Deprecated(
    message = "Renamed to NoOpEventProvider.",
    replaceWith = ReplaceWith("NoOpEventProvider")
)
val NoOpHolidayProvider: NepaliEventProvider get() = NoOpEventProvider

/** The 3.1.0 name for [excludingClosures]. */
@Deprecated(
    message = "Renamed to excludingClosures, which is what it does: an event that leaves the " +
            "institution open no longer blocks its day.",
    replaceWith = ReplaceWith("excludingClosures(provider)")
)
fun NepaliSelectableDates.excludingHolidays(
    provider: NepaliEventProvider,
): NepaliSelectableDates = excludingClosures(provider)
