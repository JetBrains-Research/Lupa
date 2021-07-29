package org.jetbrains.research.ml.kotlinAnalysis

enum class GradleBlock(val simpleName: String?) {
    DEPENDENCIES("dependencies"),
    PLUGINS("plugins"),
    ALL_PROJECTS("allprojects"),
    REPOSITORIES("repositories");

    companion object {
        fun fromSimpleName(simpleName: String) = values()
            .firstOrNull { it.simpleName.equals(simpleName, ignoreCase = true) }

        fun availableKeys() = values().map { it.simpleName }
    }
}
