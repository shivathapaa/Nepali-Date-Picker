import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.mavenPublish")
}

kotlin {
    // Backend / native-only targets in addition to the Compose-supported set defined by the
    // `picker.kotlinMultiplatform` convention. These targets are pure kotlinx-datetime + stdlib
    // consumers (server, CLI, embedded, Apple wearables / TV, Apple x86_64 simulators, wasmWasi),
    // and intentionally exclude any Compose runtime dependency.
    linuxX64()
    linuxArm64()
    mingwX64()

    iosX64()
    macosX64()

    watchosArm64()
    watchosSimulatorArm64()
    watchosX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }

        // Intermediate source set covering every target that ships Compose runtime. Provides
        // `actual typealias` for the `@OptionalExpectation` annotations declared in commonMain so
        // Compose stability hints survive on Compose targets, while non-Compose targets compile
        // the annotations away.
        val composeTargetsMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.runtime)
            }
        }

        listOf(
            androidMain,
            jvmMain,
            iosArm64Main,
            iosSimulatorArm64Main,
            macosArm64Main,
            jsMain,
            wasmJsMain,
        ).forEach { sourceSet ->
            sourceSet.get().dependsOn(composeTargetsMain)
        }

        jsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.0"))
        }

        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.0"))
        }
    }
}
