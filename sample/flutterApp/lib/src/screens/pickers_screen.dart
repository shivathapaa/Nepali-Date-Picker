// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// Single, localized, dual-date and range grids, matching the sibling
/// showcases' Pickers screen section for section.
class PickersScreen extends StatefulWidget {
  const PickersScreen({super.key});

  @override
  State<PickersScreen> createState() => _PickersScreenState();
}

class _PickersScreenState extends State<PickersScreen> {
  String _defaultPick = 'none';
  String _preselectedPick = 'none';
  String _dualPick = 'none';
  String _dualGregorian = 'none';
  String _nepaliPick = 'none';
  String _rangeStart = 'none';
  String _rangeEnd = 'none';
  String _dualRangeStart = 'none';
  String _dualRangeEnd = 'none';

  Future<void> _showGregorian(NepaliDate? date) async {
    if (date == null) return;
    final gregorian = await NepaliDateConverter.convertBsToAd(
        date.year, date.month, date.dayOfMonth);
    if (!mounted) return;
    setState(() => _dualGregorian = calendarText(gregorian));
  }

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Pickers',
        children: [
          DemoSection(
            title: 'Default picker',
            subtitle: 'No initial selection. The mode toggle switches between '
                'calendar and typed input.',
            child: Column(children: [
              NepaliDatePicker(
                onDateSelected: (date) =>
                    setState(() => _defaultPick = calendarText(date)),
              ),
              SelectionSummary(_defaultPick),
            ]),
          ),
          DemoSection(
            title: 'Pre-selected date',
            subtitle: 'Opens on Baisakh 15, 2081 with that date already '
                'chosen and its month displayed.',
            child: Column(children: [
              NepaliDatePicker(
                initialSelectedDate: SampleDefaults.preselectedDate,
                onDateSelected: (date) =>
                    setState(() => _preselectedPick = calendarText(date)),
              ),
              SelectionSummary(_preselectedPick),
            ]),
          ),
          DemoSection(
            title: 'With the Gregorian date',
            subtitle: 'Every day carries its Gregorian equivalent, and the '
                'headline shows both calendars.',
            child: Column(children: [
              NepaliDatePicker(
                initialSelectedDate: SampleDefaults.preselectedDate,
                showEnglishDate: true,
                onDateSelected: (date) {
                  setState(() => _dualPick = calendarText(date));
                  _showGregorian(date);
                },
              ),
              SelectionSummary(_dualPick),
              LabeledValue('Gregorian', _dualGregorian),
            ]),
          ),
          DemoSection(
            title: 'Nepali language',
            subtitle: 'The same picker, localized: Nepali month and weekday '
                'names with Devanagari digits.',
            child: Column(children: [
              NepaliDatePicker(
                locale: SampleDefaults.nepali,
                onDateSelected: (date) =>
                    setState(() => _nepaliPick = calendarText(date)),
              ),
              SelectionSummary(_nepaliPick),
            ]),
          ),
          DemoSection(
            title: 'Range picker',
            subtitle: 'A ten-day range pre-selected, months stacked '
                'vertically the Material way.',
            child: Column(children: [
              NepaliDateRangePicker(
                initialSelectedStartDate: SampleDefaults.preselectedDate,
                initialSelectedEndDate: const SimpleDate(2081, 1, 25),
                locale: SampleDefaults.englishRange,
                onRangeSelected: (start, end) => setState(() {
                  _rangeStart = calendarText(start);
                  _rangeEnd = calendarText(end);
                }),
              ),
              LabeledValue('Start', _rangeStart),
              LabeledValue('End', _rangeEnd),
            ]),
          ),
          DemoSection(
            title: 'Range with Gregorian dates',
            subtitle: 'Horizontal paging, and both calendars on every day.',
            child: Column(children: [
              NepaliDateRangePicker(
                showMonthsVertically: false,
                showEnglishDate: true,
                onRangeSelected: (start, end) => setState(() {
                  _dualRangeStart = calendarText(start);
                  _dualRangeEnd = calendarText(end);
                }),
              ),
              LabeledValue('Start', _dualRangeStart),
              LabeledValue('End', _dualRangeEnd),
            ]),
          ),
        ],
      );
}
