/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The calendar's option bag restates the composable's default arguments, because Kotlin defaults do
 * not survive the Objective-C bridge. These pin the values, so a default changed in `NepaliCalendar`
 * cannot leave iOS on the old one unnoticed.
 */
class NepaliCalendarViewOptionsTest {

    @Test
    fun optionsMatchTheComposableDefaults() {
        val options = NepaliCalendarViewOptions()

        assertTrue(options.showTodayButton)
        assertFalse(options.showCalendarSystemToggle)
        // The calendar fills its container, so it shows both calendars and its neighbours' days,
        // where the picker sized for a dialog shows neither.
        assertTrue(options.showAdjacentMonthDays)
        assertTrue(options.showSecondaryDates)
        assertNull(options.secondaryDateLocale, "A null locale falls back to the calendar's own.")
        assertEquals(CalendarSystem.BIKRAM_SAMBAT, options.initialCalendarSystem)
        assertFalse(options.showDaySummary)
        assertFalse(options.showMonthEvents)
    }

    @Test
    fun secondaryDates_fallBackToTheCalendarsOwnLocale() {
        val locale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)
        val options = NepaliCalendarViewOptions()

        assertEquals(locale, options.secondaryLocaleOrNull(locale))
    }

    @Test
    fun secondaryDates_keepTheLocaleTheHostNamed() {
        val calendarLocale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)
        val englishSecondary = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
        val options = NepaliCalendarViewOptions().apply {
            secondaryDateLocale = englishSecondary
        }

        assertEquals(englishSecondary, options.secondaryLocaleOrNull(calendarLocale))
    }

    @Test
    fun secondaryDatesOff_leavesOneNumberPerCell() {
        val options = NepaliCalendarViewOptions().apply { showSecondaryDates = false }

        assertNull(options.secondaryLocaleOrNull(NepaliDateLocale()))
    }
}
