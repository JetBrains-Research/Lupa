package org.jetbrains.research.lupa.kotlinAnalysis.util

import com.intellij.openapi.diagnostic.Logger
import org.postgresql.util.PSQLException
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/**
 * Classes that extend this abstract class are used for database connection.
 */
abstract class DatabaseConnectionBase {
    protected var conn: Connection? = null
    protected val logger: Logger = Logger.getInstance(DatabaseConnectionBase::class.java)
}

/**
 * This class is used for Postgres database connection.
 *
 * @param configEnv name of environmental variable containing path to the database config file.
 * This file should contain host and database name, user and password.
 */
open class PostgresDatabaseConnection(configEnv: String) : DatabaseConnectionBase() {
    init {
        val props = Properties()
        val propertiesFilePath = System.getenv(configEnv)

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
}
