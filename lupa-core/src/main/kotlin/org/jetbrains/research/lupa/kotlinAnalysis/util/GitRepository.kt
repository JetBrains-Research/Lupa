package org.jetbrains.research.lupa.kotlinAnalysis.util

import com.intellij.openapi.diagnostic.Logger
import java.nio.file.Path
import kotlin.io.path.name

class GitRepository(val path: Path, separator: String = "#") {
    val username: String?
    val repositoryName: String?
    private val logger: Logger = Logger.getInstance(GitRepository::class.java)

    init {
        val split = path.fileName.name.split(separator)
        if (split.size < 2) {
            logger.info("Couldn't parse username and repository name")
            username = null
            repositoryName = null
        } else {
            username = split[0]
            repositoryName = split[1]
        }
    }
}
