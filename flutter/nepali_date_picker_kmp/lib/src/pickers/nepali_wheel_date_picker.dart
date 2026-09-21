// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/locale.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 280;

// Headroom above the wheel rows for the calendar-system toggle and padding.
const double _measurementHeadroom = 160;

/// The wheel (spinner) picker, rendered natively and embedded as a platform
/// view.
///
/// The wheel always has a selection, so [onDateChange] fires with a full
/// calendar for the initial date and for every spin after it.
class NepaliWheelDatePicker extends StatelessWidget {
  const NepaliWheelDatePicker({
    super.key,
    this.initialDate,
    this.locale = const NepaliDateLocale(),
    this.yearRange,
    this.selectableDates,
    this.itemHeight = 44,
    this.visibleItemCount = 5,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showCalendarSystemToggle = false,
    required this.onDateChange,
  });

  /// The date the wheel starts on; null starts on today.
  final SimpleDate? initialDate;

  final NepaliDateLocale locale;
  final YearRange? yearRange;
  final NepaliSelectableDates? selectableDates;

  /// One wheel row's height in logical pixels.
  final double itemHeight;

  final int visibleItemCount;
  final CalendarSystem initialCalendarSystem;
  final bool showCalendarSystemToggle;
  final ValueChanged<NepaliDate> onDateChange;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'wheel',
          initialDate: initialDate,
          locale: locale,
          yearRange: yearRange,
          selectableDates: selectableDates,
          extras: <String, Object?>{
            'itemHeight': itemHeight,
            'visibleItemCount': visibleItemCount,
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'showCalendarSystemToggle': showCalendarSystemToggle,
          },
        ),
        callbacks: PickerViewCallbacks(
          onDateSelected: (date) {
            if (date != null) onDateChange(date);
          },
        ),
        fallbackHeight: _fallbackHeight,
        measurementHeight: itemHeight * visibleItemCount + _measurementHeadroom,
      );
}
