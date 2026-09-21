// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// The Bikram Sambat and Gregorian switch on every surface that carries it.
class CalendarSwitchScreen extends StatefulWidget {
  const CalendarSwitchScreen({super.key});

  @override
  State<CalendarSwitchScreen> createState() => _CalendarSwitchScreenState();
}

class _CalendarSwitchScreenState extends State<CalendarSwitchScreen> {
  String _switchable = 'none';
  String _gregorianFirst = 'none';
  String _both = 'none';
  String _adjacent = 'none';
  String _wheel = 'none';
  String _docked = 'none';
  String _field = 'none';
  String _rangeStart = 'none';
  String _rangeEnd = 'none';
  CalendarSystem _external = CalendarSystem.bikramSambat;

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Calendar switch',
        children: [
          DemoSection(
            title: 'With the B.S. / A.D. switch',
            subtitle: 'The toggle above the grid swaps the calendar system '
                'in place.',
            child: Column(children: [
              NepaliDatePicker(
                initialSelectedDate: SampleDefaults.switchSeed,
                showCalendarSystemToggle: true,
                onDateSelected: (date) =>
                    setState(() => _switchable = calendarText(date)),
              ),
              SelectionSummary(_switchable),
            ]),
          ),
          DemoSection(
            title: 'Gregorian-first, no switch',
            subtitle: 'Opens on the Gregorian calendar. Days before '
                'AD 1913-04-13 have no Bikram Sambat equivalent.',
            child: Column(children: [
              NepaliDatePicker(
                initialSelectedDate: SampleDefaults.switchSeed,
                initialCalendarSystem: CalendarSystem.gregorian,
                onDateSelected: (date) =>
                    setState(() => _gregorianFirst = calendarText(date)),
              ),
              SelectionSummary(_gregorianFirst),
            ]),
          ),
          DemoSection(
            title: 'Both calendars, switchable',
            subtitle: 'Dual dates in the grid and the system toggle together.',
            child: Column(children: [
              NepaliDatePicker(
                initialSelectedDate: SampleDefaults.switchSeed,
                showEnglishDate: true,
                showCalendarSystemToggle: true,
                onDateSelected: (date) =>
                    setState(() => _both = calendarText(date)),
              ),
              SelectionSummary(_both),
            ]),
          ),
          DemoSection(
            title: 'Neighbouring months in the empty cells',
            subtitle: 'Shrawan 2083 needs six rows; the leading and trailing '
                'cells carry the neighbouring months.',
            child: Column(children: [
              NepaliDatePicker(
                initialSelectedDate: SampleDefaults.tallMonth,
                showCalendarSystemToggle: true,
                showAdjacentMonthDays: true,
                onDateSelected: (date) =>
                    setState(() => _adjacent = calendarText(date)),
              ),
              SelectionSummary(_adjacent),
            ]),
          ),
          DemoSection(
            title: 'Switch outside the picker',
            subtitle: 'The stand-alone toggle drives a picker rebuilt for '
                'the chosen system.',
            child: Column(children: [
              NepaliCalendarSystemToggle(
                initialCalendarSystem: _external,
                onCalendarSystemChanged: (system) =>
                    setState(() => _external = system),
              ),
              NepaliDatePicker(
                key: ValueKey('external-${_external.name}'),
                initialCalendarSystem: _external,
                onDateSelected: (_) {},
              ),
            ]),
          ),
          DemoSection(
            title: 'Wheel, switchable',
            subtitle: 'The wheel spins either calendar.',
            child: Column(children: [
              NepaliWheelDatePicker(
                showCalendarSystemToggle: true,
                onDateChange: (date) =>
                    setState(() => _wheel = calendarText(date)),
              ),
              SelectionSummary(_wheel),
            ]),
          ),
          DemoSection(
            title: 'Docked, Gregorian-first',
            subtitle: 'The compact field opens its popup on the Gregorian '
                'calendar with the switch available.',
            child: Column(children: [
              NepaliDatePickerDocked(
                label: 'Date (A.D.)',
                initialCalendarSystem: CalendarSystem.gregorian,
                showCalendarSystemToggle: true,
                showAdjacentMonthDays: true,
                onDateSelected: (date) =>
                    setState(() => _docked = calendarText(date)),
              ),
              SelectionSummary(_docked),
            ]),
          ),
          DemoSection(
            title: 'Field typed in Gregorian',
            subtitle: 'The filled field takes Gregorian input; its own '
                'calendar carries the switch.',
            child: Column(children: [
              NepaliDateField(
                outlined: false,
                label: 'Date (A.D.)',
                initialCalendarSystem: CalendarSystem.gregorian,
                showCalendarSystemToggle: true,
                showAdjacentMonthDays: true,
                onValueChanged: (date) => setState(
                    () => _field = date == null ? 'none' : dateText(date)),
              ),
              LabeledValue('Parsed', _field),
            ]),
          ),
          DemoSection(
            title: 'Range picker, switchable',
            subtitle: 'A pre-selected week, horizontal paging, and the '
                'system toggle.',
            child: Column(children: [
              NepaliDateRangePicker(
                initialSelectedStartDate: const SimpleDate(2083, 4, 28),
                initialSelectedEndDate: const SimpleDate(2083, 5, 4),
                showMonthsVertically: false,
                showCalendarSystemToggle: true,
                showAdjacentMonthDays: true,
                onRangeSelected: (start, end) => setState(() {
                  _rangeStart = calendarText(start);
                  _rangeEnd = calendarText(end);
                }),
              ),
              LabeledValue('Start', _rangeStart),
              LabeledValue('End', _rangeEnd),
            ]),
          ),
        ],
      );
}
