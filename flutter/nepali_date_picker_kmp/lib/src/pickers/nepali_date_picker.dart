// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/locale.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 512;

// The frame the native view is measured in; the calendar with the English-date headline is the tallest mode.
const double _measurementHeight = 600;

/// The Material3 Nepali calendar picker, rendered by the native Compose
/// implementation and embedded as a platform view.
///
/// The widget sizes itself to the height the picker reports, so it can sit
/// inside any layout. [onDateSelected] fires with the full calendar for
/// every selection, and with null when the selection is cleared.
class NepaliDatePicker extends StatelessWidget {
  const NepaliDatePicker({
    super.key,
    this.initialSelectedDate,
    this.locale = const NepaliDateLocale(),
    this.yearRange,
    this.selectableDates,
    this.showModeToggle = true,
    this.showTodayButton = true,
    this.showEnglishDate = false,
    this.englishDateLocale,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showCalendarSystemToggle = false,
    this.showAdjacentMonthDays = false,
    this.events,
    required this.onDateSelected,
  });

  final SimpleDate? initialSelectedDate;
  final NepaliDateLocale locale;

  /// The selectable Bikram Sambat years; null uses the engine's full range.
  final YearRange? yearRange;

  final NepaliSelectableDates? selectableDates;
  final bool showModeToggle;
  final bool showTodayButton;

  /// Also shows each day's Gregorian date inside the grid.
  final bool showEnglishDate;

  final NepaliDateLocale? englishDateLocale;
  final CalendarSystem initialCalendarSystem;
  final bool showCalendarSystemToggle;
  final bool showAdjacentMonthDays;
  final NepaliPickerEventOptions? events;
  final ValueChanged<NepaliDate?> onDateSelected;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'datePicker',
          initialDate: initialSelectedDate,
          locale: locale,
          yearRange: yearRange,
          selectableDates: selectableDates,
          events: events,
          extras: <String, Object?>{
            'showModeToggle': showModeToggle,
            'showTodayButton': showTodayButton,
            'showEnglishDate': showEnglishDate,
            'englishLocale': englishDateLocale == null
                ? null
                : localeCreationEntry(englishDateLocale!),
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'showCalendarSystemToggle': showCalendarSystemToggle,
            'showAdjacentMonthDays': showAdjacentMonthDays,
          },
        ),
        callbacks: PickerViewCallbacks(onDateSelected: onDateSelected),
        fallbackHeight: _fallbackHeight,
        measurementHeight: _measurementHeight,
      );
}
