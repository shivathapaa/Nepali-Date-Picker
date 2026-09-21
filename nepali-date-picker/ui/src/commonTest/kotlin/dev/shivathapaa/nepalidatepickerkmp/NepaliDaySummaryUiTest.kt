/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTestApi::class, ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test

/** Saturdays in Asoj 2082, which opens on a Wednesday. */
private val Saturday = SimpleDate(2082, 6, 4)
private val Sunday = SimpleDate(2082, 6, 5)

/** What one day reads as: the verdict first, then the weekly rule and whatever is named on it. */
class NepaliDaySummaryUiTest {

    private val language = CalendarTestLocale.language

    private fun policyWith(vararg events: NepaliCalendarEvent) =
        NepaliCalendarPolicy(provider = eventsProviderOf(*events))

    @Test
    fun aWeeklyOffDay_readsAsClosedAndSaysWhy() = runComposeUiTest {
        setContent { NepaliDaySummary(date = Saturday, locale = CalendarTestLocale) }

        onNodeWithText(language.closedText).assertExists()
        onNodeWithText(language.weeklyOffText).assertExists()
    }

    @Test
    fun anOrdinaryDay_readsAsWorkingAndCarriesNothing() = runComposeUiTest {
        setContent { NepaliDaySummary(date = Sunday, locale = CalendarTestLocale) }

        onNodeWithText(language.workingDayText).assertExists()
        onNodeWithText(language.noEventsOnDayText).assertExists()
    }

    @Test
    fun aClosingEvent_readsAsClosedAndNamesTheEvent() = runComposeUiTest {
        val holiday = NepaliCalendarEvent(
            date = Sunday,
            name = "Constitution Day",
            kind = NepaliEventKind.GovernmentPublic
        )
        setContent {
            NepaliDaySummary(
                date = Sunday,
                policy = policyWith(holiday),
                locale = CalendarTestLocale
            )
        }

        onNodeWithText(language.closedText).assertExists()
        onNodeWithText(holiday.name).assertExists()
        onAllNodesWithText(language.noEventsOnDayText).assertCountEquals(0)
    }

    @Test
    fun anObservance_leavesTheDayWorkingAndStillNamesIt() = runComposeUiTest {
        val programme = NepaliCalendarEvent(
            date = Sunday,
            name = "School programme",
            kind = NepaliEventKind.Observance
        )
        setContent {
            NepaliDaySummary(
                date = Sunday,
                policy = policyWith(programme),
                locale = CalendarTestLocale
            )
        }

        onNodeWithText(language.workingDayText).assertExists()
        onNodeWithText(programme.name).assertExists()
    }

    @Test
    fun aSaturdayCarryingAFestival_saysBoth() = runComposeUiTest {
        val festival = NepaliCalendarEvent(
            date = Saturday,
            name = "Ghatasthapana",
            kind = NepaliEventKind.Religious
        )
        setContent {
            NepaliDaySummary(
                date = Saturday,
                policy = policyWith(festival),
                locale = CalendarTestLocale
            )
        }

        onNodeWithText(language.weeklyOffText).assertExists()
        onNodeWithText(festival.name).assertExists()
    }

    @Test
    fun withNothingPicked_theCalendarOverloadAsksForADate() = runComposeUiTest {
        setContent {
            NepaliDaySummary(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                )
            )
        }

        onNodeWithText(language.selectDateText).assertExists()
    }

    @Test
    fun withADayPicked_theCalendarOverloadWritesItOut() = runComposeUiTest {
        setContent {
            NepaliDaySummary(
                state = rememberNepaliCalendarState(
                    initialSelectedDate = Saturday,
                    locale = CalendarTestLocale
                )
            )
        }

        onNodeWithText(language.closedText).assertExists()
        onNodeWithText(language.weeklyOffText).assertExists()
    }

    @Test
    fun inNepali_theVerdictIsWrittenInNepali() = runComposeUiTest {
        val nepali = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)
        setContent { NepaliDaySummary(date = Saturday, locale = nepali) }

        onNodeWithText(NepaliDatePickerLang.NEPALI.closedText).assertExists()
        onNodeWithText(NepaliDatePickerLang.NEPALI.weeklyOffText).assertExists()
    }
}
