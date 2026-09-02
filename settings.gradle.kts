enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NepaliDatePickerKmp"

include(":nepali-date-picker:core")
include(":nepali-date-picker:ui")
include(":nepali-date-picker:serialization")

include(":sample:androidApp")
include(":sample:composeApp")