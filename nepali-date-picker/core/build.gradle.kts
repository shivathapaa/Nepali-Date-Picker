plugins {
    id("picker.kotlinMultiplatform")
    id("picker.mavenPublish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            // @Immutable annotation only; pulls compose-runtime, not Material3 / UI.
            implementation(libs.runtime)
        }

        jsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.0"))
        }

        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.0"))
        }
    }
}
