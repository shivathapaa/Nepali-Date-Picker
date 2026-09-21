// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// The index: a Today readout, then the same four sections in the same order
// as the Compose and SwiftUI showcases, Pickers, Fields, Events, Engine.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import 'screens/calendar_screen.dart';
import 'screens/calendar_switch_screen.dart';
import 'screens/chrome_screen.dart';
import 'screens/composing_rules_screen.dart';
import 'screens/customization_screen.dart';
import 'screens/dialogs_screen.dart';
import 'screens/engine_queries_screen.dart';
import 'screens/event_queries_screen.dart';
import 'screens/events_screen.dart';
import 'screens/field_states_screen.dart';
import 'screens/pickers_screen.dart';
import 'screens/selectable_dates_screen.dart';
import 'screens/text_fields_screen.dart';
import 'screens/utilities_screen.dart';
import 'screens/wheel_docked_screen.dart';
import 'support/appearance_menu.dart';
import 'support/demo_section.dart';
import 'support/sample_defaults.dart';
import 'support/sample_palettes.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({
    super.key,
    required this.palette,
    required this.brightness,
    required this.dark,
    required this.onPaletteSelected,
    required this.onBrightnessSelected,
  });

  final SamplePalette palette;
  final NepaliPickerBrightness brightness;
  final bool dark;
  final ValueChanged<SamplePalette> onPaletteSelected;
  final ValueChanged<NepaliPickerBrightness> onBrightnessSelected;

  @override
  Widget build(BuildContext context) {
    final sections = <(String, List<(String, Widget Function())>)>[
      (
        'Pickers',
        [
          ('Pickers', PickersScreen.new),
          ('Calendar switch', CalendarSwitchScreen.new),
          ('Wheel & Docked', WheelDockedScreen.new),
          ('Dialogs', DialogsScreen.new),
          ('Chrome and dimensions', ChromeScreen.new),
          ('Customization', CustomizationScreen.new),
        ]
      ),
      (
        'Fields',
        [
          ('Text fields', TextFieldsScreen.new),
          ('Field states', FieldStatesScreen.new),
          ('Selectable dates', SelectableDatesScreen.new),
          ('Composing rules', ComposingRulesScreen.new),
        ]
      ),
      (
        'Calendar',
        [
          ('Month calendar', CalendarScreen.new),
        ]
      ),
      (
        'Events',
        [
          ('Events', EventsScreen.new),
          ('Event queries', EventQueriesScreen.new),
        ]
      ),
      (
        'Engine',
        [
          ('Utilities', UtilitiesScreen.new),
          ('Engine queries', EngineQueriesScreen.new),
        ]
      ),
    ];

    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Nepali Date Picker'),
        actions: [
          AppearanceMenu(
            brightness: brightness,
            palette: palette,
            dark: dark,
            onBrightnessSelected: onBrightnessSelected,
            onPaletteSelected: onPaletteSelected,
          ),
        ],
      ),
      body: ListView(
        children: [
          const _TodayReadout(),
          for (final (header, rows) in sections) ...[
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 20, 16, 4),
              child: Text(
                header.toUpperCase(),
                style: theme.textTheme.labelMedium
                    ?.copyWith(color: theme.colorScheme.primary),
              ),
            ),
            for (final (title, builder) in rows)
              ListTile(
                title: Text(title),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute<void>(builder: (_) => builder()),
                ),
              ),
          ],
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}

/// Today in both calendars and both languages, like the SwiftUI index head.
class _TodayReadout extends StatefulWidget {
  const _TodayReadout();

  @override
  State<_TodayReadout> createState() => _TodayReadoutState();
}

class _TodayReadoutState extends State<_TodayReadout> {
  String _bikramSambat = '…';
  String _nepali = '…';
  String _gregorian = '…';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final today = await NepaliDateConverter.todayBs();
      final english = await NepaliDateConverter.formatBsDate(today,
          locale: SampleDefaults.englishText);
      final nepali = await NepaliDateConverter.formatBsDate(today,
          locale: SampleDefaults.nepaliText);
      final gregorian = await NepaliDateConverter.todayAd();
      if (!mounted) return;
      setState(() {
        _bikramSambat = english;
        _nepali = nepali;
        _gregorian = dateText(gregorian.toSimpleDate());
      });
    } on Exception {
      // Without a platform host, as in a widget test, the readout stays on
      // its placeholders.
    }
  }

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(12, 12, 12, 0),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 10),
            child: Column(
              children: [
                LabeledValue('Bikram Sambat', _bikramSambat),
                LabeledValue('नेपाली', _nepali),
                LabeledValue('Gregorian', _gregorian),
              ],
            ),
          ),
        ),
      );
}
