group = rootProject.group
version = rootProject.version

plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0" apply true
}

repositories {
    // Necessary for psiMiner
//    maven(url = "https://dl.bintray.com/egor-bogomolov/astminer")
}

open class KotlinAnalysisCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Input directory with kotlin files
    @get:Input
    val input: String? by project

    init {
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

dependencies {
    implementation(project(":kotlin-analysis-core"))
    implementation(project(":kotlin-analysis-patterns"))
    implementation(project(":kotlin-analysis-dependencies"))
    implementation(project(":kotlin-analysis-statistic"))
//  TODO: psiminer dependency caused an error because of different versions of kotlin and intellij
//    implementation("org.jetbrains.research.psiminer:psiminer") {
//        version {
//            branch = "master"
//        }
//    }
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

tasks {
    register<KotlinAnalysisCliTask>("cli") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "kotlin-analysis",
            input?.let { "--input=$it" }
        )
    }
}
