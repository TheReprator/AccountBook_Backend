package dev.reprator.dao

import com.zaxxer.hikari.*
import dev.reprator.modals.Language
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*
import java.io.*

object DatabaseFactory {
    fun init(config: ApplicationConfig) {

        val driverClassName = config.property("storage.driverClassName").getString()
        val databaseName = config.property("storage.databaseName").getString()
        val portNumber = config.property("storage.portNumber").getString()
        val serverName = config.property("storage.serverName").getString()

        val jdbcUrl = "jdbc:postgresql://$serverName:$portNumber/$databaseName"
        val database = Database.connect(createHikariDataSource(driverClassName, jdbcUrl))
        transaction(database) {
            SchemaUtils.create(Language)
        }
    }

    private fun createHikariDataSource(
        driverName: String,
        url: String,
    ) = HikariDataSource(HikariConfig().apply {
        jdbcUrl = url
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        driverClassName = driverName
        validate()
    })

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}