// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// Month metadata, cross-calendar month walks, the supported boundaries,
/// live pattern formatting, and a Flutter-rendered month grid built purely
/// from engine data.
class EngineQueriesScreen extends StatefulWidget {
  const EngineQueriesScreen({super.key});

  @override
  State<EngineQueriesScreen> createState() => _EngineQueriesScreenState();
}

class _EngineQueriesScreenState extends State<EngineQueriesScreen> {
  SimpleDate? _today;
  NepaliMonthInfo? _month;
  List<NepaliDate> _monthInGregorian = const [];
  String _opensOn = '…';
  String _gregorianMonths = '…';
  YearRange? _bsYears;
  YearRange? _adYears;
  String _anchor = '…';
  final Map<String, String> _convertible = {};
  String _patternText = 'EEEE, dd MMMM yyyy';
  String _patternBsEn = '…';
  String _patternBsNp = '…';
  String _patternAdEn = '…';
  String _timePatternText = 'hh:mm a';
  String _timeEn = '…';
  String _timeNp = '…';

  @override
  void initState() {
    super.initState();
    _start();
  }

  Future<void> _start() async {
    final today = (await NepaliDateConverter.todayBs()).toSimpleDate();
    _today = today;
    await _loadMonth(today.year, today.month);
    await _loadBoundaries();
    await _reformatPatterns();
    await _reformatTimes();
  }

  Future<void> _loadMonth(int year, int month) async {
    final info = await NepaliDateConverter.bsMonth(year, month);
    final gregorian =
        await NepaliDateConverter.adCalendarsInBsMonth(year, month);
    final opensOn = await NepaliDateConverter.weekdayName(info.firstDayOfMonth);
    final crossed = {for (final day in gregorian) day.month}.length;
    if (!mounted) return;
    setState(() {
      _month = info;
      _monthInGregorian = gregorian;
      _opensOn = opensOn;
      _gregorianMonths = '$crossed';
    });
  }

  Future<void> _stepMonth(int direction) async {
    final info = _month;
    if (info == null) return;
    // The engine has no month stepper on the wire, so the walk goes through
    // day arithmetic: one past the end forward, one before the first back.
    final target = direction > 0
        ? await NepaliDateConverter.addDaysToBsDate(
            info.year, info.month, 1, info.totalDaysInMonth)
        : await NepaliDateConverter.addDaysToBsDate(info.year, info.month, 1, -1);
    await _loadMonth(target.year, target.month);
  }

  Future<void> _loadBoundaries() async {
    final bsYears = await NepaliDateConverter.bsYearRange();
    final adYears = await NepaliDateConverter.adYearRange();
    final anchor = await NepaliDateConverter.convertBsToAd(bsYears.first, 1, 1);
    for (final (year, month, day) in const [
      (1913, 4, 12),
      (1913, 4, 13),
      (2024, 1, 1),
      (2043, 12, 31),
    ]) {
      _convertible['$year-$month-$day'] =
          '${await NepaliDateConverter.isAdDateConvertible(year, month, day)}';
    }
    if (!mounted) return;
    setState(() {
      _bsYears = bsYears;
      _adYears = adYears;
      _anchor = calendarText(anchor);
    });
  }

  Future<void> _reformatPatterns() async {
    final today = _today;
    if (today == null) return;
    String bsEn;
    String bsNp;
    String adEn;
    try {
      bsEn = await NepaliDateConverter.formatBsDateByPattern(
          _patternText, today,
          language: NepaliLanguage.english);
      bsNp = await NepaliDateConverter.formatBsDateByPattern(
          _patternText, today);
      final ad = await NepaliDateConverter.convertBsToAd(
          today.year, today.month, today.dayOfMonth);
      adEn = await NepaliDateConverter.formatAdDateByPattern(
          _patternText, ad.toSimpleDate());
    } on Exception {
      bsEn = bsNp = adEn = 'pattern rejected';
    }
    if (!mounted) return;
    setState(() {
      _patternBsEn = bsEn;
      _patternBsNp = bsNp;
      _patternAdEn = adEn;
    });
  }

  Future<void> _reformatTimes() async {
    const sample = SimpleTime(hour: 16, minute: 30, second: 15);
    String english;
    String nepali;
    try {
      english = await NepaliDateConverter.formatTimeByPattern(
          _timePatternText, sample);
      nepali = await NepaliDateConverter.formatTimeByPattern(
          _timePatternText, sample,
          language: NepaliLanguage.nepali);
    } on Exception {
      english = nepali = 'pattern rejected';
    }
    if (!mounted) return;
    setState(() {
      _timeEn = english;
      _timeNp = nepali;
    });
  }

  @override
  Widget build(BuildContext context) {
    final month = _month;
    return DemoScreen(
      title: 'Engine queries',
      children: [
        DemoSection(
          title: 'A Bikram Sambat month',
          subtitle: 'Step through months; every readout is one engine call.',
          child: Column(children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                OutlinedButton(
                    onPressed: () => _stepMonth(-1),
                    child: const Text('Previous')),
                OutlinedButton(
                    onPressed: () {
                      final today = _today;
                      if (today != null) {
                        _loadMonth(today.year, today.month);
                      }
                    },
                    child: const Text('Today')),
                OutlinedButton(
                    onPressed: () => _stepMonth(1), child: const Text('Next')),
              ],
            ),
            LabeledValue(
                'Month', month == null ? '…' : '${month.year}-${month.month}'),
            LabeledValue('Days in month',
                month == null ? '…' : '${month.totalDaysInMonth}'),
            LabeledValue('Opens on', _opensOn),
          ]),
        ),
        DemoSection(
          title: 'The month as a grid',
          subtitle: 'Rendered entirely with Flutter widgets from the month '
              'metadata; the small figures are the Gregorian days.',
          child: month == null
              ? const Center(child: CircularProgressIndicator())
              : _MonthGrid(info: month, gregorian: _monthInGregorian),
        ),
        DemoSection(
          title: 'Day by day in Gregorian',
          subtitle: 'The whole month converts in one pass.',
          child: Column(children: [
            for (final day in _monthInGregorian.take(4))
              LabeledValue(
                  'Day ${_monthInGregorian.indexOf(day) + 1}',
                  calendarText(day)),
            if (_monthInGregorian.isNotEmpty)
              LabeledValue('Last day', calendarText(_monthInGregorian.last)),
            LabeledValue('Gregorian months crossed', _gregorianMonths),
          ]),
        ),
        DemoSection(
          title: 'Where the calendar stops',
          subtitle: 'The supported years and the anchor where the two '
              'calendars first meet.',
          child: Column(children: [
            LabeledValue(
                'Bikram Sambat years',
                _bsYears == null
                    ? '…'
                    : '${_bsYears!.first}..${_bsYears!.last}'),
            LabeledValue(
                'Gregorian years',
                _adYears == null
                    ? '…'
                    : '${_adYears!.first}..${_adYears!.last}'),
            LabeledValue('BS 1970-1-1 in AD', _anchor),
          ]),
        ),
        DemoSection(
          title: 'The convertible window is narrower',
          subtitle: 'Early 1913 sits inside the year range yet has no '
              'Bikram Sambat equivalent.',
          child: Column(children: [
            for (final entry in _convertible.entries)
              LabeledValue(entry.key, entry.value),
          ]),
        ),
        DemoSection(
          title: 'A pattern of your own',
          subtitle: 'Type a Unicode pattern; today renders through it in '
              'both calendars.',
          child: Column(children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: TextField(
                controller: TextEditingController(text: _patternText),
                decoration: const InputDecoration(labelText: 'Date pattern'),
                onSubmitted: (value) {
                  _patternText = value;
                  _reformatPatterns();
                },
              ),
            ),
            LabeledValue('BS, English', _patternBsEn),
            LabeledValue('BS, Nepali', _patternBsNp),
            LabeledValue('AD, English', _patternAdEn),
          ]),
        ),
        DemoSection(
          title: 'A pattern for a time',
          subtitle: 'The same idea for the clock, at 16:30:15.',
          child: Column(children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: TextField(
                controller: TextEditingController(text: _timePatternText),
                decoration: const InputDecoration(labelText: 'Time pattern'),
                onSubmitted: (value) {
                  _timePatternText = value;
                  _reformatTimes();
                },
              ),
            ),
            LabeledValue('English', _timeEn),
            LabeledValue('Nepali', _timeNp),
          ]),
        ),
      ],
    );
  }
}

class _MonthGrid extends StatelessWidget {
  const _MonthGrid({required this.info, required this.gregorian});

  final NepaliMonthInfo info;
  final List<NepaliDate> gregorian;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: GridView.count(
        crossAxisCount: 7,
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        children: [
          for (var cell = 0;
              cell < info.daysFromStartOfWeekToFirstOfMonth;
              cell++)
            const SizedBox.shrink(),
          for (var day = 1; day <= info.totalDaysInMonth; day++)
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  '$day',
                  style: TextStyle(
                    fontWeight: FontWeight.w600,
                    color: (info.daysFromStartOfWeekToFirstOfMonth + day) % 7 ==
                            0
                        ? colors.error
                        : colors.onSurface,
                  ),
                ),
                if (day <= gregorian.length)
                  Text(
                    '${gregorian[day - 1].dayOfMonth}',
                    style: TextStyle(
                        fontSize: 10, color: colors.onSurfaceVariant),
                  ),
              ],
            ),
        ],
      ),
    );
  }
}
