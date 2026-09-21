// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// Error, disabled, read-only and localized field states, and every wire
/// pattern formatting and parsing on its own.
class FieldStatesScreen extends StatefulWidget {
  const FieldStatesScreen({super.key});

  @override
  State<FieldStatesScreen> createState() => _FieldStatesScreenState();
}

class _FieldStatesScreenState extends State<FieldStatesScreen> {
  static const _patterns = <(DatePattern, String)>[
    (DatePattern.yyyySlashMmSlashDd, 'YYYY/MM/DD'),
    (DatePattern.yyyyDashMmDashDd, 'YYYY-MM-DD'),
    (DatePattern.ddSlashMmSlashYyyy, 'DD/MM/YYYY'),
    (DatePattern.ddDashMmDashYyyy, 'DD-MM-YYYY'),
  ];

  SimpleDate? _today;
  bool _touched = false;
  bool _deliveryEmpty = true;
  String _formatted = '…';
  String _devanagari = '…';
  String _parsedBack = '…';
  String _misParsed = '…';
  String _rangeStart = 'none';
  String _rangeEnd = 'none';
  bool _endBeforeStart = false;

  bool get _showError => _touched && _deliveryEmpty;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final today = (await NepaliDateConverter.todayBs()).toSimpleDate();
    const seed = SimpleDate(2081, 6, 12);
    final formatted = await NepaliDateFormatter.format(seed,
        pattern: DatePattern.yyyySlashMmSlashDd);
    final devanagari = await NepaliDateFormatter.format(seed,
        pattern: DatePattern.ddDashMmDashYyyy, script: DigitScript.devanagari);
    final parsedBack = await NepaliDateFormatter.parse(formatted,
        pattern: DatePattern.yyyySlashMmSlashDd);
    final misParsed = await NepaliDateFormatter.parse('2081/06/12',
        pattern: DatePattern.ddSlashMmSlashYyyy);
    if (!mounted) return;
    setState(() {
      _today = today;
      _formatted = formatted;
      _devanagari = devanagari;
      _parsedBack = parsedBack == null ? 'null' : dateText(parsedBack);
      _misParsed = misParsed == null ? 'null' : dateText(misParsed);
    });
  }

  @override
  Widget build(BuildContext context) {
    final today = _today;
    return DemoScreen(
      title: 'Field states',
      children: [
        DemoSection(
          title: 'Error and supporting text',
          subtitle: 'Only dates from today onwards parse; an incomplete '
              'entry flips the field into its error state.',
          child: Column(children: [
            if (today != null)
              NepaliDateField(
                label: 'Delivery date',
                selectableDates:
                    NepaliSelectableDates.after(today, includeDate: true),
                supportingText: _showError
                    ? 'Enter a complete date from today onwards'
                    : 'Format: YYYY/MM/DD',
                isError: _showError,
                onValueChanged: (date) => setState(() {
                  _touched = true;
                  _deliveryEmpty = date == null;
                }),
              ),
          ]),
        ),
        DemoSection(
          title: 'Every input pattern',
          subtitle: 'The four wire shapes as field masks, then the formatter '
              'on its own, Devanagari included.',
          child: Column(children: [
            for (final (pattern, label) in _patterns)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: NepaliDateField(
                  label: label,
                  dateFormat: pattern,
                  onValueChanged: (_) {},
                ),
              ),
            LabeledValue('format 2081-06-12', _formatted),
            LabeledValue('devanagari, day first', _devanagari),
            LabeledValue('parsed back', _parsedBack),
            LabeledValue('read with the wrong pattern', _misParsed),
          ]),
        ),
        DemoSection(
          title: 'Disabled',
          subtitle: 'A locked value the user can see but not touch.',
          child: const NepaliDateField(
            enabled: false,
            initialValue: SimpleDate(2081, 2, 8),
            label: 'Locked date',
            supportingText: 'Unlocked once the form above is complete',
            onValueChanged: _ignore,
          ),
        ),
        DemoSection(
          title: 'Read only',
          subtitle: 'Focusable and copyable, never editable.',
          child: NepaliDateField(
            readOnly: true,
            initialValue: today,
            label: 'Booked for',
            onValueChanged: (_) {},
          ),
        ),
        DemoSection(
          title: 'The filled style and its own words',
          subtitle: 'Nepali labels, placeholder and dialog buttons on the '
              'filled variant.',
          child: NepaliDateField(
            outlined: false,
            locale: SampleDefaults.nepali,
            label: 'मिति',
            placeholder: 'वर्ष/महिना/गते',
            confirmButtonText: 'ठिक छ',
            dismissButtonText: 'रद्द',
            onValueChanged: (_) {},
          ),
        ),
        DemoSection(
          title: 'A range pair in every state',
          subtitle: 'The end flags itself when it falls before the start; '
              'both sides accept Devanagari digits.',
          child: Column(children: [
            NepaliDateRangeField(
              outlined: false,
              startLabel: 'Leave from',
              endLabel: 'Leave until',
              supportingText: _endBeforeStart
                  ? 'The end cannot fall before the start'
                  : 'Both sides accept Devanagari digits',
              isEndError: _endBeforeStart,
              onRangeChanged: (start, end) => setState(() {
                _rangeStart = start == null ? 'none' : dateText(start);
                _rangeEnd = end == null ? 'none' : dateText(end);
                _endBeforeStart =
                    start != null && end != null && end.compareTo(start) < 0;
              }),
            ),
            LabeledValue('Start', _rangeStart),
            LabeledValue('End', _rangeEnd),
          ]),
        ),
      ],
    );
  }
}

void _ignore(SimpleDate? _) {}
