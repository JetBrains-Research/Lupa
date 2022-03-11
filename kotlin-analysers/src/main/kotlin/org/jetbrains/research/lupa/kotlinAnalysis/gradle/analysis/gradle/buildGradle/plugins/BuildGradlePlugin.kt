package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins

/** Plugin wrapper for plugins, declared in Gradle build files.
 *
 * For example:
 * plugins {
 *      java
 *      apply(plugin = "maven-publish")
 *      kotlin("jvm") version "1.5.21" apply true
 *      id("org.jetbrains.intellij") version "1.1.3" apply true
 * }
 * allprojects {
 *     apply {
 *          plugin("java")
 *          plugin("kotlin")
 *     }
 * }
 *
 * Will be parsed into:
 *   BuildGradlePlugin(pluginId=java, version=null, applied=true, pluginArgs=[], allProjects=true)
 *   BuildGradlePlugin(pluginId=apply(plugin = "maven-publish"),
 *                        version=null, applied=true, pluginArgs=[], allProjects=false
 *                     )
 *   BuildGradlePlugin(pluginId=kotlin, version="1.5.21", applied=true, pluginArgs=[jvm], allProjects=true)
 *   BuildGradlePlugin(pluginId=org.jetbrains.intellij, version="1.1.3", applied=true, pluginArgs=[], allProjects=false)
 */
data class BuildGradlePlugin(
    val pluginId: String,
    val version: String? = null,
    val applied: Boolean = true,
    val pluginArgs: Set<String> = emptySet(),
    val allProjects: Boolean = false,
) {
    constructor(
        pluginId: String,
        allProjectsPluginsIds: Set<String>,
        version: String? = null,
        applied: Boolean = true,
        pluginArgs: Set<String> = emptySet()
    ) : this(pluginId, version, applied, pluginArgs, allProjectsPluginsIds.contains(pluginId))
}

// TODO: move into common gradle utils
enum class GradleConstants(val key: String) {
    ID("id"),
    ALL_PROJECTS("allprojects"),
    APPLY("apply"),
    VERSION("version"),
    PLUGINS("plugins")
}
