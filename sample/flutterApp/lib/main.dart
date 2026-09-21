// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import 'src/home_screen.dart';
import 'src/support/sample_palettes.dart';

void main() {
  runApp(const NepaliDatePickerExampleApp());
}

/// The Flutter showcase, structured like the Compose and SwiftUI samples:
/// an index grouped into Pickers, Fields, Events and Engine, with the shared
/// appearance menu in the app bar.
class NepaliDatePickerExampleApp extends StatefulWidget {
  const NepaliDatePickerExampleApp({super.key});

  @override
  State<NepaliDatePickerExampleApp> createState() =>
      _NepaliDatePickerExampleAppState();
}

class _NepaliDatePickerExampleAppState extends State<NepaliDatePickerExampleApp>
    with WidgetsBindingObserver {
  SamplePalette _palette = SamplePalette.standard;
  NepaliPickerBrightness _brightness = NepaliPickerBrightness.system;

  bool get _systemDark =>
      WidgetsBinding.instance.platformDispatcher.platformBrightness ==
      Brightness.dark;

  bool get _dark => switch (_brightness) {
        NepaliPickerBrightness.system => _systemDark,
        NepaliPickerBrightness.light => false,
        NepaliPickerBrightness.dark => true,
      };

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _applyToPickers();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangePlatformBrightness() {
    setState(() {});
    _applyToPickers();
  }

  void _applyToPickers() {
    applyPaletteToPickers(
      _palette,
      brightness: _brightness,
      systemDark: _systemDark,
    );
  }

  void _select({SamplePalette? palette, NepaliPickerBrightness? brightness}) {
    setState(() {
      if (palette != null) _palette = palette;
      if (brightness != null) _brightness = brightness;
    });
    _applyToPickers();
  }

  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'Nepali Date Picker',
        theme: ThemeData(colorScheme: colorSchemeFor(_palette, dark: false)),
        darkTheme: ThemeData(colorScheme: colorSchemeFor(_palette, dark: true)),
        themeMode: switch (_brightness) {
          NepaliPickerBrightness.system => ThemeMode.system,
          NepaliPickerBrightness.light => ThemeMode.light,
          NepaliPickerBrightness.dark => ThemeMode.dark,
        },
        home: HomeScreen(
          palette: _palette,
          brightness: _brightness,
          dark: _dark,
          onPaletteSelected: (palette) => _select(palette: palette),
          onBrightnessSelected: (brightness) =>
              _select(brightness: brightness),
        ),
      );
}
