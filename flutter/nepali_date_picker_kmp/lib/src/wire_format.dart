// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'bridge.dart';
import 'mapping.dart';
import 'messages.g.dart';
import 'model/locale.dart';
import 'model/simple_types.dart';
import 'validation.dart';

/// The four fixed text-field shapes a wire date can take, mirroring the
/// shared `NepaliDateFormatter.Pattern`. Every shape is 10 characters with
/// two delimiters.
enum DatePattern {
  yyyySlashMmSlashDd('YYYY/MM/DD'),
  yyyyDashMmDashDd('YYYY-MM-DD'),
  ddSlashMmSlashYyyy('DD/MM/YYYY'),
  ddDashMmDashYyyy('DD-MM-YYYY');

  const DatePattern(this.literal);

  /// The shape as a caller would print it, usable as a placeholder.
  final String literal;
}

/// Fixed-shape wire formatting for dates, mirroring the shared
/// `NepaliDateFormatter`: strict on length, no whitespace trimming, and
/// `YYYY-MM-DD` in Latin digits as the canonical form.
class NepaliDateFormatter {
  NepaliDateFormatter._();

  static EngineApi get _api => engineApi;

  /// [date] in the given shape and digit [script].
  static Future<String> format(
    SimpleDate date, {
    DatePattern pattern = DatePattern.yyyyDashMmDashDd,
    DigitScript script = DigitScript.latin,
  }) =>
      _api.wireFormatDate(
        dateToDto(date),
        DatePatternDto.values[pattern.index],
        digitScriptToDto(script),
      );

  /// The date [input] spells in the given shape, or null when the text does
  /// not match it. Latin and Devanagari digits both parse; the month is
  /// clamped to 1..12 and the day to 1..32, with no year-range check.
  static Future<SimpleDate?> parse(
    String input, {
    DatePattern pattern = DatePattern.yyyyDashMmDashDd,
  }) async {
    final parsed =
        await _api.wireParseDate(input, DatePatternDto.values[pattern.index]);
    return parsed == null ? null : dateFromDto(parsed);
  }
}

/// Fixed-shape wire formatting for times, mirroring the shared
/// `NepaliTimeFormatter`: `HH:mm:ss`, with nanoseconds appended only when
/// present.
class NepaliTimeFormatter {
  NepaliTimeFormatter._();

  static EngineApi get _api => engineApi;

  /// [time] in the wire shape.
  static Future<String> format(SimpleTime time) async {
    NepaliDateValidation.requireTime(time);
    return _api.wireFormatTime(timeToDto(time));
  }

  /// The time [input] spells, or null when the text does not match the wire
  /// shape.
  static Future<SimpleTime?> parse(String input) async {
    final parsed = await _api.wireParseTime(input);
    return parsed == null ? null : timeFromDto(parsed);
  }
}
