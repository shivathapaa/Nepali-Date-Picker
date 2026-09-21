// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'fake_engine_api.dart';

void main() {
  late FakeEngineApi fake;

  setUp(() {
    fake = FakeEngineApi();
    bridge.engineApi = fake;
    NepaliDateValidation.debugResetCaches();
  });

  group('Bikram Sambat input guards', () {
    test('a year outside the table is rejected before the bridge', () async {
      await expectLater(
          NepaliDateConverter.convertBsToAd(1969, 1, 1), throwsArgumentError);
      await expectLater(
          NepaliDateConverter.convertBsToAd(2101, 1, 1), throwsArgumentError);
    });

    test('months stay within 1..12', () async {
      await expectLater(
          NepaliDateConverter.convertBsToAd(2082, 0, 1), throwsArgumentError);
      await expectLater(
          NepaliDateConverter.convertBsToAd(2082, 13, 1), throwsArgumentError);
    });

    test('days honor the month length the engine reports', () async {
      await expectLater(
          NepaliDateConverter.convertBsToAd(2082, 6, 31), throwsArgumentError);
      final converted = await NepaliDateConverter.convertBsToAd(2082, 6, 30);
      expect(converted.era, 1);
    });
  });

  group('Gregorian input guards', () {
    test('unconvertible early dates are rejected with the anchor note', () async {
      await expectLater(
          NepaliDateConverter.convertAdToBs(1913, 4, 12), throwsArgumentError);
      final converted = await NepaliDateConverter.convertAdToBs(1913, 4, 13);
      expect(converted.year, 2082);
    });
  });

  group('scalar guards', () {
    test('weekday and month names validate their index', () async {
      await expectLater(
          NepaliDateConverter.weekdayName(0), throwsArgumentError);
      await expectLater(
          NepaliDateConverter.weekdayName(8), throwsArgumentError);
      await expectLater(
          NepaliDateConverter.bsMonthName(13), throwsArgumentError);
    });

    test('malformed ISO text never crosses the bridge', () async {
      await expectLater(
          NepaliDateConverter.bsDateTimeFromIso('not a date'),
          throwsArgumentError);
      await expectLater(
          NepaliDateConverter.bsDateTimeFromIso('2024-13-40T99:99:99Z'),
          throwsArgumentError);
    });

    test('a hand-built calendar with a fake weekday cannot be formatted',
        () async {
      const bogus = NepaliDate(
        year: 2082,
        month: 6,
        dayOfMonth: 4,
        era: 2,
        firstDayOfMonth: 1,
        lastDayOfMonth: 1,
        totalDaysInMonth: 30,
      );
      await expectLater(
          NepaliDateConverter.formatBsDate(bogus), throwsArgumentError);
    });
  });
}
