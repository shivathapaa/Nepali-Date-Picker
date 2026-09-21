// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:nepali_date_picker_kmp/src/messages.g.dart';

import 'recording_engine_api.dart';

/// A stand-in for the native dialog and appearance host, recording what it
/// was handed and answering dialogs with [confirmed].
class RecordingHostApi implements PickerHostApi {
  /// The date every dialog completes with; null stands for a dismissal.
  RecordingHostApi({this.confirmed});

  final CalendarDto? confirmed;

  final List<DialogConfigDto> dialogs = <DialogConfigDto>[];
  final List<DialogConfigDto> fullScreenDialogs = <DialogConfigDto>[];
  final List<AppearanceDto> appearances = <AppearanceDto>[];
  int resets = 0;

  static CalendarDto get calendar => RecordingEngineApi.bsCalendar;

  @override
  Future<CalendarDto?> showDatePickerDialog(DialogConfigDto config) async {
    dialogs.add(config);
    return confirmed;
  }

  @override
  Future<CalendarDto?> showFullScreenDatePickerDialog(
      DialogConfigDto config) async {
    fullScreenDialogs.add(config);
    return confirmed;
  }

  @override
  Future<void> applyAppearance(AppearanceDto appearance) async {
    appearances.add(appearance);
  }

  @override
  Future<void> resetAppearance() async {
    resets++;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw UnimplementedError('${invocation.memberName} is not faked');
}
