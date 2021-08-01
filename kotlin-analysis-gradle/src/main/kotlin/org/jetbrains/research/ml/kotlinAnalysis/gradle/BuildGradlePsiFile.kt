package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers.GroovyBuildGradleDependenciesAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers.KtsBuildGradleDependenciesAnalyzer

/**
 * Wrapper class for build gradle [PsiFile]. Sets the interface for working with build.gradle/build.gradle.kts files
 * content.
 */
sealed class BuildGradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /**
     * Checks that gradle file contains dependency by group name.
     * For example to checks if build.gradle contains "com.android.tools" dependency.
     * */
    fun containsDependencyWithGroup(group: String): Boolean {
        return extractBuildGradleDependencies()
            .firstOrNull { it.group.contains(group) } != null
    }

    /** Extracts all dependencies from gradle file. */
    abstract fun extractBuildGradleDependencies(): List<BuildGradleDependency>
}

/**
 * Wrap for Gradle build file written on Kotlin. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class BuildGradleKtsPsiFile(psiFile: KtFile) : BuildGradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencies(): List<BuildGradleDependency> {
        return KtsBuildGradleDependenciesAnalyzer.analyze(this, GradleBlockContext())
    }
}

/**
 * Wrap for Gradle build file written on Groovy. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class BuildGradleGroovyPsiFile(psiFile: GroovyFileImpl) : BuildGradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencies(): List<BuildGradleDependency> {
        return GroovyBuildGradleDependenciesAnalyzer.analyze(this, GradleBlockContext())
    }
}
