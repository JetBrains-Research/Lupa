import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.ml.kotlinAnalysis"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.7.21" apply true
    id("org.jetbrains.intellij") version "1.10.0" apply true
    id("org.jetbrains.dokka") version "1.7.20" apply true
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
        maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")
    }

    val utilitiesProjectVersion = "2.0.5"
    val utilitiesProjectId = "org.jetbrains.research"

    dependencies {
        val log4jVersion = "2.19.0"
        // Logging
        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

        // Plugin utilities modules
        implementation("$utilitiesProjectId:plugin-utilities-core:$utilitiesProjectVersion")
        implementation("$utilitiesProjectId:plugin-utilities-test:$utilitiesProjectVersion")
        implementation("$utilitiesProjectId:plugin-utilities-python:$utilitiesProjectVersion")
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
            exclude("**/resources/**", "**/build.gradle.kts")
        }
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
        }
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }
}
