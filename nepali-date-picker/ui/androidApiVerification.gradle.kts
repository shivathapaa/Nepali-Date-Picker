/*
 * Verification for the Android embed layer.
 */

tasks.register("checkAndroidBridgeCoverage") {
    group = "verification"
    description = "Fails when a public composable has no Android View factory."
    val commonMain = layout.projectDirectory.dir("src/commonMain/kotlin").asFile
    val androidMain = layout.projectDirectory.dir("src/androidMain/kotlin").asFile
    doLast {
        require(commonMain.isDirectory && androidMain.isDirectory) {
            "Expected both src/commonMain/kotlin and src/androidMain/kotlin to exist."
        }
        // Only top-level public composables are pickers a View-based caller would want. Anything
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

        val bridge = androidMain.walkTopDown()
            .filter { it.extension == "kt" }
            .joinToString("\n") { it.readText() }

        // The factories call each composable directly, so a call site is the proof of reachability.
        val unreachable = composables.filterNot { Regex("""\n\s+$it\(""").containsMatchIn(bridge) }
        if (unreachable.isNotEmpty()) {
            throw GradleException(
                "These public composables cannot be reached from an Android View host, because " +
                    "no factory in src/androidMain calls them:\n" +
                    unreachable.joinToString("\n") { "  $it" }
            )
        }
        logger.lifecycle("All ${composables.size} public composables are reachable from Android views.")
    }
}
