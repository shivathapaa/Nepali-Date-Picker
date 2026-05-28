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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.DialogProperties
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.localizeDigits
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.latinDigitOrNull
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.icons.NepaliIcons

/**
 * Standalone outlined text field that edits a Nepali (Bikram Sambat) [SimpleDate].
 *
 * The field accepts both Latin (`2082/02/14`) and Devanagari (`२०८२/०२/१४`) digit
 * input regardless of [locale]; output digit script always follows
 * [NepaliDateLocale.resolvedDigitScript]. Users type digits only — separators are
 * inserted by a [VisualTransformation], so the underlying state holds 8 ASCII digits.
 *
 * Validation pipeline (runs on every keystroke):
 *   1. Length < 8 digits → emit `null`, [isError] stays as the caller passed it.
 *   2. Length == 8 → parse with [NepaliDateFormatter]. Parse failure → emit `null`
 *      with a non-empty error surfaced to the caller via the supplied [isError].
 *   3. Year outside [yearRange] → emit `null`, error surfaced.
 *   4. `selectableDates.isSelectableYear` or `isSelectableDate` rejects → emit `null`, error surfaced.
 *   5. All checks pass → emit the [SimpleDate]; no error.
 *
 * The composable does NOT paint its own error indicator — pass [isError] to control
 * the field's error state; consult the `onValueChange` callback for the resolved
 * [SimpleDate]?. When the caller wants to display per-rule error messages, wire
 * [supportingText] yourself based on the surface API exposed by your form layer.
 *
 * @param value externally-controlled selection. `null` means "no date".
 * @param onValueChange invoked on every keystroke with the parsed-and-validated
 *   [SimpleDate]?. `null` indicates "input is incomplete or invalid".
 * @param dateFormat input/display pattern. See [Pattern] for the supported set.
 * @param yearRange BS year range the field will accept. Out-of-range parses emit null.
 * @param selectableDates predicate that further restricts which dates the field accepts.
 * @param locale localization for output digits + label conventions. Digit script in
 *   [locale] only affects display — input is always tolerant of both Latin and Devanagari.
 *
 * @see NepaliDateField for a combo that pairs this field with a picker dialog.
 */
@Composable
fun NepaliDateTextField(
    value: SimpleDate?,
    onValueChange: (SimpleDate?) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: Pattern = Pattern.YYYY_SLASH_MM_SLASH_DD,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = { androidx.compose.material3.Text(dateFormat.literal) },
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    val onValueChangeUpdated by rememberUpdatedState(onValueChange)
    val selectableDatesUpdated by rememberUpdatedState(selectableDates)

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = value?.toDigitString(dateFormat) ?: ""))
    }

    // External value change (e.g. dialog confirmed) syncs into local state.
    LaunchedEffect(value, dateFormat) {
        val externalDigits = value?.toDigitString(dateFormat) ?: ""
        if (text.text != externalDigits) {
            text = TextFieldValue(text = externalDigits, selection = TextRange(externalDigits.length))
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            val filtered = sanitizeDigits(input.text, maxDigits = dateFormat.digitCount)
            // Preserve cursor at end after filtering — keystrokes are append-mostly.
            val newSelection = if (input.selection.collapsed && input.selection.end == input.text.length) {
                TextRange(filtered.length)
            } else {
                val clampedStart = input.selection.start.coerceAtMost(filtered.length)
                val clampedEnd = input.selection.end.coerceAtMost(filtered.length)
                TextRange(clampedStart, clampedEnd)
            }
            text = TextFieldValue(text = filtered, selection = newSelection)

            if (filtered.length < dateFormat.digitCount) {
                onValueChangeUpdated(null)
                return@OutlinedTextField
            }
            val rendered = formatDigits(filtered, dateFormat, DigitScript.LATIN)
            val parsed = NepaliDateFormatter.parse(rendered, dateFormat)
            if (parsed == null || parsed.year !in yearRange) {
                onValueChangeUpdated(null)
                return@OutlinedTextField
            }
            // Convert SimpleDate -> CustomCalendar for selectableDates predicate.
            val customCalendar: CustomCalendar? = runCatching {
                NepaliCalendarModelHolder.model.getNepaliCalendar(parsed)
            }.getOrNull()
            if (customCalendar == null ||
                !selectableDatesUpdated.isSelectableYear(parsed.year) ||
                !selectableDatesUpdated.isSelectableDate(customCalendar)
            ) {
                onValueChangeUpdated(null)
                return@OutlinedTextField
            }
            onValueChangeUpdated(parsed)
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = remember(dateFormat, locale) {
            NepaliDateMaskTransformation(dateFormat, locale.resolvedDigitScript)
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        colors = colors,
    )
}

/**
 * Material3-style date field — [NepaliDateTextField] with a trailing calendar icon
 * that opens [NepaliDatePickerDialog].
 *
 * Same params as [NepaliDateTextField]. The picker dialog respects [yearRange],
 * [selectableDates], and [locale] so the two surfaces stay in sync.
 *
 * When the user confirms a date in the dialog, [onValueChange] fires once with the
 * picked [SimpleDate]. Dismiss does not change [value].
 */
@Composable
fun NepaliDateField(
    value: SimpleDate?,
    onValueChange: (SimpleDate?) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: Pattern = Pattern.YYYY_SLASH_MM_SLASH_DD,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    selectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = { androidx.compose.material3.Text(dateFormat.literal) },
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    dialogProperties: DialogProperties = DialogProperties(),
    confirmButtonText: String = locale.language.okText,
    dismissButtonText: String = locale.language.cancelText,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val onValueChangeUpdated by rememberUpdatedState(onValueChange)

    Box {
        NepaliDateTextField(
            value = value,
            onValueChange = onValueChangeUpdated,
            modifier = modifier,
            dateFormat = dateFormat,
            yearRange = yearRange,
            selectableDates = selectableDates,
            locale = locale,
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            leadingIcon = leadingIcon,
            trailingIcon = {
                IconButton(onClick = { if (enabled && !readOnly) showDialog = true }) {
                    Icon(
                        imageVector = NepaliIcons.DateRange,
                        contentDescription = locale.language.selectDateText
                    )
                }
            },
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = colors,
        )
    }

    if (showDialog) {
        val pickerState = rememberNepaliDatePickerState(
            initialSelectedDate = value,
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
                        val picked = pickerState.selectedDate?.toSimpleDate()
                        if (picked != null) onValueChangeUpdated(picked)
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
            NepaliDatePicker(state = pickerState)
        }
    }
}

// ── internals ──────────────────────────────────────────────────────────────

/** Visual transform: insert pattern delimiters + localize digits for display. */
private class NepaliDateMaskTransformation(
    private val pattern: Pattern,
    private val digitScript: DigitScript,
) : VisualTransformation {

    private val delim1: Int = if (pattern.yearFirst) 4 else 2
    private val delim2: Int = if (pattern.yearFirst) 6 else 4
    private val fullDigitLen = pattern.digitCount

    private val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = when {
            offset <= delim1 -> offset
            offset <= delim2 -> offset + 1
            offset <= fullDigitLen -> offset + 2
            else -> fullDigitLen + 2
        }

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= delim1 -> offset
            offset <= delim2 + 1 -> offset - 1
            offset <= fullDigitLen + 1 -> offset - 2
            else -> fullDigitLen
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length > fullDigitLen) text.text.substring(0, fullDigitLen) else text.text
        val rendered = buildString(trimmed.length + 2) {
            for ((i, ch) in trimmed.withIndex()) {
                append(ch)
                if (i + 1 == delim1 || i + 1 == delim2) append(pattern.delimiter)
            }
        }
        val localized = formatDigits(rendered, pattern = null, digitScript)
        return TransformedText(AnnotatedString(localized), offsetTranslator)
    }
}

/** Sanitize input: strip non-digits, fold Devanagari → Latin, cap length. */
private fun sanitizeDigits(raw: String, maxDigits: Int): String {
    val builder = StringBuilder(raw.length)
    for (ch in raw) {
        if (builder.length >= maxDigits) break
        val latin = ch.latinDigitOrNull() ?: continue
        builder.append(latin)
    }
    return builder.toString()
}

/**
 * Build a `pattern`-shaped string from 8 raw digits and localize to [script].
 * When [raw] is shorter than `pattern.digitCount`, returns just [raw] localized
 * (mask transform inserts partial delimiters separately).
 */
private fun formatDigits(raw: String, pattern: Pattern?, script: DigitScript): String {
    val latinSource = if (pattern == null || raw.length != pattern.digitCount) {
        raw
    } else {
        val date = parseDigits(raw, pattern) ?: return raw.localizeDigits(script)
        NepaliDateFormatter.format(date, pattern, DigitScript.LATIN)
    }
    return latinSource.localizeDigits(script)
}

private fun parseDigits(raw: String, pattern: Pattern): SimpleDate? {
    if (raw.length != pattern.digitCount) return null
    val (yyyy, mm, dd) = if (pattern.yearFirst) {
        Triple(raw.substring(0, 4), raw.substring(4, 6), raw.substring(6, 8))
    } else {
        Triple(raw.substring(4, 8), raw.substring(2, 4), raw.substring(0, 2))
    }
    val year = yyyy.toIntOrNull() ?: return null
    val month = mm.toIntOrNull() ?: return null
    val day = dd.toIntOrNull() ?: return null
    return SimpleDate(year, month, day)
}

private fun SimpleDate.toDigitString(pattern: Pattern): String {
    val yyyy = year.toString().padStart(4, '0')
    val mm = month.toString().padStart(2, '0')
    val dd = dayOfMonth.toString().padStart(2, '0')
    return if (pattern.yearFirst) "$yyyy$mm$dd" else "$dd$mm$yyyy"
}

/** Singleton converter — avoids creating a new [NepaliCalendarModel] per recomposition. */
private object NepaliCalendarModelHolder {
    val model: NepaliCalendarModel = NepaliCalendarModel()
}
