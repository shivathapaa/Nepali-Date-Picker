# Nepali-Date-Picker-SPM

<p align="center">
  <img src="https://raw.githubusercontent.com/shivathapaa/Nepali-Date-Picker/main/.github/assets/nepaliDatePickerBanner.png" alt="" width="100%">
</p>

The Swift Package Manager distribution of the
[Nepali Date Picker](https://github.com/shivathapaa/Nepali-Date-Picker): a **Bikram Sambat (Nepali)
date picker and month calendar** for iOS, plus a headless **BS to AD conversion, comparison and
formatting engine** for iOS and macOS.

<p align="center">
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker-SPM/releases">
    <img alt="version" src="https://img.shields.io/github/v/release/shivathapaa/Nepali-Date-Picker-SPM?label=spm%20release" /></a>&nbsp;
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE">
    <img alt="license" src="https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg"/></a>&nbsp;
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker/releases/tag/{{SOURCE_TAG}}">
    <img alt="source" src="https://img.shields.io/badge/built%20from-{{VERSION_BADGE}}-blue" /></a>
</p>

> ## 📖 [Full documentation →](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md)
>
> Installation, the required Xcode settings, every picker, the month calendar and the complete engine
> API live in the main repository. This page covers what you need to add the package and what this
> release contains.

> **This repository is generated.** It carries no source of its own: `Package.swift`, this page and
> the release assets are all written by the
> [main repository](https://github.com/shivathapaa/Nepali-Date-Picker) on every release. Issues and
> pull requests belong [there](https://github.com/shivathapaa/Nepali-Date-Picker/issues).

## This release

| | |
| --- | --- |
| Version | **{{VERSION}}** |
| Published | {{RELEASED_ON}} |
| Built from | [`{{SOURCE_TAG}}`](https://github.com/shivathapaa/Nepali-Date-Picker/releases/tag/{{SOURCE_TAG}}) in the main repository |
| Swift tools | {{SWIFT_TOOLS}}+ |
| Platforms | iOS {{IOS_MIN}}+, macOS {{MACOS_MIN}}+ (engine only), arm64 only |
| Calendar range | BS {{BS_FIRST}}-{{BS_LAST}}, AD {{AD_FIRST}}-{{AD_LAST}} |

Both products are prebuilt XCFrameworks attached to
[this release]({{RELEASE_URL}}). Nothing is compiled on your machine.

## Which product to pick

**Depend on exactly one.**

| Product | Contains | Platforms | Swift import | Download |
| --- | --- | --- | --- | --- |
| `nepali-date-picker` | The pickers and the month calendar, **plus** the full conversion engine | iOS {{IOS_MIN}}+ | `nepali_date_picker` | {{UI_SIZE}} |
| `nepali-date-picker-core` | The conversion engine only, no UI | iOS {{IOS_MIN}}+, macOS {{MACOS_MIN}}+ | `nepali_date_picker_core` | {{CORE_SIZE}} |

> **Never add both.** Each XCFramework is a self-contained static binary and the UI framework already
> embeds the core module, so linking both duplicates the Kotlin runtime and every core symbol.

> **macOS gets the engine, not the UI.** `nepali-date-picker-core` carries a `macos-arm64` slice, so
> a Mac app has the full conversion, comparison and formatting API. The pickers and the calendar are
> iOS-only, so a macOS target that links `nepali-date-picker` fails to build with
> `no library for this platform was found`; depend on `nepali-date-picker-core` there instead.

## Installation

In Xcode: **File → Add Package Dependencies…**, paste the URL, choose a version, then pick one of the
two library products.

```
https://github.com/shivathapaa/Nepali-Date-Picker-SPM.git
```

Or in your own `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/shivathapaa/Nepali-Date-Picker-SPM.git", from: "{{VERSION}}")
],
targets: [
    .target(name: "App", dependencies: [
        .product(name: "nepali-date-picker", package: "Nepali-Date-Picker-SPM")
        // macOS target: the engine product is the one that links
        // .product(name: "nepali-date-picker-core", package: "Nepali-Date-Picker-SPM")
    ])
]
```

Pin the exact release instead when you want no surprises at all:

```swift
.package(url: "https://github.com/shivathapaa/Nepali-Date-Picker-SPM.git", exact: "{{VERSION}}")
```

```swift
import nepali_date_picker      // pickers, calendar and engine
// or
import nepali_date_picker_core // engine only
```

## Before you build

Three project settings are load-bearing. Without them the app fails to link, fails to compile, or
crashes on launch. The
[full documentation](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md#required-xcode-configuration)
explains each one and how to supply the plist key.

| Setting | Value |
| --- | --- |
| `EXCLUDED_ARCHS[sdk=iphonesimulator*]` | `x86_64` |
| `PRODUCT_MODULE_NAME` | anything not equal to `nepali_date_picker` ignoring case |
| `CADisableMinimumFrameDurationOnPhone` in `Info.plist` | `YES` (UI product only) |

## Verifying what you link

SwiftPM checks these checksums on resolve and refuses a mismatch, so reach for this table only for
an audit or a mirror.

| Asset | SHA256 |
| --- | --- |
| [`{{UI_ZIP_NAME}}`]({{DOWNLOAD_URL}}/{{UI_ZIP_NAME}}) | `{{UI_CHECKSUM}}` |
| [`{{CORE_ZIP_NAME}}`]({{DOWNLOAD_URL}}/{{CORE_ZIP_NAME}}) | `{{CORE_CHECKSUM}}` |

```bash
curl -LO {{DOWNLOAD_URL}}/{{CORE_ZIP_NAME}}
swift package compute-checksum {{CORE_ZIP_NAME}}
```

## What you get

- **Pickers.** The Material3 calendar picker, a docked field, a wheel, range pickers, typed text
  fields, and modal or full-screen dialogs. Each one is a `UIViewController` factory, so SwiftUI
  embeds it through a `UIViewControllerRepresentable`.
- **A month calendar.** `NepaliCalendarViewController` is the browsable patro rather than a date
  prompt: both calendars' numbers in every cell, the days an institution is closed for marked from
  your own event list, and optional blocks writing the picked day and the month's events out
  underneath.
- **The engine.** Conversion both ways, month grids, arithmetic, comparison, ISO 8601, locale and
  Unicode-pattern formatting, Devanagari digits, events, calendar policies and working-day maths.
  No UI, no Compose, usable from a Mac app or a command line tool.

```swift
import nepali_date_picker

let converter = NepaliDateConverter.shared
let locale = NepaliDateLocale(
    language: .english, dateFormat: .long_,
    weekDayName: .short_, monthName: .full, digitScript: nil
)

converter.todayNepaliCalendar                                             // today in Bikram Sambat
converter.convertEnglishToNepali(englishYYYY: 2024, englishMM: 3, englishDD: 21)
converter.formatNepaliDate(customCalendar: converter.todayNepaliCalendar, locale: locale)
```

A complete SwiftUI sample, every picker and the calendar included, lives at
[`sample/iosSwiftApp`](https://github.com/shivathapaa/Nepali-Date-Picker/tree/main/sample/iosSwiftApp),
and a Flutter app reaches this same package through the
[`nepali_date_picker_kmp`](https://pub.dev/packages/nepali_date_picker_kmp) plugin, demonstrated in
[`sample/flutterApp`](https://github.com/shivathapaa/Nepali-Date-Picker/tree/main/sample/flutterApp).
See
[The pickers](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md#part-1---the-pickers)
for the wrapper, the sizing rules and every variant, and
[The browsable calendar](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md#the-browsable-calendar)
for the calendar factories.

**Either calendar.** Every calendar surface can display the Gregorian calendar instead, with a
`B.S.` / `A.D.` switch the user can flip, and can fill the grid's empty cells with the neighbouring
months' days. The date handed back to Swift is always Bikram Sambat, so switching keeps the same day
selected.

**Events and holidays.** Every surface that draws a month grid takes a `NepaliEventOptions`: the
weekdays an institution never opens plus the days to mark, coloured by kind or by your own
`0xAARRGGBB` value. `NepaliCalendarPolicy` answers the queries behind it, `statusOf(date:)`,
`monthStatus(year:month:)` and `eventsIn(year:month:)`, and the converter's
`workingDaysBetween(start:end:policy:)`, `nextWorkingDay(from:policy:)` and
`addWorkingDays(from:days:policy:)` count by that same policy, with Excel `WORKDAY` semantics.
**No event data ships with the library**, so you supply a `NepaliEventProvider`. See
[Events, holidays and working days](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md#events-holidays-and-working-days).

**Theming.** `NepaliPickerAppearance` is a shared appearance proxy in the UIKit sense: set a colour
role or the light/dark choice once and every picker, field and dialog repaints, including the ones
already on screen. See
[Colours and dark mode](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md#colours-and-dark-mode).

## Links

- **[Full documentation](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-spm.md)**
- [Main repository](https://github.com/shivathapaa/Nepali-Date-Picker)
- [Release notes for {{VERSION}}](https://github.com/shivathapaa/Nepali-Date-Picker/releases/tag/{{SOURCE_TAG}})
- [API reference](https://shivathapaa.github.io/Nepali-Date-Picker/api/)
- [Sample Swift iOS project](https://github.com/shivathapaa/Nepali-Date-Picker/tree/main/sample/iosSwiftApp)
- [Sample Flutter app](https://github.com/shivathapaa/Nepali-Date-Picker/tree/main/sample/flutterApp),
  which links this package on iOS through the plugin
- Other platforms, all sharing the same calendar tables, so results match:
    - **Kotlin / Android / KMP** - [main README](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README.md),
      on [Maven Central](https://central.sonatype.com/namespace/io.github.shivathapaa)
    - **Flutter** - [README-flutter.md](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-flutter.md),
      [`nepali_date_picker_kmp`](https://pub.dev/packages/nepali_date_picker_kmp) on pub.dev, which
      ships this same XCFramework on iOS
    - **JavaScript / web** - [README-js.md](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-js.md),
      [`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component)
      and [`@nepali-date-picker/core`](https://www.npmjs.com/package/@nepali-date-picker/core) on npm
    - **Python / backend** - [`nepali_calendar_utils`](https://github.com/shivathapaa/nepali_calendar_utils)
      on PyPI

## License

[Mozilla Public License 2.0 (MPL 2.0)](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE)
