package org.jetbrains.research.ml.kotlinAnalysis.gradle

/**
 * Util class for working with Gradle file structure, for example parsing dependencies from build files.
 */
class SettingsGradleFileUtil {
    companion object {

        private const val NAME = "[^:\'\",\n]*"
        private const val QUOTES = "[\'\"]"
        private const val SEPARATOR = "[,\n]"

        private val INCLUDED_MODULES_STATEMENT_REGEX =
            "include[(]?$[$SEPARATOR?$QUOTES($NAME)$QUOTES]*[)]?"
                .toRegex(RegexOption.IGNORE_CASE)

        private val INCLUDED_MODULES_STATEMENT_REGEX2 =
            "include[(]?((?<=$QUOTES)$NAME(?=$QUOTES))*[)]?".toRegex(RegexOption.IGNORE_CASE)

        fun parseIncludedModulesFromString(includedModulesStatementLine: String): List<String> {
            return includedModulesStatementLine.replace("\\s".toRegex(), "")
                .let { modulesStatementLine ->
                    INCLUDED_MODULES_STATEMENT_REGEX.matchEntire(modulesStatementLine)
                        ?.groups
                        ?.drop(1) // zero is entire match
                        ?.mapNotNull { it?.value }
                } ?: listOf()
        }
    }
}
