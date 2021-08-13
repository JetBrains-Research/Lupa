package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins

/** Plugin wrapper for plugins, declared in Gradle build files, which holds [pluginId] values and [allprojects] flag.
 *
 * For example:
 * plugins {
 *      java
 *      kotlin("jvm") version "1.5.21" apply true
 *      id("org.jetbrains.intellij") version "1.1.3" apply true
 * }
 * allprojects {
 *     apply {
 *          plugin("java")
 *          plugin("kotlin")
 *          plugin("org.jetbrains.intellij")
 *     }
 * }
 */
data class BuildGradlePlugin(
    val pluginId: String,
    val allProjects: Boolean = false
)
