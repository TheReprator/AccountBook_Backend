package dev.reprator.configuration

import dev.reprator.core.AppDBConfiguration
import dev.reprator.core.DatabaseConfig
import io.ktor.server.config.*

fun ApplicationConfig.setupApplicationConfiguration(): AppDBConfiguration {
    val driverClass = this.property("storage.driverClassName").getString()
    val database = this.property("storage.databaseName").getString()
    val port = this.property("storage.portNumber").getString().toInt()
    val server = this.property("storage.serverName").getString()
    val userName = this.property("storage.userName").getString()
    val password = this.property("storage.password").getString()

    val appConfig = AppDBConfiguration(DatabaseConfig(driverClass, database, port, server, userName, password))
    return appConfig
}