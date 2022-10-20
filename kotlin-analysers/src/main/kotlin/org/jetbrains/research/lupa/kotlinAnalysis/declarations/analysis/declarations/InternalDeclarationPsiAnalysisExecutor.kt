package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.declarations

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

class InternalDeclarationPsiAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener,
    filename: String = "internal_declarations.csv"
) : AnalysisExecutor(executorHelper, repositoryOpener) {
    private val internalDeclarationsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf(
            "project_name",
            "module_name",
            "declaration",
            "is_expect",
            "source_set",
            "file_path",
        )
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(internalDeclarationsDataWriter)

    override val requiredFileExtensions: Set<FileExtension> = emptySet()

    override fun analyse(project: Project) {
        project.extractModules()
            .forEach { module ->
                val files = module.findPsiFilesByExtension(Extension.KT.value)
                files.forEach { psi ->
                    val relativePath = psi.virtualFile.path.getRelativePath(project).toString()
                    psi.extractElementsOfType(KtNamedDeclaration::class.java).mapNotNull {
                        InternalDeclarationPsiAnalyzer.analyze(it)?.let { analysisResult ->
                            if (!analysisResult.isActual) {
                                internalDeclarationsDataWriter.writer.println(
                                    listOf(
                                        project.name,
                                        analysisResult.moduleName,
                                        analysisResult.fqName,
                                        analysisResult.isExpect,
                                        analysisResult.sourceSet,
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
