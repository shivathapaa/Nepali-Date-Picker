// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/pickers/platform_picker_view.dart';

/// Pins what each picker widget hands the shared platform host: the variant
/// that selects the native factory, the keys its own options travel under,
/// the two heights it is drawn and measured at, and the callback the native
/// side's event reaches.
///
/// The widgets are read rather than rendered, so this runs without a platform
/// view; `platform_picker_view_test.dart` covers the hosting itself.
void main() {
  Future<NepaliPlatformPickerView> host(
      WidgetTester tester, Widget picker) async {
    await tester.pumpWidget(MaterialApp(home: Scaffold(body: picker)));
    return tester.widget<NepaliPlatformPickerView>(
        find.byType(NepaliPlatformPickerView));
  }

  testWidgets('the calendar picker sends its chrome switches', (tester) async {
    NepaliDate? selected;
    final view = await host(
      tester,
      NepaliDatePicker(
        initialSelectedDate: const SimpleDate(2082, 6, 4),
        showModeToggle: false,
        showTodayButton: false,
        showEnglishDate: true,
        englishDateLocale: const NepaliDateLocale(
            language: NepaliLanguage.english,
            dateFormat: NepaliDateFormatStyle.shortYmd),
        initialCalendarSystem: CalendarSystem.gregorian,
        showCalendarSystemToggle: true,
        showAdjacentMonthDays: true,
        onDateSelected: (date) => selected = date,
      ),
    );

    final params = view.creationParams;
    expect(params['variant'], 'datePicker');
    expect(params['initialDate'], <int>[2082, 6, 4]);
    expect(params['showModeToggle'], isFalse);
    expect(params['showTodayButton'], isFalse);
    expect(params['showEnglishDate'], isTrue);
    expect(params['englishLocale'], <int>[0, 4, 0, 0, -1]);
    expect(params['initialCalendarSystemEra'], 1);
    expect(params['showCalendarSystemToggle'], isTrue);
    expect(params['showAdjacentMonthDays'], isTrue);
    expect(view.fallbackHeight, 512);
    expect(view.measurementHeight, 600);

    view.callbacks.onDateSelected!(null);
    expect(selected, isNull);
  });

  testWidgets('the docked picker sends its field chrome', (tester) async {
    NepaliDate? selected;
    final view = await host(
      tester,
      NepaliDatePickerDocked(
        dateFormatStyle: NepaliDateFormatStyle.compactMdy,
        showTodayButton: false,
        label: 'Joined',
        placeholder: 'YYYY/MM/DD',
        onDateSelected: (date) => selected = date,
      ),
    );

    final params = view.creationParams;
    expect(params['variant'], 'docked');
    expect(params['dateFormatStyle'], NepaliDateFormatStyle.compactMdy.index);
    expect(params['showTodayButton'], isFalse);
    expect(params['label'], 'Joined');
    expect(params['placeholder'], 'YYYY/MM/DD');
    expect(view.fallbackHeight, 64);
    expect(view.measurementHeight, 600);

    view.callbacks.onDateSelected!(const NepaliDate(
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
    ));
    expect(selected?.dayOfMonth, 4);
  });

  testWidgets('the wheel measures the rows it was asked to show',
      (tester) async {
    NepaliDate? changed;
    final view = await host(
      tester,
      NepaliWheelDatePicker(
        itemHeight: 32,
        visibleItemCount: 3,
        onDateChange: (date) => changed = date,
      ),
    );

    expect(view.creationParams['variant'], 'wheel');
    expect(view.creationParams['itemHeight'], 32.0);
    expect(view.creationParams['visibleItemCount'], 3);
    expect(view.fallbackHeight, 280);
    expect(view.measurementHeight, 32 * 3 + 160);

    view.callbacks.onDateSelected!(null);
    expect(changed, isNull, reason: 'a cleared wheel selection is not a date');
  });

  testWidgets('the range picker sends its layout switches', (tester) async {
    NepaliDate? start;
    NepaliDate? end;
    final view = await host(
      tester,
      NepaliDateRangePicker(
        initialSelectedStartDate: const SimpleDate(2082, 6, 1),
        initialSelectedEndDate: const SimpleDate(2082, 6, 20),
        showMonthsVertically: false,
        showYearPickerAndMonthNavigation: false,
        onRangeSelected: (from, to) {
          start = from;
          end = to;
        },
      ),
    );

    final params = view.creationParams;
    expect(params['variant'], 'rangePicker');
    expect(params['initialDate'], <int>[2082, 6, 1]);
    expect(params['initialEndDate'], <int>[2082, 6, 20]);
    expect(params['showMonthsVertically'], isFalse);
    expect(params['showYearPickerAndMonthNavigation'], isFalse);
    expect(view.fallbackHeight, 560);
    expect(view.measurementHeight, 600);

    view.callbacks.onRangeSelected!(null, null);
    expect(start, isNull);
    expect(end, isNull);
  });

  testWidgets('the date field sends its text-field state', (tester) async {
    SimpleDate? value;
    final view = await host(
      tester,
      NepaliDateField(
        initialValue: const SimpleDate(2082, 6, 4),
        dateFormat: DatePattern.ddDashMmDashYyyy,
        outlined: false,
        label: 'Joined',
        supportingText: 'Bikram Sambat',
        isError: true,
        enabled: false,
        readOnly: true,
        confirmButtonText: 'ठिक छ',
        dismissButtonText: 'रद्द',
        onValueChanged: (date) => value = date,
      ),
    );

    final params = view.creationParams;
    expect(params['variant'], 'dateField');
    expect(params['dateFormat'], DatePattern.ddDashMmDashYyyy.index);
    expect(params['outlined'], isFalse);
    expect(params['label'], 'Joined');
    expect(params['supportingText'], 'Bikram Sambat');
    expect(params['isError'], isTrue);
    expect(params['enabled'], isFalse);
    expect(params['readOnly'], isTrue);
    expect(params['confirmButtonText'], 'ठिक छ');
    expect(params['dismissButtonText'], 'रद्द');
    expect(view.fallbackHeight, 72);
    expect(view.measurementHeight, 240);

    view.callbacks.onValueChanged!(const SimpleDate(2082, 6, 5));
    expect(value, const SimpleDate(2082, 6, 5));
  });

  testWidgets('the range field errors on each end separately', (tester) async {
    SimpleDate? start;
    SimpleDate? end;
    final view = await host(
      tester,
      NepaliDateRangeField(
        initialStartValue: const SimpleDate(2082, 6, 1),
        initialEndValue: const SimpleDate(2082, 6, 20),
        startLabel: 'From',
        endLabel: 'To',
        isStartError: false,
        isEndError: true,
        onRangeChanged: (from, to) {
          start = from;
          end = to;
        },
      ),
    );

    final params = view.creationParams;
    expect(params['variant'], 'rangeField');
    expect(params['startLabel'], 'From');
    expect(params['endLabel'], 'To');
    expect(params['isStartError'], isFalse);
    expect(params['isEndError'], isTrue);
    expect(view.fallbackHeight, 72);
    expect(view.measurementHeight, 320);

    view.callbacks.onRangeValueChanged!(
        const SimpleDate(2082, 6, 2), const SimpleDate(2082, 6, 21));
    expect(start, const SimpleDate(2082, 6, 2));
    expect(end, const SimpleDate(2082, 6, 21));
  });

  testWidgets('the calendar sends its wall-patro defaults', (tester) async {
    final view = await host(tester, const NepaliCalendar());

    final params = view.creationParams;
    expect(params['variant'], 'calendar');
    // A calendar reads against the Gregorian one and fills its corners, where
    // a picker sized for a dialog does neither.
    expect(params['showSecondaryDates'], isTrue);
    expect(params['showAdjacentMonthDays'], isTrue);
    expect(params['showTodayButton'], isTrue);
    expect(params['showCalendarSystemToggle'], isFalse);
    expect(params['showDaySummary'], isFalse);
    expect(params['showMonthEvents'], isFalse);
    expect(params['initialCalendarSystemEra'], 2);
    expect(view.fallbackHeight, 420);
    // The stacked lists grow past the grid's own fixed height.
    expect(view.measurementHeight, 1400);
  });

  testWidgets('the calendar sends the surfaces stacked under it',
      (tester) async {
    NepaliDate? selected;
    NepaliEvent? tapped;
    final view = await host(
      tester,
      NepaliCalendar(
        initialSelectedDate: const SimpleDate(2082, 6, 4),
        showSecondaryDates: false,
        secondaryDateLocale: const NepaliDateLocale(
            language: NepaliLanguage.english,
            dateFormat: NepaliDateFormatStyle.shortYmd),
        showDaySummary: true,
        showMonthEvents: true,
        initialCalendarSystem: CalendarSystem.gregorian,
        onDaySelected: (date) => selected = date,
        onEventTapped: (event) => tapped = event,
      ),
    );

    final params = view.creationParams;
    expect(params['initialDate'], <int>[2082, 6, 4]);
    expect(params['showSecondaryDates'], isFalse);
    expect(params['englishLocale'], <int>[0, 4, 0, 0, -1]);
    expect(params['showDaySummary'], isTrue);
    expect(params['showMonthEvents'], isTrue);
    expect(params['initialCalendarSystemEra'], 1);

    view.callbacks.onEventTapped!(const NepaliEvent(
      year: 2082,
      month: 6,
      dayOfMonth: 17,
      name: 'Dashain',
      kind: NepaliEventKind.religious,
      closesOffices: true,
      id: 'dashain-2082',
    ));
    expect(tapped?.id, 'dashain-2082');

    // A cleared selection never reaches a calendar's day callback: a calendar
    // reports the day that was tapped, and nothing is a tap.
    view.callbacks.onDateSelected!(null);
    expect(selected, isNull);
  });

  testWidgets('the calendar toggle reports a system, not a raw era',
      (tester) async {
    CalendarSystem? system;
    final view = await host(
      tester,
      NepaliCalendarSystemToggle(
        initialCalendarSystem: CalendarSystem.gregorian,
        language: NepaliLanguage.nepali,
        onCalendarSystemChanged: (value) => system = value,
      ),
    );

    final params = view.creationParams;
    expect(params['variant'], 'calendarSystemToggle');
    expect(params['initialCalendarSystemEra'], 1);
    expect(params['language'], NepaliLanguage.nepali.index);
    expect(view.fallbackHeight, 48);
    expect(view.measurementHeight, 140);

    view.callbacks.onCalendarSystemChanged!(2);
    expect(system, CalendarSystem.bikramSambat);

    view.callbacks.onCalendarSystemChanged!(9);
    expect(system, CalendarSystem.bikramSambat,
        reason: 'an era the shared convention does not name is ignored');
  });
}
