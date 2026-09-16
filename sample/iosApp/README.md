# `iosApp` - Compose Multiplatform on iOS

A SwiftUI shell that renders the shared Compose showcase from [`composeApp`](../composeApp). This is
the Compose Multiplatform path: the whole screen is Kotlin UI, hosted in a `UIViewController`.

```swift
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainKt.MainViewController()   // ComposeUIViewController { App() }
    }
}
```

If instead you want to see how a **native SwiftUI app** consumes the library through the published
XCFramework, that is [`iosSwiftApp`](../iosSwiftApp). The two are different consumption models and
both are worth keeping.

## Layout

```
iosApp/
├── iosApp.xcodeproj
└── iosApp/
    ├── iosApp.swift    @main App + UIViewControllerRepresentable around MainViewController()
    └── Info.plist
```

## How the Kotlin framework gets there

The Xcode project has a run script build phase that calls Gradle before compiling Swift:

```
./gradlew :sample:composeApp:embedAndSignAppleFrameworkForXcode
```

That builds the `ComposeApp` framework for the active architecture and configuration, then embeds
and signs it. No manual Gradle step is needed, and no framework is checked in.

## Running

1. Open `sample/iosApp/iosApp.xcodeproj` in Xcode (requires macOS and Xcode).
2. Pick an iOS simulator or a device.
3. Run. The first build is slow because Kotlin/Native has to link the framework.

If Xcode reports that the `ComposeApp` module is missing, build once from the command line to warm
the framework, then build again in Xcode:

```bash
./gradlew :sample:composeApp:linkDebugFrameworkIosSimulatorArm64
```
