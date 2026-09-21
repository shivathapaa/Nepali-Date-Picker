// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import Flutter
import UIKit

/// Wires the engine host, the dialog host, the appearance proxy and the
/// embedded picker views into one Flutter engine.
public class NepaliDatePickerKmpPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let messenger = registrar.messenger()
    EngineApiSetup.setUp(binaryMessenger: messenger, api: EngineApiImpl())
    PickerHostApiSetup.setUp(binaryMessenger: messenger, api: PickerHostApiImpl())
    let flutterApi = PickerViewFlutterApi(binaryMessenger: messenger)
    registrar.register(
      PickerPlatformViewFactory(flutterApi: { flutterApi }),
      withId: "nepali_date_picker_kmp/picker"
    )
  }
}
