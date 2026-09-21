// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// Converters between the pigeon wire types and the shared engine's types.
// Enum wire values map by ordinal; both declaration orders are the contract.

import Foundation
import nepali_date_picker

extension LangDto {
  var toCore: NepaliDatePickerLang { self == .english ? .english : .nepali }
}

extension NameFormatDto {
  var toCore: NameFormat {
    switch self {
    case .full: return .full
    case .medium: return .medium
    case .short: return .short_
    }
  }
}

extension DateFormatStyleDto {
  var toCore: NepaliDateFormatStyle {
    switch self {
    case .full: return .full
    case .long: return .long_
    case .medium: return .medium
    case .shortMdy: return .shortMdy
    case .shortYmd: return .shortYmd
    case .compactMdy: return .compactMdy
    case .compactYmd: return .compactYmd
    }
  }
}

extension DigitScriptDto {
  var toCore: DigitScript { self == .latin ? .latin : .devanagari }
}

extension EventKindDto {
  var toCore: NepaliEventKind {
    switch self {
    case .governmentPublic: return .governmentpublic
    case .religious: return .religious
    case .regional: return .regional
    case .observance: return .observance
    }
  }
}

extension NepaliEventKind {
  var toDto: EventKindDto {
    switch self {
    case .governmentpublic: return .governmentPublic
    case .religious: return .religious
    case .regional: return .regional
    default: return .observance
    }
  }
}

extension DatePatternDto {
  var toCore: NepaliDateFormatter.Pattern {
    switch self {
    case .yyyySlashMmSlashDd: return .yyyySlashMmSlashDd
    case .yyyyDashMmDashDd: return .yyyyDashMmDashDd
    case .ddSlashMmSlashYyyy: return .ddSlashMmSlashYyyy
    case .ddDashMmDashYyyy: return .ddDashMmDashYyyy
    }
  }
}

extension LocaleDto {
  var toCore: NepaliDateLocale {
    NepaliDateLocale(
      language: language.toCore,
      dateFormat: dateFormat.toCore,
      weekDayName: weekDayName.toCore,
      monthName: monthName.toCore,
      digitScript: digitScript?.toCore
    )
  }
}

extension DateDto {
  var toCore: SimpleDate {
    SimpleDate(year: Int32(year), month: Int32(month), dayOfMonth: Int32(dayOfMonth))
  }
}

extension TimeDto {
  var toCore: SimpleTime {
    SimpleTime(
      hour: Int32(hour),
      minute: Int32(minute),
      second: Int32(second),
      nanosecond: Int32(nanosecond)
    )
  }
}

extension SimpleTime {
  var toDto: TimeDto {
    TimeDto(
      hour: Int64(hour),
      minute: Int64(minute),
      second: Int64(second),
      nanosecond: Int64(nanosecond)
    )
  }
}

extension SimpleDate {
  var toDateDto: DateDto {
    DateDto(year: Int64(year), month: Int64(month), dayOfMonth: Int64(dayOfMonth))
  }
}

extension CalendarDto {
  var toCore: CustomCalendar {
    CustomCalendar(
      year: Int32(year),
      month: Int32(month),
      dayOfMonth: Int32(dayOfMonth),
      era: Int32(era),
      firstDayOfMonth: Int32(firstDayOfMonth),
      lastDayOfMonth: Int32(lastDayOfMonth),
      totalDaysInMonth: Int32(totalDaysInMonth),
      dayOfWeekInMonth: Int32(dayOfWeekInMonth),
      dayOfWeek: Int32(dayOfWeek),
      dayOfYear: Int32(dayOfYear),
      weekOfMonth: Int32(weekOfMonth),
      weekOfYear: Int32(weekOfYear)
    )
  }
}

extension CustomCalendar {
  var toDto: CalendarDto {
    CalendarDto(
      year: Int64(year),
      month: Int64(month),
      dayOfMonth: Int64(dayOfMonth),
      era: Int64(era),
      firstDayOfMonth: Int64(firstDayOfMonth),
      lastDayOfMonth: Int64(lastDayOfMonth),
      totalDaysInMonth: Int64(totalDaysInMonth),
      dayOfWeekInMonth: Int64(dayOfWeekInMonth),
      dayOfWeek: Int64(dayOfWeek),
      dayOfYear: Int64(dayOfYear),
      weekOfMonth: Int64(weekOfMonth),
      weekOfYear: Int64(weekOfYear)
    )
  }
}

extension NepaliMonthCalendar {
  var toDto: MonthInfoDto {
    MonthInfoDto(
      year: Int64(year),
      month: Int64(month),
      totalDaysInMonth: Int64(totalDaysInMonth),
      firstDayOfMonth: Int64(firstDayOfMonth),
      lastDayOfMonth: Int64(lastDayOfMonth),
      daysFromStartOfWeekToFirstOfMonth: Int64(daysFromStartOfWeekToFirstOfMonth)
    )
  }
}

extension MonthCalendar {
  var toDto: MonthInfoDto {
    MonthInfoDto(
      year: Int64(year),
      month: Int64(month),
      totalDaysInMonth: Int64(totalDaysInMonth),
      firstDayOfMonth: Int64(firstDayOfMonth),
      lastDayOfMonth: Int64(lastDayOfMonth),
      daysFromStartOfWeekToFirstOfMonth: Int64(daysFromStartOfWeekToFirstOfMonth)
    )
  }
}

extension CustomDateTime {
  var toDto: DateTimeDto {
    DateTimeDto(calendar: customCalendar.toDto, time: simpleTime.toDto)
  }
}

extension KotlinIntRange {
  var toDto: YearRangeDto { YearRangeDto(first: Int64(first), last: Int64(last)) }
}

extension EventDto {
  var toCore: NepaliCalendarEvent {
    NepaliCalendarEvent(
      date: SimpleDate(year: Int32(year), month: Int32(month), dayOfMonth: Int32(dayOfMonth)),
      name: name,
      kind: kind.toCore,
      closesOffices: closesOffices,
      id: id,
      payload: payload
    )
  }
}

extension NepaliCalendarEvent {
  var toDto: EventDto {
    EventDto(
      year: Int64(date.year),
      month: Int64(date.month),
      dayOfMonth: Int64(date.dayOfMonth),
      name: name,
      kind: kind.toDto,
      closesOffices: closesOffices,
      id: id,
      payload: payload
    )
  }
}

extension NepaliDayStatus {
  var toDto: DayStatusDto {
    DayStatusDto(
      isWeeklyOff: isWeeklyOff,
      isNonWorking: isNonWorking,
      primaryKind: primaryKind?.toDto,
      names: names,
      events: events.map { $0.toDto },
      closures: closures.map { $0.toDto }
    )
  }
}

/// A provider over a fixed table; equal tables compare equal on the Kotlin
/// side, keeping remembered policies stable across identical calls.
final class TableEventProvider: NSObject, NepaliEventProvider {
  private let byYear: [Int32: Set<NepaliCalendarEvent>]

  init(events: [NepaliCalendarEvent]) {
    var table: [Int32: Set<NepaliCalendarEvent>] = [:]
    for event in events {
      table[event.date.year, default: []].insert(event)
    }
    byYear = table
    super.init()
  }

  func events(year: Int32) -> Set<NepaliCalendarEvent> { byYear[year] ?? [] }

  func closesOn(date: SimpleDate) -> Bool {
    events(year: date.year).contains { $0.date == date && $0.closesOffices }
  }
}

extension PolicyDto {
  var toCore: NepaliCalendarPolicy {
    NepaliCalendarPolicy(
      weeklyOffDays: Set(weeklyOffDays.map { KotlinInt(int: Int32($0)) }),
      provider: TableEventProvider(events: events.map { $0.toCore })
    )
  }
}

/// The engine's selectable rule this description composes, or nil when
/// every date is selectable.
func selectableFromDto(_ dto: SelectableDto?) -> NepaliSelectableDates? {
  guard let dto else { return nil }
  let converter = NepaliDateConverter.shared
  var selectable: NepaliSelectableDates?
  if let min = dto.minDate?.toCore, let max = dto.maxDate?.toCore {
    selectable = converter.DateRangeSelectable(
      minDate: min,
      maxDate: max,
      includeMinDate: dto.includeMinDate,
      includeMaxDate: dto.includeMaxDate
    )
  } else if let min = dto.minDate?.toCore {
    selectable = converter.AfterDateSelectable(simpleDate: min, includeDate: dto.includeMinDate)
  } else if let max = dto.maxDate?.toCore {
    selectable = converter.BeforeDateSelectable(simpleDate: max, includeDate: dto.includeMaxDate)
  }
  let weekend = dto.excludeWeekend.map { Set($0.map { KotlinInt(int: Int32($0)) }) }
  let closures = dto.excludeClosuresOf?.toCore
  if weekend != nil || closures != nil {
    var composed = selectable ?? NepaliDatePickerDefaults.shared.AllDates
    if let weekend {
      composed = EventHelpersKt.excludingWeekends(composed, weekend: weekend)
    }
    if let closures {
      composed = EventHelpersKt.excludingClosures(composed, provider: closures.provider)
    }
    selectable = composed
  }
  return selectable
}

extension EventOptionsDto {
  var toIos: NepaliEventOptions {
    let options = NepaliEventOptions()
    options.weeklyOffDays = weeklyOffDays.map { KotlinInt(int: Int32($0)) }
    options.events = events.map { mark in
      NepaliEventInfo(
        year: Int32(mark.year),
        month: Int32(mark.month),
        dayOfMonth: Int32(mark.dayOfMonth),
        name: mark.name,
        kind: mark.kind.toCore,
        closesOffices: mark.closesOffices,
        colorArgb: Int32(truncatingIfNeeded: mark.colorArgb),
        indicate: mark.indicate
      )
    }
    options.markWeeklyOff = markWeeklyOff
    options.markEvents = markEvents
    options.tintContainer = tintContainer
    options.indicateWeeklyOff = indicateWeeklyOff
    options.describeEvents = describeEvents
    options.weeklyOffColorArgb = Int32(truncatingIfNeeded: weeklyOffColorArgb)
    options.publicHolidayColorArgb = Int32(truncatingIfNeeded: publicHolidayColorArgb)
    options.religiousColorArgb = Int32(truncatingIfNeeded: religiousColorArgb)
    options.regionalColorArgb = Int32(truncatingIfNeeded: regionalColorArgb)
    options.observanceColorArgb = Int32(truncatingIfNeeded: observanceColorArgb)
    options.markedContainerColorArgb = Int32(truncatingIfNeeded: markedContainerColorArgb)
    return options
  }
}

func calendarSystemOfEra(_ era: Int64) -> CalendarSystem {
  CalendarSystem.companion.fromEra(era: Int32(era)) ?? .bikramSambat
}
