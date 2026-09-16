import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.mavenPublish")
    id("picker.dokka")
}

kotlin {
    // Compose-free XCFramework for Swift consumers needing only the conversion engine.
    // The :ui XCFramework embeds this module, so they are alternatives; linking both
    // duplicates the Kotlin runtime and this module's symbols.
    val xcFrameworkName = "nepali-date-picker-core"
    val xcf = XCFramework(xcFrameworkName)

    // Only this framework carries a macOS slice. The `:ui` pickers stay iOS-only because Compose
    // Multiplatform exposes no embeddable AppKit host, leaving macOS without an equivalent of the
    // `UIViewController` factories Swift callers use.
    listOf(iosArm64(), iosSimulatorArm64(), macosArm64()).forEach { target ->
        target.binaries.framework {
            baseName = xcFrameworkName
            binaryOption("bundleId", "io.github.shivathapaa.$xcFrameworkName")
            xcf.add(this)
            isStatic = true
        }
    }

    // Emit .d.ts alongside the JS library so @nepali-date-picker/core ships TypeScript types.
    // Only @JsExport declarations are included.
    js {
        generateTypeScriptDefinitions()
    }

    // Native/backend targets beyond the Compose-supported set. These are pure
    // kotlinx-datetime + stdlib consumers and intentionally avoid Compose runtime dependencies.
    linuxX64()
    linuxArm64()
    mingwX64()

    watchosArm64()
    tvosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // Apple simulator targets are enabled only in CI or when explicitly opted into.
    // Their generated test tasks require installed Xcode simulator runtimes, which
    // may be unavailable locally. CI still publishes all targets; only local
    // :check / allTests paths skip these simulators.
    val isCi = System.getenv("CI") != null
    val optedIn = providers.gradleProperty("enableAppleSimulatorTargets").orNull != null
    if (isCi || optedIn) {
        iosX64()
        watchosSimulatorArm64()
        tvosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }

        // Shared by all Compose targets. Provides actual typealias for commonMain's
        // @OptionalExpectation annotations, preserving Compose stability hints while
        // non-Compose targets compile them away.
        val composeTargetsMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.runtime)
            }
        }

        listOf(
            androidMain,
            jvmMain,
            iosArm64Main,
            iosSimulatorArm64Main,
            macosArm64Main,
            jsMain,
            wasmJsMain,
        ).forEach { sourceSet ->
            sourceSet.get().dependsOn(composeTargetsMain)
        }

        // js uses the platform Intl API for time zones, so the published npm package
        // has zero dependencies. wasmJs still relies on @js-joda/timezone for zone data.
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.0"))
        }
    }
}

// Stages the compiled JS library (.mjs + .d.ts) into the npm package's dist/.
// The curated package.json stays source-controlled; Kotlin's generated manifest is excluded.
tasks.register<Sync>("packJsCore") {
    group = "publishing"
    description =
        "Copies the compiled Kotlin/JS library into js/packages/core/dist for npm packaging."
    dependsOn("jsBrowserProductionLibraryDistribution")
    from(layout.buildDirectory.dir("dist/js/productionLibrary")) {
        // Keep the curated package.json and omit source maps.
        exclude("package.json")
        exclude("*.mjs.map")

        // Stability annotations have no runtime footprint; omit Compose runtime modules.
        exclude("*compose-runtime*.mjs")

        // Remove references to the excluded source maps.
        filter { line -> if (line.startsWith("//# sourceMappingURL=")) "" else line }
    }
    into(rootProject.layout.projectDirectory.dir("js/packages/core/dist"))
}
