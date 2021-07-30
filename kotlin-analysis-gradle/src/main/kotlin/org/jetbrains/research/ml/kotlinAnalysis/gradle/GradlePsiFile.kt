package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl

/**
 * Wrapper class for gradle [PsiFile]. Sets the interface for working with build.gradle/build.gradle.kts files
 * content.
 */
sealed class GradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /**
     * Checks that gradle file contains dependency by group name.
     * For example to checks if build.gradle contains "com.android.tools" dependency.
     * */
    fun containsDependencyWithGroup(group: String): Boolean {
        return extractBuildGradleDependencies()
            .firstOrNull { it.group.contains(group) } != null
    }

    /** Extracts all dependencies from gradle file. */
    abstract fun extractBuildGradleDependencies(): List<GradleDependency>
}

/**
 * Wrap for Gradle file written on Kotlin. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class GradleKtsPsiFile(psiFile: KtFile) : GradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        return KtsGradleDependenciesAnalyzer.analyze(this, GradleBlockContext())
    }
}

/**
 * Wrap for Gradle file written on Groovy. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class GradleGroovyPsiFile(psiFile: GroovyFileImpl) : GradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        return GroovyGradleDependenciesAnalyzer.analyze(this, GradleBlockContext())
    }
}
