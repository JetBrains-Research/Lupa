package org.jetbrains.research.ml.kotlinAnalysis.gradle

/**
 * Util class for working with Gradle file structure, for example parsing dependencies from build files.
 */
class BuildGradleFileUtil {
    companion object {

        private const val NAME = "[^:\'\"]*"
        private const val QUOTES = "[\'\"]"
        private const val SEPARATORS = "[\'\":,]+"

        private val GRADLE_DEPENDENCIES_SHORT_REGEX = "(${
            BuildGradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?$QUOTES($NAME)$SEPARATORS($NAME)($SEPARATORS$NAME)?$QUOTES[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        private val GRADLE_DEPENDENCIES_FULL_REGEX = "(${
            BuildGradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?group=$QUOTES($NAME)$QUOTES,name=$QUOTES($NAME)$QUOTES(,version=$QUOTES$NAME$QUOTES)?[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        private val GRADLE_DEPENDENCIES_KOTLIN_REGEX = "(${
            BuildGradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?kotlin[(]?$QUOTES($NAME)$QUOTES.*[)]?[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        fun parseGradleDependencyParams(gradleDependencyLine: String): Triple<String, String, String>? {
            return gradleDependencyLine.replace("\\s".toRegex(), "")
                .let { dependencyLine ->
                    (GRADLE_DEPENDENCIES_SHORT_REGEX.matchEntire(dependencyLine)
                        ?: GRADLE_DEPENDENCIES_FULL_REGEX.matchEntire(dependencyLine))
                        ?.groups
                        ?.let {
                            val key = it[1]?.value ?: return null
                            val group = it[2]?.value ?: return null
                            val name = it[3]?.value ?: return null
                            Triple(key, group, name)
                        } ?: (GRADLE_DEPENDENCIES_KOTLIN_REGEX.matchEntire(dependencyLine))
                        ?.groups
                        ?.let {
                            val key = it[1]?.value ?: return null
                            val group = "org.jetbrains.kotlin"
                            val name = it[2]?.value ?: return null
                            Triple(key, group, name)
                        }
                }
        }
    }
}
