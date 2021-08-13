package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins

import org.jetbrains.research.ml.kotlinAnalysis.util.KotlinConstants

/**
 * Util class for working with Gradle plugins section, for example parsing plugins from build files.
 */
class BuildGradlePluginFileUtil {
    companion object {

        private const val NAME = "[^:{}\'\"]*"
        private const val QUOTES = "[\'\"]"

        /**
         * Regex to parse Gradle community plugin:
         *
         * build.gradle:
         * plugins {
         *     id 'com.jfrog.bintray' version '1.8.5'
         * }
         *
         * build.gradle.kts:
         * plugins {
         *     id("com.jfrog.bintray") version "1.8.5"
         * }
         */
        private val GRADLE_COMMUNITY_PLUGIN_REGEX =
            "id[(]?$QUOTES($NAME)$QUOTES[)]?(version$QUOTES$NAME$QUOTES)?(apply$NAME)?"
                .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Gradle community plugin:
         *
         * build.gradle:
         * plugins {
         *     id 'java'
         * }
         *
         * build.gradle.kts:
         * plugins {
         *     java
         * }
         */
        private val GRADLE_CORE_PLUGIN_REGEX = "(?:id)?$QUOTES?($NAME)$QUOTES?"
            .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Groovy Kts plugin inside apply:
         *
         * build.gradle.kts:
         * apply {
         *     plugin("java")
         * }
         */
        private val GRADLE_PLUGIN_KOTLIN_REGEX = "kotlin[(]?$QUOTES($NAME)$QUOTES[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Groovy Kts plugin inside apply:
         *
         * build.gradle.kts:
         * apply {
         *     plugin("java")
         * }
         */
        private val GRADLE_PLUGIN_KTS_REGEX = "[plugin][(]?$QUOTES($NAME)$QUOTES[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Groovy Gradle apply plugin:
         *
         * build.gradle:
         * apply plugin: 'java'
         */
        private val GRADLE_APPLY_BINARY_PLUGIN_GROOVY_REGEX = "applyplugin:$QUOTES($NAME)$QUOTES"
            .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Groovy Kts apply plugin:
         *
         * build.gradle.kts:
         * apply(plugin = "java")
         */
        private val GRADLE_APPLY_BINARY_PLUGIN_KTS_REGEX = "(?:plugin\\(|apply\\(plugin=)$QUOTES($NAME)$QUOTES\\)"
            .toRegex(RegexOption.IGNORE_CASE)

        private val PLUGIN_REGEXES = listOf(
            GRADLE_COMMUNITY_PLUGIN_REGEX,
            GRADLE_CORE_PLUGIN_REGEX,
            GRADLE_PLUGIN_KTS_REGEX,
        )

        private val APPLY_PLUGIN_REGEXES = listOf(
            GRADLE_APPLY_BINARY_PLUGIN_GROOVY_REGEX,
            GRADLE_APPLY_BINARY_PLUGIN_KTS_REGEX
        )

        /** Extracts plugin id from [given code line][gradlePluginLine] if it match one of plugin declaration regexes. */
        fun parseGradlePluginParams(gradlePluginLine: String): String? {
            return gradlePluginLine.replace("\\s".toRegex(), "")
                .let { pluginLine ->
                    GRADLE_PLUGIN_KOTLIN_REGEX.matchEntire(pluginLine)?.groups?.let {
                        if (it.size < 2) {
                            return null
                        }
                        val groupId = it[1]?.value ?: return null
                        "${KotlinConstants.OGR_JETBRAINS_KOTLIN.value}.$groupId"
                    } ?: PLUGIN_REGEXES.mapNotNull { it.matchEntire(pluginLine)?.groups }
                        .firstOrNull { it.size > 1 }
                        ?.let { it[1]?.value }
                }
        }

        /** Extracts plugin id from [given code line][gradlePluginLine] if it match one of apply plugin regexes. */
        fun parseGradleApplyPluginParams(gradlePluginLine: String): String? {
            return gradlePluginLine.replace("\\s".toRegex(), "")
                .let { pluginLine ->
                    APPLY_PLUGIN_REGEXES.mapNotNull { it.matchEntire(pluginLine)?.groups }
                        .firstOrNull { it.size == 2 }
                        ?.let { it[1]?.value }
                }
        }
    }
}
