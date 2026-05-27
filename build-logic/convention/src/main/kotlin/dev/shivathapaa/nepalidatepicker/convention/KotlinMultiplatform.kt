package dev.shivathapaa.nepalidatepicker.convention

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) =
    extension.apply {
        iosArm64()
        iosSimulatorArm64()

        jvm()
        macosArm64()

        js {
            browser()
            nodejs()
            useEsModules()
            binaries.library()
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            nodejs()
            binaries.library()
        }

        applyDefaultHierarchyTemplate()

        sourceSets.apply {
            commonTest.get().dependencies {
                implementation(libs.findLibrary("kotlin-test").get())
            }
        }
        targets.configureEach {
            compilations.configureEach {
                compileTaskProvider.configure {
                    compilerOptions {
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                    }
                }
            }
        }
    }.also {
        // Skip browser tests unless Chrome is available (via CHROME_BIN) or
        // explicitly opted-in via `-PenableBrowserTests`. Lets `allTests`,
        // `:check`, and IntelliJ's "run all tests" green-light on laptops
        // without Chrome while still running browser tests on CI runners
        // where Chrome is pre-installed.
        tasks.matching { it.name == "jsBrowserTest" || it.name == "wasmJsBrowserTest" }
            .configureEach {
                onlyIf {
                    System.getenv("CHROME_BIN") != null ||
                        project.findProperty("enableBrowserTests") != null
                }
            }

        // Skip the wasmWasi test task: NepaliCalendarModel constructs
        // `TimeZone.of("Asia/Kathmandu")` and Kotlin/Wasm WASI runtime has no
        // tzdata, so every test fails with IllegalTimeZoneException. The
        // wasmWasi target still ships in the published artifact for consumers
        // that only need pure converters that don't touch TZ.
        tasks.matching { it.name == "wasmWasiNodeTest" }
            .configureEach {
                onlyIf { project.findProperty("enableWasmWasiTests") != null }
            }

        // Pin JVM target across JVM + Android compilations so Compose-runtime / kotlinx-datetime
        // line up with the toolchain consumers expect (JDK 11 baseline).
        tasks.withType(KotlinJvmCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        }
    }

internal fun Project.configureKotlinMultiplatformAndroidLibrary(
    extension: KotlinMultiplatformAndroidComponentsExtension,
) {
    extension.finalizeDsl { android ->
        val moduleName = project.path.removePrefix(":").replace(':', '.').replace('-', '_')
        android.namespace = "dev.shivathapaa.nepalidatepicker.$moduleName"
        android.compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        android.minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

        // Opt the android KMP target into running `commonTest` on the JVM host (Robolectric-free).
        // Without this, AGP warns: "The 'commonTest' source directory exists, but android host
        // tests are not enabled" and `commonTest` only executes on jvm / native / js / wasmJs.
        android.withHostTestBuilder { }.configure { }
    }
}
