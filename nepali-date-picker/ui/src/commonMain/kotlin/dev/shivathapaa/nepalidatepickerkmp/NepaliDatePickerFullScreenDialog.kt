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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults

/**
 * A full-screen dialog host for the Nepali date/range pickers.
 *
 * Where [NepaliDatePickerDialog] is a centered modal with a fixed width, this fills the whole screen
 * - the Material 3 pattern that fits a **range** selection on a phone, or any picker on a small
 * screen where the 360dp modal would be cramped. The top bar carries the dismiss and confirm slots;
 * the picker goes in [content] and takes the remaining height (its own month list scrolls inside).
 *
 * @param onDismissRequest called when the user dismisses the dialog (back press / scrim).
 * @param confirmButton the confirm action shown at the end of the top bar (e.g. an OK button).
 * @param modifier the [Modifier] applied to the full-screen surface.
 * @param dismissButton the optional dismiss action shown at the start of the top bar.
 * @param title an optional centered title in the top bar.
 * @param colors the [NepaliDatePickerColors] used for the surface and divider.
 * @param properties the [DialogProperties]; defaults to non-platform width so the surface fills the screen.
 * @param content the picker to host - typically [NepaliDatePicker], [NepaliDateRangePicker], or an
 *   English-date variant. Receives a [ColumnScope].
 *
 * Example usage:
 * ```
 * if (showDialog) {
 *     NepaliDatePickerFullScreenDialog(
 *         onDismissRequest = { showDialog = false },
 *         confirmButton = { NepaliDatePickerDefaults.DialogButton("OK", { showDialog = false }) },
 *         dismissButton = { NepaliDatePickerDefaults.DialogButton("Cancel", { showDialog = false }) }
 *     ) {
 *         NepaliDateRangePicker(state = rangeState, showMonthsVertically = true)
 *     }
 * }
 * ```
 *
 * @see NepaliDatePickerDialog
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDatePickerFullScreenDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Surface(modifier = modifier.fillMaxSize(), color = colors.containerColor) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = FullScreenBarHorizontalPadding, vertical = FullScreenBarVerticalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.CenterStart) {
                        dismissButton?.invoke()
                    }
                    if (title != null) {
                        ProvideTextStyle(MaterialTheme.typography.titleMedium) { title() }
                    }
                    Box(contentAlignment = Alignment.CenterEnd) {
                        confirmButton()
                    }
                }
                HorizontalDivider(color = colors.dividerColor)
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize(), content = content)
                }
            }
        }
    }
}

private val FullScreenBarHorizontalPadding = 12.dp
private val FullScreenBarVerticalPadding = 8.dp
