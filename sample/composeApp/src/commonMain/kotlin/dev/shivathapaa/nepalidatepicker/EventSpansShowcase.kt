/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState

private const val FestivalDays = 10
private const val LeaveDays = 5
private const val YearEndSpanDays = 4
private const val CountingWindow = 21
private const val FestivalStartOffset = 3
private const val LeaveStartOffset = 2

/**
 * Something that runs longer than a day. An event covers one day, so a span is a list of entries
 * rather than a range: expand it once and every day of it colours, counts and blocks like any other
 * event, including the days on the far side of a month or a year boundary.
 */
@Composable
fun EventSpansShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val festivalStart = remember(today) { offsetDate(today, FestivalStartOffset) }
        val leaveStart = remember(today) { offsetDate(today, LeaveStartOffset) }
        val leaveEnd = remember(leaveStart) { offsetDate(leaveStart, LeaveDays - 1) }

        val festival = remember(festivalStart) {
            NepaliCalendarEvent(
                date = festivalStart,
                name = "Dashain (demo)",
                kind = NepaliEventKind.Religious,
                id = "dashain-demo"
            )
        }
        val leave = remember(leaveStart) {
            NepaliCalendarEvent(
                date = leaveStart,
                name = "Annual leave (demo)",
                kind = NepaliEventKind.Observance,
                id = "leave-42"
            )
        }

        val festivalDays = remember(festival) { festival.spanningDays(FestivalDays) }
        val leaveDays = remember(leave, leaveEnd) { leave.spanningThrough(leaveEnd) }

        val festivalPolicy = remember(festivalDays) {
            NepaliCalendarPolicy(provider = festivalDays.asEventProvider())
        }
        val leavePolicy = remember(leaveDays) {
            NepaliCalendarPolicy(provider = leaveDays.asEventProvider())
        }
        val bothPolicy = remember(festivalDays, leaveDays) {
            NepaliCalendarPolicy(provider = (festivalDays + leaveDays).asEventProvider())
        }

        DemoSection(
            "One event, ten days",
            "spanningDays(10) turns a single event into the ten entries the calendar reads, each " +
                    "carrying the same name, kind, closesOffices and id. Nothing else in the " +
                    "library changes: the days colour exactly as ten separate holidays would."
        ) {
            val state = rememberNepaliDatePickerState(initialSelectedDate = festivalStart)
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy = festivalPolicy)
            )
            LabeledValue("First day", festivalDays.first().date.formatted())
            LabeledValue("Last day", festivalDays.last().date.formatted())
            LabeledValue("Entries", festivalDays.size.toString())
        }

        DemoSection(
            "Stated by its end instead",
            "spanningThrough takes the last day and includes it, which is the shape a leave request " +
                    "and a published holiday list both come in. This one is an observance, so it " +
                    "names its days without closing them."
        ) {
            val state = rememberNepaliDatePickerState(initialSelectedDate = leaveStart)
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy = leavePolicy)
            )
            LabeledValue("From", leaveDays.first().date.formatted())
            LabeledValue("Through", leaveDays.last().date.formatted())
            LabeledValue(
                "Closes the office",
                leaveDays.any { it.closesOffices }.toString()
            )
        }

        DemoSection(
            "A span that crosses the year end",
            "Chaitra runs into Baisakh and the entries land in both years, which is what lets each " +
                    "one be reported by the year a provider is asked for. A range carried as one " +
                    "object would have to be returned by two years at once."
        ) {
            val spanYear = today.year
            val chaitraEnd = remember(spanYear) {
                val days = NepaliDateConverter.getNepaliMonthCalendar(spanYear, LastMonth).totalDaysInMonth
                SimpleDate(spanYear, LastMonth, days - 1)
            }
            val yearEndSpan = remember(chaitraEnd) {
                NepaliCalendarEvent(
                    date = chaitraEnd,
                    name = "Year end break (demo)",
                    kind = NepaliEventKind.GovernmentPublic,
                    id = "year-end"
                ).spanningDays(YearEndSpanDays)
            }
            val policy = remember(yearEndSpan) {
                NepaliCalendarPolicy(provider = yearEndSpan.asEventProvider())
            }
            val state = rememberNepaliDatePickerState(initialSelectedDate = chaitraEnd)
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy = policy)
            )
            LabeledValue(
                "In $spanYear Chaitra",
                policy.eventsIn(spanYear, LastMonth).size.toString()
            )
            LabeledValue(
                "In ${spanYear + 1} Baisakh",
                policy.eventsIn(spanYear + 1, FirstMonth).size.toString()
            )
        }

        DemoSection(
            "Folding the days back into one row",
            "Every entry of a span carries the id the event was given, so an agenda that wants one " +
                    "line per festival groups by it. The grid still marks each day; only the list " +
                    "collapses."
        ) {
            val state = rememberNepaliDatePickerState(initialSelectedDate = festivalStart)
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy = bothPolicy)
            )
            val month = state.displayedMonth
            val inMonth = bothPolicy.eventsIn(month.year, month.month)
            LabeledValue("Day entries this month", inMonth.size.toString())
            inMonth.distinctBy { it.id ?: it.name }.forEach { entry ->
                LabeledValue(entry.name, entry.date.formatted())
            }
            if (inMonth.isEmpty()) {
                Text("Page to the month the span falls in")
            }
        }

        DemoSection(
            "What a span does to the counting",
            "A span of closures takes its days out of the working count; a span of observances " +
                    "leaves them in. The same $CountingWindow days, counted under both."
        ) {
            val windowEnd = remember(today) { offsetDate(today, CountingWindow) }
            LabeledValue(
                "Working days, festival span",
                NepaliDateConverter.workingDaysBetween(today, windowEnd, festivalPolicy).toString()
            )
            LabeledValue(
                "Working days, leave span",
                NepaliDateConverter.workingDaysBetween(today, windowEnd, leavePolicy).toString()
            )
            LabeledValue(
                "Next working day, festival span",
                NepaliDateConverter.nextWorkingDay(festivalStart, festivalPolicy).formatted()
            )
            LabeledValue(
                "+1 working day from the first festival day",
                NepaliDateConverter.addWorkingDays(festivalStart, 1, festivalPolicy).formatted()
            )
        }

        DemoSection(
            "Blocking every day of a span",
            "Marking never blocks on its own. asSelectableDates turns the same policy into a picker " +
                    "rule, and the festival's ten days stop being selectable while the leave's five " +
                    "stay open, because an observance leaves the day worked."
        ) {
            val selectable = remember(bothPolicy) { bothPolicy.asSelectableDates() }
            val state = rememberNepaliDatePickerState(
                initialSelectedDate = today,
                nepaliSelectableDates = selectable
            )
            NepaliDatePicker(state = state)
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "The month a span falls in, day by day",
            "monthStatus answers a whole month in one call, resolving the first weekday once and " +
                    "walking forward. The days below are the ones this month has something on."
        ) {
            val state = rememberNepaliDatePickerState(initialSelectedDate = festivalStart)
            NepaliDatePicker(
                state = state,
                dayDecorator = NepaliDatePickerDefaults.eventDecorator(policy = bothPolicy)
            )
            val month = state.displayedMonth
            val status = bothPolicy.monthStatus(month.year, month.month)
            LabeledValue("Days in the month", status.size.toString())
            LabeledValue("Closed days", status.count { it.isNonWorking }.toString())
            LabeledValue("Weekly off days", status.count { it.isWeeklyOff }.toString())
            LabeledValue(
                "Days with something named",
                status.count { it.events.isNotEmpty() }.toString()
            )
        }
    }
}

/** Bikram Sambat month numbers the year ends and starts on. */
private const val FirstMonth = 1
private const val LastMonth = 12
