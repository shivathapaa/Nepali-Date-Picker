// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';

/// What kind of thing is on a day, mirroring the shared Kotlin
/// `NepaliEventKind`. Kinds order by strength: a government public holiday
/// outranks a religious festival, which outranks a regional day, which
/// outranks a plain observance.
enum NepaliEventKind {
  governmentPublic,
  religious,
  regional,
  observance;

  /// Whether this kind shuts offices unless the event says otherwise.
  bool get closesOfficesByDefault =>
      this == NepaliEventKind.governmentPublic ||
      this == NepaliEventKind.religious;
}

/// One thing on one Bikram Sambat day: a public holiday, a festival, a
/// programme, a meeting. Mirrors the shared Kotlin `NepaliCalendarEvent`.
///
/// The library ships no event data; apps supply their own lists to
/// `NepaliCalendarPolicy`. [id] and [payload] travel through untouched for
/// correlating results with the app's own records.
@immutable
class NepaliEvent {
  const NepaliEvent({
    required this.year,
    required this.month,
    required this.dayOfMonth,
    required this.name,
    required this.kind,
    bool? closesOffices,
    this.id,
    this.payload,
  }) : closesOffices = closesOffices ?? (kind == NepaliEventKind.governmentPublic || kind == NepaliEventKind.religious);

  final int year;
  final int month;
  final int dayOfMonth;
  final String name;
  final NepaliEventKind kind;

  /// Whether the institution is shut for this, which is what the working-day
  /// arithmetic counts by. Defaults to the kind's own convention.
  final bool closesOffices;

  final String? id;
  final String? payload;

  @override
  bool operator ==(Object other) =>
      other is NepaliEvent &&
      other.year == year &&
      other.month == month &&
      other.dayOfMonth == dayOfMonth &&
      other.name == name &&
      other.kind == kind &&
      other.closesOffices == closesOffices &&
      other.id == id &&
      other.payload == payload;

  @override
  int get hashCode => Object.hash(
      year, month, dayOfMonth, name, kind, closesOffices, id, payload);

  @override
  String toString() =>
      'NepaliEvent($year-$month-$dayOfMonth, $name, $kind, closes: '
      '$closesOffices)';
}

/// What one day is under a policy, mirroring the shared Kotlin
/// `NepaliDayStatus`.
///
/// A day that is both a weekly off day and a holiday is closed once, not
/// twice: [isNonWorking] covers either reason.
@immutable
class NepaliDayStatus {
  const NepaliDayStatus({
    required this.isWeeklyOff,
    required this.isNonWorking,
    this.primaryKind,
    this.names = const <String>[],
    this.events = const <NepaliEvent>[],
    this.closures = const <NepaliEvent>[],
  });

  /// The week closes the day, whatever else is named on it.
  final bool isWeeklyOff;

  /// The day is closed for either reason: the week or a closing event.
  final bool isNonWorking;

  /// The kind describing the day best, or null when nothing is named on it.
  final NepaliEventKind? primaryKind;

  /// The names of the day's events, strongest kind first.
  final List<String> names;

  /// The day's events in full, strongest kind first.
  final List<NepaliEvent> events;

  /// Only the events that actually shut the institution.
  final List<NepaliEvent> closures;

  @override
  bool operator ==(Object other) =>
      other is NepaliDayStatus &&
      other.isWeeklyOff == isWeeklyOff &&
      other.isNonWorking == isNonWorking &&
      other.primaryKind == primaryKind &&
      listEquals(other.names, names) &&
      listEquals(other.events, events) &&
      listEquals(other.closures, closures);

  @override
  int get hashCode => Object.hash(
        isWeeklyOff,
        isNonWorking,
        primaryKind,
        Object.hashAll(names),
        Object.hashAll(events),
        Object.hashAll(closures),
      );

  @override
  String toString() =>
      'NepaliDayStatus(weeklyOff: $isWeeklyOff, nonWorking: $isNonWorking, '
      'kind: $primaryKind, events: ${events.length})';
}
