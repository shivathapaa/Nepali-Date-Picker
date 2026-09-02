/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package sample.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliWheelDatePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState

/** Alternative pickers: the iOS-style wheel and the Material3 docked field, default and customized. */
@Composable
fun WheelDockedShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        DemoSection(
            "Wheel picker",
            "Three snapping columns. The natural choice for birth dates and dates far from today."
        ) {
            var selected by remember { mutableStateOf<CustomCalendar?>(null) }
            NepaliWheelDatePicker(onDateChange = { selected = it })
            SelectedText(selected.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Wheel picker, customized",
            "Taller rows, five visible, rounded surface, and distinct selected and unselected text."
        ) {
            var selected by remember { mutableStateOf<CustomCalendar?>(null) }
            NepaliWheelDatePicker(
                locale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI),
                itemHeight = 48.dp,
                visibleItemCount = 5,
                shape = RoundedCornerShape(24.dp),
                selectedTextStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                unselectedTextStyle = MaterialTheme.typography.bodyMedium,
                onDateChange = { selected = it }
            )
            SelectedText(selected.readout()?.let { "Selected (BS): $it" })
        }

        DemoSection(
            "Wheel picker in a dialog",
            "Host the wheel in a modal with confirm and cancel, committing only on OK."
        ) {
            var open by remember { mutableStateOf(false) }
            var draft by remember { mutableStateOf<CustomCalendar?>(null) }
            var committed by remember { mutableStateOf<CustomCalendar?>(null) }

            Button(onClick = { open = true }) { Text("Open wheel dialog") }
            SelectedText(committed.readout()?.let { "Confirmed: $it" })

            if (open) {
                NepaliDatePickerDialog(
                    onDismissRequest = { open = false },
                    confirmButton = {
                        NepaliDatePickerDefaults.DialogButton("OK", {
                            committed = draft
                            open = false
                        })
                    },
                    dismissButton = {
                        NepaliDatePickerDefaults.DialogButton("Cancel", { open = false })
                    }
                ) {
                    NepaliWheelDatePicker(
                        modifier = Modifier.padding(16.dp),
                        onDateChange = { draft = it }
                    )
                }
            }
        }

        DemoSection(
            "Docked picker",
            "A compact field with a dropdown calendar. The default forms pattern."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerDocked(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pick a date") }
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }

        DemoSection(
            "Docked picker, customized",
            "Custom field shape, inline prefix, full-date format, and a rounded elevated dropdown."
        ) {
            val state = rememberNepaliDatePickerState()
            NepaliDatePickerDocked(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Appointment") },
                placeholder = { Text("Choose a day") },
                prefix = { Text("BS ") },
                dateFormatStyle = NepaliDateFormatStyle.FULL,
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(12.dp),
                popupShape = RoundedCornerShape(8.dp),
                popupShadowElevation = 12.dp
            )
            SelectedText(state.selectedDate.readout()?.let { "Selected: $it" })
        }
    }
}
