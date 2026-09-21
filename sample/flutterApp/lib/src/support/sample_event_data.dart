// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// The demo event lists every Events and rules screen shares, the same names,
// offsets and colours the SwiftUI sample's SampleEventData carries. Built
// once per run because the offsets hang off today.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import 'sample_defaults.dart';
import 'sample_event_payload.dart';

class SampleEventData {
  SampleEventData._({
    required this.today,
    required this.events,
    required this.ownEvents,
    required this.festivalSpan,
    required this.leaveSpan,
    required this.schoolEvents,
  });

  final SimpleDate today;

  /// Named days near today: an observance, a regional day and a closure.
  final List<NepaliEvent> events;

  /// The app's own dot-events; several share a day on purpose.
  final List<NepaliEvent> ownEvents;

  /// Dashain (demo), ten days from today+3, offices closed throughout.
  final List<NepaliEvent> festivalSpan;

  /// Annual leave (demo), today+2 through today+6, offices stay open.
  final List<NepaliEvent> leaveSpan;

  /// The school's own closures, layered over the national list.
  final List<NepaliEvent> schoolEvents;

  /// The dot colours the events screen cycles through.
  static const dotColors = <Color>[
    Color(0xFF1E88E5),
    Color(0xFFF4511E),
    Color(0xFF43A047),
    Color(0xFF8E24AA),
  ];

  static Future<SampleEventData>? _loading;

  static Future<SampleEventData> load() => _loading ??= _build();

  static Future<SampleEventData> _build() async {
    final todayCalendar = await NepaliDateConverter.todayBs();
    final today = todayCalendar.toSimpleDate();

    Future<SimpleDate> offset(int days) async =>
        (await NepaliDateConverter.addDaysToBsDate(
                today.year, today.month, today.dayOfMonth, days))
            .toSimpleDate();

    NepaliEvent at(
      SimpleDate date,
      String name,
      NepaliEventKind kind, {
      String? id,
      SampleEventPayload? payload,
    }) =>
        NepaliEvent(
          year: date.year,
          month: date.month,
          dayOfMonth: date.dayOfMonth,
          name: name,
          kind: kind,
          id: id,
          payload: payload?.encode(),
        );

    final events = [
      at(
        await offset(2),
        'Company day (demo)',
        NepaliEventKind.observance,
        id: 'company-day',
        payload: const SampleEventPayload(
          description: 'Teams present what they shipped, then lunch.',
          icon: 'star',
          accentArgb: 0xFF1E88E5,
          tags: ['work'],
          venue: 'Auditorium',
          organizer: 'People team',
        ),
      ),
      at(
        await offset(3),
        'Local jatra (demo)',
        NepaliEventKind.regional,
        id: 'local-jatra',
        payload: const SampleEventPayload(
          description: 'Chariot through the old town from the afternoon.',
          icon: 'festival',
          accentArgb: 0xFFD32F2F,
          bannerUrl:
              'https://upload.wikimedia.org/wikipedia/commons/2/2b/Indra_Jatra_2014.jpg',
          tags: ['festival', 'local'],
          venue: 'Basantapur',
          link: 'https://en.wikipedia.org/wiki/Jatra',
        ),
      ),
      at(
        await offset(9),
        'Offices closed (demo)',
        NepaliEventKind.governmentPublic,
        id: 'offices-closed',
        payload: const SampleEventPayload(
          description: 'Government holiday, banks included.',
          icon: 'date',
          accentArgb: 0xFFC62828,
          tags: ['public'],
        ),
      ),
    ];

    final ownEvents = [
      at(await offset(1), 'Standup', NepaliEventKind.observance,
          id: 'standup',
          payload: const SampleEventPayload(
            description: 'Fifteen minutes, blockers only.',
            icon: 'date',
            accentArgb: 0xFF3949AB,
            tags: ['work', 'recurring'],
          )),
      at(await offset(4), 'Sprint review', NepaliEventKind.observance,
          id: 'sprint-review',
          payload: const SampleEventPayload(
            description: 'Demo what the fortnight produced.',
            icon: 'star',
            accentArgb: 0xFF5E35B1,
            tags: ['work'],
            venue: 'Meeting room 3B',
          )),
      at(await offset(4), "Aama's birthday", NepaliEventKind.observance,
          id: 'birthday-aama',
          payload: const SampleEventPayload(
            description: 'Cake at home in the evening.',
            icon: 'festival',
            accentArgb: 0xFFEC407A,
            iconUrl:
                'https://upload.wikimedia.org/wikipedia/commons/4/45/Birthday_cake.jpg',
            tags: ['personal', 'family'],
          )),
      at(await offset(6), 'Dentist', NepaliEventKind.observance,
          id: 'dentist',
          payload: const SampleEventPayload(
            description: 'Six-month check.',
            icon: 'reminder',
            accentArgb: 0xFF00897B,
            tags: ['health'],
            venue: 'Lalitpur Dental',
          )),
      at(await offset(6), 'Bank errand', NepaliEventKind.observance,
          id: 'bank-errand',
          payload: const SampleEventPayload(
            description: 'Cheque deposit before the counter closes.',
            icon: 'shopping',
            accentArgb: 0xFFEF6C00,
            tags: ['errand'],
          )),
      at(await offset(6), "Friend's wedding", NepaliEventKind.observance,
          id: 'wedding',
          payload: const SampleEventPayload(
            description: 'Reception from seven, gift arranged.',
            icon: 'person',
            accentArgb: 0xFF8E24AA,
            tags: ['personal'],
            venue: 'Party palace, Bhaktapur',
          )),
      at(await offset(6), 'Design review', NepaliEventKind.observance,
          id: 'design-review',
          payload: const SampleEventPayload(
            description: 'Two flows to sign off before handover.',
            icon: 'share',
            accentArgb: 0xFF43A047,
            tags: ['work'],
          )),
    ];

    final dashainStart = await offset(3);
    final festivalSpan = await NepaliEvent(
      year: dashainStart.year,
      month: dashainStart.month,
      dayOfMonth: dashainStart.dayOfMonth,
      name: 'Dashain (demo)',
      kind: NepaliEventKind.religious,
      closesOffices: true,
      id: 'dashain-demo',
      payload: const SampleEventPayload(
        description: 'Ten days, offices shut from the seventh.',
        icon: 'festival',
        accentArgb: 0xFFD32F2F,
        bannerUrl:
            'https://upload.wikimedia.org/wikipedia/commons/3/3a/Dashain_tika.jpg',
        tags: ['festival', 'public'],
        link: 'https://en.wikipedia.org/wiki/Dashain',
      ).encode(),
    ).spanningDays(10);

    final leaveStart = await offset(2);
    final leaveSpan = await NepaliEvent(
      year: leaveStart.year,
      month: leaveStart.month,
      dayOfMonth: leaveStart.dayOfMonth,
      name: 'Annual leave (demo)',
      kind: NepaliEventKind.observance,
      closesOffices: false,
      id: 'leave-42',
    ).spanningThrough(await offset(6));

    final founders = await offset(5);
    final examBreak = await offset(12);
    final schoolEvents = [
      NepaliEvent(
        year: founders.year,
        month: founders.month,
        dayOfMonth: founders.dayOfMonth,
        name: 'Founders Day (school)',
        kind: NepaliEventKind.regional,
        closesOffices: true,
        id: 'founders-day',
      ),
      NepaliEvent(
        year: examBreak.year,
        month: examBreak.month,
        dayOfMonth: examBreak.dayOfMonth,
        name: 'Exam break (school)',
        kind: NepaliEventKind.regional,
        closesOffices: true,
        id: 'exam-break',
      ),
    ];

    return SampleEventData._(
      today: today,
      events: events,
      ownEvents: ownEvents,
      festivalSpan: festivalSpan,
      leaveSpan: leaveSpan,
      schoolEvents: schoolEvents,
    );
  }

  /// The national closures: the named days plus the festival span.
  List<NepaliEvent> get national => [...events, ...festivalSpan];

  /// The marking options most events demos start from.
  NepaliPickerEventOptions markingOptions({
    List<int> weeklyOffDays = const [SampleDefaults.saturday],
  }) =>
      NepaliPickerEventOptions(
        weeklyOffDays: weeklyOffDays,
        events: [for (final event in national) event.asPickerEvent()],
      );
}

extension SampleEventMarks on NepaliEvent {
  /// The event as a picker mark, keeping its kind and closure flag.
  NepaliPickerEvent asPickerEvent({Color? color, bool indicate = false}) =>
      NepaliPickerEvent(
        year: year,
        month: month,
        dayOfMonth: dayOfMonth,
        name: name,
        kind: kind,
        closesOffices: closesOffices,
        color: color,
        indicate: indicate,
        // The app's own handles ride along, so a tapped entry comes back with
        // the record behind it rather than just a name.
        id: id,
        payload: payload,
      );
}
