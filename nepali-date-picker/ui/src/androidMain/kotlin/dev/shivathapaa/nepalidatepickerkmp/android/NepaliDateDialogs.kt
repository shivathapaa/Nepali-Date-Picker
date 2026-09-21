/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp.android

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerFullScreenDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedPickerDefaults

/**
 * A live dialog shown by [showNepaliDatePickerDialog] or
 * [showNepaliDatePickerFullScreenDialog]. [dismiss] closes it without invoking either caller
 * callback, for a host tearing down while the dialog is still up.
 */
fun interface NepaliDialogHandle {
    fun dismiss()
}

/**
 * Shows the picker in a modal dialog over [activity]'s content, closing itself on either button.
 * Mirrors the iOS `NepaliDatePickerDialogViewController` parameter for parameter.
 *
 * [onConfirm] receives the selection, `null` when confirming with nothing selected;
 * [onDismiss] fires for the dismiss button, a tap outside and the back gesture.
 */
fun showNepaliDatePickerDialog(
    activity: Activity,
    initialSelectedDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showEnglishDate: Boolean = false,
    englishDateLocale: NepaliDateLocale? = null,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    confirmText: String = EmbeddedPickerDefaults.DIALOG_CONFIRM_TEXT,
    dismissText: String = EmbeddedPickerDefaults.DIALOG_DISMISS_TEXT,
    tonalElevation: Float = EmbeddedPickerDefaults.DIALOG_TONAL_ELEVATION,
    cornerRadius: Float = EmbeddedPickerDefaults.DIALOG_CORNER_RADIUS,
    onConfirm: (CustomCalendar?) -> Unit,
    onDismiss: () -> Unit
): NepaliDialogHandle = nepaliDialogOverlay(activity) { dismissOverlay ->
    val dayMarks = events.toDecorator()
    val state = rememberDialogPickerState(
        initialSelectedDate, yearRange, selectableDates, locale, initialCalendarSystem
    )

    NepaliDatePickerDialog(
        onDismissRequest = {
            dismissOverlay()
            onDismiss()
        },
        confirmButton = {
            TextButton(onClick = {
                dismissOverlay()
                onConfirm(state.selectedDate)
            }) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = {
                dismissOverlay()
                onDismiss()
            }) { Text(dismissText) }
        },
        shape = RoundedCornerShape(cornerRadius.dp),
        tonalElevation = tonalElevation.dp
    ) {
        DialogPickerContent(
            state = state,
            showEnglishDate = showEnglishDate,
            englishDateLocale = englishDateLocale ?: locale,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays,
            dayMarks = dayMarks
        )
    }
}

/**
 * Shows the picker in a full-screen dialog over [activity]'s content, closing itself on either
 * button. Mirrors the iOS `NepaliDatePickerFullScreenDialogViewController`.
 */
fun showNepaliDatePickerFullScreenDialog(
    activity: Activity,
    initialSelectedDate: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showEnglishDate: Boolean = false,
    englishDateLocale: NepaliDateLocale? = null,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    title: String? = null,
    confirmText: String = EmbeddedPickerDefaults.DIALOG_CONFIRM_TEXT,
    dismissText: String = EmbeddedPickerDefaults.DIALOG_DISMISS_TEXT,
    onConfirm: (CustomCalendar?) -> Unit,
    onDismiss: () -> Unit
): NepaliDialogHandle = nepaliDialogOverlay(activity) { dismissOverlay ->
    val dayMarks = events.toDecorator()
    val state = rememberDialogPickerState(
        initialSelectedDate, yearRange, selectableDates, locale, initialCalendarSystem
    )

    NepaliDatePickerFullScreenDialog(
        onDismissRequest = {
            dismissOverlay()
            onDismiss()
        },
        confirmButton = {
            TextButton(onClick = {
                dismissOverlay()
                onConfirm(state.selectedDate)
            }) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = {
                dismissOverlay()
                onDismiss()
            }) { Text(dismissText) }
        },
        title = title?.let { text -> { Text(text) } }
    ) {
        DialogPickerContent(
            state = state,
            showEnglishDate = showEnglishDate,
            englishDateLocale = englishDateLocale ?: locale,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays,
            dayMarks = dayMarks
        )
    }
}

@Composable
private fun rememberDialogPickerState(
    initialSelectedDate: SimpleDate?,
    yearRange: IntRange,
    selectableDates: NepaliSelectableDates?,
    locale: NepaliDateLocale,
    initialCalendarSystem: CalendarSystem
): NepaliDatePickerState = remember {
    NepaliDatePickerState(
        initialSelectedDate = initialSelectedDate,
        initialDisplayedMonth = initialSelectedDate,
        yearRange = yearRange,
        initialDisplayMode = DisplayMode.Picker,
        nepaliSelectableDates = selectableDates.orAllDates(),
        locale = locale,
        initialCalendarSystem = initialCalendarSystem
    )
}

@Composable
private fun DialogPickerContent(
    state: NepaliDatePickerState,
    showEnglishDate: Boolean,
    englishDateLocale: NepaliDateLocale,
    showModeToggle: Boolean,
    showTodayButton: Boolean,
    showCalendarSystemToggle: Boolean,
    showAdjacentMonthDays: Boolean,
    dayMarks: NepaliDayDecorator?
) {
    if (showEnglishDate) {
        NepaliDatePickerWithEnglishDate(
            dayDecorator = dayMarks,
            state = state,
            englishDateLocale = englishDateLocale,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    } else {
        NepaliDatePicker(
            dayDecorator = dayMarks,
            state = state,
            showModeToggle = showModeToggle,
            showTodayButton = showTodayButton,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    }
}
