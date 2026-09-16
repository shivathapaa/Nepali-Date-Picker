# Changelog

All notable changes to **Nepali-Date-Picker (KMP)** are documented here.

The project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html). Pre-3.0 release history lives in the [GitHub Releases](https://github.com/shivathapaa/Nepali-Date-Picker/releases) page.

## 3.1.2 - macOS slice for the Swift engine framework

Patch release. No Kotlin, Compose, Swift or JavaScript API changed, so every 3.1.1 project upgrades
with a version bump.

### Swift and macOS

* `nepali-date-picker-core.xcframework` now ships a `macos-arm64` slice alongside the two iOS ones,
  so a macOS app can use the BS↔AD conversion, comparison and formatting engine from Swift. The
  generated `Package.swift` declares `.macOS(.v12)`, matching the deployment target Kotlin/Native
  builds `macosArm64` against.
* The pickers stay iOS-only. They are hosted in a `UIViewController`, and Compose Multiplatform
  publishes no embeddable AppKit host on macOS, so there is no macOS equivalent of the factories
  Swift callers use. A macOS target that links the `nepali-date-picker` product fails with
  `no library for this platform was found`; `README-spm.md` documents this under Troubleshooting.
* The macOS slice is Apple silicon only. Intel Macs are not covered.
* SPM release notes link the source tag they were built from.

### JavaScript tooling

* `verify:pack` reads npm 12's object-shaped `npm pack --json` output, which had broken the release
  gate on the newer CLI.
* The npm publish workflow is pinned to npm 12.
* Dev dependencies upgraded to clear advisories: vite, vitest and esbuild, plus TypeScript 7 and
  jsdom 30. No shipped runtime dependency changed.

## 3.1.1 - Swift package, npm packages, published API reference

Distribution release. The library now ships to Swift Package Manager and npm alongside Maven Central,
all three generated from the same calendar core, so a Bikram Sambat date converts identically in
Kotlin, Swift, JavaScript and Python. Nothing in the existing Kotlin and Compose public API changed;
every 3.1.0 project can upgrade without touching code.

### iOS and Swift

* The pickers are exposed to Swift through `UIViewController` factories, so a SwiftUI or UIKit app can
  embed them without any Compose code: `NepaliDatePickerViewController`, `NepaliDatePickerDockedViewController`,
  `NepaliWheelDatePickerViewController`, the range and dialog hosts, and the text-field hosts. Each
  factory takes a plain options object (`NepaliCalendarOptions`, `NepaliDockedOptions`, `NepaliWheelOptions`,
  `NepaliFieldOptions`, `NepaliDialogOptions`) instead of a long parameter list, since Kotlin default
  arguments do not survive the Objective-C bridge.
* Compose cannot report an intrinsic size to SwiftUI, so every factory takes an `onHeightChange`
  callback the host can drive its frame from.
* `:ui` assembles `nepali_date_picker.xcframework` (pickers plus the engine), and `:core` assembles a
  Compose-free `nepali_date_picker_core.xcframework` (~4 MB) for Swift consumers that only need
  conversion. The two are alternatives, never additive.
* Published as a Swift package at [Nepali-Date-Picker-SPM](https://github.com/shivathapaa/Nepali-Date-Picker-SPM)
  with one product per framework. Full Swift documentation lives in [`README-spm.md`](./README-spm.md).
* The export surface is now snapshot-tested. `checkIosApi` compares the linked framework against
  `nepali-date-picker/ui/api/ios.api`, `dumpIosApi` rewrites that snapshot, and `checkBridgeCoverage`
  fails if a Compose picker has no iOS factory.

### JavaScript and the web

* Two npm packages, generated from `:core` so the conversion tables match exactly:
  [`@nepali-date-picker/core`](https://www.npmjs.com/package/@nepali-date-picker/core), a
  zero-dependency BS↔AD conversion and formatting engine with TypeScript definitions, and
  [`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component),
  a framework-agnostic custom-element suite (`<nepali-date-picker>`, dialog, docked, range, field,
  range field, wheel) that works in React, Vue, Angular, Svelte and plain HTML.
* `:core` gained a `@JsExport` wrapper API and now emits `.d.ts`, so the npm engine is typed.
* Full web documentation lives in [`README-js.md`](./README-js.md), with a live showcase at
  [`/demo/`](https://shivathapaa.github.io/Nepali-Date-Picker/demo/) built from `sample/jsApp`.
* A self-contained build ships for CDN and `<script>` use: one file with Lit and the conversion
  engine inlined, 232 kB minified and 64 kB gzipped for all seven elements, reachable as
  `@nepali-date-picker/web-component/bundle` or straight from jsDelivr / unpkg. It is an ES module
  rather than UMD, since every browser that implements custom elements also supports module scripts.
* The elements ship a `custom-elements.json` manifest, so editors with custom-element support
  complete attributes, events and CSS custom properties per tag, and every element now documents the
  `--ndp-*` properties it actually reads.
* TSX typings for the tags are generated from that manifest and opt in with a single import of
  `@nepali-date-picker/web-component/react`. No runtime, no dependency, no peer dependency. They
  encode what React genuinely does with a custom element: `onChange` arrives as a synthetic event, so
  the payload is at `event.nativeEvent.detail`, and `invalid` and `cancel` never reach an
  `on`-prefixed prop at all, so no props are generated for them.
* `NepaliDateFieldInvalidDetail` is exported instead of being an anonymous inline type, alongside
  `NepaliDatePickerChangeEvent`, `NepaliDateRangeChangeEvent` and `NepaliDateFieldInvalidEvent` for
  typing listeners.
* Fixed: `<nepali-date-field>` and `<nepali-date-range-field>` declared their own `--ndp-*` defaults,
  which beat an inherited value, so page-level theming such as `:root { --ndp-accent: ... }` never
  reached them. All seven elements now take their defaults from the shared token block, and theming
  behaves the same everywhere.
* `verify:pack` packs both packages, installs them into a throwaway consumer and round-trips a
  conversion before a release can publish, so a broken `exports` map or an incomplete `dist` cannot
  ship. It also resolves the CDN bundle through the `exports` map, rejects any bare import left in
  it, and loads it in a DOM to confirm all seven tags register. npm publishing uses OIDC trusted
  publishing instead of a long-lived token.

### Documentation

* A multi-module Dokka API reference is published to
  [`/api/`](https://shivathapaa.github.io/Nepali-Date-Picker/api/).
* Every sample app is documented in [`sample/`](./sample), including a SwiftUI sample
  (`sample/iosSwiftApp`) that consumes the XCFramework the way a real Swift project does.

### Fixes

* `<nepali-date-picker>` rendered its calendar grid with the columns misaligned, ignored theming
  applied to an ancestor element, and laid the full-screen variant out incorrectly.
* The Compose sample sized a picker to its content in only one direction.
* `kotlin-js-store/yarn.lock` is committed and kept current, so JS and Wasm CI builds resolve pinned
  dependencies.

## 3.1.0 - New picker experiences, serialization, digit script, holidays, accessibility, and performance

Additive release. No breaking changes to existing public symbols; every prior API works unchanged and all new APIs are opt-in.

### New `:ui` composables

* `NepaliWheelDatePicker()` (experimental) - a scroll/wheel picker with three snapping columns (Year, Month, Day). It reads the day-count table directly, so it has no month pager and no per-cell BS/AD conversion, and it always shows the correct 29 to 32 days for the chosen month. Best for birth dates and dates far from today.
* `NepaliDatePickerDocked()` (experimental) - the Material3 docked pattern: a read-only outlined field with a dropdown calendar anchored below it. The dropdown closes once a date is picked. The right default for forms and desktop/web.
* `NepaliDatePickerFullScreenDialog()` (experimental) - a full-screen host with a top bar (dismiss, title, confirm) that fits a range selection on a phone. Reuses any picker as its content.
* `NepaliDateRangeTextField()` / `NepaliDateRangeField()` (experimental) - two stacked outlined fields for a Bikram Sambat date range with start-before-end validation; `NepaliDateRangeField` adds a calendar icon that opens the range picker dialog.

### Customization and robustness

* The text-field composables (`NepaliDateTextField`, `NepaliDateField`, `NepaliDateRangeTextField`, `NepaliDateRangeField`) and `NepaliDatePickerDocked` now expose the full Material3 surface: `shape`, `textStyle`, `prefix`, `suffix`, and `interactionSource` (plus a `trailingIcon` and dropdown `popupShape` / `popupShadowElevation` on the docked field).
* `NepaliWheelDatePicker` exposes `itemHeight`, `visibleItemCount` (coerced to an odd number), `shape`, and per-row `selectedTextStyle` / `unselectedTextStyle`.
* The pickers no longer crash on bad input. `rememberNepaliDatePickerState` / `rememberNepaliDateRangePickerState` (and the state setters) coerce instead of throwing: the displayed month is clamped into `yearRange`, and an out-of-range or non-existent initial selected date resolves to no selection. The field composables likewise seed their dialog state defensively.

### Accessibility

* Every calendar day cell now exposes a `contentDescription` with its full localized date (and "today"), so screen readers announce the whole cell instead of just the number.
* Localized content descriptions on the display-mode toggle and the previous/next month arrows (previously an unlocalized placeholder or `null`).
* Weekday header letters map to their full weekday name via `clearAndSetSemantics`.
* New localized strings on `NepaliDatePickerLang`: `switchToInputModeContentDescription`, `switchToCalendarModeContentDescription`, `nextMonthContentDescription`, `previousMonthContentDescription`, `selectYearContentDescription`.

### Performance

* `:core` now memoizes month details and computes the day offset in O(1) via a cumulative-days prefix table, instead of re-summing every year on each call. Behavior-preserving.
* Recomposition fixes across all four pickers: `NepaliCalendarModel` and `today` are now remembered instead of rebuilt every recomposition; `derivedStateOf` over pure values replaced with plain `remember`; a per-cell `MutableState` allocation removed; day cells are keyed on the month so a reused list slot cannot serve a stale date. `NepaliCalendarModel` now reports `stable` in the Compose compiler report.

### Correctness fixes

* `todayNepaliCalendar` / `todayNepaliSimpleDate` / `todayEnglish*` now read the wall clock on each access. They were captured once at construction, so `today` never rolled over at midnight for the process lifetime.
* English dates before the earliest convertible anchor (1913-04-13) now throw `IllegalArgumentException`. They previously passed the year-only range check and silently returned Nepali 1970-01-01.
* Out-of-table years now surface a clear `IllegalArgumentException` instead of leaking the lookup map's `NoSuchElementException`.
* `NepaliDatePickerColors.equals` / `hashCode` now include `navigationContentColor`, `dividerColor`, and `dateTextFieldColors` (previously omitted, which could cause wrong Compose skipping).
* `weekOfMonth` uses the clamped day of month.

### De-duplication and theming

* All four calendar pickers now render through a single `NepaliMonth` / `NepaliDay` cell path (parameterized by day shape and an optional English day). This removes the forked `NepaliEnglishMonth` / `NepaliEnglishDay` / `NepaliEnglishMonthsNavigation` and a duplicate year-dropdown button, so the English variants inherit the same accessibility and performance work.
* Digit localization, the Nepali day-period mapping, and the text-field input mask each collapse to a single shared implementation.
* The dialog's hand-rolled `FlowRow` clone is replaced with the stable `androidx.compose.foundation.layout.FlowRow`.
* `dateTextFieldColors` is now reachable through `NepaliDatePickerDefaults.colors(...)`.

### Tests, docs, and build

* New Compose UI test infrastructure for `:ui`, with behavioral tests for day selection, per-day accessibility descriptions, the wheel, the docked picker, and the full-screen dialog.
* `NepaliDateConverter` is documented as the recommended facade over `NepaliCalendarModel`.
* Opt-in Compose compiler stability and recomposability reports via `-PenableComposeReports`.
* Toolchain: Kotlin 2.4.10, Compose 1.12.0, Material3 1.12.0-alpha03.

### New artifact

* `io.github.shivathapaa:nepali-date-picker-serialization:3.1.0` — optional `kotlinx-serialization` `KSerializer`s for `SimpleDate`, `SimpleTime`, `CustomCalendar`, `NepaliMonthCalendar`. Bring this in when you want to serialize Nepali dates over JSON / Protobuf / CBOR via Ktor, Room `TypeConverter`, DataStore, or any other `kotlinx-serialization` consumer. The `:core` POM stays annotation-free — no `kotlinx-serialization` dependency leaks into projects that do not depend on this artifact. Ships the full `:core` target matrix (every `kotlinx-datetime` target). Two flavors per primary type: string form (default, terse) and struct form (`*StructSerializer`). `NepaliDatePickerSerializersModule` registers all four defaults in one line.

### `:core` additive surface

#### `DigitScript` - numeral script independent of language

* New `enum class DigitScript { LATIN, DEVANAGARI }` in `dev.shivathapaa.nepalidatepickerkmp.data`. Lets consumer locales that share the Devanagari digit table (Maithili, Newari, Hindi, Marathi, Bhojpuri) reuse the same numeral rendering without touching `NepaliDatePickerLang`.
* New `NepaliDatePickerLang.defaultDigitScript()` extension.
* New `String.localizeDigits(DigitScript)`, `String.localizeDigits(NepaliDateLocale)`, `String.toLatinDigits()` extensions on `NepaliDateConverter`.
* New `Char.latinDigitOrNull()` public extension.
* `NepaliDateLocale` gains a defaulted `digitScript: DigitScript? = null` parameter and a `resolvedDigitScript` computed property. `.copy(language = ...)` cascades the digit script automatically when no explicit override exists; explicit overrides survive a `.copy()`.
* Dedups the private digit table that previously lived in both `NepaliDateConverter.convertToNepaliNumber` and `NepaliCalendarModel`. Both now route through `DigitScript.digits`.

#### `NepaliDateFormatter` - text-field parse / format primitive

* New `object NepaliDateFormatter` in `dev.shivathapaa.nepalidatepickerkmp.data`.
* `Pattern` enum: `YYYY_SLASH_MM_SLASH_DD`, `YYYY_DASH_MM_DASH_DD`, `DD_SLASH_MM_SLASH_YYYY`, `DD_DASH_MM_DASH_YYYY`.
* `format(date, pattern, script)` and `parse(input, pattern): SimpleDate?`. Parser accepts both Latin (`2082/02/14`) and Devanagari (`२०८२/०२/१४`) digit input regardless of locale.

#### Holiday provider SPI + working-day arithmetic

* New package `dev.shivathapaa.nepalidatepickerkmp.holiday`:
  * `interface NepaliHolidayProvider { holidays(year), isHoliday(date) }`.
  * `HolidayEntry(date, name, kind)` + `HolidayKind { GovernmentPublic, Religious, Regional, Observance }`.
  * `object NoOpHolidayProvider`.
  * `object NepaliWeekend { val Default = setOf(7) }` (Saturday only, matching Nepali office convention; 1-based-Sunday day-of-week numbering as elsewhere).
* New extension helpers:
  * `NepaliSelectableDates.excludingHolidays(provider)` - chainable.
  * `NepaliSelectableDates.excludingWeekends(weekend)` - chainable.
  * `NepaliDateConverter.workingDaysBetween(start, end, provider, weekend): Int` - exclusive of `end`, matching `getNepaliDaysInBetween` convention.
  * `NepaliDateConverter.nextWorkingDay(from, provider, weekend): SimpleDate` - inclusive of `from`.
  * `NepaliDateConverter.addWorkingDays(from, days, provider, weekend): SimpleDate` - Excel `WORKDAY` semantics: `0` is identity, positive walks forward strictly, negative walks back.
* Library ships **no** holiday data by design - sarkari bida and religious lists change year to year, and we don't want consumers stuck on baked-in stale data. The reference paid implementation of the SPI is `dev.shivathapaa.patro:patro-calendar`.

### `:ui` additive surface

* New `@Composable fun NepaliDateTextField(...)` - standalone outlined text field that edits a BS `SimpleDate`. Closes the gap where `NepaliDateInput` was only reachable through the picker dialog.
* New `@Composable fun NepaliDateField(...)` - Material3-style combo: `NepaliDateTextField` plus a trailing calendar icon that opens `NepaliDatePickerDialog`. The dialog respects the same `yearRange` / `selectableDates` / `locale` settings.
* Validation pipeline per keystroke: length check → parse → `yearRange` → `selectableDates`. Caller-supplied `isError` controls the field's error indicator; `onValueChange` emits `SimpleDate?` (`null` when input is incomplete or fails validation).

### Deprecations (still functional with `ReplaceWith`)

* `String.convertToNepaliNumber()` → `localizeDigits(DigitScript.DEVANAGARI)`.
* `String.convertToEnglishNumber()` → `toLatinDigits()`.

### Docs / infra

* New `.github/workflows/pages.yml` - builds `:sample:composeApp:wasmJsBrowserDistribution` on every push to `main`, strips the JS sourcemap (~1.5 MB), substitutes `VERSION_NAME` into the index banner, and deploys to **https://shivathapaa.github.io/Nepali-Date-Picker/**. Bundle size 4.40 MB gzipped (Skiko 8.25 MB raw is the floor - CMP wasmJs minimum).
* Sample `index.html` polished with OG / Twitter card meta, page description, theme color, footer linking to Maven Central + repo, and a loader spinner that sits behind the Compose canvas.
* "Live demo →" badge added to README.
* Roadmap docs for the five features under `docs/roadmap/issue-{1..5}.md`.

### Test coverage

* 16 new `commonTest` cases for `DigitScript`.
* 22 new for `NepaliDateFormatter`.
* 21 new for the holiday SPI + working-day helpers.
* 26 new for the four serializers, exercised on every `:serialization` target.

### Post-merge consumer step

GitHub repository owner needs to set **Settings → Pages → Source: "GitHub Actions"** once for the deploy to start working. Cannot be automated from the workflow side.

## 3.0.0 - Multi-module restructure (breaking)

The single `nepali-date-picker` artifact is replaced by two Maven coordinates and the project now uses a `build-logic/` convention-plugin layout. See the [migration guide in the README](./README.md#migrating-from-26x-to-30x) for the recipe.

### Breaking changes

#### Maven coordinates split

| Before (`2.6.x`) | After (`3.0.0`) |
| --- | --- |
| `io.github.shivathapaa:nepali-date-picker:2.6.2` | `io.github.shivathapaa:nepali-date-picker-ui:3.0.0` (full picker - transitively brings `-core`) |
| - | `io.github.shivathapaa:nepali-date-picker-core:3.0.0` (converter + calendar utilities only, no Compose Material3) |

There is **no umbrella artifact**. Consumers that previously depended on `nepali-date-picker` must switch to `nepali-date-picker-ui` (or `nepali-date-picker-core` if they don't need the UI).

#### Calendar constants extracted to `NepaliCalendarDefaults` (in `:core`)

Five symbols moved out of `NepaliDatePickerDefaults` (which now lives in `:ui` and contains only Compose / Material3 defaults):

* `NepaliDatePickerDefaults.NepaliYearRange` → `NepaliCalendarDefaults.NepaliYearRange`
* `NepaliDatePickerDefaults.EnglishYearRange` → `NepaliCalendarDefaults.EnglishYearRange`
* `NepaliDatePickerDefaults.startingNepaliCalendar` → `NepaliCalendarDefaults.startingNepaliCalendar`
* `NepaliDatePickerDefaults.endNepaliCalendar` → `NepaliCalendarDefaults.endNepaliCalendar`
* `NepaliDatePickerDefaults.startingEnglishCalendar` → `NepaliCalendarDefaults.startingEnglishCalendar`

No aliases are kept on `NepaliDatePickerDefaults`. Update the qualifier and add the import `dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults`.

#### Visibility promotions

To allow `:ui` to call into `:core` across the artifact boundary, the following are now `public`:

* `class NepaliCalendarModel` (was `internal`)
* `fun NepaliCalendarModel.compareDates(CustomCalendar, …)` (was `internal`)
* `fun NepaliCalendarModel.compareDates(SimpleDate, …)` (was `internal`)

`removeSlashDelimiter` and `datePatternAsInputFormat` stay `internal` to `:core`.

`NepaliSelectableDates` interface moved to `:core` (same package `dev.shivathapaa.nepalidatepickerkmp`), so consumers that depend on `-ui` see no import change. Consumers that depend only on `-core` can now implement / produce instances without pulling the UI artifact.

### Expanded `:core` target set

`:core` now ships for the full kotlinx-datetime target matrix instead of being limited to the Compose Multiplatform target set:

* Added: `linuxX64`, `linuxArm64`, `mingwX64`, `iosX64`, `macosX64`, `watchosArm64`, `watchosSimulatorArm64`, `watchosX64`, `tvosArm64`, `tvosSimulatorArm64`, `tvosX64`, `wasmWasi`.
* Unchanged: `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js(IR)`, `wasmJs`.
* `:ui` target set is unchanged (Compose Multiplatform 1.11 dropped Apple x86_64 so `-ui` cannot follow `-core` everywhere).

To make this work without dragging Compose into non-Compose targets, `@Immutable` / `@Stable` are now declared as `expect` annotations in the new package `dev.shivathapaa.nepalidatepickerkmp.annotation`. On Compose-supported targets they `actual typealias` to `androidx.compose.runtime.Immutable` / `Stable` via an intermediate `composeTargetsMain` source set, preserving Compose stability hints. On other targets `@OptionalExpectation` makes them a compile-time no-op. The `:core` Maven POM no longer lists `compose-runtime` as a dependency.

### `:core` additive surface

* `SimpleDate` now implements `Comparable<SimpleDate>` (lexicographic by year → month → dayOfMonth). Enables `<`, `<=`, `>=`, `>`, `sorted()`, and `coerceIn(range)` directly on `SimpleDate` instances.
* `NepaliCalendarDefaults.FIRST_DAY_OF_WEEK: Int = 1` constant added (matches the existing `firstDayOfMonth` 1-based-Sunday convention used across the converter).

### Non-breaking

* The library package (`dev.shivathapaa.nepalidatepickerkmp.*`) is unchanged.
* All composables (`NepaliDatePicker`, `NepaliDatePickerDialog`, `NepaliDateRangePicker`, `NepaliDateRangePickerWithEnglishDate`, `NepaliDateInput`, `NepaliDateRangeInput`) keep their existing signatures.
* `NepaliDateConverter`, `NepaliDateLocale`, `CustomCalendar`, and the surrounding data types are unchanged.
* iOS XCFramework continues to ship as `nepali-date-picker.xcframework` (now produced by the `:ui` module).

### Internal / project layout

* New `build-logic/convention/` project (included via `includeBuild`) hosts three convention plugins:
  * `picker.kotlinMultiplatform` - KMP targets (Android, iOS arm64 + simulator, JVM, macOS arm64, JS, WasmJs), JVM 11 toolchain, `kotlin-test` for `commonTest`.
  * `picker.composeMultiplatform` - Compose Multiplatform + Compose Compiler plugins, common Material3 / runtime / foundation dependencies.
  * `picker.mavenPublish` - Vanniktech Maven Publish plugin, `KotlinMultiplatform` publication with `SourcesJar.Sources()`, conditional signing.
* POM metadata (group, license, developer, SCM) lives in the root `gradle.properties`; each submodule sets only `POM_ARTIFACT_ID`, `POM_NAME`, and `POM_DESCRIPTION`.
* JS / WasmJs library targets no longer declare `binaries.executable()` alongside `binaries.library()` (was a Gradle 9 task-ordering conflict).
* Added `:moduleGraph` and `:checkAll` root tasks for quick inspection.
