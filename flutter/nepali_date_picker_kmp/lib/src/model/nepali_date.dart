// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';

import 'simple_types.dart';

/// A fully described calendar date in either the Bikram Sambat or the
/// Gregorian system, mirroring the shared Kotlin `CustomCalendar`.
///
/// [era] is 1 for AD (Gregorian) and 2 for BS (Bikram Sambat). Weekday and
/// month indices are 1 based: weekday 1 is Sunday through 7 for Saturday, and
/// month 1 is Baisakh or January. Fields the producing call does not compute
/// hold -1.
@immutable
class NepaliDate {
  const NepaliDate({
    required this.year,
    required this.month,
    required this.dayOfMonth,
    required this.era,
    required this.firstDayOfMonth,
    required this.lastDayOfMonth,
    required this.totalDaysInMonth,
    this.dayOfWeekInMonth = -1,
    this.dayOfWeek = -1,
    this.dayOfYear = -1,
    this.weekOfMonth = -1,
    this.weekOfYear = -1,
  });

  final int year;
  final int month;
  final int dayOfMonth;
  final int era;
  final int firstDayOfMonth;
  final int lastDayOfMonth;
  final int totalDaysInMonth;
  final int dayOfWeekInMonth;
  final int dayOfWeek;
  final int dayOfYear;
  final int weekOfMonth;
  final int weekOfYear;

  /// The plain year, month and day of this date, dropping the derived fields.
  SimpleDate toSimpleDate() => SimpleDate(year, month, dayOfMonth);

  /// Keys match the Kotlin serialization wire form of `CustomCalendar`.
  Map<String, Object?> toJson() => <String, Object?>{
        'year': year,
        'month': month,
        'dayOfMonth': dayOfMonth,
        'era': era,
        'firstDayOfMonth': firstDayOfMonth,
        'lastDayOfMonth': lastDayOfMonth,
        'totalDaysInMonth': totalDaysInMonth,
        'dayOfWeekInMonth': dayOfWeekInMonth,
        'dayOfWeek': dayOfWeek,
        'dayOfYear': dayOfYear,
        'weekOfMonth': weekOfMonth,
        'weekOfYear': weekOfYear,
      };

  factory NepaliDate.fromJson(Map<String, Object?> json) => NepaliDate(
        year: json['year']! as int,
        month: json['month']! as int,
        dayOfMonth: json['dayOfMonth']! as int,
        era: json['era']! as int,
        firstDayOfMonth: json['firstDayOfMonth']! as int,
        lastDayOfMonth: json['lastDayOfMonth']! as int,
        totalDaysInMonth: json['totalDaysInMonth']! as int,
        dayOfWeekInMonth: (json['dayOfWeekInMonth'] ?? -1) as int,
        dayOfWeek: (json['dayOfWeek'] ?? -1) as int,
        dayOfYear: (json['dayOfYear'] ?? -1) as int,
        weekOfMonth: (json['weekOfMonth'] ?? -1) as int,
        weekOfYear: (json['weekOfYear'] ?? -1) as int,
      );

  @override
  bool operator ==(Object other) =>
      other is NepaliDate &&
      other.year == year &&
      other.month == month &&
      other.dayOfMonth == dayOfMonth &&
      other.era == era &&
      other.firstDayOfMonth == firstDayOfMonth &&
      other.lastDayOfMonth == lastDayOfMonth &&
      other.totalDaysInMonth == totalDaysInMonth &&
      other.dayOfWeekInMonth == dayOfWeekInMonth &&
      other.dayOfWeek == dayOfWeek &&
      other.dayOfYear == dayOfYear &&
      other.weekOfMonth == weekOfMonth &&
      other.weekOfYear == weekOfYear;

  @override
  int get hashCode => Object.hash(
        year,
        month,
        dayOfMonth,
        era,
        firstDayOfMonth,
        lastDayOfMonth,
        totalDaysInMonth,
        dayOfWeekInMonth,
        dayOfWeek,
        dayOfYear,
        weekOfMonth,
        weekOfYear,
      );

  @override
  String toString() =>
      'NepaliDate($year-$month-$dayOfMonth, era: $era, dayOfWeek: $dayOfWeek)';
}

/// A [NepaliDate] paired with a [SimpleTime], mirroring the shared Kotlin
/// `CustomDateTime`.
@immutable
class NepaliDateTime {
  const NepaliDateTime({required this.calendar, required this.time});

  final NepaliDate calendar;
  final SimpleTime time;

  Map<String, Object?> toJson() => <String, Object?>{
        'customCalendar': calendar.toJson(),
        'simpleTime': time.toJson(),
      };

  factory NepaliDateTime.fromJson(Map<String, Object?> json) => NepaliDateTime(
        calendar:
            NepaliDate.fromJson(json['customCalendar']! as Map<String, Object?>),
        time: SimpleTime.fromJson(json['simpleTime']! as Map<String, Object?>),
      );

  @override
  bool operator ==(Object other) =>
      other is NepaliDateTime && other.calendar == calendar && other.time == time;

  @override
  int get hashCode => Object.hash(calendar, time);

  @override
  String toString() => 'NepaliDateTime($calendar, $time)';
}

/// Month-level metadata for laying out a calendar grid, mirroring the shared
/// Kotlin `NepaliMonthCalendar`.
///
/// [daysFromStartOfWeekToFirstOfMonth] is the number of empty leading cells
/// before day 1 in a Sunday-first grid.
@immutable
class NepaliMonthInfo {
  const NepaliMonthInfo({
    required this.year,
    required this.month,
    required this.totalDaysInMonth,
    required this.firstDayOfMonth,
    required this.lastDayOfMonth,
    required this.daysFromStartOfWeekToFirstOfMonth,
  });

  final int year;
  final int month;
  final int totalDaysInMonth;
  final int firstDayOfMonth;
  final int lastDayOfMonth;
  final int daysFromStartOfWeekToFirstOfMonth;

  @override
  bool operator ==(Object other) =>
      other is NepaliMonthInfo &&
      other.year == year &&
      other.month == month &&
      other.totalDaysInMonth == totalDaysInMonth &&
      other.firstDayOfMonth == firstDayOfMonth &&
      other.lastDayOfMonth == lastDayOfMonth &&
      other.daysFromStartOfWeekToFirstOfMonth ==
          daysFromStartOfWeekToFirstOfMonth;

  @override
  int get hashCode => Object.hash(
        year,
        month,
        totalDaysInMonth,
        firstDayOfMonth,
        lastDayOfMonth,
        daysFromStartOfWeekToFirstOfMonth,
      );

  @override
  String toString() =>
      'NepaliMonthInfo($year-$month, days: $totalDaysInMonth, leading: '
      '$daysFromStartOfWeekToFirstOfMonth)';
}
