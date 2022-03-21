package org.jetbrains.research.lupa.kotlinAnalysis.util.python.jupyter

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import java.io.File

/**
 * Data class for storing code cells data [codeCells] and name of
 * Jupyter notebook file [name].
 */
data class NotebookData(val codeCells: JsonArray, val name: String)

/**
 * Class used to transform Jupyter notebook to Python script.
 *
 * @property notebookJson a Json representation of Jupyter notebook.
 * @property notebookName name of Jupyter notebook file.
 */
class Notebook (notebookJson: JsonObject, private val notebookName: String) {
    private val data = NotebookData(getCodeCells(notebookJson), notebookName)
    private val scriptNamePostfix = "_ntb2scr"

    private fun filterCellsByType(ntbJSON: JsonObject, cellType: String): JsonArray {
        return Gson().toJsonTree(ntbJSON.getAsJsonArray(NotebookJson.CELLS.key).toList()
            .mapIndexedNotNull { _, cellJson ->
                cellJson as JsonObject
                if (cellJson.get(NotebookJsonCell.TYPE.key).toString() == cellType) cellJson else null
        }).asJsonArray
    }

    private fun getCodeCells(ntbJSON: JsonObject) = filterCellsByType(ntbJSON, NotebookJsonCell.CODE.key)

    /**
     * Create string from source of the cell [cell].
     *
     * @property cell is a Json representation of Notebook cell,
     * which contains source as a JsonArray of lines of the cell.
     */
    private fun transformCellToString(cell: JsonElement) : String {
        var cellString = ""
        for (cellJson in cell.asJsonObject.get(NotebookJsonCell.SOURCE.key).asJsonArray.toList())
            cellString += cellJson.asString

        return cellString
    }

    /**
     * Create string from sources of all cells in notebook.
     */
    private fun transformNotebookToString() : String {
        var notebookString = ""
        for (cellJson in this.data.codeCells.toList())
            notebookString += transformCellToString(cellJson) + "\n"

        return notebookString
    }

    /**
     * Save Notebook as .py Python script.
     */
    fun saveNotebookAsScript(targetDir: String) {
        val scriptFile = File("$targetDir${this.notebookName}${this.scriptNamePostfix}.${FileExtension.PY.value}")
        scriptFile.writeText(transformNotebookToString())
    }

    enum class NotebookJsonCell(val key: String) {
        TYPE("cell_type"),
        CODE("code"),
        SOURCE("source")
    }

    enum class NotebookJson(val key: String) {
        CELLS("cells"),
    }
}
