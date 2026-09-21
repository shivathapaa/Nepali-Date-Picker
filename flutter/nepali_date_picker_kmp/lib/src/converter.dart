// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'bridge.dart';
import 'mapping.dart';
import 'messages.g.dart';
import 'model/locale.dart';
import 'model/nepali_date.dart';
import 'model/simple_types.dart';
import 'validation.dart';

/// The Bikram Sambat conversion engine, backed by the compiled Kotlin
/// Multiplatform `:core` module, the same binary the Android, iOS and npm
/// artifacts ship.
///
/// Every call crosses a platform channel, so everything returns a [Future];
/// the work itself is a synchronous table lookup on the platform thread.
/// Dates use 1 based months and weekdays: month 1 is Baisakh or January,
/// weekday 1 is Sunday through 7 for Saturday.
///
/// Inputs are validated on the Dart side against the supported ranges
/// (Bikram Sambat 1970..2100, Gregorian 1913..2043) and the real month
/// lengths; an out of range date throws an [ArgumentError] without crossing
/// the bridge.
class NepaliDateConverter {
  NepaliDateConverter._();

  static EngineApi get _api => engineApi;

  /// The inclusive Bikram Sambat year range the conversion table covers.
  static Future<YearRange> bsYearRange() =>
      NepaliDateValidation.bsYearRange(_api);

  /// The inclusive Gregorian year range the conversion table covers.
  static Future<YearRange> adYearRange() =>
      NepaliDateValidation.adYearRange(_api);

  /// The Gregorian years a Gregorian-first calendar should offer for a
  /// Bikram Sambat year range, so both calendars cover the same real days.
  static Future<YearRange> adYearRangeForBsYears(int first, int last) async =>
      yearRangeFromDto(await _api.getAdYearRangeForBsYears(first, last));

  /// Today in the Bikram Sambat calendar, in the Asia/Kathmandu zone.
  static Future<NepaliDate> todayBs() async =>
      calendarFromDto(await _api.getTodayBs());

  /// Today in the Gregorian calendar, in the Asia/Kathmandu zone.
  static Future<NepaliDate> todayAd() async =>
      calendarFromDto(await _api.getTodayAd());

  /// The current wall-clock time in the Asia/Kathmandu zone.
  static Future<SimpleTime> currentTime() async =>
      timeFromDto(await _api.getCurrentTime());

  /// Converts a Gregorian (AD) date to its Bikram Sambat equivalent.
  static Future<NepaliDate> convertAdToBs(
      int year, int month, int dayOfMonth) async {
    await NepaliDateValidation.requireConvertibleAd(_api, year, month, dayOfMonth);
    return calendarFromDto(await _api.convertAdToBs(year, month, dayOfMonth));
  }

  /// Converts a Bikram Sambat (BS) date to its Gregorian equivalent.
  static Future<NepaliDate> convertBsToAd(
      int year, int month, int dayOfMonth) async {
    await NepaliDateValidation.requireBsDate(_api, year, month, dayOfMonth);
    return calendarFromDto(await _api.convertBsToAd(year, month, dayOfMonth));
  }

  /// The fully described Bikram Sambat calendar for a BS date.
  static Future<NepaliDate> bsCalendar(
      int year, int month, int dayOfMonth) async {
    await NepaliDateValidation.requireBsDate(_api, year, month, dayOfMonth);
    return calendarFromDto(await _api.getBsCalendar(year, month, dayOfMonth));
  }

  /// The fully described Gregorian calendar for an AD date.
  static Future<NepaliDate> adCalendar(
      int year, int month, int dayOfMonth) async {
    await NepaliDateValidation.requireConvertibleAd(_api, year, month, dayOfMonth);
    return calendarFromDto(await _api.getAdCalendar(year, month, dayOfMonth));
  }

  /// Whether a Gregorian date has a Bikram Sambat equivalent. The year range
  /// alone is not a sufficient check: the calendars start mid-year relative
  /// to each other, so early 1913 sits inside the range yet cannot convert.
  static Future<bool> isAdDateConvertible(
          int year, int month, int dayOfMonth) =>
      _api.isAdDateConvertible(year, month, dayOfMonth);

  /// Month-grid metadata for a Bikram Sambat month.
  static Future<NepaliMonthInfo> bsMonth(int year, int month) async {
    await NepaliDateValidation.requireBsMonth(_api, year, month);
    return monthInfoFromDto(await _api.getBsMonth(year, month));
  }

  /// Month-grid metadata for a Gregorian month.
  static Future<NepaliMonthInfo> adMonth(int year, int month) async {
    await NepaliDateValidation.requireAdMonth(_api, year, month);
    return monthInfoFromDto(await _api.getAdMonth(year, month));
  }

  /// Every day of a Gregorian month as its Bikram Sambat equivalent, in day
  /// order and in one conversion pass. Days before the conversion anchor
  /// (AD 1913-04-13) come back as null.
  static Future<List<NepaliDate?>> bsCalendarsInAdMonth(
      int year, int month) async {
    await NepaliDateValidation.requireAdMonth(_api, year, month);
    final days = await _api.getBsCalendarsInAdMonth(year, month);
    return List.unmodifiable(
        days.map((day) => day == null ? null : calendarFromDto(day)));
  }

  /// Every day of a Bikram Sambat month as its Gregorian equivalent, in day
  /// order and in one conversion pass.
  static Future<List<NepaliDate>> adCalendarsInBsMonth(
      int year, int month) async {
    await NepaliDateValidation.requireBsMonth(_api, year, month);
    final days = await _api.getAdCalendarsInBsMonth(year, month);
    return List.unmodifiable(days.map(calendarFromDto));
  }

  /// Total days in a Bikram Sambat month, from the conversion table.
  static Future<int> totalDaysInBsMonth(int year, int month) async {
    await NepaliDateValidation.requireBsMonth(_api, year, month);
    return _api.getTotalDaysInBsMonth(year, month);
  }

  /// Total days in a Gregorian month, leap years included.
  static Future<int> totalDaysInAdMonth(int year, int month) async {
    await NepaliDateValidation.requireAdMonth(_api, year, month);
    return _api.getTotalDaysInAdMonth(year, month);
  }

  /// The Bikram Sambat date [days] after (or before, when negative) the
  /// given date. Throws when the result leaves the supported range.
  static Future<NepaliDate> addDaysToBsDate(
      int year, int month, int dayOfMonth, int days) async {
    await NepaliDateValidation.requireBsDate(_api, year, month, dayOfMonth);
    return calendarFromDto(
        await _api.addDaysToBsDate(year, month, dayOfMonth, days));
  }

  /// Days between two Bikram Sambat dates, end exclusive. Add 1 to include
  /// the end date.
  static Future<int> bsDaysBetween(SimpleDate start, SimpleDate end) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, start);
    await NepaliDateValidation.requireBsSimpleDate(_api, end);
    return _api.getBsDaysBetween(dateToDto(start), dateToDto(end));
  }

  /// Days between two Gregorian dates, end exclusive. Add 1 to include the
  /// end date.
  static Future<int> adDaysBetween(SimpleDate start, SimpleDate end) =>
      _api.getAdDaysBetween(dateToDto(start), dateToDto(end));

  /// Compares two Bikram Sambat dates: negative when [from] is earlier,
  /// zero when equal, positive when later.
  static Future<int> compareBsDates(SimpleDate from, SimpleDate to) =>
      _api.compareBsDates(dateToDto(from), dateToDto(to));

  /// The weekday name, 1 for Sunday through 7 for Saturday.
  static Future<String> weekdayName(
    int dayOfWeek, {
    NameFormat format = NameFormat.full,
    NepaliLanguage language = NepaliLanguage.english,
  }) async {
    NepaliDateValidation.requireWeekday(dayOfWeek);
    return _api.getWeekdayName(
        dayOfWeek, nameFormatToDto(format), langToDto(language));
  }

  /// The Bikram Sambat month name, 1 for Baisakh through 12 for Chaitra.
  static Future<String> bsMonthName(
    int month, {
    NameFormat format = NameFormat.full,
    NepaliLanguage language = NepaliLanguage.english,
  }) async {
    NepaliDateValidation.requireMonth(month);
    return _api.getBsMonthName(month, nameFormatToDto(format), langToDto(language));
  }

  /// The Gregorian month name, 1 for January through 12 for December.
  static Future<String> adMonthName(
    int month, {
    NameFormat format = NameFormat.full,
    NepaliLanguage language = NepaliLanguage.english,
  }) async {
    NepaliDateValidation.requireMonth(month);
    return _api.getAdMonthName(month, nameFormatToDto(format), langToDto(language));
  }

  /// Formats a Bikram Sambat date with a locale preset. [calendar] must be a
  /// BS calendar produced by this engine, so the weekday it carries is real.
  static Future<String> formatBsDate(
    NepaliDate calendar, {
    NepaliDateLocale locale = const NepaliDateLocale(),
  }) async {
    NepaliDateValidation.requireFormattableCalendar(calendar);
    return _api.formatBsDate(calendarToDto(calendar), localeToDto(locale));
  }

  /// Formats a Gregorian date with a locale preset. [calendar] must be an AD
  /// calendar produced by this engine.
  static Future<String> formatAdDate(
    NepaliDate calendar, {
    NepaliDateLocale locale = const NepaliDateLocale(),
  }) async {
    NepaliDateValidation.requireFormattableCalendar(calendar);
    return _api.formatAdDate(calendarToDto(calendar), localeToDto(locale));
  }

  /// Formats a Bikram Sambat date with a Unicode-style pattern such as
  /// `"yyyy MMMM d, EEEE"`. The full calendar is resolved internally, so
  /// day-of-year and week-of-year tokens work.
  static Future<String> formatBsDateByPattern(
    String pattern,
    SimpleDate date, {
    NepaliLanguage language = NepaliLanguage.nepali,
  }) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, date);
    return _api.formatBsDateByPattern(pattern, dateToDto(date), langToDto(language));
  }

  /// Formats a Gregorian date with a Unicode-style pattern.
  static Future<String> formatAdDateByPattern(
    String pattern,
    SimpleDate date, {
    NepaliLanguage language = NepaliLanguage.english,
  }) async {
    await NepaliDateValidation.requireConvertibleAd(
        _api, date.year, date.month, date.dayOfMonth);
    return _api.formatAdDateByPattern(pattern, dateToDto(date), langToDto(language));
  }

  /// Formats a time of day with a Unicode-style pattern such as `"hh:mm a"`.
  static Future<String> formatTimeByPattern(
    String pattern,
    SimpleTime time, {
    NepaliLanguage language = NepaliLanguage.english,
  }) async {
    NepaliDateValidation.requireTime(time);
    return _api.formatTimeByPattern(pattern, timeToDto(time), langToDto(language));
  }

  /// Formats a Bikram Sambat date, and optionally a time, with one pattern.
  static Future<String> formatBsDateTimeByPattern(
    String pattern,
    SimpleDate date, {
    SimpleTime? time,
    NepaliLanguage language = NepaliLanguage.nepali,
  }) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, date);
    if (time != null) NepaliDateValidation.requireTime(time);
    return _api.formatBsDateTimeByPattern(
      pattern,
      dateToDto(date),
      time == null ? null : timeToDto(time),
      langToDto(language),
    );
  }

  /// Formats a Gregorian date, and optionally a time, with one pattern.
  static Future<String> formatAdDateTimeByPattern(
    String pattern,
    SimpleDate date, {
    SimpleTime? time,
    NepaliLanguage language = NepaliLanguage.english,
  }) async {
    await NepaliDateValidation.requireConvertibleAd(
        _api, date.year, date.month, date.dayOfMonth);
    if (time != null) NepaliDateValidation.requireTime(time);
    return _api.formatAdDateTimeByPattern(
      pattern,
      dateToDto(date),
      time == null ? null : timeToDto(time),
      langToDto(language),
    );
  }

  /// A time of day in English, 12 hour ("4:30 PM") or 24 hour ("16:30").
  static Future<String> formatTimeEnglish(
    SimpleTime time, {
    bool use12HourFormat = true,
  }) async {
    NepaliDateValidation.requireTime(time);
    return _api.formatTimeEnglish(timeToDto(time), use12HourFormat);
  }

  /// A time of day in Nepali, with day-period words in the 12 hour form.
  static Future<String> formatTimeNepali(
    SimpleTime time, {
    bool use12HourFormat = true,
  }) async {
    NepaliDateValidation.requireTime(time);
    return _api.formatTimeNepali(timeToDto(time), use12HourFormat);
  }

  /// A Bikram Sambat date and Nepali time as an ISO 8601 UTC timestamp.
  /// Without [time], the current Asia/Kathmandu time is used.
  static Future<String> bsDateTimeToIso(SimpleDate date, {SimpleTime? time}) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, date);
    final resolved = time ?? await currentTime();
    NepaliDateValidation.requireTime(resolved);
    return _api.bsDateTimeToIso(dateToDto(date), timeToDto(resolved));
  }

  /// A Gregorian date and Nepali time as an ISO 8601 UTC timestamp.
  /// Without [time], the current Asia/Kathmandu time is used.
  static Future<String> adDateTimeToIso(SimpleDate date, {SimpleTime? time}) async {
    await NepaliDateValidation.requireConvertibleAd(
        _api, date.year, date.month, date.dayOfMonth);
    final resolved = time ?? await currentTime();
    NepaliDateValidation.requireTime(resolved);
    return _api.adDateTimeToIso(dateToDto(date), timeToDto(resolved));
  }

  /// Parses an ISO 8601 timestamp into a Bikram Sambat date and Nepali time.
  static Future<NepaliDateTime> bsDateTimeFromIso(String isoDateTime) async {
    NepaliDateValidation.requireIsoDateTime(isoDateTime);
    return dateTimeFromDto(await _api.bsDateTimeFromIso(isoDateTime));
  }

  /// Parses an ISO 8601 timestamp into a Gregorian date and Nepali time.
  static Future<NepaliDateTime> adDateTimeFromIso(String isoDateTime) async {
    NepaliDateValidation.requireIsoDateTime(isoDateTime);
    return dateTimeFromDto(await _api.adDateTimeFromIso(isoDateTime));
  }

  /// Rewrites the ASCII digits in [text] into [script]; everything else
  /// passes through unchanged.
  static Future<String> localizeDigits(
    String text, {
    DigitScript script = DigitScript.devanagari,
  }) =>
      _api.localizeDigits(text, digitScriptToDto(script));

  /// Rewrites Devanagari digits in [text] back to ASCII 0-9.
  static Future<String> toLatinDigits(String text) => _api.toLatinDigits(text);
}
