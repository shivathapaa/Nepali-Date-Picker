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

package dev.shivathapaa.nepalidatepickerkmp.android

import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the View-based option bags, with the expectations the iOS option test already holds: a
 * kind decides whether a day closes unless the caller says otherwise, and the bag maps field for
 * field onto the shape both hosts share.
 */
class NepaliEventOptionsTest {

    @Test
    fun aKindDecidesWhetherTheDayClosesUnlessTheCallerSaysOtherwise() {
        val holiday = NepaliEventInfo(2082, 6, 24, "Vijaya Dashami", NepaliEventKind.GovernmentPublic)
        val festival = NepaliEventInfo(2082, 6, 21, "Ghatasthapana", NepaliEventKind.Religious)
        val meeting = NepaliEventInfo(2082, 6, 9, "Staff meeting", NepaliEventKind.Observance)
        val workedHoliday =
            NepaliEventInfo(2082, 6, 24, "Skeleton shift", NepaliEventKind.GovernmentPublic, false)

        assertTrue(holiday.closesOffices)
        assertTrue(festival.closesOffices)
        assertFalse(meeting.closesOffices)
        assertFalse(workedHoliday.closesOffices)
    }

    @Test
    fun theBagMapsFieldForFieldOntoTheSharedShape() {
        val config = NepaliEventOptions(
            weeklyOffDays = listOf(7, 1),
            events = listOf(
                NepaliEventInfo(
                    year = 2082,
                    month = 6,
                    dayOfMonth = 9,
                    name = "Staff meeting",
                    kind = NepaliEventKind.Observance,
                    colorArgb = 0xFF8E24AA.toInt(),
                    indicate = true
                )
            ),
            markWeeklyOff = false,
            markEvents = false,
            tintContainer = true,
            indicateWeeklyOff = true,
            describeEvents = false,
            weeklyOffColorArgb = 0xFFD32F2F.toInt(),
            publicHolidayColorArgb = 0xFF8E24AA.toInt(),
            religiousColorArgb = 0xFF1E88E5.toInt(),
            regionalColorArgb = 0xFF00897B.toInt(),
            observanceColorArgb = 0xFF6D4C41.toInt(),
            markedContainerColorArgb = 0x22D32F2F
        ).toEmbeddedConfig()

        assertEquals(listOf(7, 1), config.weeklyOffDays)
        assertFalse(config.markWeeklyOff)
        assertFalse(config.markEvents)
        assertTrue(config.tintContainer)
        assertTrue(config.indicateWeeklyOff)
        assertFalse(config.describeEvents)
        assertEquals(0xFFD32F2F.toInt(), config.weeklyOffColorArgb)
        assertEquals(0xFF8E24AA.toInt(), config.publicHolidayColorArgb)
        assertEquals(0xFF1E88E5.toInt(), config.religiousColorArgb)
        assertEquals(0xFF00897B.toInt(), config.regionalColorArgb)
        assertEquals(0xFF6D4C41.toInt(), config.observanceColorArgb)
        assertEquals(0x22D32F2F, config.markedContainerColorArgb)

        val entry = config.events.single()
        assertEquals(2082, entry.year)
        assertEquals(6, entry.month)
        assertEquals(9, entry.dayOfMonth)
        assertEquals("Staff meeting", entry.name)
        assertEquals(NepaliEventKind.Observance, entry.kind)
        assertFalse(entry.closesOffices)
        assertEquals(0xFF8E24AA.toInt(), entry.colorArgb)
        assertTrue(entry.indicate)
    }

    @Test
    fun anUnconfiguredBagMarksTheNepaliOfficeWeek() {
        val config = NepaliEventOptions().toEmbeddedConfig()

        assertEquals(listOf(7), config.weeklyOffDays)
        assertTrue(config.events.isEmpty())
        assertTrue(config.markWeeklyOff)
        assertTrue(config.markEvents)
        assertFalse(config.tintContainer)
        assertFalse(config.indicateWeeklyOff)
        assertTrue(config.describeEvents)
        assertEquals(0, config.weeklyOffColorArgb)
    }

    @Test
    fun anAbsentSelectableRuleBecomesTheAllDatesRule() {
        val calendar = CustomCalendar(
            year = 2082,
            month = 6,
            dayOfMonth = 4,
            era = 2,
            firstDayOfMonth = 3,
            lastDayOfMonth = 4,
            totalDaysInMonth = 30,
            dayOfWeek = 6
        )
        val fallback = (null as NepaliSelectableDates?).orAllDates()

        assertTrue(fallback.isSelectableDate(calendar))
        assertTrue(fallback.isSelectableYear(2082))
    }

    @Test
    fun aGivenSelectableRuleIsKeptAsItIs() {
        val onlyEvenDays = object : NepaliSelectableDates {
            override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
                customCalendar.dayOfMonth % 2 == 0
        }

        assertEquals(onlyEvenDays, onlyEvenDays.orAllDates())
    }

    @Test
    fun theAppearanceProxyStartsAndResetsAtMaterialsOwnValues() {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        NepaliPickerAppearance.primaryArgb = 0xFFB1D18A.toInt()
        NepaliPickerAppearance.surfaceArgb = 0xFF12140E.toInt()

        NepaliPickerAppearance.reset()

        assertEquals(NepaliPickerBrightness.System, NepaliPickerAppearance.brightness)
        assertEquals(0, NepaliPickerAppearance.primaryArgb)
        assertEquals(0, NepaliPickerAppearance.onPrimaryArgb)
        assertEquals(0, NepaliPickerAppearance.primaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.onPrimaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.secondaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.onSecondaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.surfaceArgb)
        assertEquals(0, NepaliPickerAppearance.onSurfaceArgb)
        assertEquals(0, NepaliPickerAppearance.surfaceVariantArgb)
        assertEquals(0, NepaliPickerAppearance.onSurfaceVariantArgb)
        assertEquals(0, NepaliPickerAppearance.outlineArgb)
    }
}
