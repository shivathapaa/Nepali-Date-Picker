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

/**
 * The month and weekday names each [NepaliDatePickerLang] answers with, in calendar order: index 0
 * is Baisakh or January, and index 0 of a weekday list is Sunday.
 */

internal val nepaliMonths = listOf(
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

internal val nepaliMonthsInEnglish = listOf(
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

internal val nepaliWeekdays = listOf(
    NepaliWeekdayName("आ", "आईत", "आईतबार"),
    NepaliWeekdayName("सो", "सोम", "सोमबार"),
    NepaliWeekdayName("मं", "मंगल", "मंगलबार"),
    NepaliWeekdayName("बु", "बुध", "बुधबार"),
    NepaliWeekdayName("बि", "बिहि", "बिहिबार"),
    NepaliWeekdayName("शु", "शुक्र", "शुक्रबार"),
    NepaliWeekdayName("श", "शनि", "शनिबार")
)

internal val englishWeekdays = listOf(
    NepaliWeekdayName("S", "Sun", "Sunday"),
    NepaliWeekdayName("M", "Mon", "Monday"),
    NepaliWeekdayName("T", "Tue", "Tuesday"),
    NepaliWeekdayName("W", "Wed", "Wednesday"),
    NepaliWeekdayName("T", "Thu", "Thursday"),
    NepaliWeekdayName("F", "Fri", "Friday"),
    NepaliWeekdayName("S", "Sat", "Saturday")
)

internal val englishMonthsInEnglish = listOf(
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

internal val englishMonthsInNepali = listOf(
    NepaliMonthName("जन", "जनवरी"),
    NepaliMonthName("फेब्रु", "फेब्रुअरी"),
    NepaliMonthName("मार्च", "मार्च"),
    NepaliMonthName("अप्रि", "अप्रिल"),
    NepaliMonthName("मे", "मे"),
    NepaliMonthName("जुन", "जुन"),
    NepaliMonthName("जुला", "जुलाई"),
    NepaliMonthName("अग", "अगस्ट"),
    NepaliMonthName("सेप्ट", "सेप्टेम्बर"),
    NepaliMonthName("अक्टो", "अक्टोबर"),
    NepaliMonthName("नोभे", "नोभेम्बर"),
    NepaliMonthName("डिसे", "डिसेम्बर")
)
