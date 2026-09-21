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
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/**
 * Something that happens on one Bikram Sambat day: a public holiday, a festival, a school
 * programme, a deadline, a birthday.
 *
 * A holiday is an event like any other. What separates them is [closesOffices], not the name: two
 * days can both be called Dashain and only one of them close the school. That is why the flag lives
 * here rather than on [kind], where a taxonomy would have to decide for every institution at once.
 *
 * @property date Bikram Sambat date the event falls on. One day per entry, so a ten-day festival is
 *   ten entries, which [spanningDays] and [spanningThrough] build from one.
 * @property name Display name, for example "Dashain - Vijaya Dashami" or "Sprint review".
 * @property kind Category, which is what a calendar colors by. See [NepaliEventKind].
 * @property closesOffices Whether the institution is shut for this. Defaults to what [kind] usually
 *   means, so a `GovernmentPublic` entry closes the day and an `Observance` does not, and either can
 *   be overridden: a regional holiday closes one district and not the next, and a school programme
 *   closes nothing at all.
 * @property id An identifier the app can correlate back to its own record, carried through
 *   untouched. The library never reads it.
 * @property payload Anything else the app wants back when the day is tapped, as an opaque string:
 *   JSON, a URL, an identifier list. The library never parses it, which is what keeps `:core` free
 *   of a JSON dependency and able to compile on every target.
 *
 *   Pictures are the usual reason to reach for it. Nothing here loads or draws an image: keep the
 *   URL in the payload, read it back when the day or the line is tapped, and render it with
 *   whatever the app already uses, an `AsyncImage`, an `<img>`, a background, an icon. The same
 *   string arrives unchanged on Swift, on a View-based Android host, in Flutter and in the browser.
 */
@Immutable
data class NepaliCalendarEvent(
    val date: SimpleDate,
    val name: String,
    val kind: NepaliEventKind,
    val closesOffices: Boolean = kind.closesOfficesByDefault,
    val id: String? = null,
    val payload: String? = null
)

/**
 * What kind of thing an event is, which is what a calendar colors by.
 *
 * Deliberately narrow, because adding a case is a breaking change. An app with its own taxonomy
 * picks the closest kind and keeps its own category in
 * [NepaliCalendarEvent.payload]; whether the day is worked is [NepaliCalendarEvent.closesOffices]'s
 * job either way.
 */
enum class NepaliEventKind {
    /** Bank / government office is closed. Sarkari bida. */
    GovernmentPublic,

    /** Religious or cultural, Dashain, Tihar, Holi, Id, Christmas, and the like. */
    Religious,

    /** Province- or district-level, not nationally observed. */
    Regional,

    /** Recognized but ordinarily worked: World Health Day, a school programme, a meeting. */
    Observance;

    /**
     * How strongly a kind describes a day when several land on it, lowest first. A day the offices
     * close is called that before anything else it also happens to be, and an observance yields to
     * everything. Sorting and color resolution both read this, so the two never disagree.
     */
    val priority: Int
        get() = when (this) {
            GovernmentPublic -> 0
            Religious -> 1
            Regional -> 2
            Observance -> 3
        }

    /**
     * Whether an event of this kind usually shuts the institution, and so what
     * [NepaliCalendarEvent.closesOffices] defaults to. Only a starting point: the event itself has
     * the final say, because the same kind closes one place and not another.
     */
    val closesOfficesByDefault: Boolean
        get() = when (this) {
            GovernmentPublic, Religious, Regional -> true
            Observance -> false
        }
}
