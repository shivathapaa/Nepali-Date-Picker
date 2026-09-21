// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// A compact tour of the plugin: the conversion engine, the embedded
// Material3 picker and the native dialog. The full multi-page showcase
// lives in the repository at sample/flutterApp.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

void main() {
  runApp(const NepaliDatePickerExampleApp());
}

class NepaliDatePickerExampleApp extends StatelessWidget {
  const NepaliDatePickerExampleApp({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'Nepali Date Picker KMP',
        theme: ThemeData(colorSchemeSeed: const Color(0xFF006E1C)),
        home: const _Home(),
      );
}

class _Home extends StatefulWidget {
  const _Home();

  @override
  State<_Home> createState() => _HomeState();
}

class _HomeState extends State<_Home> {
  String _today = 'loading';
  String _selected = 'nothing yet';
  String _day = 'nothing yet';
  String _dialog = 'nothing yet';

  @override
  void initState() {
    super.initState();
    _loadToday();
  }

  Future<void> _loadToday() async {
    final today = await NepaliDateConverter.todayBs();
    final formatted = await NepaliDateConverter.formatBsDate(
      today,
      locale: const NepaliDateLocale(
        language: NepaliLanguage.nepali,
        dateFormat: NepaliDateFormatStyle.full,
      ),
    );
    final ad = await NepaliDateConverter.convertBsToAd(
        today.year, today.month, today.dayOfMonth);
    if (!mounted) return;
    setState(() =>
        _today = '$formatted  (AD ${ad.year}-${ad.month}-${ad.dayOfMonth})');
  }

  Future<void> _openDialog() async {
    final picked = await showNepaliDatePickerDialog();
    if (!mounted) return;
    setState(() => _dialog = picked == null
        ? 'dismissed'
        : '${picked.year}-${picked.month}-${picked.dayOfMonth}');
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Nepali Date Picker KMP')),
        body: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            ListTile(title: const Text('Today'), subtitle: Text(_today)),
            const SizedBox(height: 8),
            NepaliDatePicker(
              onDateSelected: (date) => setState(() => _selected = date == null
                  ? 'cleared'
                  : '${date.year}-${date.month}-${date.dayOfMonth}'),
            ),
            Text('Selected: $_selected'),
            const SizedBox(height: 16),
            const Text('A browsable calendar, events and all'),
            NepaliCalendar(
              showDaySummary: true,
              showMonthEvents: true,
              onDaySelected: (date) => setState(
                () => _day = '${date.year}-${date.month}-${date.dayOfMonth}',
              ),
            ),
            Text('Day: $_day'),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: _openDialog,
              child: const Text('Open the native dialog'),
            ),
            Text('Dialog: $_dialog'),
          ],
        ),
      );
}
