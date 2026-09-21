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
import androidx.compose.ui.graphics.takeOrElse
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind

/**
 * The colors a marked day is drawn in, one slot per kind of thing a day can carry.
 *
 * Build one with [NepaliDatePickerDefaults.markerColors], which fills every slot from
 * `MaterialTheme.colorScheme`, so the palette follows the app's theme into dark mode on its own.
 * Override any slot to match a product's own palette; the rest keep the theme's value.
 *
 * The holiday slots are what [NepaliDatePickerDefaults.eventDecorator] paints the four
 * [dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind] cases in. [eventColor] and
 * [personalColor] carry no meaning of their own, and are there for an app's own categories to
 * borrow rather than inventing colors at the call site.
 *
 * @property weeklyOffColor a day the institution never opens, a Saturday in most of Nepal and both
 *   Saturday and Sunday in many schools. Repeats every week, so it is the quietest thing a day can
 *   be, even though it shares `colorScheme.error` with a public holiday by default.
 * @property publicHolidayColor a day offices close. The loudest slot, `colorScheme.error` by default.
 * @property religiousColor a religious or cultural festival.
 * @property regionalColor a holiday kept in one province or district rather than nationally.
 * @property observanceColor a day that is recognized but keeps offices open.
 * @property eventColor a general-purpose event slot for an app's own categories.
 * @property personalColor a second general-purpose slot, for a birthday or a reminder.
 * @property markedContainerColor the disc behind a day that is tinted whole rather than dotted.
 */
@Immutable
class NepaliDayMarkerColors(
    val weeklyOffColor: Color,
    val publicHolidayColor: Color,
    val religiousColor: Color,
    val regionalColor: Color,
    val observanceColor: Color,
    val eventColor: Color,
    val personalColor: Color,
    val markedContainerColor: Color
) {
    /**
     * The slot [kind] is painted in, which is what keeps a grid's dots, an event list's rows and an
     * app's own chrome calling the same kind the same colour.
     */
    fun colorFor(kind: NepaliEventKind): Color = when (kind) {
        NepaliEventKind.GovernmentPublic -> publicHolidayColor
        NepaliEventKind.Religious -> religiousColor
        NepaliEventKind.Regional -> regionalColor
        NepaliEventKind.Observance -> observanceColor
    }

    /**
     * A copy of this palette with some slots replaced. A slot left as [Color.Unspecified] keeps
     * the value it already has.
     */
    fun copy(
        weeklyOffColor: Color = this.weeklyOffColor,
        publicHolidayColor: Color = this.publicHolidayColor,
        religiousColor: Color = this.religiousColor,
        regionalColor: Color = this.regionalColor,
        observanceColor: Color = this.observanceColor,
        eventColor: Color = this.eventColor,
        personalColor: Color = this.personalColor,
        markedContainerColor: Color = this.markedContainerColor
    ) = NepaliDayMarkerColors(
        weeklyOffColor.takeOrElse { this.weeklyOffColor },
        publicHolidayColor.takeOrElse { this.publicHolidayColor },
        religiousColor.takeOrElse { this.religiousColor },
        regionalColor.takeOrElse { this.regionalColor },
        observanceColor.takeOrElse { this.observanceColor },
        eventColor.takeOrElse { this.eventColor },
        personalColor.takeOrElse { this.personalColor },
        markedContainerColor.takeOrElse { this.markedContainerColor }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NepaliDayMarkerColors) return false
        return weeklyOffColor == other.weeklyOffColor &&
                publicHolidayColor == other.publicHolidayColor &&
                religiousColor == other.religiousColor &&
                regionalColor == other.regionalColor &&
                observanceColor == other.observanceColor &&
                eventColor == other.eventColor &&
                personalColor == other.personalColor &&
                markedContainerColor == other.markedContainerColor
    }

    override fun hashCode(): Int {
        var result = weeklyOffColor.hashCode()
        result = 31 * result + publicHolidayColor.hashCode()
        result = 31 * result + religiousColor.hashCode()
        result = 31 * result + regionalColor.hashCode()
        result = 31 * result + observanceColor.hashCode()
        result = 31 * result + eventColor.hashCode()
        result = 31 * result + personalColor.hashCode()
        result = 31 * result + markedContainerColor.hashCode()
        return result
    }
}
