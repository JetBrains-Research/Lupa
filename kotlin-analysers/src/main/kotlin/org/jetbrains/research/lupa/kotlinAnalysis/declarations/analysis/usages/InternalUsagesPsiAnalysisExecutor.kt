package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNamedDeclaration
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

class InternalUsagesPsiAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener,
    filename: String = "internal_usages.csv"
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

    override fun analyse(project: Project) {
        project.extractModules()
            .forEach { module ->
                val files = module.findPsiFilesByExtension(Extension.KT.value)
                files.forEach { psi ->
                    val relativePath = psi.virtualFile.path.getRelativePath(project).toString()
                    psi.extractElementsOfType(KtNamedDeclaration::class.java).forEach {
                        InternalUsagesPsiAnalyzer.analyze(it)?.let { analysisResultList ->
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
