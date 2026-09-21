# Samples

Runnable apps that consume the library exactly the way a real project would. None of them is
published as a package; the `androidApp` and `flutterApp` showcases ride along on each GitHub
release as `nepali-date-picker-sample.apk` and `nepali-date-picker-flutter-sample.apk`.

```
sample/
├── composeApp/    Shared Compose Multiplatform showcase + desktop and web entry points
├── androidApp/    Android host for the shared showcase
├── iosApp/        SwiftUI host that renders the shared showcase through Compose Multiplatform
├── iosSwiftApp/   Native SwiftUI app that consumes the iOS XCFramework bridge
├── jsApp/         Vite showcase for the npm web packages (deployed to Pages under /demo)
└── flutterApp/    Flutter showcase for the nepali_date_picker_kmp plugin (Android and iOS)
```

## What each sample consumes

The six apps reach the library through four different distribution paths, so each one shows what a
consumer on that path gets.

| Sample | Consumes | Path under test |
| --- | --- | --- |
| [`composeApp`](composeApp) | `projects.nepaliDatePicker.ui` and `projects.nepaliDatePicker.serialization` (Gradle project dependencies) | The Compose API on desktop, web (`js` and `wasmJs`), Android and iOS, plus the serializers |
| [`androidApp`](androidApp) | `:sample:composeApp` | The Android artifact inside a real `ComponentActivity` |
| [`iosApp`](iosApp) | `:sample:composeApp` as a Kotlin framework | Compose Multiplatform hosted in SwiftUI |
| [`iosSwiftApp`](iosSwiftApp) | `nepali_date_picker.xcframework` from `:nepali-date-picker:ui` | The Swift bridge (`UIViewController` factories) the SPM release ships |
| [`jsApp`](jsApp) | Built `dist` of `@nepali-date-picker/core` and `@nepali-date-picker/web-component` | The npm packages and the custom elements |
| [`flutterApp`](flutterApp) | [`flutter/nepali_date_picker_kmp`](../flutter/nepali_date_picker_kmp) as a path dependency | The pigeon bridge, the embedded platform views and the native dialogs a pub.dev consumer gets |

## Identifiers

Every sample sits under `dev.shivathapaa.nepalidatepicker`, distinct from the library's own
`dev.shivathapaa.nepalidatepickerkmp`. The two iOS apps take a leaf each, so installing one does
not replace the other.

| Sample | Identifier |
| --- | --- |
| Kotlin package, shared by `composeApp` and `androidApp` | `dev.shivathapaa.nepalidatepicker` |
| `androidApp` namespace and `applicationId` | `dev.shivathapaa.nepalidatepicker` |
| `composeApp` desktop bundle (macOS) | `dev.shivathapaa.nepalidatepicker` |
| `iosApp` bundle identifier | `dev.shivathapaa.nepalidatepicker.compose` |
| `iosSwiftApp` bundle identifier | `dev.shivathapaa.nepalidatepicker.swift` |
| `flutterApp` Android `applicationId` and iOS bundle identifier | `dev.shivathapaa.nepalidatepicker.flutter` |

## Running them

All Gradle commands run from the repository root through the wrapper. The first run downloads
dependencies and can take several minutes; later runs are fast.

| Goal | Command |
| --- | --- |
| Desktop (Compose) | `./gradlew :sample:composeApp:run` |
| Web, Compose on Wasm | `./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun` |
| Web, Compose on JS | `./gradlew :sample:composeApp:jsBrowserDevelopmentRun` |
| Android | `./gradlew :sample:androidApp:installDebug`, or run the `androidApp` configuration in Android Studio |
| iOS, Compose UI | Open `sample/iosApp/iosApp.xcodeproj` in Xcode and run |
| iOS, Swift consumer | `./gradlew :nepali-date-picker:ui:assembleNepali-date-pickerReleaseXCFramework`, then open `sample/iosSwiftApp/iosSwiftApp.xcodeproj` and run |
| Web components (npm) | `cd js && npm install && npm run build`, then `cd sample/jsApp && npm install && npm run dev` |
| Flutter | `./gradlew publishToMavenLocal`, then `cd sample/flutterApp && flutter run` (iOS first needs `flutter/nepali_date_picker_kmp/tool/stage_ios_framework.sh`) |

### Prerequisites

- **JDK 17 or newer** for every Gradle sample (CI builds on 21).
- **Android Studio** or the Android SDK for `androidApp` (`minSdk` 23, `compileSdk` 37).
- **Xcode** on macOS for `iosApp` and `iosSwiftApp`.
- **Node.js 18 or newer** for `jsApp`.
- **Flutter 3.44 or newer** for `flutterApp` (Android and iOS only; the plugin bridges the compiled binaries, so web and desktop have no path to them).

## Which one should I run?

- To see the **Compose pickers** the library ships for Android, desktop and web: `composeApp`
  (desktop is the fastest loop).
- To check a change against a **real Android app**: `androidApp`.
- To check **Compose Multiplatform on iOS**: `iosApp`.
- To check the **Swift API surface** a SPM consumer sees, including Xcode-side requirements:
  `iosSwiftApp`.
- To see the **framework-agnostic web pickers** and the headless conversion engine the way a
  JavaScript consumer uses them: `jsApp`.
- To check what a **Flutter consumer** gets from pub.dev, engine and embedded pickers alike:
  `flutterApp`.

## How the showcases are organised

`composeApp`, `iosSwiftApp` and `flutterApp` are each an index of screens grouped into
**Pickers**, **Fields**, **Events** and **Engine**, so the three read the same way;
`flutterApp` mirrors the `iosSwiftApp` rows one for one. `jsApp` is a single scrolling page of
cards covering the same ground for the custom elements.

Each also covers the Bikram Sambat and Gregorian display options: `composeApp` folds them into the
picker, wheel, docked, dialog and text-field screens, `iosSwiftApp` gives them a "Calendar switch"
screen, and `jsApp` gives them two cards.

### The appearance menu

Every sample can be switched between a light, dark and system brightness and six Material palettes
(default, green, blue, orange, red, yellow), from a control in its top bar or toolbar. The palettes
are the same colour values in all three, so the same screen can be compared across platforms, and
the screenshots in the root README are reproduced by picking a palette here.

| Sample | Control | Where the palette lives |
| --- | --- | --- |
| `composeApp` (and so `androidApp`, `iosApp`, desktop, web) | Gear icon in the top app bar | [`SampleColorSchemes.kt`](composeApp/src/commonMain/kotlin/dev/shivathapaa/nepalidatepicker/SampleColorSchemes.kt) |
| `iosSwiftApp` | Palette icon in the navigation bar | [`SamplePaletteRoles.swift`](iosSwiftApp/iosSwiftApp/SamplePaletteRoles.swift), applied through `NepaliPickerAppearance` |
| `jsApp` | Theme and Palette selects in the toolbar | The `--ndp-*` custom properties in [`styles.css`](jsApp/styles.css) |
| `flutterApp` | Palette icon in the app bar | [`sample_palettes.dart`](flutterApp/lib/src/support/sample_palettes.dart), applied to the Flutter theme and through the Dart `NepaliPickerAppearance` |

The three routes differ because that is how a consumer themes each one: a Compose app supplies a
`ColorScheme`, a Swift app sets the shared `NepaliPickerAppearance` proxy, and a web page sets CSS
custom properties.

`:nepali-date-picker:serialization` is demonstrated only in `composeApp`: `:ui` exports `:core`
into the XCFramework and not the serializers, and the npm packages carry no equivalent.
