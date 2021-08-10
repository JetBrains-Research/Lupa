package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle

/** Dependency declaration configuration. */
enum class BuildGradleDependencyConfiguration(
    val key: String
) {
    KAPT("kapt"),
    API("api"),
    CLASSPATH("classpath"),
    COMPILE_ONLY("compileOnly"),
    RUNTIME_ONLY("runtimeOnly"),
    COMPILE("compile"),
    IMPLEMENTATION("implementation"),
    ANNOTATION_PROCESSOR("annotationProcessor"),
    TEST_IMPLEMENTATION("testImplementation"),
    TEST_RUNTIME_ONLY("testRuntimeOnly"),
    TEST_RUNTIME("testRuntime"),
    TEST_COMPILE("testCompile"),
    ANDROID_TEST_IMPLEMENTATION("androidTestImplementation");

    companion object {
        fun fromKey(yamlKey: String) = values().firstOrNull { it.key.equals(yamlKey, ignoreCase = true) }
        fun availableKeys() = values().map { it.key }
    }
}

/** Dependency wrapper for gradle files, which holds [groupId], [artifactId], [configuration] values
 * and [allprojects] flag.
 *
 * For example:
 * allprojects {
 *     dependencies {
 *          classpath "com.android.tools.build:gradle:4.1.1"
 *     }
 * }
 * [groupId] = "com.android.tools.build"
 * [artifactId] = "gradle"
 * [version] = "4.1.1"
 * [configuration] = ["classpath"][GradleDependencyConfiguration.CLASSPATH]
 * [allProjects] = True (because of inside allprojects block)
 * */
data class GradleDependency(
    val groupId: String,
    val artifactId: String,
    val configuration: GradleDependencyConfiguration?,
    val allProjects: Boolean = false
) {
    override fun toString(): String = "${configuration?.key ?: "none"} $group:$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BuildGradleDependency) return false

        return group == other.group &&
                name == other.name &&
                configuration == other.configuration
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (configuration?.hashCode() ?: 0)
        return result
    }
}
