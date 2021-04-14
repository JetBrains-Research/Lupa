import java.net.URI

rootProject.name = "kotlin-analysis"

include(
    "kotlin-analysis-core",
    "kotlin-analysis-plugin",
    "kotlin-analysis-patterns",
    "kotlin-analysis-dependencies",
    "kotlin-analysis-statistic"
)

sourceControl {
    gitRepository(URI.create("https://github.com/JetBrains-Research/psiminer.git")) {
        producesModule("org.jetbrains.research.psiminer:psiminer")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}
