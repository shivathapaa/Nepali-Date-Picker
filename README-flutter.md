# Nepali Date Picker for Flutter

The Flutter distribution of this library is the pub.dev package
[`nepali_date_picker_kmp`](https://pub.dev/packages/nepali_date_picker_kmp), a plugin that
bridges the compiled Kotlin Multiplatform engine and pickers in this repository. The same
binaries ship to every channel, so the Flutter, Android, iOS, JS and JVM answers always agree.

```yaml
dependencies:
  nepali_date_picker_kmp: ^3.3.0
```

```dart
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

final bs = await NepaliDateConverter.convertAdToBs(2026, 9, 20);
final picked = await showNepaliDatePickerDialog();
```

What it carries:

- The `:core` engine behind async facades: conversion, month grids, arithmetic, formatting,
  digit localization, ISO 8601, wire formatters, events, policies and working-day math, with the
  same curated surface as the npm package.
- The `:ui` Compose Material3 pickers as embedded widgets and native dialogs, themed through the
  same appearance proxy the Swift host uses.

Platform notes:

- Android and iOS. Web and desktop consumers use `@nepali-date-picker/core` (npm) or the Maven
  artifacts directly.
- Android needs `compileSdk` 37 and `minSdk` 23 or later.
- iOS needs iOS 15 and Swift Package Manager (the Flutter default since 3.44). There is no
  CocoaPods support.

The full API and the current limitations are documented in the
[package README](./flutter/nepali_date_picker_kmp/README.md). A multi-page showcase lives beside
the other platforms in [`sample/flutterApp`](./sample/flutterApp).
