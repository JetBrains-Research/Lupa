package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.plugins.groovy.lang.psi.util.childrenOfType
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType

sealed class GradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {
    val GRADLE_DEPENDENCIES_BLOCK_NAME = "dependencies"
    val GRADLE_PLUGINS_BLOCK_NAME = "plugins"

    abstract fun extractBuildGradleDependencyByName(name: String): GradleDependency?
    abstract fun extractBuildGradleDependencies(): List<GradleDependency>
}

class GradleGroovyPsiFile(private val psiFile: GroovyFileImpl) : GradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencyByName(name: String): GradleDependency? {
        return extractBuildGradleDependencies().firstOrNull { it.name == name }
    }

    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        val dependenciesClosableBlock = extractElementsOfType(GrMethodCall::class.java)
            .firstOrNull { it.text.startsWith(GRADLE_DEPENDENCIES_BLOCK_NAME) }
            ?.childrenOfType<GrClosableBlock>()?.firstOrNull()

        return dependenciesClosableBlock?.children?.filterIsInstance<GrApplicationStatement>()
            ?.mapTo(mutableListOf()) { dependency ->
                GradleDependency(dependency.argumentList.text,
                    dependency.callReference?.let { GradleDependencyConfiguration.fromYamlKey(it.methodName) })
            } ?: listOf()
    }
}

class GradleKtxPsiFile(private val psiFile: KtFile) : GradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencyByName(name: String): GradleDependency? {
        return extractBuildGradleDependencies().firstOrNull { it.name == name }
    }

    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        return psiFile.script?.declarations?.map { dependency ->
            GradleDependency(dependency.text, GradleDependencyConfiguration.IMPLEMENTATION)
        } ?: listOf()
    }
}
