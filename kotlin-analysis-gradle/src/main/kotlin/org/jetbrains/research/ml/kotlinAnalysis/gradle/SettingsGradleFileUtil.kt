package org.jetbrains.research.ml.kotlinAnalysis.gradle

/**
 * Util class for working with Gradle file structure, for example parsing dependencies from build files.
 */
class SettingsGradleFileUtil {
    companion object {

        private const val NAME = "[^:\'\",\n]*"
        private const val QUOTES = "[\'\"]"

        private val INCLUDED_MODULES_STATEMENT_REGEX =
            "include[(]?(.*)[)]?".toRegex(RegexOption.IGNORE_CASE)

        private val MODULE_NAME_REGEX = "$QUOTES:?($NAME)$QUOTES".toRegex(RegexOption.IGNORE_CASE)

        fun parseIncludedModulesFromString(includedModulesStatementLine: String): List<String> {
            return includedModulesStatementLine.replace("\\s".toRegex(), "")
                .let { modulesStatementLine ->
                    INCLUDED_MODULES_STATEMENT_REGEX.matchEntire(modulesStatementLine)
                        ?.groups?.get(1)?.value
                        ?.let { modules -> MODULE_NAME_REGEX.findAll(modules) }
                        ?.map { it.groupValues[1] }?.toList()
                } ?: listOf()
        }
    }
}
