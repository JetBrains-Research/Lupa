package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.analyzers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.BuildGradlePlugin
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.GradleConstants

/**
 * Internal class to store plugins ids from the allprojects{ ... } section
 */
internal data class GradlePluginFromAllProjectsSection(
    val pluginId: String,
    val isNew: Boolean = false
)

/**
 * Analyzer for gradle plugins which extracts all applied or declared plugins in build.gradle.kts file
 *
 * Currently supports the following notations:
 *  for the plugins{ ... } section:
 *   - java or `java-gradle-plugin`
 *   - apply(plugin = "maven-publish")
 *   - kotlin("jvm") version kotlinVersion apply true
 *   - kotlin("jvm") version "1.5.21"
 *   - id("org.jetbrains.intellij") version "1.1.3" apply true
 *   - kotlin("jvm") version kotlinVersion apply true (without resolving)
 *  for the allprojects{ ... } section:
 *   - apply{ ... }:
 *    - plugin("java")
 *   - apply( ... ):
 *    - plugin("org.jetbrains.groovy")
 */
object KtsBuildGradlePluginsAnalyzer :
    PsiAnalyzer<PsiElement, List<BuildGradlePlugin>> {

    override fun analyze(psiElement: PsiElement): List<BuildGradlePlugin> {
        val plugins = mutableListOf<BuildGradlePlugin>()
        ApplicationManager.getApplication().invokeAndWait {
            val allProjectsPlugins = psiElement.extractAllProjectsPlugins()
            val (newPlugins, existingPlugins) = allProjectsPlugins.partition { it.isNew }
            val existingPluginsIds = existingPlugins.map { it.pluginId }.toSet()
            newPlugins.forEach { plugins.add(BuildGradlePlugin(it.pluginId, allProjects = true)) }

            val pluginsBody = psiElement.findPluginsBody<KtCallExpression, KtBlockExpression>() ?: return@invokeAndWait
            pluginsBody.children.forEach { plugin ->
                when (plugin) {
                    // apply(plugin = "maven-publish")
                    is KtCallExpression -> {
                        val id = plugin.findFirstChild<KtLiteralStringTemplateEntry>()?.text ?: return@forEach
                        plugins.add(BuildGradlePlugin(id, existingPluginsIds))
                    }
                    // java, `java`
                    is KtReferenceExpression -> plugins.add(
                        BuildGradlePlugin(
                            plugin.text.trim('`'),
                            existingPluginsIds
                        )
                    )
                    // kotlin("jvm") version "1.5.21" apply true
                    is KtBinaryExpression -> plugins.add(
                        plugin.parsePluginFromLongNotation(existingPluginsIds) ?: return@forEach
                    )
                }
            }
        }
        return plugins
    }

    /**
     * Parse the olugin information from the long plugin's notation, e.g.
     *  kotlin("jvm") version "1.5.21" apply true
     *
     * @param allProjectsPluginsIds stores the set of plugins ids that should be imported in all project's modules
     */
    private fun PsiElement.parsePluginFromLongNotation(allProjectsPluginsIds: Set<String>): BuildGradlePlugin? {
        // If apply block is not specifies the value is true
        val applied = this.findFirstChild<KtConstantExpression>()?.text?.toBoolean() ?: true
        val otherInfo = this.findFirstChild<KtBinaryExpression>(toIncludeRoot = false)

        val idWithArgs = otherInfo?.findFirstChild<KtCallExpression>() ?: return null
        var id = idWithArgs.findFirstChild<KtNameReferenceExpression>()?.text ?: return null
        val args = idWithArgs.findFirstChild<KtValueArgumentList>()?.children?.mapNotNull {
            it.findFirstChild<KtValueArgument>()?.findFirstChild<KtLiteralStringTemplateEntry>()?.text
        }?.toMutableList() ?: mutableListOf()
        if (id == GradleConstants.ID.key) {
            require(args.isNotEmpty()) { "The plugin ${this.text} does not have specification" }
            id = args.removeFirst()
        }
        // <kotlin("jvm")> - first child, <version> - second child, <"1.5.21"> - third child
        val version = otherInfo.pluginVersion(3)
        return BuildGradlePlugin(id, allProjectsPluginsIds, version, applied, pluginArgs = args.toSet())
    }

    /**
     * Extract all plugins from the allprojects{ ... } section in the build.gradle.kts file
     */
    private fun PsiElement.extractAllProjectsPlugins(): Set<GradlePluginFromAllProjectsSection> {
        val applyNodes =
            this.findAllProjectsBody<KtCallExpression, KtBlockExpression>()
                ?.children?.filter { GradleConstants.APPLY.key in it.text }
                ?: return emptySet()
        return applyNodes.flatMap { extractPlugins(it) }.toSet()
    }

    /**
     * Extract plugins from the apply section in the allprojects{ ... } section in the build.gradle.kts file
     * Handle two cases:
     * apply { ... } and apply()
     *
     * The first one means the plugin was included in the project and just indicates to include
     *  the existing plugin to all modules in the project
     * The second one means the plugin was NOT included in the project and indicates
     *  that the plugin should be included in all modules
     *
     * @param applyNode the apply node (possible two cases)
     */
    private fun extractPlugins(applyNode: PsiElement): List<GradlePluginFromAllProjectsSection> {
        var isNew = false
        //  apply { ... } (old plugin) OR apply() (new plugin)
        val applyBlock =
            applyNode.findFirstChild<KtBlockExpression>() ?: applyNode.findFirstChild<KtValueArgumentList>()?.let {
                isNew = true
                it
            } ?: return emptyList()
        return applyBlock.children.mapNotNull {
            val id = it.findFirstChild<KtLiteralStringTemplateEntry>()?.text ?: return@mapNotNull null
            GradlePluginFromAllProjectsSection(id, isNew)
        }
    }
}
