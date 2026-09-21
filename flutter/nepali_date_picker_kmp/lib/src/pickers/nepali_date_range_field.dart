// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/locale.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import '../wire_format.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 72;

// The frame the native view is measured in; both fields with labels, errors and supporting text is the tallest mode.
const double _measurementHeight = 320;

/// The native start and end date fields, with an optional range picker
/// dialog behind them, embedded as a platform view.
///
/// [outlined] chooses the bare pair of text fields; the filled variant opens
/// the range calendar dialog. [onRangeChanged] fires with the plain dates as
/// they are typed or picked, null for a side that is empty or unparseable.
class NepaliDateRangeField extends StatelessWidget {
  const NepaliDateRangeField({
    super.key,
    this.initialStartValue,
    this.initialEndValue,
    this.locale = const NepaliDateLocale(),
    this.dateFormat = DatePattern.yyyySlashMmSlashDd,
    this.yearRange,
    this.selectableDates,
    this.outlined = true,
    this.startLabel,
    this.endLabel,
    this.supportingText,
    this.isStartError = false,
    this.isEndError = false,
    this.enabled = true,
    this.readOnly = false,
    this.confirmButtonText,
    this.dismissButtonText,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showCalendarSystemToggle = false,
    this.showAdjacentMonthDays = false,
    this.events,
    required this.onRangeChanged,
  });

  final SimpleDate? initialStartValue;
  final SimpleDate? initialEndValue;
  final NepaliDateLocale locale;
  final DatePattern dateFormat;
  final YearRange? yearRange;
  final NepaliSelectableDates? selectableDates;
  final bool outlined;
  final String? startLabel;
  final String? endLabel;
  final String? supportingText;
  final bool isStartError;
  final bool isEndError;
  final bool enabled;
  final bool readOnly;
  final String? confirmButtonText;
  final String? dismissButtonText;
  final CalendarSystem initialCalendarSystem;
  final bool showCalendarSystemToggle;
  final bool showAdjacentMonthDays;
  final NepaliPickerEventOptions? events;
  final void Function(SimpleDate? start, SimpleDate? end) onRangeChanged;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'rangeField',
          initialDate: initialStartValue,
          initialEndDate: initialEndValue,
          locale: locale,
          yearRange: yearRange,
          selectableDates: selectableDates,
          events: events,
          extras: <String, Object?>{
            'dateFormat': dateFormat.index,
            'outlined': outlined,
            'startLabel': startLabel,
            'endLabel': endLabel,
            'supportingText': supportingText,
            'isStartError': isStartError,
            'isEndError': isEndError,
            'enabled': enabled,
            'readOnly': readOnly,
            'confirmButtonText': confirmButtonText,
            'dismissButtonText': dismissButtonText,
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'showCalendarSystemToggle': showCalendarSystemToggle,
            'showAdjacentMonthDays': showAdjacentMonthDays,
          },
        ),
        callbacks: PickerViewCallbacks(onRangeValueChanged: onRangeChanged),
        fallbackHeight: _fallbackHeight,
        measurementHeight: _measurementHeight,
      );
}
