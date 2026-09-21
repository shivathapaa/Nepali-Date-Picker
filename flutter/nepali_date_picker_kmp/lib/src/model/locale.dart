// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';

/// The language names and labels are produced in, mirroring the shared
/// Kotlin `NepaliDatePickerLang`.
enum NepaliLanguage { english, nepali }

/// How long a weekday or month name is, mirroring the shared Kotlin
/// `NameFormat`: `full` is "Monday" or "Baisakh", `medium` and `short`
/// shorten progressively.
enum NameFormat { full, medium, short }

/// The preset shapes a formatted date can take, mirroring the shared Kotlin
/// `NepaliDateFormatStyle`.
///
/// `full` reads "Monday, Asar 21, 2024", `long` drops the weekday, `medium`
/// leads with the year, the `short` styles are slash dates with a four digit
/// year and the `compact` styles with a two digit year.
enum NepaliDateFormatStyle {
  full,
  long,
  medium,
  shortMdy,
  shortYmd,
  compactMdy,
  compactYmd,
}

/// The numeral script digits are written in: ASCII 0-9 or Devanagari
/// (Nepali) numerals, mirroring the shared Kotlin `DigitScript`.
enum DigitScript { latin, devanagari }

/// Which calendar a date belongs to. [era] carries the shared numeric
/// convention: 1 for Gregorian (AD), 2 for Bikram Sambat (BS).
enum CalendarSystem {
  gregorian(1),
  bikramSambat(2);

  const CalendarSystem(this.era);

  final int era;

  /// The system for a shared era number, or null when [era] is not 1 or 2.
  static CalendarSystem? fromEra(int era) => switch (era) {
        1 => CalendarSystem.gregorian,
        2 => CalendarSystem.bikramSambat,
        _ => null,
      };
}

/// How dates render: language, preset style, name lengths and digit script,
/// mirroring the shared Kotlin `NepaliDateLocale`.
///
/// Leaving [digitScript] null follows [language]: Nepali text uses
/// Devanagari digits and English text uses Latin digits.
@immutable
class NepaliDateLocale {
  const NepaliDateLocale({
    this.language = NepaliLanguage.english,
    this.dateFormat = NepaliDateFormatStyle.long,
    this.weekDayName = NameFormat.full,
    this.monthName = NameFormat.full,
    this.digitScript,
  });

  final NepaliLanguage language;
  final NepaliDateFormatStyle dateFormat;
  final NameFormat weekDayName;
  final NameFormat monthName;
  final DigitScript? digitScript;

  /// The script actually used: the explicit choice, or the language default.
  DigitScript get resolvedDigitScript =>
      digitScript ??
      (language == NepaliLanguage.nepali
          ? DigitScript.devanagari
          : DigitScript.latin);

  @override
  bool operator ==(Object other) =>
      other is NepaliDateLocale &&
      other.language == language &&
      other.dateFormat == dateFormat &&
      other.weekDayName == weekDayName &&
      other.monthName == monthName &&
      other.digitScript == digitScript;

  @override
  int get hashCode =>
      Object.hash(language, dateFormat, weekDayName, monthName, digitScript);

  @override
  String toString() =>
      'NepaliDateLocale($language, $dateFormat, weekday: $weekDayName, '
      'month: $monthName, digits: ${digitScript ?? "auto"})';
}
