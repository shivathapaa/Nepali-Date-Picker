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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertTrue

class NepaliDatePickerDockedTest {

    @Test
    fun docked_isClosedInitially_andOpensOnTrailingIconClick() = runComposeUiTest {
        setContent {
            NepaliDatePickerDocked(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
                )
            )
        }

        // Dropdown collapsed → no calendar day cells rendered yet.
        assertTrue(onAllNodesWithText("15").fetchSemanticsNodes().isEmpty())

        // Tap the trailing calendar button to open the dropdown.
        onNodeWithContentDescription(
            NepaliDatePickerLang.ENGLISH.switchToCalendarModeContentDescription
        ).performClick()

        onNodeWithText("15").assertExists()
    }
}
