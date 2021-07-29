package org.jetbrains.research.ml.kotlinAnalysis.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.BuildGradleFileUtil
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleDependency

/** Analyser for Gradle settings included modules extraction form [KtCallExpression]. */
object KtsIncludedModulesAnalyzer :
    PsiAnalyzerWithContextImpl<GradleBlockContext, KtCallExpression, GradleDependency?>
        (KtCallExpression::class.java) {

    override fun analyze(psiElement: KtCallExpression, context: GradleBlockContext): GradleDependency? {
        return if (context.blocksStack.contains(GradleBlock.DEPENDENCIES)) {
            BuildGradleFileUtil.parseGradleDependencyFromString(psiElement.text)
        } else {
            null
        }
    }
}

/** */
object KtsGradleDependenciesAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, GradleDependency?, List<GradleDependency>>(
        listOf(KtsGradleBlockContextController),
        listOf(KtsBuildGradleDependencyAnalyzer),
        GradleDependenciesAggregator
    )
