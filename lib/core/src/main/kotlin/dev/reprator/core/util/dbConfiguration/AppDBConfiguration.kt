package dev.reprator.core.util.dbConfiguration

data class AppDBConfiguration(val databaseConfig: DatabaseConfig)

data class DatabaseConfig(
    val driverClass: String,
    val dbName: String,
    val port: Int,
    val serverName: String,
    val userName: String,
    val password: String
)