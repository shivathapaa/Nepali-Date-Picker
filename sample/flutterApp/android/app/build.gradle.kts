plugins {
    id("com.android.application")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "dev.shivathapaa.nepalidatepicker.flutter"
    // The nepali-date-picker library compiles against API 37.
    compileSdk = maxOf(flutter.compileSdkVersion, 37)
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        // A leaf of its own, so this sample and the Compose one can sit side by side
        // on a device.
        applicationId = "dev.shivathapaa.nepalidatepicker.flutter"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    packaging {
        jniLibs {
            // Compresses the Flutter engine and AOT code inside the APK
            useLegacyPackaging = true
        }
    }

    signingConfigs {
        sampleReleaseSigning()?.let { signing ->
            create("release") {
                storeFile = file(signing.storePath)
                storePassword = signing.storePassword
                keyAlias = signing.keyAlias
                keyPassword = signing.keyPassword
            }
        }
    }

    buildTypes {
        release {
            // Without the release key in the environment the build falls back to the
            // debug key, so `flutter run --release` still installs locally.
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

/**
 * Release signing read from `SAMPLE_KEYSTORE_PATH`, `SAMPLE_KEYSTORE_PASSWORD`,
 * `SAMPLE_KEY_ALIAS` and `SAMPLE_KEY_PASSWORD`, or null when any of them is unset.
 */
private data class SampleSigning(
    val storePath: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

private fun sampleReleaseSigning(): SampleSigning? {
    fun env(name: String) = providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }
    return SampleSigning(
        storePath = env("SAMPLE_KEYSTORE_PATH") ?: return null,
        storePassword = env("SAMPLE_KEYSTORE_PASSWORD") ?: return null,
        keyAlias = env("SAMPLE_KEY_ALIAS") ?: return null,
        keyPassword = env("SAMPLE_KEY_PASSWORD") ?: return null,
    )
}

flutter {
    source = "../.."
}
