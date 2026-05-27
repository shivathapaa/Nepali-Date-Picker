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

/**
 * Full verification across every leaf subproject. Includes browser tests
 * (`jsBrowserTest`, `wasmJsBrowserTest`) which require Chrome installed on
 * the host - intended for CI runners.
 */
tasks.register("checkAll") {
    group = "verification"
    description = "Runs :check across every leaf subproject (includes browser tests)."
    // `:nepali-date-picker` and `:sample` are grouping containers with no
    // plugins applied, so they never get a `check` task - only descend to
    // leaves that actually carry plugins.
    dependsOn(
        subprojects
            .filter { it.subprojects.isEmpty() }
            .map { "${it.path}:check" }
    )
}

/**
 * Fast verification for local dev - JVM + native + JS/Wasm node, no browser.
 * Mirrors what `_test.yml` runs on the macOS CI leg so behaviour is consistent
 * between laptops without Chrome and the publish gate.
 */
tasks.register("checkLocal") {
    group = "verification"
    description = "Runs JVM + Android host + Apple native + JS/Wasm node tests (no browser, no Chrome required)."
    dependsOn(
        ":nepali-date-picker:core:jvmTest",
        ":nepali-date-picker:core:testAndroidHostTest",
        ":nepali-date-picker:core:iosSimulatorArm64Test",
        ":nepali-date-picker:core:macosArm64Test",
        ":nepali-date-picker:core:jsNodeTest",
        ":nepali-date-picker:core:wasmJsNodeTest"
    )
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
