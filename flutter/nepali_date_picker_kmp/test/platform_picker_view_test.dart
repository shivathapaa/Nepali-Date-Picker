// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp/src/pickers/platform_picker_view.dart';

/// Pins the sizing contract every embedded picker rides on: the native view
/// is always measured at the generous measurement height while the widget
/// only occupies the height Compose reported, clipped.
///
/// Losing that split is what traps a picker at its smallest size after a mode
/// switch, so the clamp, the fallback and the measurement frame are all
/// asserted rather than left to a device run.
void main() {
  const double measurementHeight = 600;
  const double fallbackHeight = 240;
  const String heightChannel =
      'dev.flutter.pigeon.nepali_date_picker_kmp.PickerViewFlutterApi.onHeightChanged';

  late List<int> createdViewIds;

  setUp(() {
    createdViewIds = <int>[];
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(SystemChannels.platform_views, (call) async {
      if (call.method == 'create') {
        createdViewIds.add((call.arguments as Map<Object?, Object?>)['id']! as int);
        return 0;
      }
      return null;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(SystemChannels.platform_views, null);
  });

  Future<void> reportHeight(int viewId, double height) async {
    await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .handlePlatformMessage(
      heightChannel,
      const StandardMessageCodec()
          .encodeMessage(<Object?>[viewId, height]),
      (_) {},
    );
  }

  Future<void> pumpPicker(
    WidgetTester tester, {
    PickerViewCallbacks? callbacks,
    bool mounted = true,
  }) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: mounted
            ? NepaliPlatformPickerView(
                creationParams:
                    buildPickerCreationParams(variant: 'datePicker'),
                callbacks: callbacks ?? PickerViewCallbacks(),
                fallbackHeight: fallbackHeight,
                measurementHeight: measurementHeight,
              )
            : const SizedBox.shrink(),
      ),
    ));
    await tester.pump();
  }

  double hostedHeight(WidgetTester tester) => tester
      .widget<SizedBox>(find.ancestor(
        of: find.byType(OverflowBox),
        matching: find.byType(SizedBox),
      ))
      .height!;

  testWidgets('the view is measured generously before any report lands',
      (tester) async {
    await pumpPicker(tester);

    expect(hostedHeight(tester), fallbackHeight);
    final overflow = tester.widget<OverflowBox>(find.byType(OverflowBox));
    expect(overflow.minHeight, measurementHeight);
    expect(overflow.maxHeight, measurementHeight);
    expect(find.byType(ClipRect), findsAtLeastNWidgets(1));
    expect(tester.getSize(find.byType(NepaliPlatformPickerView)).height,
        fallbackHeight);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a reported height becomes the height the widget occupies',
      (tester) async {
    await pumpPicker(tester);

    await reportHeight(createdViewIds.single, 412);
    await tester.pump();

    expect(hostedHeight(tester), 412);
    expect(tester.widget<OverflowBox>(find.byType(OverflowBox)).maxHeight,
        measurementHeight);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a report taller than the frame is clamped to it',
      (tester) async {
    await pumpPicker(tester);

    await reportHeight(createdViewIds.single, 5000);
    await tester.pump();

    expect(hostedHeight(tester), measurementHeight);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a zero report is noise and leaves the last height alone',
      (tester) async {
    await pumpPicker(tester);

    await reportHeight(createdViewIds.single, 412);
    await tester.pump();
    await reportHeight(createdViewIds.single, 0);
    await tester.pump();

    expect(hostedHeight(tester), 412);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('switching modes both ways returns the original height',
      (tester) async {
    await pumpPicker(tester);
    final viewId = createdViewIds.single;

    await reportHeight(viewId, 560);
    await tester.pump();
    await reportHeight(viewId, 180);
    await tester.pump();
    expect(hostedHeight(tester), 180);

    await reportHeight(viewId, 560);
    await tester.pump();
    expect(hostedHeight(tester), 560);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('the caller hears the raw report, clamping included',
      (tester) async {
    final reported = <double>[];
    await pumpPicker(tester,
        callbacks: PickerViewCallbacks(
            onHeightChanged: (height) => reported.add(height)));

    await reportHeight(createdViewIds.single, 5000);
    await tester.pump();

    expect(reported, <double>[5000]);
    expect(hostedHeight(tester), measurementHeight);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a disposed view stops hearing its callbacks', (tester) async {
    final reported = <double>[];
    await pumpPicker(tester,
        callbacks: PickerViewCallbacks(
            onHeightChanged: (height) => reported.add(height)));
    final viewId = createdViewIds.single;

    await pumpPicker(tester, mounted: false);
    await reportHeight(viewId, 412);
    await tester.pump();

    expect(reported, isEmpty);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('an unsupported platform says so instead of embedding',
      (tester) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: NepaliPlatformPickerView(
          creationParams: buildPickerCreationParams(variant: 'datePicker'),
          callbacks: PickerViewCallbacks(),
          fallbackHeight: fallbackHeight,
          measurementHeight: measurementHeight,
        ),
      ),
    ));

    expect(find.byType(ErrorWidget), findsOneWidget);
    expect(find.byType(AndroidView), findsNothing);
    expect(find.byType(UiKitView), findsNothing);
  }, variant: TargetPlatformVariant.only(TargetPlatform.macOS));
}
