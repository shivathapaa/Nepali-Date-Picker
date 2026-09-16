# `androidApp` - the Android host

A deliberately thin Android application. It contributes no UI of its own: a single
`ComponentActivity` calls `setContent { App() }`, where `App()` is the shared showcase from
[`composeApp`](../composeApp). Everything you see on screen is defined there.

The point of keeping it this thin is that it tests the Android artifact and its packaging (manifest,
edge-to-edge, `minSdk`, R8-free debug build) without a second copy of the demos to maintain.

## Layout

```
src/main/
├── AndroidManifest.xml
└── kotlin/dev/shivathapaa/sample/app/main.kt    AppActivity, enableEdgeToEdge(), setContent { App() }
```

## Configuration

| Setting | Value |
| --- | --- |
| `applicationId` | `sample.app.androidApp` |
| `minSdk` / `compileSdk` / `targetSdk` | from `gradle/libs.versions.toml` (23 / 37 / 37) |
| JVM target | 11 |

## Running

```bash
./gradlew :sample:androidApp:installDebug   # installs on the connected device or running emulator
```

Or open the project in Android Studio and run the `androidApp` configuration. Shared Gradle run
configurations live in `.run/`.

CI builds `:sample:androidApp:assembleDebug` on both the Linux and macOS legs, so a break in the
Android packaging fails the build before a release.

## Note for API levels below 26

The library uses `kotlinx-datetime`, so consumers on `minSdk` below 26 need core library
desugaring. See the "Android setup for api levels below 26" section of the [root
README](../../README.md).
