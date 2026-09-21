// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';

import '../model/events.dart';
import '../model/locale.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import 'platform_picker_view.dart';

const double _fallbackHeight = 420;

// The frame the native view is measured in. The grid alone is fixed height;
// the day's summary and the month's list stack under it and need the room.
const double _measurementHeight = 1400;

/// A browsable Nepali month calendar, rendered by the native Compose
/// implementation and embedded as a platform view.
///
/// Unlike [NepaliDatePicker], which asks for a date, this fills the width it
/// is given and reads like a wall patro: both calendars' numbers in every
/// cell, the neighbouring months filling the corners, and the days an
/// institution is closed for marked from [events].
///
/// Ask for [showDaySummary] or [showMonthEvents] to stack the picked day's
/// verdict and the month's event list under the grid. They are drawn natively
/// inside this one view, because a calendar and a list in two platform views
/// cannot share a selection. To render those lists in Dart instead, leave both
/// off and read `NepaliCalendarPolicy.monthStatus` and
/// `NepaliCalendarPolicy.eventsIn` from the engine.
///
/// The widget sizes itself to the height the calendar reports, so it can sit
/// as one item of a scrolling list.
class NepaliCalendar extends StatelessWidget {
  const NepaliCalendar({
    super.key,
    this.initialSelectedDate,
    this.locale = const NepaliDateLocale(),
    this.yearRange,
    this.showTodayButton = true,
    this.showCalendarSystemToggle = false,
    this.showAdjacentMonthDays = true,
    this.showSecondaryDates = true,
    this.secondaryDateLocale,
    this.initialCalendarSystem = CalendarSystem.bikramSambat,
    this.showDaySummary = false,
    this.showMonthEvents = false,
    this.events,
    this.onDaySelected,
    this.onEventTapped,
  });

  final SimpleDate? initialSelectedDate;
  final NepaliDateLocale locale;

  /// The Bikram Sambat years the calendar pages over; null uses the engine's
  /// full range.
  final YearRange? yearRange;

  final bool showTodayButton;
  final bool showCalendarSystemToggle;

  /// Fills the slots around the month with its neighbours' days, drawn faded.
  /// On by default, which is what makes the grid read as a wall calendar.
  final bool showAdjacentMonthDays;

  /// Pairs every cell with the same day in the other calendar.
  final bool showSecondaryDates;

  /// The language and digits those second numbers are written in; null uses
  /// [locale].
  final NepaliDateLocale? secondaryDateLocale;

  final CalendarSystem initialCalendarSystem;

  /// Writes the picked day out under the grid: whether the institution is
  /// shut, why, and everything named on it.
  final bool showDaySummary;

  /// Lists the month's events under the grid, a span as a single line.
  final bool showMonthEvents;

  /// The institution's weekly rule and the days it names. Null keeps Nepal's
  /// usual office week, Saturday off, with nothing named.
  final NepaliPickerEventOptions? events;

  /// Fires with the day a tap picks.
  final ValueChanged<NepaliDate>? onDaySelected;

  /// Fires with the event behind a tapped line of the stacked month list, and
  /// never while [showMonthEvents] is off.
  final ValueChanged<NepaliEvent>? onEventTapped;

  @override
  Widget build(BuildContext context) => NepaliPlatformPickerView(
        creationParams: buildPickerCreationParams(
          variant: 'calendar',
          initialDate: initialSelectedDate,
          locale: locale,
          yearRange: yearRange,
          events: events,
          extras: <String, Object?>{
            'showTodayButton': showTodayButton,
            'showCalendarSystemToggle': showCalendarSystemToggle,
            'showAdjacentMonthDays': showAdjacentMonthDays,
            'showSecondaryDates': showSecondaryDates,
            'englishLocale': secondaryDateLocale == null
                ? null
                : localeCreationEntry(secondaryDateLocale!),
            'initialCalendarSystemEra': initialCalendarSystem.era,
            'showDaySummary': showDaySummary,
            'showMonthEvents': showMonthEvents,
          },
        ),
        callbacks: PickerViewCallbacks(
          onDateSelected: (date) {
            if (date != null) onDaySelected?.call(date);
          },
          onEventTapped: onEventTapped,
        ),
        fallbackHeight: _fallbackHeight,
        measurementHeight: _measurementHeight,
      );
}
