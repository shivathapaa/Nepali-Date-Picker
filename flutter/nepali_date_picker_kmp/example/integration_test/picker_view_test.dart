// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// Proves the embedded Compose surfaces actually compose inside a Flutter
// activity: the wheel reports its initial selection, and the calendar reports
// a tapped day, through the callback channel only after the native view has
// measured and rendered.

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('an embedded wheel composes and reports through the bridge',
      (tester) async {
    final firstChange = Completer<NepaliDate>();

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: NepaliWheelDatePicker(
            onDateChange: (date) {
              if (!firstChange.isCompleted) firstChange.complete(date);
            },
          ),
        ),
      ),
    );

    NepaliDate? reported;
    for (var attempt = 0; attempt < 100 && !firstChange.isCompleted; attempt++) {
      await tester.pump(const Duration(milliseconds: 100));
    }
    if (firstChange.isCompleted) reported = await firstChange.future;

    expect(reported, isNotNull,
        reason: 'the native wheel never reported a selection, so the '
            'Compose view did not reach composition');
    expect(reported!.era, 2);
    final years = await NepaliDateConverter.bsYearRange();
    expect(years.contains(reported.year), isTrue);
  });

  testWidgets('an embedded calendar reports the day it is given',
      (tester) async {
    final firstDay = Completer<NepaliDate>();

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: SingleChildScrollView(
            child: NepaliCalendar(
              initialSelectedDate: const SimpleDate(2082, 6, 4),
              showDaySummary: true,
              onDaySelected: (date) {
                if (!firstDay.isCompleted) firstDay.complete(date);
              },
            ),
          ),
        ),
      ),
    );

    // The calendar reports a day only when one is tapped, so the native grid
    // has to have laid out before the tap can land on it.
    for (var attempt = 0; attempt < 30; attempt++) {
      await tester.pump(const Duration(milliseconds: 100));
    }
    final grid = find.byType(NepaliCalendar);
    expect(grid, findsOneWidget);
    final bounds = tester.getRect(grid);
    // A day near the middle of the grid, below the navigation and weekday
    // rows, which every month has a cell at.
    await tester.tapAt(Offset(bounds.center.dx, bounds.top + bounds.height * 0.4));

    NepaliDate? reported;
    for (var attempt = 0; attempt < 50 && !firstDay.isCompleted; attempt++) {
      await tester.pump(const Duration(milliseconds: 100));
    }
    if (firstDay.isCompleted) reported = await firstDay.future;

    expect(reported, isNotNull,
        reason: 'the native calendar never reported a tapped day, so the '
            'Compose grid did not reach composition');
    expect(reported!.era, 2);
    final years = await NepaliDateConverter.bsYearRange();
    expect(years.contains(reported.year), isTrue);
  });
}
