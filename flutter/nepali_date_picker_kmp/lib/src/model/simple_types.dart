// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';

/// A calendar-agnostic year, month and day, in either the Bikram Sambat or
/// the Gregorian system depending on where it is used.
///
/// Month is 1 based in both systems: 1 is Baisakh in Bikram Sambat and
/// January in Gregorian. [dayOfMonth] defaults to 1, mirroring the shared
/// Kotlin `SimpleDate`.
@immutable
class SimpleDate implements Comparable<SimpleDate> {
  const SimpleDate(this.year, this.month, [this.dayOfMonth = 1]);

  final int year;
  final int month;
  final int dayOfMonth;

  /// Chronological order by year, then month, then day.
  @override
  int compareTo(SimpleDate other) {
    if (year != other.year) return year.compareTo(other.year);
    if (month != other.month) return month.compareTo(other.month);
    return dayOfMonth.compareTo(other.dayOfMonth);
  }

  /// Keys match the Kotlin serialization wire form of `SimpleDate`.
  Map<String, Object?> toJson() => <String, Object?>{
        'year': year,
        'month': month,
        'dayOfMonth': dayOfMonth,
      };

  factory SimpleDate.fromJson(Map<String, Object?> json) => SimpleDate(
        json['year']! as int,
        json['month']! as int,
        (json['dayOfMonth'] ?? 1) as int,
      );

  @override
  bool operator ==(Object other) =>
      other is SimpleDate &&
      other.year == year &&
      other.month == month &&
      other.dayOfMonth == dayOfMonth;

  @override
  int get hashCode => Object.hash(year, month, dayOfMonth);

  @override
  String toString() => 'SimpleDate($year, $month, $dayOfMonth)';
}

/// A wall-clock time of day in the Asia/Kathmandu zone (UTC+05:45, no DST).
///
/// [hour] is 0..23, [minute] and [second] are 0..59 and [nanosecond] is
/// 0..999999999, mirroring the shared Kotlin `SimpleTime`.
@immutable
class SimpleTime {
  const SimpleTime({
    required this.hour,
    required this.minute,
    this.second = 0,
    this.nanosecond = 0,
  });

  final int hour;
  final int minute;
  final int second;
  final int nanosecond;

  /// Keys match the Kotlin serialization wire form of `SimpleTime`.
  Map<String, Object?> toJson() => <String, Object?>{
        'hour': hour,
        'minute': minute,
        'second': second,
        'nanosecond': nanosecond,
      };

  factory SimpleTime.fromJson(Map<String, Object?> json) => SimpleTime(
        hour: json['hour']! as int,
        minute: json['minute']! as int,
        second: (json['second'] ?? 0) as int,
        nanosecond: (json['nanosecond'] ?? 0) as int,
      );

  @override
  bool operator ==(Object other) =>
      other is SimpleTime &&
      other.hour == hour &&
      other.minute == minute &&
      other.second == second &&
      other.nanosecond == nanosecond;

  @override
  int get hashCode => Object.hash(hour, minute, second, nanosecond);

  @override
  String toString() => 'SimpleTime($hour:$minute:$second.$nanosecond)';
}

/// An inclusive range of years, in whichever calendar system the producing
/// call documents.
@immutable
class YearRange {
  const YearRange(this.first, this.last);

  final int first;
  final int last;

  /// Whether [year] lies inside the range, both ends included.
  bool contains(int year) => year >= first && year <= last;

  @override
  bool operator ==(Object other) =>
      other is YearRange && other.first == first && other.last == last;

  @override
  int get hashCode => Object.hash(first, last);

  @override
  String toString() => 'YearRange($first..$last)';
}
