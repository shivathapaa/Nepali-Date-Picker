// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// Wheel dimensions, docked formats, a narrowed year range, a chrome-less
/// range calendar and the dialog's own surface.
class ChromeScreen extends StatefulWidget {
  const ChromeScreen({super.key});

  @override
  State<ChromeScreen> createState() => _ChromeScreenState();
}

class _ChromeScreenState extends State<ChromeScreen> {
  static const _seed = SimpleDate(2081, 6, 10);

  static const _formatStyles = <(NepaliDateFormatStyle, String)>[
    (NepaliDateFormatStyle.full, 'Full'),
    (NepaliDateFormatStyle.long, 'Long'),
    (NepaliDateFormatStyle.medium, 'Medium'),
    (NepaliDateFormatStyle.shortYmd, 'Short Y-M-D'),
    (NepaliDateFormatStyle.shortMdy, 'Short M-D-Y'),
    (NepaliDateFormatStyle.compactYmd, 'Compact Y-M-D'),
    (NepaliDateFormatStyle.compactMdy, 'Compact M-D-Y'),
  ];

  NepaliDateFormatStyle _dockedStyle = NepaliDateFormatStyle.medium;
  final Map<NepaliDateFormatStyle, String> _styleSamples = {};
  YearRange? _narrowYears;
  YearRange? _narrowGregorian;
  String _dialogSurface = 'none';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final seedCalendar = await NepaliDateConverter.bsCalendar(
        _seed.year, _seed.month, _seed.dayOfMonth);
    for (final (style, _) in _formatStyles) {
      _styleSamples[style] = await NepaliDateConverter.formatBsDate(
        seedCalendar,
        locale: NepaliDateLocale(dateFormat: style),
      );
    }
    final today = await NepaliDateConverter.todayBs();
    final years = YearRange(today.year - 2, today.year + 2);
    final gregorian = await NepaliDateConverter.adYearRangeForBsYears(
        years.first, years.last);
    if (!mounted) return;
    setState(() {
      _narrowYears = years;
      _narrowGregorian = gregorian;
    });
  }

  @override
  Widget build(BuildContext context) {
    final years = _narrowYears;
    return DemoScreen(
      title: 'Chrome and dimensions',
      children: [
        DemoSection(
          title: 'How big a wheel is',
          subtitle: 'A compact wheel against a roomy Nepali one; row height '
              'and visible rows are the wheel\'s own dials.',
          child: Column(children: [
            NepaliWheelDatePicker(
              initialDate: _seed,
              itemHeight: 32,
              visibleItemCount: 3,
              onDateChange: (_) {},
            ),
            const SizedBox(height: 12),
            NepaliWheelDatePicker(
              initialDate: _seed,
              locale: SampleDefaults.nepali,
              itemHeight: 56,
              visibleItemCount: 7,
              onDateChange: (_) {},
            ),
          ]),
        ),
        DemoSection(
          title: 'What a docked field writes',
          subtitle: 'The same selection through every format style.',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: DropdownButton<NepaliDateFormatStyle>(
                  value: _dockedStyle,
                  isExpanded: true,
                  onChanged: (style) {
                    if (style != null) setState(() => _dockedStyle = style);
                  },
                  items: [
                    for (final (style, label) in _formatStyles)
                      DropdownMenuItem(value: style, child: Text(label)),
                  ],
                ),
              ),
              NepaliDatePickerDocked(
                key: ValueKey('docked-${_dockedStyle.name}'),
                label: 'Date',
                initialSelectedDate: _seed,
                dateFormatStyle: _dockedStyle,
                onDateSelected: (_) {},
              ),
              const SizedBox(height: 8),
              for (final (style, label) in _formatStyles)
                LabeledValue(label, _styleSamples[style] ?? '…'),
            ],
          ),
        ),
        DemoSection(
          title: 'A narrowed year range',
          subtitle: 'Two years either side of today; the Gregorian menu '
              'covers the same span of real days.',
          child: Column(children: [
            if (years != null)
              NepaliDatePicker(
                key: ValueKey('narrow-${years.first}'),
                yearRange: years,
                onDateSelected: (_) {},
              ),
            LabeledValue('Offered years',
                years == null ? '…' : '${years.first}..${years.last}'),
            LabeledValue(
                'Gregorian equivalent',
                _narrowGregorian == null
                    ? '…'
                    : '${_narrowGregorian!.first}..${_narrowGregorian!.last}'),
          ]),
        ),
        DemoSection(
          title: 'A range calendar with its chrome off',
          subtitle: 'No mode toggle, no today button, no year picker or '
              'month navigation; just the grid.',
          child: NepaliDateRangePicker(
            initialSelectedStartDate: _seed,
            initialSelectedEndDate: const SimpleDate(2081, 6, 20),
            showModeToggle: false,
            showTodayButton: false,
            showYearPickerAndMonthNavigation: false,
            onRangeSelected: (_, _) {},
          ),
        ),
        DemoSection(
          title: 'A range calendar showing both calendars',
          subtitle: 'Nepali locale on the range, an English locale for the '
              'paired Gregorian dates.',
          child: NepaliDateRangePicker(
            locale: SampleDefaults.nepali,
            showEnglishDate: true,
            englishDateLocale: SampleDefaults.englishRange,
            onRangeSelected: (_, _) {},
          ),
        ),
        DemoSection(
          title: "The dialog's own surface",
          subtitle: 'A square, flat dialog against a rounded, raised one.',
          child: Column(children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                FilledButton.tonal(
                  onPressed: () => _openDialog(cornerRadius: 2, elevation: 0),
                  child: const Text('Square and flat'),
                ),
                FilledButton.tonal(
                  onPressed: () => _openDialog(cornerRadius: 36, elevation: 16),
                  child: const Text('Rounded and raised'),
                ),
              ],
            ),
            const SizedBox(height: 8),
            LabeledValue('Confirmed', _dialogSurface),
          ]),
        ),
      ],
    );
  }

  Future<void> _openDialog(
      {required double cornerRadius, required double elevation}) async {
    final picked = await showNepaliDatePickerDialog(
      initialSelectedDate: _seed,
      cornerRadius: cornerRadius,
      tonalElevation: elevation,
    );
    if (!mounted) return;
    setState(() =>
        _dialogSurface = picked == null ? 'dismissed' : calendarText(picked));
  }
}
