import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // The Android target is what :sample:androidApp consumes. Without it Gradle falls back to the
    // jvm variant, which packages the desktop entry point and Skiko into the APK.
    androidLibrary {
        namespace = "dev.shivathapaa.nepalidatepicker.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.compose.ui)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(projects.nepaliDatePicker.ui)
            implementation(projects.nepaliDatePicker.serialization)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.icons.core)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.shivathapaa.nepalidatepicker.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NepaliDatePickerSample"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "dev.shivathapaa.nepalidatepicker"
            }
        }
    }
}