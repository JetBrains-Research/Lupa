package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.GradleDependenciesUtil.Companion.GRADLE_DEPENDENCIES_BLOCK_NAME
import org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.GradleDependenciesUtil.Companion.parseGradleDependencyFromString

/**
 * Wrap for Gradle file written on Kotlin. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class GradleKtxPsiFile(private val psiFile: KtFile) : GradlePsiFile(psiFile) {

    /**
     * In Kptlin file dependency is stored inside
     * [dependency][KtCallExpression] block ->
     * [implementation|api|complile|...][KtCallExpression] block ->
     * as its arguments
     */
    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        return psiFile.script
            ?.extractElementsOfType(KtCallExpression::class.java)
            ?.filter {
                (it.calleeExpression as? KtNameReferenceExpression)
                    ?.getReferencedName() == GRADLE_DEPENDENCIES_BLOCK_NAME
            }
            ?.flatMap { it.valueArguments }
            ?.filterIsInstance<KtLambdaArgument>()
            ?.map { it.getLambdaExpression()?.bodyExpression }
            ?.flatMap { it?.children?.toList() ?: listOf() }
            ?.filterIsInstance<KtCallExpression>()
            ?.mapNotNull { parseGradleDependencyFromString(it.text) }
            ?: listOf()
    }
}
