// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:nepali_date_picker_flutter_sample/src/support/sample_palettes.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

/// Pins the shared palette table and the way it reaches the native pickers.
///
/// The same six palettes exist in `SampleColorSchemes.kt` and
/// `SamplePaletteRoles.swift`; these tests keep the Dart copy well formed and
/// prove that picking one writes all eleven appearance slots, which is what
/// makes the Flutter chrome and the embedded pickers recolour together.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const appearanceChannel =
      'dev.flutter.pigeon.nepali_date_picker_kmp.PickerHostApi.applyAppearance';
  const codec = StandardMessageCodec();
  var applied = 0;

  setUp(() {
    applied = 0;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMessageHandler(appearanceChannel, (message) async {
      applied++;
      return codec.encodeMessage(<Object?>[null]);
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMessageHandler(appearanceChannel, null);
  });

  List<int> slotsOf(PaletteRoles roles) => <int>[
        roles.primary,
        roles.onPrimary,
        roles.primaryContainer,
        roles.onPrimaryContainer,
        roles.secondaryContainer,
        roles.onSecondaryContainer,
        roles.surface,
        roles.onSurface,
        roles.surfaceVariant,
        roles.onSurfaceVariant,
        roles.outline,
      ];

  test('every palette fills all eleven slots in both brightnesses', () {
    expect(SamplePalette.values, hasLength(6));
    for (final palette in SamplePalette.values) {
      for (final dark in <bool>[false, true]) {
        final slots = slotsOf(palette.roles(dark: dark));
        expect(slots, hasLength(11), reason: '${palette.label} dark=$dark');
        for (final slot in slots) {
          expect(slot >> 24 & 0xFF, 0xFF,
              reason: '${palette.label} dark=$dark has a translucent slot');
        }
      }
    }
  });

  test('light and dark are different palettes, and so are the six', () {
    for (final palette in SamplePalette.values) {
      expect(slotsOf(palette.roles(dark: false)),
          isNot(slotsOf(palette.roles(dark: true))),
          reason: palette.label);
    }
    final primaries = SamplePalette.values
        .map((palette) => palette.roles(dark: false).primary)
        .toSet();
    expect(primaries, hasLength(SamplePalette.values.length));
  });

  test('the Flutter scheme takes its roles from the same table', () {
    final roles = SamplePalette.green.roles(dark: false);
    final scheme = colorSchemeFor(SamplePalette.green, dark: false);

    expect(scheme.brightness, Brightness.light);
    expect(scheme.primary, Color(roles.primary));
    expect(scheme.onPrimary, Color(roles.onPrimary));
    expect(scheme.primaryContainer, Color(roles.primaryContainer));
    expect(scheme.secondaryContainer, Color(roles.secondaryContainer));
    expect(scheme.surface, Color(roles.surface));
    expect(scheme.onSurface, Color(roles.onSurface));
    expect(scheme.surfaceContainerHighest, Color(roles.surfaceVariant));
    expect(scheme.onSurfaceVariant, Color(roles.onSurfaceVariant));
    expect(scheme.outline, Color(roles.outline));
    expect(scheme.surfaceTint, Color(roles.primary),
        reason: 'the accent tints elevated surfaces, as on the native side');
    expect(colorSchemeFor(SamplePalette.green, dark: true).brightness,
        Brightness.dark);
  });

  test('picking a palette writes every native slot and pushes it once',
      () async {
    await applyPaletteToPickers(
      SamplePalette.blue,
      brightness: NepaliPickerBrightness.dark,
      systemDark: false,
    );

    final roles = SamplePalette.blue.roles(dark: true);
    expect(NepaliPickerAppearance.brightness, NepaliPickerBrightness.dark);
    expect(NepaliPickerAppearance.primary, Color(roles.primary));
    expect(NepaliPickerAppearance.onPrimary, Color(roles.onPrimary));
    expect(NepaliPickerAppearance.primaryContainer,
        Color(roles.primaryContainer));
    expect(NepaliPickerAppearance.onPrimaryContainer,
        Color(roles.onPrimaryContainer));
    expect(NepaliPickerAppearance.secondaryContainer,
        Color(roles.secondaryContainer));
    expect(NepaliPickerAppearance.onSecondaryContainer,
        Color(roles.onSecondaryContainer));
    expect(NepaliPickerAppearance.surface, Color(roles.surface));
    expect(NepaliPickerAppearance.onSurface, Color(roles.onSurface));
    expect(NepaliPickerAppearance.surfaceVariant, Color(roles.surfaceVariant));
    expect(NepaliPickerAppearance.onSurfaceVariant,
        Color(roles.onSurfaceVariant));
    expect(NepaliPickerAppearance.outline, Color(roles.outline));
    expect(applied, 1);
  });

  test('following the system takes the device brightness', () async {
    await applyPaletteToPickers(
      SamplePalette.red,
      brightness: NepaliPickerBrightness.system,
      systemDark: true,
    );

    expect(NepaliPickerAppearance.brightness, NepaliPickerBrightness.system);
    expect(NepaliPickerAppearance.primary,
        Color(SamplePalette.red.roles(dark: true).primary));

    await applyPaletteToPickers(
      SamplePalette.red,
      brightness: NepaliPickerBrightness.system,
      systemDark: false,
    );

    expect(NepaliPickerAppearance.primary,
        Color(SamplePalette.red.roles(dark: false).primary));
  });
}
