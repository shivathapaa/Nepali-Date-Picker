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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CalendarTag = "calendar"
private val CalendarTestWidth = 360.dp

/** How far two heights may differ and still count as the same, in device-independent pixels. */
private val HeightTolerance = 1.dp

/**
 * The calendar's geometry: the same height whatever the month's shape, and a touch target that
 * survives the cells sharing the width between them.
 */
class NepaliCalendarGridSizingTest {

    private fun SemanticsNodeInteractionsProvider.calendarHeight(): Dp =
        onNodeWithTag(CalendarTag).getUnclippedBoundsInRoot().let { it.bottom - it.top }

    @Test
    fun monthsOfDifferentShapes_drawAtTheSameHeight() = runComposeUiTest {
        lateinit var state: NepaliCalendarState
        setContent {
            state = rememberNepaliCalendarState(
                // Baisakh 2082 opens mid-week and needs five rows; Asoj needs six.
                initialDisplayedMonth = SimpleDate(2082, 1, 1),
                locale = CalendarTestLocale
            )
            Box(modifier = Modifier.testTag(CalendarTag)) {
                NepaliCalendar(
                    state = state,
                    modifier = Modifier.width(CalendarTestWidth),
                    secondaryDateLocale = null
                )
            }
        }

        val firstMonthHeight = calendarHeight()
        repeat(5) {
            onNodeWithContentDescription(
                CalendarTestLocale.language.nextMonthContentDescription
            ).performClick()
            waitForIdle()
        }
        val laterMonthHeight = calendarHeight()

        assertTrue(
            abs((firstMonthHeight - laterMonthHeight).value) <= HeightTolerance.value,
            "A month grid changed height while paging: $firstMonthHeight then $laterMonthHeight"
        )
    }

    @Test
    fun aDayCell_keepsItsTouchTargetHeight() = runComposeUiTest {
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialDisplayedMonth = CalendarTestMonth,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(CalendarTestWidth),
                secondaryDateLocale = null
            )
        }

        val cell = onNodeWithContentDescription("Asoj 5, 2082", substring = true)
            .getUnclippedBoundsInRoot()
        val cellHeight = cell.bottom - cell.top
        val cellWidth = cell.right - cell.left

        // The calendar draws the pickers' own day cell, so a day is the same size on both.
        assertEquals(DateStateLayerHeight, cellHeight)
        assertEquals(DateStateLayerWidth, cellWidth)
    }

    @Test
    fun theCalendar_doesNotScrollVertically() = runComposeUiTest {
        setContent {
            Box(modifier = Modifier.testTag(CalendarTag)) {
                NepaliCalendar(
                    state = rememberNepaliCalendarState(
                        initialDisplayedMonth = CalendarTestMonth,
                        locale = CalendarTestLocale
                    ),
                    modifier = Modifier.width(CalendarTestWidth),
                    secondaryDateLocale = null
                )
            }
        }

        val height = calendarHeight()
        // Navigation row, weekday row and six week rows, and nothing that could scroll away.
        assertTrue(
            height.value < RecommendedSizeForAccessibility.value * 12,
            "The calendar grew to $height, which is taller than its fixed layout allows"
        )
    }
}
