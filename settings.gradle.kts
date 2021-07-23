import java.net.URI

rootProject.name = "kotlin-analysis"

include(
    "kotlin-analysis-core",
    "kotlin-analysis-plugin",
    "kotlin-analysis-clones",
    "kotlin-analysis-dependencies",
    "kotlin-analysis-statistic"
)

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
    gitRepository(URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
        producesModule("$utilitiesProjectName:plugin-utilities-test")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven(url = "https://nexus.gluonhq.com/nexus/content/repositories/releases")
    }
}
