// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

// Golden conversion vectors against the real compiled engine. The same
// facts hold in the Kotlin, npm and Python test suites; any mismatch here
// is a bridge defect, never a calendar one.
//
// Run on a device or emulator:
//   flutter test integration_test/plugin_integration_test.dart

import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('year ranges match the published table bounds', (tester) async {
    expect(await NepaliDateConverter.bsYearRange(), const YearRange(1970, 2100));
    expect(await NepaliDateConverter.adYearRange(), const YearRange(1913, 2043));
  });

  testWidgets('the anchor converts both ways', (tester) async {
    final bs = await NepaliDateConverter.convertAdToBs(1913, 4, 13);
    expect(bs.year, 1970);
    expect(bs.month, 1);
    expect(bs.dayOfMonth, 1);
    expect(bs.era, 2);
    expect(bs.dayOfWeek, 1, reason: '1970-01-01 BS was a Sunday');

    final ad = await NepaliDateConverter.convertBsToAd(1970, 1, 1);
    expect(ad.year, 1913);
    expect(ad.month, 4);
    expect(ad.dayOfMonth, 13);
    expect(ad.era, 1);
  });

  testWidgets('known new-year vectors hold', (tester) async {
    const vectors = <(int, int, int, int, int, int)>[
      (2077, 1, 1, 2020, 4, 13),
      (2081, 1, 1, 2024, 4, 13),
      (2082, 1, 1, 2025, 4, 14),
    ];
    for (final (bsY, bsM, bsD, adY, adM, adD) in vectors) {
      final ad = await NepaliDateConverter.convertBsToAd(bsY, bsM, bsD);
      expect((ad.year, ad.month, ad.dayOfMonth), (adY, adM, adD),
          reason: 'BS $bsY-$bsM-$bsD');
      final bs = await NepaliDateConverter.convertAdToBs(adY, adM, adD);
      expect((bs.year, bs.month, bs.dayOfMonth), (bsY, bsM, bsD),
          reason: 'AD $adY-$adM-$adD');
    }
  });

  testWidgets('every day of a month round-trips', (tester) async {
    final info = await NepaliDateConverter.bsMonth(2082, 6);
    final adDays = await NepaliDateConverter.adCalendarsInBsMonth(2082, 6);
    expect(adDays.length, info.totalDaysInMonth);
    for (var day = 1; day <= info.totalDaysInMonth; day++) {
      final ad = adDays[day - 1];
      final back = await NepaliDateConverter.convertAdToBs(
          ad.year, ad.month, ad.dayOfMonth);
      expect((back.year, back.month, back.dayOfMonth), (2082, 6, day));
    }
  });

  testWidgets('the convertibility edge sits just before the anchor',
      (tester) async {
    expect(await NepaliDateConverter.isAdDateConvertible(1913, 4, 13), isTrue);
    expect(await NepaliDateConverter.isAdDateConvertible(1913, 4, 12), isFalse);
  });

  testWidgets('out of range input fails as ArgumentError without crashing',
      (tester) async {
    expect(() => NepaliDateConverter.convertBsToAd(1969, 1, 1),
        throwsArgumentError);
    expect(() => NepaliDateConverter.convertBsToAd(2082, 13, 1),
        throwsArgumentError);
    expect(() => NepaliDateConverter.convertBsToAd(2082, 6, 40),
        throwsArgumentError);
  });

  testWidgets('formatting and digits work through the bridge', (tester) async {
    final formatted = await NepaliDateConverter.formatBsDate(
      await NepaliDateConverter.bsCalendar(2082, 6, 4),
      locale: const NepaliDateLocale(language: NepaliLanguage.nepali),
    );
    expect(formatted, isNotEmpty);

    expect(await NepaliDateConverter.localizeDigits('2082'), '२०८२');
    expect(await NepaliDateConverter.toLatinDigits('२०८२'), '2082');
    expect(
      await NepaliDateFormatter.format(const SimpleDate(2082, 6, 4)),
      '2082-06-04',
    );
  });

  testWidgets('policies compute statuses and working days', (tester) async {
    final policy = NepaliCalendarPolicy(
      events: const [
        NepaliEvent(
          year: 2082,
          month: 6,
          dayOfMonth: 24,
          name: 'Vijaya Dashami',
          kind: NepaliEventKind.governmentPublic,
        ),
      ],
    );
    final status = await policy.statusOf(const SimpleDate(2082, 6, 24));
    expect(status.isNonWorking, isTrue);
    expect(status.names, contains('Vijaya Dashami'));

    final saturday = await policy.monthStatus(2082, 6);
    expect(saturday.where((day) => day.isWeeklyOff), isNotEmpty);

    final next = await policy.nextWorkingDay(const SimpleDate(2082, 6, 24));
    expect((next.year, next.month), (2082, 6));
    expect(next.dayOfMonth, greaterThan(24));
  });
}
