// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:nepali_date_picker_kmp/src/messages.g.dart';

/// A stand-in engine that answers every call with a fixed value and records
/// what it was asked, so a test can pin the arguments a facade marshals and
/// the model it builds from the answer.
///
/// The recorded names are the wire method names, which is the point: a facade
/// that starts calling a different engine method has to fail here.
class RecordingEngineApi implements EngineApi {
  /// [parsesText] decides what the wire parsers answer: a date and a time
  /// when it holds, nulls when it stands for text that does not match the
  /// shape.
  RecordingEngineApi({this.parsesText = true});

  final bool parsesText;

  /// Wire method names in call order, validation calls included.
  final List<String> calls = <String>[];

  /// Positional arguments per entry of [calls].
  final List<List<Object?>> arguments = <List<Object?>>[];

  /// The name and arguments of the most recent call.
  String get lastCall => calls.last;

  List<Object?> get lastArguments => arguments.last;

  static CalendarDto calendarDto({
    int year = 2082,
    int month = 6,
    int dayOfMonth = 4,
    int era = 2,
  }) =>
      CalendarDto(
        year: year,
        month: month,
        dayOfMonth: dayOfMonth,
        era: era,
        firstDayOfMonth: 3,
        lastDayOfMonth: 4,
        totalDaysInMonth: 30,
        dayOfWeekInMonth: 1,
        dayOfWeek: 6,
        dayOfYear: 160,
        weekOfMonth: 1,
        weekOfYear: 24,
      );

  static final CalendarDto bsCalendar = calendarDto();
  static final CalendarDto adCalendar =
      calendarDto(year: 2025, month: 9, dayOfMonth: 20, era: 1);
  static final TimeDto time =
      TimeDto(hour: 16, minute: 30, second: 15, nanosecond: 0);
  static final EventDto event = EventDto(
    year: 2082,
    month: 6,
    dayOfMonth: 24,
    name: 'Vijaya Dashami',
    kind: EventKindDto.governmentPublic,
    closesOffices: true,
  );
  static final DayStatusDto status = DayStatusDto(
    isWeeklyOff: false,
    isNonWorking: true,
    primaryKind: EventKindDto.governmentPublic,
    names: const <String>['Vijaya Dashami'],
    events: <EventDto>[event],
    closures: <EventDto>[event],
  );
  static final MonthInfoDto month = MonthInfoDto(
    year: 2082,
    month: 6,
    totalDaysInMonth: 30,
    firstDayOfMonth: 3,
    lastDayOfMonth: 4,
    daysFromStartOfWeekToFirstOfMonth: 2,
  );

  late final Map<String, Object?> _responses = <String, Object?>{
    'getBsYearRange': Future<YearRangeDto>.value(
        YearRangeDto(first: 1970, last: 2100)),
    'getAdYearRange': Future<YearRangeDto>.value(
        YearRangeDto(first: 1913, last: 2043)),
    'getAdYearRangeForBsYears':
        Future<YearRangeDto>.value(YearRangeDto(first: 2023, last: 2034)),
    'getTodayBs': Future<CalendarDto>.value(bsCalendar),
    'getTodayAd': Future<CalendarDto>.value(adCalendar),
    'getCurrentTime': Future<TimeDto>.value(time),
    'convertAdToBs': Future<CalendarDto>.value(bsCalendar),
    'convertBsToAd': Future<CalendarDto>.value(adCalendar),
    'getBsCalendar': Future<CalendarDto>.value(bsCalendar),
    'getAdCalendar': Future<CalendarDto>.value(adCalendar),
    'isAdDateConvertible': Future<bool>.value(true),
    'getBsMonth': Future<MonthInfoDto>.value(month),
    'getAdMonth': Future<MonthInfoDto>.value(month),
    'getBsCalendarsInAdMonth':
        Future<List<CalendarDto?>>.value(<CalendarDto?>[null, bsCalendar]),
    'getAdCalendarsInBsMonth':
        Future<List<CalendarDto>>.value(<CalendarDto>[adCalendar]),
    'getTotalDaysInBsMonth': Future<int>.value(30),
    'getTotalDaysInAdMonth': Future<int>.value(31),
    'addDaysToBsDate': Future<CalendarDto>.value(bsCalendar),
    'getBsDaysBetween': Future<int>.value(11),
    'getAdDaysBetween': Future<int>.value(12),
    'compareBsDates': Future<int>.value(-1),
    'getWeekdayName': Future<String>.value('Friday'),
    'getBsMonthName': Future<String>.value('Asoj'),
    'getAdMonthName': Future<String>.value('September'),
    'formatBsDate': Future<String>.value('Asoj 4, 2082'),
    'formatAdDate': Future<String>.value('September 20, 2025'),
    'formatBsDateByPattern': Future<String>.value('२०८२ असोज ४'),
    'formatAdDateByPattern': Future<String>.value('2025 September 20'),
    'formatTimeByPattern': Future<String>.value('04:30 PM'),
    'formatBsDateTimeByPattern': Future<String>.value('२०८२ असोज ४, ०४:३० PM'),
    'formatAdDateTimeByPattern':
        Future<String>.value('2025 September 20, 04:30 PM'),
    'formatTimeEnglish': Future<String>.value('4:30 PM'),
    'formatTimeNepali': Future<String>.value('दिउँसो ४:३०'),
    'bsDateTimeToIso': Future<String>.value('2025-09-20T10:45:15Z'),
    'adDateTimeToIso': Future<String>.value('2025-09-20T10:45:15Z'),
    'bsDateTimeFromIso': Future<DateTimeDto>.value(
        DateTimeDto(calendar: bsCalendar, time: time)),
    'adDateTimeFromIso': Future<DateTimeDto>.value(
        DateTimeDto(calendar: adCalendar, time: time)),
    'localizeDigits': Future<String>.value('२०८२'),
    'toLatinDigits': Future<String>.value('2082'),
    'wireFormatDate': Future<String>.value('2082-06-04'),
    'wireParseDate': Future<DateDto?>.value(
        parsesText ? DateDto(year: 2082, month: 6, dayOfMonth: 4) : null),
    'wireFormatTime': Future<String>.value('16:30:15'),
    'wireParseTime': Future<TimeDto?>.value(parsesText ? time : null),
    'statusOf': Future<DayStatusDto>.value(status),
    'monthStatus': Future<List<DayStatusDto>>.value(<DayStatusDto>[status]),
    'eventsOn': Future<List<EventDto>>.value(<EventDto>[event]),
    'eventsIn': Future<List<EventDto>>.value(<EventDto>[event]),
    'isNonWorkingDay': Future<bool>.value(true),
    'workingDaysBetween': Future<int>.value(7),
    'nextWorkingDay': Future<CalendarDto>.value(bsCalendar),
    'addWorkingDays': Future<CalendarDto>.value(bsCalendar),
    'eventSpanningDays':
        Future<List<EventDto>>.value(<EventDto>[event, event]),
    'eventSpanningThrough':
        Future<List<EventDto>>.value(<EventDto>[event, event, event]),
  };

  /// The policy each policy-taking call was given, latest last.
  List<PolicyDto> get sentPolicies => <PolicyDto>[
        for (final entry in arguments)
          if (entry.isNotEmpty && entry.first is PolicyDto)
            entry.first! as PolicyDto
      ];

  /// The arguments the named method was called with, or null when it was
  /// never called.
  List<Object?>? argumentsOf(String name) {
    final index = calls.indexOf(name);
    return index == -1 ? null : arguments[index];
  }

  @override
  dynamic noSuchMethod(Invocation invocation) {
    final name = _nameOf(invocation.memberName);
    calls.add(name);
    arguments.add(invocation.positionalArguments);
    final response = _responses[name];
    if (response == null) throw UnimplementedError('$name is not faked');
    return response;
  }

  static String _nameOf(Symbol member) {
    final text = member.toString();
    return text.substring(text.indexOf('"') + 1, text.lastIndexOf('"'));
  }
}
