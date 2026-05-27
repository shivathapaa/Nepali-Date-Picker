import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class MavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.vanniktech.maven.publish")

        extensions.configure<MavenPublishBaseExtension> {
            configure(KotlinMultiplatform(sourcesJar = SourcesJar.Sources()))
            publishToMavenCentral()
            // Sign only when a key is provided so local/CI builds without credentials succeed.
            val hasSigningKey = providers.gradleProperty("signingInMemoryKey").isPresent ||
                providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent
            if (hasSigningKey) {
                signAllPublications()
            }
        }
    }
}
