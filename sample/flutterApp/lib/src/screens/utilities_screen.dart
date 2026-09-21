// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';

/// The converter's whole surface with no picker in sight: conversions,
/// spans, formatting, clocks, ISO, working days, comparisons, digits, names.
class UtilitiesScreen extends StatefulWidget {
  const UtilitiesScreen({super.key});

  @override
  State<UtilitiesScreen> createState() => _UtilitiesScreenState();
}

class _UtilitiesScreenState extends State<UtilitiesScreen> {
  final Map<String, String> _values = {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  void _put(String key, String value) => _values[key] = value;

  Future<void> _load() async {
    final todayBs = await NepaliDateConverter.todayBs();
    final todayAd = await NepaliDateConverter.todayAd();
    final today = todayBs.toSimpleDate();
    final now = await NepaliDateConverter.currentTime();

    _put('today.bs', calendarText(todayBs));
    _put('today.ad', calendarText(todayAd));
    _put('today.time', await NepaliDateConverter.formatTimeEnglish(now,
        use12HourFormat: false));

    final toBs = await NepaliDateConverter.convertAdToBs(2024, 3, 21);
    final toAd = await NepaliDateConverter.convertBsToAd(2081, 1, 1);
    _put('convert.toBs', calendarText(toBs));
    _put('convert.toAd', calendarText(toAd));

    _put(
        'span.bsYear',
        '${await NepaliDateConverter.bsDaysBetween(const SimpleDate(2081, 1, 1), const SimpleDate(2082, 1, 1))} days');
    _put(
        'span.adYear',
        '${await NepaliDateConverter.adDaysBetween(const SimpleDate(2024, 1, 1), const SimpleDate(2025, 1, 1))} days');
    _put('span.bsMonth',
        '${await NepaliDateConverter.totalDaysInBsMonth(2081, 1)} days');
    _put('span.adMonth',
        '${await NepaliDateConverter.totalDaysInAdMonth(2024, 2)} days');

    _put(
        'format.english',
        await NepaliDateConverter.formatBsDate(todayBs,
            locale: SampleDefaults.englishText));
    _put(
        'format.nepali',
        await NepaliDateConverter.formatBsDate(todayBs,
            locale: SampleDefaults.nepaliText));
    _put(
        'format.pattern',
        await NepaliDateConverter.formatBsDateByPattern(
            'EEEE, MMMM d, yyyy', today,
            language: NepaliLanguage.english));

    _put('time.en12', await NepaliDateConverter.formatTimeEnglish(now));
    _put('time.en24', await NepaliDateConverter.formatTimeEnglish(now,
        use12HourFormat: false));
    _put('time.np12', await NepaliDateConverter.formatTimeNepali(now));
    _put('time.np24', await NepaliDateConverter.formatTimeNepali(now,
        use12HourFormat: false));

    final iso = await NepaliDateConverter.bsDateTimeToIso(today, time: now);
    final parsed = await NepaliDateConverter.bsDateTimeFromIso(iso);
    _put('iso.text', iso);
    _put('iso.back', calendarText(parsed.calendar));

    final windowEnd = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 30))
        .toSimpleDate();
    final weekendOnly = NepaliCalendarPolicy();
    final never = NepaliCalendarPolicy(weeklyOffDays: const <int>{});
    final plusTwo = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 2))
        .toSimpleDate();
    final plusThree = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 3))
        .toSimpleDate();
    final withHolidays = NepaliCalendarPolicy(events: [
      NepaliEvent(
          year: plusTwo.year,
          month: plusTwo.month,
          dayOfMonth: plusTwo.dayOfMonth,
          name: 'Closed (demo)',
          kind: NepaliEventKind.governmentPublic),
      NepaliEvent(
          year: plusThree.year,
          month: plusThree.month,
          dayOfMonth: plusThree.dayOfMonth,
          name: 'Closed (demo)',
          kind: NepaliEventKind.governmentPublic),
    ]);
    _put('working.default',
        '${await weekendOnly.workingDaysBetween(today, windowEnd)} of 30');
    _put('working.never',
        '${await never.workingDaysBetween(today, windowEnd)} of 30');
    _put('working.plusFive',
        calendarText(await withHolidays.addWorkingDays(today, 5)));

    final plusTen = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 10))
        .toSimpleDate();
    final compared = await NepaliDateConverter.compareBsDates(today, plusTen);
    _put('compare.result',
        '$compared (${compared < 0 ? 'earlier' : 'not earlier'})');

    _put('digits.devanagari', await NepaliDateConverter.localizeDigits('2081'));
    _put(
        'digits.byLocale',
        await NepaliDateConverter.localizeDigits('1234567890',
            script: DigitScript.devanagari));
    _put('digits.latin', await NepaliDateConverter.toLatinDigits('२०८१'));

    _put('names.bsMonth', await NepaliDateConverter.bsMonthName(1));
    _put(
        'names.bsMonthNp',
        await NepaliDateConverter.bsMonthName(1,
            language: NepaliLanguage.nepali));
    _put(
        'names.adMonth',
        await NepaliDateConverter.adMonthName(3, format: NameFormat.medium));
    _put('names.weekday', await NepaliDateConverter.weekdayName(1));
    _put(
        'names.weekdayNp',
        await NepaliDateConverter.weekdayName(7,
            language: NepaliLanguage.nepali));

    if (mounted) setState(() {});
  }

  String _get(String key) => _values[key] ?? '…';

  @override
  Widget build(BuildContext context) => DemoScreen(
        title: 'Utilities',
        children: [
          DemoSection(
            title: 'Today',
            subtitle: 'Both calendars and the Kathmandu clock.',
            child: Column(children: [
              LabeledValue('Bikram Sambat', _get('today.bs')),
              LabeledValue('Gregorian', _get('today.ad')),
              LabeledValue('Time', _get('today.time')),
            ]),
          ),
          DemoSection(
            title: 'Conversions',
            subtitle: 'A Gregorian day into Bikram Sambat and back the '
                'other way.',
            child: Column(children: [
              LabeledValue('2024-03-21 AD', _get('convert.toBs')),
              LabeledValue('2081-01-01 BS', _get('convert.toAd')),
            ]),
          ),
          DemoSection(
            title: 'Spans and month lengths',
            subtitle: 'Whole years and single months in both calendars.',
            child: Column(children: [
              LabeledValue('BS 2081', _get('span.bsYear')),
              LabeledValue('AD 2024', _get('span.adYear')),
              LabeledValue('Baisakh 2081', _get('span.bsMonth')),
              LabeledValue('February 2024', _get('span.adMonth')),
            ]),
          ),
          DemoSection(
            title: 'Formatting',
            subtitle: 'The preset styles in both languages and a pattern of '
                'your own.',
            child: Column(children: [
              LabeledValue('English', _get('format.english')),
              LabeledValue('Nepali', _get('format.nepali')),
              LabeledValue('EEEE, MMMM d, yyyy', _get('format.pattern')),
            ]),
          ),
          DemoSection(
            title: 'Time formatting',
            subtitle: 'Twelve and twenty-four hours, English and Nepali.',
            child: Column(children: [
              LabeledValue('English, 12h', _get('time.en12')),
              LabeledValue('English, 24h', _get('time.en24')),
              LabeledValue('Nepali, 12h', _get('time.np12')),
              LabeledValue('Nepali, 24h', _get('time.np24')),
            ]),
          ),
          DemoSection(
            title: 'ISO 8601',
            subtitle: 'A Bikram Sambat moment as a UTC timestamp and back.',
            child: Column(children: [
              LabeledValue('Timestamp', _get('iso.text')),
              LabeledValue('Parsed back', _get('iso.back')),
            ]),
          ),
          DemoSection(
            title: 'Working days',
            subtitle: 'Thirty days out under the default weekend, no weekend '
                'at all, and a list of closures.',
            child: Column(children: [
              LabeledValue('Saturday weekend', _get('working.default')),
              LabeledValue('No weekly off', _get('working.never')),
              LabeledValue('+5 with holidays', _get('working.plusFive')),
            ]),
          ),
          DemoSection(
            title: 'Comparisons',
            subtitle: 'Negative means the first date is earlier.',
            child: LabeledValue('today vs today+10', _get('compare.result')),
          ),
          DemoSection(
            title: 'Digit scripts',
            subtitle: 'Latin to Devanagari and back again.',
            child: Column(children: [
              LabeledValue('2081', _get('digits.devanagari')),
              LabeledValue('1234567890', _get('digits.byLocale')),
              LabeledValue('२०८१', _get('digits.latin')),
            ]),
          ),
          DemoSection(
            title: 'Names',
            subtitle: 'Month and weekday names by length and language.',
            child: Column(children: [
              LabeledValue('BS month 1', _get('names.bsMonth')),
              LabeledValue('BS month 1, Nepali', _get('names.bsMonthNp')),
              LabeledValue('AD month 3, medium', _get('names.adMonth')),
              LabeledValue('Weekday 1', _get('names.weekday')),
              LabeledValue('Weekday 7, Nepali', _get('names.weekdayNp')),
            ]),
          ),
        ],
      );
}
