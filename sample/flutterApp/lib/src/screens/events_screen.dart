// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';
import '../support/sample_event_data.dart';

/// Marking days on every surface: weeks, closures, style switches, brand
/// colours and the app's own dots.
class EventsScreen extends StatefulWidget {
  const EventsScreen({super.key});

  @override
  State<EventsScreen> createState() => _EventsScreenState();
}

class _EventsScreenState extends State<EventsScreen> {
  SampleEventData? _data;
  bool _markWeeklyOff = true;
  bool _markEvents = true;
  bool _tintContainer = false;
  bool _indicateWeeklyOff = false;
  bool _describeEvents = true;

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
        title: 'Events',
        children: const [Center(child: CircularProgressIndicator())],
      );
    }

    final marks = [for (final event in data.national) event.asPickerEvent()];
    final officeWeek = NepaliPickerEventOptions(events: marks);
    final schoolWeek = NepaliPickerEventOptions(
      weeklyOffDays: const [SampleDefaults.saturday, SampleDefaults.sunday],
      events: marks,
    );
    final switched = NepaliPickerEventOptions(
      events: marks,
      markWeeklyOff: _markWeeklyOff,
      markEvents: _markEvents,
      tintContainer: _tintContainer,
      indicateWeeklyOff: _indicateWeeklyOff,
      describeEvents: _describeEvents,
    );
    final branded = NepaliPickerEventOptions(
      events: marks,
      tintContainer: true,
      weeklyOffColor: const Color(0xFF8E24AA),
      publicHolidayColor: const Color(0xFFD32F2F),
      religiousColor: const Color(0xFF1E88E5),
      regionalColor: const Color(0xFF00897B),
      observanceColor: const Color(0xFF6D4C41),
      markedContainerColor: const Color(0x22D32F2F),
    );
    final dotted = NepaliPickerEventOptions(
      events: [
        ...marks,
        for (final (index, event) in data.ownEvents.indexed)
          event.asPickerEvent(
            color: SampleEventData
                .dotColors[index % SampleEventData.dotColors.length],
            indicate: true,
          ),
      ],
    );

    return DemoScreen(
      title: 'Events',
      children: [
        DemoSection(
          title: 'An office week',
          subtitle: 'Saturday off, the named days and the festival span '
              'coloured by kind.',
          child: NepaliDatePicker(events: officeWeek, onDateSelected: (_) {}),
        ),
        DemoSection(
          title: 'A school week',
          subtitle: 'The same days over a Saturday and Sunday weekend.',
          child: NepaliDatePicker(events: schoolWeek, onDateSelected: (_) {}),
        ),
        DemoSection(
          title: 'What the marking may draw',
          subtitle: 'Every display switch live; the key rebuilds the picker '
              'per combination.',
          child: Column(children: [
            SwitchListTile(
              title: const Text('Colour the weekly off day'),
              value: _markWeeklyOff,
              onChanged: (value) => setState(() => _markWeeklyOff = value),
            ),
            SwitchListTile(
              title: const Text('Colour the events'),
              value: _markEvents,
              onChanged: (value) => setState(() => _markEvents = value),
            ),
            SwitchListTile(
              title: const Text('Tint the day container'),
              value: _tintContainer,
              onChanged: (value) => setState(() => _tintContainer = value),
            ),
            SwitchListTile(
              title: const Text('Dot the weekly off day'),
              value: _indicateWeeklyOff,
              onChanged: (value) => setState(() => _indicateWeeklyOff = value),
            ),
            SwitchListTile(
              title: const Text('Describe events for accessibility'),
              value: _describeEvents,
              onChanged: (value) => setState(() => _describeEvents = value),
            ),
            NepaliDatePicker(
              key: ValueKey('marks-$_markWeeklyOff-$_markEvents-'
                  '$_tintContainer-$_indicateWeeklyOff-$_describeEvents'),
              events: switched,
              onDateSelected: (_) {},
            ),
          ]),
        ),
        DemoSection(
          title: 'Colours of your own',
          subtitle: 'Every palette slot overridden and the container tinted.',
          child: NepaliDatePicker(events: branded, onDateSelected: (_) {}),
        ),
        DemoSection(
          title: "Dots are the app's own events",
          subtitle: 'Closed days keep their colour; the dots mean something '
              'is scheduled, several to a day.',
          child: NepaliDatePicker(events: dotted, onDateSelected: (_) {}),
        ),
        DemoSection(
          title: 'The docked field',
          subtitle: 'The same marking inside the docked popup.',
          child: NepaliDatePickerDocked(
            label: 'Pick a day',
            events: officeWeek,
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'The range calendar',
          subtitle: 'Horizontal paging with the same marking.',
          child: NepaliDateRangePicker(
            showMonthsVertically: false,
            events: officeWeek,
            onRangeSelected: (_, _) {},
          ),
        ),
        DemoSection(
          title: "The text field's own calendar",
          subtitle: 'The filled field opens a marked calendar.',
          child: NepaliDateField(
            outlined: false,
            label: 'Date',
            events: officeWeek,
            onValueChanged: (_) {},
          ),
        ),
      ],
    );
  }
}
