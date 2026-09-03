import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("picker.kotlinMultiplatform")
    id("picker.mavenPublish")
    id("picker.dokka")
}

kotlin {
    // Compose-free XCFramework for Swift consumers that only need the conversion engine. The `:ui`
    // XCFramework statically embeds this module, so the two frameworks are alternatives, never
    // additive: linking both would duplicate the Kotlin runtime and this module's symbols.
    val xcFrameworkName = "nepali-date-picker-core"
    val xcf = XCFramework(xcFrameworkName)

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = xcFrameworkName
            binaryOption("bundleId", "io.github.shivathapaa.$xcFrameworkName")
            xcf.add(this)
            isStatic = true
        }
    }

    // Emit `.d.ts` alongside the JS library so the npm package (`@nepali-date-picker/core`) ships
    // TypeScript types. Only `@JsExport` declarations (the `js` package wrapper) are described.
    js {
        generateTypeScriptDefinitions()
    }

    // Backend / native-only targets in addition to the Compose-supported set defined by the
    // `picker.kotlinMultiplatform` convention. These targets are pure kotlinx-datetime + stdlib
    // consumers (server, CLI, embedded, Apple wearables / TV, Apple x86_64 simulators, wasmWasi),
    // and intentionally exclude any Compose runtime dependency.
    linuxX64()
    linuxArm64()
    mingwX64()

    watchosArm64()
    tvosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // Apple simulator targets — declared only when the host is CI or the
    // maintainer opts in. They compile cross-platform fine, but their
    // auto-generated simulator test tasks read Xcode's installed-runtime list
    // at configuration time, which fails on dev laptops that don't have the
    // tvOS / watchOS simulator SDKs installed. Publishing happens under CI
    // (CI=true) so the published artifact still ships every target — only
    // local `:check` / `allTests` paths skip them.
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

        // Intermediate source set covering every target that ships Compose runtime. Provides
        // `actual typealias` for the `@OptionalExpectation` annotations declared in commonMain so
        // Compose stability hints survive on Compose targets, while non-Compose targets compile
        // the annotations away.
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

        // The `js` target resolves time zones through the platform `Intl` API (kotlinx-datetime
        // 0.8.0), so it needs no `@js-joda/*` runtime dependency; the published npm package ships
        // with zero dependencies. `wasmJs` still relies on `@js-joda/timezone` for zone data.
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.0"))
        }
    }
}

// Stages the compiled JS library (ESM `.mjs` + generated `.d.ts`) into the npm package directory
// consumed by the `js/` workspace. The package manifest at `js/packages/core/package.json` is
// source-controlled and stable; this task only refreshes the generated `dist/` payload. Kotlin's
// own generated `package.json` is excluded so it never shadows the curated one.
tasks.register<Sync>("packJsCore") {
    group = "publishing"
    description = "Copies the compiled Kotlin/JS library into js/packages/core/dist for npm packaging."
    dependsOn("jsBrowserProductionLibraryDistribution")
    from(layout.buildDirectory.dir("dist/js/productionLibrary")) {
        // Drop Kotlin's own package.json (the curated one is source-controlled) and the source maps,
        // which point at Kotlin sources not shipped to npm and only add weight for JS consumers.
        exclude("package.json")
        exclude("*.mjs.map")
        // The Compose runtime modules ship only because `:core`'s jsMain aliases the @Immutable /
        // @Stable stability annotations to `androidx.compose.runtime` (see composeTargetsMain). Those
        // annotations have no runtime footprint, so the JS entry never imports these files; excluding
        // them keeps the npm package lean without affecting the Kotlin build or `:ui`'s stability.
        exclude("*compose-runtime*.mjs")
        // Strip the now-dangling `sourceMappingURL` comment so tools do not look for the removed maps.
        filter { line -> if (line.startsWith("//# sourceMappingURL=")) "" else line }
    }
    into(rootProject.layout.projectDirectory.dir("js/packages/core/dist"))
}
