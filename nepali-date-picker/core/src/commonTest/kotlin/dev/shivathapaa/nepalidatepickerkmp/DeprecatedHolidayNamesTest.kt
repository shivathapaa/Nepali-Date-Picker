/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:Suppress("DEPRECATION") // The point of these tests is that the deprecated names still work.

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.holiday.HolidayEntry
import dev.shivathapaa.nepalidatepickerkmp.holiday.HolidayKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliHolidayPolicy
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.NoOpHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.event.excludingClosures
import dev.shivathapaa.nepalidatepickerkmp.holiday.excludingHolidays
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliWeekend
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend as EventWeekend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The 3.1.0 holiday package after the move to `event`. Code that only *refers* to the old names,
 * through the old package, keeps compiling, which is what the deprecated aliases promise; code that
 * *implemented* `NepaliHolidayProvider` renames its overrides, which the deprecation message says.
 */
class DeprecatedHolidayNamesTest {

    @Test
    fun theOldWeekendObjectStillResolvesThroughTheOldPackage() {
        assertEquals(NepaliWeekend.Default, EventWeekend.Default)
    }

    @Test
    fun theOldTypeNamesStillNameTheNewTypes() {
        val entry: HolidayEntry = NepaliCalendarEvent(
            date = SimpleDate(2082, 1, 1),
            name = "New Year",
            kind = HolidayKind.GovernmentPublic
        )

        assertEquals(NepaliEventKind.GovernmentPublic, entry.kind)
        assertTrue(entry.closesOffices, "a public holiday still closes the day")
    }

    @Test
    fun theOldProviderNameStillNamesTheNewInterface() {
        val provider: NepaliHolidayProvider = object : NepaliEventProvider {
            override fun events(year: Int): Set<NepaliCalendarEvent> = emptySet()
        }

        assertTrue(provider.events(2082).isEmpty())
    }

    @Test
    fun theOldPolicyAndNoOpStillResolve() {
        val policy: NepaliHolidayPolicy = NepaliCalendarPolicy(provider = NoOpHolidayProvider)

        assertEquals(NoOpEventProvider, policy.provider)
        assertFalse(policy.isNonWorkingDay(SimpleDate(2082, 1, 2)))
    }

    @Test
    fun theOldSelectableWrapperForwardsToTheNewOne() {
        val closing = object : NepaliEventProvider {
            override fun events(year: Int): Set<NepaliCalendarEvent> = setOf(
                NepaliCalendarEvent(SimpleDate(2082, 1, 1), "Closed", NepaliEventKind.GovernmentPublic)
            )
        }
        val base = object : NepaliSelectableDates {}
        val date = NepaliDateConverter.getNepaliCalendar(2082, 1, 1)

        assertEquals(
            base.excludingClosures(closing).isSelectableDate(date),
            base.excludingHolidays(closing).isSelectableDate(date)
        )
        assertFalse(base.excludingHolidays(closing).isSelectableDate(date))
    }
}
