// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import '../support/demo_section.dart';
import '../support/sample_defaults.dart';
import '../support/sample_event_data.dart';

/// Asking a policy about spans, days, whole months and working-day counts,
/// and proving that marking never blocks on its own.
class EventQueriesScreen extends StatefulWidget {
  const EventQueriesScreen({super.key});

  @override
  State<EventQueriesScreen> createState() => _EventQueriesScreenState();
}

class _EventQueriesScreenState extends State<EventQueriesScreen> {
  SampleEventData? _data;
  NepaliCalendarPolicy? _officePolicy;
  String _festivalCount = '…';
  String _festivalFirst = '…';
  String _festivalLast = '…';
  String _festivalCloses = '…';
  String _leaveCount = '…';
  String _statusDay = '…';
  String _statusLine = '…';
  String _statusNames = '…';
  String _monthDays = '…';
  String _monthClosed = '…';
  String _monthWeeklyOff = '…';
  String _monthNamed = '…';
  String _officeDays = '…';
  String _festivalOnlyDays = '…';
  String _leaveDays = '…';
  String _nextWorking = '…';
  String _plusFive = '…';
  String _minusFive = '…';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final data = await SampleEventData.load();
    final today = data.today;

    final officePolicy = NepaliCalendarPolicy(
      weeklyOffDays: const {SampleDefaults.saturday},
      events: data.national,
    );
    final festivalOnly = NepaliCalendarPolicy(
      weeklyOffDays: const <int>{},
      events: data.festivalSpan,
    );
    final leaveOnly = NepaliCalendarPolicy(
      weeklyOffDays: const <int>{},
      events: data.leaveSpan,
    );

    final festivalFirst = data.festivalSpan.first;
    final festivalLast = data.festivalSpan.last;

    final windowEnd = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 21))
        .toSimpleDate();

    final probe = (await NepaliDateConverter.addDaysToBsDate(
            today.year, today.month, today.dayOfMonth, 4))
        .toSimpleDate();
    final status = await officePolicy.statusOf(probe);
    final monthStatuses =
        await officePolicy.monthStatus(today.year, today.month);
    final monthEvents = await officePolicy.eventsIn(today.year, today.month);
    final namedInMonth = <String>{
      for (final event in monthEvents) event.id ?? event.name,
    };

    final officeDays = await officePolicy.workingDaysBetween(today, windowEnd);
    final festivalDays =
        await festivalOnly.workingDaysBetween(today, windowEnd);
    final leaveDays = await leaveOnly.workingDaysBetween(today, windowEnd);
    final nextWorking = await officePolicy.nextWorkingDay(today);
    final plusFive = await officePolicy.addWorkingDays(today, 5);
    final minusFive = await officePolicy.addWorkingDays(today, -5);

    if (!mounted) return;
    setState(() {
      _data = data;
      _officePolicy = officePolicy;
      _festivalCount = '${data.festivalSpan.length} days';
      _festivalFirst =
          '${festivalFirst.year}/${festivalFirst.month}/${festivalFirst.dayOfMonth}';
      _festivalLast =
          '${festivalLast.year}/${festivalLast.month}/${festivalLast.dayOfMonth}';
      _festivalCloses = '${festivalFirst.closesOffices}';
      _leaveCount = '${data.leaveSpan.length} days';
      _statusDay = dateText(probe);
      _statusLine = status.isNonWorking
          ? 'closed${status.isWeeklyOff ? ', weekly off' : ''}'
          : 'working day';
      _statusNames =
          status.names.isEmpty ? 'nothing named' : status.names.join(', ');
      _monthDays = '${monthStatuses.length}';
      _monthClosed =
          '${monthStatuses.where((day) => day.isNonWorking).length}';
      _monthWeeklyOff =
          '${monthStatuses.where((day) => day.isWeeklyOff).length}';
      _monthNamed = '${namedInMonth.length}';
      _officeDays = '$officeDays';
      _festivalOnlyDays = '$festivalDays';
      _leaveDays = '$leaveDays';
      _nextWorking = calendarText(nextWorking);
      _plusFive = calendarText(plusFive);
      _minusFive = calendarText(minusFive);
    });
  }

  @override
  Widget build(BuildContext context) {
    final data = _data;
    final officePolicy = _officePolicy;
    if (data == null || officePolicy == null) {
      return DemoScreen(
        title: 'Event queries',
        children: const [Center(child: CircularProgressIndicator())],
      );
    }
    return DemoScreen(
      title: 'Event queries',
      children: [
        DemoSection(
          title: 'An event that runs longer than a day',
          subtitle: 'The festival span closes offices for ten days; the '
              'leave span never closes anything.',
          child: Column(children: [
            LabeledValue('Dashain (demo)', _festivalCount),
            LabeledValue('First day', _festivalFirst),
            LabeledValue('Last day', _festivalLast),
            LabeledValue('Closes offices', _festivalCloses),
            LabeledValue('Annual leave (demo)', _leaveCount),
          ]),
        ),
        DemoSection(
          title: 'What one day is',
          subtitle: 'The status of a probe day under the office policy.',
          child: Column(children: [
            LabeledValue('Day', _statusDay),
            LabeledValue('Status', _statusLine),
            LabeledValue('Named', _statusNames),
          ]),
        ),
        DemoSection(
          title: 'A whole month at once',
          subtitle: 'One call walks the month; another lists its events.',
          child: Column(children: [
            LabeledValue('Days', _monthDays),
            LabeledValue('Closed', _monthClosed),
            LabeledValue('Weekly off', _monthWeeklyOff),
            LabeledValue('Named entries', _monthNamed),
          ]),
        ),
        DemoSection(
          title: 'Counting working days',
          subtitle: 'The next three weeks under three policies, and the '
              'arithmetic around today.',
          child: Column(children: [
            LabeledValue('Office policy', _officeDays),
            LabeledValue('Festival span only', _festivalOnlyDays),
            LabeledValue('Leave span only', _leaveDays),
            LabeledValue('Next working day', _nextWorking),
            LabeledValue('Five working days on', _plusFive),
            LabeledValue('Five working days back', _minusFive),
          ]),
        ),
        DemoSection(
          title: 'Marking never blocks on its own',
          subtitle: 'The same policy marks the days and, separately, '
              'refuses them through the selectable rule.',
          child: NepaliDatePicker(
            events: data.markingOptions(),
            selectableDates:
                NepaliSelectableDates(excludeClosuresOf: officePolicy),
            onDateSelected: (_) {},
          ),
        ),
      ],
    );
  }
}
