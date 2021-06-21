import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.ml.kotlinAnalysis"
version = "1.0"

plugins {
    java
    kotlin("jvm") version "1.4.32" apply true
    id("org.jetbrains.intellij") version "0.7.2" apply true
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

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
    }

    intellij {
        type = "IC"
        version = "2020.3.3"
        downloadSources = false
        setPlugins("java", "Kotlin", "maven", "gradle", "Groovy")
        updateSinceUntilBuild = true
    }

    ktlint {
        enableExperimentalRules.set(true)
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
