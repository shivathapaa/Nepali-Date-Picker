// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// The scrolling wheel and the compact docked field.
class WheelDockedScreen extends StatefulWidget {
  const WheelDockedScreen({super.key});

  @override
  State<WheelDockedScreen> createState() => _WheelDockedScreenState();
}

class _WheelDockedScreenState extends State<WheelDockedScreen> {
  static const _preselected = SimpleDate(2081, 6, 10);

  String _wheel = 'none';
  String _nepaliWheel = 'none';
  String _docked = 'none';
  String _compactDocked = 'none';

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Wheel & Docked',
        children: [
          DemoSection(
            title: 'Wheel picker',
            subtitle: 'Starts on today; every spin reports a full calendar.',
            child: Column(children: [
              NepaliWheelDatePicker(
                onDateChange: (date) =>
                    setState(() => _wheel = calendarText(date)),
              ),
              SelectionSummary(_wheel),
            ]),
          ),
          DemoSection(
            title: 'Wheel, pre-selected and localized',
            subtitle: 'Opens on Ashoj 10, 2081 with Nepali names and digits.',
            child: Column(children: [
              NepaliWheelDatePicker(
                initialDate: _preselected,
                locale: SampleDefaults.nepali,
                onDateChange: (date) =>
                    setState(() => _nepaliWheel = calendarText(date)),
              ),
              SelectionSummary(_nepaliWheel),
            ]),
          ),
          DemoSection(
            title: 'Docked picker',
            subtitle: 'A text field with a dropdown calendar under it.',
            child: Column(children: [
              NepaliDatePickerDocked(
                label: 'Date',
                initialSelectedDate: _preselected,
                onDateSelected: (date) =>
                    setState(() => _docked = calendarText(date)),
              ),
              SelectionSummary(_docked),
            ]),
          ),
          DemoSection(
            title: 'Docked with a compact format',
            subtitle: 'The field writes a compact year-first date and drops '
                'the today button.',
            child: Column(children: [
              NepaliDatePickerDocked(
                label: 'Date',
                initialSelectedDate: _preselected,
                dateFormatStyle: NepaliDateFormatStyle.compactYmd,
                showTodayButton: false,
                onDateSelected: (date) =>
                    setState(() => _compactDocked = calendarText(date)),
              ),
              SelectionSummary(_compactDocked),
            ]),
          ),
        ],
      );
}
