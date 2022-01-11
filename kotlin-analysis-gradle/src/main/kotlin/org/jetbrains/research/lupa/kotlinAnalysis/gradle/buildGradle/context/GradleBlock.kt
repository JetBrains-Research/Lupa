package org.jetbrains.research.lupa.kotlinAnalysis.gradle.buildGradle.context

/** Scope block in gradle file. */
enum class GradleBlock(val simpleName: String) {
    DEPENDENCIES("dependencies"),
    PLUGINS("plugins"),
    ALL_PROJECTS("allprojects"),
    REPOSITORIES("repositories");

    companion object {
        fun fromSimpleName(simpleName: String) = values()
            .firstOrNull { it.simpleName.equals(simpleName, ignoreCase = true) }
    }
}
