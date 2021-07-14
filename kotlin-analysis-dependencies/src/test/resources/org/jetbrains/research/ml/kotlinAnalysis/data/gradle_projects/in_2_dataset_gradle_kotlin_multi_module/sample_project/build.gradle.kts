group = "com.jetbrains.kotlin.analysis.test"
version = "1.0-SNAPSHOT"

plugins {
    java
    kotlin("jvm") version "1.4.32" apply true
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
    }
}
