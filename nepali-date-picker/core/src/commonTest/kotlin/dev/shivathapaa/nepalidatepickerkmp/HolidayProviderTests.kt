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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.holiday.HolidayEntry
import dev.shivathapaa.nepalidatepickerkmp.holiday.HolidayKind
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliWeekend
import dev.shivathapaa.nepalidatepickerkmp.holiday.NoOpHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.holiday.excludingHolidays
import dev.shivathapaa.nepalidatepickerkmp.holiday.excludingWeekends
import dev.shivathapaa.nepalidatepickerkmp.holiday.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.holiday.workingDaysBetween
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HolidayProviderTests {

    private class FakeHolidayProvider(holidays: Set<SimpleDate>) : NepaliHolidayProvider {
        private val byYear: Map<Int, Set<HolidayEntry>> = holidays
            .groupBy { it.year }
            .mapValues { (_, dates) ->
                dates.mapTo(HashSet()) {
                    HolidayEntry(it, name = "fake", kind = HolidayKind.GovernmentPublic)
                }
            }

        override fun holidays(year: Int): Set<HolidayEntry> = byYear[year].orEmpty()
    }

    // ── NoOpHolidayProvider ────────────────────────────────────────────────

    @Test
    fun noOp_neverFlagsHoliday() {
        assertFalse(NoOpHolidayProvider.isHoliday(SimpleDate(2082, 1, 1)))
        assertEquals(emptySet(), NoOpHolidayProvider.holidays(2082))
    }

    // ── NepaliSelectableDates wrappers ─────────────────────────────────────

    @Test
    fun excludingHolidays_rejectsHolidayDate_keepsRest() {
        val holiday = SimpleDate(2082, 1, 1)
        val provider = FakeHolidayProvider(setOf(holiday))
        val base = object : NepaliSelectableDates {}
        val wrapped = base.excludingHolidays(provider)

        val holidayCal = NepaliDateConverter.getNepaliCalendar(holiday.year, holiday.month, holiday.dayOfMonth)
        val normalCal = NepaliDateConverter.getNepaliCalendar(2082, 1, 2)

        assertFalse(wrapped.isSelectableDate(holidayCal))
        assertTrue(wrapped.isSelectableDate(normalCal))
        assertTrue(wrapped.isSelectableYear(2082))
    }

    @Test
    fun excludingHolidays_respectsWrappedPredicate() {
        val provider = NoOpHolidayProvider
        val onlyEvenDays = object : NepaliSelectableDates {
            override fun isSelectableDate(customCalendar: dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar) =
                customCalendar.dayOfMonth % 2 == 0
        }
        val wrapped = onlyEvenDays.excludingHolidays(provider)
        val odd = NepaliDateConverter.getNepaliCalendar(2082, 1, 1)
        val even = NepaliDateConverter.getNepaliCalendar(2082, 1, 2)
        assertFalse(wrapped.isSelectableDate(odd))
        assertTrue(wrapped.isSelectableDate(even))
    }

    @Test
    fun excludingWeekends_default_rejectsSaturdayOnly() {
        val base = object : NepaliSelectableDates {}
        val wrapped = base.excludingWeekends()
        // Find a known Saturday and a known non-Saturday in 2082.
        for (offset in 0..7) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == 7) assertFalse(wrapped.isSelectableDate(cal), "Saturday should be rejected")
            else assertTrue(wrapped.isSelectableDate(cal), "non-Saturday should be selectable")
        }
    }

    @Test
    fun excludingWeekends_customSet_rejectsFridayAndSaturday() {
        val base = object : NepaliSelectableDates {}
        val wrapped = base.excludingWeekends(weekend = setOf(6, 7))
        for (offset in 0..7) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            val expectSelectable = cal.dayOfWeek !in setOf(6, 7)
            assertEquals(expectSelectable, wrapped.isSelectableDate(cal),
                "dayOfWeek=${cal.dayOfWeek} for offset=$offset")
        }
    }

    // ── workingDaysBetween ─────────────────────────────────────────────────

    @Test
    fun workingDaysBetween_emptyRange_returnsZero() {
        val d = SimpleDate(2082, 1, 1)
        assertEquals(0, NepaliDateConverter.workingDaysBetween(d, d, NoOpHolidayProvider))
    }

    @Test
    fun workingDaysBetween_startAfterEnd_throws() {
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.workingDaysBetween(
                SimpleDate(2082, 1, 2),
                SimpleDate(2082, 1, 1),
                NoOpHolidayProvider
            )
        }
    }

    @Test
    fun workingDaysBetween_noHolidaysNoWeekends_equalsRawSpan() {
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 1, 11) // exclusive — 10 days
        val span = NepaliDateConverter.getNepaliDaysInBetween(start, end)
        val working = NepaliDateConverter.workingDaysBetween(start, end, NoOpHolidayProvider, weekend = emptySet())
        assertEquals(span, working)
        assertEquals(10, working)
    }

    @Test
    fun workingDaysBetween_defaultWeekend_skipsOneSaturdayPerWeek() {
        val start = SimpleDate(2082, 1, 1)
        // 14 days span, default weekend (Sat only) — should skip 2 Saturdays.
        val end = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, 14).let {
            SimpleDate(it.year, it.month, it.dayOfMonth)
        }
        val working = NepaliDateConverter.workingDaysBetween(start, end, NoOpHolidayProvider)
        assertEquals(12, working) // 14 - 2 Saturdays
    }

    @Test
    fun workingDaysBetween_holidaysSkipped() {
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 1, 8) // 7-day span exclusive
        val holiday = SimpleDate(2082, 1, 3)
        val provider = FakeHolidayProvider(setOf(holiday))
        // Default weekend skips Saturdays in that window too.
        val rawSpan = NepaliDateConverter.getNepaliDaysInBetween(start, end) // 7
        var weekendCount = 0
        for (offset in 0 until rawSpan) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == 7) weekendCount++
        }
        val expected = rawSpan - weekendCount - 1 // 1 holiday
        val working = NepaliDateConverter.workingDaysBetween(start, end, provider)
        assertEquals(expected, working)
    }

    @Test
    fun workingDaysBetween_holidayOnWeekend_notDoubleCounted() {
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 1, 15)
        // Find a Saturday in the window and mark it as a holiday — should not change count.
        var saturday: SimpleDate? = null
        for (offset in 0 until 14) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == 7) { saturday = SimpleDate(cal.year, cal.month, cal.dayOfMonth); break }
        }
        requireNotNull(saturday)

        val noHoliday = NepaliDateConverter.workingDaysBetween(start, end, NoOpHolidayProvider)
        val withSaturdayMarkedHoliday = NepaliDateConverter.workingDaysBetween(
            start, end, FakeHolidayProvider(setOf(saturday))
        )
        assertEquals(noHoliday, withSaturdayMarkedHoliday)
    }

    @Test
    fun workingDaysBetween_crossingYearBoundary() {
        // 2081/12/25 → 2082/01/15 — spans year boundary.
        val start = SimpleDate(2081, 12, 25)
        val endCal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
            2081, 12, 25, 20
        )
        val end = SimpleDate(endCal.year, endCal.month, endCal.dayOfMonth)
        val working = NepaliDateConverter.workingDaysBetween(
            start, end, NoOpHolidayProvider, weekend = emptySet()
        )
        assertEquals(20, working) // 20-day span, no weekends, no holidays
    }

    // ── nextWorkingDay ─────────────────────────────────────────────────────

    @Test
    fun nextWorkingDay_alreadyWorking_returnsSame() {
        // Find a known weekday in 2082.
        for (offset in 0..7) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek != 7) {
                val d = SimpleDate(cal.year, cal.month, cal.dayOfMonth)
                assertEquals(d, NepaliDateConverter.nextWorkingDay(d, NoOpHolidayProvider))
                return
            }
        }
        error("no weekday in 8-day window")
    }

    @Test
    fun nextWorkingDay_onSaturday_skipsToSunday() {
        var sat: SimpleDate? = null
        for (offset in 0..7) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == 7) { sat = SimpleDate(cal.year, cal.month, cal.dayOfMonth); break }
        }
        requireNotNull(sat)
        val next = NepaliDateConverter.nextWorkingDay(sat, NoOpHolidayProvider)
        val nextCal = NepaliDateConverter.getNepaliCalendar(next.year, next.month, next.dayOfMonth)
        assertEquals(1, nextCal.dayOfWeek) // Sunday
    }

    @Test
    fun nextWorkingDay_onHoliday_skipsToNext() {
        val holiday = SimpleDate(2082, 1, 1)
        val provider = FakeHolidayProvider(setOf(holiday))
        val next = NepaliDateConverter.nextWorkingDay(holiday, provider)
        assertTrue(next > holiday)
    }

    @Test
    fun nextWorkingDay_allMarkedHoliday_throws() {
        // Pathological provider that flags every date as a holiday — confirms the
        // scan limit kicks in. Tests the contract that providers may override
        // isHoliday alone without populating holidays(year).
        val alwaysHoliday = object : NepaliHolidayProvider {
            override fun holidays(year: Int): Set<HolidayEntry> = emptySet()
            override fun isHoliday(date: SimpleDate): Boolean = true
        }
        assertFailsWith<IllegalStateException> {
            NepaliDateConverter.nextWorkingDay(SimpleDate(2082, 1, 1), alwaysHoliday, weekend = emptySet())
        }
    }

    // ── addWorkingDays ─────────────────────────────────────────────────────

    @Test
    fun addWorkingDays_zero_returnsSame() {
        val d = SimpleDate(2082, 1, 1)
        assertEquals(d, NepaliDateConverter.addWorkingDays(d, 0, NoOpHolidayProvider))
    }

    @Test
    fun addWorkingDays_plusOne_strictlyAfter_skipsWeekend() {
        // Find a Friday.
        var friday: SimpleDate? = null
        for (offset in 0..14) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == 6) { friday = SimpleDate(cal.year, cal.month, cal.dayOfMonth); break }
        }
        requireNotNull(friday)
        val next = NepaliDateConverter.addWorkingDays(friday, 1, NoOpHolidayProvider)
        val nextCal = NepaliDateConverter.getNepaliCalendar(next.year, next.month, next.dayOfMonth)
        assertEquals(1, nextCal.dayOfWeek) // Friday + 1 working day = Sunday (skip Saturday)
    }

    @Test
    fun addWorkingDays_negative_walksBack() {
        // Find a Sunday.
        var sunday: SimpleDate? = null
        for (offset in 0..14) {
            val cal = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == 1) { sunday = SimpleDate(cal.year, cal.month, cal.dayOfMonth); break }
        }
        requireNotNull(sunday)
        val prev = NepaliDateConverter.addWorkingDays(sunday, -1, NoOpHolidayProvider)
        val prevCal = NepaliDateConverter.getNepaliCalendar(prev.year, prev.month, prev.dayOfMonth)
        assertEquals(6, prevCal.dayOfWeek) // Sunday - 1 working day = Friday (skip Saturday going back)
    }

    @Test
    fun addWorkingDays_skipsHolidays() {
        val from = SimpleDate(2082, 1, 1)
        val nextDay = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, 1).let {
            SimpleDate(it.year, it.month, it.dayOfMonth)
        }
        // Mark nextDay as a holiday (assume it's a weekday — if it's Saturday, this asserts the skip still works since both rules apply).
        val provider = FakeHolidayProvider(setOf(nextDay))
        val target = NepaliDateConverter.addWorkingDays(from, 1, provider, weekend = emptySet())
        assertTrue(target > nextDay, "should skip the holiday at nextDay")
    }

    @Test
    fun addWorkingDays_defaultWeekendMatchesNepaliConvention() {
        // Confirm NepaliWeekend.Default == setOf(7) (Saturday only).
        assertEquals(setOf(7), NepaliWeekend.Default)
    }
}
