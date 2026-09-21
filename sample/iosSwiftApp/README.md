# `iosSwiftApp` - the native SwiftUI consumer

A plain SwiftUI app that uses the library the way a Swift Package Manager consumer does: it links
`nepali_date_picker.xcframework` and reaches the pickers through the Kotlin `UIViewController`
factories, with no Compose code of its own.

It exercises the iOS bridge end to end: the exported Swift surface, the framework name, the
architecture slices and the Xcode-side requirements.

For the other iOS model, a screen that is entirely Compose Multiplatform, see
[`iosApp`](../iosApp).

## Layout

```
iosSwiftApp/
├── iosSwiftApp.xcodeproj
└── iosSwiftApp/
    ├── SampleApp.swift                 @main entry point
    ├── ContentView.swift               Today's date in BS / Nepali / AD, plus navigation to each screen
    ├── NepaliPickerRepresentables.swift One UIViewControllerRepresentable per Kotlin factory, plus SampleDefaults
    ├── SampleSupport.swift             DemoSection, AutoSized, the argb and boxing helpers, and the
    │                                   Swift-side policies and providers
    ├── SampleAppearance.swift          The appearance menu, applied through NepaliPickerAppearance
    ├── SamplePaletteRoles.swift        The six palettes as ARGB role values, light and dark
    │
    │   Pickers
    ├── PickersScreen.swift             Calendar and range pickers
    ├── CalendarSystemScreen.swift      Bikram Sambat / Gregorian display and the filled grid
    ├── WheelDockedScreen.swift         Wheel and docked pickers
    ├── DialogsScreen.swift             Dialog and full-screen dialog hosts
    ├── ChromeOptionsScreen.swift       Wheel row height and visible count, docked format styles and
    │                                   popup elevation, range chrome flags, dialog shape and
    │                                   elevation, and a narrowed year range
    ├── CustomizationScreen.swift       Locale, visibility toggles, corner radius
    │
    │   Fields
    ├── FieldsScreen.swift              Date and range text fields
    ├── FieldStatesScreen.swift         isError and supportingText, disabled, read only, the filled
    │                                   style's own button words, and every input pattern
    ├── SelectableDatesScreen.swift     NepaliSelectableDates rules
    ├── PolicyCompositionScreen.swift   excludingWeekends / excludingClosures / filtered / plus
    │                                   composing one rule out of parts
    │
    │   Events
    ├── EventsScreen.swift              NepaliEventOptions: an office week, a school week, all six
    │                                   display switches, ARGB colours, dots, and every surface
    ├── EventQueriesScreen.swift        statusOf / eventsIn / monthStatus, spans, working-day
    │                                   arithmetic, and asSelectableDates
    ├── SampleEventData.swift           The event data the screens mark with, spans included
    │
    │   Engine
    ├── UtilitiesScreen.swift           NepaliDateConverter with no picker on screen
    └── EngineQueriesScreen.swift       Month details, cross-calendar listings, the supported range
                                        and its boundaries, Unicode patterns, ISO both ways, and
                                        NepaliDateFormatter
```

`:nepali-date-picker:serialization` is **not** part of this framework: `:ui` exports `:core` only, so
there is nothing to demonstrate here. The Compose showcase covers it instead.

`NepaliPickerRepresentables.swift` is the file worth reading first. Every picker is a thin
representable: build an options object, call the Kotlin factory, forward the height callback.

```swift
let options = NepaliCalendarOptions()
options.showModeToggle = true

return NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
    initialSelectedDate: initialSelectedDate,
    locale: locale,
    yearRangeStart: yearRange.lowerBound,
    yearRangeEnd: yearRange.upperBound,
    selectableDates: selectableDates,
    options: options,
    events: events,
    onHeightChange: { onHeightChange(CGFloat($0)) },
    onDateSelected: onDateSelected
)
```

`events` is a `NepaliEventOptions?`: the weekdays the institution never opens plus the days to mark,
with colours as `0xAARRGGBB` integers because Compose colours cannot cross the bridge. `nil` draws
the calendar plain. Marking never blocks a date; pass `policy.asSelectableDates()` through
`selectableDates` when a screen should refuse the days it marks.

Compose cannot report an intrinsic size to SwiftUI, so each factory takes an `onHeightChange`
callback and the representable drives the SwiftUI frame from it.

`NepaliCalendarSystemToggleViewController` is the one factory that hosts no picker: it renders the
`B.S.` / `A.D.` switch on its own, so the app can drive the displayed calendar from its own chrome.
`CalendarSystemScreen.swift` shows that alongside every picker's `initialCalendarSystem`,
`showCalendarSystemToggle` and `showAdjacentMonthDays` option.

## How the framework gets there

The Xcode project links the XCFramework from the Gradle build output:

```
nepali-date-picker/ui/build/XCFrameworks/release/nepali_date_picker.xcframework
```

An `AssembleXCFramework` aggregate target runs Gradle before the app target, so later builds stay
fresh automatically:

```
./gradlew :nepali-date-picker:ui:assembleNepali-date-pickerReleaseXCFramework
```

**The first build needs the framework to already exist.** Xcode resolves the Swift module graph
while planning the build, so a framework produced by a script phase during that same build arrives
too late and the `import nepali_date_picker` fails. Run the Gradle task once by hand before the
first Xcode build.

The framework comes from `:nepali-date-picker:ui`, which statically embeds and re-exports
`:nepali-date-picker:core`, so `NepaliDateConverter` and `NepaliCalendarDefaults` are available from
the same module.

## Running

```bash
# once, before the first Xcode build
./gradlew :nepali-date-picker:ui:assembleNepali-date-pickerReleaseXCFramework
```

Then open `sample/iosSwiftApp/iosSwiftApp.xcodeproj` and run on a simulator or device.

Or build it from the command line:

```bash
cd sample/iosSwiftApp
xcodebuild \
  -project iosSwiftApp.xcodeproj \
  -scheme iosSwiftApp \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -configuration Debug \
  build
```

## Requirements

| Setting | Value |
| --- | --- |
| Module name | `nepali_date_picker` |
| Swift version | 5.0 |
| Deployment target | iOS 26.1 (lower it in the project settings if your simulator is older) |
| Slices | `iosArm64` and `iosSimulatorArm64`, both arm64 only |

## Related checks

The bridge has Gradle-side guards that run without Xcode:

| Task | What it does |
| --- | --- |
| `./gradlew :nepali-date-picker:ui:checkIosApi` | Compares the linked framework's export surface against `nepali-date-picker/ui/api/ios.api` |
| `./gradlew :nepali-date-picker:ui:dumpIosApi` | Rewrites that snapshot after an intentional API change |
| `./gradlew :nepali-date-picker:ui:checkBridgeCoverage` | Fails if a picker exists in Compose but has no iOS factory |
