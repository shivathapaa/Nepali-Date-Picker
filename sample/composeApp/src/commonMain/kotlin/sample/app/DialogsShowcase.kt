/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package sample.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerFullScreenDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

// DialogButton takes its click before an `enabled` flag, so a trailing lambda cannot bind to it.
// This wrapper puts the click last so the call sites can read naturally.
@Composable
private fun dialogButton(text: String, onClick: () -> Unit) {
    NepaliDatePickerDefaults.DialogButton(text = text, onButtonClick = onClick)
}

/** Modal and full-screen dialog hosts around the same pickers. */
@Composable
fun DialogsShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        DemoSection(
            "Modal date dialog",
            "The standard Material3 modal, dismissed on confirm or cancel."
        ) {
            var show by rememberSaveable { mutableStateOf(false) }
            var result by remember { mutableStateOf<String?>(null) }
            val state = rememberNepaliDatePickerState()

            Button(onClick = { show = true }) { Text("Open date dialog") }
            SelectedText(result?.let { "Picked: $it" })

            if (show) {
                NepaliDatePickerDialog(
                    onDismissRequest = { show = false },
                    confirmButton = {
                        dialogButton("OK") {
                            result = state.selectedDate.readout()
                            show = false
                        }
                    },
                    dismissButton = {
                        dialogButton("Cancel") { show = false }
                    }
                ) {
                    NepaliDatePicker(state = state)
                }
            }
        }

        DemoSection(
            "Modal range dialog",
            "The range picker hosted in a modal, using horizontal months so it stays compact."
        ) {
            var show by rememberSaveable { mutableStateOf(false) }
            var result by remember { mutableStateOf<String?>(null) }
            val state = rememberNepaliDateRangePickerState()

            Button(onClick = { show = true }) { Text("Open range dialog") }
            SelectedText(result)

            if (show) {
                NepaliDatePickerDialog(
                    onDismissRequest = { show = false },
                    confirmButton = {
                        dialogButton("OK") {
                            val start = state.selectedStartNepaliDate.readout()
                            val end = state.selectedEndNepaliDate.readout()
                            result = "Range: ${start ?: "..."} to ${end ?: "..."}"
                            show = false
                        }
                    },
                    dismissButton = {
                        dialogButton("Cancel") { show = false }
                    }
                ) {
                    NepaliDateRangePicker(state = state, showMonthsVertically = false)
                }
            }
        }

        DemoSection(
            "Full-screen range dialog",
            "A full-screen host with vertical months, for a focused range selection flow."
        ) {
            var show by rememberSaveable { mutableStateOf(false) }
            var result by remember { mutableStateOf<String?>(null) }
            val state = rememberNepaliDateRangePickerState()

            Button(onClick = { show = true }) { Text("Open full-screen dialog") }
            SelectedText(result)

            if (show) {
                NepaliDatePickerFullScreenDialog(
                    onDismissRequest = { show = false },
                    confirmButton = {
                        dialogButton("OK") {
                            val start = state.selectedStartNepaliDate.readout()
                            val end = state.selectedEndNepaliDate.readout()
                            result = "Range: ${start ?: "..."} to ${end ?: "..."}"
                            show = false
                        }
                    },
                    dismissButton = {
                        dialogButton("Cancel") { show = false }
                    },
                    title = { Text("Select a range") }
                ) {
                    NepaliDateRangePicker(state = state, showMonthsVertically = true)
                }
            }
        }

        DemoSection(
            "Custom-styled dialog",
            "The modal accepts its own shape and tonal elevation."
        ) {
            var show by rememberSaveable { mutableStateOf(false) }
            val state = rememberNepaliDatePickerState()

            Button(onClick = { show = true }) { Text("Open styled dialog") }

            if (show) {
                NepaliDatePickerDialog(
                    onDismissRequest = { show = false },
                    confirmButton = {
                        dialogButton("Done") { show = false }
                    },
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 8.dp
                ) {
                    NepaliDatePicker(state = state)
                }
            }
        }
    }
}
