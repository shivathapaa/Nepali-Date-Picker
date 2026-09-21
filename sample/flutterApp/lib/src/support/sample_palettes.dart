// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// The colour roles behind each sample palette, in the same values the Compose
// and SwiftUI showcases use, so the three render the same calendar.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

/// The Material roles the native appearance proxy takes, one set per mode.
class PaletteRoles {
  const PaletteRoles({
    required this.primary,
    required this.onPrimary,
    required this.primaryContainer,
    required this.onPrimaryContainer,
    required this.secondaryContainer,
    required this.onSecondaryContainer,
    required this.surface,
    required this.onSurface,
    required this.surfaceVariant,
    required this.onSurfaceVariant,
    required this.outline,
  });

  final int primary;
  final int onPrimary;
  final int primaryContainer;
  final int onPrimaryContainer;
  final int secondaryContainer;
  final int onSecondaryContainer;
  final int surface;
  final int onSurface;
  final int surfaceVariant;
  final int onSurfaceVariant;
  final int outline;
}

/// A palette the showcase can switch to, named by the hue it is built around.
enum SamplePalette {
  standard('Default'),
  green('Green'),
  blue('Blue'),
  orange('Orange'),
  red('Red'),
  yellow('Yellow');

  const SamplePalette(this.label);

  final String label;

  PaletteRoles roles({required bool dark}) => switch (this) {
        SamplePalette.standard => dark ? _standardDark : _standardLight,
        SamplePalette.green => dark ? _greenDark : _greenLight,
        SamplePalette.blue => dark ? _blueDark : _blueLight,
        SamplePalette.orange => dark ? _orangeDark : _orangeLight,
        SamplePalette.red => dark ? _redDark : _redLight,
        SamplePalette.yellow => dark ? _yellowDark : _yellowLight,
      };
}

const _standardLight = PaletteRoles(
  primary: 0xFF6750A4, onPrimary: 0xFFFFFFFF,
  primaryContainer: 0xFFEADDFF, onPrimaryContainer: 0xFF21005D,
  secondaryContainer: 0xFFE8DEF8, onSecondaryContainer: 0xFF1D192B,
  surface: 0xFFFEF7FF, onSurface: 0xFF1D1B20,
  surfaceVariant: 0xFFE7E0EC, onSurfaceVariant: 0xFF49454F,
  outline: 0xFF79747E,
);

const _standardDark = PaletteRoles(
  primary: 0xFFD0BCFF, onPrimary: 0xFF381E72,
  primaryContainer: 0xFF4F378B, onPrimaryContainer: 0xFFEADDFF,
  secondaryContainer: 0xFF4A4458, onSecondaryContainer: 0xFFE8DEF8,
  surface: 0xFF141218, onSurface: 0xFFE6E0E9,
  surfaceVariant: 0xFF49454F, onSurfaceVariant: 0xFFCAC4D0,
  outline: 0xFF938F99,
);

const _greenLight = PaletteRoles(
  primary: 0xFF4C662B, onPrimary: 0xFFFFFFFF,
  primaryContainer: 0xFFCDEDA3, onPrimaryContainer: 0xFF354E16,
  secondaryContainer: 0xFFDCE7C8, onSecondaryContainer: 0xFF404A33,
  surface: 0xFFF9FAEF, onSurface: 0xFF1A1C16,
  surfaceVariant: 0xFFE1E4D5, onSurfaceVariant: 0xFF44483D,
  outline: 0xFF75796C,
);

const _greenDark = PaletteRoles(
  primary: 0xFFB1D18A, onPrimary: 0xFF1F3701,
  primaryContainer: 0xFF354E16, onPrimaryContainer: 0xFFCDEDA3,
  secondaryContainer: 0xFF404A33, onSecondaryContainer: 0xFFDCE7C8,
  surface: 0xFF12140E, onSurface: 0xFFE2E3D8,
  surfaceVariant: 0xFF44483D, onSurfaceVariant: 0xFFC5C8BA,
  outline: 0xFF8F9285,
);

const _blueLight = PaletteRoles(
  primary: 0xFF415F91, onPrimary: 0xFFFFFFFF,
  primaryContainer: 0xFFD6E3FF, onPrimaryContainer: 0xFF284777,
  secondaryContainer: 0xFFDAE2F9, onSecondaryContainer: 0xFF3E4759,
  surface: 0xFFF9F9FF, onSurface: 0xFF191C20,
  surfaceVariant: 0xFFE0E2EC, onSurfaceVariant: 0xFF44474E,
  outline: 0xFF74777F,
);

const _blueDark = PaletteRoles(
  primary: 0xFFAAC7FF, onPrimary: 0xFF0A305F,
  primaryContainer: 0xFF284777, onPrimaryContainer: 0xFFD6E3FF,
  secondaryContainer: 0xFF3E4759, onSecondaryContainer: 0xFFDAE2F9,
  surface: 0xFF111318, onSurface: 0xFFE2E2E9,
  surfaceVariant: 0xFF44474E, onSurfaceVariant: 0xFFC4C6D0,
  outline: 0xFF8E9099,
);

const _orangeLight = PaletteRoles(
  primary: 0xFF8B5000, onPrimary: 0xFFFFFFFF,
  primaryContainer: 0xFFFFDCC2, onPrimaryContainer: 0xFF6A3B00,
  secondaryContainer: 0xFFFFDCC2, onSecondaryContainer: 0xFF5A422D,
  surface: 0xFFFFF8F5, onSurface: 0xFF221A14,
  surfaceVariant: 0xFFF3DFD1, onSurfaceVariant: 0xFF51453A,
  outline: 0xFF837468,
);

const _orangeDark = PaletteRoles(
  primary: 0xFFFFB870, onPrimary: 0xFF4A2800,
  primaryContainer: 0xFF6A3B00, onPrimaryContainer: 0xFFFFDCC2,
  secondaryContainer: 0xFF5A422D, onSecondaryContainer: 0xFFFFDCC2,
  surface: 0xFF1A120C, onSurface: 0xFFF0DFD4,
  surfaceVariant: 0xFF51453A, onSurfaceVariant: 0xFFD6C3B5,
  outline: 0xFF9E8E81,
);

const _redLight = PaletteRoles(
  primary: 0xFF904A43, onPrimary: 0xFFFFFFFF,
  primaryContainer: 0xFFFFDAD6, onPrimaryContainer: 0xFF73342D,
  secondaryContainer: 0xFFFFDAD6, onSecondaryContainer: 0xFF5D3F3B,
  surface: 0xFFFFF8F7, onSurface: 0xFF231919,
  surfaceVariant: 0xFFF5DDDA, onSurfaceVariant: 0xFF534341,
  outline: 0xFF857371,
);

const _redDark = PaletteRoles(
  primary: 0xFFFFB4AB, onPrimary: 0xFF561E19,
  primaryContainer: 0xFF73342D, onPrimaryContainer: 0xFFFFDAD6,
  secondaryContainer: 0xFF5D3F3B, onSecondaryContainer: 0xFFFFDAD6,
  surface: 0xFF1A1110, onSurface: 0xFFF1DEDC,
  surfaceVariant: 0xFF534341, onSurfaceVariant: 0xFFD8C2BE,
  outline: 0xFFA08C8A,
);

const _yellowLight = PaletteRoles(
  primary: 0xFF6D5E0F, onPrimary: 0xFFFFFFFF,
  primaryContainer: 0xFFF8E287, onPrimaryContainer: 0xFF534600,
  secondaryContainer: 0xFFEEE2BC, onSecondaryContainer: 0xFF4E472A,
  surface: 0xFFFFF9EE, onSurface: 0xFF1E1B13,
  surfaceVariant: 0xFFEAE2D0, onSurfaceVariant: 0xFF4B4739,
  outline: 0xFF7C7767,
);

const _yellowDark = PaletteRoles(
  primary: 0xFFDBC66E, onPrimary: 0xFF3A3000,
  primaryContainer: 0xFF534600, onPrimaryContainer: 0xFFF8E287,
  secondaryContainer: 0xFF4E472A, onSecondaryContainer: 0xFFEEE2BC,
  surface: 0xFF15130B, onSurface: 0xFFE9E2D0,
  surfaceVariant: 0xFF4B4739, onSurfaceVariant: 0xFFCDC6B4,
  outline: 0xFF969080,
);

/// The Flutter scheme for one palette and mode, derived exactly the way the
/// Compose sample derives its container tones, so the Flutter chrome around
/// the native pickers sits on the same ramp as the calendar inside them.
ColorScheme colorSchemeFor(SamplePalette palette, {required bool dark}) {
  final roles = palette.roles(dark: dark);
  Color color(int argb) => Color(argb);
  final surface = color(roles.surface);
  final surfaceVariant = color(roles.surfaceVariant);
  final base = ColorScheme.fromSeed(
    seedColor: color(roles.primary),
    brightness: dark ? Brightness.dark : Brightness.light,
  );
  return base.copyWith(
    primary: color(roles.primary),
    onPrimary: color(roles.onPrimary),
    primaryContainer: color(roles.primaryContainer),
    onPrimaryContainer: color(roles.onPrimaryContainer),
    secondaryContainer: color(roles.secondaryContainer),
    onSecondaryContainer: color(roles.onSecondaryContainer),
    surface: surface,
    onSurface: color(roles.onSurface),
    surfaceContainerHighest: surfaceVariant,
    onSurfaceVariant: color(roles.onSurfaceVariant),
    surfaceTint: color(roles.primary),
    outline: color(roles.outline),
    surfaceContainerLowest:
        dark ? Color.lerp(surface, Colors.black, 0.3)! : Colors.white,
    surfaceContainerLow:
        Color.lerp(surface, surfaceVariant, dark ? 0.17 : 0.25)!,
    surfaceContainer: Color.lerp(surface, surfaceVariant, dark ? 0.25 : 0.45)!,
    surfaceContainerHigh:
        Color.lerp(surface, surfaceVariant, dark ? 0.46 : 0.65)!,
    surfaceBright:
        dark ? Color.lerp(surface, surfaceVariant, 0.75)! : surface,
    surfaceDim: dark ? surface : Color.lerp(surface, surfaceVariant, 0.85)!,
  );
}

/// Pushes the palette into the shared native appearance proxy, the same
/// eleven slots the SwiftUI sample writes, so every live picker repaints.
Future<void> applyPaletteToPickers(
  SamplePalette palette, {
  required NepaliPickerBrightness brightness,
  required bool systemDark,
}) {
  final dark = switch (brightness) {
    NepaliPickerBrightness.system => systemDark,
    NepaliPickerBrightness.light => false,
    NepaliPickerBrightness.dark => true,
  };
  final roles = palette.roles(dark: dark);
  NepaliPickerAppearance.brightness = brightness;
  NepaliPickerAppearance.primary = Color(roles.primary);
  NepaliPickerAppearance.onPrimary = Color(roles.onPrimary);
  NepaliPickerAppearance.primaryContainer = Color(roles.primaryContainer);
  NepaliPickerAppearance.onPrimaryContainer = Color(roles.onPrimaryContainer);
  NepaliPickerAppearance.secondaryContainer = Color(roles.secondaryContainer);
  NepaliPickerAppearance.onSecondaryContainer =
      Color(roles.onSecondaryContainer);
  NepaliPickerAppearance.surface = Color(roles.surface);
  NepaliPickerAppearance.onSurface = Color(roles.onSurface);
  NepaliPickerAppearance.surfaceVariant = Color(roles.surfaceVariant);
  NepaliPickerAppearance.onSurfaceVariant = Color(roles.onSurfaceVariant);
  NepaliPickerAppearance.outline = Color(roles.outline);
  return NepaliPickerAppearance.apply();
}
