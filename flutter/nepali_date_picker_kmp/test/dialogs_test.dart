// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'dart:ui';

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'recording_engine_api.dart';
import 'recording_host_api.dart';

/// Pins the native dialog configuration: the defaults a caller gets, the
/// values that reach the host, and the date a confirmation comes back as.
void main() {
  late RecordingEngineApi engine;
  late RecordingHostApi host;

  setUp(() {
    engine = RecordingEngineApi();
    bridge.engineApi = engine;
    host = RecordingHostApi(confirmed: RecordingHostApi.calendar);
    bridge.pickerHostApi = host;
    NepaliDateValidation.debugResetCaches();
  });

  test('an unconfigured dialog fills the published year range', () async {
    await showNepaliDatePickerDialog();

    final config = host.dialogs.single;
    expect(config.yearRangeStart, 1970);
    expect(config.yearRangeEnd, 2100);
    expect(config.initialSelectedDate, isNull);
    expect(config.locale.language, LangDto.english);
    expect(config.selectable, isNull);
    expect(config.eventOptions, isNull);
    expect(config.title, isNull);
    expect(config.confirmText, 'OK');
    expect(config.dismissText, 'Cancel');
    expect(config.tonalElevation, 6);
    expect(config.cornerRadius, 28);
    expect(config.initialCalendarSystemEra, 2);
    expect(config.showModeToggle, isTrue);
    expect(config.showTodayButton, isTrue);
    expect(config.showEnglishDate, isFalse);
    expect(config.showCalendarSystemToggle, isFalse);
    expect(config.showAdjacentMonthDays, isFalse);
  });

  test('a configured dialog carries every choice across', () async {
    await showNepaliDatePickerDialog(
      initialSelectedDate: const SimpleDate(2082, 6, 4),
      locale: const NepaliDateLocale(language: NepaliLanguage.nepali),
      yearRange: const YearRange(2080, 2090),
      showModeToggle: false,
      showTodayButton: false,
      showEnglishDate: true,
      englishDateLocale: const NepaliDateLocale(
          dateFormat: NepaliDateFormatStyle.shortYmd),
      initialCalendarSystem: CalendarSystem.gregorian,
      showCalendarSystemToggle: true,
      showAdjacentMonthDays: true,
      confirmText: 'ठिक छ',
      dismissText: 'रद्द',
      tonalElevation: 0,
      cornerRadius: 4,
    );

    final config = host.dialogs.single;
    expect(config.initialSelectedDate?.dayOfMonth, 4);
    expect(config.locale.language, LangDto.nepali);
    expect(config.yearRangeStart, 2080);
    expect(config.yearRangeEnd, 2090);
    expect(config.showModeToggle, isFalse);
    expect(config.showTodayButton, isFalse);
    expect(config.showEnglishDate, isTrue);
    expect(config.englishDateLocale?.dateFormat, DateFormatStyleDto.shortYmd);
    expect(config.initialCalendarSystemEra, 1);
    expect(config.showCalendarSystemToggle, isTrue);
    expect(config.showAdjacentMonthDays, isTrue);
    expect(config.confirmText, 'ठिक छ');
    expect(config.dismissText, 'रद्द');
    expect(config.tonalElevation, 0);
    expect(config.cornerRadius, 4);
  });

  test('a selectable rule and events cross as wire descriptions', () async {
    await showNepaliDatePickerDialog(
      selectableDates: NepaliSelectableDates(
        minDate: const SimpleDate(2082, 6, 1),
        maxDate: const SimpleDate(2082, 6, 20),
        includeMinDate: true,
        excludeWeekend: const {7},
        excludeClosuresOf: NepaliCalendarPolicy(
          weeklyOffDays: const {7},
          events: const [
            NepaliEvent(
              year: 2082,
              month: 6,
              dayOfMonth: 24,
              name: 'Vijaya Dashami',
              kind: NepaliEventKind.governmentPublic,
            ),
          ],
        ),
      ),
      events: const NepaliPickerEventOptions(
        weeklyOffDays: <int>[7, 1],
        events: <NepaliPickerEvent>[
          NepaliPickerEvent(
            year: 2082,
            month: 6,
            dayOfMonth: 9,
            name: 'Staff meeting',
            kind: NepaliEventKind.observance,
            color: Color(0xFF8E24AA),
            indicate: true,
          ),
        ],
        tintContainer: true,
        religiousColor: Color(0xFF1E88E5),
      ),
    );

    final config = host.dialogs.single;
    final selectable = config.selectable!;
    expect(selectable.minDate?.dayOfMonth, 1);
    expect(selectable.maxDate?.dayOfMonth, 20);
    expect(selectable.includeMinDate, isTrue);
    expect(selectable.includeMaxDate, isFalse);
    expect(selectable.excludeWeekend, <int>[7]);
    expect(selectable.excludeClosuresOf?.events.single.name, 'Vijaya Dashami');

    final events = config.eventOptions!;
    expect(events.weeklyOffDays, <int>[7, 1]);
    expect(events.tintContainer, isTrue);
    expect(events.religiousColorArgb, 0xFF1E88E5);
    expect(events.regionalColorArgb, 0);
    final mark = events.events.single;
    expect(mark.name, 'Staff meeting');
    expect(mark.kind, EventKindDto.observance);
    expect(mark.closesOffices, isFalse);
    expect(mark.colorArgb, 0xFF8E24AA);
    expect(mark.indicate, isTrue);
  });

  test('the full-screen dialog drops the dialog chrome and takes a title',
      () async {
    await showNepaliDatePickerFullScreenDialog(title: 'Pick a date');

    expect(host.dialogs, isEmpty);
    final config = host.fullScreenDialogs.single;
    expect(config.title, 'Pick a date');
    expect(config.tonalElevation, 0);
    expect(config.cornerRadius, 0);
  });

  test('a confirmation becomes a calendar and a dismissal becomes null',
      () async {
    expect((await showNepaliDatePickerDialog())?.year, 2082);

    bridge.pickerHostApi = RecordingHostApi();
    expect(await showNepaliDatePickerDialog(), isNull);
    expect(await showNepaliDatePickerFullScreenDialog(), isNull);
  });

  test('an initial date outside the table never opens a dialog', () async {
    await expectLater(
        showNepaliDatePickerDialog(
            initialSelectedDate: const SimpleDate(2101, 1, 1)),
        throwsArgumentError);
    expect(host.dialogs, isEmpty);
  });
}
