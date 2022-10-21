package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages.resolve

import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages.InternalUsagesExtractionTestBase
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages.references.InternalUsagesExtractionTest
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.analyzer.InternalUsagesWithResolvePsiAnalyzer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class InternalUsagesWithResolveExtractionTest :
    ParametrizedBaseTest(getResourcesRootPath(::InternalUsagesWithResolveExtractionTest)),
    InternalUsagesExtractionTestBase {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::InternalUsagesExtractionTest,
            outExtension = Extension.TXT
        )
    }

    @Test
    fun testInternalUsageInFile() {
        testInternalUsageInFile(inFile!!, outFile!!, myFixture, KtNameReferenceExpression::class.java) {
            InternalUsagesWithResolvePsiAnalyzer.analyze(it as KtNameReferenceExpression)
        }
    }
}
