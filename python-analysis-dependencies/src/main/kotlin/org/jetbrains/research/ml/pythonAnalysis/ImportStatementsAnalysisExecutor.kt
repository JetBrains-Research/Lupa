package org.jetbrains.research.ml.pythonAnalysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyImportStatement
import com.jetbrains.python.statistics.modules
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractPyElementsOfType
import java.nio.file.Path

/**
 * Executor for import statements analysis which collects full qualified names of all import statements in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportStatementsAnalysisExecutor(outputDir: Path, filename: String = "import_statements_data.csv") :
    AnalysisExecutor() {
    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "import").joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        val importStatements = project.extractPyElementsOfType(PyImportStatement::class.java)
        val fqNames = importStatements.flatMap { ImportStatementPsiAnalyzer.analyze(it) }.toMutableSet()

        val fromImportStatements = project.extractPyElementsOfType(PyFromImportStatement::class.java)
        fqNames.addAll(
            fromImportStatements.flatMap {
                FromImportStatementPsiAnalyzer.analyze(
                    it,
                    ignoreRelativeImports = true,
                )
            }
        )

        val contentRoots = project.modules.flatMap { ModuleRootManager.getInstance(it).contentRoots.toList() }.toSet()
        val packageRoots = contentRoots.flatMap { collectPackageRoots(project, it) }.toSet()
        val packageNames = packageRoots.flatMap { collectPackageNames(project, it) }.toSet()

        fqNames.retainAll { importName -> !isLocalImport(importName, packageNames) }

        fqNames.ifNotEmpty {
            dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
        }
    }

    companion object {
        private const val INIT_DOT_PY = "__init__.py"

        private fun isPackage(file: VirtualFile, fileIndex: ProjectFileIndex): Boolean =
            !fileIndex.isExcluded(file) && file.isDirectory && file.findChild(INIT_DOT_PY) != null

        private fun collectPackageRoots(project: Project, contentRoot: VirtualFile): List<VirtualFile> {
            val packageRoots = mutableListOf<VirtualFile>()

            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            VfsUtilCore.visitChildrenRecursively(contentRoot, object : VirtualFileVisitor<Unit>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    return if (isPackage(file, fileIndex)) {
                        packageRoots.add(file)
                        false
                    } else {
                        true
                    }
                }
            })

            return packageRoots
        }

        private fun collectPackageNames(project: Project, packageRoot: VirtualFile): List<String> {
            val packageNames = mutableListOf<String>()

            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            VfsUtilCore.visitChildrenRecursively(packageRoot, object : VirtualFileVisitor<Unit>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    return if (file == packageRoot) {
                        true
                    } else if (isPackage(file, fileIndex)) {
                        VfsUtilCore.getRelativePath(file, packageRoot, '.')
                            ?.let { packageNames.add("${packageRoot.name}.${it}") }
                        true
                    } else {
                        false
                    }
                }
            })

            packageNames.add(packageRoot.name)

            return packageNames
        }

        private fun isLocalImport(importName: String, packageNames: Set<String>): Boolean {
            val importNameWithDot = "$importName."
            val packageNamesWithDot = packageNames.map { "$it." }
            return packageNamesWithDot.any { packageNameWithDot -> importNameWithDot.startsWith(packageNameWithDot) }
        }
    }
}
