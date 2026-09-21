// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/pickers/platform_picker_view.dart';

/// Pins the Dart half of the embedded picker's creation-parameter contract:
/// the key names, the positional entries and what an absent value becomes.
/// The Kotlin `PickerParamsTest` decodes the same shape on the host side.
void main() {
  test('an unconfigured picker sends nulls rather than invented defaults', () {
    final params = buildPickerCreationParams(variant: 'datePicker');

    expect(params['variant'], 'datePicker');
    expect(params['initialDate'], isNull);
    expect(params['initialEndDate'], isNull);
    expect(params['yearRangeStart'], isNull);
    expect(params['yearRangeEnd'], isNull);
    expect(params['selectable'], isNull);
    expect(params['events'], isNull);
    expect(params['locale'], <int>[0, 1, 0, 0, -1]);
  });

  test('dates and the year range travel as plain numbers', () {
    final params = buildPickerCreationParams(
      variant: 'rangePicker',
      initialDate: const SimpleDate(2082, 6, 4),
      initialEndDate: const SimpleDate(2082, 6, 20),
      yearRange: const YearRange(2080, 2090),
    );

    expect(params['initialDate'], <int>[2082, 6, 4]);
    expect(params['initialEndDate'], <int>[2082, 6, 20]);
    expect(params['yearRangeStart'], 2080);
    expect(params['yearRangeEnd'], 2090);
  });

  test('the locale entry lists its axes in the order the host reads them', () {
    final entry = localeCreationEntry(const NepaliDateLocale(
      language: NepaliLanguage.nepali,
      dateFormat: NepaliDateFormatStyle.shortMdy,
      weekDayName: NameFormat.short,
      monthName: NameFormat.full,
      digitScript: DigitScript.devanagari,
    ));

    expect(entry, <int>[1, 3, 2, 0, 1]);
  });

  test('a selectable rule sends its bounds, weekend and closure policy', () {
    final params = buildPickerCreationParams(
      variant: 'datePicker',
      selectableDates: NepaliSelectableDates(
        minDate: const SimpleDate(2082, 6, 1),
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
    );

    final selectable = params['selectable']! as Map<String, Object?>;
    expect(selectable['minDate'], <int>[2082, 6, 1]);
    expect(selectable['maxDate'], isNull);
    expect(selectable['includeMinDate'], isTrue);
    expect(selectable['includeMaxDate'], isFalse);
    expect(selectable['excludeWeekend'], <int>[7]);
    expect(selectable['policyWeeklyOffDays'], <int>[7]);
    expect(selectable['policyEvents'],
        <List<Object?>>[
          <Object?>[2082, 6, 24, 'Vijaya Dashami', 0, true]
        ]);
  });

  test('events send their rows, marking switches and colors', () {
    final params = buildPickerCreationParams(
      variant: 'datePicker',
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
            id: 'meeting-42',
            payload: '{"room":"3B"}',
          ),
        ],
        markWeeklyOff: false,
        tintContainer: true,
        religiousColor: Color(0xFF1E88E5),
      ),
    );

    final events = params['events']! as Map<String, Object?>;
    expect(events['weeklyOffDays'], <int>[7, 1]);
    // The app's own handles ride along untouched, which is what a tapped entry
    // hands back.
    expect(events['events'], <List<Object?>>[
      <Object?>[
        2082,
        6,
        9,
        'Staff meeting',
        3,
        false,
        0xFF8E24AA,
        true,
        'meeting-42',
        '{"room":"3B"}',
      ]
    ]);
    expect(events['markWeeklyOff'], isFalse);
    expect(events['markEvents'], isTrue);
    expect(events['tintContainer'], isTrue);
    expect(events['religiousColorArgb'], 0xFF1E88E5);
    expect(events['regionalColorArgb'], 0);
    expect(events['markedContainerColorArgb'], 0);
  });

  test('a variant adds its own keys without losing the shared ones', () {
    final params = buildPickerCreationParams(
      variant: 'wheelPicker',
      extras: const <String, Object?>{'itemHeight': 32.0, 'visibleItemCount': 3},
    );

    expect(params['variant'], 'wheelPicker');
    expect(params['itemHeight'], 32.0);
    expect(params['visibleItemCount'], 3);
    expect(params.containsKey('locale'), isTrue);
  });
}
