// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// The shared plumbing every embedded picker widget rides on: one platform
// view type, one creation-parameter map shape, and one callback hub keyed by
// platform view id. The Kotlin and Swift factories parse the same keys.

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../mapping.dart';
import '../messages.g.dart';
import '../model/locale.dart';
import '../model/events.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';

/// The single platform view type; the `variant` creation parameter selects
/// the picker.
const String nepaliPickerViewType = 'nepali_date_picker_kmp/picker';

/// Raw callbacks for one live platform view.
class PickerViewCallbacks {
  PickerViewCallbacks({
    this.onHeightChanged,
    this.onDateSelected,
    this.onRangeSelected,
    this.onValueChanged,
    this.onRangeValueChanged,
    this.onCalendarSystemChanged,
    this.onEventTapped,
    this.onDisplayedMonthChanged,
  });

  final void Function(double height)? onHeightChanged;
  final void Function(NepaliDate? date)? onDateSelected;
  final void Function(NepaliDate? start, NepaliDate? end)? onRangeSelected;
  final void Function(SimpleDate? value)? onValueChanged;
  final void Function(SimpleDate? start, SimpleDate? end)? onRangeValueChanged;
  final void Function(int era)? onCalendarSystemChanged;
  final void Function(NepaliEvent event)? onEventTapped;
  final void Function(int year, int month, int era)? onDisplayedMonthChanged;
}

/// Routes native picker events to the widget that owns each view id.
class PickerViewEventHub implements PickerViewFlutterApi {
  PickerViewEventHub._();

  static final Map<int, PickerViewCallbacks> _views = <int, PickerViewCallbacks>{};
  static bool _initialized = false;

  static void ensureInitialized() {
    if (_initialized) return;
    _initialized = true;
    PickerViewFlutterApi.setUp(PickerViewEventHub._());
  }

  static void register(int viewId, PickerViewCallbacks callbacks) {
    ensureInitialized();
    _views[viewId] = callbacks;
  }

  static void unregister(int viewId) {
    _views.remove(viewId);
  }

  @override
  void onHeightChanged(int viewId, double height) {
    _views[viewId]?.onHeightChanged?.call(height);
  }

  @override
  void onDateSelected(int viewId, CalendarDto? date) {
    _views[viewId]
        ?.onDateSelected
        ?.call(date == null ? null : calendarFromDto(date));
  }

  @override
  void onRangeSelected(int viewId, CalendarDto? start, CalendarDto? end) {
    _views[viewId]?.onRangeSelected?.call(
          start == null ? null : calendarFromDto(start),
          end == null ? null : calendarFromDto(end),
        );
  }

  @override
  void onValueChanged(int viewId, DateDto? value) {
    _views[viewId]
        ?.onValueChanged
        ?.call(value == null ? null : dateFromDto(value));
  }

  @override
  void onRangeValueChanged(int viewId, DateDto? start, DateDto? end) {
    _views[viewId]?.onRangeValueChanged?.call(
          start == null ? null : dateFromDto(start),
          end == null ? null : dateFromDto(end),
        );
  }

  @override
  void onCalendarSystemChanged(int viewId, int era) {
    _views[viewId]?.onCalendarSystemChanged?.call(era);
  }

  @override
  void onEventTapped(int viewId, EventDto event) {
    _views[viewId]?.onEventTapped?.call(eventFromDto(event));
  }

  @override
  void onDisplayedMonthChanged(int viewId, int year, int month, int era) {
    _views[viewId]?.onDisplayedMonthChanged?.call(year, month, era);
  }
}

List<int>? _dateEntry(SimpleDate? date) =>
    date == null ? null : <int>[date.year, date.month, date.dayOfMonth];

List<int> localeCreationEntry(NepaliDateLocale locale) => <int>[
      locale.language.index,
      locale.dateFormat.index,
      locale.weekDayName.index,
      locale.monthName.index,
      locale.digitScript?.index ?? -1,
    ];

Map<String, Object?>? _selectableEntry(NepaliSelectableDates? selectable) {
  if (selectable == null) return null;
  final policy = selectable.excludeClosuresOf;
  return <String, Object?>{
    'minDate': _dateEntry(selectable.minDate),
    'maxDate': _dateEntry(selectable.maxDate),
    'includeMinDate': selectable.includeMinDate,
    'includeMaxDate': selectable.includeMaxDate,
    'excludeWeekend': selectable.excludeWeekend?.toList(),
    'policyWeeklyOffDays': policy?.weeklyOffDays.toList(),
    'policyEvents': policy?.events
        .map((event) => <Object?>[
              event.year,
              event.month,
              event.dayOfMonth,
              event.name,
              event.kind.index,
              event.closesOffices,
            ])
        .toList(),
  };
}

Map<String, Object?>? _eventsEntry(NepaliPickerEventOptions? events) {
  if (events == null) return null;
  return <String, Object?>{
    'weeklyOffDays': events.weeklyOffDays,
    'events': events.events
        .map((event) => <Object?>[
              event.year,
              event.month,
              event.dayOfMonth,
              event.name,
              event.kind.index,
              event.closesOffices,
              event.color?.toARGB32() ?? 0,
              event.indicate,
              event.id,
              event.payload,
            ])
        .toList(),
    'markWeeklyOff': events.markWeeklyOff,
    'markEvents': events.markEvents,
    'tintContainer': events.tintContainer,
    'indicateWeeklyOff': events.indicateWeeklyOff,
    'describeEvents': events.describeEvents,
    'weeklyOffColorArgb': events.weeklyOffColor?.toARGB32() ?? 0,
    'publicHolidayColorArgb': events.publicHolidayColor?.toARGB32() ?? 0,
    'religiousColorArgb': events.religiousColor?.toARGB32() ?? 0,
    'regionalColorArgb': events.regionalColor?.toARGB32() ?? 0,
    'observanceColorArgb': events.observanceColor?.toARGB32() ?? 0,
    'markedContainerColorArgb': events.markedContainerColor?.toARGB32() ?? 0,
  };
}

/// The creation-parameter map one embedded picker view is configured with.
/// Only [variant] is always read; the platform factories fall back to the
/// same defaults the View and ViewController factories use for absent keys.
Map<String, Object?> buildPickerCreationParams({
  required String variant,
  SimpleDate? initialDate,
  SimpleDate? initialEndDate,
  NepaliDateLocale locale = const NepaliDateLocale(),
  YearRange? yearRange,
  NepaliSelectableDates? selectableDates,
  NepaliPickerEventOptions? events,
  Map<String, Object?> extras = const <String, Object?>{},
}) =>
    <String, Object?>{
      'variant': variant,
      'initialDate': _dateEntry(initialDate),
      'initialEndDate': _dateEntry(initialEndDate),
      'locale': localeCreationEntry(locale),
      'yearRangeStart': yearRange?.first,
      'yearRangeEnd': yearRange?.last,
      'selectable': _selectableEntry(selectableDates),
      'events': _eventsEntry(events),
      ...extras,
    };

/// Hosts one native picker: the platform view is always measured at
/// [measurementHeight] while the widget itself takes the height Compose
/// reports back, with the gap clipped away.
///
/// Keeping those two heights apart is the whole point. Compose measures
/// inside the frame it is given, so if the view shrank with the layout, a
/// report could never exceed the current frame and the content would be
/// trapped at its smallest size; switching a picker to typed input and back
/// is exactly that case. Measuring unbounded instead is not an option,
/// because Compose forbids infinite height above a vertically scrolling
/// component and the range picker has one. This mirrors the `AutoSized`
/// host the SwiftUI sample uses for the same reason.
class NepaliPlatformPickerView extends StatefulWidget {
  const NepaliPlatformPickerView({
    super.key,
    required this.creationParams,
    required this.callbacks,
    required this.fallbackHeight,
    required this.measurementHeight,
  });

  final Map<String, Object?> creationParams;
  final PickerViewCallbacks callbacks;

  /// Shown until the first height report lands.
  final double fallbackHeight;

  /// Height the native view is measured in: generous enough for the tallest
  /// mode the picker can show. A vertically scrolling picker fills it and
  /// scrolls inside it.
  final double measurementHeight;

  @override
  State<NepaliPlatformPickerView> createState() =>
      _NepaliPlatformPickerViewState();
}

class _NepaliPlatformPickerViewState extends State<NepaliPlatformPickerView> {
  double? _height;
  int? _viewId;

  void _onViewCreated(int id) {
    _viewId = id;
    PickerViewEventHub.register(
      id,
      PickerViewCallbacks(
        onHeightChanged: (height) {
          // Compose reports on every layout pass; ignore the noise, and
          // never take more than the frame the view is measured in.
          final clamped = height.clamp(0.0, widget.measurementHeight);
          if (mounted && clamped > 0 && clamped != _height) {
            setState(() => _height = clamped);
          }
          widget.callbacks.onHeightChanged?.call(height);
        },
        onDateSelected: widget.callbacks.onDateSelected,
        onRangeSelected: widget.callbacks.onRangeSelected,
        onValueChanged: widget.callbacks.onValueChanged,
        onRangeValueChanged: widget.callbacks.onRangeValueChanged,
        onCalendarSystemChanged: widget.callbacks.onCalendarSystemChanged,
        onEventTapped: widget.callbacks.onEventTapped,
        onDisplayedMonthChanged: widget.callbacks.onDisplayedMonthChanged,
      ),
    );
  }

  @override
  void dispose() {
    final id = _viewId;
    if (id != null) PickerViewEventHub.unregister(id);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // The pickers scroll and drag internally, so the view claims vertical
    // gestures eagerly instead of arbitrating with enclosing scrollables.
    final gestures = <Factory<OneSequenceGestureRecognizer>>{
      Factory<OneSequenceGestureRecognizer>(EagerGestureRecognizer.new),
    };

    final Widget view;
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        view = AndroidView(
          viewType: nepaliPickerViewType,
          creationParams: widget.creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          gestureRecognizers: gestures,
          onPlatformViewCreated: _onViewCreated,
        );
      case TargetPlatform.iOS:
        view = UiKitView(
          viewType: nepaliPickerViewType,
          creationParams: widget.creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          gestureRecognizers: gestures,
          onPlatformViewCreated: _onViewCreated,
        );
      default:
        view = ErrorWidget(UnsupportedError(
            'nepali_date_picker_kmp embeds pickers on Android and iOS only'));
    }

    // The clip hides the measurement frame's unused tail; hit testing stops
    // at the visible bounds on its own, so the hidden part cannot swallow
    // taps meant for whatever follows it.
    return ClipRect(
      child: SizedBox(
        height: _height ?? widget.fallbackHeight,
        width: double.infinity,
        child: OverflowBox(
          alignment: Alignment.topCenter,
          minHeight: widget.measurementHeight,
          maxHeight: widget.measurementHeight,
          child: view,
        ),
      ),
    );
  }
}
