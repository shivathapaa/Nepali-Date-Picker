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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val EnglishLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)

// A decorator runs outside composition, so its colours are resolved by the caller rather than
// read from MaterialTheme inside the lambda.
private val HolidayRed = Color(0xFFB3261E)

/** What a decorated grid says out loud, and how little changes for an undecorated one. */
class NepaliDayDecoratorUiTest {

    @Test
    fun decoratedDay_announcesWhatTheEventIsCalled() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                ),
                dayDecorator = NepaliDayDecorator { day ->
                    if (day.date.dayOfMonth != 15) {
                        null
                    } else {
                        NepaliDayDecoration(
                            contentColor = HolidayRed,
                            indicators = listOf(HolidayRed),
                            contentDescription = "Constitution Day"
                        )
                    }
                }
            )
        }

        onNodeWithContentDescription("Constitution Day", substring = true).assertIsDisplayed()
    }

    @Test
    fun onlyTheDecoratedDaysCarryTheMarking() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                ),
                dayDecorator = NepaliDayDecorator { day ->
                    if (day.date.dayOfMonth != 15) {
                        null
                    } else {
                        NepaliDayDecoration(contentDescription = "Constitution Day")
                    }
                }
            )
        }

        val marked = onAllNodesWithContentDescription("Constitution Day", substring = true)
            .fetchSemanticsNodes()
        assertEquals(1, marked.size, "exactly one day of the month is decorated")
    }

    @Test
    fun anUndecoratedGridKeepsItsPlainDateDescriptions() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                )
            )
        }

        val describedDays =
            onAllNodesWithContentDescription("2081", substring = true).fetchSemanticsNodes()
        assertTrue(describedDays.isNotEmpty(), "every day still announces its own date")
    }
}
