// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'recording_engine_api.dart';

/// Pins the span helpers over one event: what they send, what they refuse
/// locally, and the read-only list they answer with.
void main() {
  late RecordingEngineApi engine;

  setUp(() {
    engine = RecordingEngineApi();
    bridge.engineApi = engine;
    NepaliDateValidation.debugResetCaches();
  });

  const event = NepaliEvent(
    year: 2082,
    month: 6,
    dayOfMonth: 21,
    name: 'Dashain',
    kind: NepaliEventKind.governmentPublic,
    id: 'dashain',
  );

  test('a span of days repeats the event, identity included', () async {
    final span = await event.spanningDays(2);

    expect(span, hasLength(2));
    expect(span.first.kind, NepaliEventKind.governmentPublic);
    final sent = engine.argumentsOf('eventSpanningDays')!;
    expect((sent.first as EventDto).name, 'Dashain');
    expect((sent.first as EventDto).dayOfMonth, 21);
    expect((sent.first as EventDto).id, 'dashain');
    expect(sent.last, 2);
    expect(() => span.clear(), throwsUnsupportedError);
  });

  test('a span through a date carries both ends', () async {
    final span = await event.spanningThrough(const SimpleDate(2082, 6, 23));

    expect(span, hasLength(3));
    final sent = engine.argumentsOf('eventSpanningThrough')!;
    expect((sent.last as DateDto).dayOfMonth, 23);
    expect(() => span.clear(), throwsUnsupportedError);
  });

  test('an empty or backwards span is refused before the bridge', () async {
    await expectLater(event.spanningDays(0), throwsArgumentError);
    await expectLater(event.spanningDays(-1), throwsArgumentError);
    await expectLater(
        event.spanningThrough(const SimpleDate(2082, 6, 20)),
        throwsArgumentError);
    expect(engine.calls, isEmpty);
  });

  test('a single day span is the event itself', () async {
    await event.spanningThrough(const SimpleDate(2082, 6, 21));

    expect(engine.calls.single, 'eventSpanningThrough');
  });
}
