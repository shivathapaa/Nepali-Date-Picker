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

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.icons.NepaliIcons

/**
 * Two stacked [NepaliDateTextField]s editing a Bikram Sambat date range.
 *
 * Validation per keystroke is identical to [NepaliDateTextField] for each side,
 * plus a cross-field rule: a parsed `endValue < startValue` is rejected - the
 * field emits the offending side as `null` and surfaces `isError = true` to the
 * caller via [isStartError] / [isEndError].
 *
 * The composable emits each side independently through [onRangeChange]; callers
 * who want a single `Pair<SimpleDate?, SimpleDate?>` callback can wrap.
 *
 * @param startValue externally-controlled start selection.
 * @param endValue externally-controlled end selection.
 * @param onRangeChange invoked on every keystroke. First arg is the new start
 *   (or unchanged); second is the new end. Both `null` means the user has not
 *   completed input on that side or it failed validation.
 *
 * @see NepaliDateRangeField for a combo that pairs this with the range picker dialog.
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDateRangeTextField(
    startValue: SimpleDate?,
    endValue: SimpleDate?,
    onRangeChange: (start: SimpleDate?, end: SimpleDate?) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: Pattern = Pattern.YYYY_SLASH_MM_SLASH_DD,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale,
    startLabel: @Composable (() -> Unit)? = { Text(locale.language.startDate) },
    endLabel: @Composable (() -> Unit)? = { Text(locale.language.endDate) },
    supportingText: @Composable (() -> Unit)? = null,
    isStartError: Boolean = false,
    isEndError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    val onRangeChangeUpdated by rememberUpdatedState(onRangeChange)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            NepaliDateTextField(
                value = startValue,
                onValueChange = { newStart ->
                    val coercedEnd = if (newStart != null && endValue != null && endValue < newStart) null else endValue
                    onRangeChangeUpdated(newStart, coercedEnd)
                },
                modifier = Modifier.weight(1f),
                dateFormat = dateFormat,
                yearRange = yearRange,
                selectableDates = selectableDates,
                locale = locale,
                label = startLabel,
                isError = isStartError,
                enabled = enabled,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = colors,
            )
            NepaliDateTextField(
                value = endValue,
                onValueChange = { newEnd ->
                    val rejectedAsEarlier = newEnd != null && startValue != null && newEnd < startValue
                    onRangeChangeUpdated(startValue, if (rejectedAsEarlier) null else newEnd)
                },
                modifier = Modifier.weight(1f),
                dateFormat = dateFormat,
                yearRange = yearRange,
                selectableDates = selectableDates,
                locale = locale,
                label = endLabel,
                isError = isEndError,
                enabled = enabled,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions.copy(imeAction = ImeAction.Done),
                keyboardActions = keyboardActions,
                colors = colors,
            )
        }
        if (supportingText != null) supportingText()
    }
}

/**
 * Material3-style range field - [NepaliDateRangeTextField] with a trailing calendar
 * icon that opens [NepaliDatePickerDialog] hosting [NepaliDateRangePicker].
 *
 * Same params as [NepaliDateRangeTextField] plus dialog [properties]. Dialog
 * respects the same [yearRange], [selectableDates], and [locale] so the two
 * surfaces stay in sync. Confirming in the dialog fires [onRangeChange] once
 * with the picked range. Dismiss does not change either value.
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDateRangeField(
    startValue: SimpleDate?,
    endValue: SimpleDate?,
    onRangeChange: (start: SimpleDate?, end: SimpleDate?) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: Pattern = Pattern.YYYY_SLASH_MM_SLASH_DD,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale,
    startLabel: @Composable (() -> Unit)? = { Text(locale.language.startDate) },
    endLabel: @Composable (() -> Unit)? = { Text(locale.language.endDate) },
    supportingText: @Composable (() -> Unit)? = null,
    isStartError: Boolean = false,
    isEndError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    dialogProperties: DialogProperties = DialogProperties(),
    confirmButtonText: String = locale.language.okText,
    dismissButtonText: String = locale.language.cancelText,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val onRangeChangeUpdated by rememberUpdatedState(onRangeChange)

    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NepaliDateRangeTextField(
                startValue = startValue,
                endValue = endValue,
                onRangeChange = onRangeChangeUpdated,
                modifier = Modifier.weight(1f),
                dateFormat = dateFormat,
                yearRange = yearRange,
                selectableDates = selectableDates,
                locale = locale,
                startLabel = startLabel,
                endLabel = endLabel,
                supportingText = supportingText,
                isStartError = isStartError,
                isEndError = isEndError,
                enabled = enabled,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = colors,
            )
            IconButton(onClick = { if (enabled && !readOnly) showDialog = true }) {
                Icon(
                    imageVector = NepaliIcons.DateRange,
                    contentDescription = locale.language.selectDateText
                )
            }
        }
    }

    if (showDialog) {
        val pickerState = rememberNepaliDateRangePickerState(
            initialSelectedStartNepaliDate = startValue,
            initialSelectedEndNepaliDate = endValue,
            yearRange = yearRange,
            nepaliSelectableDates = selectableDates,
            locale = locale,
        )
        NepaliDatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                NepaliDatePickerDefaults.DialogButton(
                    text = confirmButtonText,
                    onButtonClick = {
                        showDialog = false
                        val start = pickerState.selectedStartNepaliDate?.toSimpleDate()
                        val end = pickerState.selectedEndNepaliDate?.toSimpleDate()
                        onRangeChangeUpdated(start, end)
                    }
                )
            },
            dismissButton = {
                NepaliDatePickerDefaults.DialogButton(
                    text = dismissButtonText,
                    onButtonClick = { showDialog = false }
                )
            },
            properties = dialogProperties,
        ) {
            NepaliDateRangePicker(state = pickerState)
        }
    }
}
