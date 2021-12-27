package org.jetbrains.research.ml.kotlinAnalysis.util

import java.nio.file.Path
import kotlin.io.path.name

class GitRepository(val path: Path, separator: String = "#") {
    val username: String
    val repositoryName: String

    init {
        val split = path.fileName.name.split(separator)
        username = split[0]
        repositoryName = split[1]
    }
}
