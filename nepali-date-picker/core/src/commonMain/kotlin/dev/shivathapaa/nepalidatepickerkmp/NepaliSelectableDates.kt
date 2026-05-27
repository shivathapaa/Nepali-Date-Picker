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

import androidx.compose.runtime.Stable
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar

/**
 * Predicate over [CustomCalendar] that the date picker UI consults to enable or disable individual
 * days and years. Lives in `:nepali-date-picker:core` because the converter factories
 * ([dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.BeforeDateSelectable] etc.)
 * also produce instances and we want those callable without the UI artifact on the classpath.
 */
@Stable
interface NepaliSelectableDates {

    /**
     * Returns true if the date item representing the [customCalendar] should be enabled for
     * selection in the UI.
     */
    fun isSelectableDate(customCalendar: CustomCalendar) = true

    /**
     * Returns true if a given [year] should be enabled for selection in the UI. When a year is
     * defined as non selectable, all the dates in that year will also be non selectable.
     */
    fun isSelectableYear(year: Int) = true
}
