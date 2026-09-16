/*
 * Verification for the iOS export surface.
 */

val headerFile = layout.buildDirectory.file(
    "XCFrameworks/release/nepali_date_picker.xcframework/ios-arm64/" +
        "nepali_date_picker.framework/Headers/nepali_date_picker.h"
)
val apiFile = layout.projectDirectory.file("api/ios.api")

/**
 * Every Swift-visible name the framework exports, sorted, one per line.
 */
fun readExportedNames(): List<String> {
    val header = headerFile.get().asFile
    require(header.exists()) {
        "Header not found at ${header.path}. Run assembleNepali-date-pickerReleaseXCFramework first."
    }
    val swiftName = Regex("""swift_name\("([^"]+)"\)""")
    return swiftName.findAll(header.readText())
        .map { it.groupValues[1] }
        .filterNot { it.startsWith("Kotlin") }
        .distinct()
        .sorted()
        .toList()
}

tasks.register("dumpIosApi") {
    group = "verification"
    description = "Writes the framework's Swift-visible surface to api/ios.api."
    dependsOn("assembleNepali-date-pickerReleaseXCFramework")
    doLast {
        val target = apiFile.asFile
        target.parentFile.mkdirs()
        target.writeText(readExportedNames().joinToString("\n", postfix = "\n"))
        logger.lifecycle("Wrote ${readExportedNames().size} names to ${target.path}")
    }
}

tasks.register("checkIosApi") {
    group = "verification"
    description = "Fails when the framework's Swift-visible surface drifts from api/ios.api."
    dependsOn("assembleNepali-date-pickerReleaseXCFramework")
    doLast {
        val expectedFile = apiFile.asFile
        require(expectedFile.exists()) { "Missing ${expectedFile.path}. Run dumpIosApi." }
        val expected = expectedFile.readLines().filter { it.isNotBlank() }
        val actual = readExportedNames()

        val removed = expected - actual.toSet()
        val added = actual - expected.toSet()
        if (removed.isNotEmpty() || added.isNotEmpty()) {
            val report = buildString {
                appendLine("iOS export surface changed.")
                removed.forEach { appendLine("  removed: $it") }
                added.forEach { appendLine("  added:   $it") }
                appendLine("Run dumpIosApi and commit api/ios.api if this is intended.")
            }
            throw GradleException(report)
        }
        logger.lifecycle("iOS export surface matches api/ios.api (${actual.size} names).")
    }
}

tasks.register("checkBridgeCoverage") {
    group = "verification"
    description = "Fails when a public composable has no iOS ViewController factory."
    val commonMain = layout.projectDirectory.dir("src/commonMain/kotlin").asFile
    val iosMain = layout.projectDirectory.dir("src/iosMain/kotlin").asFile
    doLast {
        require(commonMain.isDirectory && iosMain.isDirectory) {
            "Expected both src/commonMain/kotlin and src/iosMain/kotlin to exist."
        }
        // Only top-level public composables are pickers a Swift caller would want. Anything
        // indented sits inside an object (the Defaults slot builders), and anything carrying a
        // visibility modifier is a helper.
        val composables = commonMain.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                val lines = file.readLines()
                lines.mapIndexedNotNull { index, line ->
                    val name = Regex("""^fun (Nepali[A-Za-z]*)\(""").find(line)?.groupValues?.get(1)
                    val annotated = name != null && lines
                        .subList(maxOf(0, index - 12), index)
                        .any { it.trimStart().startsWith("@Composable") }
                    name.takeIf { annotated }
                }
            }
            .distinct()
            .toSortedSet()

        val bridge = iosMain.walkTopDown()
            .filter { it.extension == "kt" }
            .joinToString("\n") { it.readText() }

        // The factories call each composable directly, so a call site is the proof of reachability.
        val unreachable = composables.filterNot { Regex("""\n\s+$it\(""").containsMatchIn(bridge) }
        if (unreachable.isNotEmpty()) {
            throw GradleException(
                "These public composables cannot be reached from Swift, because no factory in " +
                    "src/iosMain calls them:\n" + unreachable.joinToString("\n") { "  $it" }
            )
        }
        logger.lifecycle("All ${composables.size} public composables are reachable from Swift.")
    }
}
