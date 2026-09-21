// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliTimeFormatter
import dev.shivathapaa.nepalidatepickerkmp.event.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.event.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import dev.shivathapaa.nepalidatepickerkmp.event.spanningThrough
import dev.shivathapaa.nepalidatepickerkmp.event.workingDaysBetween

/**
 * The engine host: every call is a synchronous delegation to the shared
 * `NepaliDateConverter` and its companions. Kotlin exceptions surface to
 * Dart as platform errors through pigeon's wrapper.
 */
internal class EngineApiImpl : EngineApi {

    override fun getBsYearRange(): YearRangeDto =
        NepaliCalendarDefaults.NepaliYearRange.toDto()

    override fun getAdYearRange(): YearRangeDto =
        NepaliCalendarDefaults.EnglishYearRange.toDto()

    override fun getAdYearRangeForBsYears(first: Long, last: Long): YearRangeDto =
        NepaliCalendarDefaults.gregorianYearRangeFor(first.toInt()..last.toInt()).toDto()

    override fun getTodayBs(): CalendarDto =
        NepaliDateConverter.todayNepaliCalendar.toDto()

    override fun getTodayAd(): CalendarDto =
        NepaliDateConverter.todayEnglishCalendar.toDto()

    override fun getCurrentTime(): TimeDto = NepaliDateConverter.currentTime.toDto()

    override fun convertAdToBs(year: Long, month: Long, dayOfMonth: Long): CalendarDto =
        NepaliDateConverter.convertEnglishToNepali(
            year.toInt(), month.toInt(), dayOfMonth.toInt()
        ).toDto()

    override fun convertBsToAd(year: Long, month: Long, dayOfMonth: Long): CalendarDto =
        NepaliDateConverter.convertNepaliToEnglish(
            year.toInt(), month.toInt(), dayOfMonth.toInt()
        ).toDto()

    override fun getBsCalendar(year: Long, month: Long, dayOfMonth: Long): CalendarDto =
        NepaliDateConverter.getNepaliCalendar(
            year.toInt(), month.toInt(), dayOfMonth.toInt()
        ).toDto()

    override fun getAdCalendar(year: Long, month: Long, dayOfMonth: Long): CalendarDto =
        NepaliDateConverter.getEnglishCalendar(
            year.toInt(), month.toInt(), dayOfMonth.toInt()
        ).toDto()

    override fun isAdDateConvertible(year: Long, month: Long, dayOfMonth: Long): Boolean =
        NepaliDateConverter.isEnglishDateConvertible(
            year.toInt(), month.toInt(), dayOfMonth.toInt()
        )

    override fun getBsMonth(year: Long, month: Long): MonthInfoDto =
        NepaliDateConverter.getNepaliMonthCalendar(year.toInt(), month.toInt()).toDto()

    override fun getAdMonth(year: Long, month: Long): MonthInfoDto =
        NepaliDateConverter.getEnglishMonthCalendar(year.toInt(), month.toInt()).toDto()

    override fun getBsCalendarsInAdMonth(year: Long, month: Long): List<CalendarDto?> =
        NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(year.toInt(), month.toInt())
            .map { it.nepaliCalendar?.toDto() }

    override fun getAdCalendarsInBsMonth(year: Long, month: Long): List<CalendarDto> =
        NepaliDateConverter.getEnglishCalendarsInNepaliMonth(year.toInt(), month.toInt())
            .map { it.toDto() }

    override fun getTotalDaysInBsMonth(year: Long, month: Long): Long =
        NepaliDateConverter.getTotalDaysInNepaliMonth(year.toInt(), month.toInt()).toLong()

    override fun getTotalDaysInAdMonth(year: Long, month: Long): Long =
        NepaliDateConverter.getTotalDaysInEnglishMonth(year.toInt(), month.toInt()).toLong()

    override fun addDaysToBsDate(
        year: Long,
        month: Long,
        dayOfMonth: Long,
        days: Long
    ): CalendarDto = NepaliDateConverter.getNepaliCalendarAfterAdditionOrSubtraction(
        year.toInt(), month.toInt(), dayOfMonth.toInt(), days.toInt()
    ).toDto()

    override fun getBsDaysBetween(start: DateDto, end: DateDto): Long =
        NepaliDateConverter.getNepaliDaysInBetween(start.toCore(), end.toCore()).toLong()

    override fun getAdDaysBetween(start: DateDto, end: DateDto): Long =
        NepaliDateConverter.getEnglishDaysInBetween(start.toCore(), end.toCore()).toLong()

    override fun compareBsDates(from: DateDto, to: DateDto): Long =
        NepaliDateConverter.compareDates(
            from.toCore(), to.year.toInt(), to.month.toInt(), to.dayOfMonth.toInt()
        ).toLong()

    override fun getWeekdayName(
        dayOfWeek: Long,
        format: NameFormatDto,
        language: LangDto
    ): String = NepaliDateConverter.getWeekdayName(
        dayOfWeek.toInt(), format.toCore(), language.toCore()
    )

    override fun getBsMonthName(month: Long, format: NameFormatDto, language: LangDto): String =
        NepaliDateConverter.getMonthName(month.toInt(), format.toCore(), language.toCore())

    override fun getAdMonthName(month: Long, format: NameFormatDto, language: LangDto): String =
        NepaliDateConverter.getEnglishMonthName(month.toInt(), format.toCore(), language.toCore())

    override fun formatBsDate(calendar: CalendarDto, locale: LocaleDto): String =
        NepaliDateConverter.formatNepaliDate(calendar.toCore(), locale.toCore())

    override fun formatAdDate(calendar: CalendarDto, locale: LocaleDto): String =
        NepaliDateConverter.formatEnglishDate(calendar.toCore(), locale.toCore())

    override fun formatBsDateByPattern(pattern: String, date: DateDto, language: LangDto): String =
        NepaliDateConverter.formatNepaliDateByUnicodePattern(
            pattern,
            NepaliDateConverter.getNepaliCalendar(
                date.year.toInt(), date.month.toInt(), date.dayOfMonth.toInt()
            ),
            language.toCore()
        )

    override fun formatAdDateByPattern(pattern: String, date: DateDto, language: LangDto): String =
        NepaliDateConverter.formatEnglishDateByUnicodePattern(
            pattern,
            NepaliDateConverter.getEnglishCalendar(
                date.year.toInt(), date.month.toInt(), date.dayOfMonth.toInt()
            ),
            language.toCore()
        )

    override fun formatTimeByPattern(pattern: String, time: TimeDto, language: LangDto): String =
        NepaliDateConverter.formatTimeByUnicodePattern(pattern, time.toCore(), language.toCore())

    override fun formatBsDateTimeByPattern(
        pattern: String,
        date: DateDto,
        time: TimeDto?,
        language: LangDto
    ): String = NepaliDateConverter.formatNepaliDateTimeByUnicodePattern(
        pattern,
        NepaliDateConverter.getNepaliCalendar(
            date.year.toInt(), date.month.toInt(), date.dayOfMonth.toInt()
        ),
        time?.toCore(),
        language.toCore()
    )

    override fun formatAdDateTimeByPattern(
        pattern: String,
        date: DateDto,
        time: TimeDto?,
        language: LangDto
    ): String = NepaliDateConverter.formatEnglishDateTimeByUnicodePattern(
        pattern,
        NepaliDateConverter.getEnglishCalendar(
            date.year.toInt(), date.month.toInt(), date.dayOfMonth.toInt()
        ),
        time?.toCore(),
        language.toCore()
    )

    override fun formatTimeEnglish(time: TimeDto, use12HourFormat: Boolean): String =
        NepaliDateConverter.getFormattedTimeInEnglish(time.toCore(), use12HourFormat)

    override fun formatTimeNepali(time: TimeDto, use12HourFormat: Boolean): String =
        NepaliDateConverter.getFormattedTimeInNepali(time.toCore(), use12HourFormat)

    override fun bsDateTimeToIso(date: DateDto, time: TimeDto): String =
        NepaliDateConverter.formatNepaliDateTimeToIsoFormat(date.toCore(), time.toCore())

    override fun adDateTimeToIso(date: DateDto, time: TimeDto): String =
        NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(date.toCore(), time.toCore())

    override fun bsDateTimeFromIso(isoDateTime: String): DateTimeDto =
        NepaliDateConverter.getNepaliDateTimeFromIsoFormat(isoDateTime).toDto()

    override fun adDateTimeFromIso(isoDateTime: String): DateTimeDto =
        NepaliDateConverter.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime).toDto()

    override fun localizeDigits(text: String, script: DigitScriptDto): String =
        with(NepaliDateConverter) { text.localizeDigits(script.toCore()) }

    override fun toLatinDigits(text: String): String =
        with(NepaliDateConverter) { text.toLatinDigits() }

    override fun wireFormatDate(
        date: DateDto,
        pattern: DatePatternDto,
        script: DigitScriptDto
    ): String = NepaliDateFormatter.format(date.toCore(), pattern.toCore(), script.toCore())

    override fun wireParseDate(input: String, pattern: DatePatternDto): DateDto? =
        NepaliDateFormatter.parse(input, pattern.toCore())?.let {
            DateDto(
                year = it.year.toLong(),
                month = it.month.toLong(),
                dayOfMonth = it.dayOfMonth.toLong()
            )
        }

    override fun wireFormatTime(time: TimeDto): String =
        NepaliTimeFormatter.format(time.toCore())

    override fun wireParseTime(input: String): TimeDto? =
        NepaliTimeFormatter.parse(input)?.toDto()

    override fun statusOf(policy: PolicyDto, date: DateDto): DayStatusDto =
        policy.toCore().statusOf(date.toCore()).toDto()

    override fun monthStatus(policy: PolicyDto, year: Long, month: Long): List<DayStatusDto> =
        policy.toCore().monthStatus(year.toInt(), month.toInt()).map { it.toDto() }

    override fun eventsOn(policy: PolicyDto, date: DateDto): List<EventDto> =
        policy.toCore().eventsOn(date.toCore()).map { it.toDto() }

    override fun eventsIn(policy: PolicyDto, year: Long, month: Long): List<EventDto> =
        policy.toCore().eventsIn(year.toInt(), month.toInt()).map { it.toDto() }

    override fun isNonWorkingDay(policy: PolicyDto, date: DateDto): Boolean =
        policy.toCore().isNonWorkingDay(date.toCore())

    override fun workingDaysBetween(policy: PolicyDto, start: DateDto, end: DateDto): Long =
        NepaliDateConverter.workingDaysBetween(
            start.toCore(), end.toCore(), policy.toCore()
        ).toLong()

    override fun nextWorkingDay(policy: PolicyDto, from: DateDto): CalendarDto =
        NepaliDateConverter.nextWorkingDay(from.toCore(), policy.toCore())
            .let { NepaliDateConverter.getNepaliCalendar(it.year, it.month, it.dayOfMonth) }
            .toDto()

    override fun addWorkingDays(policy: PolicyDto, from: DateDto, days: Long): CalendarDto =
        NepaliDateConverter.addWorkingDays(from.toCore(), days.toInt(), policy.toCore())
            .let { NepaliDateConverter.getNepaliCalendar(it.year, it.month, it.dayOfMonth) }
            .toDto()

    override fun eventSpanningDays(event: EventDto, days: Long): List<EventDto> =
        event.toCore().spanningDays(days.toInt()).map { it.toDto() }

    override fun eventSpanningThrough(event: EventDto, end: DateDto): List<EventDto> =
        event.toCore().spanningThrough(end.toCore()).map { it.toDto() }
}
