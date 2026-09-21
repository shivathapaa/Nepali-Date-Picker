# `androidApp` - the Android host

A thin Android application. It contributes no UI of its own: a single `ComponentActivity` calls
`setContent { App() }`, where `App()` is the shared showcase from [`composeApp`](../composeApp).
Everything you see on screen is defined there.

## Layout

```
src/main/
├── AndroidManifest.xml
└── kotlin/dev/shivathapaa/nepalidatepicker/main.kt    AppActivity, enableEdgeToEdge(), setContent { App() }
```

## Configuration

| Setting | Value |
| --- | --- |
| `namespace` / `applicationId` | `dev.shivathapaa.nepalidatepicker` |
| `minSdk` / `compileSdk` / `targetSdk` | from `gradle/libs.versions.toml` (23 / 37 / 37) |
| JVM target | 11 |

## Running

```bash
./gradlew :sample:androidApp:installDebug   # installs on the connected device or running emulator
```

Or open the project in Android Studio and run the `androidApp` configuration. Shared Gradle run
configurations live in `.run/`.

## Note for API levels below 26

The library uses `kotlinx-datetime`, so consumers on `minSdk` below 26 need core library
desugaring. See the "Android setup for api levels below 26" section of the [root
README](../../README.md).
