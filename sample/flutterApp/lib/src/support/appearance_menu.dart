// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// The app-bar appearance control: brightness and palette, in the same order
// and with the same labels and swatches as the Compose and SwiftUI menus.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import 'sample_palettes.dart';

class AppearanceMenu extends StatelessWidget {
  const AppearanceMenu({
    super.key,
    required this.brightness,
    required this.palette,
    required this.dark,
    required this.onBrightnessSelected,
    required this.onPaletteSelected,
  });

  final NepaliPickerBrightness brightness;
  final SamplePalette palette;

  /// The resolved mode, so palette swatches show the colour they would apply.
  final bool dark;

  final ValueChanged<NepaliPickerBrightness> onBrightnessSelected;
  final ValueChanged<SamplePalette> onPaletteSelected;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return PopupMenuButton<Object>(
      icon: const Icon(Icons.palette_outlined),
      tooltip: 'Appearance',
      onSelected: (choice) {
        if (choice is NepaliPickerBrightness) onBrightnessSelected(choice);
        if (choice is SamplePalette) onPaletteSelected(choice);
      },
      itemBuilder: (context) => [
        _sectionLabel(theme, 'BRIGHTNESS'),
        for (final choice in NepaliPickerBrightness.values)
          _choice(
            value: choice,
            label: switch (choice) {
              NepaliPickerBrightness.system => 'System',
              NepaliPickerBrightness.light => 'Light',
              NepaliPickerBrightness.dark => 'Dark',
            },
            selected: choice == brightness,
          ),
        const PopupMenuDivider(),
        _sectionLabel(theme, 'PALETTE'),
        for (final choice in SamplePalette.values)
          _choice(
            value: choice,
            label: choice.label,
            selected: choice == palette,
            swatch: Color(choice.roles(dark: dark).primary),
            swatchBorder: theme.colorScheme.outlineVariant,
          ),
      ],
    );
  }

  PopupMenuEntry<Object> _sectionLabel(ThemeData theme, String text) =>
      PopupMenuItem<Object>(
        enabled: false,
        height: 32,
        child: Text(text, style: theme.textTheme.labelSmall),
      );

  PopupMenuEntry<Object> _choice({
    required Object value,
    required String label,
    required bool selected,
    Color? swatch,
    Color? swatchBorder,
  }) =>
      PopupMenuItem<Object>(
        value: value,
        child: Row(
          children: [
            if (swatch != null) ...[
              Container(
                width: 18,
                height: 18,
                decoration: BoxDecoration(
                  color: swatch,
                  shape: BoxShape.circle,
                  border: Border.all(color: swatchBorder!, width: 1),
                ),
              ),
              const SizedBox(width: 12),
            ],
            Expanded(child: Text(label)),
            if (selected) const Icon(Icons.check, size: 18),
            if (!selected) const SizedBox(width: 18),
          ],
        ),
      );
}
