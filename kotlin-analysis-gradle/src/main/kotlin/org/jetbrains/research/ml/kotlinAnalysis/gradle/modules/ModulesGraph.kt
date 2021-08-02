package org.jetbrains.research.ml.kotlinAnalysis.gradle.modules

import com.intellij.openapi.module.Module
import com.intellij.util.containers.addIfNotNull
import org.jetbrains.research.ml.kotlinAnalysis.gradle.BuildGradleDependency
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager


class GradleDependenciesCollector : ModuleVisitor {

    val moduleNameToGradleDependencies = mutableMapOf<String, MutableSet<BuildGradleDependency>>()

    override fun visitModule(moduleNode: ModulesGraph.ModuleNode) {
        val module = moduleNode.module
        if (moduleNameToGradleDependencies.containsKey(module.name)) {
            return
        }
        moduleNameToGradleDependencies[module.name] = mutableSetOf()
        moduleNameToGradleDependencies[module.name] = GradleFileManager
            .extractBuildGradleFileFromModule(moduleNode.module)
            ?.extractBuildGradleDependencies()?.toMutableSet()
            ?: mutableSetOf()

        moduleNode.parentModules.forEach { parentModule ->
            visitModule(parentModule)
            moduleNameToGradleDependencies[module.name]!!.addAll(
                moduleNameToGradleDependencies[parentModule.module.name]!!.filter { it.allProjects }
            )
        }
    }
}

interface ModuleVisitor {

    fun visitModule(moduleNode: ModulesGraph.ModuleNode)
}

class ModulesGraph(modules: List<Module>) {

    class ModuleNode(
        val module: Module,
        val parentModules: MutableList<ModuleNode> = mutableListOf(),
        val subModules: MutableList<ModuleNode> = mutableListOf()
    ) {
        fun accept(visitor: ModuleVisitor) {
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

    fun accept(visitor: ModuleVisitor) {
        moduleNodeByName.values.forEach { it.accept(visitor) }
    }
}
