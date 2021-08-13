package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies

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
 */
data class BuildGradleDependency(
    val groupId: String,
    val artifactId: String,
    val configuration: BuildGradleDependencyConfiguration?,
    val allProjects: Boolean = false
)
