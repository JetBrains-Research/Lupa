package org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions

import com.jetbrains.python.psi.PyFromImportStatement

private const val RELATIVE_LEVEL_OF_ABSOLUTE_IMPORT = 0

/**
 * Checks if the import is absolute.
 *
 * For example, `from some_package import some_function` is absolute
 * and `from .some_other_package import some_other_function` is not absolute.
 */
fun PyFromImportStatement.isAbsoluteImport(): Boolean = this.relativeLevel == RELATIVE_LEVEL_OF_ABSOLUTE_IMPORT
