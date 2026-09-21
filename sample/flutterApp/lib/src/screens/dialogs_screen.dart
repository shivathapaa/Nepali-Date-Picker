// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// Modal and full-screen hosts around the same picker.
class DialogsScreen extends StatefulWidget {
  const DialogsScreen({super.key});

  @override
  State<DialogsScreen> createState() => _DialogsScreenState();
}

class _DialogsScreenState extends State<DialogsScreen> {
  static const _preselected = SimpleDate(2081, 3, 5);

  String _confirmed = 'none';

  Future<void> _record(Future<NepaliDate?> dialog) async {
    final picked = await dialog;
    if (!mounted) return;
    setState(
        () => _confirmed = picked == null ? 'dismissed' : calendarText(picked));
  }

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Dialogs',
        children: [
          DemoSection(
            title: 'Dialog',
            subtitle: 'The picker in a modal dialog; the future completes on '
                'confirm or dismiss.',
            child: Center(
              child: FilledButton(
                onPressed: () => _record(showNepaliDatePickerDialog(
                  initialSelectedDate: _preselected,
                )),
                child: const Text('Open dialog'),
              ),
            ),
          ),
          DemoSection(
            title: 'Full-screen dialog',
            subtitle: 'The same picker filling the screen, with a title.',
            child: Center(
              child: FilledButton.tonal(
                onPressed: () => _record(showNepaliDatePickerFullScreenDialog(
                  initialSelectedDate: _preselected,
                  title: 'Pick a date',
                )),
                child: const Text('Open full-screen dialog'),
              ),
            ),
          ),
          DemoSection(
            title: 'Localized dialog',
            subtitle: 'Nepali locale with the buttons in its own words.',
            child: Center(
              child: FilledButton.tonal(
                onPressed: () => _record(showNepaliDatePickerDialog(
                  initialSelectedDate: _preselected,
                  locale: SampleDefaults.nepali,
                  confirmText: 'ठिक छ',
                  dismissText: 'रद्द',
                )),
                child: const Text('Open Nepali dialog'),
              ),
            ),
          ),
          DemoSection(
            title: 'Switchable dialog',
            subtitle: 'Dual dates, the system toggle and neighbouring month '
                'days, all inside the dialog.',
            child: Center(
              child: FilledButton.tonal(
                onPressed: () => _record(showNepaliDatePickerDialog(
                  initialSelectedDate: SampleDefaults.tallMonth,
                  showEnglishDate: true,
                  showCalendarSystemToggle: true,
                  showAdjacentMonthDays: true,
                )),
                child: const Text('Open switchable dialog'),
              ),
            ),
          ),
          DemoSection(
            title: 'What came back',
            subtitle: 'Every button above writes into the same readout.',
            child: LabeledValue('Confirmed', _confirmed),
          ),
        ],
      );
}
