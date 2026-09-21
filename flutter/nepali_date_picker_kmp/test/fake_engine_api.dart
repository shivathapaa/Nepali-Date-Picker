// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:nepali_date_picker_kmp/src/messages.g.dart';

/// The table facts the fake serves: the published ranges and a fixed month
/// length, enough for the validation and mapping layers under test.
class FakeEngineApi implements EngineApi {
  FakeEngineApi({this.daysInBsMonth = 30, this.daysInAdMonth = 31});

  final int daysInBsMonth;
  final int daysInAdMonth;

  /// Every policy the wrappers sent over, latest last.
  final List<PolicyDto> sentPolicies = <PolicyDto>[];

  static CalendarDto calendar({
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

  @override
  Future<YearRangeDto> getBsYearRange() async =>
      YearRangeDto(first: 1970, last: 2100);

  @override
  Future<YearRangeDto> getAdYearRange() async =>
      YearRangeDto(first: 1913, last: 2043);

  @override
  Future<int> getTotalDaysInBsMonth(int year, int month) async => daysInBsMonth;

  @override
  Future<int> getTotalDaysInAdMonth(int year, int month) async => daysInAdMonth;

  @override
  Future<bool> isAdDateConvertible(int year, int month, int dayOfMonth) async =>
      year > 1913 || (month > 4 || (month == 4 && dayOfMonth >= 13));

  @override
  Future<CalendarDto> convertBsToAd(int year, int month, int dayOfMonth) async =>
      calendar(year: 2026, month: 9, dayOfMonth: 20, era: 1);

  @override
  Future<CalendarDto> convertAdToBs(int year, int month, int dayOfMonth) async =>
      calendar();

  @override
  Future<CalendarDto> getTodayBs() async => calendar();

  @override
  Future<String> localizeDigits(String text, DigitScriptDto script) async =>
      'localized:$text:${script.name}';

  @override
  Future<DayStatusDto> statusOf(PolicyDto policy, DateDto date) async {
    sentPolicies.add(policy);
    return DayStatusDto(
      isWeeklyOff: false,
      isNonWorking: true,
      primaryKind: EventKindDto.governmentPublic,
      names: const <String>['Vijaya Dashami'],
      events: const <EventDto>[],
      closures: const <EventDto>[],
    );
  }

  @override
  Future<int> workingDaysBetween(PolicyDto policy, DateDto start, DateDto end) async {
    sentPolicies.add(policy);
    return 7;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw UnimplementedError('${invocation.memberName} is not faked');
}
