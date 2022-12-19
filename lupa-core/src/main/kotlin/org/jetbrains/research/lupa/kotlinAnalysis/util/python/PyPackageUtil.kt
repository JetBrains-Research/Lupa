package org.jetbrains.research.lupa.kotlinAnalysis.util.python

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.jetbrains.python.statistics.modules

/**
 * A set of functions for working with packages in python projects.
 */
object PyPackageUtil {
    private const val INIT_DOT_PY = "__init__.py"

    /**
     * Returns a list of paths to all found packages in the [project].
     * The path starts with the package root and is separated by a dot.
     */
    fun gatherPackageNames(project: Project): Set<String> {
        return ApplicationManager.getApplication().runReadAction<Set<String>> {
            val contentRoots =
                project.modules.flatMap { ModuleRootManager.getInstance(it).contentRoots.toList() }.toSet()
            val packageRoots = contentRoots.flatMap { collectPackageRoots(project, it) }.toSet()
            packageRoots.flatMap { collectPackageNames(project, it) }.toSet()
        }
    }

    /**
     * Checks if the [file] is a package. The package is the directory in which the file `__init__.py` is located.
     */
    private fun isPackage(file: VirtualFile, fileIndex: ProjectFileIndex): Boolean =
        !fileIndex.isExcluded(file) && file.isDirectory && file.findChild(INIT_DOT_PY) != null

    /**
     * Finds the packages that are closest to the [content root][contentRoot] in the [project].
     */
    private fun collectPackageRoots(project: Project, contentRoot: VirtualFile): List<VirtualFile> {
        val packageRoots = mutableListOf<VirtualFile>()

        val fileIndex = ProjectRootManager.getInstance(project).fileIndex
        VfsUtilCore.visitChildrenRecursively(
            contentRoot,
            object : VirtualFileVisitor<Unit>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    return if (isPackage(file, fileIndex) && file != contentRoot) {
                        packageRoots.add(file)
                        false
                    } else {
                        true
                    }
                }
            },
        )

        return packageRoots
    }

    /**
     * Finds the packages that are contained inside the [package root][packageRoot] in the [project].
     */
    private fun collectPackageNames(project: Project, packageRoot: VirtualFile): List<String> {
        val packageNames = mutableListOf<String>()

        val fileIndex = ProjectRootManager.getInstance(project).fileIndex
        VfsUtilCore.visitChildrenRecursively(
            packageRoot,
            object : VirtualFileVisitor<Unit>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    return if (file == packageRoot) {
                        true
                    } else if (isPackage(file, fileIndex)) {
                        VfsUtilCore.getRelativePath(file, packageRoot, '.')
                            ?.let { packageNames.add("${packageRoot.name}.$it") }
                        true
                    } else {
                        false
                    }
                }
            },
        )

        packageNames.add(packageRoot.name)

        return packageNames
    }

    /**
     * Checks if the [fully qualified name][fqName] starts with the [fully qualified name of any package][packageNames].
     */
    fun isFqNameInAnyPackage(fqName: String, packageNames: Iterable<String>): Boolean {
        // Add a dot at the end of the fully qualified name and at the end of the package names.
        // This is necessary to correctly determine whether the fully qualified name is contained in the package or not.
        // For example, if we have a package named 'plot' and the fully qualified name 'plotly',
        // then this name is not contained in this package, and we should return false.
        val importNameWithDot = "$fqName."
        val packageNamesWithDot = packageNames.map { "$it." }
        return packageNamesWithDot.any { packageNameWithDot -> importNameWithDot.startsWith(packageNameWithDot) }
    }
}
