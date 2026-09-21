// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the Android half of the embedded picker's creation-parameter contract:
 * the key names, the positional entries, and the defaults an absent key falls
 * back to. The Dart half writes the same shape in `buildPickerCreationParams`.
 *
 * Numbers arrive as whatever the standard message codec decoded, so both `Int`
 * and `Long` entries appear here on purpose.
 */
internal class PickerParamsTest {

    private fun calendar(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        dayOfWeek: Int = 2
    ) = CustomCalendar(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth,
        era = 2,
        firstDayOfMonth = 1,
        lastDayOfMonth = 30,
        totalDaysInMonth = 30,
        dayOfWeek = dayOfWeek
    )

    @Test
    fun anEmptyMapFallsBackToTheFactoryDefaults() {
        val params = PickerParams(emptyMap<String, Any?>())

        assertEquals("datePicker", params.variant)
        assertNull(params.initialDate)
        assertNull(params.initialEndDate)
        assertNull(params.englishLocale)
        assertNull(params.selectableDates)
        assertNull(params.events)
        assertEquals(NepaliCalendarDefaults.NepaliYearRange, params.yearRange)
        assertEquals(CalendarSystem.BIKRAM_SAMBAT, params.calendarSystem)
        assertEquals(NepaliDatePickerLang.ENGLISH, params.language)
        assertEquals(NepaliDateFormatter.Pattern.YYYY_SLASH_MM_SLASH_DD, params.dateFormat)
        assertEquals(NepaliDateFormatStyle.MEDIUM, params.dateFormatStyle)
        assertEquals(NepaliDatePickerLang.ENGLISH, params.locale.language)
        assertEquals(true, params.bool("missing", true))
        assertEquals(7, params.int("missing", 7))
        assertEquals(56f, params.float("missing", 56f))
        assertNull(params.string("missing"))
    }

    @Test
    fun theDateEntriesDecodeAsYearMonthDay() {
        val params = PickerParams(
            mapOf(
                "variant" to "rangePicker",
                "initialDate" to listOf(2082, 6, 4),
                "initialEndDate" to listOf<Long>(2082, 6, 20)
            )
        )

        assertEquals("rangePicker", params.variant)
        assertEquals(2082, params.initialDate?.year)
        assertEquals(6, params.initialDate?.month)
        assertEquals(4, params.initialDate?.dayOfMonth)
        assertEquals(20, params.initialEndDate?.dayOfMonth)
    }

    @Test
    fun theLocaleEntryDecodesEveryAxisInOrder() {
        val params = PickerParams(
            mapOf(
                "locale" to listOf(1, 3, 2, 0, 1),
                "englishLocale" to listOf(0, 2, 0, 0, -1)
            )
        )

        with(params.locale) {
            assertEquals(NepaliDatePickerLang.NEPALI, language)
            assertEquals(NepaliDateFormatStyle.SHORT_MDY, dateFormat)
            assertEquals(NameFormat.SHORT, weekDayName)
            assertEquals(NameFormat.FULL, monthName)
            assertEquals(DigitScript.DEVANAGARI, digitScript)
        }
        with(params.englishLocale!!) {
            assertEquals(NepaliDatePickerLang.ENGLISH, language)
            assertEquals(NepaliDateFormatStyle.MEDIUM, dateFormat)
            assertNull(digitScript)
        }
    }

    @Test
    fun aYearRangeNeedsBothEndsToReplaceTheDefault() {
        assertEquals(
            2080..2090,
            PickerParams(mapOf("yearRangeStart" to 2080, "yearRangeEnd" to 2090L)).yearRange
        )
        assertEquals(
            NepaliCalendarDefaults.NepaliYearRange,
            PickerParams(mapOf("yearRangeStart" to 2080)).yearRange
        )
    }

    @Test
    fun theEventEntryDecodesMarkingColorsAndRows() {
        val params = PickerParams(
            mapOf(
                "events" to mapOf(
                    "weeklyOffDays" to listOf(7, 1),
                    "events" to listOf(
                        listOf(2082, 6, 4, "Ghatasthapana", 1, true, 0xFFD32F2F.toInt(), false),
                        listOf(2082, 6, 9, "Staff meeting", 3, false, 0, true)
                    ),
                    "markWeeklyOff" to false,
                    "tintContainer" to true,
                    "religiousColorArgb" to 0xFF1E88E5.toInt()
                )
            )
        )

        val events = params.events!!
        assertEquals(listOf(7, 1), events.weeklyOffDays)
        assertFalse(events.markWeeklyOff)
        assertTrue(events.tintContainer)
        assertTrue(events.markEvents)
        assertEquals(0xFF1E88E5.toInt(), events.religiousColorArgb)
        assertEquals(0, events.regionalColorArgb)
        with(events.events[0]) {
            assertEquals("Ghatasthapana", name)
            assertEquals(NepaliEventKind.Religious, kind)
            assertTrue(closesOffices)
            assertEquals(0xFFD32F2F.toInt(), colorArgb)
            assertFalse(indicate)
        }
        with(events.events[1]) {
            assertEquals(NepaliEventKind.Observance, kind)
            assertFalse(closesOffices)
            assertTrue(indicate)
        }
    }

    @Test
    fun aBoundedSelectableRuleRefusesOutsideItsRange() {
        val params = PickerParams(
            mapOf(
                "selectable" to mapOf(
                    "minDate" to listOf(2082, 6, 1),
                    "maxDate" to listOf(2082, 6, 20),
                    "includeMinDate" to true,
                    "includeMaxDate" to true
                )
            )
        )

        val selectable = params.selectableDates!!
        assertTrue(selectable.isSelectableDate(calendar(2082, 6, 1)))
        assertTrue(selectable.isSelectableDate(calendar(2082, 6, 20)))
        assertFalse(selectable.isSelectableDate(calendar(2082, 6, 21)))
        assertFalse(selectable.isSelectableDate(calendar(2082, 5, 30)))
    }

    @Test
    fun theWeekendAndClosureDecoratorsComposeOverTheRange() {
        val params = PickerParams(
            mapOf(
                "selectable" to mapOf(
                    "minDate" to listOf(2082, 6, 1),
                    "includeMinDate" to true,
                    "excludeWeekend" to listOf(7),
                    "policyWeeklyOffDays" to listOf<Int>(),
                    "policyEvents" to listOf(
                        listOf(2082, 6, 9, "Vijaya Dashami", 0, true)
                    )
                )
            )
        )

        val selectable = params.selectableDates!!
        assertTrue(selectable.isSelectableDate(calendar(2082, 6, 4)))
        assertFalse(selectable.isSelectableDate(calendar(2082, 6, 5, dayOfWeek = 7)))
        assertFalse(selectable.isSelectableDate(calendar(2082, 6, 9)))
        assertFalse(selectable.isSelectableDate(calendar(2081, 6, 4)))
    }

    @Test
    fun aWeekendRuleAloneStillSelectsEveryOtherDay() {
        val params = PickerParams(
            mapOf("selectable" to mapOf("excludeWeekend" to listOf(7)))
        )

        val selectable = params.selectableDates!!
        assertFalse(selectable.isSelectableDate(calendar(2082, 6, 5, dayOfWeek = 7)))
        assertTrue(selectable.isSelectableDate(calendar(1970, 1, 1)))
    }
}
