# Samples

Runnable apps that consume the library exactly the way a real project would. Nothing here is
published; the samples exist to demonstrate the API, to be the manual test bed for a release, and to
catch packaging regressions in CI.

```
sample/
├── composeApp/    Shared Compose Multiplatform showcase + desktop and web entry points
├── androidApp/    Android host for the shared showcase
├── iosApp/        SwiftUI host that renders the shared showcase through Compose Multiplatform
├── iosSwiftApp/   Native SwiftUI app that consumes the iOS XCFramework bridge
└── jsApp/         Vite showcase for the npm web packages (deployed to Pages under /demo)
```

## What each sample consumes

The five apps deliberately reach the library through three different distribution paths, so a
mistake in any one of them shows up in a sample rather than in a consumer's project.

| Sample | Consumes | Path under test |
| --- | --- | --- |
| [`composeApp`](composeApp) | `projects.nepaliDatePicker.ui` (Gradle project dependency) | The Compose API on desktop, web (`js` and `wasmJs`), Android and iOS |
| [`androidApp`](androidApp) | `:sample:composeApp` | The Android artifact inside a real `ComponentActivity` |
| [`iosApp`](iosApp) | `:sample:composeApp` as a Kotlin framework | Compose Multiplatform hosted in SwiftUI |
| [`iosSwiftApp`](iosSwiftApp) | `nepali_date_picker.xcframework` from `:nepali-date-picker:ui` | The Swift bridge (`UIViewController` factories) the SPM release ships |
| [`jsApp`](jsApp) | Built `dist` of `@nepali-date-picker/core` and `@nepali-date-picker/web-component` | The npm packages and the custom elements |

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

### Prerequisites

- **JDK 17 or newer** for every Gradle sample (CI builds on 21).
- **Android Studio** or the Android SDK for `androidApp` (`minSdk` 23, `compileSdk` 37).
- **Xcode** on macOS for `iosApp` and `iosSwiftApp`.
- **Node.js 18 or newer** for `jsApp`.

## Which one should I run?

- To see the **Compose pickers** the library ships for Android, desktop and web: `composeApp`
  (desktop is the fastest loop).
- To check a change against a **real Android app**: `androidApp`.
- To check **Compose Multiplatform on iOS**: `iosApp`.
- To check the **Swift API surface** a SPM consumer sees, including Xcode-side requirements:
  `iosSwiftApp`. This is the one CI builds with `xcodebuild`.
- To see the **framework-agnostic web pickers** and the headless conversion engine the way a
  JavaScript consumer uses them: `jsApp`.

## Keeping the showcases in sync

`composeApp`, `iosSwiftApp` and `jsApp` all present the same groups (pickers, wheel and docked,
dialogs, text fields, customization, selectable dates, utilities), and each covers the Bikram Sambat
and Gregorian display options as well: `composeApp` folds them into the picker, wheel, docked, dialog
and text-field groups, `iosSwiftApp` gives them a "Calendar switch" screen, and `jsApp` gives them two
cards. When a new public API lands, add it to each showcase that can host it, so no platform silently
lags behind.
