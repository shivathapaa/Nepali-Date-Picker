/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.compareDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.jvm.JvmInline

@Composable
internal fun NepaliDateInputContent(
    selectedDate: CustomCalendar?,
    onDateSelectionChange: (CustomCalendar?) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    language: NepaliDatePickerLang,
    nepaliSelectableDates: NepaliSelectableDates,
    colors: NepaliDatePickerColors
) {
    val errorDateOutOfYearRange =
        calendarModel.localizeNumber(
            stringToLocalize = "${language.errorDateOutOfYearRange} ${yearRange.first} - ${yearRange.last}",
            locale = language
        )


    val dateInputValidator =
        remember {
            NepaliDateInputValidator(
                yearRange = yearRange,
                nepaliSelectableDates = nepaliSelectableDates,
                errorInvalidMonthOrDay = language.errorInvalidMonthOrDay,
                errorDateInvalidInput = language.errorInvalidDay,
                errorDateOutOfYearRange = errorDateOutOfYearRange,
                errorInvalidNotAllowed = language.errorDateNotAllowed,
                errorInvalidRangeInput = "" // Not used for a single date input
            )
        }

    NepaliDateInputTextField(
        modifier = Modifier.fillMaxWidth().padding(NepaliDateInputTextFieldPadding),
        calendarModel = calendarModel,
        label = { Text(language.nepaliDate) },
        placeholder = { Text(PatternFormat) },
        initialSelectedDate = selectedDate,
        onDateSelectionChange = onDateSelectionChange,
        nepaliDateInputIdentifier = NepaliDateInputIdentifier.SingleDateInput,
        nepaliDateInputValidator =
        dateInputValidator.apply {
            // Only need to apply the start date, as this is for a single date input.
            currentStartDate = selectedDate
        },
        language = language,
        colors = colors
    )
}

@Composable
internal fun NepaliDateInputTextField(
    modifier: Modifier,
    initialSelectedDate: CustomCalendar?,
    onDateSelectionChange: (CustomCalendar?) -> Unit,
    calendarModel: NepaliCalendarModel,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    nepaliDateInputIdentifier: NepaliDateInputIdentifier,
    nepaliDateInputValidator: NepaliDateInputValidator,
    language: NepaliDatePickerLang,
    colors: NepaliDatePickerColors
) {
    val errorText = rememberSaveable { mutableStateOf("") }
    var text by
    rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text =
                initialSelectedDate?.let { date ->
                    val year = date.year.toString()
                    val month = date.month.toString().padStart(2, '0')
                    val day = date.dayOfMonth.toString().padStart(2, '0')
                    "$year$month$day"
                } ?: "",
                TextRange(0, 0)
            )
        )
    }

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            if (
                input.text.length <= LengthOfCharOfFormattedInputDate &&
                input.text.all { it.isDigit() }
            ) {
                text = input
                val trimmedText = input.text.trim()
                if (
                    trimmedText.isEmpty() ||
                    trimmedText.length < LengthOfCharOfFormattedInputDate
                ) {
                    errorText.value = ""
                    onDateSelectionChange(null)
                } else {
                    val parsedDate = calendarModel.parse(trimmedText)
                    errorText.value =
                        nepaliDateInputValidator.validate(
                            dateToValidate = parsedDate,
                            nepaliDateInputIdentifier = nepaliDateInputIdentifier
                        )
                    // Set the parsed date only if the error validation returned an empty string.
                    // Otherwise, set it to null, as the validation failed.
                    onDateSelectionChange(
                        if (errorText.value.isEmpty()) {
                            parsedDate
                        } else {
                            null
                        }
                    )
                }
            }
        },
        modifier =
        modifier
            .padding(
                bottom =
                if (errorText.value.isNotBlank()) {
                    0.dp
                } else {
                    NepaliDateInputTextNonErroneousBottomPadding
                }
            ),
        label = label,
        placeholder = placeholder,
        supportingText = { if (errorText.value.isNotBlank()) Text(errorText.value) },
        isError = errorText.value.isNotBlank(),
        visualTransformation = DateVisualTransformation(language, calendarModel::localizeNumber),
        keyboardOptions =
        KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        colors = colors.dateTextFieldColors
    )
}

@Stable
internal class NepaliDateInputValidator(
    private val yearRange: IntRange,
    private val nepaliSelectableDates: NepaliSelectableDates,
    private val errorInvalidMonthOrDay: String,
    private val errorDateInvalidInput: String,
    private val errorDateOutOfYearRange: String,
    private val errorInvalidNotAllowed: String,
    private val errorInvalidRangeInput: String,
    internal var currentStartDate: CustomCalendar? = null,
    internal var currentEndDate: CustomCalendar? = null,
) {
    fun validate(
        dateToValidate: CustomCalendar?,
        nepaliDateInputIdentifier: NepaliDateInputIdentifier
    ): String {
        if (dateToValidate == null) {
            return errorInvalidMonthOrDay
        }
        // Check that the date is within the valid range of years.
        if (!yearRange.contains(dateToValidate.year)) {
            return errorDateOutOfYearRange
        }

        if (dateToValidate.totalDaysInMonth == -1) {
            return errorDateInvalidInput
        }

        // Check that the provided NepaliSelectableDates allows this date to be selected.
        with(nepaliSelectableDates) {
            if (
                !isSelectableYear(dateToValidate.year) ||
                !isSelectableDate(dateToValidate)
            ) {
                return errorInvalidNotAllowed
            }
        }

        // Additional validation when the NepaliDateInputIdentifier is for start of end dates in a range input
        if (
            (nepaliDateInputIdentifier == NepaliDateInputIdentifier.StartDateInput && (compareDates(
                dateToValidate.toSimpleDate(),
                currentEndDate?.year ?: IntMaxValue,
                currentEndDate?.month ?: IntMaxValue,
                currentEndDate?.dayOfMonth ?: IntMaxValue
            ) >= 0)) ||
            (nepaliDateInputIdentifier == NepaliDateInputIdentifier.EndDateInput && (compareDates(
                dateToValidate.toSimpleDate(),
                currentStartDate?.year ?: IntMinValue,
                currentStartDate?.month ?: IntMinValue,
                currentStartDate?.dayOfMonth ?: IntMinValue
            ) < 0))
        ) {
            // The input start date is after the end date, or the end date is before the start date.
            return errorInvalidRangeInput
        }

        return ""
    }
}

@Immutable
@JvmInline
internal value class NepaliDateInputIdentifier internal constructor(internal val value: Int) {

    companion object {
        /** Single date input */
        val SingleDateInput = NepaliDateInputIdentifier(0)

        /** A start date input */
        val StartDateInput = NepaliDateInputIdentifier(1)

        /** An end date input */
        val EndDateInput = NepaliDateInputIdentifier(2)
    }

    override fun toString() =
        when (this) {
            SingleDateInput -> "SingleDateInput"
            StartDateInput -> "StartDateInput"
            EndDateInput -> "EndDateInput"
            else -> "Unknown"
        }
}

private class DateVisualTransformation(
    private val language: NepaliDatePickerLang,
    private val localizeNumber: (String, NepaliDatePickerLang) -> String
) : VisualTransformation {
    private val firstDelimiterOffset: Int = 4 // Index of first delimiter (/)
    private val secondDelimiterOffset: Int = 7 // Index of second delimiter (/)
    private val dateFormatLength: Int = LengthOfCharOfFormattedInputDate

    private val dateOffsetTranslator =
        object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset < firstDelimiterOffset -> offset
                    offset < secondDelimiterOffset -> offset + 1
                    offset <= dateFormatLength -> offset + 2
                    else -> dateFormatLength + 2 // 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= firstDelimiterOffset - 1 -> offset
                    offset <= secondDelimiterOffset - 1 -> offset - 1
                    offset <= dateFormatLength + 1 -> offset - 2
                    else -> dateFormatLength // 8
                }
            }
        }

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmedText =
            if (text.text.length > dateFormatLength) {
                text.text.substring(0 until dateFormatLength)
            } else {
                text.text
            }
        var transformedText = ""
        trimmedText.forEachIndexed { index, char ->
            transformedText += char
            if (index + 1 == firstDelimiterOffset || index + 2 == secondDelimiterOffset) {
                transformedText += "/" // Add delimiter
            }
        }

        val localizedString = localizeNumber(transformedText, language)

        return TransformedText(AnnotatedString(localizedString), dateOffsetTranslator)
    }
}

internal const val PatternFormat = "YYYY/MM/DD"

internal val NepaliDateInputTextFieldPadding =
    PaddingValues(start = 24.dp, end = 24.dp, top = 10.dp)

private const val LengthOfCharOfFormattedInputDate = 8

private val NepaliDateInputTextNonErroneousBottomPadding = 16.dp

private const val IntMaxValue = Int.MAX_VALUE
private const val IntMinValue = Int.MIN_VALUE
