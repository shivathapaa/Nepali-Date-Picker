// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'recording_engine_api.dart';

/// Pins which engine method each converter call reaches, with which
/// arguments, and what the answer becomes on the Dart side.
void main() {
  late RecordingEngineApi engine;

  setUp(() {
    engine = RecordingEngineApi();
    bridge.engineApi = engine;
    NepaliDateValidation.debugResetCaches();
  });

  group('year ranges', () {
    test('both published ranges come back as inclusive ranges', () async {
      expect(await NepaliDateConverter.bsYearRange(),
          const YearRange(1970, 2100));
      expect(await NepaliDateConverter.adYearRange(),
          const YearRange(1913, 2043));
    });

    test('the Gregorian range for Bikram Sambat years is asked for as given',
        () async {
      expect(await NepaliDateConverter.adYearRangeForBsYears(2080, 2090),
          const YearRange(2023, 2034));
      expect(engine.lastCall, 'getAdYearRangeForBsYears');
      expect(engine.lastArguments, <Object?>[2080, 2090]);
    });
  });

  group('today and now', () {
    test('today in either calendar arrives as a full calendar', () async {
      expect((await NepaliDateConverter.todayBs()).era, 2);
      expect(engine.lastCall, 'getTodayBs');
      expect((await NepaliDateConverter.todayAd()).era, 1);
      expect(engine.lastCall, 'getTodayAd');
    });

    test('the current time carries every component', () async {
      expect(await NepaliDateConverter.currentTime(),
          const SimpleTime(hour: 16, minute: 30, second: 15, nanosecond: 0));
    });
  });

  group('conversion', () {
    test('a Gregorian date converts after its convertibility is checked',
        () async {
      final result = await NepaliDateConverter.convertAdToBs(2025, 9, 20);

      expect(result.year, 2082);
      expect(engine.calls, contains('isAdDateConvertible'));
      expect(engine.argumentsOf('convertAdToBs'), <Object?>[2025, 9, 20]);
    });

    test('a Bikram Sambat date converts after its day is checked', () async {
      final result = await NepaliDateConverter.convertBsToAd(2082, 6, 4);

      expect(result.era, 1);
      expect(engine.argumentsOf('getTotalDaysInBsMonth'), <Object?>[2082, 6]);
      expect(engine.argumentsOf('convertBsToAd'), <Object?>[2082, 6, 4]);
    });

    test('an out of range year never reaches the engine', () async {
      await expectLater(
          NepaliDateConverter.convertBsToAd(2101, 1, 1), throwsArgumentError);
      expect(engine.calls, isNot(contains('convertBsToAd')));
    });

    test('convertibility is answered without a local guard', () async {
      expect(await NepaliDateConverter.isAdDateConvertible(1913, 4, 12), isTrue);
      expect(engine.calls.single, 'isAdDateConvertible');
    });
  });

  group('calendars and months', () {
    test('a described calendar is fetched for either calendar', () async {
      expect((await NepaliDateConverter.bsCalendar(2082, 6, 4)).dayOfWeek, 6);
      expect(engine.argumentsOf('getBsCalendar'), <Object?>[2082, 6, 4]);
      expect((await NepaliDateConverter.adCalendar(2025, 9, 20)).era, 1);
      expect(engine.argumentsOf('getAdCalendar'), <Object?>[2025, 9, 20]);
    });

    test('month metadata maps every grid field', () async {
      final info = await NepaliDateConverter.bsMonth(2082, 6);

      expect(info.year, 2082);
      expect(info.totalDaysInMonth, 30);
      expect(info.daysFromStartOfWeekToFirstOfMonth, 2);
      expect((await NepaliDateConverter.adMonth(2025, 9)).month, 6);
      expect(engine.argumentsOf('getAdMonth'), <Object?>[2025, 9]);
    });

    test('a month of Gregorian days keeps the unconvertible ones null',
        () async {
      final days = await NepaliDateConverter.bsCalendarsInAdMonth(1913, 4);

      expect(days, hasLength(2));
      expect(days.first, isNull);
      expect(days.last?.year, 2082);
      expect(() => days.add(null), throwsUnsupportedError);
    });

    test('a month of Bikram Sambat days comes back unmodifiable', () async {
      final days = await NepaliDateConverter.adCalendarsInBsMonth(2082, 6);

      expect(days.single.era, 1);
      expect(() => days.clear(), throwsUnsupportedError);
    });

    test('month lengths are asked of the table', () async {
      expect(await NepaliDateConverter.totalDaysInBsMonth(2082, 6), 30);
      expect(await NepaliDateConverter.totalDaysInAdMonth(2025, 9), 31);
      expect(engine.argumentsOf('getTotalDaysInAdMonth'), <Object?>[2025, 9]);
    });

    test('a month outside the supported years is refused locally', () async {
      await expectLater(
          NepaliDateConverter.bsMonth(2082, 13), throwsArgumentError);
      await expectLater(
          NepaliDateConverter.adMonth(1900, 1), throwsArgumentError);
      expect(engine.calls, isNot(contains('getBsMonth')));
    });
  });

  group('arithmetic', () {
    test('day arithmetic passes the signed offset through', () async {
      await NepaliDateConverter.addDaysToBsDate(2082, 6, 4, -10);

      expect(engine.argumentsOf('addDaysToBsDate'), <Object?>[2082, 6, 4, -10]);
    });

    test('days between travel as two dates, each validated', () async {
      expect(
          await NepaliDateConverter.bsDaysBetween(
              const SimpleDate(2082, 6, 1), const SimpleDate(2082, 6, 12)),
          11);
      final sent = engine.argumentsOf('getBsDaysBetween')!;
      expect((sent.first as DateDto).dayOfMonth, 1);
      expect((sent.last as DateDto).dayOfMonth, 12);
    });

    test('Gregorian days between cross without a local guard', () async {
      expect(
          await NepaliDateConverter.adDaysBetween(
              const SimpleDate(2025, 9, 1), const SimpleDate(2025, 9, 13)),
          12);
      expect(engine.calls.single, 'getAdDaysBetween');
    });

    test('comparison keeps the argument order it was given', () async {
      expect(
          await NepaliDateConverter.compareBsDates(
              const SimpleDate(2082, 6, 1), const SimpleDate(2082, 6, 12)),
          -1);
      final sent = engine.argumentsOf('compareBsDates')!;
      expect((sent.first as DateDto).dayOfMonth, 1);
    });
  });
}
