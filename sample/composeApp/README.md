# `composeApp` - the shared Compose showcase

The Compose Multiplatform module that holds every UI demo in this repository. Android
([`androidApp`](../androidApp)) and iOS ([`iosApp`](../iosApp)) render the same `App()` composable,
and this module also carries the desktop and browser entry points, so one screen written here shows
up on five platforms.

It depends on `projects.nepaliDatePicker.ui` and `projects.nepaliDatePicker.serialization` (the Gradle
projects, not published coordinates), which means a change in the library is visible on the next run
with no publishing step.

## Layout

```
src/
├── commonMain/kotlin/dev/shivathapaa/nepalidatepicker/
│   ├── App.kt                      The index and the detail host. Every screen is one
│   │                               ShowcaseEntry in one of four ShowcaseGroups
│   ├── Components.kt               Small shared building blocks (DemoSection, LabeledValue, ...)
│   ├── SampleTheme.kt              The MaterialTheme wrapper and the appearance menu in the top bar
│   ├── SamplePalette.kt            The six palettes the menu offers
│   ├── SampleThemeMode.kt          Light, dark or system brightness
│   ├── SampleColorSchemes.kt       The light and dark ColorScheme behind each palette
│   │
│   │   Pickers
│   ├── PickersShowcase.kt          NepaliDatePicker, NepaliDatePickerWithEnglishDate, range picker,
│   │                               the B.S. / A.D. switch and the filled grid
│   ├── WheelDockedShowcase.kt      NepaliWheelDatePicker and NepaliDatePickerDocked
│   ├── DialogsShowcase.kt          NepaliDatePickerDialog and NepaliDatePickerFullScreenDialog
│   ├── PickerStateShowcase.kt      The state as an API: display mode, year range, writing the
│   │                               selection and the month from outside, both calendars' view
│   ├── HoistedStateShowcase.kt     NepaliDatePickerState built outside composition and owned by a
│   │                               plain state holder, shared by two surfaces
│   ├── CustomizationShowcase.kt    Colors and copy(), locale, headline and title slots, the
│   │                               exported range headline, the dialog's own defaults
│   │
│   │   Fields
│   ├── TextFieldsShowcase.kt       NepaliDateTextField / NepaliDateField and their range variants
│   ├── InputPatternsShowcase.kt    Every NepaliDateFormatter.Pattern as a field, and format /
│   │                               parse / replaceDelimiter without a field at all
│   ├── SelectableDatesShowcase.kt  NepaliSelectableDates rules (windows, weekends, holidays)
│   │
│   │   Events
│   ├── HolidaysShowcase.kt         The "Calendar policy" screen. NepaliCalendarPolicy: an office
│   │                               week vs a school week, merged and filtered providers,
│   │                               working-day arithmetic
│   ├── EventsShowcase.kt           dayDecorator: one colour for a closed day, dots for events,
│   │                               and `then` to carry both on the same day
│   ├── EventSpansShowcase.kt       spanningDays / spanningThrough: an event that runs longer than
│   │                               a day, across a month and a year end, and what it does to
│   │                               counting, blocking and monthStatus
│   ├── EventStylesShowcase.kt      Every display-style switch live, the whole marker palette, and
│   │                               a NepaliDayDecorator written by hand
│   ├── EventSurfacesShowcase.kt    The same marking on all nine surfaces that take a decorator,
│   │                               fields and dialogs included
│   ├── SampleEvents.kt             The sample's stand-in event store and the maps it feeds the
│   │                               decorator
│   ├── SampleDayDetails.kt         The panels the sample draws from that data: one day's detail
│   │                               and a month's agenda
│   ├── SampleEventProvider.kt      The sample's providers, national and one school's own; the
│   │                               library ships no event data
│   │
│   │   Engine
│   ├── UtilitiesShowcase.kt        NepaliDateConverter with no UI involved
│   ├── FormattingShowcase.kt       Every format style in both calendars, the month and weekday
│   │                               names at every length, and the digit scripts
│   ├── PatternsAndIsoShowcase.kt   The five Unicode-pattern formatters, the two clocks, and ISO
│   │                               8601 written and parsed in both calendars
│   ├── MonthQueriesShowcase.kt     Month details, the three cross-calendar listings, and the four
│   │                               NepaliCalendarModel methods the facade does not carry
│   ├── RangeBoundariesShowcase.kt  NepaliCalendarDefaults: the supported years, the anchor days,
│   │                               the convertible window, and how out-of-range input is coerced
│   ├── SerializationShowcase.kt    Every published type round-tripped through JSON with
│   │                               NepaliDatePickerSerializersModule
│   └── SerializationModels.kt      The Json instance and the @Serializable records that screen
│                                   encodes, kept apart from the screen that draws them
├── jvmMain/kotlin/dev/shivathapaa/nepalidatepicker/main.kt    Desktop window
├── iosMain/kotlin/dev/shivathapaa/nepalidatepicker/main.kt    MainViewController() for the SwiftUI host
└── webMain/                             Browser entry point, shared by the js and wasmJs targets
```

Adding a demo means adding a file and one `ShowcaseEntry` in the right `ShowcaseGroup` in `App.kt`.
Every platform picks it up automatically.

## Targets

`android`, `jvm`, `js` (browser), `wasmJs` (browser), `iosArm64` and `iosSimulatorArm64`. The iOS
targets produce a static `ComposeApp` framework that `iosApp` embeds, and the Android target is
what `androidApp` consumes.

## Running

```bash
# Desktop, the fastest loop while iterating on a screen
./gradlew :sample:composeApp:run

# Browser, Compose on Wasm
./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun

# Browser, Compose on JS
./gradlew :sample:composeApp:jsBrowserDevelopmentRun
```

For Android run [`androidApp`](../androidApp); for iOS open [`iosApp`](../iosApp) in Xcode.

## Conventions

The sample follows the same rules as the library: no hardcoded colors, `MaterialTheme` for all
styling, and experimental composables behind `@OptIn(ExperimentalNepaliDatePickerApi::class)`. The
wheel, docked, full-screen dialog, range field and `NepaliCalendarSystemToggle` demos all need that
opt-in. No screen calls a deprecated API; the replacements are in the migration notes of the
[root README](../../README.md#migrating).
