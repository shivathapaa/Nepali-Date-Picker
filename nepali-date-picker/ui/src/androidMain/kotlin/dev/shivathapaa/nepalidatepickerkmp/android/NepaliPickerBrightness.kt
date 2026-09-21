/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp.android

/**
 * Which brightness the hosted pickers draw at.
 *
 * [System] follows the device's dark-mode setting and switches with it while a picker is on
 * screen. [Light] and [Dark] pin the picker regardless of the device setting, for an app that
 * themes itself independently of Android.
 */
enum class NepaliPickerBrightness {
    System,
    Light,
    Dark
}
