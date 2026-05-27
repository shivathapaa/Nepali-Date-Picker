# Contributing to Nepali-Date-Picker

Thank you for considering contributing to Nepali-Date-Picker! We welcome contributions of all kinds,
including bug fixes, new features, and more. By contributing,
you help make this project better for everyone.

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code. Be kind to everyone!

## Getting Started

To get started with contributing to Nepali-Date-Picker:

1. Fork the repository: Click the "Fork" button at the top of this repository.
2. Clone your fork:
```bash
git clone https://github.com/shivathapaa/Nepali-Date-Picker/
```
3. Create a branch:
```bash
git checkout -b feature/your-feature-name
```
```bash
git checkout -b bugfix/your-bug-fix-name
```
4. Make your changes: Follow the guidelines below for contributing.
5. Ensure all commits are signed using GPG. Unsigned commits will not be accepted. Learn how to [sign commits](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits).
6. Commit your changes:
```bash
git commit -m "Describe your changes"
```
7. Push your branch:
```bash
git push origin feature/your-feature-name
```
8. Submit a pull request: Go to the repository on GitHub and open a pull request.

## How to Contribute

### Reporting Bugs

If you find a bug in the project, please:

1. Search existing issues to see if the bug has already been reported.
2. Open a new issue and include (follow the template in issues):
    - A clear and descriptive title.
    - A detailed description of the bug.
    - Steps to reproduce the bug.
    - Any relevant screenshots or code snippets.

### Suggesting Features

To suggest a new feature:

1. Check existing feature requests to see if the feature has already been suggested.
2. Open a new issue with the title "Feature Request: [Your Feature]" and provide:
    - A detailed description of the feature.
    - Try to answer `Why?` question.
    - Any examples or use cases.

### Submitting Pull Requests

> Try not to open pull request for minor typos if its not breaking anything in code logics. However, feel free to [open an issue](https://github.com/shivathapaa/Nepali-Date-Picker/issues/new/choose) for reporting it.

When you're ready to submit a pull request:

1. Ensure your code adheres to the style guide. (You can refer [Style Guide from Kotlin Docs](https://kotlinlang.org/docs/coding-conventions.html) or [Style Guide from Android Devlopers](https://developer.android.com/kotlin/style-guide))
2. Include tests for any new features or bug fixes.
3. Ensure all tests pass.
4. Update the documentation as needed.
5. Open a pull request with a clear description of what you've done and why.

### Running tests locally

The repository ships shared IntelliJ / Android Studio run configurations under `.run/` - open the project and they appear in the run-configuration dropdown. From the terminal:

```bash
# Fast local sweep - JVM + Apple native + JS/Wasm node (no browser, no Chrome required).
# Mirrors the macOS leg of CI, runs green on a typical laptop without Xcode simulator SDKs.
./gradlew checkLocal

# Full :check across every leaf module. Includes browser tests, so needs Chrome (via CHROME_BIN)
# or `-PenableBrowserTests`. Intended for CI runners.
./gradlew checkAll

# Single platform examples (mirrors what the run configurations under .run/ do):
./gradlew :nepali-date-picker:core:jvmTest
./gradlew :nepali-date-picker:core:iosSimulatorArm64Test
./gradlew :nepali-date-picker:core:macosArm64Test
./gradlew :nepali-date-picker:core:jsNodeTest
./gradlew :nepali-date-picker:core:wasmJsNodeTest
```

Opt-in flags for environments that can run them:
- `-PenableBrowserTests` - runs `jsBrowserTest` / `wasmJsBrowserTest` (needs Chrome).
- `-PenableAppleSimulatorTargets` (or `CI=true`) - declares the `iosX64`, `watchosSimulatorArm64`, `tvosSimulatorArm64` targets locally; CI already sets `CI=true`, so the published artifact always ships them.

### Running the sample app

The repository ships a Kotlin Multiplatform sample under `sample/` so you can verify changes end-to-end on every platform the picker UI supports.

| Module | Targets |
| --- | --- |
| `:sample:androidApp` | Android application |
| `:sample:composeApp` | Compose Multiplatform module: Desktop (JVM), JS browser, Wasm browser, iOS framework |
| `sample/iosApp/iosApp.xcodeproj` | Xcode project that consumes the iOS framework produced by `:sample:composeApp` |

#### Android
Open the project in Android Studio and run the `androidApp` configuration, or from the terminal:
```bash
# Install the debug build on a connected device / running emulator.
./gradlew :sample:androidApp:installDebug

# Just build the APK without installing.
./gradlew :sample:androidApp:assembleDebug
# Output: sample/androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

#### Desktop (JVM)
```bash
# Run the Compose Desktop app.
./gradlew :sample:composeApp:run

# Build a native installer for your host OS (Dmg on macOS, Msi on Windows, Deb on Linux).
./gradlew :sample:composeApp:packageDmg     # macOS
./gradlew :sample:composeApp:packageMsi     # Windows
./gradlew :sample:composeApp:packageDeb     # Linux
```

#### iOS
Open `sample/iosApp/iosApp.xcodeproj` in Xcode and run the standard scheme on a simulator or device. The Xcode build invokes the Gradle task that produces the `ComposeApp` framework from `:sample:composeApp`. The [Kotlin Multiplatform Mobile plugin for Android Studio](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) also works.

#### JS Browser
```bash
./gradlew :sample:composeApp:jsBrowserDevelopmentRun
```

#### Wasm Browser
```bash
./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun
```

#### Publishing locally for end-to-end testing
To consume your local changes from another project, publish the two artifacts to `mavenLocal()`:
```bash
./gradlew :nepali-date-picker:core:publishToMavenLocal :nepali-date-picker:ui:publishToMavenLocal
```
A shared run configuration `Publish To MavenLocal` under `.run/` does the same from the IDE.

## New to Git?

Resources: https://lab.github.com and https://try.github.com/

## License

This project is licensed under [MPL 2.0](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE)

## About Nepali-Date-Picker

The Nepali Date Picker Library provides UI and different utilities for working with Nepali dates on Android, iOS, and Kotlin Multiplatform (KMP). It aligns with the Material3 Date Picker, and acts as a bridge between Nepali and Gregorian calendars.

