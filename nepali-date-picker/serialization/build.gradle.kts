import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.mavenPublish")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Match :core's expanded target matrix — serialization is pure-Kotlin, no Compose / UI
    // dependency, so it ships everywhere :core ships.
    linuxX64()
    linuxArm64()
    mingwX64()

    watchosArm64()
    tvosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // Apple simulator targets — gated like :core. Local laptops without tvOS / watchOS
    // simulator SDKs would otherwise fail configuration when the auto-generated test tasks
    // read Xcode's runtime list.
    val isCi = System.getenv("CI") != null
    val optedIn = providers.gradleProperty("enableAppleSimulatorTargets").orNull != null
    if (isCi || optedIn) {
        iosX64()
        watchosSimulatorArm64()
        tvosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.nepaliDatePicker.core)
            api(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
