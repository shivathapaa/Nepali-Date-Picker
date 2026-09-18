plugins {
    // Declared apply false so plugin classes land on every subproject's classpath
    // exactly once. Convention plugins under :build-logic apply them where needed.
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.jetbrainsCompose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.mavenPublish).apply(false)
    alias(libs.plugins.dokka).apply(false)
}

// Dokka multi-module aggregation. The root project is the aggregator: it applies Dokka and pulls
// each documented module in through the `dokka` configuration, producing one combined HTML site at
// `build/dokka/html`. Per-module settings (source links, visibility, kotlinx cross-links) live in
// the `picker.dokka` convention plugin; only the site-wide identity is set here.
//
// `apply false` above keeps Dokka's classes on every subproject's classpath so the convention
// plugin can apply it to the leaves; the root then applies it imperatively to act as aggregator.
apply(plugin = "org.jetbrains.dokka")

dependencies {
    "dokka"(project(":nepali-date-picker:core"))
    "dokka"(project(":nepali-date-picker:ui"))
    "dokka"(project(":nepali-date-picker:serialization"))
}

extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension> {
    moduleName.set("Nepali Date Picker")
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
 *
 * Covers all three published modules. `:ui`'s Compose tests are listed only on the
 * targets that can host them: `jsNodeTest` has no Skiko bindings under plain Node,
 * and this module's Android host tests run without Robolectric, so
 * `android.os.Build` is never populated. Both are environment limits, not gaps in
 * the suite - the same tests pass on JVM, Apple native and macOS.
 */
tasks.register("checkLocal") {
    group = "verification"
    description = "Runs JVM + Android host + Apple native + JS/Wasm node tests across every module (no browser, no Chrome required)."
    dependsOn(
        ":nepali-date-picker:core:jvmTest",
        ":nepali-date-picker:core:testAndroidHostTest",
        ":nepali-date-picker:core:iosSimulatorArm64Test",
        ":nepali-date-picker:core:macosArm64Test",
        ":nepali-date-picker:core:jsNodeTest",
        ":nepali-date-picker:core:wasmJsNodeTest",
        ":nepali-date-picker:core:wasmWasiNodeTest",
        ":nepali-date-picker:serialization:jvmTest",
        ":nepali-date-picker:serialization:testAndroidHostTest",
        ":nepali-date-picker:serialization:iosSimulatorArm64Test",
        ":nepali-date-picker:serialization:macosArm64Test",
        ":nepali-date-picker:serialization:jsNodeTest",
        ":nepali-date-picker:serialization:wasmJsNodeTest",
        ":nepali-date-picker:serialization:wasmWasiNodeTest",
        ":nepali-date-picker:ui:jvmTest",
        ":nepali-date-picker:ui:iosSimulatorArm64Test",
        ":nepali-date-picker:ui:macosArm64Test",
        ":nepali-date-picker:ui:checkBridgeCoverage"
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
              ← Compose Multiplatform (runtime, foundation, ui, material3)
            :sample:composeApp
              ← :nepali-date-picker:ui
            :sample:androidApp
              ← :sample:composeApp
            """.trimIndent()
        )
    }
}
