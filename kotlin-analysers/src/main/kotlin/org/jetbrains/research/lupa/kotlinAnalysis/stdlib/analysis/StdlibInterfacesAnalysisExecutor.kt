package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractModules
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.findPsiFilesByExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.lupa.kotlinAnalysis.util.getRelativePath
import org.jetbrains.research.pluginUtilities.util.Extension
import java.nio.file.Path

class StdlibInterfacesAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener,
    filename: String = "stdlib_interfaces_data.csv",
) : AnalysisExecutor(executorHelper, repositoryOpener) {
    private val stdlibInterfacesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf(
            "project_name",
            "interface_path",
            "interface_name",
            "function_name",
            "base_interfaces",
        )
            .joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(stdlibInterfacesDataWriter)

    override val requiredFileExtensions: Set<FileExtension> = emptySet()

    override fun analyse(project: Project) {
        ApplicationManager.getApplication().runReadAction {
            project.extractModules().forEach { module ->
                val files = module.findPsiFilesByExtension(Extension.KT.value)
                files.forEach { psi ->
                    val relativePath = psi.virtualFile.path.getRelativePath(project).toString()
                    psi.extractElementsOfType(KtClass::class.java).forEach {
                        StdlibInterfacesAnalyzer.analyze(it)?.let { analysisResultList ->
                            analysisResultList.forEach { r ->
                                stdlibInterfacesDataWriter.writer.println(
                                    listOf(
                                        project.name,
                                        relativePath,
                                        r.interfaceName,
                                        r.functionName,
                                        r.baseInterfaces.joinToString(";")
                                    ).joinToString(separator = ","),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
