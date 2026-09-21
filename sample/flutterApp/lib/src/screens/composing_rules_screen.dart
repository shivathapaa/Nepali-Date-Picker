// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';
import '../support/sample_event_data.dart';

/// Building one selection rule out of a window, a week and holiday lists,
/// and counting with the same composition.
///
/// The sibling showcases also merge and filter providers as objects; here
/// events cross the bridge as data, so the merging and filtering happen on
/// the Dart lists before the policy is built, to the same effect.
class ComposingRulesScreen extends StatefulWidget {
  const ComposingRulesScreen({super.key});

  @override
  State<ComposingRulesScreen> createState() => _ComposingRulesScreenState();
}

class _ComposingRulesScreenState extends State<ComposingRulesScreen> {
  SampleEventData? _data;
  SimpleDate? _windowEnd;
  List<String> _mergedNames = const [];
  String _officeDays = '…';
  String _schoolDays = '…';
  String _nextWorking = '…';
  String _plusTen = '…';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final data = await SampleEventData.load();
    final today = data.today;
    final windowEnd = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 21))
        .toSimpleDate();

    final officePolicy = NepaliCalendarPolicy(
      weeklyOffDays: const {SampleDefaults.saturday},
      events: data.national,
    );
    final schoolPolicy = NepaliCalendarPolicy(
      weeklyOffDays: const {SampleDefaults.saturday, SampleDefaults.sunday},
      events: [...data.national, ...data.schoolEvents],
    );

    final merged = <String>{
      for (final event
          in await schoolPolicy.eventsIn(today.year, today.month))
        event.name,
    }.toList();

    final officeDays = await officePolicy.workingDaysBetween(today, windowEnd);
    final schoolDays = await schoolPolicy.workingDaysBetween(today, windowEnd);
    final nextWorking = await schoolPolicy.nextWorkingDay(today);
    final plusTen = await schoolPolicy.addWorkingDays(today, 10);

    if (!mounted) return;
    setState(() {
      _data = data;
      _windowEnd = windowEnd;
      _mergedNames = merged;
      _officeDays = '$officeDays';
      _schoolDays = '$schoolDays';
      _nextWorking = calendarText(nextWorking);
      _plusTen = calendarText(plusTen);
    });
  }

  @override
  Widget build(BuildContext context) {
    final data = _data;
    final windowEnd = _windowEnd;
    if (data == null || windowEnd == null) {
      return DemoScreen(
        title: 'Composing rules',
        children: const [Center(child: CircularProgressIndicator())],
      );
    }
    final today = data.today;
    final officePolicy = NepaliCalendarPolicy(
      weeklyOffDays: const {SampleDefaults.saturday},
      events: data.national,
    );
    final closuresOnly = NepaliCalendarPolicy(
      weeklyOffDays: const <int>{},
      events: [
        for (final event in data.national)
          if (event.kind == NepaliEventKind.governmentPublic) event,
      ],
    );
    final schoolPolicy = NepaliCalendarPolicy(
      weeklyOffDays: const {SampleDefaults.saturday, SampleDefaults.sunday},
      events: [...data.national, ...data.schoolEvents],
    );

    return DemoScreen(
      title: 'Composing rules',
      children: [
        DemoSection(
          title: 'Start from a window',
          subtitle: 'Three weeks from today, nothing else refused yet.',
          child: NepaliDatePicker(
            selectableDates: NepaliSelectableDates.range(today, windowEnd,
                includeMinDate: true, includeMaxDate: true),
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'Take the week out',
          subtitle: 'The same window, Saturdays refused.',
          child: NepaliDatePicker(
            selectableDates: NepaliSelectableDates(
              minDate: today,
              maxDate: windowEnd,
              includeMinDate: true,
              includeMaxDate: true,
              excludeWeekend: const {SampleDefaults.saturday},
            ),
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'Take the holidays out too',
          subtitle: 'The office policy closes the named days and the '
              'festival span on top of the week.',
          child: NepaliDatePicker(
            selectableDates: NepaliSelectableDates(
              minDate: today,
              maxDate: windowEnd,
              includeMinDate: true,
              includeMaxDate: true,
              excludeWeekend: const {SampleDefaults.saturday},
              excludeClosuresOf: officePolicy,
            ),
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'Only the kinds this screen cares about',
          subtitle: 'The event list filtered to government closures before '
              'the policy is built, the data-side spelling of a filtered '
              'provider.',
          child: NepaliDatePicker(
            selectableDates: NepaliSelectableDates(
              minDate: today,
              maxDate: windowEnd,
              includeMinDate: true,
              includeMaxDate: true,
              excludeClosuresOf: closuresOnly,
            ),
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'Two lists, one rule',
          subtitle: 'National and school events concatenated into one '
              'policy, weekends Saturday and Sunday.',
          child: Column(children: [
            NepaliDatePicker(
              selectableDates: NepaliSelectableDates(
                excludeClosuresOf: schoolPolicy,
                excludeWeekend: const {
                  SampleDefaults.saturday,
                  SampleDefaults.sunday,
                },
              ),
              onDateSelected: (_) {},
            ),
            for (final name in _mergedNames) LabeledValue('Named', name),
          ]),
        ),
        DemoSection(
          title: 'The same composition, counting',
          subtitle: 'Working days over the next three weeks under each '
              'policy, and the arithmetic that follows.',
          child: Column(children: [
            LabeledValue('Office working days', _officeDays),
            LabeledValue('School working days', _schoolDays),
            LabeledValue('Next school working day', _nextWorking),
            LabeledValue('Ten school days out', _plusTen),
          ]),
        ),
      ],
    );
  }
}
