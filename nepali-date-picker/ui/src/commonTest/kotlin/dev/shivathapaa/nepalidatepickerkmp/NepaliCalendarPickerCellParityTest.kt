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

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** The width a picker draws itself at, which is what the calendar is compared against. */
private val PickerWidth = 360.dp

private val TestDay = SimpleDate(2082, 6, 5)

/**
 * The calendar draws its days through the pickers' own cell, so a day is the same size and sits in
 * the same place on both surfaces. These compare the two directly, which is what stops the calendar
 * from growing a look of its own.
 */
class NepaliCalendarPickerCellParityTest {

    private fun SemanticsNodeInteractionsProvider.daySize(description: String): DpSize {
        val bounds = onNodeWithContentDescription(description, substring = true)
            .getUnclippedBoundsInRoot()
        return DpSize(bounds.right - bounds.left, bounds.bottom - bounds.top)
    }

    private fun pickerDaySize(dual: Boolean): DpSize {
        var size = DpSize.Zero
        runComposeUiTest {
            setContent {
                NepaliDatePicker(
                    state = rememberNepaliDatePickerState(
                        initialDisplayedMonth = CalendarTestMonth,
                        locale = CalendarTestLocale
                    ),
                    modifier = Modifier.width(PickerWidth),
                    secondaryDateLocale = if (dual) CalendarTestLocale else null,
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
            }
            size = daySize("Asoj 5, 2082")
        }
        return size
    }

    private fun calendarDaySize(dual: Boolean): DpSize {
        var size = DpSize.Zero
        runComposeUiTest {
            setContent {
                NepaliCalendar(
                    state = rememberNepaliCalendarState(
                        initialDisplayedMonth = CalendarTestMonth,
                        locale = CalendarTestLocale
                    ),
                    modifier = Modifier.width(PickerWidth),
                    secondaryDateLocale = if (dual) CalendarTestLocale else null
                )
            }
            size = daySize("Asoj 5, 2082")
        }
        return size
    }

    @Test
    fun aSingleDateDay_isTheSameSizeOnBothSurfaces() {
        assertEquals(pickerDaySize(dual = false), calendarDaySize(dual = false))
    }

    @Test
    fun aDualDateDay_isTheSameSizeOnBothSurfaces() {
        assertEquals(pickerDaySize(dual = true), calendarDaySize(dual = true))
    }

    @Test
    fun aDay_announcesTheSameThingOnBothSurfaces() = runComposeUiTest {
        // The calendar adds the policy's marks to a description, and everything before them is the
        // date itself, spoken exactly as a picker speaks it.
        setContent {
            NepaliCalendar(
                state = rememberNepaliCalendarState(
                    initialSelectedDate = TestDay,
                    locale = CalendarTestLocale
                ),
                modifier = Modifier.width(PickerWidth),
                secondaryDateLocale = null
            )
        }

        onNodeWithContentDescription("Sunday, Asoj 5, 2082").assertExists()
    }
}
