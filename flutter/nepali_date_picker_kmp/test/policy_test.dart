// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'recording_engine_api.dart';

/// Pins the policy facade: the description crosses as data on every call,
/// each query reaches the engine method that names it, and the lists come
/// back read-only.
void main() {
  late RecordingEngineApi engine;

  setUp(() {
    engine = RecordingEngineApi();
    bridge.engineApi = engine;
    NepaliDateValidation.debugResetCaches();
  });

  const date = SimpleDate(2082, 6, 24);
  const event = NepaliEvent(
    year: 2082,
    month: 6,
    dayOfMonth: 24,
    name: 'Vijaya Dashami',
    kind: NepaliEventKind.governmentPublic,
    id: 'dashami',
    payload: '{"note":"tenth day"}',
  );

  NepaliCalendarPolicy office() => NepaliCalendarPolicy(
        weeklyOffDays: const {7, 1},
        events: const [event],
      );

  test('the constructor rejects weekdays outside 1..7', () {
    expect(() => NepaliCalendarPolicy(weeklyOffDays: const {0}),
        throwsArgumentError);
    expect(() => NepaliCalendarPolicy(weeklyOffDays: const {8}),
        throwsArgumentError);
    expect(NepaliCalendarPolicy(weeklyOffDays: const {7, 1}).isWeeklyOff(1),
        isTrue);
    expect(NepaliCalendarPolicy().isWeeklyOff(1), isFalse);
  });

  test('the policy crosses the bridge as data, events included', () async {
    final status = await office().statusOf(date);

    expect(status.isNonWorking, isTrue);
    expect(status.primaryKind, NepaliEventKind.governmentPublic);
    expect(status.names.single, 'Vijaya Dashami');
    expect(status.events.single.name, 'Vijaya Dashami');
    expect(status.closures.single.closesOffices, isTrue);

    final sent = engine.sentPolicies.single;
    expect(sent.weeklyOffDays, containsAll(<int>[7, 1]));
    final sentEvent = sent.events.single;
    expect(sentEvent.name, 'Vijaya Dashami');
    expect(sentEvent.kind, EventKindDto.governmentPublic);
    expect(sentEvent.closesOffices, isTrue);
    expect(sentEvent.id, 'dashami');
    expect(sentEvent.payload, '{"note":"tenth day"}');
  });

  test('every query sends the same description', () async {
    final policy = office();
    await policy.statusOf(date);
    await policy.isNonWorkingDay(date);
    await policy.eventsOn(date);

    expect(engine.sentPolicies, hasLength(3));
    expect(engine.sentPolicies.every((sent) => identical(sent, engine.sentPolicies.first)),
        isTrue);
  });

  test('a month of statuses is validated and comes back read-only', () async {
    final statuses = await office().monthStatus(2082, 6);

    expect(statuses.single.isNonWorking, isTrue);
    expect(engine.argumentsOf('monthStatus')!.sublist(1), <Object?>[2082, 6]);
    expect(() => statuses.clear(), throwsUnsupportedError);
    await expectLater(office().monthStatus(2082, 13), throwsArgumentError);
  });

  test('events on a day and in a month keep their kind', () async {
    final onDay = await office().eventsOn(date);
    expect(onDay.single.kind, NepaliEventKind.governmentPublic);
    expect((engine.argumentsOf('eventsOn')![1] as DateDto).dayOfMonth, 24);
    expect(() => onDay.clear(), throwsUnsupportedError);

    final inMonth = await office().eventsIn(2082, 6);
    expect(inMonth.single.name, 'Vijaya Dashami');
    expect(engine.argumentsOf('eventsIn')!.sublist(1), <Object?>[2082, 6]);
  });

  test('a closed day is answered without a round trip through status',
      () async {
    expect(await office().isNonWorkingDay(date), isTrue);
    expect(engine.calls, contains('isNonWorkingDay'));
    expect(engine.calls, isNot(contains('statusOf')));
  });

  test('working day queries validate their range order', () async {
    final policy = NepaliCalendarPolicy();
    await expectLater(
      policy.workingDaysBetween(
          const SimpleDate(2082, 7, 1), const SimpleDate(2082, 6, 1)),
      throwsArgumentError,
    );
    expect(
      await policy.workingDaysBetween(
          const SimpleDate(2082, 6, 1), const SimpleDate(2082, 7, 1)),
      7,
    );
    expect(engine.calls, contains('workingDaysBetween'));
  });

  test('the next working day and working-day arithmetic map back to dates',
      () async {
    final policy = office();

    expect((await policy.nextWorkingDay(date)).year, 2082);
    expect((engine.argumentsOf('nextWorkingDay')![1] as DateDto).dayOfMonth, 24);

    expect((await policy.addWorkingDays(date, -5)).era, 2);
    expect(engine.argumentsOf('addWorkingDays')!.last, -5);
  });

  test('a date outside the table is refused before any query', () async {
    final policy = office();
    await expectLater(
        policy.statusOf(const SimpleDate(2101, 1, 1)), throwsArgumentError);
    await expectLater(policy.nextWorkingDay(const SimpleDate(2082, 6, 31)),
        throwsArgumentError);
    expect(engine.calls, isNot(contains('statusOf')));
    expect(engine.calls, isNot(contains('nextWorkingDay')));
  });

  test('two policies built from the same description are equal', () {
    expect(office(), office());
    expect(office().hashCode, office().hashCode);
    expect(office(), isNot(NepaliCalendarPolicy(weeklyOffDays: const {7})));
  });
}
