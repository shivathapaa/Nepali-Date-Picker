// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'messages.g.dart';

/// The live pigeon clients every facade routes through. Mutable and
/// unexported so unit tests can substitute fakes; production code never
/// reassigns them.
EngineApi engineApi = EngineApi();
PickerHostApi pickerHostApi = PickerHostApi();
