import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.composeMultiplatform")
    id("picker.mavenPublish")
}

kotlin {
    val xcFrameworkName = "nepali-date-picker"
    val xcf = XCFramework(xcFrameworkName)

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = xcFrameworkName
            binaryOption("bundleId", "io.github.shivathapaa.$xcFrameworkName")
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
