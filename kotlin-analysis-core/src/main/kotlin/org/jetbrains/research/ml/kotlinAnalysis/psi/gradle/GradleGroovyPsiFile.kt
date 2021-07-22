package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.GradleDependenciesUtil.Companion.GRADLE_DEPENDENCIES_BLOCK_NAME
import org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.GradleDependenciesUtil.Companion.parseGradleDependencyFromString

/**
 * Wrap for Gradle file written on Groovy. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class GradleGroovyPsiFile(psiFile: GroovyFileImpl) : GradlePsiFile(psiFile) {

    /**
     * In Groovy file dependency is stored inside
     * [dependency][GrClosableBlock] block ->
     * [implementation|api|complile|...][GrApplicationStatement] block ->
     * as its arguments
     */
    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        return extractElementsOfType(GrMethodCall::class.java)
            .firstOrNull { it.text.startsWith(GRADLE_DEPENDENCIES_BLOCK_NAME) }
            ?.extractElementsOfType(GrClosableBlock::class.java)
            ?.firstOrNull()
            ?.extractElementsOfType(GrApplicationStatement::class.java)
            ?.mapNotNullTo(mutableListOf()) { dependency -> parseGradleDependencyFromString(dependency.text) }
            ?: listOf()
    }
}
