import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import dev.shivathapaa.nepalidatepicker.convention.configureKotlinMultiplatform
import dev.shivathapaa.nepalidatepicker.convention.configureKotlinMultiplatformAndroidLibrary
import dev.shivathapaa.nepalidatepicker.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            // android.kotlin.multiplatform.library auto-adds the `androidLibrary` KMP target;
            // we configure namespace/compileSdk/minSdk via the AndroidComponentsExtension.
            apply(libs.findPlugin("androidKotlinMultiplatformLibrary").get().get().pluginId)
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        }

        extensions.configure<KotlinMultiplatformExtension>(::configureKotlinMultiplatform)
        extensions.configure<KotlinMultiplatformAndroidComponentsExtension>(
            ::configureKotlinMultiplatformAndroidLibrary
        )
    }
}
