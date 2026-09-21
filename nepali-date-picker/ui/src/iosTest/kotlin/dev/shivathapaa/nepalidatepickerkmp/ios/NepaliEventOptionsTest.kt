/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.ui.graphics.Color
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliEventDisplayStyle
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Swift describes what to mark in plain types, because Compose colors and Kotlin defaults do not
 * cross the Objective-C bridge. These pin that translation: the defaults have to match the Compose
 * ones they restate, and an ARGB integer has to reach the same color a Compose caller would pass.
 */
class NepaliEventOptionsTest {

    private val themeColors = NepaliDayMarkerColors(
        weeklyOffColor = Color(0xFF111111),
        publicHolidayColor = Color(0xFF222222),
        religiousColor = Color(0xFF333333),
        regionalColor = Color(0xFF444444),
        observanceColor = Color(0xFF888888),
        eventColor = Color(0xFF555555),
        personalColor = Color(0xFF666666),
        markedContainerColor = Color(0xFF777777)
    )

    private fun event(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        name: String,
        kind: NepaliEventKind,
        closes: Boolean = kind.closesOfficesByDefault,
        argb: Int = 0,
        indicate: Boolean = false,
        id: String? = null,
        payload: String? = null
    ) = NepaliEventInfo(year, month, dayOfMonth, name, kind, closes, argb, indicate)
        .also {
            it.id = id
            it.payload = payload
        }

    @Test
    fun anEntrysIdAndPayload_reachThePolicyUntouched() {
        val options = NepaliEventOptions()
        options.events = listOf(
            event(
                year = 2082,
                month = 6,
                dayOfMonth = 17,
                name = "Indra Jatra",
                kind = NepaliEventKind.Religious,
                id = "indra-jatra",
                payload = """{"venue":"Basantapur"}"""
            )
        )

        val onThatDay = options.toPolicy().eventsOn(SimpleDate(2082, 6, 17)).single()

        // The host hands these back when the day or the line is tapped, so an app finds its own
        // record rather than a name it has to match on.
        assertEquals("indra-jatra", onThatDay.id)
        assertEquals("""{"venue":"Basantapur"}""", onThatDay.payload)
    }

    @Test
    fun defaultsMatchTheComposeOnesTheyRestate() {
        val options = NepaliEventOptions()
        val style = NepaliEventDisplayStyle.Default

        assertEquals(NepaliWeekend.Default.toList(), options.weeklyOffDays)
        assertEquals(style.colorWeeklyOff, options.markWeeklyOff)
        assertEquals(style.colorEvents, options.markEvents)
        assertEquals(style.tintContainer, options.tintContainer)
        assertEquals(style.indicateWeeklyOff, options.indicateWeeklyOff)
        assertEquals(style.describe, options.describeEvents)
        assertTrue(options.events.isEmpty())
    }

    @Test
    fun theSwitchesMapOneForOne() {
        val options = NepaliEventOptions().apply {
            markWeeklyOff = false
            markEvents = false
            tintContainer = true
            indicateWeeklyOff = true
            describeEvents = false
        }

        val style = options.toStyle()

        assertFalse(style.colorWeeklyOff)
        assertFalse(style.colorEvents)
        assertTrue(style.tintContainer)
        assertTrue(style.indicateWeeklyOff)
        assertTrue(style.indicateKinds.isEmpty(), "dots are per event on iOS, not per kind")
        assertFalse(style.describe)
    }

    @Test
    fun aWeekWrittenToAnotherConventionIsDroppedRatherThanThrowing() {
        // A Swift literal cannot be checked when it is written, and a view controller that threw
        // while being built would take the host screen down with it.
        val options = NepaliEventOptions().apply { weeklyOffDays = listOf(0, 6, 7, 8) }

        assertEquals(setOf(6, 7), options.toPolicy().weeklyOffDays)
    }

    @Test
    fun eventsReachThePolicyUnderTheirOwnYear() {
        val options = NepaliEventOptions().apply {
            events = listOf(
                event(2082, 6, 3, "Constitution Day", NepaliEventKind.GovernmentPublic),
                event(2083, 1, 1, "New Year", NepaliEventKind.GovernmentPublic)
            )
        }

        val policy = options.toPolicy()

        assertEquals(
            listOf("Constitution Day"),
            policy.eventsOn(SimpleDate(2082, 6, 3)).map { it.name }
        )
        assertEquals(listOf("New Year"), policy.eventsIn(2083, 1).map { it.name })
        assertTrue(policy.eventsIn(2082, 1).isEmpty())
    }

    @Test
    fun anEventSaysForItselfWhetherItClosesTheDay() {
        val options = NepaliEventOptions().apply {
            weeklyOffDays = emptyList()
            events = listOf(
                event(2082, 6, 3, "Annual programme", NepaliEventKind.Observance, closes = false),
                event(2082, 6, 4, "Sarkari bida", NepaliEventKind.GovernmentPublic, closes = true)
            )
        }

        val policy = options.toPolicy()

        assertFalse(policy.isNonWorkingDay(SimpleDate(2082, 6, 3)))
        assertTrue(policy.isNonWorkingDay(SimpleDate(2082, 6, 4)))
    }

    @Test
    fun aZeroArgbKeepsTheThemeSlotAndAnythingElseReplacesIt() {
        val options = NepaliEventOptions().apply {
            publicHolidayColorArgb = 0xFFB3261E.toInt()
            observanceColorArgb = 0
        }

        val colors = options.toMarkerColors(themeColors)

        assertEquals(Color(0xFFB3261E), colors.publicHolidayColor)
        assertEquals(themeColors.observanceColor, colors.observanceColor)
        assertEquals(themeColors.weeklyOffColor, colors.weeklyOffColor)
    }

    @Test
    fun everyColourSlotCanBeOverridden() {
        val options = NepaliEventOptions().apply {
            weeklyOffColorArgb = 0xFF000001.toInt()
            publicHolidayColorArgb = 0xFF000002.toInt()
            religiousColorArgb = 0xFF000003.toInt()
            regionalColorArgb = 0xFF000004.toInt()
            observanceColorArgb = 0xFF000005.toInt()
            markedContainerColorArgb = 0xFF000006.toInt()
        }

        val colors = options.toMarkerColors(themeColors)

        assertEquals(Color(0xFF000001), colors.weeklyOffColor)
        assertEquals(Color(0xFF000002), colors.publicHolidayColor)
        assertEquals(Color(0xFF000003), colors.religiousColor)
        assertEquals(Color(0xFF000004), colors.regionalColor)
        assertEquals(Color(0xFF000005), colors.observanceColor)
        assertEquals(Color(0xFF000006), colors.markedContainerColor)
        assertEquals(themeColors.eventColor, colors.eventColor, "no slot for it, so it is untouched")
    }

    @Test
    fun onlyTheEventsThatAskForADotGetOne() {
        val options = NepaliEventOptions().apply {
            events = listOf(
                event(2082, 6, 5, "Standup", NepaliEventKind.Observance, indicate = true, argb = 0xFF42A5F5.toInt()),
                event(2082, 6, 5, "Birthday", NepaliEventKind.Observance, indicate = true, argb = 0xFFFF7043.toInt()),
                event(2082, 6, 5, "Sarkari bida", NepaliEventKind.GovernmentPublic, closes = true),
                event(2082, 6, 9, "Untinted", NepaliEventKind.Religious, indicate = true)
            )
        }

        val dots = options.dotMarkers(themeColors)

        assertEquals(
            listOf(Color(0xFF42A5F5), Color(0xFFFF7043)),
            dots[SimpleDate(2082, 6, 5)],
            "the holiday colours the day and takes no dot"
        )
        assertEquals(
            listOf(themeColors.religiousColor),
            dots[SimpleDate(2082, 6, 9)],
            "a zero colour falls back to the slot its kind maps to"
        )
    }

    @Test
    fun anEmptyWeekIsKeptRatherThanFallingBackToTheDefault() {
        val options = NepaliEventOptions().apply { weeklyOffDays = emptyList() }

        assertTrue(options.toPolicy().weeklyOffDays.isEmpty(), "an institution may never close")
    }

    @Test
    fun describeOffMeansTheNamesAreNotAnnounced() {
        val options = NepaliEventOptions().apply { describeEvents = false }

        assertFalse(options.toStyle().describe)
    }

    @Test
    fun noEventsMeansNoDots() {
        assertTrue(NepaliEventOptions().dotMarkers(themeColors).isEmpty())
    }

    @Test
    fun aDayCarryingOnlyNonIndicatingEventsGetsNoDotEntry() {
        val options = NepaliEventOptions().apply {
            events = listOf(event(2082, 6, 3, "Sarkari bida", NepaliEventKind.GovernmentPublic))
        }

        assertTrue(
            options.dotMarkers(themeColors).isEmpty(),
            "a holiday colours its day without spending a dot slot"
        )
    }

    @Test
    fun eventsSpreadAcrossYearsEachReachTheirOwnYear() {
        val options = NepaliEventOptions().apply {
            events = listOf(
                event(2082, 12, 30, "Year end", NepaliEventKind.GovernmentPublic),
                event(2083, 1, 1, "Year start", NepaliEventKind.GovernmentPublic)
            )
        }
        val policy = options.toPolicy()

        assertEquals(listOf("Year end"), policy.eventsOn(SimpleDate(2082, 12, 30)).map { it.name })
        assertEquals(listOf("Year start"), policy.eventsOn(SimpleDate(2083, 1, 1)).map { it.name })
    }

    @Test
    fun anEmptyOptionsObjectStillDescribesTheDefaultWeek() {
        val policy = NepaliEventOptions().toPolicy()

        assertEquals(NepaliWeekend.Default, policy.weeklyOffDays)
        assertTrue(policy.eventsIn(2082, 1).isEmpty())
    }
}
