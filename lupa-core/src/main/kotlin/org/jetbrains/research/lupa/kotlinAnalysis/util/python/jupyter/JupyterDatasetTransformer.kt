package org.jetbrains.research.lupa.kotlinAnalysis.util.python.jupyter

import java.io.File
import java.nio.file.Path
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
// import org.jetbrains.research.lupa.kotlinAnalysis.util.getFilesWithExtensions

/**
 * Class used to create copy of initial dataset, where jupyter notebooks
 * will be replaced with python scripts.
 *
 * @property inputPath path to initial dataset directory.
 * @property inputPath path to transformed dataset directory.
 */
class JupyterDatasetTransformer(private val inputPath: Path, private val outputPath: Path) {

    /**
     * Copying dataset from initial dataset directory to transformed dataset directory.
     * Replaces notebooks with scripts in transformed dataset directory.
     */
    fun transformDataset() {
        copyDataset(this.inputPath, this.outputPath)
        replaceNotebooksWithScripts(this.outputPath)
    }

    /**
     * Created Python scripts for all Jupyter notebooks in dataset.
     */
    private fun replaceNotebooksWithScripts(datasetPath: Path) {
        File(datasetPath.toString()).walk().forEach {
            if (it.extension == FileExtension.IPYNB.value) {
                val file = File(it.toString())
                val filename = file.toString()
                val targetDir = filename.substringBefore(file.name)

                val notebookJSON: JsonObject = JsonParser.parseString(readFileAsText(filename)).asJsonObject
                val notebookName = file.nameWithoutExtension
                val ntb = Notebook(notebookJSON, notebookName)
                ntb.saveNotebookAsScript(targetDir)
            }
        }
    }
}
