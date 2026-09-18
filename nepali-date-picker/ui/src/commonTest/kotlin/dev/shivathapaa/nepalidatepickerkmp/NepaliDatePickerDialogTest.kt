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

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val DialogLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)

/** The floating dialog, which hosts whatever picker the caller puts in its content slot. */
class NepaliDatePickerDialogTest {

    @Test
    fun itShowsItsButtonsAndItsContent() = runComposeUiTest {
        setContent {
            NepaliDatePickerDialog(
                onDismissRequest = {},
                confirmButton = { TextButton(onClick = {}) { Text("OK") } },
                dismissButton = { TextButton(onClick = {}) { Text("Cancel") } }
            ) {
                Text("DIALOG_CONTENT")
            }
        }
        onNodeWithText("OK").assertExists()
        onNodeWithText("Cancel").assertExists()
        onNodeWithText("DIALOG_CONTENT").assertExists()
    }

    @Test
    fun aPickerInsideItStillSelects() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2083, 4),
                locale = DialogLocale
            )
            NepaliDatePickerDialog(
                onDismissRequest = {},
                confirmButton = { TextButton(onClick = {}) { Text("OK") } }
            ) {
                NepaliDatePicker(state = state)
            }
        }

        onNodeWithText("15").performClick()

        runOnIdle {
            assertEquals(SimpleDate(2083, 4, 15), state.selectedDate?.toSimpleDate())
        }
    }

    @Test
    fun itsConfirmingButtonReachesTheCaller() = runComposeUiTest {
        var confirmed = false
        setContent {
            NepaliDatePickerDialog(
                onDismissRequest = {},
                confirmButton = { TextButton(onClick = { confirmed = true }) { Text("OK") } }
            ) {
                Text("DIALOG_CONTENT")
            }
        }

        onNodeWithText("OK").performClick()

        runOnIdle { assertTrue(confirmed, "the confirming button did not reach the caller") }
    }

    @Test
    fun theDismissingButtonIsOptional() = runComposeUiTest {
        setContent {
            NepaliDatePickerDialog(
                onDismissRequest = {},
                confirmButton = { TextButton(onClick = {}) { Text("OK") } }
            ) {
                Text("DIALOG_CONTENT")
            }
        }
        onNodeWithText("OK").assertExists()
        onNodeWithText("Cancel").assertDoesNotExist()
    }
}
