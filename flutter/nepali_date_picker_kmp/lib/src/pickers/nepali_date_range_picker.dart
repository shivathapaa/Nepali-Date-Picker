// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/locale.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 560;

// The frame the native view is measured in; the vertically scrolling calendar fills and scrolls inside it.
const double _measurementHeight = 600;

/// The Material3 Nepali range picker, rendered natively and embedded as a
/// platform view.
///
/// [onRangeSelected] fires whenever either endpoint changes; an incomplete
/// range reports null for the missing end.
class NepaliDateRangePicker extends StatelessWidget {
  const NepaliDateRangePicker({
    super.key,
    this.initialSelectedStartDate,
    this.initialSelectedEndDate,
    this.locale = const NepaliDateLocale(),
    this.yearRange,
    this.selectableDates,
    this.showModeToggle = true,
    this.showTodayButton = true,
    this.showMonthsVertically = true,
    this.showYearPickerAndMonthNavigation = true,
    this.showEnglishDate = false,
    this.englishDateLocale,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showCalendarSystemToggle = false,
    this.showAdjacentMonthDays = false,
    this.events,
    required this.onRangeSelected,
  });

  final SimpleDate? initialSelectedStartDate;
  final SimpleDate? initialSelectedEndDate;
  final NepaliDateLocale locale;
  final YearRange? yearRange;
  final NepaliSelectableDates? selectableDates;
  final bool showModeToggle;
  final bool showTodayButton;

  /// Scroll months vertically (the Material default) or page horizontally.
  final bool showMonthsVertically;

  final bool showYearPickerAndMonthNavigation;
  final bool showEnglishDate;
  final NepaliDateLocale? englishDateLocale;
  final CalendarSystem initialCalendarSystem;
  final bool showCalendarSystemToggle;
  final bool showAdjacentMonthDays;
  final NepaliPickerEventOptions? events;
  final void Function(NepaliDate? start, NepaliDate? end) onRangeSelected;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'rangePicker',
          initialDate: initialSelectedStartDate,
          initialEndDate: initialSelectedEndDate,
          locale: locale,
          yearRange: yearRange,
          selectableDates: selectableDates,
          events: events,
          extras: <String, Object?>{
            'showModeToggle': showModeToggle,
            'showTodayButton': showTodayButton,
            'showMonthsVertically': showMonthsVertically,
            'showYearPickerAndMonthNavigation': showYearPickerAndMonthNavigation,
            'showEnglishDate': showEnglishDate,
            'englishLocale': englishDateLocale == null
                ? null
                : localeCreationEntry(englishDateLocale!),
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'showCalendarSystemToggle': showCalendarSystemToggle,
            'showAdjacentMonthDays': showAdjacentMonthDays,
          },
        ),
        callbacks: PickerViewCallbacks(onRangeSelected: onRangeSelected),
        fallbackHeight: _fallbackHeight,
        measurementHeight: _measurementHeight,
      );
}
