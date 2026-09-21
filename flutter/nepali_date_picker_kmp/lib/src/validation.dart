// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';

import 'mapping.dart';
import 'messages.g.dart';
import 'model/nepali_date.dart';
import 'model/simple_types.dart';

/// Dart-side guards for everything the Kotlin engine would reject.
///
/// The engine throws `IllegalArgumentException` for out of range input. On
/// Android that surfaces as a catchable platform error, but a Kotlin/Native
/// exception cannot cross the Swift boundary and would abort an iOS app, so
/// the same preconditions are enforced here, before any call crosses the
/// bridge, and fail as [ArgumentError] on both platforms alike.
class NepaliDateValidation {
  NepaliDateValidation._();

  static YearRange? _bsRange;
  static YearRange? _adRange;

  /// The supported Bikram Sambat years, fetched once and cached for the
  /// process lifetime; the table only changes with a library release.
  static Future<YearRange> bsYearRange(EngineApi api) async =>
      _bsRange ??= yearRangeFromDto(await api.getBsYearRange());

  /// The supported Gregorian years, fetched once and cached.
  static Future<YearRange> adYearRange(EngineApi api) async =>
      _adRange ??= yearRangeFromDto(await api.getAdYearRange());

  /// Clears the cached ranges so a test can substitute another engine.
  @visibleForTesting
  static void debugResetCaches() {
    _bsRange = null;
    _adRange = null;
  }

  static void requireMonth(int month) {
    if (month < 1 || month > 12) {
      throw ArgumentError.value(month, 'month', 'must be within 1..12');
    }
  }

  static void requireWeekday(int dayOfWeek) {
    if (dayOfWeek < 1 || dayOfWeek > 7) {
      throw ArgumentError.value(
          dayOfWeek, 'dayOfWeek', 'must be within 1..7, 1 is Sunday');
    }
  }

  static void requireTime(SimpleTime time) {
    if (time.hour < 0 || time.hour > 23) {
      throw ArgumentError.value(time.hour, 'hour', 'must be within 0..23');
    }
    if (time.minute < 0 || time.minute > 59) {
      throw ArgumentError.value(time.minute, 'minute', 'must be within 0..59');
    }
    if (time.second < 0 || time.second > 59) {
      throw ArgumentError.value(time.second, 'second', 'must be within 0..59');
    }
    if (time.nanosecond < 0 || time.nanosecond > 999999999) {
      throw ArgumentError.value(
          time.nanosecond, 'nanosecond', 'must be within 0..999999999');
    }
  }

  static Future<void> requireBsMonth(EngineApi api, int year, int month) async {
    requireMonth(month);
    final range = await bsYearRange(api);
    if (!range.contains(year)) {
      throw ArgumentError.value(year, 'year',
          'must be within the supported Bikram Sambat years $range');
    }
  }

  static Future<void> requireAdMonth(EngineApi api, int year, int month) async {
    requireMonth(month);
    final range = await adYearRange(api);
    if (!range.contains(year)) {
      throw ArgumentError.value(
          year, 'year', 'must be within the supported Gregorian years $range');
    }
  }

  static Future<void> requireBsDate(
      EngineApi api, int year, int month, int dayOfMonth) async {
    await requireBsMonth(api, year, month);
    final daysInMonth = await api.getTotalDaysInBsMonth(year, month);
    if (dayOfMonth < 1 || dayOfMonth > daysInMonth) {
      throw ArgumentError.value(dayOfMonth, 'dayOfMonth',
          'must be within 1..$daysInMonth for Bikram Sambat $year-$month');
    }
  }

  static Future<void> requireBsSimpleDate(EngineApi api, SimpleDate date) =>
      requireBsDate(api, date.year, date.month, date.dayOfMonth);

  static Future<void> requireConvertibleAd(
      EngineApi api, int year, int month, int dayOfMonth) async {
    await requireAdMonth(api, year, month);
    final daysInMonth = await api.getTotalDaysInAdMonth(year, month);
    if (dayOfMonth < 1 || dayOfMonth > daysInMonth) {
      throw ArgumentError.value(dayOfMonth, 'dayOfMonth',
          'must be within 1..$daysInMonth for $year-$month');
    }
    if (!await api.isAdDateConvertible(year, month, dayOfMonth)) {
      throw ArgumentError(
          '$year-$month-$dayOfMonth has no Bikram Sambat equivalent; the '
          'conversion anchor is 1913-04-13');
    }
  }

  /// A calendar handed to a formatter must carry a real month and weekday;
  /// the formatter indexes name tables with both.
  static void requireFormattableCalendar(NepaliDate calendar) {
    requireMonth(calendar.month);
    requireWeekday(calendar.dayOfWeek);
    if (calendar.dayOfMonth < 1 || calendar.dayOfMonth > 32) {
      throw ArgumentError.value(
          calendar.dayOfMonth, 'dayOfMonth', 'must be within 1..32');
    }
  }

  /// The engine's ISO parser rejects malformed text by throwing, so the
  /// shape and the component ranges are checked here first. Both
  /// `2024-09-09T09:00:15Z` and the zone-less local form are accepted.
  static void requireIsoDateTime(String isoDateTime) {
    final shape = RegExp(
        r'^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(:(\d{2})(\.\d{1,9})?)?(Z|[+-]\d{2}:\d{2})?$');
    final match = shape.firstMatch(isoDateTime);
    void reject() => throw ArgumentError.value(
        isoDateTime, 'isoDateTime', 'is not an ISO 8601 date-time');
    if (match == null) reject();
    final month = int.parse(match![2]!);
    final day = int.parse(match[3]!);
    final hour = int.parse(match[4]!);
    final minute = int.parse(match[5]!);
    final second = int.tryParse(match[7] ?? '0') ?? 0;
    if (month < 1 || month > 12 || day < 1 || day > 31) reject();
    if (hour > 23 || minute > 59 || second > 59) reject();
  }

  /// Weekly off day sets travel into Kotlin, whose policy constructor throws
  /// outside 1..7.
  static void requireWeeklyOffDays(Iterable<int> weeklyOffDays) {
    for (final day in weeklyOffDays) {
      if (day < 1 || day > 7) {
        throw ArgumentError.value(
            day, 'weeklyOffDays', 'every entry must be within 1..7');
      }
    }
  }
}
