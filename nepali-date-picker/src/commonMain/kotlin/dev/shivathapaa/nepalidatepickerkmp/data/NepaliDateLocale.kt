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

package dev.shivathapaa.nepalidatepickerkmp.data

import androidx.compose.runtime.Immutable

@Immutable
data class NepaliDateLocale(
    val language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH,
    val dateFormat: NepaliDateFormatStyle = NepaliDateFormatStyle.LONG,
    val weekDayName: NameFormat = NameFormat.FULL,
    val monthName: NameFormat = NameFormat.FULL
)

enum class NameFormat {
    FULL, MEDIUM, SHORT
}

enum class NepaliDateFormatStyle {
    FULL,         // Monday, Asar 21, 2024 or सोमबार, असार २१, २०२४
    LONG,         // Asar 21, 2024 or असार २१, २०२४
    MEDIUM,       // 2024 Asar 21 or २०२४ असार २१
    SHORT_MDY,    // 06/21/2024 or ०६/२१/२०२४
    SHORT_YMD,    // 2024/06/21 or २०२४/०६/२१
    COMPACT_MDY,  // 06/21/24 or ०६/२१/२४
    COMPACT_YMD   // 24/06/21 or २४/०६/२१
}

@Immutable
data class NepaliWeekdayName(val short: String, val medium: String, val full: String)

@Immutable
data class NepaliMonthName(val short: String, val full: String)

enum class NepaliDatePickerLang {
    ENGLISH {
        override val weekdays: List<NepaliWeekdayName> = englishWeekdays
        override val months: List<NepaliMonthName> = nepaliMonthsInEnglish
        override val selectDateText: String = "Select Date"
        override val datePickerTitle: String = "Select Nepali Date"
        override val cancelText: String = "Cancel"
        override val okText: String = "OK"
        override val today: String = "TODAY"
    },
    NEPALI {
        override val weekdays: List<NepaliWeekdayName> = nepaliWeekdays
        override val months: List<NepaliMonthName> = nepaliMonths
        override val selectDateText: String = "मिति चयन गर्नुहोस्"
        override val datePickerTitle: String = "नेपाली मिति चयन गर्नुहोस्"
        override val cancelText: String = "रद्द गर्नुहोस्"
        override val okText: String = "भयो"
        override val today: String = "आज"
    };

    abstract val weekdays: List<NepaliWeekdayName>
    abstract val months: List<NepaliMonthName>
    abstract val selectDateText: String
    abstract val datePickerTitle: String
    abstract val cancelText: String
    abstract val okText: String
    abstract val today: String
}

private val nepaliMonths = listOf(
    NepaliMonthName("बै", "बैशाख"),
    NepaliMonthName("जे", "जेठ"),
    NepaliMonthName("अ", "असार"),
    NepaliMonthName("सा", "साउन"),
    NepaliMonthName("भ", "भदौ"),
    NepaliMonthName("अ", "असोज"),
    NepaliMonthName("का", "कार्तिक"),
    NepaliMonthName("मं", "मंसिर"),
    NepaliMonthName("पु", "पौष"),
    NepaliMonthName("मा", "माघ"),
    NepaliMonthName("फा", "फाल्गुन"),
    NepaliMonthName("चै", "चैत")
)

private val nepaliMonthsInEnglish = listOf(
    NepaliMonthName("Bai", "Baisakh"),
    NepaliMonthName("Jes", "Jestha"),
    NepaliMonthName("Asa", "Asar"),
    NepaliMonthName("Shr", "Shrawn"),
    NepaliMonthName("Bha", "Bhadra"),
    NepaliMonthName("Aso", "Asoj"),
    NepaliMonthName("Kar", "Kartik"),
    NepaliMonthName("Man", "Mangsir"),
    NepaliMonthName("Pou", "Poush"),
    NepaliMonthName("Mag", "Magh"),
    NepaliMonthName("Pha", "Falgun"),
    NepaliMonthName("Chai", "Chaitra")
)

private val nepaliWeekdays = listOf(
    NepaliWeekdayName("आ", "आईत", "आईतबार"),
    NepaliWeekdayName("सो", "सोम", "सोमबार"),
    NepaliWeekdayName("मं", "मंगल", "मंगलबार"),
    NepaliWeekdayName("बु", "बुध", "बुधबार"),
    NepaliWeekdayName("बि", "बिहि", "बिहिबार"),
    NepaliWeekdayName("शु", "शुक्र", "शुक्रबार"),
    NepaliWeekdayName("श", "शनि", "शनिबार")
)

private val englishWeekdays = listOf(
    NepaliWeekdayName("S", "Sun", "Sunday"),
    NepaliWeekdayName("M", "Mon", "Monday"),
    NepaliWeekdayName("T", "Tue", "Tuesday"),
    NepaliWeekdayName("W", "Wed", "Wednesday"),
    NepaliWeekdayName("T", "Thu", "Thursday"),
    NepaliWeekdayName("F", "Fri", "Friday"),
    NepaliWeekdayName("S", "Sat", "Saturday")
)

internal val englishMonths = listOf(
    NepaliMonthName("Jan", "January"),
    NepaliMonthName("Feb", "February"),
    NepaliMonthName("Mar", "March"),
    NepaliMonthName("Apr", "April"),
    NepaliMonthName("May", "May"),
    NepaliMonthName("Jun", "June"),
    NepaliMonthName("Jul", "July"),
    NepaliMonthName("Aug", "August"),
    NepaliMonthName("Sep", "September"),
    NepaliMonthName("Oct", "October"),
    NepaliMonthName("Nov", "November"),
    NepaliMonthName("Dec", "December")
)