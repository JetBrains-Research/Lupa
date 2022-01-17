package org.jetbrains.research.lupa.kotlinAnalysis.util

import com.intellij.openapi.diagnostic.Logger
import java.nio.file.Path
import kotlin.io.path.name

class GitRepository(val path: Path, separator: String = "#") {
    val username: String?
    val repositoryName: String?
    private val logger: Logger = Logger.getInstance(GitRepository::class.java)

    init {
        val splitPath = path.fileName.name.split(separator)
        if (splitPath.size != 2) {
            logger.warn("Couldn't parse username and repository name")
            username = null
            repositoryName = null
        } else {
            username = splitPath[0]
            repositoryName = splitPath[1]
        }
    }
}
