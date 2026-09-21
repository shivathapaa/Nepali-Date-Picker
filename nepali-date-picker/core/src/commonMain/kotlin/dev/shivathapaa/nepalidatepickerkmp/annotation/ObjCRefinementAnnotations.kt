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

package dev.shivathapaa.nepalidatepickerkmp.annotation

// Keeps a declaration out of the generated Objective-C header on Apple targets while leaving it
// untouched everywhere else. Use it where a Kotlin signature has no faithful Objective-C form and a
// Swift-shaped alternative exists beside it, so Swift cannot reach the shape that would mislead.
// Targets that provide no `actual` compile the annotation away, the way the Compose stability
// annotations do on Linux, Windows and wasmWasi.

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
expect annotation class HiddenFromObjC()
