/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa).
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

package dev.shivathapaa.nepalidatepickerkmp.embed

import androidx.compose.ui.graphics.Color
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the event description both platform hosts hand the Compose decorator: which days the
 * policy closes, which switches reach the display style, which colours override the theme's,
 * and which events earn a dot.
 */
class EmbeddedEventDecoratorTest {

    private val base = NepaliDayMarkerColors(
        weeklyOffColor = Color(0xFF111111),
        publicHolidayColor = Color(0xFF222222),
        religiousColor = Color(0xFF333333),
        regionalColor = Color(0xFF444444),
        observanceColor = Color(0xFF555555),
        eventColor = Color(0xFF666666),
        personalColor = Color(0xFF777777),
        markedContainerColor = Color(0xFF888888)
    )

    private fun entry(
        dayOfMonth: Int,
        name: String,
        kind: NepaliEventKind = NepaliEventKind.GovernmentPublic,
        closesOffices: Boolean = true,
        colorArgb: Int = 0,
        indicate: Boolean = false
    ) = EmbeddedEventEntry(
        year = 2082,
        month = 6,
        dayOfMonth = dayOfMonth,
        name = name,
        kind = kind,
        closesOffices = closesOffices,
        colorArgb = colorArgb,
        indicate = indicate
    )

    private fun config(
        weeklyOffDays: List<Int> = listOf(7),
        events: List<EmbeddedEventEntry> = emptyList(),
        markWeeklyOff: Boolean = true,
        markEvents: Boolean = true,
        tintContainer: Boolean = false,
        indicateWeeklyOff: Boolean = false,
        describeEvents: Boolean = true,
        weeklyOffColorArgb: Int = 0,
        publicHolidayColorArgb: Int = 0,
        religiousColorArgb: Int = 0,
        regionalColorArgb: Int = 0,
        observanceColorArgb: Int = 0,
        markedContainerColorArgb: Int = 0
    ) = EmbeddedEventConfig(
        weeklyOffDays = weeklyOffDays,
        events = events,
        markWeeklyOff = markWeeklyOff,
        markEvents = markEvents,
        tintContainer = tintContainer,
        indicateWeeklyOff = indicateWeeklyOff,
        describeEvents = describeEvents,
        weeklyOffColorArgb = weeklyOffColorArgb,
        publicHolidayColorArgb = publicHolidayColorArgb,
        religiousColorArgb = religiousColorArgb,
        regionalColorArgb = regionalColorArgb,
        observanceColorArgb = observanceColorArgb,
        markedContainerColorArgb = markedContainerColorArgb
    )

    @Test
    fun thePolicyKeepsTheWeekendAndGroupsEventsByYear() {
        val policy = config(
            weeklyOffDays = listOf(7, 1),
            events = listOf(entry(24, "Vijaya Dashami"))
        ).toPolicy()

        assertEquals(setOf(7, 1), policy.weeklyOffDays)
        assertEquals(1, policy.provider.events(2082).size)
        assertTrue(policy.provider.events(2081).isEmpty())
    }

    @Test
    fun aWeekdayOutsideTheWeekIsDroppedRatherThanThrowing() {
        val policy = config(weeklyOffDays = listOf(0, 7, 8)).toPolicy()

        assertEquals(setOf(7), policy.weeklyOffDays)
    }

    @Test
    fun twoConfigurationsOverTheSameRowsDescribeEqualPolicies() {
        val rows = listOf(entry(24, "Vijaya Dashami"))

        assertEquals(config(events = rows).toPolicy(), config(events = rows).toPolicy())
    }

    @Test
    fun theSwitchesMapOneForOneOntoTheStyle() {
        val style = config(
            markWeeklyOff = false,
            markEvents = false,
            tintContainer = true,
            indicateWeeklyOff = true,
            describeEvents = false
        ).toStyle()

        assertFalse(style.colorWeeklyOff)
        assertFalse(style.colorEvents)
        assertTrue(style.tintContainer)
        assertTrue(style.indicateWeeklyOff)
        assertFalse(style.describe)
        assertTrue(
            style.indicateKinds.isEmpty(),
            "dots are per event here, so the status channel draws none"
        )
    }

    @Test
    fun aZeroColorKeepsTheThemesOwnSlot() {
        val colors = config().toMarkerColors(base)

        assertEquals(base.weeklyOffColor, colors.weeklyOffColor)
        assertEquals(base.publicHolidayColor, colors.publicHolidayColor)
        assertEquals(base.markedContainerColor, colors.markedContainerColor)
        assertEquals(base.eventColor, colors.eventColor, "slots with no override stay as they were")
    }

    @Test
    fun everyOverrideReachesTheSlotItNames() {
        val colors = config(
            weeklyOffColorArgb = 0xFFD32F2F.toInt(),
            publicHolidayColorArgb = 0xFF8E24AA.toInt(),
            religiousColorArgb = 0xFF1E88E5.toInt(),
            regionalColorArgb = 0xFF00897B.toInt(),
            observanceColorArgb = 0xFF6D4C41.toInt(),
            markedContainerColorArgb = 0x22D32F2F
        ).toMarkerColors(base)

        assertEquals(Color(0xFFD32F2F), colors.weeklyOffColor)
        assertEquals(Color(0xFF8E24AA), colors.publicHolidayColor)
        assertEquals(Color(0xFF1E88E5), colors.religiousColor)
        assertEquals(Color(0xFF00897B), colors.regionalColor)
        assertEquals(Color(0xFF6D4C41), colors.observanceColor)
        assertEquals(Color(0x22D32F2F), colors.markedContainerColor)
    }

    @Test
    fun onlyTheEventsThatAskedForADotGetOne() {
        val markers = config(
            events = listOf(
                entry(24, "Vijaya Dashami"),
                entry(9, "Staff meeting", NepaliEventKind.Observance, closesOffices = false, indicate = true)
            )
        ).dotMarkers(base)

        assertEquals(setOf(SimpleDate(2082, 6, 9)), markers.keys)
        assertEquals(listOf(base.observanceColor), markers.getValue(SimpleDate(2082, 6, 9)))
    }

    @Test
    fun aDotTakesItsOwnColorOverTheKindsSlot() {
        val markers = config(
            events = listOf(
                entry(9, "Exam", NepaliEventKind.Observance, colorArgb = 0xFF1E88E5.toInt(), indicate = true)
            )
        ).dotMarkers(base)

        assertEquals(listOf(Color(0xFF1E88E5)), markers.getValue(SimpleDate(2082, 6, 9)))
    }

    @Test
    fun twoDottedEventsOnOneDayKeepBothDots() {
        val markers = config(
            events = listOf(
                entry(9, "Meeting", NepaliEventKind.Observance, indicate = true),
                entry(9, "Exam", NepaliEventKind.Regional, indicate = true)
            )
        ).dotMarkers(base)

        assertEquals(
            listOf(base.observanceColor, base.regionalColor),
            markers.getValue(SimpleDate(2082, 6, 9))
        )
    }
}
