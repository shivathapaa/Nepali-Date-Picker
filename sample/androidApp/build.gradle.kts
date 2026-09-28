import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "dev.shivathapaa.nepalidatepicker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.shivathapaa.nepalidatepicker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionName = providers.gradleProperty("VERSION_NAME").get()
        versionCode = sampleVersionCode(versionName!!)
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Without the release key in the environment the build falls back to the
            // debug key, so a local release build still installs.
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

/**
 * Maps `major.minor.patch` (any pre-release suffix ignored) to `major * 10000 + minor * 100 + patch`,
 * so every library release installs over the previous sample.
 */
private fun sampleVersionCode(versionName: String): Int {
    val (major, minor, patch) = versionName.substringBefore('-').split('.').map(String::toInt)
    return major * 10_000 + minor * 100 + patch
}

dependencies {
    implementation(libs.runtime)
    implementation(libs.compose.ui)
    implementation(libs.foundation)
    implementation(libs.androidx.activityCompose)

    implementation(project(":sample:composeApp")) {
        exclude(group = "org.jetbrains.runtime", module = "jbr-api")
    }
}