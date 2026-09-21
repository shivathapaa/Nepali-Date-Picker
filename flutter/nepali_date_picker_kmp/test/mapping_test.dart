// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/mapping.dart';
import 'package:nepali_date_picker_kmp/src/messages.g.dart';

/// Pins the wire mapping the whole bridge rests on.
///
/// Public enums map to their generated counterparts by index, so a reordered
/// or extended enum on either side would keep analyzing and quietly change
/// what a value means; the name lists have to match entry for entry. The
/// Kotlin `WireEnumMappingTest` pins the same contract on the host side.
void main() {
  group('enum ordering', () {
    test('every enum matches its wire counterpart entry for entry', () {
      expect(NepaliLanguage.values.map((value) => value.name),
          LangDto.values.map((value) => value.name));
      expect(NameFormat.values.map((value) => value.name),
          NameFormatDto.values.map((value) => value.name));
      expect(NepaliDateFormatStyle.values.map((value) => value.name),
          DateFormatStyleDto.values.map((value) => value.name));
      expect(DigitScript.values.map((value) => value.name),
          DigitScriptDto.values.map((value) => value.name));
      expect(NepaliEventKind.values.map((value) => value.name),
          EventKindDto.values.map((value) => value.name));
      expect(DatePattern.values.map((value) => value.name),
          DatePatternDto.values.map((value) => value.name));
    });

    test('the converters pick the counterpart at the same index', () {
      expect(langToDto(NepaliLanguage.nepali), LangDto.nepali);
      expect(nameFormatToDto(NameFormat.short), NameFormatDto.short);
      expect(dateFormatStyleToDto(NepaliDateFormatStyle.compactYmd),
          DateFormatStyleDto.compactYmd);
      expect(digitScriptToDto(DigitScript.devanagari),
          DigitScriptDto.devanagari);
      for (final kind in NepaliEventKind.values) {
        expect(eventKindFromDto(eventKindToDto(kind)), kind);
      }
    });
  });

  group('value mapping', () {
    test('a date and a time keep every component both ways', () {
      const date = SimpleDate(2082, 6, 4);
      expect(dateFromDto(dateToDto(date)), date);

      const time =
          SimpleTime(hour: 16, minute: 30, second: 15, nanosecond: 500);
      expect(timeFromDto(timeToDto(time)), time);
    });

    test('a calendar survives the round trip field for field', () {
      const calendar = NepaliDate(
        year: 2082,
        month: 6,
        dayOfMonth: 4,
        era: 2,
        firstDayOfMonth: 3,
        lastDayOfMonth: 4,
        totalDaysInMonth: 30,
        dayOfWeekInMonth: 1,
        dayOfWeek: 6,
        dayOfYear: 160,
        weekOfMonth: 1,
        weekOfYear: 24,
      );

      expect(calendarFromDto(calendarToDto(calendar)), calendar);
    });

    test('month metadata and a year range arrive whole', () {
      final info = monthInfoFromDto(MonthInfoDto(
        year: 2082,
        month: 6,
        totalDaysInMonth: 30,
        firstDayOfMonth: 3,
        lastDayOfMonth: 4,
        daysFromStartOfWeekToFirstOfMonth: 2,
      ));

      expect(info.totalDaysInMonth, 30);
      expect(info.daysFromStartOfWeekToFirstOfMonth, 2);
      expect(yearRangeFromDto(YearRangeDto(first: 1970, last: 2100)),
          const YearRange(1970, 2100));
    });

    test('a locale sends its axes and leaves an unset script null', () {
      final locale = localeToDto(const NepaliDateLocale(
        language: NepaliLanguage.nepali,
        dateFormat: NepaliDateFormatStyle.shortYmd,
        weekDayName: NameFormat.medium,
        monthName: NameFormat.short,
      ));

      expect(locale.language, LangDto.nepali);
      expect(locale.dateFormat, DateFormatStyleDto.shortYmd);
      expect(locale.weekDayName, NameFormatDto.medium);
      expect(locale.monthName, NameFormatDto.short);
      expect(locale.digitScript, isNull);
      expect(
          localeToDto(const NepaliDateLocale(digitScript: DigitScript.latin))
              .digitScript,
          DigitScriptDto.latin);
    });

    test('an event keeps its identity and payload both ways', () {
      const event = NepaliEvent(
        year: 2082,
        month: 6,
        dayOfMonth: 24,
        name: 'Vijaya Dashami',
        kind: NepaliEventKind.governmentPublic,
        id: 'dashami',
        payload: '{"note":"tenth day"}',
      );

      final round = eventFromDto(eventToDto(event));
      expect(round.name, 'Vijaya Dashami');
      expect(round.kind, NepaliEventKind.governmentPublic);
      expect(round.closesOffices, isTrue);
      expect(round.id, 'dashami');
      expect(round.payload, '{"note":"tenth day"}');
    });

    test('a day status arrives with unmodifiable lists', () {
      final status = dayStatusFromDto(DayStatusDto(
        isWeeklyOff: true,
        isNonWorking: true,
        primaryKind: EventKindDto.religious,
        names: const <String>['Vijaya Dashami'],
        events: <EventDto>[
          EventDto(
            year: 2082,
            month: 6,
            dayOfMonth: 24,
            name: 'Vijaya Dashami',
            kind: EventKindDto.religious,
            closesOffices: true,
          ),
        ],
        closures: const <EventDto>[],
      ));

      expect(status.isWeeklyOff, isTrue);
      expect(status.primaryKind, NepaliEventKind.religious);
      expect(status.names.single, 'Vijaya Dashami');
      expect(status.events.single.kind, NepaliEventKind.religious);
      expect(() => status.names.add('x'), throwsUnsupportedError);
      expect(() => status.events.clear(), throwsUnsupportedError);
    });

    test('a status without a leading kind keeps it null', () {
      final status = dayStatusFromDto(DayStatusDto(
        isWeeklyOff: false,
        isNonWorking: false,
        names: const <String>[],
        events: const <EventDto>[],
        closures: const <EventDto>[],
      ));

      expect(status.primaryKind, isNull);
      expect(status.isNonWorking, isFalse);
    });
  });
}
