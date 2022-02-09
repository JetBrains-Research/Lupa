package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.depenencies

import org.jetbrains.research.lupa.kotlinAnalysis.util.KotlinConstants

/**
 * Util class for working with Gradle dependencies section, for example parsing dependencies from build files.
 */
class BuildGradleDependencyFileUtil {
    companion object {

        private const val NAME = "[^:{}\'\"]*"
        private const val QUOTES = "[\'\"]"
        private const val SEPARATORS = "[\'\":,]+"

        /**
         * Regex to parse Gradle dependency:
         *
         * build.gradle:
         * implementation "com.google.code.gson:gson:2.8.7" -- standard
         * implementation 'com.google.code.gson:gson:2.8.7' -- single quotes
         * implementation "com.google.code.gson:gson" -- without version
         *
         * build.gradle.kts:
         * implementation("com.google.code.gson:gson:2.8.7") -- standard
         * implementation("com.google.code.gson","gson","2.8.7") -- with comma separation
         * ... same as for build.gradle but in build.gradle.kts notation with brackets ...
         **/
        private val GRADLE_DEPENDENCIES_SHORT_REGEX = "(${
            BuildGradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?$QUOTES($NAME)$SEPARATORS($NAME)(?:$SEPARATORS($NAME))?$QUOTES[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Gradle dependency:
         *
         * build.gradle:
         * implementation group="com.google.code.gson",name="gson",version="2.8.7" -- standard
         * implementation group='com.google.code.gson',name='gson',version='2.8.7' -- single quotes
         * implementation group='com.google.code.gson',name='gson' -- without version
         *
         * build.gradle.kts:
         * implementation(group="com.google.code.gson",name="gson",version="2.8.7") -- standard
         * ... same as for build.gradle but in build.gradle.kts notation with brackets ...
         **/
        private val GRADLE_DEPENDENCIES_FULL_REGEX = "(${
            BuildGradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?group=$QUOTES($NAME)$QUOTES,name=$QUOTES($NAME)$QUOTES(?:,version=$QUOTES($NAME)$QUOTES)?[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex to parse Kotlin specific Gradle dependency, which is use short version for
         * 'org.jetbrains.kotlin' libraries:
         *
         * build.gradle.kts:
         * classpath(kotlin("gradle-plugin", "1.5.20")) ~ classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20"))
         * or classpath(kotlin("gradle-plugin", version = "1.5.20"))
         **/
        private val GRADLE_DEPENDENCIES_KOTLIN_REGEX = "(${
            BuildGradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?kotlin[(]?$QUOTES($NAME)$QUOTES(?:,(?:version=)?$QUOTES($NAME)$QUOTES)?.*[)]?[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        fun parseGradleDependencyParams(gradleDependencyLine: String): BuildGradleDependency? {
            return gradleDependencyLine.replace("\\s".toRegex(), "")
                .let { dependencyLine ->
                    (GRADLE_DEPENDENCIES_SHORT_REGEX.matchEntire(dependencyLine)
                        ?: GRADLE_DEPENDENCIES_FULL_REGEX.matchEntire(dependencyLine))
                        ?.groups
                        ?.let {
                            if (it.size < 4) {
                                return null
                            }
                            val configKey = it[1]?.value ?: return null
                            val groupId = it[2]?.value ?: return null
                            val artifactId = it[3]?.value ?: return null
                            val version = if (it.size > 4) it[4]?.value else null

                            BuildGradleDependency(
                                groupId,
                                artifactId,
                                BuildGradleDependencyConfiguration.fromKey(configKey),
                                version
                            )
                        } ?: (GRADLE_DEPENDENCIES_KOTLIN_REGEX.matchEntire(dependencyLine))
                        ?.groups
                        ?.let {
                            if (it.size < 3) {
                                return null
                            }
                            val configKey = it[1]?.value ?: return null
                            val groupId = KotlinConstants.OGR_JETBRAINS_KOTLIN.value
                            val artifactId = it[2]?.value
                                ?.let { kotlinArtifactId -> "${KotlinConstants.KOTLIN.value}-$kotlinArtifactId" }
                                ?: return null
                            val version = if (it.size > 3) it[3]?.value else null
                            BuildGradleDependency(
                                groupId,
                                artifactId,
                                BuildGradleDependencyConfiguration.fromKey(configKey),
                                version
                            )
                        }
                }
        }
    }
}
