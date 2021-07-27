package org.jetbrains.research.ml.kotlinAnalysis

/**
 * Util class for working with Gradle file structure, for example parsing dependencies from build files.
 */
class GradleFileUtil {
    companion object {

        private val GRADLE_DEPENDENCIES_SHORT_REGEX = "(${
            GradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?[\'\"]([^:]*)[\'\":,]+([^:]*)([\'\":,]+.*)?[\'\"][)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        private val GRADLE_DEPENDENCIES_FULL_REGEX = "(${
            GradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?group=[\'\"]([^:]*)['\"]name=['\"]([^:]*)['\"](version=['\"][^:]*['\"])?[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        private val GRADLE_DEPENDENCIES_KOTLIN_REGEX = "(${
            GradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        })[(]?kotlin[(]?['\"]([^:'\"]*)['\"].*[)]?[)]?"
            .toRegex(RegexOption.IGNORE_CASE)

        fun parseGradleDependencyFromString(gradleDependencyLine: String): GradleDependency? {
            return gradleDependencyLine.replace("\\s".toRegex(), "")
                .let { dependencyLine ->
                    (GRADLE_DEPENDENCIES_SHORT_REGEX.matchEntire(dependencyLine)
                        ?: GRADLE_DEPENDENCIES_FULL_REGEX.matchEntire(dependencyLine))
                        ?.groups?.let {
                            val key = it[1]?.value ?: return null
                            val group = it[2]?.value ?: return null
                            val name = it[3]?.value ?: return null
                            GradleDependency(group, name, GradleDependencyConfiguration.fromKey(key))
                        } ?: (GRADLE_DEPENDENCIES_KOTLIN_REGEX.matchEntire(dependencyLine))
                        ?.groups?.let {
                            val key = it[1]?.value ?: return null
                            val group = "org.jetbrains.kotlin"
                            val name = it[2]?.value ?: return null
                            GradleDependency(group, name, GradleDependencyConfiguration.fromKey(key))
                        }
                }
        }
    }
}
