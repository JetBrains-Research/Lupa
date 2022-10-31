package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.executor

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.InternalUsagesAnalysisResult
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractModules
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.findPsiFilesByExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.lupa.kotlinAnalysis.util.getRelativePath
import org.jetbrains.research.pluginUtilities.util.Extension
import java.nio.file.Path

abstract class InternalUsagesPsiAnalysisExecutorBase(
    val filename: String,
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener,
) : AnalysisExecutor(executorHelper, repositoryOpener) {

    private val internalUsagesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf(
            "project_name",
            "module_name",
            "declaration",
            "usage",
            "source_set",
            "file_path",
        )
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(internalUsagesDataWriter)

    override val requiredFileExtensions: Set<FileExtension> = emptySet()

    fun analyse(
        project: Project, clazz: Class<out KtElement>,
        analyze: (KtElement) -> List<InternalUsagesAnalysisResult>?
    ) {
        project.extractModules().forEach { module ->
            val files = module.findPsiFilesByExtension(Extension.KT.value)
            files.forEach { psi ->
                val relativePath = psi.virtualFile.path.getRelativePath(project).toString()
                psi.extractElementsOfType(clazz).forEach {
                    analyze(it)?.let { analysisResultList ->
                        analysisResultList.toSet().forEach { r ->
                            internalUsagesDataWriter.writer.println(
                                listOf(
                                    project.name,
                                    r.moduleName,
                                    r.declarationFqName,
                                    r.usageFqName,
                                    r.sourceSet,
                                    relativePath,
                                ).joinToString(separator = ",")
                            )
                        }
                    }
                }
            }
        }
    }
}
