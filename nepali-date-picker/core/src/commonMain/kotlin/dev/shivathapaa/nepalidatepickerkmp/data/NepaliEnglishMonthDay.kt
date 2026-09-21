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

package dev.shivathapaa.nepalidatepickerkmp.data

import dev.shivathapaa.nepalidatepickerkmp.annotation.Immutable

/**
 * One day of a Gregorian month beside the Bikram Sambat calendar it converts to, which is absent for
 * a day the conversion table does not reach.
 *
 * Pairing the two keeps the absence expressible everywhere the library ships. A list of optional
 * elements says the same thing in Kotlin, but Objective-C cannot describe element optionality, so
 * such a list reaches Swift as `[Any]` with a missing day arriving as `NSNull`, which reads as
 * present. Here the optionality sits on a property, where every platform keeps it.
 *
 * @property englishDayOfMonth the Gregorian day this entry describes, 1 for the first of the month.
 * @property nepaliCalendar the Bikram Sambat calendar for that day, or `null` when the day falls
 *   before [dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults.minConvertibleEnglishDate].
 */
@Immutable
data class NepaliEnglishMonthDay(
    val englishDayOfMonth: Int,
    val nepaliCalendar: CustomCalendar?
)
