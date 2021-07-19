package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.isAndroidProject

/** Runner for kotlin dependencies analysis. */
object KotlinDependenciesAnalysisRunner : KotlinAnalysisRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-dependencies-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ImportDirectivesAnalysisExecutor(args.outputDir).execute(args.inputDir) { projectPath ->
            ProjectUtil.openOrImport(projectPath, null, true)
                .takeIf { !it.isAndroidProject() }
        }
    }
}
