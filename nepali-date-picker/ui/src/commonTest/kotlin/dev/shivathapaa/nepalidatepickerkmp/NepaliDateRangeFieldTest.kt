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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val RangeLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
private val English = NepaliDatePickerLang.ENGLISH

/** The outlined pair of typed range fields, which has no dialog of its own. */
class NepaliDateRangeTextFieldTest {

    @Test
    fun bothLabelsAreShown() = runComposeUiTest {
        setContent {
            NepaliDateRangeTextField(
                startValue = null,
                endValue = null,
                onRangeChange = { _, _ -> },
                locale = RangeLocale
            )
        }
        onNodeWithText(English.startDate).assertExists()
        onNodeWithText(English.endDate).assertExists()
    }

    @Test
    fun typingBothEndsReportsTheRange() = runComposeUiTest {
        var start: SimpleDate? = null
        var end: SimpleDate? = null
        setContent {
            var startValue by remember { mutableStateOf<SimpleDate?>(null) }
            var endValue by remember { mutableStateOf<SimpleDate?>(null) }
            NepaliDateRangeTextField(
                startValue = startValue,
                endValue = endValue,
                onRangeChange = { newStart, newEnd ->
                    startValue = newStart
                    endValue = newEnd
                    start = newStart
                    end = newEnd
                },
                locale = RangeLocale
            )
        }

        onNodeWithText(English.startDate).performTextInput("20830601")
        onNodeWithText(English.endDate).performTextInput("20830610")

        runOnIdle {
            assertEquals(SimpleDate(2083, 6, 1), start)
            assertEquals(SimpleDate(2083, 6, 10), end)
        }
    }

    @Test
    fun anEndBeforeTheStartIsRefused() = runComposeUiTest {
        var end: SimpleDate? = SimpleDate(2083, 6, 10)
        setContent {
            var endValue by remember { mutableStateOf<SimpleDate?>(null) }
            NepaliDateRangeTextField(
                startValue = SimpleDate(2083, 6, 10),
                endValue = endValue,
                onRangeChange = { _, newEnd ->
                    endValue = newEnd
                    end = newEnd
                },
                locale = RangeLocale
            )
        }

        onNodeWithText(English.endDate).performTextInput("20830601")

        runOnIdle { assertNull(end) }
    }

    @Test
    fun anExistingRangeIsWrittenOutInTheTypedCalendar() = runComposeUiTest {
        setContent {
            NepaliDateRangeTextField(
                startValue = SimpleDate(2083, 6, 1),
                endValue = SimpleDate(2083, 6, 10),
                onRangeChange = { _, _ -> },
                locale = RangeLocale,
                calendarSystem = CalendarSystem.GREGORIAN
            )
        }
        // BS 2083-06-01 is AD 2026-09-17 and BS 2083-06-10 is AD 2026-09-26.
        onNodeWithText("2026/09/17").assertExists()
        onNodeWithText("2026/09/26").assertExists()
    }
}

/** The filled pair, which opens a range calendar from its trailing icon. */
class NepaliDateRangeFieldTest {

    @Test
    fun theTrailingIconOpensARangeCalendar() = runComposeUiTest {
        setContent {
            NepaliDateRangeField(
                startValue = SimpleDate(2083, 6, 1),
                endValue = null,
                onRangeChange = { _, _ -> },
                locale = RangeLocale
            )
        }

        assertTrue(onAllNodesWithText("15").fetchSemanticsNodes().isEmpty())
        onNodeWithContentDescription(English.selectDateText).performClick()
        onNodeWithText("15").assertExists()
    }

    @Test
    fun theCalendarItOpensCanFillTheGridWithTheNeighbouringMonths() = runComposeUiTest {
        setContent {
            NepaliDateRangeField(
                startValue = SimpleDate(2083, 4, 1),
                endValue = null,
                onRangeChange = { _, _ -> },
                locale = RangeLocale,
                showAdjacentMonthDays = true
            )
        }

        onNodeWithContentDescription(English.selectDateText).performClick()

        // Shrawan 2083 borrows Asar 32, a day number no Gregorian month and no short month reaches.
        onNodeWithText("32").assertExists()
    }

    @Test
    fun theCalendarItOpensCanShowTheGregorianGrid() = runComposeUiTest {
        setContent {
            NepaliDateRangeField(
                startValue = SimpleDate(2083, 6, 1),
                endValue = null,
                onRangeChange = { _, _ -> },
                locale = RangeLocale,
                calendarSystem = CalendarSystem.GREGORIAN,
                showCalendarSystemToggle = true
            )
        }

        onNodeWithContentDescription(English.selectDateText).performClick()

        onNodeWithContentDescription(English.switchToGregorianContentDescription).assertExists()
        onNodeWithText(English.englishDateRangePickerTitle).assertExists()
    }

    @Test
    fun retypingTheStartClearsAnEndThatNoLongerFollowsIt() = runComposeUiTest {
        var end: SimpleDate? = SimpleDate(2083, 6, 10)
        setContent {
            var startValue by remember { mutableStateOf<SimpleDate?>(SimpleDate(2083, 6, 1)) }
            var endValue by remember { mutableStateOf<SimpleDate?>(SimpleDate(2083, 6, 10)) }
            NepaliDateRangeField(
                startValue = startValue,
                endValue = endValue,
                onRangeChange = { newStart, newEnd ->
                    startValue = newStart
                    endValue = newEnd
                    end = newEnd
                },
                locale = RangeLocale
            )
        }

        onNodeWithText("2083/06/01").performTextClearance()
        onNodeWithText(English.startDate).performTextInput("20830620")

        runOnIdle { assertNull(end) }
    }
}
