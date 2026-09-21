import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.composeMultiplatform")
    id("picker.mavenPublish")
    id("picker.dokka")
}

// Structural checks on the iOS export: dumpIosApi, checkIosApi, checkBridgeCoverage.
apply(from = "iosApiVerification.gradle.kts")

// Structural check on the Android embed layer: checkAndroidBridgeCoverage.
apply(from = "androidApiVerification.gradle.kts")

kotlin {
    val xcFrameworkName = "nepali-date-picker"
    val xcf = XCFramework(xcFrameworkName)

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = xcFrameworkName
            binaryOption("bundleId", "io.github.shivathapaa.$xcFrameworkName")
            // Kotlin/Native exports only this module's declarations. Without an
            // explicit export, Swift consumers get the pickers but cannot reach
            // the conversion engine (NepaliDateConverter, NepaliCalendarDefaults),
            // and the core types that leak in through :ui signatures arrive under
            // module-prefixed names. Exporting :core restores the single flat
            // surface iOS consumers had before the 3.0.0 module split.
            export(projects.nepaliDatePicker.core)
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.nepaliDatePicker.core)
        }

        // Internal machinery shared by the iOS ViewController factories and the Android View
        // factories: appearance scheme derivation, event decorators and content measuring.
        // Nothing in it is exported; each platform keeps its own public host surface.
        val embedMain by creating {
            dependsOn(commonMain.get())
        }

        iosMain {
            dependsOn(embedMain)
        }

        // The Android host layer attaches synthetic view-tree owners for hosts without a
        // ComponentActivity, such as a Flutter activity; activity-compose carries the
        // lifecycle and saved-state APIs those owners implement.
        androidMain {
            dependsOn(embedMain)
            dependencies {
                implementation(libs.androidx.activityCompose)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }

        jsMain.dependencies {
            implementation(libs.html.core)
        }
    }
}
