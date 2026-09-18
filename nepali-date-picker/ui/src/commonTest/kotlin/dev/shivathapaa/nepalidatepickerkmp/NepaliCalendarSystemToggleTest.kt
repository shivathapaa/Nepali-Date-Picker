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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The switch on its own, which is public so an app can drive
 * [NepaliDatePickerState.displayedCalendarSystem] from its own chrome instead of from the picker.
 */
class NepaliCalendarSystemToggleTest {

    private val english = NepaliDatePickerLang.ENGLISH

    @Test
    fun bothSegmentsAreLabelledAndTheShownCalendarIsSelected() = runComposeUiTest {
        setContent {
            NepaliCalendarSystemToggle(
                calendarSystem = CalendarSystem.BIKRAM_SAMBAT,
                onCalendarSystemChange = {},
                language = english
            )
        }

        onNodeWithText(english.bikramSambatShort).assertExists()
        onNodeWithText(english.gregorianShort).assertExists()
        onNodeWithContentDescription(english.switchToBikramSambatContentDescription).assertIsSelected()
        onNodeWithContentDescription(english.switchToGregorianContentDescription).assertIsNotSelected()
    }

    @Test
    fun tappingTheOtherSegmentReportsIt() = runComposeUiTest {
        val reported = mutableListOf<CalendarSystem>()
        setContent {
            NepaliCalendarSystemToggle(
                calendarSystem = CalendarSystem.BIKRAM_SAMBAT,
                onCalendarSystemChange = { reported += it },
                language = english
            )
        }

        onNodeWithContentDescription(english.switchToGregorianContentDescription).performClick()
        assertEquals(listOf(CalendarSystem.GREGORIAN), reported)
    }

    @Test
    fun tappingTheSelectedSegmentReportsNothing() = runComposeUiTest {
        val reported = mutableListOf<CalendarSystem>()
        setContent {
            NepaliCalendarSystemToggle(
                calendarSystem = CalendarSystem.BIKRAM_SAMBAT,
                onCalendarSystemChange = { reported += it },
                language = english
            )
        }

        onNodeWithContentDescription(english.switchToBikramSambatContentDescription).performClick()
        assertEquals(emptyList(), reported)
    }

    @Test
    fun theSelectedSegmentFollowsTheCalendarItIsGiven() = runComposeUiTest {
        setContent {
            var calendarSystem by remember { mutableStateOf(CalendarSystem.BIKRAM_SAMBAT) }
            NepaliCalendarSystemToggle(
                calendarSystem = calendarSystem,
                onCalendarSystemChange = { calendarSystem = it },
                language = english
            )
        }

        onNodeWithContentDescription(english.switchToGregorianContentDescription).performClick()

        onNodeWithContentDescription(english.switchToGregorianContentDescription).assertIsSelected()
        onNodeWithContentDescription(english.switchToBikramSambatContentDescription).assertIsNotSelected()
    }

    @Test
    fun theLabelsAndDescriptionsFollowTheLanguage() = runComposeUiTest {
        val nepali = NepaliDatePickerLang.NEPALI
        setContent {
            NepaliCalendarSystemToggle(
                calendarSystem = CalendarSystem.GREGORIAN,
                onCalendarSystemChange = {},
                language = nepali
            )
        }

        onNodeWithText(nepali.bikramSambatShort).assertExists()
        onNodeWithText(nepali.gregorianShort).assertExists()
        onNodeWithContentDescription(nepali.switchToGregorianContentDescription).assertIsSelected()
    }
}
