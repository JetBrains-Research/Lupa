package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules

import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependency

/** Interface for [project's module graph][ModulesGraph] visitors. */
interface ModulesGraphVisitor {

    fun visitModule(moduleNode: ModulesGraph.ModuleNode)
}

/**
 * [ModulesGraphVisitor] implementation for gradle dependencies collection.
 *
 * In [visitModule] collector get build gradle files from given module and extracts dependencies from them.
 * Then it calls [visitModule] for all given module's parents and add dependencies from allproject section
 * to given module dependencies.
 * All results are cached, so if [GradleDependenciesCollector] has already visited parent module, result is .
 */
class GradleDependenciesCollector : ModulesGraphVisitor {

    private val moduleNameToGradleDependencies = mutableMapOf<String, MutableSet<BuildGradleDependency>>()

    fun getModuleNameToGradleDependencies() = moduleNameToGradleDependencies.toMap()

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
