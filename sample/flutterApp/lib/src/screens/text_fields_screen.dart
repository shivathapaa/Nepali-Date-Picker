// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// Typed entry through the outlined, filled and range fields.
class TextFieldsScreen extends StatefulWidget {
  const TextFieldsScreen({super.key});

  @override
  State<TextFieldsScreen> createState() => _TextFieldsScreenState();
}

class _TextFieldsScreenState extends State<TextFieldsScreen> {
  static const _preselected = SimpleDate(2081, 2, 8);

  String _outlined = 'none';
  String _filled = 'none';
  String _dayFirst = 'none';
  String _rangeStart = 'none';
  String _rangeEnd = 'none';

  String _text(SimpleDate? date) => date == null ? 'none' : dateText(date);

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Text fields',
        children: [
          DemoSection(
            title: 'Outlined, pre-filled',
            subtitle: 'The default field shape, opening on Jestha 8, 2081.',
            child: Column(children: [
              NepaliDateField(
                label: 'Date',
                initialValue: _preselected,
                onValueChanged: (date) =>
                    setState(() => _outlined = _text(date)),
              ),
              LabeledValue('Parsed', _outlined),
            ]),
          ),
          DemoSection(
            title: 'Filled and empty',
            subtitle: 'The filled variant opens its own calendar from the '
                'trailing icon.',
            child: Column(children: [
              NepaliDateField(
                outlined: false,
                label: 'Date',
                onValueChanged: (date) => setState(() => _filled = _text(date)),
              ),
              LabeledValue('Parsed', _filled),
            ]),
          ),
          DemoSection(
            title: 'Day first, in Nepali',
            subtitle: 'A DD-MM-YYYY mask with Nepali words and digits.',
            child: Column(children: [
              NepaliDateField(
                label: 'मिति',
                locale: SampleDefaults.nepali,
                dateFormat: DatePattern.ddDashMmDashYyyy,
                onValueChanged: (date) =>
                    setState(() => _dayFirst = _text(date)),
              ),
              LabeledValue('Parsed', _dayFirst),
            ]),
          ),
          DemoSection(
            title: 'A range pair',
            subtitle: 'The start pre-filled and the end waiting; both sides '
                'report as they parse.',
            child: Column(children: [
              NepaliDateRangeField(
                initialStartValue: _preselected,
                onRangeChanged: (start, end) => setState(() {
                  _rangeStart = _text(start);
                  _rangeEnd = _text(end);
                }),
              ),
              LabeledValue('Start', _rangeStart),
              LabeledValue('End', _rangeEnd),
            ]),
          ),
        ],
      );
}
