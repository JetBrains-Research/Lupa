package org.jetbrains.research.ml.kotlinAnalysis.tagging

/** Tag for project which describes it's content of theme. */
enum class ProjectTag(val value: String?) {
    UNDEFINED("undefined"),
    ANDROID("android"),
    OTHER("other");
}
