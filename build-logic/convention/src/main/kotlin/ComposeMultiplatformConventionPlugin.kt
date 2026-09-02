import dev.shivathapaa.nepalidatepicker.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("jetbrainsCompose").get().get().pluginId)
            apply(libs.findPlugin("composeCompiler").get().get().pluginId)
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.get().dependencies {
                    implementation(libs.findLibrary("runtime").get())
                    implementation(libs.findLibrary("foundation").get())
                    implementation(libs.findLibrary("compose-ui").get())
                    implementation(libs.findLibrary("material3").get())
                }
            }
        }

        // Compose compiler stability and recomposability reports.
        //
        // Opt in with `./gradlew <task> -PenableComposeReports`, so ordinary local builds don't
        // pay the extra IO cost. CI can enable the same flag and diff the generated
        // `<module>/build/compose-reports/*-classes.txt` against a checked-in baseline to catch
        // stability regressions during review.
        //
        // The reports label each class stable / runtime / unstable and each composable
        // restartable / skippable / readonly, which is what to inspect when a composable stops
        // being skippable.
        if (providers.gradleProperty("enableComposeReports").isPresent) {
            extensions.configure<ComposeCompilerGradlePluginExtension> {
                val out = layout.buildDirectory.dir("compose-reports")
                reportsDestination.set(out)
                metricsDestination.set(out)
                includeSourceInformation.set(true)
            }
        }
    }
}
