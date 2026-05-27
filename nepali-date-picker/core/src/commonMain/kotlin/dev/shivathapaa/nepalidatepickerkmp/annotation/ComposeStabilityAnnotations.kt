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

package dev.shivathapaa.nepalidatepickerkmp.annotation

// Expect-annotations let `:core` advertise Compose stability on Compose-supported targets
// (Android, JVM, iosArm64, iosSimulatorArm64, macosArm64, JS, WasmJs) while still compiling on
// non-Compose targets (Linux, Windows, watchOS, tvOS, Apple x86_64, wasmWasi). On targets that
// don't provide an `actual`, `@OptionalExpectation` makes the annotation a no-op at compile time.

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
expect annotation class Immutable()

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
)
expect annotation class Stable()
