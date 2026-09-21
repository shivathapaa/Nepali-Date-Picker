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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayMarkerColors
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val Saturday = 7
private const val FestivalDays = 10

/**
 * A Swift caller expands a span with the same two calls a Kotlin one does and hands the result over
 * as [NepaliEventInfo]. These pin that trip: every day of the span has to survive the translation
 * and reach the policy the pickers read, including the days on the far side of a year boundary.
 */
class NepaliEventOptionsSpanTest {

    /** A palette with every slot set, so a dot's colour can be told apart from an unset one. */
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

    private val festival = NepaliCalendarEvent(
        date = SimpleDate(2082, 6, 17),
        name = "Dashain",
        kind = NepaliEventKind.Religious,
        id = "dashain-2082",
        payload = """{"district":"all"}"""
    )

    private fun optionsOver(
        span: List<NepaliCalendarEvent>,
        weeklyOffDays: List<Int> = listOf(Saturday),
        indicate: Boolean = false
    ): NepaliEventOptions {
        val options = NepaliEventOptions()
        options.weeklyOffDays = weeklyOffDays
        options.events = span.map { it.asInfo(indicate = indicate) }
        return options
    }

    private fun NepaliCalendarEvent.asInfo(argb: Int = 0, indicate: Boolean = false) = NepaliEventInfo(
        year = date.year,
        month = date.month,
        dayOfMonth = date.dayOfMonth,
        name = name,
        kind = kind,
        closesOffices = closesOffices,
        colorArgb = argb,
        indicate = indicate
    ).also {
        it.id = this.id
        it.payload = this.payload
    }

    @Test
    fun everyDayOfASpanReachesThePolicy() {
        val span = festival.spanningDays(FestivalDays)
        val policy = optionsOver(span).toPolicy()

        for (day in 17 until 17 + FestivalDays) {
            assertEquals(
                listOf("Dashain"),
                policy.eventsOn(SimpleDate(2082, 6, day)).map { it.name },
                "day $day"
            )
            assertTrue(policy.isNonWorkingDay(SimpleDate(2082, 6, day)), "day $day")
        }
        assertTrue(policy.eventsOn(SimpleDate(2082, 6, 17 + FestivalDays)).isEmpty())
        assertEquals(FestivalDays, policy.eventsIn(2082, 6).size)
    }

    @Test
    fun theClosureFlagCrossesTheBridgePerEntry() {
        val leave = NepaliCalendarEvent(
            date = SimpleDate(2082, 6, 17),
            name = "Annual leave",
            kind = NepaliEventKind.Religious,
            closesOffices = false
        )

        val policy = optionsOver(leave.spanningThrough(SimpleDate(2082, 6, 21))).toPolicy()

        assertEquals(5, policy.eventsIn(2082, 6).size)
        assertFalse(
            policy.isNonWorkingDay(SimpleDate(2082, 6, 19)),
            "an entry that keeps the doors open must not close the day after the trip over"
        )
    }

    @Test
    fun aSpanAcrossTheYearEndIsReportedByBothYears() {
        val endOfChaitra = NepaliDateConverter.getNepaliMonthCalendar(2082, 12).totalDaysInMonth
        val span = festival.copy(date = SimpleDate(2082, 12, endOfChaitra))
            .spanningThrough(SimpleDate(2083, 1, 3))

        val policy = optionsOver(span).toPolicy()

        assertEquals(1, policy.eventsIn(2082, 12).size)
        assertEquals(3, policy.eventsIn(2083, 1).size)
        assertTrue(policy.isNonWorkingDay(SimpleDate(2083, 1, 1)))
    }

    @Test
    fun aSpanTakesItsDotsPerDayWhenTheEntriesAskForThem() {
        val span = festival.spanningDays(3)
        val options = optionsOver(span, indicate = true)
        val colors = options.toMarkerColors(themeColors)

        val dots = options.dotMarkers(colors)

        assertEquals(3, dots.size, "one keyed entry per day of the span")
        assertContentEquals(
            listOf(SimpleDate(2082, 6, 17), SimpleDate(2082, 6, 18), SimpleDate(2082, 6, 19)),
            dots.keys.sortedBy { it.dayOfMonth }
        )
        assertTrue(dots.values.all { it == listOf(themeColors.religiousColor) })
    }

    @Test
    fun anUnindicatedSpanSpendsNoDots() {
        val options = optionsOver(festival.spanningDays(3))

        assertTrue(options.dotMarkers(themeColors).isEmpty())
    }

    @Test
    fun aSpanBlocksItsDaysOnlyThroughASelectableRule() {
        val span = festival.spanningDays(3)
        val policy = optionsOver(span).toPolicy()
        val rule = policy.asSelectableDates()

        for (day in 17..19) {
            val calendar = NepaliDateConverter.getNepaliCalendar(2082, 6, day)
            assertFalse(rule.isSelectableDate(calendar), "day $day")
        }
        assertTrue(
            rule.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2082, 6, 20)),
            "the day after the span stays selectable"
        )
    }
}
