package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

/**
 * Util class for working with Gradle dependencies, for example parsing dependencies from build files.
 */
class GradleDependenciesUtil {
    companion object {
        const val GRADLE_DEPENDENCIES_BLOCK_NAME = "dependencies"

        private val GRADLE_DEPENDENCIES_SHORT_REGEX = ".*(${
            GradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        }).*[\'\"](.*)[\'\":,]+(.*)[\'\":,]+(.*)[\'\"].*".toRegex(RegexOption.IGNORE_CASE)

        private val GRADLE_DEPENDENCIES_FULL_REGEX = ".*(${
            GradleDependencyConfiguration.availableKeys().joinToString(separator = "|")
        }).*group\\s+=\\s+[\'\"](.*)['\"].*name\\s+=\\s+['\"](.*)['\"].*".toRegex(RegexOption.IGNORE_CASE)

        fun parseGradleDependencyFromString(gradleDependencyLine: String): GradleDependency? {
            return (GRADLE_DEPENDENCIES_SHORT_REGEX.find(gradleDependencyLine)
                ?: GRADLE_DEPENDENCIES_FULL_REGEX.find(gradleDependencyLine))
                ?.groups?.let {
                    val key = it[1]?.value ?: return@let null
                    val group = it[2]?.value ?: return@let null
                    val name = it[3]?.value ?: return@let null
                    GradleDependency(group, name, GradleDependencyConfiguration.fromKey(key))
                }
        }
    }
}
