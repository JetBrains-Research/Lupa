package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependency
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.analyzers.GroovyBuildGradleDependenciesAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.analyzers.KtsBuildGradleDependenciesAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.BuildGradlePlugin
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.analyzers.GroovyBuildGradlePluginsAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.analyzers.KtsBuildGradlePluginsAnalyzer

/**
 * Wrapper class for build gradle [PsiFile]. Sets the interface for working with build.gradle/build.gradle.kts files
 * content.
 */
sealed class BuildGradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /**
     * Checks that gradle file contains dependency by group id.
     * For example to checks if build.gradle contains "com.android.tools" dependency.
     */
    fun containsDependencyWithGroupId(groupId: String): Boolean {
        return extractBuildGradleDependencies()
            .firstOrNull { it.groupId.contains(groupId) } != null
    }

    /** Extracts all dependencies from gradle file. */
    abstract fun extractBuildGradleDependencies(): Set<BuildGradleDependency>

    /** Extracts all plugins from gradle file. */
    abstract fun extractBuildGradlePlugins(): Set<BuildGradlePlugin>
}

/**
 * Wrap for Gradle build file written on Kotlin. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class BuildGradleKtsPsiFile(psiFile: KtFile) : BuildGradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencies(): Set<BuildGradleDependency> {
        return KtsBuildGradleDependenciesAnalyzer.analyze(this, GradleBlockContext())
    }

    override fun extractBuildGradlePlugins(): Set<BuildGradlePlugin> {
        return KtsBuildGradlePluginsAnalyzer.analyze(this, GradleBlockContext())
    }
}

/**
 * Wrap for Gradle build file written on Groovy. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class BuildGradleGroovyPsiFile(psiFile: GroovyFileImpl) : BuildGradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencies(): Set<BuildGradleDependency> {
        return GroovyBuildGradleDependenciesAnalyzer.analyze(this, GradleBlockContext())
    }

    override fun extractBuildGradlePlugins(): Set<BuildGradlePlugin> {
        return GroovyBuildGradlePluginsAnalyzer.analyze(this, GradleBlockContext())
    }
}
