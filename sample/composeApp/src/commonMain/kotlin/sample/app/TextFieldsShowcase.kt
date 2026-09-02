/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package sample.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeTextField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateTextField
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

/** Typed date entry: standalone fields, picker-backed fields, patterns, styling, and validation. */
@Composable
fun TextFieldsShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        DemoSection(
            "Date text field",
            "Type a date directly. Latin and Devanagari digits are both accepted."
        ) {
            var value by rememberSimpleDateState()
            NepaliDateTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") }
            )
            SelectedText(value.readout()?.let { "Parsed: $it" })
        }

        DemoSection(
            "Date field with picker",
            "The same field plus a trailing calendar icon that opens the dialog."
        ) {
            var value by rememberSimpleDateState()
            NepaliDateField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") }
            )
            SelectedText(value.readout()?.let { "Parsed: $it" })
        }

        DemoSection(
            "Range text field",
            "Two linked fields. An end earlier than the start is rejected."
        ) {
            var start by remember { mutableStateOf<SimpleDate?>(null) }
            var end by remember { mutableStateOf<SimpleDate?>(null) }
            NepaliDateRangeTextField(
                startValue = start,
                endValue = end,
                onRangeChange = { s, e -> start = s; end = e },
                modifier = Modifier.fillMaxWidth()
            )
            SelectedText(rangeReadoutOrNull(start.readout(), end.readout()))
        }

        DemoSection(
            "Range field with picker",
            "The linked fields plus a trailing icon that opens the range dialog."
        ) {
            var start by remember { mutableStateOf<SimpleDate?>(null) }
            var end by remember { mutableStateOf<SimpleDate?>(null) }
            NepaliDateRangeField(
                startValue = start,
                endValue = end,
                onRangeChange = { s, e -> start = s; end = e },
                modifier = Modifier.fillMaxWidth()
            )
            SelectedText(rangeReadoutOrNull(start.readout(), end.readout()))
        }

        DemoSection(
            "Input patterns",
            "The separator and field order follow the chosen pattern; the placeholder shows the mask."
        ) {
            var a by rememberSimpleDateState()
            NepaliDateTextField(
                value = a,
                onValueChange = { a = it },
                modifier = Modifier.fillMaxWidth(),
                dateFormat = Pattern.DD_SLASH_MM_SLASH_YYYY,
                label = { Text("DD/MM/YYYY") }
            )
            VerticalGap()
            var b by rememberSimpleDateState()
            NepaliDateTextField(
                value = b,
                onValueChange = { b = it },
                modifier = Modifier.fillMaxWidth(),
                dateFormat = Pattern.YYYY_DASH_MM_DASH_DD,
                label = { Text("YYYY-MM-DD") }
            )
        }

        DemoSection(
            "Fully customized field",
            "Custom shape, prefix and suffix, text style, colors, and Nepali output."
        ) {
            var value by rememberSimpleDateState()
            NepaliDateTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                locale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI),
                label = { Text("मिति") },
                prefix = { Text("वि.सं. ") },
                suffix = { Text(" BS") },
                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            SelectedText(value.readout()?.let { "Parsed: $it" })
        }

        DemoSection(
            "Validation and errors",
            "The field emits null until the input is a complete, valid date. Drive isError from that."
        ) {
            var value by rememberSimpleDateState()
            var touched by remember { mutableStateOf(false) }
            val showError = touched && value == null
            NepaliDateField(
                value = value,
                onValueChange = {
                    touched = true
                    value = it
                },
                modifier = Modifier.fillMaxWidth(),
                isError = showError,
                label = { Text("Required date") },
                supportingText = {
                    Text(if (showError) "Enter a complete, valid BS date" else "Format: YYYY/MM/DD")
                }
            )
            SelectedText(value.readout()?.let { "Parsed: $it" })
        }
    }
}

private fun rangeReadoutOrNull(start: String?, end: String?): String? =
    if (start == null && end == null) null else "Range: ${start ?: "..."} to ${end ?: "..."}"
