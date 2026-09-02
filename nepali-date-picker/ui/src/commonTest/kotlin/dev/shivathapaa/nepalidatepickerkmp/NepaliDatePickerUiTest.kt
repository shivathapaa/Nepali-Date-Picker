/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTestApi::class, ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val EnglishLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)

class NepaliDatePickerUiTest {

    @Test
    fun datePicker_rendersDaysOfDisplayedMonth() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                )
            )
        }
        onNodeWithText("15").assertIsDisplayed()
    }

    /** Verifies the per-day accessibility contentDescription added to every cell. */
    @Test
    fun datePicker_dayCell_exposesLocalizedDateContentDescription() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                )
            )
        }
        // Each day cell announces its full localized date, so several nodes carry the year in
        // their contentDescription (one per day of the displayed month).
        val describedDays =
            onAllNodesWithContentDescription("2081", substring = true).fetchSemanticsNodes()
        assertTrue(
            describedDays.isNotEmpty(),
            "expected day cells to expose the localized date (incl. year) in contentDescription"
        )
    }

    /** Verifies day-cell click drives selection through the hoisted state. */
    @Test
    fun datePicker_clickingDay_updatesSelectedDate() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2081, 5),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state)
        }
        onNodeWithText("15").performClick()
        runOnIdle {
            assertEquals(2081, state.selectedDate?.year)
            assertEquals(5, state.selectedDate?.month)
            assertEquals(15, state.selectedDate?.dayOfMonth)
        }
    }
}

class NepaliDatePickerFullScreenDialogTest {

    @Test
    fun fullScreenDialog_showsTitleButtonsAndContent() = runComposeUiTest {
        setContent {
            NepaliDatePickerFullScreenDialog(
                onDismissRequest = {},
                confirmButton = { Text("OK") },
                dismissButton = { Text("Cancel") },
                title = { Text("Pick a date") }
            ) {
                Text("PICKER_CONTENT")
            }
        }
        onNodeWithText("Pick a date").assertExists()
        onNodeWithText("OK").assertExists()
        onNodeWithText("Cancel").assertExists()
        onNodeWithText("PICKER_CONTENT").assertExists()
    }
}

class NepaliDatePickerRobustnessTest {

    /** An out-of-range or invalid initial date must coerce, not crash. */
    @Test
    fun datePicker_outOfRangeInitial_coercesAndDoesNotCrash() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(1500, 1, 1),
                initialDisplayedMonth = SimpleDate(3000, 13, 40),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state)
        }
        runOnIdle {
            assertNull(state.selectedDate)
            assertTrue(state.displayedMonth.year in NepaliCalendarDefaults.NepaliYearRange)
        }
    }

    /** Opening the picker from a field seeded with an out-of-range value must not crash. */
    @Test
    fun dateField_outOfRangeValue_opensPickerWithoutCrash() = runComposeUiTest {
        setContent {
            NepaliDateField(
                value = SimpleDate(1500, 1, 1),
                onValueChange = {},
                locale = EnglishLocale
            )
        }
        onNodeWithContentDescription(NepaliDatePickerLang.ENGLISH.selectDateText).performClick()
        onNodeWithText("15").assertExists()
    }
}
