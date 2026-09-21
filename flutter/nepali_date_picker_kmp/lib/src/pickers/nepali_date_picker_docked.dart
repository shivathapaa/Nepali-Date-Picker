// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/locale.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 64;

// The frame the native view is measured in; the field with its calendar popup open is the tallest mode.
const double _measurementHeight = 600;

/// The docked variant: a text field that opens the calendar in a popup
/// anchored under it, rendered natively and embedded as a platform view.
///
/// The reported height grows while the popup is open and the widget resizes
/// with it. [label] and [placeholder] are plain strings because the native
/// side has no composable slots to fill.
class NepaliDatePickerDocked extends StatelessWidget {
  const NepaliDatePickerDocked({
    super.key,
    this.initialSelectedDate,
    this.locale = const NepaliDateLocale(),
    this.yearRange,
    this.selectableDates,
    this.dateFormatStyle = NepaliDateFormatStyle.medium,
    this.showTodayButton = true,
    this.label,
    this.placeholder,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showCalendarSystemToggle = false,
    this.showAdjacentMonthDays = false,
    this.events,
    required this.onDateSelected,
  });

  final SimpleDate? initialSelectedDate;
  final NepaliDateLocale locale;
  final YearRange? yearRange;
  final NepaliSelectableDates? selectableDates;

  /// How the selected date reads inside the field.
  final NepaliDateFormatStyle dateFormatStyle;

  final bool showTodayButton;
  final String? label;
  final String? placeholder;
  final CalendarSystem initialCalendarSystem;
  final bool showCalendarSystemToggle;
  final bool showAdjacentMonthDays;
  final NepaliPickerEventOptions? events;
  final ValueChanged<NepaliDate?> onDateSelected;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'docked',
          initialDate: initialSelectedDate,
          locale: locale,
          yearRange: yearRange,
          selectableDates: selectableDates,
          events: events,
          extras: <String, Object?>{
            'dateFormatStyle': dateFormatStyle.index,
            'showTodayButton': showTodayButton,
            'label': label,
            'placeholder': placeholder,
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
