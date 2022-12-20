package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages.references

import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages.InternalUsagesExtractionTestBase
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.analyzer.InternalUsagesPsiAnalyzer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class InternalUsagesExtractionTest : ParametrizedBaseTest(getResourcesRootPath(::InternalUsagesExtractionTest)),
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
            outExtension = Extension.TXT,
        )
    }

    @Test
    fun testInternalUsageInFile() {
        testInternalUsageInFile(inFile!!, outFile!!, myFixture, KtNamedDeclaration::class.java) {
            InternalUsagesPsiAnalyzer.analyze(it as KtNamedDeclaration)
        }
    }
}
