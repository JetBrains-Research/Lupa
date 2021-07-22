import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.ml.kotlinAnalysis"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.5.21" apply true
    id("org.jetbrains.intellij") version "1.1.3" apply true
    id("org.jetbrains.dokka") version "1.4.30" apply true
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply true
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
        plugin("org.jetbrains.dokka")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(platform("org.jetbrains.kotlin:kotlin-reflect:1.5.10"))

        // Plugin utilities modules
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = "main"
            }
        }
        implementation("$utilitiesProjectName:plugin-utilities-test") {
            version {
                branch = "main"
            }
        }
    }

    intellij {
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
        downloadSources.set(properties("platformDownloadSources").toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    ktlint {
        enableExperimentalRules.set(true)
        filter {
            exclude("**/resources/**")
        }
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "11"
            targetCompatibility = "11"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }
}
