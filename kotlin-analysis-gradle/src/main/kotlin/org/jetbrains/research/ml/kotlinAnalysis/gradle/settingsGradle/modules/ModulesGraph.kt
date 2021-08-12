package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules

import com.intellij.openapi.module.Module
import com.intellij.util.containers.addIfNotNull
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules.ModulesGraph.ModuleNode

/**
 * [ModulesGraph] is project's module graph, which is build according to gradle settings files in projects.
 *
 * Vertexes of this graph are [module nodes][ModuleNode] one for each module in project. Edges are created by rule:
 * if module A contains submodules B and C in include section of it's gradle settings file, graph contains edges
 * A -> B and A -> C ([subModules] list of A node contains B's and C's nodes).
 * Also graph has back edges B -> A and C -> A ([parentModules] list of B's and C's node contains A's nodes).
 */
class ModulesGraph(modules: List<Module>) {

    class ModuleNode(
        val module: Module,
        val parentModules: MutableList<ModuleNode> = mutableListOf(),
        val subModules: MutableList<ModuleNode> = mutableListOf()
    ) {
        fun accept(visitor: ModulesGraphVisitor) {
            visitor.visitModule(this)
        }
    }

    private val moduleNodeByName: Map<String, ModuleNode> = modules
        .map { ModuleNode(it) }
        .associateBy { it.module.name }

    init {
        modules.forEach { module ->
            GradleFileManager.extractSettingsGradleFileFromModule(module)
                ?.extractIncludedModuleNames()
                ?.forEach { subModuleName ->
                    addEdge(module.name, subModuleName)
                }
        }
    }

    private fun addEdge(fromModuleName: String, toModuleName: String) {
        val fromModuleNode = moduleNodeByName[fromModuleName]
        val toModuleNode = moduleNodeByName[toModuleName]
        fromModuleNode?.subModules?.addIfNotNull(toModuleNode)
        toModuleNode?.parentModules?.addIfNotNull(toModuleNode)
    }

    fun accept(visitor: ModulesGraphVisitor) {
        moduleNodeByName.values.forEach { it.accept(visitor) }
    }
}
