group = rootProject.group
version = rootProject.version

plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0" apply true
}

open class BaseAnalysisCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Name of the analysis runner
    @get:Input
    val runner: String? by project

    // Input directory with kotlin files
    @get:Input
    val input: String? by project

    // Output directory to store indexes and methods data
    @get:Input
    val output: String? by project

    init {
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Djdk.module.illegalAccess.silent=true"
        )
        maxHeapSize = "20g"
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

open class PythonAnalysisCliTask : BaseAnalysisCliTask() {

    // TODO
    @Input
    @Optional
    val venv = objectFactory.property<String>()

    init {
        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Djdk.module.illegalAccess.silent=true"
        )
        maxHeapSize = "20g"
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

dependencies {
    implementation(project(":kotlin-analysis-core"))
    implementation(project(":kotlin-analysis-clones"))
    implementation(project(":kotlin-analysis-dependencies"))
    implementation(project(":kotlin-analysis-statistic"))
    implementation(project(":kotlin-analysis-gradle"))
    implementation(project(":python-analysis-dependencies"))
    implementation(project(":python-analysis-call-expressions"))
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

tasks {
    register<BaseAnalysisCliTask>("cli") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            runner,
            input?.let { "--input=$it" },
            output?.let { "--output=$it" }
        )
    }

    register<PythonAnalysisCliTask>("python-cli") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            runner,
            input?.let { "--input=$it" },
            output?.let { "--output=$it" },
            venv.let { "--venv=$it" }
        )
    }
}
