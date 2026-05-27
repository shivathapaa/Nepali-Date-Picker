package dev.shivathapaa.nepalidatepicker.convention

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) =
    extension.apply {
        iosArm64()
        iosSimulatorArm64()

        jvm()
        macosArm64()

        js {
            browser()
            nodejs()
            useEsModules()
            binaries.library()
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            nodejs()
            binaries.library()
        }

        applyDefaultHierarchyTemplate()

        sourceSets.apply {
            commonTest.get().dependencies {
                implementation(libs.findLibrary("kotlin-test").get())
            }
        }
    }.also {
        // Pin JVM target across JVM + Android compilations so Compose-runtime / kotlinx-datetime
        // line up with the toolchain consumers expect (JDK 11 baseline).
        tasks.withType(KotlinJvmCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        }
    }

internal fun Project.configureKotlinMultiplatformAndroidLibrary(
    extension: KotlinMultiplatformAndroidComponentsExtension,
) {
    extension.finalizeDsl { android ->
        val moduleName = project.path.removePrefix(":").replace(':', '.')
        android.namespace = "dev.shivathapaa.nepalidatepicker.$moduleName"
        android.compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        android.minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
    }
}
