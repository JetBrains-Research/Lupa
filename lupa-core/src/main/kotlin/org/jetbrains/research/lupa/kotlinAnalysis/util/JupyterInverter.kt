package org.jetbrains.research.lupa

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

class JupyterInverter(input: String, output: String) {
    val inputDir = input
    val outputDir = output

    class Notebook(notebookJSON: JSONObject, filename: String) {
        var markdownCells: JSONArray
        var codeCells: JSONArray
        val metaData: JSONObject = notebookJSON.get("metadata") as JSONObject
        val ntbName: String = filename

        init {
            this.markdownCells = getMarkdownCells(notebookJSON)
            this.codeCells = getCodeCells(notebookJSON)
        }

        private fun cellToArray(cellJSON: JSONObject): JSONArray {
            val source = cellJSON.get("source")
            return if (source is String) {
                val arr = source.split("\n").toMutableList()
                for (i in 0 until arr.size - 1) {
                    arr[i] = arr[i] + "\n"
                }

                JSONArray(arr)
            } else source as JSONArray
        }

        private fun getCellLength(cellJSON: JSONObject): Int {
            return cellToArray(cellJSON).length()
        }

        private fun getCellsByType(ntbJSON: JSONObject, cellType: String): JSONArray {
            val resultedArray: ArrayList<JSONObject> = arrayListOf()
            val cellArray: JSONArray = ntbJSON.get("cells") as JSONArray
            var startLine = 0

            for (i in 0 until cellArray.length()) {
                val cell = cellArray.getJSONObject(i)

                cell.put("cellNumber", i)
                if (cell.get("cell_type") == "code") {
                    cell.put("startLine", startLine)
                    startLine += getCellLength(cell)
                }

                if (cell.get("cell_type") == cellType) resultedArray.add(cell)
            }

            return JSONArray(resultedArray)
        }

        private fun getMarkdownCells(ntbJSON: JSONObject): JSONArray {
            return getCellsByType(ntbJSON, "markdown")
        }

        private fun getCodeCells(ntbJSON: JSONObject): JSONArray {
            return getCellsByType(ntbJSON, "code")
        }

        fun toScriptString(): String {
            var resultString = ""
            for (i in 0 until this.codeCells.length()) {
                val cell = this.codeCells.getJSONObject(i)
                val tmpJSON: JSONArray = cellToArray(cell)

                if (tmpJSON.length() > 0) {
                    for (j in 0 until tmpJSON.length()) resultString += tmpJSON.get(j)
                }
                resultString = resultString.trim()
                resultString += "\n"
            }

            return resultString
        }

        fun saveAsPyScript(targetDir: String) {
            val scriptString = toScriptString()
            val extraFlag = "_ntb2scr"
            val extension = ".py"

            val fileName = targetDir + this.ntbName + extraFlag + extension
            val scriptFile = File(fileName)

            scriptFile.printWriter().use { out -> out.print(scriptString) }
        }
    }

    fun readJsonFromString(jsonString: String): JSONObject {
        return JSONObject(jsonString)
    }

    fun readFileAsText(filename: String): String {
        return File(filename).readText()
    }

    fun getFilenamesInFolder(folderName: String): ArrayList<String> {
        val filenames: ArrayList<String> = arrayListOf()

        File(folderName).walk().forEach {
            if (it.name != folderName) filenames.add(folderName + '/' + it.name)
        }

        return filenames
    }

    fun copyDataset(datasetDir: String, targetDir: String) {
        try {
            val path = Paths.get(targetDir)
            Files.createDirectory(path)
        } catch (e: IOException) {
            System.err.println("Directory already exists ")
        } finally {
            File(datasetDir).copyRecursively(File(targetDir), overwrite = true)
        }
    }

    fun replaceNotebooksInDataset(datasetDir: String) {
        File(datasetDir).walkBottomUp().forEach {
            if (it.extension == "ipynb") {
                val targetDir = it.toString().substringBefore(it.name)
                val filename = it.toString()
                val notebookJSON = readJsonFromString(readFileAsText(filename))
                val ntb = Notebook(notebookJSON, it.name)

                ntb.saveAsPyScript(targetDir)
            }
        }
    }

    fun filter() {
        copyDataset(this.inputDir, this.outputDir)
        replaceNotebooksInDataset(this.outputDir)
    }
}
