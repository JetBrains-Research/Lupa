package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.analyzers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.BuildGradlePlugin
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.GradleConstants

/**
 * Get literal from the [GrCommandArgumentList] psi node
 */
fun PsiElement.getLiteralFromApplicationStatement() = this.findFirstChild<GrCommandArgumentList>()?.getLiteral()

/**
 * Get literal from the psi node
 */
fun PsiElement.getLiteral() = this.findFirstChild<GrLiteral>()?.text?.trim('\'')

/**
 * Analyzer for gradle plugins which extracts all applied or declared plugins in build.gradle file
 *
 * Currently supports the following notations:
 *  for the plugins{ ... } section:
 *   - id 'java'
 *   - id 'org.jetbrains.kotlin.jvm' version '1.5.21'
 *   - id 'org.jetbrains.kotlin.jvm' version '1.5.21' apply true
 *   - id 'org.jetbrains.kotlin.jvm'
 *  for the allprojects{ ... } section:
 *   - apply plugin: 'java'
 *  for the top-level <apply> section:
 *   - apply plugin: 'maven-publish'
 */
object GroovyBuildGradlePluginsAnalyzer :
    PsiAnalyzer<PsiElement, List<BuildGradlePlugin>> {
    override fun analyze(psiElement: PsiElement): List<BuildGradlePlugin> {
        val plugins = mutableListOf<BuildGradlePlugin>()
        ApplicationManager.getApplication().invokeAndWait {
            val existingPluginsIds = psiElement.extractAllProjectsPluginsIds()
            psiElement.extractNewPluginsIds().forEach { plugins.add(BuildGradlePlugin(it, allProjects = false)) }

            val pluginsSectionBody =
                psiElement.findPluginsBody<GrMethodCallExpression, GrClosableBlock>() ?: return@invokeAndWait
            pluginsSectionBody.children.filterIsInstance<GrApplicationStatement>().forEach {
                // If apply block is not specifies the value is true
                val applied = if (GradleConstants.APPLY.key in it.text) {
                    it.getLiteralFromApplicationStatement()?.toBoolean() ?: true
                } else true

                val otherInfo = it.findFirstChild<GrApplicationStatement>(toIncludeRoot = false)

                val id = if (it.text.split(' ').size == 2) {
                    // the <id 'java'> case
                    it.getLiteralFromApplicationStatement()
                } else {
                    // Other cases
                    val idWithArgs =
                        otherInfo?.findFirstChild<GrReferenceExpression> { psi -> GradleConstants.ID.key in psi.text }
                    if (idWithArgs?.text == GradleConstants.ID.key) {
                        // id 'org.jetbrains.kotlin.jvm' version '1.5.21'
                        otherInfo.getLiteral()
                    } else {
                        // id 'org.jetbrains.kotlin.jvm' version '1.5.21' apply true
                        idWithArgs?.getLiteral()
                    }
                } ?: return@forEach

                // <id 'org.jetbrains.kotlin.jvm' version> - first child, <"1.5.24"> - second child
                // The second check is necessary for the <id 'org.jetbrains.kotlin.jvm' version '1.5.24'> case
                val version = otherInfo?.pluginVersion(2) ?: it.pluginVersion(2)

                val plugin = BuildGradlePlugin(id, existingPluginsIds, version, applied)
                plugins.add(plugin)
            }
        }
        return plugins
    }

    /**
     * Extract all plugins from the allprojects{ ... } section in the build.gradle file
     */
    private fun PsiElement.extractAllProjectsPluginsIds(): Set<String> {
        val allProjectsBody = this.findAllProjectsBody<GrMethodCallExpression, GrClosableBlock>()
        return allProjectsBody?.children?.filterIsInstance<GrApplicationStatement>()?.mapNotNull {
            it.getLiteral()
        }?.toSet() ?: emptySet()
    }

    /**
     * Extract all plugins from the top level <apply> section in the build.gradle file
     */
    private fun PsiElement.extractNewPluginsIds() =
        this.children.filter { it.text.trim().startsWith(GradleConstants.APPLY.key) }.mapNotNull {
            it.getLiteral()
        }.toSet()
}
