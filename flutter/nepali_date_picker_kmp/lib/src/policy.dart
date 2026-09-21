// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';

import 'bridge.dart';
import 'mapping.dart';
import 'messages.g.dart';
import 'model/events.dart';
import 'model/nepali_date.dart';
import 'model/simple_types.dart';
import 'validation.dart';

/// One institution's closed days: the weekdays it never opens plus the
/// holidays it keeps.
///
/// The library ships no event data; the app supplies its own [events] and
/// gets back what a day is and how many working days lie between two of
/// them. The description crosses the bridge as plain data with every call,
/// the same way the npm package's `createCalendarPolicy` works, so there is
/// no cross-language callback.
///
/// Weekday numbering is 1 for Sunday through 7 for Saturday. Nepal's office
/// week is `{7}`; a school closed Saturday and Sunday is `{7, 1}`; an empty
/// set is an institution that never closes for the week alone.
@immutable
class NepaliCalendarPolicy {
  NepaliCalendarPolicy({
    this.weeklyOffDays = const <int>{7},
    this.events = const <NepaliEvent>[],
  }) {
    NepaliDateValidation.requireWeeklyOffDays(weeklyOffDays);
  }

  final Set<int> weeklyOffDays;
  final List<NepaliEvent> events;

  static EngineApi get _api => engineApi;

  late final PolicyDto _dto = PolicyDto(
    weeklyOffDays: List<int>.unmodifiable(weeklyOffDays),
    events: List<EventDto>.unmodifiable(events.map(eventToDto)),
  );

  /// Whether [dayOfWeek] (1 is Sunday) is one of the weekly off days.
  bool isWeeklyOff(int dayOfWeek) => weeklyOffDays.contains(dayOfWeek);

  /// What the given Bikram Sambat date is: closed by the week, by a holiday,
  /// by both, or neither.
  Future<NepaliDayStatus> statusOf(SimpleDate date) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, date);
    return dayStatusFromDto(await _api.statusOf(_dto, dateToDto(date)));
  }

  /// Every day of one Bikram Sambat month in day order: index 0 is day 1.
  /// The month's first weekday is resolved once and the week walked forward,
  /// so a grid costs one conversion rather than one per day.
  Future<List<NepaliDayStatus>> monthStatus(int year, int month) async {
    await NepaliDateValidation.requireBsMonth(_api, year, month);
    final days = await _api.monthStatus(_dto, year, month);
    return List.unmodifiable(days.map(dayStatusFromDto));
  }

  /// The events on one Bikram Sambat date, strongest kind first, empty when
  /// there are none.
  Future<List<NepaliEvent>> eventsOn(SimpleDate date) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, date);
    final events = await _api.eventsOn(_dto, dateToDto(date));
    return List.unmodifiable(events.map(eventFromDto));
  }

  /// Every event in one Bikram Sambat month, in date order.
  Future<List<NepaliEvent>> eventsIn(int year, int month) async {
    await NepaliDateValidation.requireBsMonth(_api, year, month);
    final events = await _api.eventsIn(_dto, year, month);
    return List.unmodifiable(events.map(eventFromDto));
  }

  /// Whether the institution is closed on the given date, for either reason.
  Future<bool> isNonWorkingDay(SimpleDate date) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, date);
    return _api.isNonWorkingDay(_dto, dateToDto(date));
  }

  /// Working days in the half-open range from [start] to [end]. Add 1 to
  /// include the end date. [start] must not be after [end].
  Future<int> workingDaysBetween(SimpleDate start, SimpleDate end) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, start);
    await NepaliDateValidation.requireBsSimpleDate(_api, end);
    if (start.compareTo(end) > 0) {
      throw ArgumentError('start $start must not be after end $end');
    }
    return _api.workingDaysBetween(_dto, dateToDto(start), dateToDto(end));
  }

  /// The first working day at or after [from], which may be [from] itself.
  Future<NepaliDate> nextWorkingDay(SimpleDate from) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, from);
    return calendarFromDto(await _api.nextWorkingDay(_dto, dateToDto(from)));
  }

  /// The date [days] working days away, with Excel WORKDAY semantics: 0
  /// stays put, a positive count lands strictly after, a negative one
  /// strictly before.
  Future<NepaliDate> addWorkingDays(SimpleDate from, int days) async {
    await NepaliDateValidation.requireBsSimpleDate(_api, from);
    return calendarFromDto(
        await _api.addWorkingDays(_dto, dateToDto(from), days));
  }

  @override
  bool operator ==(Object other) =>
      other is NepaliCalendarPolicy &&
      setEquals(other.weeklyOffDays, weeklyOffDays) &&
      listEquals(other.events, events);

  @override
  int get hashCode =>
      Object.hash(Object.hashAllUnordered(weeklyOffDays), Object.hashAll(events));
}

/// The policy as wire data, for bridge internals; unexported.
PolicyDto policyToDto(NepaliCalendarPolicy policy) => policy._dto;

/// Span helpers over a single event, mirroring the shared `EventSpans`.
extension NepaliEventSpans on NepaliEvent {
  static EngineApi get _api => engineApi;

  /// This event repeated on [days] consecutive days, this date first.
  /// [days] must be at least 1 and the span must stay inside the supported
  /// Bikram Sambat years.
  Future<List<NepaliEvent>> spanningDays(int days) async {
    if (days < 1) {
      throw ArgumentError.value(days, 'days', 'must be at least 1');
    }
    final events = await _api.eventSpanningDays(eventToDto(this), days);
    return List.unmodifiable(events.map(eventFromDto));
  }

  /// This event repeated on every day through [end], both ends included.
  /// [end] must not be before this event's own date.
  Future<List<NepaliEvent>> spanningThrough(SimpleDate end) async {
    final start = SimpleDate(year, month, dayOfMonth);
    if (end.compareTo(start) < 0) {
      throw ArgumentError('end $end must not be before the event date $start');
    }
    final events = await _api.eventSpanningThrough(eventToDto(this), dateToDto(end));
    return List.unmodifiable(events.map(eventFromDto));
  }
}
