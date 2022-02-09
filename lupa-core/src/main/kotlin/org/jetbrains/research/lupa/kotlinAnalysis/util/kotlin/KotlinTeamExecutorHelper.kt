package org.jetbrains.research.lupa.kotlinAnalysis.util.kotlin

import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.util.GitRepository

data class KotlinTeamExecutorHelper(val connection: KotlinTeamDatabaseConnection? = null) : ExecutorHelper {
    override fun postExecuteAction(repo: GitRepository) {
        connection?.updateRepoDate(repo)
    }
}
