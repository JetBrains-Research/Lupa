package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle

/**
 * Util class for working with Gradle settings file structure, for example parsing included modules.
 */
class SettingsGradleFileUtil {
    companion object {

        private const val NAME = "[^:\'\",\n]*"
        private const val QUOTES = "[\'\"]"

        /**
         * Regex for included modules in settings.gradle/settings.gradle.kts. For example:
         * include (":submodule_name_1", ":submodule_name_2",...)
         * include (':submodule_name_1', ':submodule_name_2',...)
         * include ('submodule_name_1', 'submodule_name_2',...)
         */
        private val INCLUDED_MODULES_STATEMENT_REGEX =
            "include[(]?(.*)[)]?".toRegex(RegexOption.IGNORE_CASE)

        /**
         * Regex for included modules names in settings.gradle/settings.gradle.kts include section. For example:
         * ":submodule_name_1" -> submodule_name_1
         * ':submodule_name_1' -> submodule_name_1
         * 'submodule_name_1' -> submodule_name_1
         */
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
