import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

/**
 * Applies and configures the Dokka Gradle plugin for a documented library module.
 *
 * Emits API reference for public declarations only, wires GitHub source links so every symbol
 * points back at the exact line that defines it, and cross-links kotlinx types. Shared config
 * lives here so `:core`, `:ui`, and `:serialization` document identically; the root project owns
 * the multi-module aggregation that stitches these together into one site.
 */
class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.dokka")

        val moduleRelPath = path.removePrefix(":").replace(':', '/')

        extensions.configure<DokkaExtension> {
            dokkaSourceSets.configureEach {
                documentedVisibilities.set(setOf(VisibilityModifier.Public))
                reportUndocumented.set(false)
                skipEmptyPackages.set(true)

                val moduleDoc = layout.projectDirectory.file("Module.md")
                if (moduleDoc.asFile.exists()) includes.from(moduleDoc)

                sourceLink {
                    localDirectory.set(layout.projectDirectory.dir("src"))
                    remoteUrl.set(
                        URI("https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/$moduleRelPath/src")
                    )
                    remoteLineSuffix.set("#L")
                }

                externalDocumentationLinks.register("kotlinx-datetime") {
                    url.set(URI("https://kotlinlang.org/api/kotlinx-datetime/"))
                }
                externalDocumentationLinks.register("kotlinx-serialization") {
                    url.set(URI("https://kotlinlang.org/api/kotlinx.serialization/"))
                }
            }
        }
    }
}
