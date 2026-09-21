// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

void main() {
  test('SimpleDate compares chronologically and round-trips json', () {
    const earlier = SimpleDate(2082, 6, 4);
    const later = SimpleDate(2082, 7, 1);
    expect(earlier.compareTo(later), lessThan(0));
    expect(SimpleDate.fromJson(earlier.toJson()), earlier);
    expect(const SimpleDate(2082, 6).dayOfMonth, 1);
  });

  test('NepaliDate json keys follow the Kotlin serialization form', () {
    const date = NepaliDate(
      year: 2082,
      month: 6,
      dayOfMonth: 4,
      era: 2,
      firstDayOfMonth: 3,
      lastDayOfMonth: 4,
      totalDaysInMonth: 30,
      dayOfWeek: 6,
    );
    final json = date.toJson();
    expect(json['dayOfMonth'], 4);
    expect(json['totalDaysInMonth'], 30);
    expect(NepaliDate.fromJson(json), date);
    expect(date.toSimpleDate(), const SimpleDate(2082, 6, 4));
  });

  test('YearRange contains both ends', () {
    const range = YearRange(1970, 2100);
    expect(range.contains(1970), isTrue);
    expect(range.contains(2100), isTrue);
    expect(range.contains(1969), isFalse);
  });

  test('CalendarSystem maps the shared era numbers', () {
    expect(CalendarSystem.gregorian.era, 1);
    expect(CalendarSystem.bikramSambat.era, 2);
    expect(CalendarSystem.fromEra(2), CalendarSystem.bikramSambat);
    expect(CalendarSystem.fromEra(3), isNull);
  });

  test('the locale resolves its digit script from the language', () {
    const nepali = NepaliDateLocale(language: NepaliLanguage.nepali);
    const english = NepaliDateLocale();
    const pinned = NepaliDateLocale(
        language: NepaliLanguage.nepali, digitScript: DigitScript.latin);
    expect(nepali.resolvedDigitScript, DigitScript.devanagari);
    expect(english.resolvedDigitScript, DigitScript.latin);
    expect(pinned.resolvedDigitScript, DigitScript.latin);
  });

  test('an event defaults closesOffices from its kind', () {
    const holiday = NepaliEvent(
        year: 2082, month: 6, dayOfMonth: 24, name: 'Dashami',
        kind: NepaliEventKind.governmentPublic);
    const meeting = NepaliEvent(
        year: 2082, month: 6, dayOfMonth: 10, name: 'Review',
        kind: NepaliEventKind.observance);
    const openHoliday = NepaliEvent(
        year: 2082, month: 6, dayOfMonth: 24, name: 'Regional day',
        kind: NepaliEventKind.governmentPublic, closesOffices: false);
    expect(holiday.closesOffices, isTrue);
    expect(meeting.closesOffices, isFalse);
    expect(openHoliday.closesOffices, isFalse);
  });

  test('wire patterns expose their printable shape', () {
    expect(DatePattern.yyyyDashMmDashDd.literal, 'YYYY-MM-DD');
    expect(DatePattern.ddSlashMmSlashYyyy.literal, 'DD/MM/YYYY');
  });
}
