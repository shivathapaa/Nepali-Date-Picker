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

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp.android

import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.shivathapaa.nepalidatepickerkmp.NepaliCalendarSystemToggle
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang

/**
 * The Bikram Sambat and Gregorian toggle as a plain [View]. Mirrors the iOS
 * `NepaliCalendarSystemToggleViewController`.
 *
 * The toggle owns its selection: it flips on tap and reports each change through
 * [onCalendarSystemChange].
 */
fun NepaliCalendarSystemToggleView(
    context: Context,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH,
    onHeightChange: (Float) -> Unit = {},
    onCalendarSystemChange: (CalendarSystem) -> Unit
): View = nepaliPickerView(context, onHeightChange) {
    var calendarSystem by remember { mutableStateOf(initialCalendarSystem) }

    NepaliCalendarSystemToggle(
        calendarSystem = calendarSystem,
        onCalendarSystemChange = { tapped ->
            calendarSystem = tapped
            onCalendarSystemChange(tapped)
        },
        language = language
    )
}
