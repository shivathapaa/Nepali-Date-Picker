// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'dart:ui';

import 'package:flutter/foundation.dart';

import '../policy.dart';
import 'events.dart';
import 'simple_types.dart';

/// Which dates a native picker lets the user select. Marking days (see
/// [NepaliPickerEventOptions]) never blocks them; this does.
///
/// The rules compose: a date must pass every bound that is set. Arbitrary
/// predicates cannot cross the bridge; these fields cover the engine's
/// shipped rules (before, after, range, weekend and closure exclusion).
@immutable
class NepaliSelectableDates {
  const NepaliSelectableDates({
    this.minDate,
    this.maxDate,
    this.includeMinDate = false,
    this.includeMaxDate = false,
    this.excludeWeekend,
    this.excludeClosuresOf,
  });

  /// Refuses dates before this one; [includeMinDate] lets the bound itself
  /// stay selectable.
  final SimpleDate? minDate;

  /// Refuses dates after this one; [includeMaxDate] lets the bound itself
  /// stay selectable.
  final SimpleDate? maxDate;

  final bool includeMinDate;
  final bool includeMaxDate;

  /// Weekdays to refuse, 1 for Sunday through 7 for Saturday.
  final Set<int>? excludeWeekend;

  /// Refuses the days this policy's events close.
  final NepaliCalendarPolicy? excludeClosuresOf;

  /// Only dates at or after [date] (strictly after without [includeDate]).
  factory NepaliSelectableDates.after(SimpleDate date,
          {bool includeDate = false}) =>
      NepaliSelectableDates(minDate: date, includeMinDate: includeDate);

  /// Only dates at or before [date] (strictly before without [includeDate]).
  factory NepaliSelectableDates.before(SimpleDate date,
          {bool includeDate = false}) =>
      NepaliSelectableDates(maxDate: date, includeMaxDate: includeDate);

  /// Only dates between the two bounds.
  factory NepaliSelectableDates.range(
    SimpleDate minDate,
    SimpleDate maxDate, {
    bool includeMinDate = false,
    bool includeMaxDate = false,
  }) =>
      NepaliSelectableDates(
        minDate: minDate,
        maxDate: maxDate,
        includeMinDate: includeMinDate,
        includeMaxDate: includeMaxDate,
      );

  @override
  bool operator ==(Object other) =>
      other is NepaliSelectableDates &&
      other.minDate == minDate &&
      other.maxDate == maxDate &&
      other.includeMinDate == includeMinDate &&
      other.includeMaxDate == includeMaxDate &&
      setEquals(other.excludeWeekend, excludeWeekend) &&
      other.excludeClosuresOf == excludeClosuresOf;

  @override
  int get hashCode => Object.hash(
        minDate,
        maxDate,
        includeMinDate,
        includeMaxDate,
        excludeWeekend == null ? null : Object.hashAllUnordered(excludeWeekend!),
        excludeClosuresOf,
      );
}

/// One marked event for the native pickers: what it is plus how to draw it.
///
/// [color] null takes the palette slot the [kind] maps to; [indicate] gives
/// the day a dot for this entry, so dots keep meaning "something is
/// scheduled" while holidays only colour the number.
@immutable
class NepaliPickerEvent {
  const NepaliPickerEvent({
    required this.year,
    required this.month,
    required this.dayOfMonth,
    required this.name,
    required this.kind,
    bool? closesOffices,
    this.color,
    this.indicate = false,
    this.id,
    this.payload,
  }) : closesOffices = closesOffices ??
            (kind == NepaliEventKind.governmentPublic ||
                kind == NepaliEventKind.religious);

  final int year;
  final int month;
  final int dayOfMonth;
  final String name;
  final NepaliEventKind kind;
  final bool closesOffices;
  final Color? color;
  final bool indicate;

  /// An identifier the app correlates back to its own record. The library
  /// carries it through untouched and hands it back when the entry is tapped;
  /// days of one span that share it are a single line of a month's list.
  final String? id;

  /// Anything else the app wants back with the entry, as an opaque string:
  /// JSON, a URL, an identifier list. The library never parses it.
  final String? payload;

  @override
  bool operator ==(Object other) =>
      other is NepaliPickerEvent &&
      other.year == year &&
      other.month == month &&
      other.dayOfMonth == dayOfMonth &&
      other.name == name &&
      other.kind == kind &&
      other.closesOffices == closesOffices &&
      other.color == color &&
      other.indicate == indicate &&
      other.id == id &&
      other.payload == payload;

  @override
  int get hashCode => Object.hash(year, month, dayOfMonth, name, kind,
      closesOffices, color, indicate, id, payload);
}

/// What the native pickers should mark, mirroring the platform option bags:
/// weekly off days, events, the display switches and the palette overrides.
/// A null colour keeps the slot the theme resolved.
@immutable
class NepaliPickerEventOptions {
  const NepaliPickerEventOptions({
    this.weeklyOffDays = const <int>[7],
    this.events = const <NepaliPickerEvent>[],
    this.markWeeklyOff = true,
    this.markEvents = true,
    this.tintContainer = false,
    this.indicateWeeklyOff = false,
    this.describeEvents = true,
    this.weeklyOffColor,
    this.publicHolidayColor,
    this.religiousColor,
    this.regionalColor,
    this.observanceColor,
    this.markedContainerColor,
  });

  final List<int> weeklyOffDays;
  final List<NepaliPickerEvent> events;
  final bool markWeeklyOff;
  final bool markEvents;
  final bool tintContainer;
  final bool indicateWeeklyOff;
  final bool describeEvents;
  final Color? weeklyOffColor;
  final Color? publicHolidayColor;
  final Color? religiousColor;
  final Color? regionalColor;
  final Color? observanceColor;
  final Color? markedContainerColor;

  @override
  bool operator ==(Object other) =>
      other is NepaliPickerEventOptions &&
      listEquals(other.weeklyOffDays, weeklyOffDays) &&
      listEquals(other.events, events) &&
      other.markWeeklyOff == markWeeklyOff &&
      other.markEvents == markEvents &&
      other.tintContainer == tintContainer &&
      other.indicateWeeklyOff == indicateWeeklyOff &&
      other.describeEvents == describeEvents &&
      other.weeklyOffColor == weeklyOffColor &&
      other.publicHolidayColor == publicHolidayColor &&
      other.religiousColor == religiousColor &&
      other.regionalColor == regionalColor &&
      other.observanceColor == observanceColor &&
      other.markedContainerColor == markedContainerColor;

  @override
  int get hashCode => Object.hash(
        Object.hashAll(weeklyOffDays),
        Object.hashAll(events),
        markWeeklyOff,
        markEvents,
        tintContainer,
        indicateWeeklyOff,
        describeEvents,
        weeklyOffColor,
        publicHolidayColor,
        religiousColor,
        regionalColor,
        observanceColor,
        markedContainerColor,
      );
}
