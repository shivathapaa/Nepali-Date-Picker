import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.mavenPublish")
    id("picker.dokka")
}

kotlin {
    // Backend / native-only targets in addition to the Compose-supported set defined by the
    // `picker.kotlinMultiplatform` convention. These targets are pure kotlinx-datetime + stdlib
    // consumers (server, CLI, embedded, Apple wearables / TV, Apple x86_64 simulators, wasmWasi),
    // and intentionally exclude any Compose runtime dependency.
    linuxX64()
    linuxArm64()
    mingwX64()

    watchosArm64()
    tvosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // Apple simulator targets — declared only when the host is CI or the
    // maintainer opts in. They compile cross-platform fine, but their
    // auto-generated simulator test tasks read Xcode's installed-runtime list
    // at configuration time, which fails on dev laptops that don't have the
    // tvOS / watchOS simulator SDKs installed. Publishing happens under CI
    // (CI=true) so the published artifact still ships every target — only
    // local `:check` / `allTests` paths skip them.
    val isCi = System.getenv("CI") != null
    val optedIn = providers.gradleProperty("enableAppleSimulatorTargets").orNull != null
    if (isCi || optedIn) {
        iosX64()
        watchosSimulatorArm64()
        tvosSimulatorArm64()
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
