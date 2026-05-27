/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
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

package dev.shivathapaa.nepalidatepickerkmp.annotations

import kotlin.RequiresOptIn

/**
 * Marks the annotated API as experimental in the Nepali Date Picker library.
 *
 * Experimental APIs might be changed or deprecated in future versions. Deprecated APIs
 * will be marked with the `@Deprecated` annotation and eventually removed, so please
 * plan to migrate away from them.
 *
 * Use experimental APIs with caution and be prepared to adapt your code if necessary.
 *
 * ```
 *@OptIn(NepaliDatePickerExperimentalApi: : class)
 * fun myFunction() {
 * // ... use experimental API here
 * }
 * ```
 */
@RequiresOptIn(message = "This API is experimental and might be changed or removed in the future. Use is carefully!")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.TYPEALIAS
)
annotation class ExperimentalNepaliDatePickerApi