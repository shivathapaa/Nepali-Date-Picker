plugins {
    `kotlin-dsl`
}

group = "dev.shivathapaa.nepalidatepicker.buildlogic"

dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    compileOnly(libs.plugins.androidKotlinMultiplatformLibrary.toDep())
    compileOnly(libs.plugins.jetbrainsCompose.toDep())
    compileOnly(libs.plugins.composeCompiler.toDep())
    compileOnly(libs.plugins.mavenPublish.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "picker.kotlinMultiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("composeMultiplatform") {
            id = "picker.composeMultiplatform"
            implementationClass = "ComposeMultiplatformConventionPlugin"
        }
        register("mavenPublish") {
            id = "picker.mavenPublish"
            implementationClass = "MavenPublishConventionPlugin"
        }
    }
}
