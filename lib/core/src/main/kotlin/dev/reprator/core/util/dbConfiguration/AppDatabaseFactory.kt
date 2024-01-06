package dev.reprator.core.util.dbConfiguration

interface DatabaseFactory {
    fun connect()
    fun close()
}


data class DatabaseConfig(
    val driverClass: String,
    val dbName: String,
    val port: Int,
    val serverName: String,
    val userName: String,
    val password: String
)