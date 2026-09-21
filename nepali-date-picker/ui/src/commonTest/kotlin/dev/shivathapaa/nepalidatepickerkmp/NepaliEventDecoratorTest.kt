/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTestApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayInfo
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.then
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val Sunday = 1
private const val Saturday = 7

/**
 * What the ready-made holiday decorator draws: one colour per day, dots only when a style asks for
 * them, and the two channels layered by [then].
 */
class NepaliEventDecoratorTest {

    private class ListProvider(private val entries: List<NepaliCalendarEvent>) : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> =
            entries.filterTo(mutableSetOf()) { it.date.year == year }
    }

    /** The first date on or after 2082/01/01 falling on [dayOfWeek]. */
    private fun dateOn(dayOfWeek: Int): CustomCalendar {
        for (offset in 0..7) {
            val cal = NepaliDateConverter
                .getNepaliCalendarAfterAdditionOrSubtraction(2082, 1, 1, offset)
            if (cal.dayOfWeek == dayOfWeek) return cal
        }
        error("no day of week $dayOfWeek within a week of 2082/01/01")
    }

    private fun infoFor(date: CustomCalendar) = NepaliDayInfo(
        date = date,
        displayedDate = date,
        isToday = false,
        isSelected = false,
        isInRange = false,
        isEnabled = true,
        isAdjacentMonth = false
    )

    /** Runs [build] inside a composition and hands back the decoration for [date]. */
    private fun decorationFor(
        date: CustomCalendar,
        build: @Composable () -> NepaliDayDecorator
    ): NepaliDayDecoration? {
        var decoration: NepaliDayDecoration? = null
        var resolved = false
        runComposeUiTest {
            setContent {
                val decorator = build()
                decoration = decorator.decorate(infoFor(date))
                resolved = true
            }
        }
        assertTrue(resolved, "the decorator never ran")
        return decoration
    }

    @Test
    fun weeklyOffDay_isColouredAndNotDotted() {
        val saturday = dateOn(Saturday)
        var expected = Color.Unspecified

        val decoration = decorationFor(saturday) {
            expected = MaterialTheme.colorScheme.error
            NepaliDatePickerDefaults.eventDecorator(policy = NepaliCalendarPolicy.Default)
        }

        assertNotNull(decoration)
        assertEquals(expected, decoration.contentColor)
        assertTrue(decoration.indicators.isEmpty(), "a weekly off day draws no dot by default")
        assertNull(decoration.contentDescription, "there is no name to announce")
    }

    @Test
    fun plainWorkingDay_isLeftAlone() {
        val decoration = decorationFor(dateOn(2)) {
            NepaliDatePickerDefaults.eventDecorator(policy = NepaliCalendarPolicy.Default)
        }

        assertNull(decoration)
    }

    @Test
    fun schoolWeek_coloursSundayToo() {
        val schoolPolicy = NepaliCalendarPolicy(weeklyOffDays = setOf(Saturday, Sunday))

        val sunday = decorationFor(dateOn(Sunday)) {
            NepaliDatePickerDefaults.eventDecorator(policy = schoolPolicy)
        }
        val monday = decorationFor(dateOn(2)) {
            NepaliDatePickerDefaults.eventDecorator(policy = schoolPolicy)
        }

        assertNotNull(sunday)
        assertNull(monday)
    }

    @Test
    fun namedHoliday_isColouredByItsKindAndAnnounced() {
        val workday = dateOn(2)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        SimpleDate(workday.year, workday.month, workday.dayOfMonth),
                        "Dashain",
                        NepaliEventKind.Religious
                    )
                )
            )
        )
        var expected = Color.Unspecified

        val decoration = decorationFor(workday) {
            expected = MaterialTheme.colorScheme.primary
            NepaliDatePickerDefaults.eventDecorator(policy = policy)
        }

        assertNotNull(decoration)
        assertEquals(expected, decoration.contentColor, "a religious holiday takes the primary slot")
        assertTrue(decoration.indicators.isEmpty(), "no dot unless the style asks")
        assertEquals("Dashain", decoration.contentDescription)
    }

    @Test
    fun namedHolidayOnAWeeklyOffDay_takesTheHolidaysColour() {
        val saturday = dateOn(Saturday)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        SimpleDate(saturday.year, saturday.month, saturday.dayOfMonth),
                        "Dashain",
                        NepaliEventKind.Religious
                    )
                )
            )
        )
        var holidayColour = Color.Unspecified
        var weeklyColour = Color.Unspecified

        val decoration = decorationFor(saturday) {
            holidayColour = MaterialTheme.colorScheme.primary
            weeklyColour = MaterialTheme.colorScheme.error
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                colors = NepaliDatePickerDefaults.markerColors(
                    weeklyOffColor = MaterialTheme.colorScheme.error,
                    religiousColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        assertNotNull(decoration)
        assertEquals(holidayColour, decoration.contentColor, "the named holiday is the better name")
        assertTrue(holidayColour != weeklyColour, "the two slots must differ for this to mean anything")
        assertEquals("Dashain", decoration.contentDescription)
    }

    @Test
    fun observanceOnAWeeklyOffDay_staysTheWeeklyColour() {
        val saturday = dateOn(Saturday)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        SimpleDate(saturday.year, saturday.month, saturday.dayOfMonth),
                        "World Health Day",
                        NepaliEventKind.Observance
                    )
                )
            )
        )
        var weeklyColour = Color.Unspecified

        val decoration = decorationFor(saturday) {
            weeklyColour = MaterialTheme.colorScheme.error
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                colors = NepaliDatePickerDefaults.markerColors(
                    weeklyOffColor = MaterialTheme.colorScheme.error,
                    observanceColor = MaterialTheme.colorScheme.secondary
                )
            )
        }

        assertNotNull(decoration)
        assertEquals(
            weeklyColour,
            decoration.contentColor,
            "an observance keeps the office open, so it cannot make a closed day look open"
        )
        assertEquals("World Health Day", decoration.contentDescription, "the name is still announced")
    }

    @Test
    fun observanceOnAWorkingDay_takesItsOwnColour() {
        val workday = dateOn(2)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        SimpleDate(workday.year, workday.month, workday.dayOfMonth),
                        "World Health Day",
                        NepaliEventKind.Observance
                    )
                )
            )
        )
        var expected = Color.Unspecified

        val decoration = decorationFor(workday) {
            expected = MaterialTheme.colorScheme.secondary
            NepaliDatePickerDefaults.eventDecorator(policy = policy)
        }

        assertNotNull(decoration)
        assertEquals(expected, decoration.contentColor, "nothing else is competing for the day")
    }

    @Test
    fun anEventThatDoesNotCloseCannotOutrankTheWeek() {
        // The event decides, not its kind: a festival observed elsewhere leaves this office open,
        // so the Saturday it lands on still reads as a Saturday.
        val saturday = dateOn(Saturday)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        date = SimpleDate(saturday.year, saturday.month, saturday.dayOfMonth),
                        name = "Observed elsewhere",
                        kind = NepaliEventKind.Religious,
                        closesOffices = false
                    )
                )
            )
        )
        var weeklyColour = Color.Unspecified

        val decoration = decorationFor(saturday) {
            weeklyColour = MaterialTheme.colorScheme.error
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                colors = NepaliDatePickerDefaults.markerColors(
                    weeklyOffColor = MaterialTheme.colorScheme.error,
                    religiousColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        assertNotNull(decoration)
        assertEquals(weeklyColour, decoration.contentColor)
        assertEquals("Observed elsewhere", decoration.contentDescription, "still named")
    }

    @Test
    fun anObservanceThatDoesCloseOutranksTheWeek() {
        val saturday = dateOn(Saturday)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        date = SimpleDate(saturday.year, saturday.month, saturday.dayOfMonth),
                        name = "Annual programme, offices shut",
                        kind = NepaliEventKind.Observance,
                        closesOffices = true
                    )
                )
            )
        )
        var observanceColour = Color.Unspecified

        val decoration = decorationFor(saturday) {
            observanceColour = MaterialTheme.colorScheme.secondary
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                colors = NepaliDatePickerDefaults.markerColors(
                    weeklyOffColor = MaterialTheme.colorScheme.error,
                    observanceColor = MaterialTheme.colorScheme.secondary
                )
            )
        }

        assertNotNull(decoration)
        assertEquals(observanceColour, decoration.contentColor)
    }

    @Test
    fun severalEventsOnAWeeklyOffDay_takeTheStrongestClosuresColour() {
        val saturday = dateOn(Saturday)
        val date = SimpleDate(saturday.year, saturday.month, saturday.dayOfMonth)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(date, "Open thing", NepaliEventKind.Observance),
                    NepaliCalendarEvent(date, "Dashain", NepaliEventKind.Religious)
                )
            )
        )
        var religiousColour = Color.Unspecified

        val decoration = decorationFor(saturday) {
            religiousColour = MaterialTheme.colorScheme.primary
            NepaliDatePickerDefaults.eventDecorator(policy = policy)
        }

        assertNotNull(decoration)
        assertEquals(religiousColour, decoration.contentColor)
        assertEquals("Dashain, Open thing", decoration.contentDescription)
    }

    @Test
    fun indicateKinds_optsOneKindIntoADot() {
        val workday = dateOn(2)
        val date = SimpleDate(workday.year, workday.month, workday.dayOfMonth)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(date, "Constitution Day", NepaliEventKind.GovernmentPublic),
                    NepaliCalendarEvent(date, "Local jatra", NepaliEventKind.Regional)
                )
            )
        )

        val decoration = decorationFor(workday) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                style = NepaliDatePickerDefaults.eventDisplayStyle(
                    indicateKinds = setOf(NepaliEventKind.GovernmentPublic)
                )
            )
        }

        assertNotNull(decoration)
        assertEquals(1, decoration.indicators.size, "only the opted-in kind is dotted")
        assertEquals(
            "Constitution Day, Local jatra",
            decoration.contentDescription,
            "every name is still announced, strongest first"
        )
    }

    @Test
    fun everyChannelOff_leavesTheCellAlone() {
        val decoration = decorationFor(dateOn(Saturday)) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = NepaliCalendarPolicy.Default,
                style = NepaliDatePickerDefaults.eventDisplayStyle(
                    colorWeeklyOff = false,
                    colorEvents = false,
                    describe = false
                )
            )
        }

        assertNull(decoration, "nothing to draw means nothing handed to the grid")
    }

    @Test
    fun then_keepsTheLeadingColourAndMergesTheDots() {
        val saturday = dateOn(Saturday)
        val eventColour = Color(0xFF00FF00)
        var weeklyColour = Color.Unspecified

        val decoration = decorationFor(saturday) {
            weeklyColour = MaterialTheme.colorScheme.error
            NepaliDatePickerDefaults.eventDecorator(policy = NepaliCalendarPolicy.Default)
                .then(
                    NepaliDayDecorator {
                        NepaliDayDecoration(
                            contentColor = eventColour,
                            indicators = listOf(eventColour),
                            contentDescription = "Wedding"
                        )
                    }
                )
        }

        assertNotNull(decoration)
        assertEquals(weeklyColour, decoration.contentColor, "the holiday rule leads")
        assertEquals(listOf(eventColour), decoration.indicators, "the event still gets its dot")
        assertEquals("Wedding", decoration.contentDescription)
    }

    @Test
    fun indicateWeeklyOff_dotsTheWeekWhenAskedTo() {
        val decoration = decorationFor(dateOn(Saturday)) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = NepaliCalendarPolicy.Default,
                style = NepaliDatePickerDefaults.eventDisplayStyle(indicateWeeklyOff = true)
            )
        }

        assertNotNull(decoration)
        assertEquals(1, decoration.indicators.size)
    }

    @Test
    fun dotsFollowThePriorityOrderTheNamesDo() {
        val workday = dateOn(2)
        val date = SimpleDate(workday.year, workday.month, workday.dayOfMonth)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(date, "Observed", NepaliEventKind.Observance),
                    NepaliCalendarEvent(date, "Closure", NepaliEventKind.GovernmentPublic)
                )
            )
        )
        var publicColour = Color.Unspecified
        var observanceColour = Color.Unspecified

        val decoration = decorationFor(workday) {
            publicColour = MaterialTheme.colorScheme.error
            observanceColour = MaterialTheme.colorScheme.secondary
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                style = NepaliDatePickerDefaults.eventDisplayStyle(
                    indicateKinds = setOf(NepaliEventKind.GovernmentPublic, NepaliEventKind.Observance)
                )
            )
        }

        assertNotNull(decoration)
        assertEquals(listOf(publicColour, observanceColour), decoration.indicators)
    }

    @Test
    fun describeOff_drawsTheDayButSaysNothing() {
        val workday = dateOn(2)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        SimpleDate(workday.year, workday.month, workday.dayOfMonth),
                        "Dashain",
                        NepaliEventKind.Religious
                    )
                )
            )
        )

        val decoration = decorationFor(workday) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                style = NepaliDatePickerDefaults.eventDisplayStyle(describe = false)
            )
        }

        assertNotNull(decoration)
        assertNull(decoration.contentDescription)
        assertTrue(decoration.contentColor != Color.Unspecified, "still coloured")
    }

    @Test
    fun colourSwitchesOffIndependently() {
        val saturday = dateOn(Saturday)
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(
                listOf(
                    NepaliCalendarEvent(
                        SimpleDate(saturday.year, saturday.month, saturday.dayOfMonth),
                        "Dashain",
                        NepaliEventKind.Religious
                    )
                )
            )
        )

        val holidaysOff = decorationFor(saturday) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                style = NepaliDatePickerDefaults.eventDisplayStyle(colorEvents = false)
            )
        }
        val weeklyOff = decorationFor(saturday) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = policy,
                style = NepaliDatePickerDefaults.eventDisplayStyle(colorWeeklyOff = false)
            )
        }

        assertNotNull(holidaysOff)
        assertNotNull(weeklyOff)
        // With the holiday colour off the day is still closed, so the weekly slot paints it; with
        // the weekly colour off the named holiday still does.
        assertTrue(holidaysOff.contentColor != Color.Unspecified)
        assertTrue(weeklyOff.contentColor != Color.Unspecified)
    }

    @Test
    fun tintContainer_addsADiscToAClosedDay() {
        val decoration = decorationFor(dateOn(Saturday)) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = NepaliCalendarPolicy.Default,
                style = NepaliDatePickerDefaults.eventDisplayStyle(tintContainer = true)
            )
        }

        assertNotNull(decoration)
        assertTrue(decoration.containerColor != Color.Unspecified)
    }

    @Test
    fun then_chainsThreeDeepAndKeepsEveryDot() {
        val first = Color(0xFF111111)
        val second = Color(0xFF222222)
        val third = Color(0xFF333333)

        val decoration = decorationFor(dateOn(2)) {
            NepaliDayDecorator { NepaliDayDecoration(contentColor = first, indicators = listOf(first)) }
                .then(NepaliDayDecorator { NepaliDayDecoration(indicators = listOf(second)) })
                .then(NepaliDayDecorator { NepaliDayDecoration(contentColor = third, indicators = listOf(third)) })
        }

        assertNotNull(decoration)
        assertEquals(first, decoration.contentColor, "the first to specify a colour keeps it")
        assertEquals(listOf(first, second, third), decoration.indicators)
    }

    @Test
    fun then_ofTwoBlankDecoratorsIsStillNothing() {
        val decoration = decorationFor(dateOn(2)) {
            NepaliDayDecorator { null }.then(NepaliDayDecorator { null })
        }

        assertNull(decoration)
    }

    @Test
    fun then_takesTheContainerFromWhicheverSpecifiesIt() {
        val container = Color(0xFF444444)
        val content = Color(0xFF555555)

        val decoration = decorationFor(dateOn(2)) {
            NepaliDayDecorator { NepaliDayDecoration(containerColor = container) }
                .then(NepaliDayDecorator { NepaliDayDecoration(contentColor = content) })
        }

        assertNotNull(decoration)
        assertEquals(container, decoration.containerColor)
        assertEquals(content, decoration.contentColor, "the leader left this one unset")
    }

    @Test
    fun then_joinsBothDescriptionsInOrder() {
        val decoration = decorationFor(dateOn(2)) {
            NepaliDayDecorator { NepaliDayDecoration(contentDescription = "Holiday") }
                .then(NepaliDayDecorator { NepaliDayDecoration(contentDescription = "Wedding") })
        }

        assertNotNull(decoration)
        assertEquals("Holiday, Wedding", decoration.contentDescription)
    }

    @Test
    fun then_fallsBackToWhicheverSideDecorates() {
        val workday = dateOn(2)
        val eventOnly = NepaliDayDecorator { NepaliDayDecoration(contentDescription = "Standup") }

        val decoration = decorationFor(workday) {
            NepaliDatePickerDefaults.eventDecorator(policy = NepaliCalendarPolicy.Default)
                .then(eventOnly)
        }

        assertNotNull(decoration, "a plain day with an event is still decorated")
        assertEquals("Standup", decoration.contentDescription)
    }
}
