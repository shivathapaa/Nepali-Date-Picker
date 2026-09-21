// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';
import 'package:nepali_date_picker_kmp/src/bridge.dart' as bridge;
import 'package:nepali_date_picker_kmp/src/messages.g.dart';
import 'package:nepali_date_picker_kmp/src/validation.dart';

import 'recording_engine_api.dart';

/// Pins the fixed-shape wire formatters: the pattern and script each call
/// carries, and the null that unmatched text comes back as.
void main() {
  late RecordingEngineApi engine;

  setUp(() {
    engine = RecordingEngineApi();
    bridge.engineApi = engine;
    NepaliDateValidation.debugResetCaches();
  });

  test('every pattern prints the shape a caller can show as a placeholder',
      () {
    expect(DatePattern.yyyySlashMmSlashDd.literal, 'YYYY/MM/DD');
    expect(DatePattern.yyyyDashMmDashDd.literal, 'YYYY-MM-DD');
    expect(DatePattern.ddSlashMmSlashYyyy.literal, 'DD/MM/YYYY');
    expect(DatePattern.ddDashMmDashYyyy.literal, 'DD-MM-YYYY');
    for (final pattern in DatePattern.values) {
      expect(pattern.literal.length, 10);
    }
  });

  test('a date formats in the canonical shape unless told otherwise',
      () async {
    expect(await NepaliDateFormatter.format(const SimpleDate(2082, 6, 4)),
        '2082-06-04');
    expect(engine.lastArguments[1], DatePatternDto.yyyyDashMmDashDd);
    expect(engine.lastArguments[2], DigitScriptDto.latin);
    expect((engine.lastArguments.first as DateDto).dayOfMonth, 4);
  });

  test('the pattern and the script both reach the engine', () async {
    await NepaliDateFormatter.format(
      const SimpleDate(2082, 6, 4),
      pattern: DatePattern.ddSlashMmSlashYyyy,
      script: DigitScript.devanagari,
    );

    expect(engine.lastArguments[1], DatePatternDto.ddSlashMmSlashYyyy);
    expect(engine.lastArguments[2], DigitScriptDto.devanagari);
  });

  test('parsed text becomes a date, in the pattern it was read with',
      () async {
    expect(
        await NepaliDateFormatter.parse('04/06/2082',
            pattern: DatePattern.ddSlashMmSlashYyyy),
        const SimpleDate(2082, 6, 4));
    expect(engine.lastArguments,
        <Object?>['04/06/2082', DatePatternDto.ddSlashMmSlashYyyy]);
  });

  test('text that does not match the shape parses as null', () async {
    bridge.engineApi = RecordingEngineApi(parsesText: false);

    expect(await NepaliDateFormatter.parse('not a date'), isNull);
    expect(await NepaliTimeFormatter.parse('not a time'), isNull);
  });

  test('a time formats and parses in the wire shape', () async {
    expect(
        await NepaliTimeFormatter.format(
            const SimpleTime(hour: 16, minute: 30, second: 15)),
        '16:30:15');
    expect((engine.lastArguments.single as TimeDto).second, 15);
    expect(await NepaliTimeFormatter.parse('16:30:15'),
        const SimpleTime(hour: 16, minute: 30, second: 15));
  });

  test('an impossible time never reaches the engine', () async {
    await expectLater(
        NepaliTimeFormatter.format(const SimpleTime(hour: 0, minute: 60)),
        throwsArgumentError);
    expect(engine.calls, isEmpty);
  });
}
