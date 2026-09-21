// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import Foundation
import nepali_date_picker

/// The engine host: every call is a synchronous delegation to the shared
/// `NepaliDateConverter` and its companions inside the Kotlin framework.
///
/// Kotlin/Native exceptions cannot cross into Swift, so the Dart layer
/// validates inputs before calling; everything reaching here is in range.
final class EngineApiImpl: EngineApi {

  private let converter = NepaliDateConverter.shared
  private let defaults = NepaliCalendarDefaults.shared

  func getBsYearRange() throws -> YearRangeDto { defaults.NepaliYearRange.toDto }

  func getAdYearRange() throws -> YearRangeDto { defaults.EnglishYearRange.toDto }

  func getAdYearRangeForBsYears(first: Int64, last: Int64) throws -> YearRangeDto {
    defaults.gregorianYearRangeFor(
      nepaliYearRange: KotlinIntRange(start: Int32(first), endInclusive: Int32(last))
    ).toDto
  }

  func getTodayBs() throws -> CalendarDto { converter.todayNepaliCalendar.toDto }

  func getTodayAd() throws -> CalendarDto { converter.todayEnglishCalendar.toDto }

  func getCurrentTime() throws -> TimeDto { converter.currentTime.toDto }

  func convertAdToBs(year: Int64, month: Int64, dayOfMonth: Int64) throws -> CalendarDto {
    converter.convertEnglishToNepali(
      englishYYYY: Int32(year), englishMM: Int32(month), englishDD: Int32(dayOfMonth)
    ).toDto
  }

  func convertBsToAd(year: Int64, month: Int64, dayOfMonth: Int64) throws -> CalendarDto {
    converter.convertNepaliToEnglish(
      nepaliYYYY: Int32(year), nepaliMM: Int32(month), nepaliDD: Int32(dayOfMonth)
    ).toDto
  }

  func getBsCalendar(year: Int64, month: Int64, dayOfMonth: Int64) throws -> CalendarDto {
    converter.getNepaliCalendar(
      nepaliYYYY: Int32(year), nepaliMM: Int32(month), nepaliDD: Int32(dayOfMonth)
    ).toDto
  }

  func getAdCalendar(year: Int64, month: Int64, dayOfMonth: Int64) throws -> CalendarDto {
    converter.getEnglishCalendar(
      englishYYYY: Int32(year), englishMM: Int32(month), englishDD: Int32(dayOfMonth)
    ).toDto
  }

  func isAdDateConvertible(year: Int64, month: Int64, dayOfMonth: Int64) throws -> Bool {
    converter.isEnglishDateConvertible(
      englishYYYY: Int32(year), englishMM: Int32(month), englishDD: Int32(dayOfMonth)
    )
  }

  func getBsMonth(year: Int64, month: Int64) throws -> MonthInfoDto {
    converter.getNepaliMonthCalendar(nepaliYear: Int32(year), nepaliMonth: Int32(month)).toDto
  }

  func getAdMonth(year: Int64, month: Int64) throws -> MonthInfoDto {
    converter.getEnglishMonthCalendar(englishYear: Int32(year), englishMonth: Int32(month)).toDto
  }

  func getBsCalendarsInAdMonth(year: Int64, month: Int64) throws -> [CalendarDto?] {
    converter.getNepaliCalendarsInEnglishMonthByDay(
      englishYear: Int32(year), englishMonth: Int32(month)
    ).map { $0.nepaliCalendar?.toDto }
  }

  func getAdCalendarsInBsMonth(year: Int64, month: Int64) throws -> [CalendarDto] {
    converter.getEnglishCalendarsInNepaliMonth(
      nepaliYear: Int32(year), nepaliMonth: Int32(month)
    ).map { $0.toDto }
  }

  func getTotalDaysInBsMonth(year: Int64, month: Int64) throws -> Int64 {
    Int64(converter.getTotalDaysInNepaliMonth(year: Int32(year), month: Int32(month)))
  }

  func getTotalDaysInAdMonth(year: Int64, month: Int64) throws -> Int64 {
    Int64(converter.getTotalDaysInEnglishMonth(year: Int32(year), month: Int32(month)))
  }

  func addDaysToBsDate(year: Int64, month: Int64, dayOfMonth: Int64, days: Int64) throws
    -> CalendarDto
  {
    converter.getNepaliCalendarAfterAdditionOrSubtraction(
      year: Int32(year),
      month: Int32(month),
      dayOfMonth: Int32(dayOfMonth),
      daysToAdjust: Int32(days)
    ).toDto
  }

  func getBsDaysBetween(start: DateDto, end: DateDto) throws -> Int64 {
    Int64(converter.getNepaliDaysInBetween(startDate: start.toCore, endDate: end.toCore))
  }

  func getAdDaysBetween(start: DateDto, end: DateDto) throws -> Int64 {
    Int64(converter.getEnglishDaysInBetween(startDate: start.toCore, endDate: end.toCore))
  }

  func compareBsDates(from: DateDto, to: DateDto) throws -> Int64 {
    Int64(
      converter.compareDates(
        simpleDate: from.toCore,
        year: Int32(to.year),
        month: Int32(to.month),
        dayOfMonth: Int32(to.dayOfMonth)
      ))
  }

  func getWeekdayName(dayOfWeek: Int64, format: NameFormatDto, language: LangDto) throws
    -> String
  {
    converter.getWeekdayName(
      dayOfWeek: Int32(dayOfWeek), format: format.toCore, language: language.toCore)
  }

  func getBsMonthName(month: Int64, format: NameFormatDto, language: LangDto) throws -> String {
    converter.getMonthName(month: Int32(month), format: format.toCore, language: language.toCore)
  }

  func getAdMonthName(month: Int64, format: NameFormatDto, language: LangDto) throws -> String {
    converter.getEnglishMonthName(
      month: Int32(month), format: format.toCore, language: language.toCore)
  }

  func formatBsDate(calendar: CalendarDto, locale: LocaleDto) throws -> String {
    converter.formatNepaliDate(customCalendar: calendar.toCore, locale: locale.toCore)
  }

  func formatAdDate(calendar: CalendarDto, locale: LocaleDto) throws -> String {
    converter.formatEnglishDate(customCalendar: calendar.toCore, locale: locale.toCore)
  }

  func formatBsDateByPattern(pattern: String, date: DateDto, language: LangDto) throws -> String {
    converter.formatNepaliDateByUnicodePattern(
      unicodePattern: pattern,
      calendar: converter.getNepaliCalendar(
        nepaliYYYY: Int32(date.year), nepaliMM: Int32(date.month), nepaliDD: Int32(date.dayOfMonth)
      ),
      language: language.toCore
    )
  }

  func formatAdDateByPattern(pattern: String, date: DateDto, language: LangDto) throws -> String {
    converter.formatEnglishDateByUnicodePattern(
      unicodePattern: pattern,
      calendar: converter.getEnglishCalendar(
        englishYYYY: Int32(date.year), englishMM: Int32(date.month), englishDD: Int32(date.dayOfMonth)
      ),
      language: language.toCore
    )
  }

  func formatTimeByPattern(pattern: String, time: TimeDto, language: LangDto) throws -> String {
    converter.formatTimeByUnicodePattern(
      unicodePattern: pattern, time: time.toCore, language: language.toCore)
  }

  func formatBsDateTimeByPattern(
    pattern: String, date: DateDto, time: TimeDto?, language: LangDto
  ) throws -> String {
    converter.formatNepaliDateTimeByUnicodePattern(
      unicodePattern: pattern,
      calendar: converter.getNepaliCalendar(
        nepaliYYYY: Int32(date.year), nepaliMM: Int32(date.month), nepaliDD: Int32(date.dayOfMonth)
      ),
      time: time?.toCore,
      language: language.toCore
    )
  }

  func formatAdDateTimeByPattern(
    pattern: String, date: DateDto, time: TimeDto?, language: LangDto
  ) throws -> String {
    converter.formatEnglishDateTimeByUnicodePattern(
      unicodePattern: pattern,
      calendar: converter.getEnglishCalendar(
        englishYYYY: Int32(date.year), englishMM: Int32(date.month), englishDD: Int32(date.dayOfMonth)
      ),
      time: time?.toCore,
      language: language.toCore
    )
  }

  func formatTimeEnglish(time: TimeDto, use12HourFormat: Bool) throws -> String {
    converter.getFormattedTimeInEnglish(simpleTime: time.toCore, use12HourFormat: use12HourFormat)
  }

  func formatTimeNepali(time: TimeDto, use12HourFormat: Bool) throws -> String {
    converter.getFormattedTimeInNepali(simpleTime: time.toCore, use12HourFormat: use12HourFormat)
  }

  func bsDateTimeToIso(date: DateDto, time: TimeDto) throws -> String {
    converter.formatNepaliDateTimeToIsoFormat(nepaliDate: date.toCore, time: time.toCore)
  }

  func adDateTimeToIso(date: DateDto, time: TimeDto) throws -> String {
    converter.formatEnglishDateNepaliTimeToIsoFormat(englishDate: date.toCore, time: time.toCore)
  }

  func bsDateTimeFromIso(isoDateTime: String) throws -> DateTimeDto {
    converter.getNepaliDateTimeFromIsoFormat(isoDateTime: isoDateTime).toDto
  }

  func adDateTimeFromIso(isoDateTime: String) throws -> DateTimeDto {
    converter.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime: isoDateTime).toDto
  }

  func localizeDigits(text: String, script: DigitScriptDto) throws -> String {
    converter.localizeDigits(text, script: script.toCore)
  }

  func toLatinDigits(text: String) throws -> String {
    converter.toLatinDigits(text)
  }

  func wireFormatDate(date: DateDto, pattern: DatePatternDto, script: DigitScriptDto) throws
    -> String
  {
    NepaliDateFormatter.shared.format(
      date: date.toCore, pattern: pattern.toCore, script: script.toCore)
  }

  func wireParseDate(input: String, pattern: DatePatternDto) throws -> DateDto? {
    NepaliDateFormatter.shared.parse(input: input, pattern: pattern.toCore)?.toDateDto
  }

  func wireFormatTime(time: TimeDto) throws -> String {
    NepaliTimeFormatter.shared.format(time: time.toCore)
  }

  func wireParseTime(input: String) throws -> TimeDto? {
    NepaliTimeFormatter.shared.parse(input: input)?.toDto
  }

  func statusOf(policy: PolicyDto, date: DateDto) throws -> DayStatusDto {
    policy.toCore.statusOf(date: date.toCore).toDto
  }

  func monthStatus(policy: PolicyDto, year: Int64, month: Int64) throws -> [DayStatusDto] {
    policy.toCore.monthStatus(year: Int32(year), month: Int32(month)).map { $0.toDto }
  }

  func eventsOn(policy: PolicyDto, date: DateDto) throws -> [EventDto] {
    policy.toCore.eventsOn(date: date.toCore).map { $0.toDto }
  }

  func eventsIn(policy: PolicyDto, year: Int64, month: Int64) throws -> [EventDto] {
    policy.toCore.eventsIn(year: Int32(year), month: Int32(month)).map { $0.toDto }
  }

  func isNonWorkingDay(policy: PolicyDto, date: DateDto) throws -> Bool {
    policy.toCore.isNonWorkingDay(date: date.toCore)
  }

  func workingDaysBetween(policy: PolicyDto, start: DateDto, end: DateDto) throws -> Int64 {
    Int64(
      converter.workingDaysBetween(start: start.toCore, end: end.toCore, policy: policy.toCore))
  }

  func nextWorkingDay(policy: PolicyDto, from: DateDto) throws -> CalendarDto {
    let day = converter.nextWorkingDay(from: from.toCore, policy: policy.toCore)
    return converter.getNepaliCalendar(
      nepaliYYYY: day.year, nepaliMM: day.month, nepaliDD: day.dayOfMonth
    ).toDto
  }

  func addWorkingDays(policy: PolicyDto, from: DateDto, days: Int64) throws -> CalendarDto {
    let day = converter.addWorkingDays(
      from: from.toCore, days: Int32(days), policy: policy.toCore)
    return converter.getNepaliCalendar(
      nepaliYYYY: day.year, nepaliMM: day.month, nepaliDD: day.dayOfMonth
    ).toDto
  }

  func eventSpanningDays(event: EventDto, days: Int64) throws -> [EventDto] {
    event.toCore.spanningDays(days: Int32(days)).map { $0.toDto }
  }

  func eventSpanningThrough(event: EventDto, end: DateDto) throws -> [EventDto] {
    event.toCore.spanningThrough(end: end.toCore).map { $0.toDto }
  }
}
