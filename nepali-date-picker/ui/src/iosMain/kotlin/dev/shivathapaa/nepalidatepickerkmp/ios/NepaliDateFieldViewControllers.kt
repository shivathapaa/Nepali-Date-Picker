/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

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
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import platform.UIKit.UIViewController

/**
 * Hosts a single typed date entry field.
 *
 * `options.outlined` picks [NepaliDateTextField] when true and the filled [NepaliDateField] when
 * false. The controller owns the field contents, so Swift only observes them through
 * [onValueChange].
 *
 * @param initialValue date the field opens with, or `null` for empty.
 * @param locale language, date format and digit script the field renders with.
 * @param dateFormat digit layout the user types into, for example `YYYY_SLASH_MM_SLASH_DD`.
 * @param yearRangeStart first Bikram Sambat year accepted.
 * @param yearRangeEnd last Bikram Sambat year accepted.
 * @param selectableDates policy deciding which dates are valid, or `null` to accept every date.
 * @param options appearance and behaviour knobs, or `null` for the library defaults.
 * @param onHeightChange receives the content height in points, so the caller can size its frame.
 * @param onValueChange invoked whenever the field parses to a new value, or to `null` while the
 * entry is incomplete.
 */
fun NepaliDateFieldViewController(
    initialValue: SimpleDate?,
    locale: NepaliDateLocale,
    dateFormat: Pattern,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    selectableDates: NepaliSelectableDates?,
    options: NepaliFieldOptions?,
    onHeightChange: (Float) -> Unit,
    onValueChange: (SimpleDate?) -> Unit
): UIViewController = nepaliPickerViewController(onHeightChange) {
    val opts = options ?: NepaliFieldOptions()
    var value by remember { mutableStateOf(initialValue) }

    val shape = RoundedCornerShape(opts.cornerRadius.dp)
    val label: (@Composable () -> Unit)? = opts.label?.let { text -> { Text(text) } }
    val supportingText: (@Composable () -> Unit)? =
        opts.supportingText?.let { text -> { Text(text) } }
    // A null placeholder keeps the library's own hint, which spells out the expected pattern.
    val placeholder: @Composable () -> Unit =
        opts.placeholder?.let { text -> { Text(text) } } ?: { Text(dateFormat.literal) }

    val handleChange: (SimpleDate?) -> Unit = { newValue ->
        value = newValue
        onValueChange(newValue)
    }

    if (opts.outlined) {
        NepaliDateTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            isError = opts.isError,
            enabled = opts.enabled,
            readOnly = opts.readOnly,
            shape = shape
        )
    } else {
        NepaliDateField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = handleChange,
            dateFormat = dateFormat,
            yearRange = yearRangeOf(yearRangeStart, yearRangeEnd),
            selectableDates = selectableDates.orAllDates(),
            locale = locale,
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            isError = opts.isError,
            enabled = opts.enabled,
            readOnly = opts.readOnly,
            shape = shape,
            confirmButtonText = opts.confirmButtonText ?: locale.language.okText,
            dismissButtonText = opts.dismissButtonText ?: locale.language.cancelText
        )
    }
}
