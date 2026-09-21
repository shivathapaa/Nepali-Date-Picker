// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'dart:ui';

import 'bridge.dart';
import 'messages.g.dart';

/// Which brightness the native pickers draw at. [system] follows the device
/// setting and switches with it while a picker is on screen; [light] and
/// [dark] pin the pickers regardless of it.
enum NepaliPickerBrightness { system, light, dark }

/// The colours every native picker, field and dialog draws with, shared by
/// the whole app.
///
/// This drives the platform appearance proxies (`NepaliPickerAppearance` on
/// both Android and iOS): assign slots, then [apply]. Pickers already on
/// screen repaint. A null slot keeps Material's own value for that role, so
/// an app can override only its accent and leave the rest alone.
///
/// ```dart
/// NepaliPickerAppearance.brightness = NepaliPickerBrightness.dark;
/// NepaliPickerAppearance.primary = const Color(0xFFB1D18A);
/// await NepaliPickerAppearance.apply();
/// ```
class NepaliPickerAppearance {
  NepaliPickerAppearance._();

  static PickerHostApi get _api => pickerHostApi;

  /// Whether the pickers follow the device setting, or pin light or dark.
  static NepaliPickerBrightness brightness = NepaliPickerBrightness.system;

  /// The accent: selected days, the today ring, the confirm button.
  static Color? primary;

  /// Content drawn on top of [primary], such as a selected day's number.
  static Color? onPrimary;

  /// The filled container behind a selected year and a range's endpoints.
  static Color? primaryContainer;

  /// Content drawn on top of [primaryContainer].
  static Color? onPrimaryContainer;

  /// The softer container behind the days inside a selected range.
  static Color? secondaryContainer;

  /// Content drawn on top of [secondaryContainer].
  static Color? onSecondaryContainer;

  /// The picker's own background; dialog and field surfaces derive from it.
  static Color? surface;

  /// Day numbers, headlines and labels.
  static Color? onSurface;

  /// The raised surface behind fields and the highest container tone.
  static Color? surfaceVariant;

  /// Weekday letters, supporting text and unselected navigation icons.
  static Color? onSurfaceVariant;

  /// Field borders, dividers and the outline of an unselected day.
  static Color? outline;

  /// Pushes the current slots to the native proxies on this platform.
  static Future<void> apply() => _api.applyAppearance(
        AppearanceDto(
          brightness: BrightnessDto.values[brightness.index],
          primaryArgb: primary?.toARGB32() ?? 0,
          onPrimaryArgb: onPrimary?.toARGB32() ?? 0,
          primaryContainerArgb: primaryContainer?.toARGB32() ?? 0,
          onPrimaryContainerArgb: onPrimaryContainer?.toARGB32() ?? 0,
          secondaryContainerArgb: secondaryContainer?.toARGB32() ?? 0,
          onSecondaryContainerArgb: onSecondaryContainer?.toARGB32() ?? 0,
          surfaceArgb: surface?.toARGB32() ?? 0,
          onSurfaceArgb: onSurface?.toARGB32() ?? 0,
          surfaceVariantArgb: surfaceVariant?.toARGB32() ?? 0,
          onSurfaceVariantArgb: onSurfaceVariant?.toARGB32() ?? 0,
          outlineArgb: outline?.toARGB32() ?? 0,
        ),
      );

  /// Restores every role to Material's own value, here and in the native
  /// proxies.
  static Future<void> reset() async {
    brightness = NepaliPickerBrightness.system;
    primary = null;
    onPrimary = null;
    primaryContainer = null;
    onPrimaryContainer = null;
    secondaryContainer = null;
    onSecondaryContainer = null;
    surface = null;
    onSurface = null;
    surfaceVariant = null;
    onSurfaceVariant = null;
    outline = null;
    await _api.resetAppearance();
  }
}
