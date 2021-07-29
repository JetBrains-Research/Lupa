package org.jetbrains.research.ml.kotlinAnalysis.gradle

/** Dependency declaration configuration. */
enum class GradleDependencyConfiguration(
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

/** Dependency wrapper for gradle files, which holds [group] and [configuration].
 *
 * For example:
 *     dependencies {
 *          classpath "com.android.tools.build:gradle:4.1.1"
 *     }
 * [group] = "com.android.tools.build:gradle:4.1.1"
 * [configuration] = [GradleDependencyConfiguration.CLASSPATH]
 * */
data class GradleDependency(
    val group: String,
    val name: String,
    val configuration: GradleDependencyConfiguration?,
    val allProjects: Boolean = false
) {
    override fun toString(): String = "${configuration?.key ?: "none"} $group:$name"
}
