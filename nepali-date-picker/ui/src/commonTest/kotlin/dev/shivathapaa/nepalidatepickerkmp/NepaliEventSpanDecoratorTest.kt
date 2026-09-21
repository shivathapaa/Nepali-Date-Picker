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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayInfo
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val Saturday = 7
private const val SpanLength = 5

/**
 * An event that runs longer than a day, seen from the drawing side: the decorator has to mark every
 * day the span covers and nothing past it, and the precedence between a span and the weekly rule
 * has to be the same on day four as it is on day one.
 */
class NepaliEventSpanDecoratorTest {

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

    private fun calendarAt(date: SimpleDate): CustomCalendar =
        NepaliDateConverter.getNepaliCalendar(date.year, date.month, date.dayOfMonth)

    private fun infoFor(date: CustomCalendar) = NepaliDayInfo(
        date = date,
        displayedDate = date,
        isToday = false,
        isSelected = false,
        isInRange = false,
        isEnabled = true,
        isAdjacentMonth = false
    )

    /** Runs [build] inside a composition and hands back the decoration for each of [dates]. */
    private fun decorationsFor(
        dates: List<CustomCalendar>,
        build: @Composable () -> NepaliDayDecorator
    ): List<NepaliDayDecoration?> {
        val decorations = mutableListOf<NepaliDayDecoration?>()
        runComposeUiTest {
            setContent {
                val decorator = build()
                decorations.clear()
                dates.forEach { decorations.add(decorator.decorate(infoFor(it))) }
            }
        }
        assertEquals(dates.size, decorations.size, "the decorator never ran")
        return decorations
    }

    private fun policyOver(
        span: List<NepaliCalendarEvent>,
        weeklyOffDays: Set<Int> = emptySet()
    ) = NepaliCalendarPolicy(weeklyOffDays = weeklyOffDays, provider = ListProvider(span))

    @Test
    fun everyDayOfASpanIsMarkedTheSameWay() {
        val start = SimpleDate(2082, 6, 10)
        val span = NepaliCalendarEvent(start, "Dashain", NepaliEventKind.Religious)
            .spanningDays(SpanLength)
        val days = span.map { calendarAt(it.date) }

        val decorations = decorationsFor(days) {
            NepaliDatePickerDefaults.eventDecorator(policy = policyOver(span))
        }

        val colors = decorations.map { assertNotNull(it, "a day of the span went unmarked").contentColor }
        assertEquals(1, colors.toSet().size, "every day of one span carries one colour")
        assertTrue(colors.first() != Color.Unspecified)
    }

    @Test
    fun theDayAfterASpanIsLeftAlone() {
        val start = SimpleDate(2082, 6, 10)
        val span = NepaliCalendarEvent(start, "Dashain", NepaliEventKind.Religious)
            .spanningDays(SpanLength)
        val dayAfter = calendarAt(SimpleDate(2082, 6, 10 + SpanLength))
        val dayBefore = calendarAt(SimpleDate(2082, 6, 9))

        val decorations = decorationsFor(listOf(dayBefore, dayAfter)) {
            NepaliDatePickerDefaults.eventDecorator(policy = policyOver(span))
        }

        assertNull(decorations[0], "the day before the span is not part of it")
        assertNull(decorations[1], "the day after the span is not part of it")
    }

    @Test
    fun aClosingSpanOutranksTheWeekOnEveryDayItCovers() {
        val saturday = dateOn(Saturday)
        val start = NepaliDateConverter
            .getNepaliCalendarAfterAdditionOrSubtraction(saturday.year, saturday.month, saturday.dayOfMonth, -2)
        val span = NepaliCalendarEvent(
            SimpleDate(start.year, start.month, start.dayOfMonth),
            "Dashain",
            NepaliEventKind.Religious
        ).spanningDays(SpanLength)
        val policy = policyOver(span, weeklyOffDays = setOf(Saturday))

        val decorations = decorationsFor(listOf(saturday)) {
            NepaliDatePickerDefaults.eventDecorator(policy = policy)
        }
        var expected = Color.Unspecified
        runComposeUiTest {
            setContent { expected = NepaliDatePickerDefaults.markerColors().religiousColor }
        }

        assertEquals(expected, assertNotNull(decorations.single()).contentColor)
    }

    @Test
    fun anObservanceSpanLeavesTheWeeklyColourInPlace() {
        val saturday = dateOn(Saturday)
        val start = NepaliDateConverter
            .getNepaliCalendarAfterAdditionOrSubtraction(saturday.year, saturday.month, saturday.dayOfMonth, -2)
        val span = NepaliCalendarEvent(
            SimpleDate(start.year, start.month, start.dayOfMonth),
            "Sports week",
            NepaliEventKind.Observance
        ).spanningDays(SpanLength)
        val policy = policyOver(span, weeklyOffDays = setOf(Saturday))

        val decorations = decorationsFor(listOf(saturday)) {
            NepaliDatePickerDefaults.eventDecorator(policy = policy)
        }
        var weeklyOff = Color.Unspecified
        runComposeUiTest {
            setContent { weeklyOff = NepaliDatePickerDefaults.markerColors().weeklyOffColor }
        }

        assertEquals(
            weeklyOff,
            assertNotNull(decorations.single()).contentColor,
            "a span that keeps the doors open cannot make a closed day look open"
        )
    }

    @Test
    fun everyDayOfASpanIsAnnouncedByName() {
        val start = SimpleDate(2082, 6, 10)
        val span = NepaliCalendarEvent(start, "Dashain", NepaliEventKind.Religious)
            .spanningDays(SpanLength)
        val days = span.map { calendarAt(it.date) }

        val decorations = decorationsFor(days) {
            NepaliDatePickerDefaults.eventDecorator(policy = policyOver(span))
        }

        assertTrue(
            decorations.all { it?.contentDescription == "Dashain" },
            "a span is named on each of its days, not only the first"
        )
    }

    @Test
    fun aSpanTakesADotOnEveryDayWhenItsKindIsOptedIn() {
        val start = SimpleDate(2082, 6, 10)
        val span = NepaliCalendarEvent(start, "Sarkari bida", NepaliEventKind.GovernmentPublic)
            .spanningDays(SpanLength)
        val days = span.map { calendarAt(it.date) }

        val decorations = decorationsFor(days) {
            NepaliDatePickerDefaults.eventDecorator(
                policy = policyOver(span),
                style = NepaliDatePickerDefaults.eventDisplayStyle(
                    indicateKinds = setOf(NepaliEventKind.GovernmentPublic)
                )
            )
        }

        assertTrue(decorations.all { it?.indicators?.size == 1 }, "each day of the span gets its dot")
    }

    @Test
    fun aSpanCrossingTheYearEndKeepsMarkingOnTheFarSide() {
        val endOfChaitra = NepaliDateConverter.getNepaliMonthCalendar(2082, 12).totalDaysInMonth
        val span = NepaliCalendarEvent(
            SimpleDate(2082, 12, endOfChaitra),
            "Year end break",
            NepaliEventKind.GovernmentPublic
        ).spanningDays(3)
        val days = span.map { calendarAt(it.date) }

        val decorations = decorationsFor(days) {
            NepaliDatePickerDefaults.eventDecorator(policy = policyOver(span))
        }

        assertEquals(setOf(2082, 2083), days.map { it.year }.toSet(), "the span has to straddle the year")
        assertTrue(decorations.all { it != null }, "the days in the next year are marked too")
    }
}
