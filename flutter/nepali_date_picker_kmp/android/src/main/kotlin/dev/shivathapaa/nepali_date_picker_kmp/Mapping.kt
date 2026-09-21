// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.CustomDateTime
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleTime
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.excludingClosures
import dev.shivathapaa.nepalidatepickerkmp.event.excludingWeekends

// Converters between the pigeon wire types and the shared engine's types.
// Enum wire values map by ordinal; both declaration orders are the contract.

internal fun LangDto.toCore(): NepaliDatePickerLang = NepaliDatePickerLang.entries[ordinal]

internal fun NameFormatDto.toCore(): NameFormat = NameFormat.entries[ordinal]

internal fun DateFormatStyleDto.toCore(): NepaliDateFormatStyle =
    NepaliDateFormatStyle.entries[ordinal]

internal fun DigitScriptDto.toCore(): DigitScript = DigitScript.entries[ordinal]

internal fun EventKindDto.toCore(): NepaliEventKind = NepaliEventKind.entries[ordinal]

internal fun NepaliEventKind.toDto(): EventKindDto = EventKindDto.entries[ordinal]

internal fun DatePatternDto.toCore(): NepaliDateFormatter.Pattern =
    NepaliDateFormatter.Pattern.entries[ordinal]

internal fun LocaleDto.toCore(): NepaliDateLocale = NepaliDateLocale(
    language = language.toCore(),
    dateFormat = dateFormat.toCore(),
    weekDayName = weekDayName.toCore(),
    monthName = monthName.toCore(),
    digitScript = digitScript?.toCore()
)

internal fun DateDto.toCore(): SimpleDate =
    SimpleDate(year.toInt(), month.toInt(), dayOfMonth.toInt())

internal fun TimeDto.toCore(): SimpleTime =
    SimpleTime(hour.toInt(), minute.toInt(), second.toInt(), nanosecond.toInt())

internal fun SimpleTime.toDto(): TimeDto = TimeDto(
    hour = hour.toLong(),
    minute = minute.toLong(),
    second = second.toLong(),
    nanosecond = nanosecond.toLong()
)

internal fun CalendarDto.toCore(): CustomCalendar = CustomCalendar(
    year = year.toInt(),
    month = month.toInt(),
    dayOfMonth = dayOfMonth.toInt(),
    era = era.toInt(),
    firstDayOfMonth = firstDayOfMonth.toInt(),
    lastDayOfMonth = lastDayOfMonth.toInt(),
    totalDaysInMonth = totalDaysInMonth.toInt(),
    dayOfWeekInMonth = dayOfWeekInMonth.toInt(),
    dayOfWeek = dayOfWeek.toInt(),
    dayOfYear = dayOfYear.toInt(),
    weekOfMonth = weekOfMonth.toInt(),
    weekOfYear = weekOfYear.toInt()
)

internal fun CustomCalendar.toDto(): CalendarDto = CalendarDto(
    year = year.toLong(),
    month = month.toLong(),
    dayOfMonth = dayOfMonth.toLong(),
    era = era.toLong(),
    firstDayOfMonth = firstDayOfMonth.toLong(),
    lastDayOfMonth = lastDayOfMonth.toLong(),
    totalDaysInMonth = totalDaysInMonth.toLong(),
    dayOfWeekInMonth = dayOfWeekInMonth.toLong(),
    dayOfWeek = dayOfWeek.toLong(),
    dayOfYear = dayOfYear.toLong(),
    weekOfMonth = weekOfMonth.toLong(),
    weekOfYear = weekOfYear.toLong()
)

internal fun NepaliMonthCalendar.toDto(): MonthInfoDto = MonthInfoDto(
    year = year.toLong(),
    month = month.toLong(),
    totalDaysInMonth = totalDaysInMonth.toLong(),
    firstDayOfMonth = firstDayOfMonth.toLong(),
    lastDayOfMonth = lastDayOfMonth.toLong(),
    daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth.toLong()
)

internal fun MonthCalendar.toDto(): MonthInfoDto = MonthInfoDto(
    year = year.toLong(),
    month = month.toLong(),
    totalDaysInMonth = totalDaysInMonth.toLong(),
    firstDayOfMonth = firstDayOfMonth.toLong(),
    lastDayOfMonth = lastDayOfMonth.toLong(),
    daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth.toLong()
)

internal fun CustomDateTime.toDto(): DateTimeDto =
    DateTimeDto(calendar = customCalendar.toDto(), time = simpleTime.toDto())

internal fun IntRange.toDto(): YearRangeDto =
    YearRangeDto(first = first.toLong(), last = last.toLong())

internal fun EventDto.toCore(): NepaliCalendarEvent = NepaliCalendarEvent(
    date = SimpleDate(year.toInt(), month.toInt(), dayOfMonth.toInt()),
    name = name,
    kind = kind.toCore(),
    closesOffices = closesOffices,
    id = id,
    payload = payload
)

internal fun NepaliCalendarEvent.toDto(): EventDto = EventDto(
    year = date.year.toLong(),
    month = date.month.toLong(),
    dayOfMonth = date.dayOfMonth.toLong(),
    name = name,
    kind = kind.toDto(),
    closesOffices = closesOffices,
    id = id,
    payload = payload
)

internal fun NepaliDayStatus.toDto(): DayStatusDto = DayStatusDto(
    isWeeklyOff = isWeeklyOff,
    isNonWorking = isNonWorking,
    primaryKind = primaryKind?.toDto(),
    names = names,
    events = events.map { it.toDto() },
    closures = closures.map { it.toDto() }
)

/**
 * A provider over a fixed table, equal to another one built from the same
 * events, so remembered policies stay stable across identical calls.
 */
internal data class TableEventProvider(
    private val byYear: Map<Int, Set<NepaliCalendarEvent>>
) : NepaliEventProvider {
    override fun events(year: Int): Set<NepaliCalendarEvent> = byYear[year].orEmpty()
}

internal fun PolicyDto.toCore(): NepaliCalendarPolicy = NepaliCalendarPolicy(
    weeklyOffDays = weeklyOffDays.map { it.toInt() }.toSet(),
    provider = TableEventProvider(
        events.map { it.toCore() }
            .groupBy { it.date.year }
            .mapValues { (_, list) -> list.toSet() }
    )
)

/**
 * The engine's selectable rule this description composes, or null when every
 * date is selectable.
 */
internal fun SelectableDto?.toCore(): NepaliSelectableDates? {
    if (this == null) return null
    val min = minDate?.toCore()
    val max = maxDate?.toCore()
    var selectable: NepaliSelectableDates? = when {
        min != null && max != null -> NepaliDateConverter.DateRangeSelectable(
            minDate = min,
            maxDate = max,
            includeMinDate = includeMinDate,
            includeMaxDate = includeMaxDate
        )
        min != null -> NepaliDateConverter.AfterDateSelectable(min, includeMinDate)
        max != null -> NepaliDateConverter.BeforeDateSelectable(max, includeMaxDate)
        else -> null
    }
    val weekend = excludeWeekend?.map { it.toInt() }?.toSet()
    val closures = excludeClosuresOf?.toCore()
    if (weekend != null || closures != null) {
        var composed = selectable ?: AllDatesSelectable
        if (weekend != null) composed = composed.excludingWeekends(weekend)
        if (closures != null) composed = composed.excludingClosures(closures.provider)
        selectable = composed
    }
    return selectable
}

/** Every date selectable, the interface's own defaults. */
internal object AllDatesSelectable : NepaliSelectableDates
