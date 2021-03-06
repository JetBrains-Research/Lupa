group = "com.jetbrains.kotlin.analysis.test"
version = "1.0-SNAPSHOT"

plugins {
    java
    apply(plugin = "maven-publish")
    `java-gradle-plugin`
    val kotlinVersion = "1.5.21"
    id("org.jetbrains.intellij") version "1.1.3" apply true
    kotlin("jvm") version kotlinVersion apply true
    kotlin("jvm") version "1.5.21" apply true
    kotlin("jvm") version "1.5.21" apply false
    kotlin("jvm") version "1.5.21"
}

repositories {
    mavenCentral()
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    apply(plugin("org.jetbrains.groovy"))

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath(kotlin("gradle-plugin", "1.5.20"))
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
        implementation("org.apache.commons:commons-math3")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test:runner:1.3.0")
        testCompile("junit", "junit", "4.12")
        testRuntimeOnly(group="junit", name="junit", version="4.12")
        compileOnly("com.google.guava:guava:23.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    }
}
