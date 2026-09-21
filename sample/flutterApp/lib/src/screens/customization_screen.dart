// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';

/// One NepaliDateLocale composed from its four axes, plus the picker chrome
/// toggles, driving a live picker.
class CustomizationScreen extends StatefulWidget {
  const CustomizationScreen({super.key});

  @override
  State<CustomizationScreen> createState() => _CustomizationScreenState();
}

class _CustomizationScreenState extends State<CustomizationScreen> {
  NepaliLanguage _language = NepaliLanguage.english;
  NepaliDateFormatStyle _dateFormat = NepaliDateFormatStyle.long;
  NameFormat _weekDayName = NameFormat.full;
  NameFormat _monthName = NameFormat.full;
  DigitScript? _digitScript;
  bool _showModeToggle = true;
  bool _showTodayButton = true;
  String _today = '…';

  NepaliDateLocale get _locale => NepaliDateLocale(
        language: _language,
        dateFormat: _dateFormat,
        weekDayName: _weekDayName,
        monthName: _monthName,
        digitScript: _digitScript,
      );

  @override
  void initState() {
    super.initState();
    _reformatToday();
  }

  Future<void> _reformatToday() async {
    final today = await NepaliDateConverter.todayBs();
    final formatted =
        await NepaliDateConverter.formatBsDate(today, locale: _locale);
    if (!mounted) return;
    setState(() => _today = formatted);
  }

  void _update(VoidCallback change) {
    setState(change);
    _reformatToday();
  }

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Customization',
        children: [
          DemoSection(
            title: 'Locale',
            subtitle: 'Language, name lengths and the digit script, the four '
                'axes one NepaliDateLocale carries.',
            child: Column(children: [
              _segmented<NepaliLanguage>(
                label: 'Language',
                value: _language,
                options: const [
                  (NepaliLanguage.english, 'English'),
                  (NepaliLanguage.nepali, 'Nepali'),
                ],
                onChanged: (value) => _update(() => _language = value),
              ),
              _segmented<NameFormat>(
                label: 'Weekdays',
                value: _weekDayName,
                options: const [
                  (NameFormat.full, 'Sunday'),
                  (NameFormat.medium, 'Sun'),
                  (NameFormat.short, 'S'),
                ],
                onChanged: (value) => _update(() => _weekDayName = value),
              ),
              _segmented<NameFormat>(
                label: 'Months',
                value: _monthName,
                options: const [
                  (NameFormat.full, 'Full'),
                  (NameFormat.medium, 'Medium'),
                  (NameFormat.short, 'Short'),
                ],
                onChanged: (value) => _update(() => _monthName = value),
              ),
              _segmented<DigitScript?>(
                label: 'Digits',
                value: _digitScript,
                options: const [
                  (null, 'Locale default'),
                  (DigitScript.latin, 'Latin'),
                  (DigitScript.devanagari, 'Devanagari'),
                ],
                onChanged: (value) => _update(() => _digitScript = value),
              ),
            ]),
          ),
          DemoSection(
            title: 'Format style',
            subtitle: 'Every preset shape, applied to today.',
            child: Column(children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: DropdownButton<NepaliDateFormatStyle>(
                  value: _dateFormat,
                  isExpanded: true,
                  onChanged: (style) {
                    if (style != null) _update(() => _dateFormat = style);
                  },
                  items: [
                    for (final style in NepaliDateFormatStyle.values)
                      DropdownMenuItem(value: style, child: Text(style.name)),
                  ],
                ),
              ),
              LabeledValue('Today', _today),
            ]),
          ),
          DemoSection(
            title: 'Picker chrome',
            subtitle: 'The header toggles the picker itself carries.',
            child: Column(children: [
              SwitchListTile(
                title: const Text('Show mode toggle'),
                value: _showModeToggle,
                onChanged: (value) => setState(() => _showModeToggle = value),
              ),
              SwitchListTile(
                title: const Text('Show today button'),
                value: _showTodayButton,
                onChanged: (value) => setState(() => _showTodayButton = value),
              ),
            ]),
          ),
          DemoSection(
            title: 'Live picker',
            subtitle: 'Rebuilt for every choice above; the key is the whole '
                'configuration.',
            child: NepaliDatePicker(
              key: ValueKey('$_language-$_dateFormat-$_weekDayName-'
                  '$_monthName-$_digitScript-$_showModeToggle-'
                  '$_showTodayButton'),
              locale: _locale,
              showModeToggle: _showModeToggle,
              showTodayButton: _showTodayButton,
              onDateSelected: (_) {},
            ),
          ),
        ],
      );

  Widget _segmented<T>({
    required String label,
    required T value,
    required List<(T, String)> options,
    required ValueChanged<T> onChanged,
  }) =>
      Padding(
        padding: const EdgeInsets.fromLTRB(12, 4, 12, 4),
        child: Row(children: [
          SizedBox(width: 88, child: Text(label)),
          Expanded(
            child: SegmentedButton<T>(
              segments: [
                for (final (option, text) in options)
                  ButtonSegment(value: option, label: Text(text)),
              ],
              selected: {value},
              onSelectionChanged: (selection) => onChanged(selection.first),
            ),
          ),
        ]),
      );
}
