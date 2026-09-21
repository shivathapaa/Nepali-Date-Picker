// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// The shipped selectable-date rules: after, before and a bounded window.
///
/// The sibling showcases also write a custom rule in their own language;
/// arbitrary predicates cannot cross the bridge, so here the weekend
/// exclusion stands in as the composed example.
class SelectableDatesScreen extends StatefulWidget {
  const SelectableDatesScreen({super.key});

  @override
  State<SelectableDatesScreen> createState() => _SelectableDatesScreenState();
}

class _SelectableDatesScreenState extends State<SelectableDatesScreen> {
  SimpleDate? _today;
  SimpleDate? _windowEnd;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final today = (await NepaliDateConverter.todayBs()).toSimpleDate();
    final end = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 30))
        .toSimpleDate();
    if (!mounted) return;
    setState(() {
      _today = today;
      _windowEnd = end;
    });
  }

  @override
  Widget build(BuildContext context) {
    final today = _today;
    final windowEnd = _windowEnd;
    if (today == null || windowEnd == null) {
      return DemoScreen(
        title: 'Selectable dates',
        children: const [Center(child: CircularProgressIndicator())],
      );
    }
    return DemoScreen(
      title: 'Selectable dates',
      children: [
        DemoSection(
          title: 'Future dates only',
          subtitle: 'Days strictly after today are selectable.',
          child: NepaliDatePicker(
            selectableDates: NepaliSelectableDates.after(today),
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'Past dates only',
          subtitle: 'Today and everything before it.',
          child: NepaliDatePicker(
            selectableDates:
                NepaliSelectableDates.before(today, includeDate: true),
            onDateSelected: (_) {},
          ),
        ),
        DemoSection(
          title: 'A bounded window',
          subtitle: 'Thirty days from today, both endpoints included.',
          child: Column(children: [
            NepaliDatePicker(
              selectableDates: NepaliSelectableDates.range(
                today,
                windowEnd,
                includeMinDate: true,
                includeMaxDate: true,
              ),
              onDateSelected: (_) {},
            ),
            LabeledValue(
                'Window', '${dateText(today)} .. ${dateText(windowEnd)}'),
          ]),
        ),
        DemoSection(
          title: 'A window without its Saturdays',
          subtitle: 'The same window with the weekly off day refused; rules '
              'compose instead of crossing the bridge as code.',
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
      ],
    );
  }
}
