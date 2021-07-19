package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

/**
 * Dependency declaration configuration.
 */
enum class GradleDependencyConfiguration(
    val yamlKey: String
) {
    KAPT("kapt"),
    COMPILE_ONLY("compileOnly"),
    IMPLEMENTATION("implementation"),
    API("api"),
    TEST_IMPLEMENTATION("testImplementation"),
    TEST_RUNTIME_ONLY("testRuntimeOnly"),
    ANDROID_TEST_IMPLEMENTATION("androidTestImplementation");

    companion object {
        fun fromYamlKey(yamlKey: String) = values().firstOrNull { it.yamlKey == yamlKey }
        fun availableYamlKeys() = values().joinToString { "'${it.yamlKey}'" }
    }
}

/**
 * Dependency for build.gradle files
 */
data class GradleDependency(val name: String, val configuration: GradleDependencyConfiguration?)
