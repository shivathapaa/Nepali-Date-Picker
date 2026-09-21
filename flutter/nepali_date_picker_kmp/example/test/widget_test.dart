// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:nepali_date_picker_kmp_example/main.dart';

void main() {
  testWidgets('the compact demo shows its three sections', (tester) async {
    await tester.pumpWidget(const NepaliDatePickerExampleApp());
    expect(find.text('Today'), findsOneWidget);
    expect(find.textContaining('Selected:'), findsOneWidget);
    expect(find.text('Open the native dialog'), findsOneWidget);
  });
}
