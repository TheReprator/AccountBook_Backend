package dev.reprator.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.core.util.dbConfiguration.DatabaseConfig
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.language.data.TableLanguage
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DefaultDatabaseFactory(private val dbConfig: DatabaseConfig) : DatabaseFactory {

    private val dataSource: HikariDataSource by lazy {
        val jdbcUrl = "jdbc:postgresql://${dbConfig.serverName}:${dbConfig.port}/${dbConfig.dbName}?user=${dbConfig.userName}&password=${dbConfig.password}"
        createHikariDataSource(dbConfig.driverClass, jdbcUrl)
    }

    override fun connect() {
        val database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(TableLanguage)
        }
    }

    override fun close() {
        dataSource.close()
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