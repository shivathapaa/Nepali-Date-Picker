# flutterApp

The Flutter showcase for the [`nepali_date_picker_kmp`](../../flutter/nepali_date_picker_kmp)
plugin, structured the way `composeApp` and `iosSwiftApp` are: a Today readout, then an index
grouped into **Pickers**, **Fields**, **Events** and **Engine**, with the shared appearance menu
in the app bar. The screens mirror the SwiftUI showcase row for row, so a demo found in one has
an obvious home in the other:

- **Pickers**: Pickers, Calendar switch, Wheel & Docked, Dialogs, Chrome and dimensions,
  Customization
- **Fields**: Text fields, Field states, Selectable dates, Composing rules
- **Events**: Events, Event queries
- **Engine**: Utilities, Engine queries (including a month grid rendered purely with Flutter
  widgets from engine data)

The appearance menu switches between a light, dark and system brightness and the six shared
Material palettes; the values live in
[`sample_palettes.dart`](lib/src/support/sample_palettes.dart) and match `SampleColorSchemes.kt`
and `SamplePaletteRoles.swift`, applied both to the Flutter theme and, through the Dart
`NepaliPickerAppearance`, to every native picker on screen.

Two things the bridge cannot carry are absent: custom selectable-date predicates (the shipped
before, after, range, weekend and closure rules stand in) and provider objects (events travel as
lists, so merging and filtering happen on the Dart lists to the same effect).

Android and iOS only, which is what the plugin supports. Flutter web and desktop consumers use
`@nepali-date-picker/core` from npm instead.

## Running

While the wrapped library version is unpublished, publish it locally first from the repository
root:

```sh
./gradlew publishToMavenLocal
```

Android:

```sh
cd sample/flutterApp
flutter run
```

iOS additionally needs the Kotlin framework staged once:

```sh
flutter/nepali_date_picker_kmp/tool/stage_ios_framework.sh
cd sample/flutterApp
flutter run
```
