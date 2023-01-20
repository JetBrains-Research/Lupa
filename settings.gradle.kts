@file:Suppress("trailing-comma-on-call-site", "trailing-comma-on-declaration-site")

rootProject.name = "lupa"

include(
    "lupa-core",
    "lupa-test",
    "lupa-runner",

    "kotlin-analysers",
    "python-analysers"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://nexus.gluonhq.com/nexus/content/repositories/releases")
    }
}
