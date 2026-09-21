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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NoOpEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.filtered
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.plus
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Composing providers, and the boundaries the composed result has to hold at: the ends of the
 * supported range, a week with no days off and a week with every day off, and lists that overlap or
 * report nothing.
 */
class NepaliEventCompositionTests {

    private class ListProvider(private val entries: List<NepaliCalendarEvent>) : NepaliEventProvider {
        var callCount = 0
            private set

        override fun events(year: Int): Set<NepaliCalendarEvent> {
            callCount++
            return entries.filterTo(mutableSetOf()) { it.date.year == year }
        }
    }

    /** Answers the closure question directly, which the contract allows, and lists nothing. */
    private object AlwaysClosedProvider : NepaliEventProvider {
        override fun events(year: Int): Set<NepaliCalendarEvent> = emptySet()
        override fun closesOn(date: SimpleDate): Boolean = true
    }

    private fun entry(date: SimpleDate, name: String, kind: NepaliEventKind = NepaliEventKind.Religious) =
        NepaliCalendarEvent(date, name, kind)

    private val HolidayKindPublic = NepaliEventKind.GovernmentPublic
    private val HolidayKindRegional = NepaliEventKind.Regional

    private val firstYear = NepaliCalendarDefaults.NepaliYearRange.first
    private val lastYear = NepaliCalendarDefaults.NepaliYearRange.last

    // Provider composition

    @Test
    fun plus_withAnEmptyProvider_changesNothing() {
        val date = SimpleDate(2082, 1, 1)
        val only = ListProvider(listOf(entry(date, "New Year")))

        assertEquals(only.events(2082), (only + NoOpEventProvider).events(2082))
        assertEquals(only.events(2082), (NoOpEventProvider + only).events(2082))
    }

    @Test
    fun plus_dedupesAnEntryBothListsReport() {
        val date = SimpleDate(2082, 1, 1)
        val shared = entry(date, "New Year", NepaliEventKind.GovernmentPublic)
        val national = ListProvider(listOf(shared))
        val school = ListProvider(listOf(shared, entry(date, "Founders Day", NepaliEventKind.Regional)))

        val merged = (national + school).events(2082)

        assertEquals(2, merged.size, "the entry both lists carry is one holiday, not two")
    }

    @Test
    fun plus_chainsAcrossThreeLists() {
        val date = SimpleDate(2082, 1, 1)
        val merged = ListProvider(listOf(entry(date, "A"))) +
                ListProvider(listOf(entry(date, "B"))) +
                ListProvider(listOf(entry(date, "C")))

        assertEquals(setOf("A", "B", "C"), merged.events(2082).map { it.name }.toSet())
    }

    @Test
    fun plus_hearsASideThatAnswersClosesOnWithoutListingTheEvent() {
        val date = SimpleDate(2082, 1, 1)
        val listed = ListProvider(listOf(entry(date, "New Year")))

        assertTrue((listed + AlwaysClosedProvider).closesOn(SimpleDate(2082, 5, 9)))
        assertTrue((AlwaysClosedProvider + listed).closesOn(SimpleDate(2082, 5, 9)))
    }

    @Test
    fun plus_ofTwoListsClosesExactlyWhatEitherListCloses() {
        val closed = SimpleDate(2082, 1, 1)
        val open = SimpleDate(2082, 1, 2)
        val merged = ListProvider(listOf(entry(closed, "Bida", NepaliEventKind.GovernmentPublic))) +
                ListProvider(listOf(entry(open, "Programme", NepaliEventKind.Observance)))

        assertTrue(merged.closesOn(closed))
        assertFalse(merged.closesOn(open), "an observance leaves the day worked on either side")
    }

    @Test
    fun filtered_withNoMatches_isEmptyAndLeavesTheSourceAlone() {
        val date = SimpleDate(2082, 1, 1)
        val source = ListProvider(
            listOf(entry(date, "Observed", NepaliEventKind.Observance))
        )
        val narrowed = source.filtered { it.kind == NepaliEventKind.GovernmentPublic }

        assertTrue(narrowed.events(2082).isEmpty())
        assertEquals(1, source.events(2082).size, "filtering is a view, not a mutation")
    }

    @Test
    fun filtered_chainsAndNarrowsFurtherEachTime() {
        val date = SimpleDate(2082, 1, 1)
        val source = ListProvider(
            listOf(
                entry(date, "Closure", NepaliEventKind.GovernmentPublic),
                entry(date, "Festival", NepaliEventKind.Religious),
                entry(date, "Observed", NepaliEventKind.Observance)
            )
        )

        val closures = source.filtered { it.kind.closesOfficesByDefault }
        val publicOnly = closures.filtered { it.kind == NepaliEventKind.GovernmentPublic }

        assertEquals(2, closures.events(2082).size)
        assertEquals(1, publicOnly.events(2082).size)
    }

    @Test
    fun aMergedProviderStillHonoursTheYearItIsAskedFor() {
        val merged = ListProvider(listOf(entry(SimpleDate(2082, 1, 1), "A"))) +
                ListProvider(listOf(entry(SimpleDate(2083, 1, 1), "B")))

        assertEquals(listOf("A"), merged.events(2082).map { it.name })
        assertEquals(listOf("B"), merged.events(2083).map { it.name })
        assertTrue(merged.events(2084).isEmpty())
    }

    // Range ends

    @Test
    fun theFirstAndLastSupportedYears_resolveLikeAnyOther() {
        val policy = NepaliCalendarPolicy.Default

        val firstDay = policy.statusOf(SimpleDate(firstYear, 1, 1))
        val lastMonth = NepaliDateConverter.getTotalDaysInNepaliMonth(lastYear, 12)
        val lastDay = policy.statusOf(SimpleDate(lastYear, 12, lastMonth))

        assertFalse(firstDay.events.isNotEmpty())
        assertFalse(lastDay.events.isNotEmpty())
        assertEquals(
            NepaliDateConverter.getNepaliCalendar(firstYear, 1, 1).dayOfWeek == 7,
            firstDay.isWeeklyOff
        )
    }

    @Test
    fun aYearOutsideTheTable_failsTheWayTheConverterDoes() {
        val policy = NepaliCalendarPolicy.Default

        assertFailsWith<IllegalArgumentException> {
            policy.statusOf(SimpleDate(lastYear + 1, 1, 1))
        }
    }

    @Test
    fun holidaysIn_outsideTheTable_isEmptyRatherThanThrowing() {
        val policy = NepaliCalendarPolicy(
            provider = ListProvider(listOf(entry(SimpleDate(2082, 1, 1), "New Year")))
        )

        assertTrue(policy.eventsIn(lastYear + 1, 1).isEmpty())
        assertTrue(policy.eventsOn(SimpleDate(lastYear + 1, 1, 1)).isEmpty())
    }

    @Test
    fun holidaysIn_coversEveryMonthOfAYear() {
        val entries = (1..12).map { month -> entry(SimpleDate(2082, month, 1), "Month $month") }
        val policy = NepaliCalendarPolicy(provider = ListProvider(entries))

        for (month in 1..12) {
            assertEquals(
                listOf("Month $month"),
                policy.eventsIn(2082, month).map { it.name },
                "month $month should see only its own entry"
            )
        }
    }

    // Weekly sets at their extremes

    @Test
    fun anEmptyWeek_neverClosesAnything() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = emptySet())

        for (dayOfWeek in 1..7) {
            assertFalse(policy.isWeeklyOff(dayOfWeek), "day $dayOfWeek should be worked")
        }
        assertEquals(
            14,
            NepaliDateConverter.workingDaysBetween(SimpleDate(2082, 1, 1), SimpleDate(2082, 1, 15), policy)
        )
    }

    @Test
    fun aWeekWithEveryDayOff_leavesNoWorkingDayToFind() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = (1..7).toSet())

        assertEquals(
            0,
            NepaliDateConverter.workingDaysBetween(SimpleDate(2082, 1, 1), SimpleDate(2082, 1, 15), policy)
        )
        assertFailsWith<IllegalStateException> {
            NepaliDateConverter.nextWorkingDay(SimpleDate(2082, 1, 1), policy)
        }
        assertFailsWith<IllegalStateException> {
            NepaliDateConverter.addWorkingDays(SimpleDate(2082, 1, 1), 1, policy)
        }
    }

    @Test
    fun weeklyMembership_holdsAcrossTheWholeSupportedRange() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = setOf(1, 7))

        for (year in firstYear..lastYear step 10) {
            for (offset in 0..6) {
                val calendar = NepaliDateConverter
                    .getNepaliCalendarAfterAdditionOrSubtraction(year, 1, 1, offset)
                assertEquals(
                    calendar.dayOfWeek in setOf(1, 7),
                    policy.isWeeklyOff(calendar.dayOfWeek),
                    "year $year, offset $offset, dayOfWeek ${calendar.dayOfWeek}"
                )
            }
        }
    }

    // Arithmetic parity with the provider overloads

    @Test
    fun policyArithmetic_agreesWithTheProviderOverloads() {
        val holiday = SimpleDate(2082, 1, 5)
        val provider = ListProvider(listOf(entry(holiday, "Something")))
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 2, 1)

        for (weekend in listOf(emptySet(), setOf(7), setOf(1, 7), setOf(6, 7))) {
            val policy = NepaliCalendarPolicy(weeklyOffDays = weekend, provider = provider)

            assertEquals(
                NepaliDateConverter.workingDaysBetween(start, end, provider, weekend),
                NepaliDateConverter.workingDaysBetween(start, end, policy),
                "span with weekend $weekend"
            )
            assertEquals(
                NepaliDateConverter.nextWorkingDay(start, provider, weekend),
                NepaliDateConverter.nextWorkingDay(start, policy),
                "next working day with weekend $weekend"
            )
            assertEquals(
                NepaliDateConverter.addWorkingDays(start, 7, provider, weekend),
                NepaliDateConverter.addWorkingDays(start, 7, policy),
                "forward walk with weekend $weekend"
            )
            assertEquals(
                NepaliDateConverter.addWorkingDays(start, -7, provider, weekend),
                NepaliDateConverter.addWorkingDays(start, -7, policy),
                "backward walk with weekend $weekend"
            )
        }
    }

    @Test
    fun anEmptySpan_countsNothingAndABackwardsSpanFails() {
        val date = SimpleDate(2082, 1, 1)

        assertEquals(0, NepaliDateConverter.workingDaysBetween(date, date, NepaliCalendarPolicy.Default))
        assertFailsWith<IllegalArgumentException> {
            NepaliDateConverter.workingDaysBetween(
                SimpleDate(2082, 1, 2), date, NepaliCalendarPolicy.Default
            )
        }
    }

    @Test
    fun walkingBackwardsCrossesTheYearBoundary() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = emptySet())
        val start = SimpleDate(2082, 1, 3)

        val walked = NepaliDateConverter.addWorkingDays(start, -5, policy)

        assertEquals(2081, walked.year)
        assertEquals(12, walked.month)
    }

    // monthStatus

    @Test
    fun monthStatus_coversEveryDayOfEveryMonthOfAYear() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = setOf(7))

        for (month in 1..12) {
            val days = policy.monthStatus(2082, month)
            assertEquals(
                NepaliDateConverter.getTotalDaysInNepaliMonth(2082, month),
                days.size,
                "month $month"
            )
            for ((index, status) in days.withIndex()) {
                val calendar = NepaliDateConverter.getNepaliCalendar(2082, month, index + 1)
                assertEquals(
                    calendar.dayOfWeek == 7,
                    status.isWeeklyOff,
                    "2082-$month-${index + 1} has dayOfWeek ${calendar.dayOfWeek}"
                )
            }
        }
    }

    @Test
    fun monthStatus_atTheEndsOfTheSupportedRange() {
        val policy = NepaliCalendarPolicy.Default

        assertEquals(
            NepaliDateConverter.getTotalDaysInNepaliMonth(firstYear, 1),
            policy.monthStatus(firstYear, 1).size
        )
        assertEquals(
            NepaliDateConverter.getTotalDaysInNepaliMonth(lastYear, 12),
            policy.monthStatus(lastYear, 12).size
        )
    }

    @Test
    fun monthStatus_ofAMonthWithNothingNamed_isAllPlainDays() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = ListProvider(listOf(entry(SimpleDate(2082, 1, 1), "Only in month one")))
        )

        val second = policy.monthStatus(2082, 2)

        assertTrue(second.all { it.events.isEmpty() && !it.isNonWorking })
    }

    @Test
    fun monthStatus_outsideTheTable_failsLikeTheConverter() {
        assertFailsWith<IllegalArgumentException> {
            NepaliCalendarPolicy.Default.monthStatus(lastYear + 1, 1)
        }
    }

    @Test
    fun monthStatus_putsSeveralEventsOnTheRightDay() {
        val policy = NepaliCalendarPolicy(
            weeklyOffDays = emptySet(),
            provider = ListProvider(
                listOf(
                    entry(SimpleDate(2082, 3, 10), "Second", HolidayKindRegional),
                    entry(SimpleDate(2082, 3, 10), "First", HolidayKindPublic),
                    entry(SimpleDate(2082, 3, 11), "Next day", HolidayKindPublic)
                )
            )
        )

        val month = policy.monthStatus(2082, 3)

        assertEquals(listOf("First", "Second"), month[9].names, "strongest kind first")
        assertEquals(listOf("Next day"), month[10].names)
        assertTrue(month[8].events.isEmpty())
    }

    // Composition carries the whole event

    @Test
    fun composedProviders_keepEveryFieldOfAnEvent() {
        val detailed = NepaliCalendarEvent(
            date = SimpleDate(2082, 1, 1),
            name = "Tihar",
            kind = NepaliEventKind.Religious,
            closesOffices = false,
            id = "evt-1",
            payload = """{"note":"शुभ"}"""
        )
        val merged = ListProvider(listOf(detailed)) + ListProvider(emptyList())

        val roundTripped = merged.events(2082).single()

        assertEquals(detailed, roundTripped)
        assertEquals("evt-1", roundTripped.id)
        assertEquals("""{"note":"शुभ"}""", roundTripped.payload)
    }

    @Test
    fun filtered_canNarrowToWhatActuallyCloses() {
        val date = SimpleDate(2082, 1, 1)
        val source = ListProvider(
            listOf(
                NepaliCalendarEvent(date, "Closed", NepaliEventKind.Regional, closesOffices = true),
                NepaliCalendarEvent(date, "Open", NepaliEventKind.Regional, closesOffices = false)
            )
        )

        val closures = source.filtered { it.closesOffices }

        assertEquals(listOf("Closed"), closures.events(2082).map { it.name })
        assertEquals(2, source.events(2082).size, "the source is untouched")
    }

    @Test
    fun aDayOfNonClosingEventsIsCountedAsWorked() {
        val start = SimpleDate(2082, 1, 1)
        val end = SimpleDate(2082, 1, 8)
        val programmes = ListProvider(
            (1..7).map {
                NepaliCalendarEvent(
                    SimpleDate(2082, 1, it), "Programme", NepaliEventKind.Observance
                )
            }
        )

        assertEquals(
            NepaliDateConverter.workingDaysBetween(start, end, NepaliCalendarPolicy(emptySet())),
            NepaliDateConverter.workingDaysBetween(
                start, end, NepaliCalendarPolicy(emptySet(), programmes)
            ),
            "a week of programmes closes nothing"
        )
    }

    @Test
    fun addWorkingDays_ofZero_staysPutEvenOnAClosedDay() {
        val policy = NepaliCalendarPolicy(weeklyOffDays = (1..7).toSet())
        val date = SimpleDate(2082, 1, 1)

        assertEquals(date, NepaliDateConverter.addWorkingDays(date, 0, policy))
    }

    @Test
    fun filtered_keepsAClosureTheProviderAnswersWithoutListingIt() {
        val date = SimpleDate(2082, 1, 1)

        assertTrue(
            AlwaysClosedProvider.filtered { true }.closesOn(date),
            "a provider that answers closesOn directly has nothing for the filter to drop"
        )
        assertTrue(
            NepaliCalendarPolicy(emptySet(), AlwaysClosedProvider.filtered { true })
                .isNonWorkingDay(date),
            "and the policy hears it through the filter too"
        )
    }

    @Test
    fun filtered_ofEverything_answersWhatTheSourceDoes() {
        val date = SimpleDate(2082, 1, 1)
        val source = ListProvider(
            listOf(
                NepaliCalendarEvent(date, "Closed", HolidayKindPublic),
                NepaliCalendarEvent(date, "Programme", NepaliEventKind.Observance)
            )
        )

        val identical = source.filtered { true }

        assertEquals(source.events(2082), identical.events(2082))
        assertEquals(source.closesOn(date), identical.closesOn(date))
        assertEquals(
            source.closesOn(SimpleDate(2082, 1, 2)),
            identical.closesOn(SimpleDate(2082, 1, 2))
        )
    }

    @Test
    fun filtered_dropsAClosureWhoseEventTheFilterRemoved() {
        val date = SimpleDate(2082, 1, 1)
        val source = ListProvider(listOf(NepaliCalendarEvent(date, "Regional", HolidayKindRegional)))

        val publicOnly = source.filtered { it.kind == HolidayKindPublic }

        assertTrue(source.closesOn(date), "the regional holiday closes the day")
        assertFalse(publicOnly.closesOn(date), "and narrowing the list narrows the closure with it")
        assertFalse(
            NepaliCalendarPolicy(emptySet(), publicOnly).isNonWorkingDay(date),
            "so the arithmetic counts the day as worked"
        )
    }

    @Test
    fun filtered_keepsAClosureWhoseEventSurvived() {
        val date = SimpleDate(2082, 1, 1)
        val source = ListProvider(
            listOf(
                NepaliCalendarEvent(date, "Public", HolidayKindPublic),
                NepaliCalendarEvent(date, "Regional", HolidayKindRegional)
            )
        )

        val publicOnly = source.filtered { it.kind == HolidayKindPublic }

        assertTrue(publicOnly.closesOn(date), "one surviving closure is enough to shut the day")
    }
}
