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

package dev.shivathapaa.nepalidatepickerkmp.android

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateTextField
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.embed.EmbeddedPickerDefaults

/**
 * The date text field, with an optional picker dialog behind it, as a plain [View]. Mirrors the
 * iOS `NepaliDateFieldViewController` parameter for parameter.
 *
 * [outlined] chooses the bare text field; the filled variant opens the calendar dialog and takes
 * the confirm and dismiss texts. [onValueChange] fires with the plain date as it is typed or
 * picked, `null` while the input is empty or unparseable.
 */
fun NepaliDateFieldView(
    context: Context,
    initialValue: SimpleDate? = null,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    dateFormat: Pattern = Pattern.YYYY_SLASH_MM_SLASH_DD,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates? = null,
    outlined: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    confirmButtonText: String? = null,
    dismissButtonText: String? = null,
    cornerRadius: Float = EmbeddedPickerDefaults.FIELD_CORNER_RADIUS,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    events: NepaliEventOptions? = null,
    onHeightChange: (Float) -> Unit = {},
    onValueChange: (SimpleDate?) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    val dayMarks = events.toDecorator()
    var value by remember { mutableStateOf(initialValue) }

    val shape = RoundedCornerShape(cornerRadius.dp)
    val labelSlot: (@Composable () -> Unit)? = label?.let { text -> { Text(text) } }
    val supportingTextSlot: (@Composable () -> Unit)? =
        supportingText?.let { text -> { Text(text) } }
    val placeholderSlot: @Composable () -> Unit =
        placeholder?.let { text -> { Text(text) } } ?: { Text(dateFormat.literal) }

    val handleChange: (SimpleDate?) -> Unit = { newValue ->
        value = newValue
        onValueChange(newValue)
    }

    if (outlined) {
        NepaliDateTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRange,
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            label = labelSlot,
            placeholder = placeholderSlot,
            supportingText = supportingTextSlot,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            shape = shape,
            calendarSystem = initialCalendarSystem
        )
    } else {
        NepaliDateField(
            dayDecorator = dayMarks,
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRange,
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            label = labelSlot,
            placeholder = placeholderSlot,
            supportingText = supportingTextSlot,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            shape = shape,
            confirmButtonText = confirmButtonText ?: locale.language.okText,
            dismissButtonText = dismissButtonText ?: locale.language.cancelText,
            calendarSystem = initialCalendarSystem,
            showCalendarSystemToggle = showCalendarSystemToggle,
            showAdjacentMonthDays = showAdjacentMonthDays
        )
    }
}
