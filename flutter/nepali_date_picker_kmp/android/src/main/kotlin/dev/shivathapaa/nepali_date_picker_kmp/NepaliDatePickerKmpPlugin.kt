// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/**
 * Wires the engine host, the dialog host, the appearance proxy and the
 * embedded picker views into one Flutter engine.
 */
class NepaliDatePickerKmpPlugin : FlutterPlugin, ActivityAware {

    private val pickerHost = PickerHostApiImpl()
    private var flutterApi: PickerViewFlutterApi? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        EngineApi.setUp(binding.binaryMessenger, EngineApiImpl())
        PickerHostApi.setUp(binding.binaryMessenger, pickerHost)
        flutterApi = PickerViewFlutterApi(binding.binaryMessenger)
        binding.platformViewRegistry.registerViewFactory(
            "nepali_date_picker_kmp/picker",
            PickerPlatformViewFactory { requireNotNull(flutterApi) { "plugin detached" } }
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        EngineApi.setUp(binding.binaryMessenger, null)
        PickerHostApi.setUp(binding.binaryMessenger, null)
        flutterApi = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        pickerHost.activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        pickerHost.activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        pickerHost.activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        pickerHost.activity = null
    }
}
