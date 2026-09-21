// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:flutter_sample/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('the index carries the four shared sections', (tester) async {
    await tester.pumpWidget(const NepaliDatePickerExampleApp());
    await tester.pump();

    expect(find.text('PICKERS'), findsOneWidget);
    expect(find.text('Pickers'), findsOneWidget);
    expect(find.text('Calendar switch'), findsOneWidget);

    for (final header in ['FIELDS', 'EVENTS', 'ENGINE']) {
      await tester.scrollUntilVisible(find.text(header), 200);
      expect(find.text(header), findsOneWidget);
    }
    expect(find.text('Composing rules'), findsOneWidget);
    await tester.scrollUntilVisible(find.text('Engine queries'), 200);
    expect(find.text('Utilities'), findsOneWidget);
  });

  testWidgets('the appearance menu lists brightness and every palette',
      (tester) async {
    await tester.pumpWidget(const NepaliDatePickerExampleApp());
    await tester.pump();

    await tester.tap(find.byIcon(Icons.palette_outlined));
    await tester.pumpAndSettle();

    for (final label in ['System', 'Light', 'Dark']) {
      expect(find.text(label), findsOneWidget);
    }
    for (final label in [
      'Default',
      'Green',
      'Blue',
      'Orange',
      'Red',
      'Yellow'
    ]) {
      expect(find.text(label), findsOneWidget);
    }
  });
}
