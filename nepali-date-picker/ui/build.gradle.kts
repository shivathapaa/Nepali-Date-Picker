import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.composeMultiplatform")
    id("picker.mavenPublish")
    id("picker.dokka")
}

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
