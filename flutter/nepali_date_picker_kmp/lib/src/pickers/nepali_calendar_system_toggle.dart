// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/locale.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 48;

// The frame the native view is measured in; the toggle only ever grows by a text-size step.
const double _measurementHeight = 140;

/// The Bikram Sambat and Gregorian toggle, rendered natively and embedded as
/// a platform view.
///
/// The toggle owns its selection: it flips on tap and reports each change
/// through [onCalendarSystemChanged].
class NepaliCalendarSystemToggle extends StatelessWidget {
  const NepaliCalendarSystemToggle({
    super.key,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.language = NepaliLanguage.english,
    required this.onCalendarSystemChanged,
  });

  final CalendarSystem initialCalendarSystem;
  final NepaliLanguage language;
  final ValueChanged<CalendarSystem> onCalendarSystemChanged;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'calendarSystemToggle',
          extras: <String, Object?>{
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'language': language.index,
          },
        ),
        callbacks: PickerViewCallbacks(
          onCalendarSystemChanged: (era) {
            final system = CalendarSystem.fromEra(era);
            if (system != null) onCalendarSystemChanged(system);
          },
        ),
        fallbackHeight: _fallbackHeight,
        measurementHeight: _measurementHeight,
      );
}
