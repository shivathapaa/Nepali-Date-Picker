// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// Pigeon schema for the bridge to the compiled Kotlin Multiplatform engine.
// Regenerate the bound code after editing:
//   dart run pigeon --input pigeons/messages.dart

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/src/messages.g.dart',
    kotlinOut:
        'android/src/main/kotlin/dev/shivathapaa/nepali_date_picker_kmp/Messages.g.kt',
    kotlinOptions: KotlinOptions(package: 'dev.shivathapaa.nepali_date_picker_kmp'),
    swiftOut:
        'ios/nepali_date_picker_kmp/Sources/nepali_date_picker_kmp/Messages.g.swift',
    dartPackageName: 'nepali_date_picker_kmp',
  ),
)
// The wire values of every enum follow the Kotlin declaration order, so the
// host sides map by ordinal.
enum LangDto { english, nepali }

enum NameFormatDto { full, medium, short }

enum DateFormatStyleDto {
  full,
  long,
  medium,
  shortMdy,
  shortYmd,
  compactMdy,
  compactYmd,
}

enum DigitScriptDto { latin, devanagari }

enum EventKindDto { governmentPublic, religious, regional, observance }

enum DatePatternDto {
  yyyySlashMmSlashDd,
  yyyyDashMmDashDd,
  ddSlashMmSlashYyyy,
  ddDashMmDashYyyy,
}

enum BrightnessDto { system, light, dark }

/// A plain year, month and day in whichever calendar the call documents.
class DateDto {
  const DateDto({
    required this.year,
    required this.month,
    required this.dayOfMonth,
  });

  final int year;
  final int month;
  final int dayOfMonth;
}

/// A wall-clock time of day in the Asia/Kathmandu zone.
class TimeDto {
  const TimeDto({
    required this.hour,
    required this.minute,
    required this.second,
    required this.nanosecond,
  });

  final int hour;
  final int minute;
  final int second;
  final int nanosecond;
}

/// The shared `CustomCalendar`: a fully described date. Fields the producing
/// call does not compute hold -1.
class CalendarDto {
  const CalendarDto({
    required this.year,
    required this.month,
    required this.dayOfMonth,
    required this.era,
    required this.firstDayOfMonth,
    required this.lastDayOfMonth,
    required this.totalDaysInMonth,
    required this.dayOfWeekInMonth,
    required this.dayOfWeek,
    required this.dayOfYear,
    required this.weekOfMonth,
    required this.weekOfYear,
  });

  final int year;
  final int month;
  final int dayOfMonth;
  final int era;
  final int firstDayOfMonth;
  final int lastDayOfMonth;
  final int totalDaysInMonth;
  final int dayOfWeekInMonth;
  final int dayOfWeek;
  final int dayOfYear;
  final int weekOfMonth;
  final int weekOfYear;
}

/// Month-grid metadata for either calendar system.
class MonthInfoDto {
  const MonthInfoDto({
    required this.year,
    required this.month,
    required this.totalDaysInMonth,
    required this.firstDayOfMonth,
    required this.lastDayOfMonth,
    required this.daysFromStartOfWeekToFirstOfMonth,
  });

  final int year;
  final int month;
  final int totalDaysInMonth;
  final int firstDayOfMonth;
  final int lastDayOfMonth;
  final int daysFromStartOfWeekToFirstOfMonth;
}

/// A calendar paired with a time of day.
class DateTimeDto {
  const DateTimeDto({required this.calendar, required this.time});

  final CalendarDto calendar;
  final TimeDto time;
}

/// An inclusive year range.
class YearRangeDto {
  const YearRangeDto({required this.first, required this.last});

  final int first;
  final int last;
}

/// How dates render: language, preset style, name lengths and digit script.
/// A null [digitScript] follows the language.
class LocaleDto {
  const LocaleDto({
    required this.language,
    required this.dateFormat,
    required this.weekDayName,
    required this.monthName,
    this.digitScript,
  });

  final LangDto language;
  final DateFormatStyleDto dateFormat;
  final NameFormatDto weekDayName;
  final NameFormatDto monthName;
  final DigitScriptDto? digitScript;
}

/// One thing on one Bikram Sambat day.
class EventDto {
  const EventDto({
    required this.year,
    required this.month,
    required this.dayOfMonth,
    required this.name,
    required this.kind,
    required this.closesOffices,
    this.id,
    this.payload,
  });

  final int year;
  final int month;
  final int dayOfMonth;
  final String name;
  final EventKindDto kind;
  final bool closesOffices;
  final String? id;
  final String? payload;
}

/// One institution's closed days: the weekdays it never opens plus the
/// holidays it keeps, sent as data with every policy call.
class PolicyDto {
  const PolicyDto({required this.weeklyOffDays, required this.events});

  final List<int> weeklyOffDays;
  final List<EventDto> events;
}

/// What one day is under a policy.
class DayStatusDto {
  const DayStatusDto({
    required this.isWeeklyOff,
    required this.isNonWorking,
    this.primaryKind,
    required this.names,
    required this.events,
    required this.closures,
  });

  final bool isWeeklyOff;
  final bool isNonWorking;
  final EventKindDto? primaryKind;
  final List<String> names;
  final List<EventDto> events;
  final List<EventDto> closures;
}

/// The three date-bound selectable rules the engine ships, plus the policy
/// decorators. Custom predicates cannot cross the bridge.
class SelectableDto {
  const SelectableDto({
    this.minDate,
    this.maxDate,
    required this.includeMinDate,
    required this.includeMaxDate,
    this.excludeWeekend,
    this.excludeClosuresOf,
  });

  /// Dates before this are refused; alone it means "this date and later".
  final DateDto? minDate;

  /// Dates after this are refused; alone it means "this date and earlier".
  final DateDto? maxDate;

  final bool includeMinDate;
  final bool includeMaxDate;

  /// Weekdays to refuse, 1 for Sunday through 7 for Saturday.
  final List<int>? excludeWeekend;

  /// Refuse days this policy's events close.
  final PolicyDto? excludeClosuresOf;
}

/// Every slot of the appearance proxy; `0` keeps Material's own value.
class AppearanceDto {
  const AppearanceDto({
    required this.brightness,
    required this.primaryArgb,
    required this.onPrimaryArgb,
    required this.primaryContainerArgb,
    required this.onPrimaryContainerArgb,
    required this.secondaryContainerArgb,
    required this.onSecondaryContainerArgb,
    required this.surfaceArgb,
    required this.onSurfaceArgb,
    required this.surfaceVariantArgb,
    required this.onSurfaceVariantArgb,
    required this.outlineArgb,
  });

  final BrightnessDto brightness;
  final int primaryArgb;
  final int onPrimaryArgb;
  final int primaryContainerArgb;
  final int onPrimaryContainerArgb;
  final int secondaryContainerArgb;
  final int onSecondaryContainerArgb;
  final int surfaceArgb;
  final int onSurfaceArgb;
  final int surfaceVariantArgb;
  final int onSurfaceVariantArgb;
  final int outlineArgb;
}

/// Everything a modal picker dialog needs. Field meanings mirror the View and
/// ViewController factories.
class DialogConfigDto {
  const DialogConfigDto({
    this.initialSelectedDate,
    required this.locale,
    required this.yearRangeStart,
    required this.yearRangeEnd,
    this.selectable,
    required this.showModeToggle,
    required this.showTodayButton,
    required this.showEnglishDate,
    this.englishDateLocale,
    required this.initialCalendarSystemEra,
    required this.showCalendarSystemToggle,
    required this.showAdjacentMonthDays,
    this.eventOptions,
    this.title,
    required this.confirmText,
    required this.dismissText,
    required this.tonalElevation,
    required this.cornerRadius,
  });

  final DateDto? initialSelectedDate;
  final LocaleDto locale;
  final int yearRangeStart;
  final int yearRangeEnd;
  final SelectableDto? selectable;
  final bool showModeToggle;
  final bool showTodayButton;
  final bool showEnglishDate;
  final LocaleDto? englishDateLocale;
  final int initialCalendarSystemEra;
  final bool showCalendarSystemToggle;
  final bool showAdjacentMonthDays;
  final EventOptionsDto? eventOptions;
  final String? title;
  final String confirmText;
  final String dismissText;
  final double tonalElevation;
  final double cornerRadius;
}

/// One marked event for the pickers: an [EventDto] plus how to draw it.
class EventMarkDto {
  const EventMarkDto({
    required this.year,
    required this.month,
    required this.dayOfMonth,
    required this.name,
    required this.kind,
    required this.closesOffices,
    required this.colorArgb,
    required this.indicate,
  });

  final int year;
  final int month;
  final int dayOfMonth;
  final String name;
  final EventKindDto kind;
  final bool closesOffices;
  final int colorArgb;
  final bool indicate;
}

/// What the pickers should mark; mirrors the platform event option bags.
class EventOptionsDto {
  const EventOptionsDto({
    required this.weeklyOffDays,
    required this.events,
    required this.markWeeklyOff,
    required this.markEvents,
    required this.tintContainer,
    required this.indicateWeeklyOff,
    required this.describeEvents,
    required this.weeklyOffColorArgb,
    required this.publicHolidayColorArgb,
    required this.religiousColorArgb,
    required this.regionalColorArgb,
    required this.observanceColorArgb,
    required this.markedContainerColorArgb,
  });

  final List<int> weeklyOffDays;
  final List<EventMarkDto> events;
  final bool markWeeklyOff;
  final bool markEvents;
  final bool tintContainer;
  final bool indicateWeeklyOff;
  final bool describeEvents;
  final int weeklyOffColorArgb;
  final int publicHolidayColorArgb;
  final int religiousColorArgb;
  final int regionalColorArgb;
  final int observanceColorArgb;
  final int markedContainerColorArgb;
}

/// The synchronous conversion engine. Every method delegates to the compiled
/// Kotlin `:core` module; failures surface as platform errors with the
/// Kotlin exception message.
@HostApi()
abstract class EngineApi {
  YearRangeDto getBsYearRange();
  YearRangeDto getAdYearRange();
  YearRangeDto getAdYearRangeForBsYears(int first, int last);
  CalendarDto getTodayBs();
  CalendarDto getTodayAd();
  TimeDto getCurrentTime();

  CalendarDto convertAdToBs(int year, int month, int dayOfMonth);
  CalendarDto convertBsToAd(int year, int month, int dayOfMonth);
  CalendarDto getBsCalendar(int year, int month, int dayOfMonth);
  CalendarDto getAdCalendar(int year, int month, int dayOfMonth);
  bool isAdDateConvertible(int year, int month, int dayOfMonth);

  MonthInfoDto getBsMonth(int year, int month);
  MonthInfoDto getAdMonth(int year, int month);
  List<CalendarDto?> getBsCalendarsInAdMonth(int year, int month);
  List<CalendarDto> getAdCalendarsInBsMonth(int year, int month);
  int getTotalDaysInBsMonth(int year, int month);
  int getTotalDaysInAdMonth(int year, int month);

  CalendarDto addDaysToBsDate(int year, int month, int dayOfMonth, int days);
  int getBsDaysBetween(DateDto start, DateDto end);
  int getAdDaysBetween(DateDto start, DateDto end);
  int compareBsDates(DateDto from, DateDto to);

  String getWeekdayName(int dayOfWeek, NameFormatDto format, LangDto language);
  String getBsMonthName(int month, NameFormatDto format, LangDto language);
  String getAdMonthName(int month, NameFormatDto format, LangDto language);

  String formatBsDate(CalendarDto calendar, LocaleDto locale);
  String formatAdDate(CalendarDto calendar, LocaleDto locale);
  String formatBsDateByPattern(String pattern, DateDto date, LangDto language);
  String formatAdDateByPattern(String pattern, DateDto date, LangDto language);
  String formatTimeByPattern(String pattern, TimeDto time, LangDto language);
  String formatBsDateTimeByPattern(
    String pattern,
    DateDto date,
    TimeDto? time,
    LangDto language,
  );
  String formatAdDateTimeByPattern(
    String pattern,
    DateDto date,
    TimeDto? time,
    LangDto language,
  );
  String formatTimeEnglish(TimeDto time, bool use12HourFormat);
  String formatTimeNepali(TimeDto time, bool use12HourFormat);

  String bsDateTimeToIso(DateDto date, TimeDto time);
  String adDateTimeToIso(DateDto date, TimeDto time);
  DateTimeDto bsDateTimeFromIso(String isoDateTime);
  DateTimeDto adDateTimeFromIso(String isoDateTime);

  String localizeDigits(String text, DigitScriptDto script);
  String toLatinDigits(String text);

  String wireFormatDate(DateDto date, DatePatternDto pattern, DigitScriptDto script);
  DateDto? wireParseDate(String input, DatePatternDto pattern);
  String wireFormatTime(TimeDto time);
  TimeDto? wireParseTime(String input);

  DayStatusDto statusOf(PolicyDto policy, DateDto date);
  List<DayStatusDto> monthStatus(PolicyDto policy, int year, int month);
  List<EventDto> eventsOn(PolicyDto policy, DateDto date);
  List<EventDto> eventsIn(PolicyDto policy, int year, int month);
  bool isNonWorkingDay(PolicyDto policy, DateDto date);
  int workingDaysBetween(PolicyDto policy, DateDto start, DateDto end);
  CalendarDto nextWorkingDay(PolicyDto policy, DateDto from);
  CalendarDto addWorkingDays(PolicyDto policy, DateDto from, int days);

  List<EventDto> eventSpanningDays(EventDto event, int days);
  List<EventDto> eventSpanningThrough(EventDto event, DateDto end);
}

/// Native picker presentation and theming.
@HostApi()
abstract class PickerHostApi {
  /// Presents the modal picker dialog and completes with the confirmed date,
  /// or null when it was dismissed.
  @async
  CalendarDto? showDatePickerDialog(DialogConfigDto config);

  /// Presents the full-screen picker dialog and completes with the confirmed
  /// date, or null when it was dismissed.
  @async
  CalendarDto? showFullScreenDatePickerDialog(DialogConfigDto config);

  /// Applies the palette to every hosted picker, current and future.
  void applyAppearance(AppearanceDto appearance);

  /// Restores every appearance slot to Material's own value.
  void resetAppearance();
}

/// Events flowing back from embedded picker views, keyed by the platform
/// view's id.
@FlutterApi()
abstract class PickerViewFlutterApi {
  void onHeightChanged(int viewId, double height);
  void onDateSelected(int viewId, CalendarDto? date);
  void onRangeSelected(int viewId, CalendarDto? start, CalendarDto? end);
  void onValueChanged(int viewId, DateDto? value);
  void onRangeValueChanged(int viewId, DateDto? start, DateDto? end);
  void onCalendarSystemChanged(int viewId, int era);

  /// The event behind a tapped line of an embedded calendar's month list.
  void onEventTapped(int viewId, EventDto event);

  /// The month an embedded calendar moved to, in the calendar it displays.
  void onDisplayedMonthChanged(int viewId, int year, int month, int era);
}
