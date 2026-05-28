# Changelog

All notable changes to **Nepali-Date-Picker (KMP)** are documented here.

The project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html). Pre-3.0 release history lives in the [GitHub Releases](https://github.com/shivathapaa/Nepali-Date-Picker/releases) page.

## 3.1.0 - Digit script, text-field input, holiday SPI, serialization artifact

Five additive features in one release. No breaking changes — every existing public symbol works unchanged; new APIs are opt-in.

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
