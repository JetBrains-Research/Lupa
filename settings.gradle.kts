@file:Suppress("trailing-comma-on-call-site", "trailing-comma-on-declaration-site")

import java.net.URI

rootProject.name = "lupa"

include(
    "lupa-core",
    "lupa-test",
    "lupa-runner",

    "kotlin-analysers",
    "python-analysers"
)

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
    gitRepository(URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
        producesModule("$utilitiesProjectName:plugin-utilities-python")
        producesModule("$utilitiesProjectName:plugin-utilities-test")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://nexus.gluonhq.com/nexus/content/repositories/releases")
    }
}
