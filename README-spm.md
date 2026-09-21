# Nepali Date Picker - Swift Package Manager (iOS, macOS engine)

<p align="center">
  <img src=".github/assets/nepaliDatePickerBanner.png" alt="" width="100%">
</p>

A **Bikram Sambat (Nepali) date picker** for iOS, plus a headless **BS ↔ AD conversion, comparison
and formatting engine** for iOS and macOS. The pickers are the same Material3-aligned Compose
Multiplatform UI that ships to Android, hosted inside a `UIViewController` so SwiftUI and UIKit can
embed them directly.

<p align="center">
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker-SPM/releases">
    <img alt="version" src="https://img.shields.io/github/v/release/shivathapaa/Nepali-Date-Picker-SPM?label=spm%20release" /></a>&nbsp;
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE">
    <img alt="license" src="https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg"/></a>&nbsp;
  <a href="https://shivathapaa.github.io/Nepali-Date-Picker/api/">
    <img src="https://img.shields.io/badge/API%20reference-%E2%86%92-12100E?labelColor=E2E3D8" alt="API reference"/></a>
</p>

> **Where the code lives.** The library is developed in
> [shivathapaa/Nepali-Date-Picker](https://github.com/shivathapaa/Nepali-Date-Picker). The Swift
> package is published to [shivathapaa/Nepali-Date-Picker-SPM](https://github.com/shivathapaa/Nepali-Date-Picker-SPM),
> which carries no source of its own: its `Package.swift`, its README and the release assets are all
> regenerated on every release. Open issues and pull requests against the main repository.

> **Other platforms.** The same calendar tables power
> [`nepali_calendar_utils`](https://github.com/shivathapaa/nepali_calendar_utils) on PyPI and
> [`@nepali-date-picker/core`](https://www.npmjs.com/package/@nepali-date-picker/core) /
> [`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component)
> on npm, so results match across Swift, Kotlin, Python and JavaScript. Their guides are the
> [main README](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README.md) for Kotlin
> Multiplatform and Android, and
> [README-js.md](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-js.md) for the
> web.

<br>

<details>
  <summary><b>Table of Contents</b></summary>

* [Requirements](#requirements)
* [Which product to pick](#which-product-to-pick)
* [Installation](#installation)
* [Required Xcode configuration](#required-xcode-configuration)
* [Swift naming conventions](#swift-naming-conventions)
* [Part 1 - The pickers](#part-1---the-pickers)
    * [How hosting works](#how-hosting-works)
    * [Step 1 - paste the support file](#step-1---paste-the-support-file)
    * [Step 2 - paste the representables](#step-2---paste-the-representables)
    * [Sizing](#sizing)
    * [Options](#options)
    * [What cannot cross the bridge](#what-cannot-cross-the-bridge)
    * [Calendar picker](#calendar-picker)
    * [Range picker](#range-picker)
    * [Docked picker](#docked-picker)
    * [Wheel picker](#wheel-picker)
    * [Dialogs](#dialogs)
    * [Typed entry fields](#typed-entry-fields)
    * [Picker state](#picker-state)
    * [Defaults](#defaults)
    * [Restricting selectable dates](#restricting-selectable-dates)
    * [Marking the days a picker draws](#marking-the-days-a-picker-draws)
    * [Localization and appearance](#localization-and-appearance)
        * [Colours and dark mode](#colours-and-dark-mode)
* [Part 2 - The engine](#part-2---the-engine)
    * [Types](#types)
    * [Enums](#enums)
    * [Today and current time](#today-and-current-time)
    * [Conversion](#conversion)
    * [Month and day counts](#month-and-day-counts)
    * [Arithmetic](#arithmetic)
    * [Compare and count between](#compare-and-count-between)
    * [Names](#names)
    * [Formatting a date](#formatting-a-date)
    * [Formatting time](#formatting-time)
    * [ISO 8601](#iso-8601)
    * [Date and time text (the wire format)](#date-and-time-text-the-wire-format)
    * [Digits](#digits)
    * [Events, holidays and working days](#events-holidays-and-working-days)
    * [NepaliCalendarModel](#nepalicalendarmodel)
    * [Calendar tables](#calendar-tables)
* [Troubleshooting](#troubleshooting)
* [Sample app](#sample-app)
* [Support](#support)
* [License](#license)
</details>

<br>

## Requirements

| | |
| --- | --- |
| Minimum deployment target | iOS 14, macOS 12 (engine only) |
| Architectures, `nepali-date-picker` | `ios-arm64` (device), `ios-arm64-simulator` (Apple silicon) |
| Architectures, `nepali-date-picker-core` | the same two, plus `macos-arm64` |
| Swift tools | 5.5+ |
| Supported range | BS **1970–2100**, AD **1913–2043** |

The archives ship **arm64 only**. There is no `x86_64` simulator slice, so an Intel Mac, or an
Apple silicon Mac building for the Rosetta simulator, cannot link the framework. See
[Required Xcode configuration](#required-xcode-configuration).

> **macOS covers the engine, not the pickers.** `nepali-date-picker-core` carries a `macos-arm64`
> slice, so a macOS app gets the full conversion, comparison and formatting API. The pickers stay
> iOS-only: they are hosted in a `UIViewController`, and Compose Multiplatform publishes no
> embeddable AppKit host to mirror that on macOS. Building a macOS target against the
> `nepali-date-picker` product fails with `no library for this platform was found`. See
> [Troubleshooting](#troubleshooting).

> **Indexing (important).** Months and weekdays are **1-based**. Month `1` = Baisakh … `12` = Chaitra.
> Weekday `1` = Sunday … `7` = Saturday. `era`: `1` = AD, `2` = BS. The first day of the week is
> Sunday, and the default weekend in Nepal is Saturday only.

## Which product to pick

The package vends two products. **Depend on exactly one.**

| Product | Contains | Platforms | Swift import | Download |
| --- | --- | --- | --- | --- |
| `nepali-date-picker` | The Compose pickers **plus** the full conversion engine | iOS 14+ | `nepali_date_picker` | ~60 MB |
| `nepali-date-picker-core` | The conversion engine only, no UI | iOS 14+, macOS 12+ | `nepali_date_picker_core` | ~4 MB |

Pick `nepali-date-picker-core` when you build your own SwiftUI or UIKit interface and only need
BS ↔ AD conversion, month details, formatting and comparison. It is also the only product a
**macOS** target can link.

> **Never add both.** Unlike the Maven artifacts, where `-ui` depends on `-core` and a single copy
> lands on the classpath, each XCFramework is a self-contained static binary and the UI framework
> already embeds the core module. Linking both duplicates the Kotlin runtime and every core symbol.

Everything in [Part 2 - The engine](#part-2---the-engine) is available from **both** products. Only
[Part 1 - The pickers](#part-1---the-pickers) requires `nepali-date-picker`.

## Installation

In Xcode: **File → Add Package Dependencies…**, paste the repository URL, choose a version, then
pick one of the two library products.

```
https://github.com/shivathapaa/Nepali-Date-Picker-SPM.git
```

Or declare it in your own `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/shivathapaa/Nepali-Date-Picker-SPM.git", from: "3.3.0")
],
targets: [
    .target(
        name: "App",
        dependencies: [
            .product(name: "nepali-date-picker", package: "Nepali-Date-Picker-SPM")
            // or, for the engine alone:
            // .product(name: "nepali-date-picker-core", package: "Nepali-Date-Picker-SPM")
        ]
    )
]
```

Then import the module. Note the **underscores**: the module name is derived from the framework
binary, not from the product name.

```swift
import nepali_date_picker      // pickers + engine
// or
import nepali_date_picker_core // engine only
```

## Required Xcode configuration

Three settings are load-bearing. Without them the app fails to link, fails to compile, or crashes on
launch.

| Setting | Value | Why |
| --- | --- | --- |
| `EXCLUDED_ARCHS[sdk=iphonesimulator*]` | `x86_64` | The XCFramework has no Intel simulator slice, so the linker reports `found architecture 'arm64', required architecture 'x86_64'`. |
| `PRODUCT_MODULE_NAME` | anything not equal to `nepali_date_picker` ignoring case | Swift cannot disambiguate two modules whose names differ only by case on a case-insensitive file system. An app named "Nepali Date Picker" defaults to module `Nepali_Date_Picker` and fails with `cannot load module 'Nepali_Date_Picker' as 'nepali_date_picker'`. |
| `CADisableMinimumFrameDurationOnPhone` in `Info.plist` | `YES` | Compose Multiplatform aborts during launch through `PlistSanityCheck` if the key is missing. Only needed for the UI product. |

The plist key cannot be supplied through `INFOPLIST_KEY_…`, because that mechanism only forwards a
fixed set of known keys. With `GENERATE_INFOPLIST_FILE = YES`, point `INFOPLIST_FILE` at a partial
plist and Xcode merges its generated keys into it:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CADisableMinimumFrameDurationOnPhone</key>
	<true/>
</dict>
</plist>
```

## Swift naming conventions

Kotlin declarations arrive through the Objective-C bridge, which renames a few things. These trip
people up:

| Kotlin | Swift | Note |
| --- | --- | --- |
| `NepaliDateConverter` (object) | `NepaliDateConverter.shared` | Every engine call goes through `.shared`. |
| `NameFormat.SHORT` | `NameFormat.short_` | `short` is a C keyword, so a `_` is appended. |
| `NepaliDateFormatStyle.LONG` | `NepaliDateFormatStyle.long_` | Same reason. |
| `Int` | `Int32` | Years, months and days are 32-bit on the bridge. |
| `IntRange` | `KotlinIntRange` | Read `.first` and `.last`; the picker factories take plain `Int32` bounds instead. |
| top-level functions | `<FileName>Kt.function(...)` | For example `NepaliDatePickerViewControllersKt`. |

Two engine functions are **deprecated** and warn; prefer the replacements:

| Deprecated | Use instead |
| --- | --- |
| `convertToNepaliNumber(_:)` | `localizeDigits(_:script:)` with `.devanagari` |
| `convertToEnglishNumber(_:)` | `toLatinDigits(_:)` |

Two more are **unavailable**, so referencing them fails to compile rather than warning:

| Unavailable | Use instead |
| --- | --- |
| `todayNepaliDate` | `todayNepaliSimpleDate` or `todayNepaliCalendar` |
| `todayEnglishDate` | `todayEnglishSimpleDate` or `todayEnglishCalendar` |

---

# Part 1 - The pickers

> Requires the `nepali-date-picker` product. Available from **3.1.1**.

## How hosting works

Compose `@Composable` functions cannot be called from Swift. The library therefore exposes one
factory per picker, each returning a `UIViewController` that hosts the Compose scene already wrapped
in `MaterialTheme` and a `Surface`. That theme follows the device's interface style and anything the
app has set on [`NepaliPickerAppearance`](#colours-and-dark-mode).

Every factory is a top-level Kotlin function, so Swift reaches it through the file's generated class
(`NepaliDatePickerViewControllersKt` and friends). All eight pickers share the same shape, and the
three calendar factories below follow it with their own callbacks:

| Parameter | Type | Notes |
| --- | --- | --- |
| the initial value | `SimpleDate?` | Named per picker: `initialSelectedDate`, `initialDate`, `initialValue`, or the `initialSelectedStartDate` / `initialSelectedEndDate` pair. |
| `locale` | `NepaliDateLocale` | Required, no default across the bridge. See [Localization and appearance](#localization-and-appearance). |
| `yearRangeStart` / `yearRangeEnd` | `Int32` | Two plain bounds, not an `IntRange`. |
| `selectableDates` | `NepaliSelectableDates?` | `nil` allows every date. |
| `options` | one options class per picker, nullable | `nil` takes every library default. See [Options](#options). |
| `events` | `NepaliEventOptions?` | The days to mark. `nil` draws the calendar plain. See [Marking the days a picker draws](#marking-the-days-a-picker-draws). The wheel has no day cells, so it has no `events`. |
| `onHeightChange` | `(KotlinFloat) -> Void` | Content height in points. See [Sizing](#sizing). |
| the callback | closure | Fires on every change, including the initial value. |

> **`onHeightChange` hands you a boxed `KotlinFloat`, not a `Float`.** Kotlin function types box
> their primitive parameters on the Objective-C bridge. `KotlinFloat` is an `NSNumber` subclass, so
> convert with `CGFloat(truncating:)`. Plain `CGFloat($0)` resolves to the deprecated `NSNumber`
> initializer and warns.

The two dialogs add a `calendarOptions` before `options`, and the two field factories add a
`dateFormat` after `locale`.

Here are the exact Swift signatures, copied from the generated header:

```swift
NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
    initialSelectedDate:locale:yearRangeStart:yearRangeEnd:selectableDates:options:events:onHeightChange:onDateSelected:)

NepaliDatePickerViewControllersKt.NepaliDatePickerDockedViewController(
    initialSelectedDate:locale:yearRangeStart:yearRangeEnd:selectableDates:options:events:onHeightChange:onDateSelected:)

NepaliDatePickerViewControllersKt.NepaliWheelDatePickerViewController(
    initialDate:locale:yearRangeStart:yearRangeEnd:selectableDates:options:onHeightChange:onDateChange:)

NepaliDateRangeViewControllersKt.NepaliDateRangePickerViewController(
    initialSelectedStartDate:initialSelectedEndDate:locale:yearRangeStart:yearRangeEnd:selectableDates:options:events:onHeightChange:onRangeSelected:)

NepaliDateFieldViewControllersKt.NepaliDateFieldViewController(
    initialValue:locale:dateFormat:yearRangeStart:yearRangeEnd:selectableDates:options:events:onHeightChange:onValueChange:)

NepaliDateRangeViewControllersKt.NepaliDateRangeFieldViewController(
    initialStartValue:initialEndValue:locale:dateFormat:yearRangeStart:yearRangeEnd:selectableDates:options:events:onHeightChange:onRangeChange:)

NepaliDateDialogViewControllersKt.NepaliDatePickerDialogViewController(
    initialSelectedDate:locale:yearRangeStart:yearRangeEnd:selectableDates:calendarOptions:options:events:onHeightChange:onConfirm:onDismiss:)

NepaliDateDialogViewControllersKt.NepaliDatePickerFullScreenDialogViewController(
    initialSelectedDate:locale:yearRangeStart:yearRangeEnd:selectableDates:calendarOptions:options:events:onHeightChange:onConfirm:onDismiss:)
```

### The browsable calendar

`NepaliCalendar` is the read-a-month surface rather than the pick-a-date one: it fills the frame it
is given, shows both calendars' numbers and the neighbouring months' days by default, and marks the
days the `events` policy closes. Asking for the day's summary or the month's list stacks them inside
the same controller, so all three share one selection.

It takes no `selectableDates`: a calendar browses, and refusing days stays the pickers' job.

```swift
NepaliCalendarViewControllersKt.NepaliCalendarViewController(
    initialSelectedDate:locale:yearRangeStart:yearRangeEnd:options:events:onHeightChange:onDaySelected:onEventTapped:)

NepaliCalendarViewControllersKt.NepaliDaySummaryViewController(
    date:locale:events:onHeightChange:)

NepaliCalendarViewControllersKt.NepaliMonthEventListViewController(
    year:month:locale:events:onHeightChange:onEventTapped:)
```

`onDaySelected` hands back the tapped day together with its `NepaliDayStatus`, so a screen knows
whether the institution is shut and what is named on the day. `onEventTapped` hands back the entry
behind a tapped line with its `id` and `payload` untouched, which is where an app keeps its own
record: a description, a colour, image URLs it fetches and draws itself. Set them on
`NepaliEventInfo` after construction, since the initializer keeps the shape it already had:

```swift
let info = NepaliEventInfo(
    year: 2083, month: 6, dayOfMonth: 17,
    name: "Indra Jatra", kind: .religious, closesOffices: true,
    colorArgb: 0, indicate: false
)
info.id = "indra-jatra"
info.payload = #"{"imageUrl":"https://example.org/jatra.jpg"}"#
```

One more factory hosts the `B.S.` / `A.D.` switch on its own, for when it belongs in your own chrome
(a navigation bar, a settings row) rather than inside a picker. It takes no dates, so it does not
follow the shape above:

```swift
NepaliCalendarSystemToggleViewControllerKt.NepaliCalendarSystemToggleViewController(
    initialCalendarSystem:language:onHeightChange:onCalendarSystemChange:)
```

Kotlin default arguments do not survive the Objective-C bridge, so **every** argument has to be
written out at every call. That is what the two files below are for: paste them once and the rest of
this document becomes one-liners.

From UIKit, embed the controller directly instead:

```swift
let controller = NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
    initialSelectedDate: nil,
    locale: NepaliPickerDefaults.english,
    yearRangeStart: 1970,
    yearRangeEnd: 2100,
    selectableDates: nil,
    options: nil,
    events: nil,
    onHeightChange: { [weak self] height in
        self?.applyHeight(CGFloat(truncating: height))
    },
    onDateSelected: { [weak self] selected in
        self?.apply(selected)
    }
)
addChild(controller)
view.addSubview(controller.view)
controller.didMove(toParent: self)
```

## Step 1 - paste the support file

`NepaliPickerSupport.swift`. Shared locales, the year range, the container that sizes a hosted
picker to the height Compose reports, and the two conversions the bridge needs: `0xAARRGGBB` colours
as signed integers, and weekday numbers as boxed `KotlinInt`s.

```swift
import SwiftUI
import nepali_date_picker

/// A `0xAARRGGBB` literal as the signed integer the bridge takes. An opaque colour overflows
/// `Int32`, so the bit pattern is reinterpreted rather than converted.
func argb(_ value: UInt32) -> Int32 { Int32(bitPattern: value) }

/// Kotlin's `List<Int>` and `Set<Int>` arrive boxed, so weekday numbers are wrapped once here.
extension Array where Element == Int32 {
    var boxed: [KotlinInt] { map { KotlinInt(int: $0) } }

    var boxedSet: Set<KotlinInt> { Set(boxed) }
}

/// Day-of-week numbers, in the library's 1-based-Sunday convention.
enum Weekday {
    static let sunday: Int32 = 1
    static let saturday: Int32 = 7
}

/// Shared defaults so every screen agrees on the year range and locale.
enum NepaliPickerDefaults {
    static let yearRange: ClosedRange<Int32> = {
        let range = NepaliCalendarDefaults.shared.NepaliYearRange
        return range.first...range.last
    }()

    /// Corner radius for every typed field, in points. The Material default is nearly square, which
    /// reads as unfinished next to rounded SwiftUI controls.
    static let fieldCornerRadius: Float = 14

    static let english = NepaliDateLocale(
        language: .english,
        dateFormat: .long_,
        weekDayName: .short_,
        monthName: .full,
        digitScript: nil
    )

    static let nepali = NepaliDateLocale(
        language: .nepali,
        dateFormat: .long_,
        weekDayName: .short_,
        monthName: .full,
        digitScript: nil
    )

    /// Range headlines print both ends, which wraps badly at picker width. A numeric style keeps
    /// them on one line.
    static let englishRange = NepaliDateLocale(
        language: .english,
        dateFormat: .shortYmd,
        weekDayName: .short_,
        monthName: .full,
        digitScript: nil
    )
}

/// Sizes a hosted picker to the height Compose reports, instead of a guessed constant.
///
/// The scene is always measured in `measurementHeight`, while the surrounding layout takes the
/// height Compose reports back. Keep the two frames separate: a scene that shrinks with the layout
/// can never report more than its current height, and stays trapped at its smallest size.
struct AutoSized<Content: View>: View {
    /// Height the scene is measured in. Generous enough for the tallest mode the picker can show.
    var measurementHeight: CGFloat = 420
    @ViewBuilder var content: (@escaping (CGFloat) -> Void) -> Content

    @State private var measured: CGFloat?

    var body: some View {
        content { reported in
            // Compose reports on every layout pass; ignore the noise.
            if measured == nil || abs(measured! - reported) > 0.5 { measured = reported }
        }
        .frame(height: measurementHeight, alignment: .top)
        .frame(height: measured ?? measurementHeight, alignment: .top)
        .clipped()
        // Clip the scene out of hit testing too, so the part hanging below the visible height
        // cannot swallow taps meant for whatever follows it.
        .contentShape(Rectangle())
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

extension CustomCalendar {
    /// The library's typed dates are separate structs, so drop the calendar detail when a plain
    /// year/month/day triple is all an API needs.
    var simple: SimpleDate { SimpleDate(year: year, month: month, dayOfMonth: dayOfMonth) }
}
```

## Step 2 - paste the representables

`NepaliPickerRepresentables.swift`. One thin `UIViewControllerRepresentable` per factory, every
argument filled in, every knob surfaced as a property with the library's own default. Paste the
whole file, or just the wrappers you need.

```swift
import SwiftUI
import nepali_date_picker

/// The full calendar picker, optionally paired with its Gregorian equivalent.
struct NepaliDatePickerView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedDate: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.english
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    /// The days to mark. `nil` draws the calendar plain.
    var events: NepaliEventOptions?
    var showModeToggle: Bool = true
    var showTodayButton: Bool = true
    var showEnglishDate: Bool = false
    var englishDateLocale: NepaliDateLocale?
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onDateSelected: (CustomCalendar?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliCalendarOptions()
        options.showModeToggle = showModeToggle
        options.showTodayButton = showTodayButton
        options.showEnglishDate = showEnglishDate
        options.englishDateLocale = englishDateLocale
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
            initialSelectedDate: initialSelectedDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            events: events,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onDateSelected: onDateSelected
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The range calendar, which tracks a start and an end date.
struct NepaliDateRangePickerView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedStartDate: SimpleDate?
    var initialSelectedEndDate: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.englishRange
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    /// The days to mark. `nil` draws the calendar plain.
    var events: NepaliEventOptions?
    var showModeToggle: Bool = true
    var showTodayButton: Bool = true
    var showMonthsVertically: Bool = true
    var showYearPickerAndMonthNavigation: Bool = true
    var showEnglishDate: Bool = false
    var englishDateLocale: NepaliDateLocale?
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onRangeSelected: (CustomCalendar?, CustomCalendar?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliRangeCalendarOptions()
        options.showModeToggle = showModeToggle
        options.showTodayButton = showTodayButton
        options.showMonthsVertically = showMonthsVertically
        options.showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation
        options.showEnglishDate = showEnglishDate
        options.englishDateLocale = englishDateLocale
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDateRangeViewControllersKt.NepaliDateRangePickerViewController(
            initialSelectedStartDate: initialSelectedStartDate,
            initialSelectedEndDate: initialSelectedEndDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            events: events,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onRangeSelected: onRangeSelected
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The compact field that opens the calendar in a popup.
struct NepaliDatePickerDockedView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedDate: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.english
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    /// The days to mark. `nil` draws the calendar plain.
    var events: NepaliEventOptions?
    var dateFormatStyle: NepaliDateFormatStyle = .medium
    var showTodayButton: Bool = true
    var label: String?
    var placeholder: String?
    var cornerRadius: Float = NepaliPickerDefaults.fieldCornerRadius
    var popupShadowElevation: Float = 6
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onDateSelected: (CustomCalendar?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliDockedOptions()
        options.dateFormatStyle = dateFormatStyle
        options.showTodayButton = showTodayButton
        options.label = label
        options.placeholder = placeholder
        options.cornerRadius = cornerRadius
        options.popupShadowElevation = popupShadowElevation
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDatePickerViewControllersKt.NepaliDatePickerDockedViewController(
            initialSelectedDate: initialSelectedDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            events: events,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onDateSelected: onDateSelected
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The scrolling year/month/day wheel. Always has a selection.
struct NepaliWheelDatePickerView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialDate: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.english
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var itemHeight: Float = 44
    var visibleItemCount: Int32 = 5
    var cornerRadius: Float = 20
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var onDateChange: (CustomCalendar) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliWheelOptions()
        options.itemHeight = itemHeight
        options.visibleItemCount = visibleItemCount
        options.cornerRadius = cornerRadius
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle

        return NepaliDatePickerViewControllersKt.NepaliWheelDatePickerViewController(
            initialDate: initialDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onDateChange: onDateChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// A single typed date entry field, outlined or filled.
struct NepaliDateFieldView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialValue: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.english
    var dateFormat: NepaliDateFormatter.Pattern = .yyyySlashMmSlashDd
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    /// The days to mark. `nil` draws the calendar plain.
    var events: NepaliEventOptions?
    var outlined: Bool = true
    var label: String?
    var placeholder: String?
    var supportingText: String?
    var isError: Bool = false
    var enabled: Bool = true
    var readOnly: Bool = false
    var confirmButtonText: String?
    var dismissButtonText: String?
    var cornerRadius: Float = NepaliPickerDefaults.fieldCornerRadius
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onValueChange: (SimpleDate?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliFieldOptions()
        options.outlined = outlined
        options.label = label
        options.placeholder = placeholder
        options.supportingText = supportingText
        options.isError = isError
        options.enabled = enabled
        options.readOnly = readOnly
        options.confirmButtonText = confirmButtonText
        options.dismissButtonText = dismissButtonText
        options.cornerRadius = cornerRadius
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDateFieldViewControllersKt.NepaliDateFieldViewController(
            initialValue: initialValue,
            locale: locale,
            dateFormat: dateFormat,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            events: events,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onValueChange: onValueChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The paired start and end entry fields.
struct NepaliDateRangeFieldView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialStartValue: SimpleDate?
    var initialEndValue: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.english
    var dateFormat: NepaliDateFormatter.Pattern = .yyyySlashMmSlashDd
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    /// The days to mark. `nil` draws the calendar plain.
    var events: NepaliEventOptions?
    var outlined: Bool = true
    var startLabel: String?
    var endLabel: String?
    var supportingText: String?
    var isStartError: Bool = false
    var isEndError: Bool = false
    var enabled: Bool = true
    var readOnly: Bool = false
    var confirmButtonText: String?
    var dismissButtonText: String?
    var cornerRadius: Float = NepaliPickerDefaults.fieldCornerRadius
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onRangeChange: (SimpleDate?, SimpleDate?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliRangeFieldOptions()
        options.outlined = outlined
        options.startLabel = startLabel
        options.endLabel = endLabel
        options.supportingText = supportingText
        options.isStartError = isStartError
        options.isEndError = isEndError
        options.enabled = enabled
        options.readOnly = readOnly
        options.confirmButtonText = confirmButtonText
        options.dismissButtonText = dismissButtonText
        options.cornerRadius = cornerRadius
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDateRangeViewControllersKt.NepaliDateRangeFieldViewController(
            initialStartValue: initialStartValue,
            initialEndValue: initialEndValue,
            locale: locale,
            dateFormat: dateFormat,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            events: events,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onRangeChange: onRangeChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The modal dialog holding a calendar, presented full screen from SwiftUI.
struct NepaliDatePickerDialogView: UIViewControllerRepresentable {
    var initialSelectedDate: SimpleDate?
    var locale: NepaliDateLocale = NepaliPickerDefaults.english
    var yearRange: ClosedRange<Int32> = NepaliPickerDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    /// The days to mark. `nil` draws the calendar plain.
    var events: NepaliEventOptions?
    var fullScreen: Bool = false
    var title: String?
    var confirmText: String = "OK"
    var dismissText: String = "Cancel"
    var tonalElevation: Float = 6
    var cornerRadius: Float = 28
    var showEnglishDate: Bool = false
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onConfirm: (CustomCalendar?) -> Void
    var onDismiss: () -> Void

    private var options: NepaliDialogOptions {
        let options = NepaliDialogOptions()
        options.title = title
        options.confirmText = confirmText
        options.dismissText = dismissText
        options.tonalElevation = tonalElevation
        options.cornerRadius = cornerRadius
        return options
    }

    /// Configures the calendar the dialog hosts, as opposed to the dialog chrome around it.
    private var calendarOptions: NepaliCalendarOptions {
        let options = NepaliCalendarOptions()
        options.showEnglishDate = showEnglishDate
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays
        return options
    }

    func makeUIViewController(context: Context) -> UIViewController {
        if fullScreen {
            return NepaliDateDialogViewControllersKt.NepaliDatePickerFullScreenDialogViewController(
                initialSelectedDate: initialSelectedDate,
                locale: locale,
                yearRangeStart: yearRange.lowerBound,
                yearRangeEnd: yearRange.upperBound,
                selectableDates: selectableDates,
                calendarOptions: calendarOptions,
                options: options,
                events: events,
                // A dialog is an overlay, so its inline height is meaningless.
                onHeightChange: { _ in },
                onConfirm: onConfirm,
                onDismiss: onDismiss
            )
        }
        return NepaliDateDialogViewControllersKt.NepaliDatePickerDialogViewController(
            initialSelectedDate: initialSelectedDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            calendarOptions: calendarOptions,
            options: options,
            events: events,
            onHeightChange: { _ in },
            onConfirm: onConfirm,
            onDismiss: onDismiss
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The `B.S.` / `A.D.` switch on its own, for driving a picker from the app's own chrome.
struct NepaliCalendarSystemToggleView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var language: NepaliDatePickerLang = .english
    var onCalendarSystemChange: (CalendarSystem) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        NepaliCalendarSystemToggleViewControllerKt.NepaliCalendarSystemToggleViewController(
            initialCalendarSystem: initialCalendarSystem,
            language: language,
            onHeightChange: { onHeightChange(CGFloat(truncating: $0)) },
            onCalendarSystemChange: onCalendarSystemChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

Every section from [Calendar picker](#calendar-picker) onwards assumes these two files are in your
target.

## Sizing

A hosted controller has no intrinsic height, so SwiftUI needs a `.frame(height:)`. Guessing one
crops the last week of the calendar, or leaves a void under a text field. Every factory therefore
takes `onHeightChange`, which reports the content height in points as Compose measures it. The
`AutoSized` container from [Step 1](#step-1---paste-the-support-file) applies that report for you.

`measurementHeight` is the height the *scene* is measured in, not a floor on the layout. It has to be
generous enough for the tallest mode the picker can show, because Compose measures inside the frame
it is given and a picker with too little room lays out cropped, then reports that cropped height.
The surrounding layout still collapses to whatever Compose reports back.

Workable values, the same ones the sample app uses:

| Picker | `measurementHeight` |
| --- | --- |
| Calendar | `560` |
| Calendar with Gregorian dates | `620` |
| Range calendar | `640` |
| Wheel | `240` |
| Docked picker, typed fields | the `420` default |

```swift
@State private var selected: CustomCalendar?
@State private var typed: SimpleDate?

AutoSized(measurementHeight: 560) { report in
    NepaliDatePickerView(onHeightChange: report, initialSelectedDate: nil) { selected = $0 }
}

AutoSized { report in
    NepaliDateFieldView(onHeightChange: report, initialValue: nil) { typed = $0 }
}
```

### Two more rules

- **Changing a parameter does not rebuild it.** `makeUIViewController` runs once per view identity.
  To apply a new locale, attach `.id(...)` keyed on whatever changed.
- **Give the grid room.** The calendar needs close to the full screen width. Nesting padding inside
  a padded card truncates the headline and clips the Gregorian sub-labels in the rightmost column.

## Options

The customization each picker accepts is gathered into one options class per picker, because Kotlin
default arguments do not survive the Objective-C bridge. Construct it empty, set only what you want
to change, and pass `nil` to accept every default. The representables in
[Step 2](#step-2---paste-the-representables) already do this, so you normally set a property on the
wrapper instead:

```swift
// Through the representable.
AutoSized(measurementHeight: 560) { report in
    NepaliDatePickerView(onHeightChange: report, initialSelectedDate: nil, showTodayButton: false) {
        selected = $0
    }
}

// Or against the factory directly.
let options = NepaliCalendarOptions()   // every property already holds the library default
options.showTodayButton = false

NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
    initialSelectedDate: nil,
    locale: NepaliPickerDefaults.english,
    yearRangeStart: 1970,
    yearRangeEnd: 2100,
    selectableDates: nil,
    options: options,                   // or nil for the defaults
    events: nil,                        // nothing marked
    onHeightChange: { _ in },
    onDateSelected: { selected = $0 }
)
```

Marking is separate from these: `events` is its own parameter on every factory that draws a month
grid, documented under [Events, holidays and working days](#events-holidays-and-working-days).

Every property, with its default:

| Class | Property | Type | Default |
| --- | --- | --- | --- |
| `NepaliCalendarOptions` | `showModeToggle` | `Bool` | `true` |
| | `showTodayButton` | `Bool` | `true` |
| | `showEnglishDate` | `Bool` | `false` |
| | `englishDateLocale` | `NepaliDateLocale?` | `nil`, falls back to the picker's locale |
| | `initialCalendarSystem` | `CalendarSystem` | `.bikramSambat` |
| | `showCalendarSystemToggle` | `Bool` | `false` |
| | `showAdjacentMonthDays` | `Bool` | `false` |
| `NepaliRangeCalendarOptions` | `showModeToggle` | `Bool` | `true` |
| | `showTodayButton` | `Bool` | `true` |
| | `showMonthsVertically` | `Bool` | `true` |
| | `showYearPickerAndMonthNavigation` | `Bool` | `true` |
| | `showEnglishDate` | `Bool` | `false` |
| | `englishDateLocale` | `NepaliDateLocale?` | `nil` |
| | `initialCalendarSystem` | `CalendarSystem` | `.bikramSambat` |
| | `showCalendarSystemToggle` | `Bool` | `false`, needs `showYearPickerAndMonthNavigation` |
| | `showAdjacentMonthDays` | `Bool` | `false` |
| `NepaliWheelOptions` | `itemHeight` | `Float` | `44` |
| | `visibleItemCount` | `Int32` | `5`, odd numbers centre the selection |
| | `cornerRadius` | `Float` | `20` |
| | `initialCalendarSystem` | `CalendarSystem` | `.bikramSambat` |
| | `showCalendarSystemToggle` | `Bool` | `false` |
| `NepaliDockedOptions` | `dateFormatStyle` | `NepaliDateFormatStyle` | `.medium` |
| | `showTodayButton` | `Bool` | `true` |
| | `label` | `String?` | `nil` |
| | `placeholder` | `String?` | `nil` |
| | `cornerRadius` | `Float` | `4` |
| | `popupShadowElevation` | `Float` | `6` |
| | `initialCalendarSystem` | `CalendarSystem` | `.bikramSambat` |
| | `showCalendarSystemToggle` | `Bool` | `false` |
| | `showAdjacentMonthDays` | `Bool` | `false` |
| `NepaliFieldOptions` | `outlined` | `Bool` | `true` |
| | `label` | `String?` | `nil` |
| | `placeholder` | `String?` | `nil`, keeps the hint spelling out the pattern |
| | `supportingText` | `String?` | `nil` |
| | `isError` | `Bool` | `false` |
| | `enabled` | `Bool` | `true` |
| | `readOnly` | `Bool` | `false` |
| | `confirmButtonText` | `String?` | `nil`, keeps the localized "OK" |
| | `dismissButtonText` | `String?` | `nil`, keeps the localized "Cancel" |
| | `cornerRadius` | `Float` | `4` |
| | `initialCalendarSystem` | `CalendarSystem` | `.bikramSambat` |
| | `showCalendarSystemToggle` | `Bool` | `false`, only the filled style has a dialog to show it in |
| | `showAdjacentMonthDays` | `Bool` | `false`, same |
| `NepaliRangeFieldOptions` | `outlined` | `Bool` | `true` |
| | `startLabel` | `String?` | `nil`, keeps the localized "Start Date" |
| | `endLabel` | `String?` | `nil`, keeps the localized "End Date" |
| | `supportingText` | `String?` | `nil` |
| | `isStartError` | `Bool` | `false` |
| | `isEndError` | `Bool` | `false` |
| | `enabled` | `Bool` | `true` |
| | `readOnly` | `Bool` | `false` |
| | `confirmButtonText` | `String?` | `nil` |
| | `dismissButtonText` | `String?` | `nil` |
| | `cornerRadius` | `Float` | `4` |
| | `initialCalendarSystem` | `CalendarSystem` | `.bikramSambat` |
| | `showCalendarSystemToggle` | `Bool` | `false`, only the filled style has a dialog to show it in |
| | `showAdjacentMonthDays` | `Bool` | `false`, same |
| `NepaliDialogOptions` | `title` | `String?` | `nil`, only the full-screen dialog draws one |
| | `confirmText` | `String` | `"OK"` |
| | `dismissText` | `String` | `"Cancel"` |
| | `tonalElevation` | `Float` | `6`, ignored by the full-screen dialog |
| | `cornerRadius` | `Float` | `28` |

A `nil` text property keeps the library's own default rather than blanking it, so leaving
`startLabel` alone still shows the localized "Start Date". Dimensions are in points.

`showAdjacentMonthDays` fills the grid's empty cells with the neighbouring months' days, drawn
faded; tapping one selects that day and moves the grid to its month.

`initialCalendarSystem` and `showCalendarSystemToggle` control which calendar the grid shows and
whether the user can switch it. Only the display changes: the date handed back to Swift is always
Bikram Sambat, so switching keeps the same day selected. On the field options these apply to the
calendar the field types in, and to the dialog the filled style opens; the outlined style has no
dialog, so it reads `initialCalendarSystem` and ignores the other two.

`outlined` picks between the two Material styles: `NepaliDateTextField` / `NepaliDateRangeTextField`
when true, and the filled `NepaliDateField` / `NepaliDateRangeField` when false. The filled variants
open a confirmation dialog, which is what `confirmButtonText` and `dismissButtonText` label.

## What cannot cross the bridge

These parameters take Compose types Swift cannot construct, so they stay Kotlin-only and the
factories use the library defaults:

`modifier`, `colors`, `textStyle` / `selectedTextStyle` / `unselectedTextStyle`, `keyboardOptions`,
`keyboardActions`, `interactionSource`, `dialogProperties`, and the `@Composable` slots
(`title`, `headline`, `leadingIcon`, `trailingIcon`, `prefix`, `suffix`). Shapes are covered by the
`cornerRadius` properties instead. Everything else the composables accept is reachable.

One engine call is absent for the same reason. `getNepaliCalendarsInEnglishMonth` answers
`List<CustomCalendar?>` in Kotlin, and Objective-C cannot describe an optional list *element*, so it
is not exported. Call `getNepaliCalendarsInEnglishMonthByDay` instead: it returns
`[NepaliEnglishMonthDay]`, one entry per Gregorian day, each carrying `englishDayOfMonth` and an
optional `nepaliCalendar`.

```swift
for day in NepaliDateConverter.shared.getNepaliCalendarsInEnglishMonthByDay(
    englishYear: 2026, englishMonth: 9
) {
    if let nepali = day.nepaliCalendar {
        print(day.englishDayOfMonth, nepali.year, nepali.month, nepali.dayOfMonth)
    }
}
```

The `nepali-date-picker-serialization` Maven artifact is also absent: a `KSerializer` is a
Kotlin-only construct and does not cross to Objective-C. Swift has `Codable` instead, and it reaches
the same payloads: see
[Date and time text (the wire format)](#date-and-time-text-the-wire-format) for the exact shapes and
paste-ready structs.

## Calendar picker

`NepaliDatePickerViewControllersKt.NepaliDatePickerViewController`

| Parameter | Type | Meaning |
| --- | --- | --- |
| `initialSelectedDate` | `SimpleDate?` | Pre-selected date, or `nil` for none. Also sets the displayed month. |
| `locale` | `NepaliDateLocale` | Language, date format, name widths, digit script. |
| `yearRangeStart` / `yearRangeEnd` | `Int32` | Selectable BS year bounds. |
| `selectableDates` | `NepaliSelectableDates?` | Which dates are enabled. |
| `options` | `NepaliCalendarOptions?` | Appearance and behaviour, or `nil` for the defaults. See [Options](#options). |
| `events` | `NepaliEventOptions?` | Days to mark, or `nil` for a plain calendar. See [Marking the days a picker draws](#marking-the-days-a-picker-draws). |
| `onHeightChange` | `(KotlinFloat) -> Void` | Measured content height in points. See [Sizing](#sizing). |
| `onDateSelected` | `(CustomCalendar?) -> Void` | Fires on every change. |

```swift
struct CalendarExample: View {
    @State private var selected: CustomCalendar?

    var body: some View {
        VStack(spacing: 12) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 15)
                ) { selected = $0 }
            }
            Text(selected.map { "\($0.year)/\($0.month)/\($0.dayOfMonth)" } ?? "none")
        }
    }
}
```

Set `showEnglishDate` to pair every Bikram Sambat day with its Gregorian equivalent. That variant is
taller, so raise `measurementHeight` to `620`:

```swift
AutoSized(measurementHeight: 620) { report in
    NepaliDatePickerView(
        onHeightChange: report,
        initialSelectedDate: nil,
        showEnglishDate: true,
        englishDateLocale: NepaliPickerDefaults.english
    ) { selected = $0 }
}
```

### Showing the Gregorian calendar

The same grid can display either calendar. `showCalendarSystemToggle` draws a `B.S.` / `A.D.` switch
in the header, and `initialCalendarSystem` decides which one it opens on. Only the display changes:
`onDateSelected` always hands back a Bikram Sambat `CustomCalendar`, so switching keeps the same day
selected and any `NepaliSelectableDates` rule keeps working untouched.

```swift
AutoSized(measurementHeight: 580) { report in
    NepaliDatePickerView(
        onHeightChange: report,
        initialSelectedDate: SimpleDate(year: 2083, month: 6, dayOfMonth: 1),
        initialCalendarSystem: .gregorian,   // opens on September 2026
        showCalendarSystemToggle: true       // and the user can switch back
    ) { selected = $0 }
}
```

Days before AD 1913-04-13 have no Bikram Sambat equivalent, so a Gregorian grid draws them disabled
rather than hiding the month. `NepaliDateConverter.shared.isEnglishDateConvertible(...)` answers that
for a single date.

### Filling the empty cells

`showAdjacentMonthDays` fills the blank cells around the month with the days either side of it, drawn
faded, the way a wall calendar does. Tapping one selects that day and moves the grid to its month. The
fill reaches the end of the last row holding a day of the displayed month and stops there, so a short
month still ends on a blank row.

```swift
AutoSized(measurementHeight: 580) { report in
    NepaliDatePickerView(
        onHeightChange: report,
        initialSelectedDate: SimpleDate(year: 2083, month: 4, dayOfMonth: 15),
        showAdjacentMonthDays: true
    ) { selected = $0 }
}
```

A borrowed day the picker cannot select is drawn faded and inert: it neither selects nor moves the
grid. At the first and last month the picker covers there is no neighbour to borrow from, so those
cells stay blank. VoiceOver reads a borrowed day with its own month and year plus a note that choosing
it moves the grid, since the fading that says so to a sighted user says nothing to a screen reader.

### Driving the switch from your own chrome

`NepaliCalendarSystemToggleViewController` hosts the switch on its own, for a navigation bar or a
settings row. The picker seeds its calendar once, so give it an `id` that changes with the selection to
rebuild it:

```swift
struct ChromeSwitchExample: View {
    @State private var system: CalendarSystem = .bikramSambat
    @State private var selected: CustomCalendar?

    var body: some View {
        VStack(spacing: 12) {
            AutoSized(measurementHeight: 60) { report in
                NepaliCalendarSystemToggleView(onHeightChange: report) { system = $0 }
            }
            AutoSized(measurementHeight: 580) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    initialCalendarSystem: system
                ) { selected = $0 }
                .id(system)
            }
        }
    }
}
```

## Range picker

`NepaliDateRangeViewControllersKt.NepaliDateRangePickerViewController`

| Parameter | Type | Meaning |
| --- | --- | --- |
| `initialSelectedStartDate` | `SimpleDate?` | Start of the pre-selected range, or `nil`. |
| `initialSelectedEndDate` | `SimpleDate?` | End of the pre-selected range, or `nil`. |
| `locale` | `NepaliDateLocale` | Language, date format, name widths, digit script. |
| `yearRangeStart` / `yearRangeEnd` | `Int32` | Selectable BS year bounds. |
| `selectableDates` | `NepaliSelectableDates?` | Which dates are enabled. |
| `options` | `NepaliRangeCalendarOptions?` | Adds `showMonthsVertically` and `showYearPickerAndMonthNavigation`. |
| `events` | `NepaliEventOptions?` | Days to mark, or `nil` for a plain calendar. |
| `onHeightChange` | `(KotlinFloat) -> Void` | Measured content height in points. |
| `onRangeSelected` | `(CustomCalendar?, CustomCalendar?) -> Void` | Either end may be `nil` while the range is still being built. |

The headline prints both dates, which wraps mid-word at picker width, so the wrapper defaults to
`NepaliPickerDefaults.englishRange` with its numeric `.shortYmd` format.

```swift
struct RangeExample: View {
    @State private var start: CustomCalendar?
    @State private var end: CustomCalendar?

    var body: some View {
        AutoSized(measurementHeight: 640) { report in
            NepaliDateRangePickerView(
                onHeightChange: report,
                initialSelectedStartDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 5),
                initialSelectedEndDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 20),
                showMonthsVertically: true
            ) { newStart, newEnd in
                start = newStart
                end = newEnd
            }
        }
    }
}
```

## Docked picker

`NepaliDatePickerViewControllersKt.NepaliDatePickerDockedViewController`

A compact field that opens the calendar in a popup. `NepaliDockedOptions` carries `dateFormatStyle`,
which controls how the chosen date is written inside the field, plus `label`, `placeholder`,
`cornerRadius` and `popupShadowElevation`.

The controller wraps the field, so left alone it settles at roughly the field's height. A Compose
popup is clipped to its host, so raise `measurementHeight` to about `380` if you want the calendar to
open inline. That space sits empty while the popup is closed, which is the trade you are making.

```swift
struct DockedExample: View {
    @State private var selected: CustomCalendar?

    var body: some View {
        AutoSized(measurementHeight: 380) { report in
            NepaliDatePickerDockedView(
                onHeightChange: report,
                initialSelectedDate: nil,
                dateFormatStyle: .medium,
                label: "Date of birth",
                placeholder: "Pick a date"
            ) { selected = $0 }
        }
    }
}
```

## Wheel picker

`NepaliDatePickerViewControllersKt.NepaliWheelDatePickerViewController`

Scrolling year / month / day columns. Takes `initialDate` (`nil` means today) and always has a
selection, so `onDateChange` receives a non-optional `CustomCalendar`.

```swift
struct WheelExample: View {
    @State private var selected: CustomCalendar?

    var body: some View {
        AutoSized(measurementHeight: 240) { report in
            NepaliWheelDatePickerView(
                onHeightChange: report,
                initialDate: nil,          // nil starts on today
                locale: NepaliPickerDefaults.nepali,
                itemHeight: 44,
                visibleItemCount: 5,
                cornerRadius: 20
            ) { selected = $0 }
        }
    }
}
```

## Dialogs

`NepaliDateDialogViewControllersKt.NepaliDatePickerDialogViewController` and
`…NepaliDatePickerFullScreenDialogViewController`

Both render the dialog immediately into a transparent controller, so add it over your content with
`overlay` and remove it when the callback fires. They take a `NepaliDialogOptions` for the chrome and
a separate `calendarOptions` for the calendar inside, which is why the wrapper above surfaces
`showEnglishDate`, `initialCalendarSystem`, `showCalendarSystemToggle` and `showAdjacentMonthDays` as
its own properties. `tonalElevation` is ignored by the full-screen variant, which has no floating
surface. Neither reports a useful inline height, so the wrapper discards `onHeightChange`.

Do not wrap either one in a `sheet` or a `fullScreenCover`. The dialog already covers the screen, and
a modal presentation around it closes out of step with it.

```swift
struct DialogExample: View {
    @State private var showing = false
    @State private var showingFullScreen = false
    @State private var confirmed: CustomCalendar?

    var body: some View {
        VStack(spacing: 12) {
            Button("Open dialog") { showing = true }
            Button("Open full-screen dialog") { showingFullScreen = true }
            Text(confirmed.map { "\($0.year)/\($0.month)/\($0.dayOfMonth)" } ?? "none")
        }
        .overlay {
            if showing {
                NepaliDatePickerDialogView(
                    initialSelectedDate: SimpleDate(year: 2081, month: 3, dayOfMonth: 5),
                    confirmText: "OK",
                    dismissText: "Cancel",
                    onConfirm: { date in
                        confirmed = date
                        showing = false
                    },
                    onDismiss: { showing = false }
                )
                .ignoresSafeArea()
            }
        }
        .overlay {
            if showingFullScreen {
                NepaliDatePickerDialogView(
                    initialSelectedDate: nil,
                    fullScreen: true,
                    title: "Pick a date",
                    onConfirm: { date in
                        confirmed = date
                        showingFullScreen = false
                    },
                    onDismiss: { showingFullScreen = false }
                )
                .ignoresSafeArea()
            }
        }
    }
}
```

## Typed entry fields

`NepaliDateFieldViewControllersKt.NepaliDateFieldViewController` handles a single date and
`NepaliDateRangeViewControllersKt.NepaliDateRangeFieldViewController` the start / end pair. Both take
a `dateFormat: NepaliDateFormatter.Pattern` that masks typing, and an options object whose `outlined`
flag chooses the Material style. The callback delivers `nil` while the entry is incomplete or
invalid.

| `NepaliDateFormatter.Pattern` | Types as |
| --- | --- |
| `.yyyySlashMmSlashDd` | `2081/01/15` |
| `.yyyyDashMmDashDd` | `2081-01-15` |
| `.ddSlashMmSlashYyyy` | `15/01/2081` |
| `.ddDashMmDashYyyy` | `15-01-2081` |

```swift
struct FieldsExample: View {
    @State private var value: SimpleDate?
    @State private var start: SimpleDate?
    @State private var end: SimpleDate?

    var body: some View {
        VStack(spacing: 16) {
            // Outlined, the default style.
            AutoSized { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: nil,
                    dateFormat: .yyyySlashMmSlashDd,
                    label: "Joining date",
                    supportingText: "Bikram Sambat"
                ) { value = $0 }
            }

            // Filled, which opens a confirmation dialog on tap.
            AutoSized { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: SimpleDate(year: 2081, month: 1, dayOfMonth: 15),
                    dateFormat: .ddDashMmDashYyyy,
                    outlined: false,
                    label: "Joining date",
                    confirmButtonText: "OK",
                    dismissButtonText: "Cancel"
                ) { value = $0 }
            }

            AutoSized { report in
                NepaliDateRangeFieldView(
                    onHeightChange: report,
                    initialStartValue: nil,
                    initialEndValue: nil,
                    dateFormat: .yyyySlashMmSlashDd,
                    startLabel: "From",
                    endLabel: "To"
                ) { newStart, newEnd in
                    start = newStart
                    end = newEnd
                }
            }
        }
    }
}
```

## Picker state

The factories own their state, so most apps only need the callbacks. When you want to read more than
the selection, build the state yourself and keep a reference. Both factories are top-level Kotlin
functions and work outside composition:

```swift
let state = NepaliDatePickerKt.NepaliDatePickerState(
    initialSelectedDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 15),
    initialDisplayedMonth: nil,      // nil follows the selection
    yearRange: NepaliCalendarDefaults.shared.NepaliYearRange,
    initialDisplayMode: 0,           // 0 = calendar, 1 = typed input
    nepaliSelectableDates: NepaliDatePickerDefaults.shared.AllDates,
    locale: NepaliPickerDefaults.english,
    initialCalendarSystem: .bikramSambat
)
```

| `NepaliDatePickerState` | Type | Meaning |
| --- | --- | --- |
| `selectedDate` | `CustomCalendar?` | The selection, always Bikram Sambat. |
| `selectedEnglishDate` | `CustomCalendar?` | The same day in Gregorian. |
| `displayedMonth` | `NepaliMonthCalendar` | The Bikram Sambat month holding the first day of the visible grid. |
| `displayedCalendarSystem` | `CalendarSystem` | Which calendar the grid shows. Assigning it re-anchors the grid on the selection. |
| `displayedMonthCalendar` | `MonthCalendar` | The month on screen, in whichever calendar is displayed. |
| `englishYearRange` | `KotlinIntRange` | Gregorian years derived from `yearRange`, clamped into `EnglishYearRange`. |
| `displayMode` | `Int32` | `0` calendar, `1` typed input. |
| `yearRange` | `KotlinIntRange` | Selectable Bikram Sambat years. |
| `nepaliSelectableDates` | `NepaliSelectableDates` | The rule deciding which dates are enabled. |
| `locale` | `NepaliDateLocale` | Language, format and digit script. |

`NepaliDateRangePickerState` mirrors it with `selectedStartNepaliDate`, `selectedEndNepaliDate`,
`selectedStartEnglishDate`, `selectedEndEnglishDate`, and a
`setSelection(startNepaliDate:endNepaliDate:)` for driving it programmatically:

```swift
let rangeState = NepaliDateRangePickerKt.NepaliDateRangePickerState(
    initialSelectedStartNepaliDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 5),
    initialSelectedEndNepaliDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 20),
    initialDisplayedMonth: nil,
    yearRange: NepaliCalendarDefaults.shared.NepaliYearRange,
    initialDisplayMode: 0,
    nepaliSelectableDates: NepaliDatePickerDefaults.shared.AllDates,
    locale: NepaliPickerDefaults.englishRange,
    initialCalendarSystem: .bikramSambat
)

// setSelection takes CustomCalendar, not SimpleDate, so build the ends through the converter.
let converter = NepaliDateConverter.shared
rangeState.setSelection(
    startNepaliDate: converter.getNepaliCalendar(nepaliYYYY: 2081, nepaliMM: 2, nepaliDD: 1),
    endNepaliDate: converter.getNepaliCalendar(nepaliYYYY: 2081, nepaliMM: 2, nepaliDD: 10)
)
```

Note that the state factory names the range ends `initialSelectedStartNepaliDate` /
`initialSelectedEndNepaliDate`, while the view controller factory shortens them to
`initialSelectedStartDate` / `initialSelectedEndDate`.

> These states are Compose snapshot objects. Reading them from Swift gives a value at that moment;
> they do not publish to SwiftUI. Use the `onDateSelected` / `onRangeSelected` callbacks to observe
> changes.

## Defaults

`NepaliDatePickerDefaults.shared` holds what the pickers fall back to:

| Member | Meaning |
| --- | --- |
| `AllDates` | A `NepaliSelectableDates` that allows every date. |
| `DefaultLocale` | English, `LONG` format, full names. |
| `DefaultRangePickerLocale` | The range pickers' default locale. |
| `DateFormatStyle` | Default style used when writing a selected date. |
| `FIRST_DAY_OF_WEEK` | `1`, Sunday. |
| `TonalElevation` | Dialog tonal elevation. |

`NepaliDatePickerColors` is also exported, but every field is a Compose `Color`, which Swift cannot
construct. Theme the pickers through `NepaliPickerAppearance` instead, which takes the same roles as
`0xAARRGGBB` integers and also carries the light or dark choice. See
[Colours and dark mode](#colours-and-dark-mode).

## Restricting selectable dates

Three helpers build a policy for you:

```swift
let converter = NepaliDateConverter.shared
let today = converter.todayNepaliSimpleDate
let inThirtyDays = converter.getNepaliCalendarAfterAdditionOrSubtraction(
    year: today.year, month: today.month, dayOfMonth: today.dayOfMonth, daysToAdjust: 30
).simple

let futureOnly = converter.AfterDateSelectable(simpleDate: today, includeDate: false)
let pastAndToday = converter.BeforeDateSelectable(simpleDate: today, includeDate: true)
let nextMonth = converter.DateRangeSelectable(
    minDate: today,
    maxDate: inThirtyDays,
    includeMinDate: true,
    includeMaxDate: true
)
```

Hand any of them to a picker through `selectableDates`:

```swift
AutoSized(measurementHeight: 560) { report in
    NepaliDatePickerView(
        onHeightChange: report,
        initialSelectedDate: nil,
        selectableDates: futureOnly
    ) { selected = $0 }
}
```

`NepaliSelectableDates` is a plain Kotlin interface, so your app can implement it directly in Swift:

```swift
final class EvenDaysOnly: NepaliSelectableDates {
    func isSelectableDate(customCalendar: CustomCalendar) -> Bool {
        customCalendar.dayOfMonth % 2 == 0
    }
    func isSelectableYear(year: Int32) -> Bool { true }
}
```

Disallowed dates render greyed out rather than disappearing.

## Localization and appearance

One `NepaliDateLocale` drives language, the formatted headline, name widths and digits. It has no
default across the bridge, which is why [Step 1](#step-1---paste-the-support-file) defines a few:

```swift
let nepali = NepaliDateLocale(
    language: .nepali,      // .english | .nepali
    dateFormat: .long_,     // headline style
    weekDayName: .short_,   // column headers
    monthName: .full,
    digitScript: nil        // nil follows the language
)

@State private var useNepali = true

AutoSized(measurementHeight: 560) { report in
    NepaliDatePickerView(
        onHeightChange: report,
        initialSelectedDate: nil,
        locale: useNepali ? nepali : NepaliPickerDefaults.english
    ) { selected = $0 }
}
// makeUIViewController runs once per view identity, so a changed locale needs a new identity.
.id(useNepali)
```

> **Use `.short_` for `weekDayName`.** The calendar grid renders the *medium* weekday name ("Sun")
> for every format except `SHORT`, and that overflows the narrow day columns on a phone. `SHORT`
> gives the single letter the grid is sized for.

> **Prefer `.long_` for `dateFormat` in a picker.** `FULL` puts the weekday in the headline, which
> then shows the same single letter the grid uses. `LONG` omits the weekday entirely
> ("Baisakh 15, 2081").

### Colours and dark mode

`NepaliPickerAppearance` is a shared appearance proxy, in the sense `UINavigationBar.appearance()`
is one: set it once and every hosted picker, field and dialog repaints, including the ones already
on screen. It exists because a Swift caller has no way to install a Compose theme around a hosted
controller.

Every colour is a `0xAARRGGBB` value, and `0` means "use Material's own value for this role", so an
app can override only its accent and leave the rest alone. `brightness` is `.system` by default,
which follows the device's interface style and switches with it.

```swift
let appearance = NepaliPickerAppearance.shared

appearance.brightness = .system              // .system | .light | .dark
appearance.primaryArgb = argb(0xFF4C662B)    // selected day, today's ring, confirm button
appearance.onPrimaryArgb = argb(0xFFFFFFFF)  // the number inside a selected day
appearance.surfaceArgb = argb(0xFFF9FAEF)    // the picker's background
appearance.onSurfaceArgb = argb(0xFF1A1C16)  // day numbers and headlines
```

`argb` is the helper from [Step 1](#step-1---paste-the-support-file): an opaque colour overflows
`Int32`, so the bit pattern is reinterpreted rather than converted.

| Role | What it colours |
| --- | --- |
| `primaryArgb` | Selected days, the today ring, the confirm button, the cursor. |
| `onPrimaryArgb` | Content on top of `primaryArgb`. |
| `primaryContainerArgb` / `onPrimaryContainerArgb` | A selected year, and a range's endpoints. |
| `secondaryContainerArgb` / `onSecondaryContainerArgb` | The days inside a selected range. |
| `surfaceArgb` / `onSurfaceArgb` | The picker background, and the text on it. |
| `surfaceVariantArgb` / `onSurfaceVariantArgb` | Field surfaces, weekday letters, supporting text. |
| `outlineArgb` | Field borders, dividers, the outline of an unselected day. |

Setting both `surfaceArgb` and `surfaceVariantArgb` also derives the surface container tones
Material uses behind dialogs, menus and cards, so a themed app does not get its own calendar sitting
on Material's default neutral. `reset()` returns every role to Material's value and the brightness
to the device setting.

The `iosSwiftApp` sample wires this to a toolbar menu; see
[`SampleAppearance.swift`](./sample/iosSwiftApp/iosSwiftApp/SampleAppearance.swift) for a complete
six-palette implementation.

---

# Part 2 - The engine

> Available from **both** products. Compose-free, so it runs anywhere, including on a server.

Every call goes through the shared instance:

```swift
let converter = NepaliDateConverter.shared
```

## Types

```text
SimpleDate        // year, month, dayOfMonth
SimpleTime        // hour, minute, second, nanosecond

CustomCalendar    // a full BS or AD date
                  //   year, month, dayOfMonth, era (1 = AD, 2 = BS)
                  //   firstDayOfMonth, lastDayOfMonth, totalDaysInMonth
                  //   dayOfWeekInMonth, dayOfWeek (1 = Sunday … 7 = Saturday)
                  //   dayOfYear, weekOfMonth, weekOfYear

NepaliMonthCalendar  // year, month, totalDaysInMonth, firstDayOfMonth,
                     // lastDayOfMonth, daysFromStartOfWeekToFirstOfMonth

CustomDateTime    // customCalendar, simpleTime

NepaliEnglishMonthDay  // one Gregorian day paired with its BS calendar
                       //   englishDayOfMonth, nepaliCalendar (nil outside the
                       //   convertible range)

NepaliCalendarEvent  // one thing on one day
                     //   date, name, kind, closesOffices, id, payload
NepaliDayStatus      // what a policy says about a day
                     //   isWeeklyOff, events
                     //   isNonWorking, primaryKind, names, closures (derived)
```

Year bounds come from `NepaliCalendarDefaults.shared`:

```swift
let bs = NepaliCalendarDefaults.shared.NepaliYearRange   // 1970...2100
let ad = NepaliCalendarDefaults.shared.EnglishYearRange  // 1913...2043
(bs.first, bs.last)

// The calendars start mid-year relative to each other, so the year range alone is not a
// sufficient bound. These name the exact first and last convertible English dates.
NepaliCalendarDefaults.shared.minConvertibleEnglishDate  // 1913-04-13
NepaliCalendarDefaults.shared.maxConvertibleEnglishDate  // 2043-12-31
NepaliDateConverter.shared.isEnglishDateConvertible(englishYYYY: 1913, englishMM: 4, englishDD: 12) // false
```

## Enums

| Enum | Cases |
| --- | --- |
| `NepaliDatePickerLang` | `.english`, `.nepali` |
| `NepaliDateFormatStyle` | `.full`, `.long_`, `.medium`, `.shortMdy`, `.shortYmd`, `.compactMdy`, `.compactYmd` |
| `NameFormat` | `.full`, `.medium`, `.short_` |
| `DigitScript` | `.latin`, `.devanagari` |
| `NepaliEventKind` | `.governmentPublic`, `.religious`, `.regional`, `.observance` |
| `CalendarSystem` | `.bikramSambat`, `.gregorian` - the named form of `era` (2 and 1) |

Format styles render as:

| Style | English | Nepali |
| --- | --- | --- |
| `.full` | `Monday, Asar 21, 2024` | `सोमबार, असार २१, २०२४` |
| `.long_` | `Asar 21, 2024` | `असार २१, २०२४` |
| `.medium` | `2024 Asar 21` | `२०२४ असार २१` |
| `.shortMdy` | `06/21/2024` | `०६/२१/२०२४` |
| `.shortYmd` | `2024/06/21` | `२०२४/०६/२१` |
| `.compactMdy` | `06/21/24` | `०६/२१/२४` |
| `.compactYmd` | `24/06/21` | `२४/०६/२१` |

## Today and current time

Dates resolve in Asia/Kathmandu.

```swift
converter.todayNepaliCalendar      // CustomCalendar, BS
converter.todayNepaliSimpleDate    // SimpleDate, BS
converter.todayEnglishCalendar     // CustomCalendar, AD
converter.todayEnglishSimpleDate   // SimpleDate, AD
converter.currentTime              // SimpleTime
```

`todayNepaliDate` and `todayEnglishDate` are exported but marked **unavailable**, so referencing
either is a compile error rather than a warning. Use the `…SimpleDate` or `…Calendar` property above.

## Conversion

```swift
converter.convertEnglishToNepali(englishYYYY: 2024, englishMM: 3, englishDD: 21)
// CustomCalendar - 2080/12/8 BS

converter.convertNepaliToEnglish(nepaliYYYY: 2081, nepaliMM: 1, nepaliDD: 1)
// CustomCalendar - 2024/4/13 AD

converter.getNepaliCalendar(nepaliYYYY: 2082, nepaliMM: 4, nepaliDD: 16)
// CustomCalendar - full breakdown for a BS date
```

## Month and day counts

```swift
converter.getNepaliMonthCalendar(nepaliYear: 2081, nepaliMonth: 5) // NepaliMonthCalendar
converter.getTotalDaysInNepaliMonth(year: 2081, month: 1)          // 31
converter.getTotalDaysInEnglishMonth(year: 2024, month: 2)         // 29
```

## Arithmetic

Month and year overflow are handled for you. Pass a negative adjustment to go backwards.

```swift
converter.getNepaliCalendarAfterAdditionOrSubtraction(
    year: 2081, month: 3, dayOfMonth: 15, daysToAdjust: 10
)
```

## Compare and count between

```swift
converter.compareDates(dateToCompareFrom: a, dateToCompareTo: b)
// < 0 earlier, 0 equal, > 0 later

converter.getNepaliDaysInBetween(
    startDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 1),
    endDate:   SimpleDate(year: 2081, month: 12, dayOfMonth: 30)
) // 364

converter.getEnglishDaysInBetween(startDate: adStart, endDate: adEnd)
```

## Names

```swift
converter.getMonthName(month: 1, format: .full, language: .english)          // "Baisakh"
converter.getMonthName(month: 1, format: .full, language: .nepali)           // "बैशाख"
converter.getEnglishMonthName(month: 3, format: .medium, language: .english) // "Mar"
converter.getWeekdayName(dayOfWeek: 1, format: .full, language: .english)    // "Sunday"
converter.getWeekdayName(dayOfWeek: 7, format: .short_, language: .nepali)   // "श"
```

The tables behind them are exported too, if you want every width at once:

A `NepaliMonthName` carries `short_` and `full`, a `NepaliWeekdayName` carries `short_`, `medium`
and `full`:

```swift
NepaliDatePickerLang.nepali.months            // [NepaliMonthName]   Baisakh … Chaitra
NepaliDatePickerLang.english.weekdays         // [NepaliWeekdayName] Sunday … Saturday
NepaliDatePickerLang.english.englishMonths    // [NepaliMonthName]   January … December

NepaliDatePickerLang.english.months[0].full   // "Baisakh"
NepaliDatePickerLang.english.weekdays[0].medium  // "Sun"
```

## Formatting a date

Through a locale:

```swift
converter.formatNepaliDate(customCalendar: today, locale: locale)
// "Tuesday, Bhadra 30, 2083"  /  "मंगलबार, भदौ ३०, २०८३"

converter.formatEnglishDate(customCalendar: adDate, locale: locale)
```

Or with a Unicode pattern, which resolves everything (including day-of-year `D` and week-of-year
`w`) from the date itself:

```swift
converter.formatNepaliDateByUnicodePattern(
    unicodePattern: "EEEE, MMMM d, yyyy",
    calendar: today,
    language: .english
) // "Tuesday, Bhadra 30, 2083"

converter.formatNepaliDateTimeByUnicodePattern(
    unicodePattern: "yyyy MMMM d, hh:mm a",
    calendar: today, time: converter.currentTime, language: .nepali
)

// Gregorian equivalents
converter.formatEnglishDateByUnicodePattern(unicodePattern: "MMM d, yyyy", calendar: adDate, language: .english)
converter.formatEnglishDateTimeByUnicodePattern(
    unicodePattern: "MMM d, yyyy hh:mm a",
    calendar: adDate, time: converter.currentTime, language: .english
)
```

## Formatting time

```swift
let time = converter.currentTime
converter.getFormattedTimeInEnglish(simpleTime: time, use12HourFormat: true)   // "11:52 AM"
converter.getFormattedTimeInEnglish(simpleTime: time, use12HourFormat: false)  // "11:52"
converter.getFormattedTimeInNepali(simpleTime: time, use12HourFormat: true)
converter.formatTimeByUnicodePattern(unicodePattern: "hh:mm a", time: time, language: .english)
```

## ISO 8601

Persist and restore round-trip safely:

```swift
let iso = converter.formatNepaliDateTimeToIsoFormat(
    nepaliDate: todaySimpleDate, time: converter.currentTime
)
let restored = converter.getNepaliDateTimeFromIsoFormat(isoDateTime: iso)
restored.customCalendar
restored.simpleTime
```

The Gregorian equivalents are `formatEnglishDateNepaliTimeToIsoFormat` and
`getEnglishDateNepaliTimeFromIsoFormat`.

## Date and time text (the wire format)

An ISO timestamp is an absolute instant in UTC. For a plain calendar date or a wall-clock time on
its own, with no zone and no conversion, use the fixed-pattern formatters instead. These produce the
same strings the Kotlin, Android and JavaScript builds persist:

```swift
let dates = NepaliDateFormatter.shared
let times = NepaliTimeFormatter.shared

dates.format(date: SimpleDate(year: 2082, month: 2, dayOfMonth: 14),
             pattern: .yyyyDashMmDashDd, script: .latin)   // "2082-02-14"
dates.parse(input: "2082-02-14", pattern: .yyyyDashMmDashDd)  // SimpleDate? , nil on mismatch

times.format(time: SimpleTime(hour: 9, minute: 30, second: 0, nanosecond: 0))  // "09:30:00"
times.parse(input: "23:59:59.123456789")                      // SimpleTime? , nil on mismatch
```

Both parsers return `nil` rather than throwing, and neither trims whitespace. `parse(input:pattern:)`
holds every pattern to exactly ten characters, so `"2082-2-14"` is `nil`. Devanagari numerals are
accepted by both. `parse` allows day 32, because some Bikram Sambat months run that long; check the
day against the real month with `getTotalDaysInNepaliMonth` when it matters.

### Talking to a Kotlin backend

The optional `nepali-date-picker-serialization` Maven artifact gives Kotlin services `KSerializer`s
for these types. They are not in this framework and they do not need to be: they read and write
exactly the strings above, so `Codable` on this side is enough.

| Type | On the wire | Produce it here with |
| --- | --- | --- |
| `SimpleDate` | `"2082-02-14"` | `NepaliDateFormatter.shared.format(date:pattern:.yyyyDashMmDashDd, script:.latin)` |
| `SimpleDate` (struct form) | `{"year":2082,"month":2,"dayOfMonth":14}` | the `Codable` struct below |
| `SimpleTime` | `"09:30:00"`, `"23:59:59.123456789"` | `NepaliTimeFormatter.shared.format(time:)` |
| `CustomCalendar` | 12-field object: `year`, `month`, `dayOfMonth`, `era`, `firstDayOfMonth`, `lastDayOfMonth`, `totalDaysInMonth`, `dayOfWeekInMonth`, `dayOfWeek`, `dayOfYear`, `weekOfMonth`, `weekOfYear` | the `Codable` struct below |
| `CalendarSystem` | `1` for AD, `2` for BS | the `era` field |
| `NepaliCalendarEvent` | `{"date":"2082-01-01","name":"…","kind":"GovernmentPublic"}`, plus `closesOffices`, `id` and `payload` when set | see the note below |
| `NepaliDayStatus` | `{"isWeeklyOff":false,"events":[…]}` | - |

Kotlin classes cannot conform to `Codable`, so declare your own structs. They are small, and the
field names and order below are what the Kotlin serializers write:

```swift
struct WireDate: Codable {
    let year: Int32
    let month: Int32
    let dayOfMonth: Int32

    init(_ date: SimpleDate) {
        year = date.year; month = date.month; dayOfMonth = date.dayOfMonth
    }

    var simpleDate: SimpleDate {
        SimpleDate(year: year, month: month, dayOfMonth: dayOfMonth)
    }
}

struct WireCalendar: Codable {
    let year: Int32
    let month: Int32
    let dayOfMonth: Int32
    let era: Int32
    let firstDayOfMonth: Int32
    let lastDayOfMonth: Int32
    let totalDaysInMonth: Int32
    // Optional on the way into Kotlin, where each defaults to -1.
    var dayOfWeekInMonth: Int32 = -1
    var dayOfWeek: Int32 = -1
    var dayOfYear: Int32 = -1
    var weekOfMonth: Int32 = -1
    var weekOfYear: Int32 = -1
}
```

For a date that is only a date, send the `SimpleDate` string. `CustomCalendar` is a fully resolved
calendar record, and most payloads do not need one.

**The one field that is spelled differently.** Kotlin writes `kind` as the enum's own name, so
`GovernmentPublic`, `Religious`, `Regional`, `Observance`, capitalised. Swift sees the same enum as
`.governmentPublic`, `.religious`, `.regional`, `.observance`. Kotlin rejects an unknown name rather
than guessing, so capitalise the first letter on the way out and lowercase it on the way back in.

`NepaliCalendarEvent` covers exactly one day. A span is written out as one entry per day carrying
the same `name`, `kind` and `id`, so expand a span before sending it and collapse the entries back
by `id` on the way in.

## Digits

```swift
converter.localizeDigits("2081", script: .devanagari)   // "२०८१"
converter.localizeDigits("1234567890", locale: locale)  // script taken from the locale
converter.toLatinDigits("२०८१")                          // "2081"

// Localize the digits of an already formatted string, following a locale.
converter.localizeNumber("2081", locale: .nepali)       // "२०८१"

// Swap the separator in a formatted date.
converter.replaceDelimiter(dateString: "2081/01/15", newDelimiter: "-", oldDelimiter: "/")

```

`latinDigitOrNull` maps a single Devanagari digit back to Latin, or `nil` if it is not a digit. It
extends Kotlin's `Char`, which the bridge lowers to `unichar` (a UTF-16 code unit), so it takes and
returns numbers rather than strings:

```swift
let devanagariSeven = Array("७".utf16)[0]
if let latin = DigitScriptKt.latinDigitOrNull(devanagariSeven) as? unichar {
    String(utf16CodeUnits: [latin], count: 1)            // "7"
}
```

For whole strings, reach for `toLatinDigits(_:)` above instead.

## Events, holidays and working days

**No event data ships with the library, by design.** Nepal's holiday list varies by employer,
province and year, and every school keeps its own calendar besides, so you supply a
`NepaliEventProvider`. Use `NoOpEventProvider.shared` when only the weekly rule matters.

A `NepaliCalendarEvent` is one thing on one day: a public holiday, a festival, a programme, a
meeting. What separates a holiday from a meeting is `closesOffices`, not the name.

```swift
let dashain = NepaliCalendarEvent(
    date: SimpleDate(year: 2082, month: 6, dayOfMonth: 25),
    name: "Vijaya Dashami",
    kind: .religious,
    closesOffices: true,   // defaults to what the kind usually means
    id: "evt-42",          // handed back untouched, for your own record
    payload: nil           // any opaque string; the library never parses it
)
```

`NepaliEventKind` is `.governmentPublic`, `.religious`, `.regional`, `.observance`. The kind decides
the colour and the default for `closesOffices`; the event itself has the final say, which is what
lets a regional holiday close one district and not the next, and a school programme close nothing.

An event covers one day, so something that runs longer is a list of entries rather than a range:

```swift
let festival = NepaliCalendarEvent(
    date: SimpleDate(year: 2082, month: 6, dayOfMonth: 17),
    name: "Dashain",
    kind: .religious,
    closesOffices: true,
    id: "dashain-2082",   // set it, so the days can be recognized as one thing again
    payload: nil
)

festival.spanningDays(days: 10)   // ten entries, Asoj 17 through 26
festival.spanningThrough(end: SimpleDate(year: 2082, month: 6, dayOfMonth: 26))
```

Every entry keeps the name, kind, `closesOffices`, `id` and `payload`, and a span running out of
Chaitra into Baisakh yields entries in both years, so each is reported by the year `events(year:)`
is asked for.

### A policy is one institution's calendar

`NepaliCalendarPolicy` pairs the weekdays an institution never opens with the events it keeps.
`NepaliWeekend.shared.Default` is **Saturday only**, matching what a Nepali office counts, rather
than the two-day weekend most libraries assume.

```swift
let office = NepaliCalendarPolicy(weeklyOffDays: NepaliWeekend.shared.Default, provider: provider)

// Kotlin's Set<Int> arrives as Set<KotlinInt>, so weekday numbers are boxed. `boxedSet` is the
// helper from Step 1.
let school = NepaliCalendarPolicy(
    weeklyOffDays: [Weekday.saturday, Weekday.sunday].boxedSet,
    provider: provider
)
```

Weekday numbers are 1-based-Sunday, so `[6, 7]` is a Friday and Saturday weekend. A number outside
`1...7` throws, since a set written to JavaScript's 0-based convention would close nothing at all.

Ask it what a day is, and render the answer yourself:

```swift
let status = office.statusOf(date: SimpleDate(year: 2082, month: 6, dayOfMonth: 3))
status.isWeeklyOff    // the week closes it
status.isNonWorking   // closed for either reason, counted once
status.primaryKind    // nil when the day is only a weekly off day
status.names          // ["Constitution Day"], strongest kind first
status.closures       // only the events that actually shut the door

office.eventsOn(date: someDate)        // one day
office.eventsIn(year: 2082, month: 6)  // a whole month, for a patro-style list
office.monthStatus(year: 2082, month: 6) // one entry per day, index 0 is day 1
```

`monthStatus` resolves the month's first weekday once and walks forward, so a grid costs one
conversion instead of one per cell. Prefer it when laying out a month.

### Arithmetic follows the same policy

```swift
converter.workingDaysBetween(start: from, end: to, policy: school)  // Int32, end exclusive
converter.nextWorkingDay(from: from, policy: school)                // SimpleDate
converter.addWorkingDays(from: from, days: 5, policy: school)       // Excel WORKDAY semantics
```

The `(provider:weekend:)` overloads are still there when you would rather pass the two separately.
An event that does not close is not counted: a week of school programmes is still five working days.

### Marking the days a picker draws

Compose colours cannot cross the Objective-C bridge in any usable form, so Swift describes what to
mark with `NepaliEventOptions` and the library builds the decorator on the Kotlin side. Colours are
`0xAARRGGBB` integers, and `0` keeps whatever the theme resolved.

```swift
let marks = NepaliEventOptions()
marks.weeklyOffDays = [Weekday.saturday, Weekday.sunday].boxed   // a school week
marks.events = [
    NepaliEventInfo(
        year: 2082, month: 6, dayOfMonth: 3,
        name: "Constitution Day", kind: .governmentPublic,
        closesOffices: true,
        colorArgb: 0,        // take the palette slot its kind maps to
        indicate: false      // colour the day without spending a dot
    ),
    NepaliEventInfo(
        year: 2082, month: 6, dayOfMonth: 5,
        name: "Standup", kind: .observance,
        closesOffices: false,
        colorArgb: Int32(bitPattern: 0xFF42A5F5),
        indicate: true       // an app's own event gets a dot
    ),
]

AutoSized(measurementHeight: 560) { report in
    NepaliDatePickerView(onHeightChange: report, initialSelectedDate: nil, events: marks) {
        selected = $0
    }
}
```

**A day takes one colour and the dots mean events.** A weekly off day repeats fifty-two times a
year, so it is coloured and never dotted. A named closure outranks the week, because "Dashain" says
more than "Saturday"; an event that leaves the door open does not, so a Saturday carrying only a
programme still reads as a Saturday.

| Property | Type | Default | What it does |
| --- | --- | --- | --- |
| `weeklyOffDays` | `[KotlinInt]` | `[7]` | Weekdays the institution never opens, 1 for Sunday through 7 for Saturday. Out-of-range numbers are dropped rather than throwing. Use the `boxed` helper from [Step 1](#step-1---paste-the-support-file). |
| `events` | `[NepaliEventInfo]` | `[]` | What to mark. |
| `markWeeklyOff` | `Bool` | `true` | Colour the weekly off days. |
| `markEvents` | `Bool` | `true` | Colour the days that carry an event. |
| `tintContainer` | `Bool` | `false` | Also give a marked day a tinted disc. |
| `indicateWeeklyOff` | `Bool` | `false` | Dot the weekly off days too. |
| `describeEvents` | `Bool` | `true` | Announce the names after the date, so the marking is never colour-only. |
| `weeklyOffColorArgb` | `Int32` | `0` | Overrides the weekly slot. `0` keeps the theme's. |
| `publicHolidayColorArgb` | `Int32` | `0` | Overrides the `.governmentPublic` slot. |
| `religiousColorArgb` | `Int32` | `0` | Overrides the `.religious` slot. |
| `regionalColorArgb` | `Int32` | `0` | Overrides the `.regional` slot. |
| `observanceColorArgb` | `Int32` | `0` | Overrides the `.observance` slot. |
| `markedContainerColorArgb` | `Int32` | `0` | The disc `tintContainer` draws. |

Every picker that draws a month grid takes `events`: the calendar, the dual-date calendar, the
range picker, the docked picker, both dialogs, and both field pickers. The wheel does not, because
it has no day cells.

### Marking never blocks

Colouring a day and refusing it are separate decisions, so a school can mark Saturday and still let
a teacher record attendance on it. Opt in when you want both:

```swift
AutoSized(measurementHeight: 560) { report in
    NepaliDatePickerView(
        onHeightChange: report,
        initialSelectedDate: nil,
        selectableDates: school.asSelectableDates(),   // now they are greyed out too
        events: marks                                  // and still marked
    ) { selected = $0 }
}
```

`asSelectableDates()` blocks the weekly off days and the events that close; an event that leaves the
institution open leaves its day selectable. The older wrappers still compose by hand:

```swift
let workingDaysOnly = EventHelpersKt.excludingClosures(
    EventHelpersKt.excludingWeekends(
        NepaliDatePickerDefaults.shared.AllDates,
        weekend: NepaliWeekend.shared.Default
    ),
    provider: provider
)
```

Year-level rejection still defers to the wrapped policy, because event data is per-date, not
per-year.

### Writing a provider in Swift

A Kotlin interface method with a default implementation is still `@required` in the generated
Objective-C protocol, so a Swift conformance has to write out **both** members, even the one Kotlin
would have supplied:

```swift
final class OfficeCalendar: NepaliEventProvider {
    private let byYear: [Int32: Set<NepaliCalendarEvent>]

    init(_ events: [NepaliCalendarEvent]) {
        byYear = Dictionary(grouping: events, by: { $0.date.year }).mapValues(Set.init)
    }

    func events(year: Int32) -> Set<NepaliCalendarEvent> { byYear[year] ?? [] }

    /// The rule Kotlin's default states: only an entry that closes the institution shuts the day.
    func closesOn(date: SimpleDate) -> Bool {
        events(year: date.year).contains { $0.date == date && $0.closesOffices }
    }
}
```

Answer `events(year:)` when a calendar has to *draw* the days. When the working-day arithmetic is
all you need, `closesOn(date:)` can answer directly and `events(year:)` can return an empty set.

Two providers combine, which is how a national list and a school's own list are put together:

```swift
let merged = EventHelpersKt.plus(nationalHolidays, other: schoolCalendar)
let closuresOnly = EventHelpersKt.filtered(merged) { $0.closesOffices }
```

### Migrating from 3.1.0

**The renamed types have to be renamed in Swift too.** Kotlin keeps `HolidayEntry`, `HolidayKind`,
`NepaliHolidayProvider` and `NepaliHolidayPolicy` as deprecated typealiases, but a typealias is a
Kotlin-only construct: it produces no Objective-C name, so none of the four reaches Swift. Rename
them at the call site:

| 3.1.0, Swift | 3.3.0, Swift |
| --- | --- |
| `HolidayEntry` | `NepaliCalendarEvent` |
| `HolidayKind` | `NepaliEventKind` |
| `NepaliHolidayProvider` | `NepaliEventProvider` |
| `NepaliHolidayPolicy` | `NepaliCalendarPolicy` |

The two declarations that are not typealiases do still resolve, deprecated, on
`DeprecatedHolidayNamesKt`: `NoOpHolidayProvider` and `excludingHolidays(_:provider:)`.

An **implementation** of the old provider has to be renamed whichever language you write it in,
because renaming a type never renames its members: `holidays(year:)` becomes `events(year:)` and
`isHoliday(date:)` becomes `closesOn(date:)`.

## NepaliCalendarModel

`NepaliDateConverter` is a facade over `NepaliCalendarModel`, which is public and locale-bound.
Reach for it when you want a fixed locale without passing one to every call, or for the few
operations the facade does not re-export:

```swift
let model = NepaliCalendarModel(locale: locale)

model.plusNepaliMonths(fromNepaliCalendar: monthCalendar, addedMonthsCount: 3)
model.minusNepaliMonths(fromNepaliCalendar: monthCalendar, subtractedMonthsCount: 3)
model.getNepaliMonth(nepaliYear: 2081, nepaliMonth: 5)
model.getNepaliCalendar(simpleNepaliDate: date)
model.parse(dateString: "2081/01/15")          // SimpleDate?, nil when unparseable
model.localizeNumber(stringToLocalize: "2081", locale: .nepali)
model.nepaliDaysInBetween(startDate: from, endDate: to)
```

Everything else on it mirrors the facade: the conversions, the formatters, the ISO helpers and the
`today*` properties.

## Calendar tables

The lookup tables that bound the supported range are exported, which is enough to answer questions
the API does not cover directly:

```swift
NepaliYearMonthMapKt.daysInMonthMap   // [Int: KotlinIntArray]  BS year -> days per month
NepaliYearMonthMapKt.nepaliDateMap    // [Int: ReferenceDate]
NepaliYearMonthMapKt.englishDateMap   // [Int: ReferenceDate]

// A ReferenceDate is the anchor pair a year's arithmetic starts from. Both ends are CustomCalendar.
let anchor = NepaliYearMonthMapKt.nepaliDateMap[2081]
anchor?.englishDate
anchor?.nepaliDate
```

Extending the supported range means extending `daysInMonthMap` in the library, not in your app.

## Troubleshooting

| Symptom | Cause and fix |
| --- | --- |
| `found architecture 'arm64', required architecture 'x86_64'` | Building for the Intel simulator. Set `EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64`. |
| `While building for macOS, no library for this platform was found in ... nepali_date_picker.xcframework` | A macOS target is linking the **UI** product, which is iOS-only. Switch the run destination to an iOS Simulator or device, or depend on `nepali-date-picker-core` instead. |
| The same error naming `nepali_date_picker_core.xcframework` | An Intel Mac, or a macOS deployment target below 12. The macOS slice is `arm64`, minimum macOS 12. |
| `cannot load module 'Your_App' as 'nepali_date_picker'` | Your app's module name matches the framework's ignoring case. Change `PRODUCT_MODULE_NAME`. |
| Crash on launch, `SIGABRT` in `PlistSanityCheck` | `CADisableMinimumFrameDurationOnPhone` is missing from `Info.plist`. |
| `No such module 'nepali_date_picker'` in the editor only | SourceKit indexes before the framework is built. Build once; it resolves. |
| Duplicate symbols or two Kotlin runtimes | Both products are linked. Depend on exactly one. |
| A picker shows blank for a moment on first display | The Kotlin runtime and Metal shaders initialise on first render. Subsequent presentations are immediate. |
| Weekday headers wrap to two lines | `weekDayName` is not `.short_`. |
| Calendar's last week is cut off | The scene was measured in a frame shorter than the grid. Raise `measurementHeight`, see [Sizing](#sizing). |
| A picker renders at zero height | `measurementHeight` is zero, so the content had no room to measure. |
| Empty band under a picker | The outer frame is pinned to `measurementHeight` instead of the reported height. Keep the two frames separate, as `AutoSized` does. |
| A picker stays short after switching to typed input | The scene shrank with the layout, so it can never report more than its current height. Measure in the fixed `measurementHeight`, size the layout from the report. |
| A field stops short of the container, leaving the host surface beside it | Material's 280pt minimum width. The factories pass `fillMaxWidth`, so this only shows if you host the composable yourself. |
| Gregorian sub-labels clipped in the last column | The grid is too narrow. Remove a layer of horizontal padding. |
| Headline truncated, or a range headline wrapping mid-word | Same width problem, or a `dateFormat` that prints the weekday. Use `.long_`, or `.shortYmd` for ranges. |
| Docked popup appears cut off | It is clipped to its host. Give that frame more height. |
| Picker ignores a changed locale | `makeUIViewController` runs once per view identity. Add `.id(...)` keyed on the change. |

## Sample app

`sample/iosSwiftApp` in the main repository is a SwiftUI app that consumes the framework exactly the
way this document describes: one screen per picker, every engine utility, the representables, and the
`AutoSized` container.

```bash
# The framework has to exist before Xcode plans the build.
./gradlew :nepali-date-picker:ui:assembleNepali-date-pickerReleaseXCFramework
open sample/iosSwiftApp/iosSwiftApp.xcodeproj
```

After that first assemble, an `AssembleXCFramework` target dependency keeps the framework in step
with the Kotlin sources, so editing the library and pressing Run in Xcode is enough.

`sample/iosApp` is a different thing: the iOS host for the Compose Multiplatform showcase shared with
Android, desktop and web. It embeds the Compose sample rather than consuming the Swift package.

A Flutter app reaches the same framework through the
[`nepali_date_picker_kmp`](https://pub.dev/packages/nepali_date_picker_kmp) plugin, which ships this
same XCFramework on iOS. The showcase is [`sample/flutterApp`](./sample/flutterApp) and the compact
demo is [`flutter/nepali_date_picker_kmp/example`](./flutter/nepali_date_picker_kmp/example); both
need the framework staged first:

```bash
./gradlew publishToMavenLocal
flutter/nepali_date_picker_kmp/tool/stage_ios_framework.sh
cd sample/flutterApp && flutter run
```

The Flutter guide is [README-flutter.md](./README-flutter.md).

## Support

Consider [starring the repository](https://github.com/shivathapaa/Nepali-Date-Picker) and sharing it
if this saved you time. Issues and pull requests are welcome on the
[main repository](https://github.com/shivathapaa/Nepali-Date-Picker/issues).

## License

[Mozilla Public License 2.0 (MPL 2.0)](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE)
