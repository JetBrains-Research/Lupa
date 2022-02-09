package org.jetbrains.research.lupa.kotlinAnalysis.util.kotlin

import org.jetbrains.research.lupa.kotlinAnalysis.util.GitRepository
import org.jetbrains.research.lupa.kotlinAnalysis.util.PostgresDatabaseConnection
import java.time.LocalDate

class KotlinTeamDatabaseConnection : PostgresDatabaseConnection("POSTGRES_KOTLIN_DB_CONFIG") {

    fun updateRepoDate(repo: GitRepository) {
        conn ?: return

        repo.username?.let { repo.repositoryName?.let { updateRepoDate(repo.username, repo.repositoryName) } }
    }

    fun updateRepoDate(username: String, repoName: String) {
        conn ?: return

        val currentDate = LocalDate.now()
        val query = conn!!.prepareStatement(
            """
                update ${RepositoriesTable.TABLE_NAME.value}
                set ${RepositoriesTable.ANALYSIS_DATE_COL.value} = ?
                where ${RepositoriesTable.USERNAME_COL.value} = ?
                and ${RepositoriesTable.REPO_NAME_COL.value} = ?"""
        )
        query.setDate(1, java.sql.Date.valueOf(currentDate))
        query.setString(2, username)
        query.setString(3, repoName)
        query.executeUpdate()
    }

    enum class RepositoriesTable(val value: String) {
        TABLE_NAME("kotlin_repositories_updates"),
        USERNAME_COL("username"),
        REPO_NAME_COL("repo_name"),
        ANALYSIS_DATE_COL("last_analysis_date"),
        PULL_DATE_COL("last_pull_date"),
    }
}
