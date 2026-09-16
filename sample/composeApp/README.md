# `composeApp` - the shared Compose showcase

The Compose Multiplatform module that holds every UI demo in this repository. Android
([`androidApp`](../androidApp)) and iOS ([`iosApp`](../iosApp)) render the same `App()` composable,
and this module also carries the desktop and browser entry points, so one screen written here shows
up on five platforms.

It depends on `projects.nepaliDatePicker.ui` (the Gradle project, not a published coordinate), which
means a change in the library is visible on the next run with no publishing step.

## Layout

```
src/
├── commonMain/kotlin/sample/app/
│   ├── App.kt                      Scaffold + scrollable tab row; the list of tabs lives here
│   ├── Components.kt               Small shared building blocks (DemoSection, LabeledValue, ...)
│   ├── PickersShowcase.kt          NepaliDatePicker, NepaliDatePickerWithEnglishDate, range picker
│   ├── WheelDockedShowcase.kt      NepaliWheelDatePicker and NepaliDatePickerDocked
│   ├── DialogsShowcase.kt          NepaliDatePickerDialog and NepaliDatePickerFullScreenDialog
│   ├── TextFieldsShowcase.kt       NepaliDateTextField / NepaliDateField and their range variants
│   ├── CustomizationShowcase.kt    Colors, locale, headline and title slots
│   ├── SelectableDatesShowcase.kt  NepaliSelectableDates rules (windows, weekends, holidays)
│   └── UtilitiesShowcase.kt        NepaliDateConverter with no UI involved
├── jvmMain/kotlin/sample/app/main.kt    Desktop window
├── iosMain/kotlin/sample/app/main.kt    MainViewController() for the SwiftUI host
└── webMain/                             Browser entry point, shared by the js and wasmJs targets
```

Adding a demo means adding a file and one `ShowcaseTab` entry in `App.kt`. Every platform picks it
up automatically.

## Targets

`jvm`, `js` (browser), `wasmJs` (browser), `iosArm64` and `iosSimulatorArm64`. The iOS targets
produce a static `ComposeApp` framework that `iosApp` embeds.

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
wheel, docked, full-screen dialog and range field demos all need that opt-in.
