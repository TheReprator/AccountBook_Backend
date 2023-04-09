package dev.reprator.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.core.AppDBConfiguration
import dev.reprator.core.DatabaseFactory
import dev.reprator.language.data.TableLanguage
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DefaultDatabaseFactory(appConfig: AppDBConfiguration) : DatabaseFactory {

    private val dbConfig = appConfig.databaseConfig
    private lateinit var dataSource: HikariDataSource

    override fun connect() {
        dataSource = hikari()
        val database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(TableLanguage)
        }
    }

    override fun close() {
        dataSource.close()
    }

    private fun hikari(): HikariDataSource {
        val jdbcUrl = "jdbc:postgresql://${dbConfig.serverName}:${dbConfig.port}/${dbConfig.dbName}?user=${dbConfig.userName}&password=${dbConfig.password}"
        return createHikariDataSource(dbConfig.driverClass, jdbcUrl)
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
}
