// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import '../bridge.dart';
import '../mapping.dart';
import '../messages.g.dart';
import '../model/locale.dart';
import '../model/nepali_date.dart';
import '../model/picker_options.dart';
import '../model/simple_types.dart';
import '../policy.dart';
import '../validation.dart';

PickerHostApi get _hostApi => pickerHostApi;
EngineApi get _engineApi => engineApi;

SelectableDto? _selectableToDto(NepaliSelectableDates? selectable) {
  if (selectable == null) return null;
  final policy = selectable.excludeClosuresOf;
  return SelectableDto(
    minDate: selectable.minDate == null ? null : dateToDto(selectable.minDate!),
    maxDate: selectable.maxDate == null ? null : dateToDto(selectable.maxDate!),
    includeMinDate: selectable.includeMinDate,
    includeMaxDate: selectable.includeMaxDate,
    excludeWeekend: selectable.excludeWeekend?.toList(),
    excludeClosuresOf: policy == null ? null : policyToDto(policy),
  );
}

EventOptionsDto? _eventsToDto(NepaliPickerEventOptions? options) {
  if (options == null) return null;
  return EventOptionsDto(
    weeklyOffDays: options.weeklyOffDays,
    events: options.events
        .map((event) => EventMarkDto(
              year: event.year,
              month: event.month,
              dayOfMonth: event.dayOfMonth,
              name: event.name,
              kind: eventKindToDto(event.kind),
              closesOffices: event.closesOffices,
              colorArgb: event.color?.toARGB32() ?? 0,
              indicate: event.indicate,
            ))
        .toList(),
    markWeeklyOff: options.markWeeklyOff,
    markEvents: options.markEvents,
    tintContainer: options.tintContainer,
    indicateWeeklyOff: options.indicateWeeklyOff,
    describeEvents: options.describeEvents,
    weeklyOffColorArgb: options.weeklyOffColor?.toARGB32() ?? 0,
    publicHolidayColorArgb: options.publicHolidayColor?.toARGB32() ?? 0,
    religiousColorArgb: options.religiousColor?.toARGB32() ?? 0,
    regionalColorArgb: options.regionalColor?.toARGB32() ?? 0,
    observanceColorArgb: options.observanceColor?.toARGB32() ?? 0,
    markedContainerColorArgb: options.markedContainerColor?.toARGB32() ?? 0,
  );
}

Future<DialogConfigDto> _dialogConfig({
  required SimpleDate? initialSelectedDate,
  required NepaliDateLocale locale,
  required YearRange? yearRange,
  required NepaliSelectableDates? selectableDates,
  required bool showModeToggle,
  required bool showTodayButton,
  required bool showEnglishDate,
  required NepaliDateLocale? englishDateLocale,
  required CalendarSystem initialCalendarSystem,
  required bool showCalendarSystemToggle,
  required bool showAdjacentMonthDays,
  required NepaliPickerEventOptions? events,
  required String? title,
  required String confirmText,
  required String dismissText,
  required double tonalElevation,
  required double cornerRadius,
}) async {
  if (initialSelectedDate != null) {
    await NepaliDateValidation.requireBsSimpleDate(_engineApi, initialSelectedDate);
  }
  final years =
      yearRange ?? await NepaliDateValidation.bsYearRange(_engineApi);
  return DialogConfigDto(
    initialSelectedDate:
        initialSelectedDate == null ? null : dateToDto(initialSelectedDate),
    locale: localeToDto(locale),
    yearRangeStart: years.first,
    yearRangeEnd: years.last,
    selectable: _selectableToDto(selectableDates),
    showModeToggle: showModeToggle,
    showTodayButton: showTodayButton,
    showEnglishDate: showEnglishDate,
    englishDateLocale:
        englishDateLocale == null ? null : localeToDto(englishDateLocale),
    initialCalendarSystemEra: initialCalendarSystem.era,
    showCalendarSystemToggle: showCalendarSystemToggle,
    showAdjacentMonthDays: showAdjacentMonthDays,
    eventOptions: _eventsToDto(events),
    title: title,
    confirmText: confirmText,
    dismissText: dismissText,
    tonalElevation: tonalElevation,
    cornerRadius: cornerRadius,
  );
}

/// Shows the Material3 Nepali picker in a native modal dialog and completes
/// with the confirmed date, or null when it was dismissed.
///
/// The dialog is presented by the platform (a Compose dialog window on
/// Android, a transparent modal controller on iOS), so no [BuildContext] is
/// involved and the Flutter navigation stack is untouched.
Future<NepaliDate?> showNepaliDatePickerDialog({
  SimpleDate? initialSelectedDate,
  NepaliDateLocale locale = const NepaliDateLocale(),
  YearRange? yearRange,
  NepaliSelectableDates? selectableDates,
  bool showModeToggle = true,
  bool showTodayButton = true,
  bool showEnglishDate = false,
  NepaliDateLocale? englishDateLocale,
  CalendarSystem initialCalendarSystem = CalendarSystem.bikramSambat,
  bool showCalendarSystemToggle = false,
  bool showAdjacentMonthDays = false,
  NepaliPickerEventOptions? events,
  String confirmText = 'OK',
  String dismissText = 'Cancel',
  double tonalElevation = 6,
  double cornerRadius = 28,
}) async {
  final config = await _dialogConfig(
    initialSelectedDate: initialSelectedDate,
    locale: locale,
    yearRange: yearRange,
    selectableDates: selectableDates,
    showModeToggle: showModeToggle,
    showTodayButton: showTodayButton,
    showEnglishDate: showEnglishDate,
    englishDateLocale: englishDateLocale,
    initialCalendarSystem: initialCalendarSystem,
    showCalendarSystemToggle: showCalendarSystemToggle,
    showAdjacentMonthDays: showAdjacentMonthDays,
    events: events,
    title: null,
    confirmText: confirmText,
    dismissText: dismissText,
    tonalElevation: tonalElevation,
    cornerRadius: cornerRadius,
  );
  final picked = await _hostApi.showDatePickerDialog(config);
  return picked == null ? null : calendarFromDto(picked);
}

/// Shows the picker in a native full-screen dialog and completes with the
/// confirmed date, or null when it was dismissed.
Future<NepaliDate?> showNepaliDatePickerFullScreenDialog({
  SimpleDate? initialSelectedDate,
  NepaliDateLocale locale = const NepaliDateLocale(),
  YearRange? yearRange,
  NepaliSelectableDates? selectableDates,
  bool showModeToggle = true,
  bool showTodayButton = true,
  bool showEnglishDate = false,
  NepaliDateLocale? englishDateLocale,
  CalendarSystem initialCalendarSystem = CalendarSystem.bikramSambat,
  bool showCalendarSystemToggle = false,
  bool showAdjacentMonthDays = false,
  NepaliPickerEventOptions? events,
  String? title,
  String confirmText = 'OK',
  String dismissText = 'Cancel',
}) async {
  final config = await _dialogConfig(
    initialSelectedDate: initialSelectedDate,
    locale: locale,
    yearRange: yearRange,
    selectableDates: selectableDates,
    showModeToggle: showModeToggle,
    showTodayButton: showTodayButton,
    showEnglishDate: showEnglishDate,
    englishDateLocale: englishDateLocale,
    initialCalendarSystem: initialCalendarSystem,
    showCalendarSystemToggle: showCalendarSystemToggle,
    showAdjacentMonthDays: showAdjacentMonthDays,
    events: events,
    title: title,
    confirmText: confirmText,
    dismissText: dismissText,
    tonalElevation: 0,
    cornerRadius: 0,
  );
  final picked = await _hostApi.showFullScreenDatePickerDialog(config);
  return picked == null ? null : calendarFromDto(picked);
}
