// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliEventInfo
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliEventOptions
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind

/**
 * The creation-parameter map one embedded picker arrives with, decoded into
 * engine types. The map shape is produced by the Dart side's
 * `buildPickerCreationParams`; absent keys fall back to the same defaults
 * the View factories use.
 */
internal class PickerParams(private val map: Map<*, *>) {

    val variant: String = map["variant"] as? String ?: "datePicker"

    val initialDate: SimpleDate? = date(map["initialDate"])
    val initialEndDate: SimpleDate? = date(map["initialEndDate"])

    val locale: NepaliDateLocale = (map["locale"] as? List<*>)?.let { entry ->
        NepaliDateLocale(
            language = NepaliDatePickerLang.entries[entry.int(0)],
            dateFormat = NepaliDateFormatStyle.entries[entry.int(1)],
            weekDayName = dev.shivathapaa.nepalidatepickerkmp.data.NameFormat.entries[entry.int(2)],
            monthName = dev.shivathapaa.nepalidatepickerkmp.data.NameFormat.entries[entry.int(3)],
            digitScript = entry.int(4).takeIf { it >= 0 }
                ?.let { dev.shivathapaa.nepalidatepickerkmp.data.DigitScript.entries[it] }
        )
    } ?: NepaliDateLocale()

    val englishLocale: NepaliDateLocale? = (map["englishLocale"] as? List<*>)?.let { entry ->
        NepaliDateLocale(
            language = NepaliDatePickerLang.entries[entry.int(0)],
            dateFormat = NepaliDateFormatStyle.entries[entry.int(1)],
            weekDayName = dev.shivathapaa.nepalidatepickerkmp.data.NameFormat.entries[entry.int(2)],
            monthName = dev.shivathapaa.nepalidatepickerkmp.data.NameFormat.entries[entry.int(3)],
            digitScript = entry.int(4).takeIf { it >= 0 }
                ?.let { dev.shivathapaa.nepalidatepickerkmp.data.DigitScript.entries[it] }
        )
    }

    val yearRange: IntRange = run {
        val start = (map["yearRangeStart"] as? Number)?.toInt()
        val end = (map["yearRangeEnd"] as? Number)?.toInt()
        if (start != null && end != null) start..end else NepaliCalendarDefaults.NepaliYearRange
    }

    val selectableDates: NepaliSelectableDates? = (map["selectable"] as? Map<*, *>)?.let { entry ->
        SelectableDto(
            minDate = date(entry["minDate"])?.toDto(),
            maxDate = date(entry["maxDate"])?.toDto(),
            includeMinDate = entry["includeMinDate"] as? Boolean ?: false,
            includeMaxDate = entry["includeMaxDate"] as? Boolean ?: false,
            excludeWeekend = (entry["excludeWeekend"] as? List<*>)?.map { (it as Number).toLong() },
            excludeClosuresOf = policy(
                entry["policyWeeklyOffDays"] as? List<*>,
                entry["policyEvents"] as? List<*>
            )
        ).toCore()
    }

    val events: NepaliEventOptions? = (map["events"] as? Map<*, *>)?.let { entry ->
        NepaliEventOptions(
            weeklyOffDays = (entry["weeklyOffDays"] as? List<*>)
                ?.map { (it as Number).toInt() } ?: listOf(7),
            events = (entry["events"] as? List<*>)?.map { row ->
                val fields = row as List<*>
                NepaliEventInfo(
                    year = fields.int(0),
                    month = fields.int(1),
                    dayOfMonth = fields.int(2),
                    name = fields[3] as String,
                    kind = NepaliEventKind.entries[fields.int(4)],
                    closesOffices = fields[5] as Boolean,
                    colorArgb = fields.int(6),
                    indicate = fields[7] as Boolean,
                    id = fields.getOrNull(8) as? String,
                    payload = fields.getOrNull(9) as? String
                )
            } ?: emptyList(),
            markWeeklyOff = entry["markWeeklyOff"] as? Boolean ?: true,
            markEvents = entry["markEvents"] as? Boolean ?: true,
            tintContainer = entry["tintContainer"] as? Boolean ?: false,
            indicateWeeklyOff = entry["indicateWeeklyOff"] as? Boolean ?: false,
            describeEvents = entry["describeEvents"] as? Boolean ?: true,
            weeklyOffColorArgb = (entry["weeklyOffColorArgb"] as? Number)?.toInt() ?: 0,
            publicHolidayColorArgb = (entry["publicHolidayColorArgb"] as? Number)?.toInt() ?: 0,
            religiousColorArgb = (entry["religiousColorArgb"] as? Number)?.toInt() ?: 0,
            regionalColorArgb = (entry["regionalColorArgb"] as? Number)?.toInt() ?: 0,
            observanceColorArgb = (entry["observanceColorArgb"] as? Number)?.toInt() ?: 0,
            markedContainerColorArgb = (entry["markedContainerColorArgb"] as? Number)?.toInt() ?: 0
        )
    }

    val calendarSystem: CalendarSystem =
        CalendarSystem.fromEra(int("initialCalendarSystemEra", 2))
            ?: CalendarSystem.BIKRAM_SAMBAT

    val language: NepaliDatePickerLang =
        NepaliDatePickerLang.entries[int("language", 0)]

    val dateFormat: NepaliDateFormatter.Pattern =
        NepaliDateFormatter.Pattern.entries[int("dateFormat", 0)]

    val dateFormatStyle: NepaliDateFormatStyle =
        NepaliDateFormatStyle.entries[int("dateFormatStyle", NepaliDateFormatStyle.MEDIUM.ordinal)]

    fun bool(key: String, fallback: Boolean): Boolean = map[key] as? Boolean ?: fallback

    fun int(key: String, fallback: Int): Int = (map[key] as? Number)?.toInt() ?: fallback

    fun float(key: String, fallback: Float): Float = (map[key] as? Number)?.toFloat() ?: fallback

    fun string(key: String): String? = map[key] as? String

    private fun date(value: Any?): SimpleDate? = (value as? List<*>)?.let {
        SimpleDate(it.int(0), it.int(1), it.int(2))
    }

    private fun policy(weeklyOffDays: List<*>?, events: List<*>?): PolicyDto? {
        if (weeklyOffDays == null && events == null) return null
        return PolicyDto(
            weeklyOffDays = weeklyOffDays?.map { (it as Number).toLong() } ?: emptyList(),
            events = events?.map { row ->
                val fields = row as List<*>
                EventDto(
                    year = fields.int(0).toLong(),
                    month = fields.int(1).toLong(),
                    dayOfMonth = fields.int(2).toLong(),
                    name = fields[3] as String,
                    kind = EventKindDto.entries[fields.int(4)],
                    closesOffices = fields[5] as Boolean,
                    id = null,
                    payload = null
                )
            } ?: emptyList()
        )
    }
}

private fun List<*>.int(index: Int): Int = (this[index] as Number).toInt()

private fun SimpleDate.toDto(): DateDto =
    DateDto(year = year.toLong(), month = month.toLong(), dayOfMonth = dayOfMonth.toLong())
