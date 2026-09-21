/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider

/**
 * The holiday source behind the Holidays tab. The library ships no holiday data, so an app brings
 * its own [NepaliEventProvider]; this one answers from Bikram Sambat dates that land on the same
 * day every year, plus three demo days a few days after [today] so the pickers always have
 * something blocked in the month they open on.
 *
 * A real provider also carries the festivals that follow the lunar calendar (Dashain, Tihar, Holi,
 * Id), which fall on a different Bikram Sambat day each year and so cannot come from a fixed table
 * like this one.
 *
 * Years outside [NepaliCalendarDefaults.NepaliYearRange] come back empty rather than throwing, and
 * a year is built once and kept, which is what keeps the inherited [NepaliEventProvider.closesOn]
 * cheap enough for the working-day helpers to call it once per day of a span.
 */
class SampleEventProvider(today: SimpleDate) : NepaliEventProvider {

    private val demoDays: List<NepaliCalendarEvent> = listOf(
        NepaliCalendarEvent(offsetDate(today, 2), "Company day (demo)", NepaliEventKind.Observance),
        NepaliCalendarEvent(offsetDate(today, 3), "Local jatra (demo)", NepaliEventKind.Regional),
        NepaliCalendarEvent(offsetDate(today, 9), "Offices closed (demo)", NepaliEventKind.GovernmentPublic)
    )

    private val byYear = mutableMapOf<Int, Set<NepaliCalendarEvent>>()

    override fun events(year: Int): Set<NepaliCalendarEvent> {
        if (year !in NepaliCalendarDefaults.NepaliYearRange) return emptySet()
        return byYear.getOrPut(year) {
            everyYearHolidays.mapTo(mutableSetOf()) {
                NepaliCalendarEvent(SimpleDate(year, it.month, it.dayOfMonth), it.name, it.kind)
            } + demoDays.filter { it.date.year == year }
        }
    }
}

/**
 * A provider answering from a list already in hand, which is what a span expands to and what an app
 * holds after a fetch. Entries are bucketed by year once, so every year is a map lookup.
 */
fun List<NepaliCalendarEvent>.asEventProvider(): NepaliEventProvider {
    val byYear = groupBy { it.date.year }.mapValues { (_, entries) -> entries.toSet() }
    return object : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
    }
}

/** The entries from [from] through [days] days later, in date order, across the years they span. */
fun NepaliEventProvider.entriesWithin(from: SimpleDate, days: Int): List<NepaliCalendarEvent> {
    val until = offsetDate(from, days)
    return (from.year..until.year)
        .flatMap { events(it) }
        .filter { it.date >= from && it.date <= until }
        .sortedBy { it.date }
}

/**
 * One school's own closures, the list an institution keeps on top of the national one. Merged with
 * [SampleEventProvider] through the library's `plus`, so neither side has to know about the other.
 */
class SampleSchoolEvents(today: SimpleDate) : NepaliEventProvider {

    private val ownDays: List<NepaliCalendarEvent> = listOf(
        NepaliCalendarEvent(offsetDate(today, 5), "Founders Day (school)", NepaliEventKind.Regional),
        NepaliCalendarEvent(offsetDate(today, 12), "Exam break (school)", NepaliEventKind.Regional)
    )

    override fun events(year: Int): Set<NepaliCalendarEvent> =
        ownDays.filterTo(mutableSetOf()) { it.date.year == year }
}

/** A holiday pinned to one Bikram Sambat month and day, repeated in every year of the range. */
private class RecurringHoliday(
    val month: Int,
    val dayOfMonth: Int,
    val name: String,
    val kind: NepaliEventKind
)

private val everyYearHolidays = listOf(
    RecurringHoliday(1, 1, "Nepali New Year", NepaliEventKind.GovernmentPublic),
    RecurringHoliday(2, 15, "Republic Day", NepaliEventKind.GovernmentPublic),
    RecurringHoliday(6, 3, "Constitution Day", NepaliEventKind.GovernmentPublic),
    RecurringHoliday(10, 1, "Maghe Sankranti", NepaliEventKind.Religious),
    RecurringHoliday(11, 7, "Democracy Day", NepaliEventKind.GovernmentPublic)
)
