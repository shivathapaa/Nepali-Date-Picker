// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';
import '../support/sample_event_card.dart';
import '../support/sample_event_data.dart';

/// The browsable calendar: a grid that fills the width, the day it is showing
/// written out, and the month listed under it, all from one policy.
class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});

  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> {
  SampleEventData? _data;
  String _picked = 'nothing yet';
  NepaliEvent? _tapped;

  @override
  void initState() {
    super.initState();
    SampleEventData.load().then((data) {
      if (mounted) setState(() => _data = data);
    });
  }

  @override
  Widget build(BuildContext context) {
    final data = _data;
    if (data == null) {
      return DemoScreen(
        title: 'Calendar',
        children: const [Center(child: CircularProgressIndicator())],
      );
    }

    final officeWeek = data.markingOptions();
    final schoolWeek = data.markingOptions(
      weeklyOffDays: const [SampleDefaults.saturday, SampleDefaults.sunday],
    );

    return DemoScreen(
      title: 'Calendar',
      children: [
        DemoSection(
          title: 'A calendar and what is on it',
          subtitle: 'The day written out and the month listed under the grid, '
              'drawn natively so all three read one policy.',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              NepaliCalendar(
                events: officeWeek,
                showDaySummary: true,
                showMonthEvents: true,
                onDaySelected: (date) => setState(() =>
                    _picked = '${date.year}-${date.month}-${date.dayOfMonth}'),
                onEventTapped: (event) => setState(() => _tapped = event),
              ),
              Text('Picked: $_picked'),
              if (_tapped != null) SampleEventCard(event: _tapped!),
            ],
          ),
        ),
        DemoSection(
          title: 'What an event can carry',
          subtitle: 'The bridge hands an event back with its id and payload '
              'untouched. The card above is this sample reading its own JSON '
              'out of that payload: a colour, an icon, tags, a venue and image '
              'links it could fetch.',
          child: Text(_tapped?.payload ?? 'Tap a line of the list above.'),
        ),
        DemoSection(
          title: 'The grid alone',
          subtitle: 'Without the stacked lists the calendar is a fixed-height '
              'block, which is what a Dart-rendered agenda sits beside.',
          child: NepaliCalendar(events: officeWeek),
        ),
        DemoSection(
          title: 'One calendar at a time',
          subtitle: 'Turning the second number off leaves one date per cell, '
              'and the switch turns the whole grid Gregorian.',
          child: NepaliCalendar(
            events: officeWeek,
            showSecondaryDates: false,
            showCalendarSystemToggle: true,
          ),
        ),
        DemoSection(
          title: "A school's week",
          subtitle: 'The same calendar under an institution closed Saturday '
              'and Sunday, with its own days named.',
          child: NepaliCalendar(
            events: schoolWeek,
            showDaySummary: true,
          ),
        ),
      ],
    );
  }
}
