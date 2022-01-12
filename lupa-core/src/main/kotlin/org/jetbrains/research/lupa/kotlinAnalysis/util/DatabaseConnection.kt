package org.jetbrains.research.lupa.kotlinAnalysis.util

import com.intellij.openapi.diagnostic.Logger
import org.postgresql.util.PSQLException
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDate
import java.util.*

class DatabaseConnection {
    private var conn: Connection? = null
    private val logger: Logger = Logger.getInstance(DatabaseConnection::class.java)

    init {
        val props = Properties()
        val propertiesFilePath = System.getenv("KOTLIN_DB_CONFIG")

        propertiesFilePath?.let {
            props.load(FileInputStream(propertiesFilePath))

            Class.forName("org.postgresql.Driver")
            val url = "jdbc:postgresql://${props.getProperty("host")}/${props.getProperty("database")}"
            try {
                conn = DriverManager.getConnection(url, props.getProperty("user"), props.getProperty("password"))
                logger.info("Connected to database")
            } catch (e: PSQLException) {
                logger.error("Couldn't connect to database")
                e.printStackTrace()
            }
        }
    }

    fun updateRepoDate(repo: GitRepository) {
        if (conn == null) {
            return
        }

        if (repo.username == null || repo.repositoryName == null) {
            return
        }

        updateRepoDate(repo.username, repo.repositoryName)
    }

    fun updateRepoDate(username: String, repoName: String) {
        if (conn == null) {
            return
        }

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
}

enum class RepositoriesTable(val value: String) {
    TABLE_NAME("kotlin_repositories_updates"),
    USERNAME_COL("username"),
    REPO_NAME_COL("repo_name"),
    ANALYSIS_DATE_COL("last_analysis_date"),
    PULL_DATE_COL("last_pull_date"),
}
