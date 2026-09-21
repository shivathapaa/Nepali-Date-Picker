# nepali_date_picker_kmp

Nepali (Bikram Sambat) date picker and BS/AD conversion engine for Flutter, bridging the compiled
[Nepali-Date-Picker](https://github.com/shivathapaa/Nepali-Date-Picker) Kotlin Multiplatform
library. The exact same engine ships to Maven Central, Swift Package Manager and npm
(`@nepali-date-picker/core`), so every platform agrees on every date.

- Conversion engine: BS/AD both ways, month grids, arithmetic, comparisons, ISO 8601,
  locale and Unicode-pattern formatting, Devanagari digits, events, working-day math.
- Material3 pickers rendered by the native Compose implementation: calendar, docked, wheel,
  range, date fields and a full-screen or modal dialog.
- Android and iOS. For Flutter web or desktop use the same engine from npm or Maven.

## Install

```yaml
dependencies:
  nepali_date_picker_kmp: ^3.3.0
```

Android needs `compileSdk 37` and `minSdk 23` or later. iOS needs iOS 15, Swift Package Manager
enabled (the default since Flutter 3.44; there is no CocoaPods support) and two host-app settings
the Compose runtime requires:

- `Info.plist`: `CADisableMinimumFrameDurationOnPhone` set to `YES` (Flutter's template already
  sets it).
- Build settings: `EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64` (the Kotlin framework ships no
  Intel simulator slice).

## The engine

Every call crosses a platform channel, so everything returns a `Future`; the work itself is a
synchronous table lookup on the platform thread. Months and weekdays are 1 based: month 1 is
Baisakh or January, weekday 1 is Sunday through 7 for Saturday. `era` is 1 for AD, 2 for BS.

```dart
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

final today = await NepaliDateConverter.todayBs();
final bs = await NepaliDateConverter.convertAdToBs(2026, 9, 20);
final ad = await NepaliDateConverter.convertBsToAd(2083, 6, 4);

final month = await NepaliDateConverter.bsMonth(2083, 6);
final grid = await NepaliDateConverter.adCalendarsInBsMonth(2083, 6);

final label = await NepaliDateConverter.formatBsDate(
  bs,
  locale: const NepaliDateLocale(
    language: NepaliLanguage.nepali,
    dateFormat: NepaliDateFormatStyle.full,
  ),
);
final pattern = await NepaliDateConverter.formatBsDateByPattern(
  'yyyy MMMM d, EEEE',
  const SimpleDate(2083, 6, 4),
);
final devanagari = await NepaliDateConverter.localizeDigits('2083-06-04');
```

Inputs are validated on the Dart side against the supported ranges (BS 1970..2100,
AD 1913..2043) and the real month lengths; out of range input throws an `ArgumentError` without
crossing the bridge, identically on both platforms.

### Events and working days

The library ships no holiday data. Hand your own events to a policy and ask it about days,
exactly like the npm package's `createCalendarPolicy`:

```dart
final policy = NepaliCalendarPolicy(
  weeklyOffDays: {7},
  events: [
    const NepaliEvent(
      year: 2083, month: 6, dayOfMonth: 17,
      name: 'Vijaya Dashami', kind: NepaliEventKind.governmentPublic,
    ),
  ],
);

final status = await policy.statusOf(const SimpleDate(2083, 6, 17));
final closedDays = await policy.monthStatus(2083, 6);
final workingDays = await policy.workingDaysBetween(
  const SimpleDate(2083, 6, 1),
  const SimpleDate(2083, 7, 1),
);
final deadline = await policy.addWorkingDays(const SimpleDate(2083, 6, 4), 10);
```

`NepaliDateFormatter` and `NepaliTimeFormatter` mirror the fixed-shape wire formatters used by
text fields, with `YYYY-MM-DD` in Latin digits as the canonical form.

## The pickers

The pickers are the library's real Compose Material3 UI, embedded as platform views. Each widget
sizes itself to the height the native side reports, so it drops into any layout, and the two
dialogs present natively with no platform view at all:

```dart
NepaliDatePicker(onDateSelected: (date) => print(date));
NepaliDateRangePicker(onRangeSelected: (start, end) {});
NepaliWheelDatePicker(onDateChange: (date) {});
NepaliDatePickerDocked(label: 'Date of birth', onDateSelected: (date) {});
NepaliDateField(label: 'Appointment', onValueChanged: (date) {});
NepaliDateRangeField(onRangeChanged: (start, end) {});
NepaliCalendarSystemToggle(onCalendarSystemChanged: (system) {});

final picked = await showNepaliDatePickerDialog();
final fullScreen = await showNepaliDatePickerFullScreenDialog(title: 'Select a date');
```

Selection rules cross the bridge as data through `NepaliSelectableDates` (before, after, range,
weekend and closure exclusion); event marks and colours through `NepaliPickerEventOptions`.

### The calendar

`NepaliCalendar` is the browsable month calendar rather than a picker: it fills the width it is
given, reads like a wall patro with both calendars' numbers per cell, and marks the days the
institution named in `events` is closed for.

```dart
NepaliCalendar(
  events: NepaliPickerEventOptions(events: marks),
  showDaySummary: true,
  showMonthEvents: true,
  onDaySelected: (date) => print(date),
  onEventTapped: (event) => print(event.id),
);
```

The day's summary and the month's list are drawn natively inside the same platform view, so all
three share one selection. Leave both off to render those lists in Dart instead, from
`NepaliCalendarPolicy.monthStatus` and `NepaliCalendarPolicy.eventsIn`.

An event's `id` and `payload` cross the bridge untouched and come back with `onEventTapped`, which
is how an app finds the record behind a line. Images belong there: keep the URL in the payload and
draw it yourself with `Image.network` or your image package of choice. Nothing in this plugin
fetches an image.

```dart
NepaliCalendar(
  events: NepaliPickerEventOptions(events: [
    NepaliPickerEvent(
      year: 2083, month: 6, dayOfMonth: 17,
      name: 'Indra Jatra', kind: NepaliEventKind.religious,
      id: 'indra-jatra',
      payload: '{"imageUrl":"https://example.org/jatra.jpg"}',
    ),
  ]),
  showMonthEvents: true,
  onEventTapped: (event) {
    final record = jsonDecode(event.payload!) as Map<String, Object?>;
    showBanner(record['imageUrl']! as String);
  },
);
```

### Theming

`NepaliPickerAppearance` drives the same appearance proxy on both platforms: assign slots, call
`apply()`, and every picker on screen repaints. A null slot keeps Material's own value.

```dart
NepaliPickerAppearance.brightness = NepaliPickerBrightness.dark;
NepaliPickerAppearance.primary = const Color(0xFFB1D18A);
await NepaliPickerAppearance.apply();
```

## Limitations

- Everything is a `Future`; there is no synchronous API over a platform channel.
- Android and iOS only. Web and desktop users take the same engine from npm or Maven Central.
- The picker widgets bring the Compose runtime into the app and run inside platform views; the
  text-field variants take input on the native side, so the software keyboard targets the
  platform view rather than a Flutter text input.
- Custom selectable-date predicates cannot cross the bridge; the shipped rules cover before,
  after, range, weekends and policy closures.
- Event data crosses as plain lists per call, never as a Dart callback into the engine.
- Pathological inputs that only fail deep inside the engine (a policy that never opens, an event
  span past the table's end) surface as errors on Android but can terminate an iOS app. The
  Dart-side validation makes this unreachable for date-shaped input.

The packaged `example/` is a compact single-file demo. The full multi-page showcase lives in the
[repository](https://github.com/shivathapaa/Nepali-Date-Picker) at `sample/flutterApp`.
