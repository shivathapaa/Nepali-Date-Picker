// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// Internal converters between the public models and the pigeon wire types.
// Every enum here is declared in the same order on both sides, so index
// mapping is the contract.

import 'messages.g.dart';
import 'model/events.dart';
import 'model/locale.dart';
import 'model/nepali_date.dart';
import 'model/simple_types.dart';

LangDto langToDto(NepaliLanguage language) => LangDto.values[language.index];

NameFormatDto nameFormatToDto(NameFormat format) =>
    NameFormatDto.values[format.index];

DateFormatStyleDto dateFormatStyleToDto(NepaliDateFormatStyle style) =>
    DateFormatStyleDto.values[style.index];

DigitScriptDto digitScriptToDto(DigitScript script) =>
    DigitScriptDto.values[script.index];

EventKindDto eventKindToDto(NepaliEventKind kind) =>
    EventKindDto.values[kind.index];

NepaliEventKind eventKindFromDto(EventKindDto kind) =>
    NepaliEventKind.values[kind.index];

LocaleDto localeToDto(NepaliDateLocale locale) => LocaleDto(
      language: langToDto(locale.language),
      dateFormat: dateFormatStyleToDto(locale.dateFormat),
      weekDayName: nameFormatToDto(locale.weekDayName),
      monthName: nameFormatToDto(locale.monthName),
      digitScript:
          locale.digitScript == null ? null : digitScriptToDto(locale.digitScript!),
    );

DateDto dateToDto(SimpleDate date) => DateDto(
      year: date.year,
      month: date.month,
      dayOfMonth: date.dayOfMonth,
    );

SimpleDate dateFromDto(DateDto dto) =>
    SimpleDate(dto.year, dto.month, dto.dayOfMonth);

TimeDto timeToDto(SimpleTime time) => TimeDto(
      hour: time.hour,
      minute: time.minute,
      second: time.second,
      nanosecond: time.nanosecond,
    );

SimpleTime timeFromDto(TimeDto dto) => SimpleTime(
      hour: dto.hour,
      minute: dto.minute,
      second: dto.second,
      nanosecond: dto.nanosecond,
    );

CalendarDto calendarToDto(NepaliDate date) => CalendarDto(
      year: date.year,
      month: date.month,
      dayOfMonth: date.dayOfMonth,
      era: date.era,
      firstDayOfMonth: date.firstDayOfMonth,
      lastDayOfMonth: date.lastDayOfMonth,
      totalDaysInMonth: date.totalDaysInMonth,
      dayOfWeekInMonth: date.dayOfWeekInMonth,
      dayOfWeek: date.dayOfWeek,
      dayOfYear: date.dayOfYear,
      weekOfMonth: date.weekOfMonth,
      weekOfYear: date.weekOfYear,
    );

NepaliDate calendarFromDto(CalendarDto dto) => NepaliDate(
      year: dto.year,
      month: dto.month,
      dayOfMonth: dto.dayOfMonth,
      era: dto.era,
      firstDayOfMonth: dto.firstDayOfMonth,
      lastDayOfMonth: dto.lastDayOfMonth,
      totalDaysInMonth: dto.totalDaysInMonth,
      dayOfWeekInMonth: dto.dayOfWeekInMonth,
      dayOfWeek: dto.dayOfWeek,
      dayOfYear: dto.dayOfYear,
      weekOfMonth: dto.weekOfMonth,
      weekOfYear: dto.weekOfYear,
    );

NepaliMonthInfo monthInfoFromDto(MonthInfoDto dto) => NepaliMonthInfo(
      year: dto.year,
      month: dto.month,
      totalDaysInMonth: dto.totalDaysInMonth,
      firstDayOfMonth: dto.firstDayOfMonth,
      lastDayOfMonth: dto.lastDayOfMonth,
      daysFromStartOfWeekToFirstOfMonth: dto.daysFromStartOfWeekToFirstOfMonth,
    );

NepaliDateTime dateTimeFromDto(DateTimeDto dto) => NepaliDateTime(
      calendar: calendarFromDto(dto.calendar),
      time: timeFromDto(dto.time),
    );

YearRange yearRangeFromDto(YearRangeDto dto) => YearRange(dto.first, dto.last);

EventDto eventToDto(NepaliEvent event) => EventDto(
      year: event.year,
      month: event.month,
      dayOfMonth: event.dayOfMonth,
      name: event.name,
      kind: eventKindToDto(event.kind),
      closesOffices: event.closesOffices,
      id: event.id,
      payload: event.payload,
    );

NepaliEvent eventFromDto(EventDto dto) => NepaliEvent(
      year: dto.year,
      month: dto.month,
      dayOfMonth: dto.dayOfMonth,
      name: dto.name,
      kind: eventKindFromDto(dto.kind),
      closesOffices: dto.closesOffices,
      id: dto.id,
      payload: dto.payload,
    );

NepaliDayStatus dayStatusFromDto(DayStatusDto dto) => NepaliDayStatus(
      isWeeklyOff: dto.isWeeklyOff,
      isNonWorking: dto.isNonWorking,
      primaryKind:
          dto.primaryKind == null ? null : eventKindFromDto(dto.primaryKind!),
      names: List.unmodifiable(dto.names),
      events: List.unmodifiable(dto.events.map(eventFromDto)),
      closures: List.unmodifiable(dto.closures.map(eventFromDto)),
    );
