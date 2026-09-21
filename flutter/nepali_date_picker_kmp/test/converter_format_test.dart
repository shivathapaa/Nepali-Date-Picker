// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'recording_engine_api.dart';

/// Pins the naming, formatting, timestamp and digit half of the converter:
/// which engine method each call reaches and how the locale, pattern and
/// time arguments are marshalled.
void main() {
  late RecordingEngineApi engine;

  setUp(() {
    engine = RecordingEngineApi();
    bridge.engineApi = engine;
    NepaliDateValidation.debugResetCaches();
  });

  const time = SimpleTime(hour: 16, minute: 30, second: 15);
  const date = SimpleDate(2082, 6, 4);

  group('names', () {
    test('a weekday is asked for with its format and language', () async {
      expect(
          await NepaliDateConverter.weekdayName(6,
              format: NameFormat.short, language: NepaliLanguage.nepali),
          'Friday');
      expect(engine.lastArguments,
          <Object?>[6, NameFormatDto.short, LangDto.nepali]);
    });

    test('month names travel for either calendar', () async {
      expect(await NepaliDateConverter.bsMonthName(6), 'Asoj');
      expect(engine.lastArguments,
          <Object?>[6, NameFormatDto.full, LangDto.english]);
      expect(await NepaliDateConverter.adMonthName(9), 'September');
      expect(engine.lastCall, 'getAdMonthName');
    });

    test('an index outside the shared 1 based range is refused locally',
        () async {
      await expectLater(NepaliDateConverter.weekdayName(8), throwsArgumentError);
      await expectLater(NepaliDateConverter.bsMonthName(0), throwsArgumentError);
      expect(engine.calls, isEmpty);
    });
  });

  group('locale presets', () {
    test('every locale axis reaches the engine', () async {
      final calendar = await NepaliDateConverter.bsCalendar(2082, 6, 4);

      await NepaliDateConverter.formatBsDate(
        calendar,
        locale: const NepaliDateLocale(
          language: NepaliLanguage.nepali,
          dateFormat: NepaliDateFormatStyle.compactYmd,
          weekDayName: NameFormat.medium,
          monthName: NameFormat.short,
          digitScript: DigitScript.devanagari,
        ),
      );

      final sent = engine.argumentsOf('formatBsDate')!;
      expect((sent.first as CalendarDto).dayOfMonth, 4);
      final locale = sent.last as LocaleDto;
      expect(locale.language, LangDto.nepali);
      expect(locale.dateFormat, DateFormatStyleDto.compactYmd);
      expect(locale.weekDayName, NameFormatDto.medium);
      expect(locale.monthName, NameFormatDto.short);
      expect(locale.digitScript, DigitScriptDto.devanagari);
    });

    test('an unset digit script stays unset on the wire', () async {
      final calendar = await NepaliDateConverter.adCalendar(2025, 9, 20);

      expect(await NepaliDateConverter.formatAdDate(calendar),
          'September 20, 2025');
      expect((engine.argumentsOf('formatAdDate')!.last as LocaleDto).digitScript,
          isNull);
    });

    test('a calendar carrying an impossible weekday is refused locally',
        () async {
      const calendar = NepaliDate(
        year: 2082,
        month: 6,
        dayOfMonth: 4,
        era: 2,
        firstDayOfMonth: 3,
        lastDayOfMonth: 4,
        totalDaysInMonth: 30,
        dayOfWeekInMonth: 1,
        dayOfWeek: 0,
        dayOfYear: 160,
        weekOfMonth: 1,
        weekOfYear: 24,
      );

      await expectLater(
          NepaliDateConverter.formatBsDate(calendar), throwsArgumentError);
      expect(engine.calls, isEmpty);
    });
  });

  group('patterns', () {
    test('a Bikram Sambat pattern travels with its date and language',
        () async {
      expect(
          await NepaliDateConverter.formatBsDateByPattern(
              'yyyy MMMM d', date),
          '२०८२ असोज ४');
      final sent = engine.argumentsOf('formatBsDateByPattern')!;
      expect(sent.first, 'yyyy MMMM d');
      expect((sent[1] as DateDto).year, 2082);
      expect(sent.last, LangDto.nepali);
    });

    test('a Gregorian pattern defaults to English', () async {
      await NepaliDateConverter.formatAdDateByPattern(
          'yyyy MMMM d', const SimpleDate(2025, 9, 20));

      expect(engine.argumentsOf('formatAdDateByPattern')!.last,
          LangDto.english);
    });

    test('a date-time pattern sends the time only when one is given',
        () async {
      await NepaliDateConverter.formatBsDateTimeByPattern('yyyy MMMM d, hh:mm a',
          date, time: time);
      expect((engine.argumentsOf('formatBsDateTimeByPattern')![2] as TimeDto)
          .minute, 30);

      engine = RecordingEngineApi();
      bridge.engineApi = engine;
      NepaliDateValidation.debugResetCaches();
      await NepaliDateConverter.formatAdDateTimeByPattern(
          'yyyy MMMM d', const SimpleDate(2025, 9, 20));
      expect(engine.argumentsOf('formatAdDateTimeByPattern')![2], isNull);
    });

    test('a time pattern carries every component', () async {
      expect(await NepaliDateConverter.formatTimeByPattern('hh:mm a', time),
          '04:30 PM');
      expect((engine.lastArguments[1] as TimeDto).second, 15);
    });
  });

  group('clock and timestamps', () {
    test('the English and Nepali clock forms pass the hour convention',
        () async {
      expect(await NepaliDateConverter.formatTimeEnglish(time), '4:30 PM');
      expect(engine.lastArguments.last, isTrue);
      expect(
          await NepaliDateConverter.formatTimeNepali(time,
              use12HourFormat: false),
          'दिउँसो ४:३०');
      expect(engine.lastArguments.last, isFalse);
    });

    test('an out of range time never reaches the engine', () async {
      await expectLater(
          NepaliDateConverter.formatTimeEnglish(
              const SimpleTime(hour: 24, minute: 0)),
          throwsArgumentError);
      expect(engine.calls, isEmpty);
    });

    test('a timestamp without a time resolves the current one first', () async {
      expect(await NepaliDateConverter.bsDateTimeToIso(date),
          '2025-09-20T10:45:15Z');
      expect(engine.calls, contains('getCurrentTime'));
      expect((engine.argumentsOf('bsDateTimeToIso')!.last as TimeDto).hour, 16);
    });

    test('a timestamp with a time uses it as given', () async {
      await NepaliDateConverter.adDateTimeToIso(const SimpleDate(2025, 9, 20),
          time: const SimpleTime(hour: 9, minute: 5));

      expect(engine.calls, isNot(contains('getCurrentTime')));
      expect((engine.argumentsOf('adDateTimeToIso')!.last as TimeDto).hour, 9);
    });

    test('a parsed timestamp becomes a calendar and a time', () async {
      final parsed =
          await NepaliDateConverter.bsDateTimeFromIso('2025-09-20T10:45:15Z');

      expect(parsed.calendar.year, 2082);
      expect(parsed.time.minute, 30);
      expect(await NepaliDateConverter.adDateTimeFromIso('2025-09-20T10:45'),
          isA<NepaliDateTime>());
    });

    test('malformed timestamp text is refused locally', () async {
      await expectLater(NepaliDateConverter.adDateTimeFromIso('2025-09-20'),
          throwsArgumentError);
      expect(engine.calls, isEmpty);
    });
  });

  group('digits', () {
    test('digits localize into the script asked for', () async {
      expect(await NepaliDateConverter.localizeDigits('2082'), '२०८२');
      expect(engine.lastArguments,
          <Object?>['2082', DigitScriptDto.devanagari]);
      expect(await NepaliDateConverter.toLatinDigits('२०८२'), '2082');
      expect(engine.lastArguments, <Object?>['२०८२']);
    });
  });
}
