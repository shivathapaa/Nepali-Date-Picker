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

// The frame the native view is measured in; the field with a label, an error and supporting text is the tallest mode.
const double _measurementHeight = 240;

/// The native single-date text field, with an optional picker dialog behind
/// it, embedded as a platform view.
///
/// [outlined] chooses the bare text field; the filled variant opens the
/// calendar dialog from its trailing icon. [onValueChanged] fires with the
/// plain date as it is typed or picked, null while the input is empty or
/// unparseable.
///
/// Because the field is rendered by the native side, the software keyboard
/// targets the platform view rather than a Flutter text input.
class NepaliDateField extends StatelessWidget {
  const NepaliDateField({
    super.key,
    this.initialValue,
    this.locale = const NepaliDateLocale(),
    this.dateFormat = DatePattern.yyyySlashMmSlashDd,
    this.yearRange,
    this.selectableDates,
    this.outlined = true,
    this.label,
    this.placeholder,
    this.supportingText,
    this.isError = false,
    this.enabled = true,
    this.readOnly = false,
    this.confirmButtonText,
    this.dismissButtonText,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showCalendarSystemToggle = false,
    this.showAdjacentMonthDays = false,
    this.events,
    required this.onValueChanged,
  });

  final SimpleDate? initialValue;
  final NepaliDateLocale locale;

  /// The wire shape typed text must match.
  final DatePattern dateFormat;

  final YearRange? yearRange;
  final NepaliSelectableDates? selectableDates;
  final bool outlined;
  final String? label;
  final String? placeholder;
  final String? supportingText;
  final bool isError;
  final bool enabled;
  final bool readOnly;
  final String? confirmButtonText;
  final String? dismissButtonText;
  final CalendarSystem initialCalendarSystem;
  final bool showCalendarSystemToggle;
  final bool showAdjacentMonthDays;
  final NepaliPickerEventOptions? events;
  final ValueChanged<SimpleDate?> onValueChanged;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'dateField',
          initialDate: initialValue,
          locale: locale,
          yearRange: yearRange,
          selectableDates: selectableDates,
          events: events,
          extras: <String, Object?>{
            'dateFormat': dateFormat.index,
            'outlined': outlined,
            'label': label,
            'placeholder': placeholder,
            'supportingText': supportingText,
            'isError': isError,
            'enabled': enabled,
            'readOnly': readOnly,
            'confirmButtonText': confirmButtonText,
            'dismissButtonText': dismissButtonText,
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'showCalendarSystemToggle': showCalendarSystemToggle,
            'showAdjacentMonthDays': showAdjacentMonthDays,
          },
        ),
        callbacks: PickerViewCallbacks(onValueChanged: onValueChanged),
        fallbackHeight: _fallbackHeight,
        measurementHeight: _measurementHeight,
      );
}
