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

import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }

        publishLibraryVariants("release")
    }

    val xcFrameworkName = "nepali-date-picker"
    val xcf = XCFramework()

    listOf(
        iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcFrameworkName

            binaryOption("bundleId", "io.github.shivathapaa.${xcFrameworkName}")
            xcf.add(this)
            isStatic = true
        }
    }

    jvm()
    macosX64()
    macosArm64()

    js(IR) {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.executable()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(libs.kotlinx.datetime)
                implementation(libs.material.icons.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(npm("@js-joda/timezone", "2.3.0"))
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(npm("@js-joda/timezone", "2.3.0"))
            }
        }
    }
}

android {
    namespace = "io.github.shivathapaa"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}


mavenPublishing {
    // Coordinates for the published artifact
    coordinates(
        groupId = "io.github.shivathapaa",
        artifactId = "nepali-date-picker",
        version = "2.5.1"
    )

    configure(
        KotlinMultiplatform(
            sourcesJar = true
        )
    )

    // POM metadata for the published artifact
    pom {
        name.set("Nepali Date Picker KMP")
        description.set("Nepali Date Picker for both Android and/or iOS and/or KMP (JVM, JS, Wasm) which aligns with the Material3 Date Picker. This library give various utilities to work with Nepali Dates and acts as a bridge between Nepali Calendar and Gregorian Calendar.")
        inceptionYear.set("2024")
        url.set("https://github.com/shivathapaa/Nepali-Date-Picker")

        licenses {
            license {
                name.set("MPL 2.0")
                url.set("https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE")
            }
        }

        developers {
            developer {
                id.set("shivathapaa")
                name.set("Shiva Thapa")
                email.set("query.shivathapaa.dev@gmail.com")
                url.set("https://github.com/shivathapaa/")
            }
        }

        // SCM information
        scm {
            url.set("https://github.com/shivathapaa/Nepali-Date-Picker")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}