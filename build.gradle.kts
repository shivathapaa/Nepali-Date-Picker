plugins {
    // Declared apply false so plugin classes land on every subproject's classpath
    // exactly once. Convention plugins under :build-logic apply them where needed.
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.jetbrainsCompose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.mavenPublish).apply(false)
}

/** Run check across every subproject in one shot. */
tasks.register("checkAll") {
    group = "verification"
    description = "Runs check across every subproject."
    dependsOn(subprojects.map { "${it.path}:check" })
}

/** Print the module dependency tree for quick inspection. */
tasks.register("moduleGraph") {
    group = "nepali-date-picker"
    description = "Prints the Nepali Date Picker internal module dependency tree."
    doLast {
        println(
            """
            Nepali Date Picker — Module Graph
            ───────────────────────────────────
            :nepali-date-picker:core
              ← (no internal deps; kotlinx-datetime + compose-runtime)
            :nepali-date-picker:ui
              ← :nepali-date-picker:core
              ← Compose Multiplatform (runtime, foundation, ui, material3, material-icons-core)
            :sample:composeApp
              ← :nepali-date-picker:ui
            :sample:androidApp
              ← :sample:composeApp
            """.trimIndent()
        )
    }
}
