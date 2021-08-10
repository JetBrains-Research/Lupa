group = "com.jetbrains.kotlin.analysis.test"
version = "1.0-SNAPSHOT"

plugins {
    java
    kotlin("jvm") version "1.5.21" apply true
}

repositories {
    mavenCentral()
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
        implementation("org.apache.commons:commons-math3:3.6.1")
    }
}
