package dev.reprator.commonFeatureImpl.imp

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.base.action.AppDatabaseFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

class DefaultDatabaseFactory(dbConfig: HikariConfig, private val tableList: Array<Table>) : AppDatabaseFactory {

    private val dataSource: HikariDataSource by lazy {
        HikariDataSource(dbConfig)
    }

    override fun connect() {
        val database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(*tableList)
            //SchemaUtils.create(TableLanguage, TableCountry, TableUserIdentity)
        }
    }

    override fun close() {
        dataSource.close()
    }
}