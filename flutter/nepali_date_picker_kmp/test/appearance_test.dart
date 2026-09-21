// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'dart:ui';

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';

import 'recording_host_api.dart';

/// Pins the appearance proxy: which slot lands in which wire field, and the
/// zero that stands for "keep Material's own value".
void main() {
  late RecordingHostApi host;

  setUp(() async {
    host = RecordingHostApi();
    bridge.pickerHostApi = host;
    await NepaliPickerAppearance.reset();
  });

  tearDown(() async {
    await NepaliPickerAppearance.reset();
  });

  test('an untouched appearance sends zeros and follows the device', () async {
    await NepaliPickerAppearance.apply();

    final sent = host.appearances.single;
    expect(sent.brightness, BrightnessDto.system);
    expect(
        <int>[
          sent.primaryArgb,
          sent.onPrimaryArgb,
          sent.primaryContainerArgb,
          sent.onPrimaryContainerArgb,
          sent.secondaryContainerArgb,
          sent.onSecondaryContainerArgb,
          sent.surfaceArgb,
          sent.onSurfaceArgb,
          sent.surfaceVariantArgb,
          sent.onSurfaceVariantArgb,
          sent.outlineArgb,
        ],
        everyElement(0));
  });

  test('every slot lands in the wire field that names it', () async {
    NepaliPickerAppearance.brightness = NepaliPickerBrightness.dark;
    NepaliPickerAppearance.primary = const Color(0xFFB1D18A);
    NepaliPickerAppearance.onPrimary = const Color(0xFF1F3701);
    NepaliPickerAppearance.primaryContainer = const Color(0xFF354E16);
    NepaliPickerAppearance.onPrimaryContainer = const Color(0xFFCDEDA3);
    NepaliPickerAppearance.secondaryContainer = const Color(0xFF3A4A34);
    NepaliPickerAppearance.onSecondaryContainer = const Color(0xFFD6E8C8);
    NepaliPickerAppearance.surface = const Color(0xFF12140E);
    NepaliPickerAppearance.onSurface = const Color(0xFFE2E3D8);
    NepaliPickerAppearance.surfaceVariant = const Color(0xFF44483D);
    NepaliPickerAppearance.onSurfaceVariant = const Color(0xFFC5C8BA);
    NepaliPickerAppearance.outline = const Color(0xFF8E9285);

    await NepaliPickerAppearance.apply();

    final sent = host.appearances.single;
    expect(sent.brightness, BrightnessDto.dark);
    expect(sent.primaryArgb, 0xFFB1D18A);
    expect(sent.onPrimaryArgb, 0xFF1F3701);
    expect(sent.primaryContainerArgb, 0xFF354E16);
    expect(sent.onPrimaryContainerArgb, 0xFFCDEDA3);
    expect(sent.secondaryContainerArgb, 0xFF3A4A34);
    expect(sent.onSecondaryContainerArgb, 0xFFD6E8C8);
    expect(sent.surfaceArgb, 0xFF12140E);
    expect(sent.onSurfaceArgb, 0xFFE2E3D8);
    expect(sent.surfaceVariantArgb, 0xFF44483D);
    expect(sent.onSurfaceVariantArgb, 0xFFC5C8BA);
    expect(sent.outlineArgb, 0xFF8E9285);
  });

  test('a partly themed app leaves the untouched roles to Material',
      () async {
    NepaliPickerAppearance.brightness = NepaliPickerBrightness.light;
    NepaliPickerAppearance.primary = const Color(0xFF1E88E5);

    await NepaliPickerAppearance.apply();

    final sent = host.appearances.single;
    expect(sent.brightness, BrightnessDto.light);
    expect(sent.primaryArgb, 0xFF1E88E5);
    expect(sent.surfaceArgb, 0);
    expect(sent.outlineArgb, 0);
  });

  test('reset clears the slots here and on the native side', () async {
    NepaliPickerAppearance.brightness = NepaliPickerBrightness.dark;
    NepaliPickerAppearance.primary = const Color(0xFF1E88E5);

    await NepaliPickerAppearance.reset();

    expect(NepaliPickerAppearance.brightness, NepaliPickerBrightness.system);
    expect(NepaliPickerAppearance.primary, isNull);
    expect(NepaliPickerAppearance.outline, isNull);
    expect(host.resets, greaterThan(0));
  });

  test('applying twice sends the latest slots both times', () async {
    NepaliPickerAppearance.primary = const Color(0xFF1E88E5);
    await NepaliPickerAppearance.apply();
    NepaliPickerAppearance.primary = const Color(0xFFD32F2F);
    await NepaliPickerAppearance.apply();

    expect(host.appearances.map((sent) => sent.primaryArgb),
        <int>[0xFF1E88E5, 0xFFD32F2F]);
  });
}
