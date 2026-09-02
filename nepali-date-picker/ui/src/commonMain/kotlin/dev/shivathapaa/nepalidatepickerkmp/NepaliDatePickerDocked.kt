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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.icons.NepaliIcons
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import kotlinx.coroutines.flow.first

/**
 * A docked / compact Nepali date picker: a read-only text field showing the selected date, with a
 * trailing calendar button that opens the [NepaliDatePicker] in a dropdown anchored to the field.
 *
 * This is the Material 3 "docked" pattern - the right default for forms and desktop / web, where a
 * full modal dialog is heavier than needed. Picking a date fills the field and closes the dropdown.
 *
 * @param state the [NepaliDatePickerState] driving the field and the calendar. See
 *   [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] applied to the field container.
 * @param label optional label for the text field.
 * @param placeholder optional placeholder shown when no date is selected.
 * @param dateFormatStyle the [NepaliDateFormatStyle] used to render the selected date in the field.
 * @param showTodayButton whether the calendar shows its `TODAY` button.
 * @param colors the [NepaliDatePickerColors]; its `dateTextFieldColors` theme the field.
 *
 * Example usage:
 * ```
 * val state = rememberNepaliDatePickerState()
 * NepaliDatePickerDocked(state = state, label = { Text("Date") })
 * ```
 *
 * @see NepaliDatePicker
 * @see NepaliDateField
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDatePickerDocked(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    dateFormatStyle: NepaliDateFormatStyle = NepaliDateFormatStyle.MEDIUM,
    showTodayButton: Boolean = true,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val calendarModel = remember(state.locale) { NepaliCalendarModel(state.locale) }
    val fieldLocale = remember(state.locale, dateFormatStyle) {
        state.locale.copy(dateFormat = dateFormatStyle)
    }
    val displayText = state.selectedDate?.let { calendarModel.formatNepaliDate(it, fieldLocale) } ?: ""

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = label,
            placeholder = placeholder,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = NepaliIcons.DateRange,
                        contentDescription = state.locale.language.switchToCalendarModeContentDescription
                    )
                }
            },
            colors = colors.dateTextFieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        if (expanded) {
            // Close the dropdown once a new date is chosen (compare against the value at open time).
            LaunchedEffect(Unit) {
                val startingSelection = state.selectedDate
                snapshotFlow { state.selectedDate }.first { it != startingSelection }
                expanded = false
            }

            Popup(
                popupPositionProvider = DropdownBelowAnchor,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier.padding(vertical = DockedPopupGap),
                    shape = NepaliDatePickerDefaults.shape,
                    color = colors.containerColor,
                    shadowElevation = DockedPopupElevation
                ) {
                    NepaliDatePicker(
                        state = state,
                        title = null,
                        showModeToggle = false,
                        showTodayButton = showTodayButton,
                        colors = colors
                    )
                }
            }
        }
    }
}

/**
 * Places the dropdown directly below its anchor, flipping above when it would overflow the bottom,
 * and clamping horizontally so it stays within the window.
 */
private val DropdownBelowAnchor = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = anchorBounds.left
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val below = anchorBounds.bottom
        val above = anchorBounds.top - popupContentSize.height
        val fitsBelow = below + popupContentSize.height <= windowSize.height
        val y = if (fitsBelow || above < 0) below else above
        return IntOffset(x, y)
    }
}

private val DockedPopupGap = 4.dp
private val DockedPopupElevation = 6.dp
