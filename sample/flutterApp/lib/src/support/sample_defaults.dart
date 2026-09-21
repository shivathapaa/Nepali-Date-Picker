// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// The locales and seed dates every screen shares, defined once like the
// SwiftUI sample's SampleDefaults, so the showcase reads consistently.

import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

class SampleDefaults {
  SampleDefaults._();

  /// Weekday numbers are 1 based, Sunday through Saturday.
  static const int sunday = 1;
  static const int saturday = 7;

  static const english = NepaliDateLocale();

  static const englishRange = NepaliDateLocale(
    dateFormat: NepaliDateFormatStyle.shortYmd,
    weekDayName: NameFormat.short,
  );

  static const englishText = NepaliDateLocale(
    dateFormat: NepaliDateFormatStyle.full,
  );

  static const nepali = NepaliDateLocale(language: NepaliLanguage.nepali);

  static const nepaliText = NepaliDateLocale(
    language: NepaliLanguage.nepali,
    dateFormat: NepaliDateFormatStyle.full,
  );

  /// The Pickers screen's fixed seed: Baisakh 15, 2081.
  static const preselectedDate = SimpleDate(2081, 1, 15);

  /// Calendar-switch seed, chosen because BS 2083-06-01 is AD 2026-09-17.
  static const switchSeed = SimpleDate(2083, 6, 1);

  /// Shrawan 2083 needs six grid rows, so adjacent-day demos use it.
  static const tallMonth = SimpleDate(2083, 4, 15);
}

/// The plain year/month/day of a full calendar, printed the way the other
/// showcases print raw dates.
String dateText(SimpleDate date) =>
    '${date.year}/${date.month}/${date.dayOfMonth}';

String calendarText(NepaliDate? date) =>
    date == null ? 'none' : '${date.year}/${date.month}/${date.dayOfMonth}';
