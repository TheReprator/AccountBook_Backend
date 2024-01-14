package dev.reprator.commonFeatureImpl.imp

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.base.action.AppDatabaseFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalKeywordApi

class DefaultDatabaseFactory(dbConfig: HikariConfig, private val callBack: (HikariDataSource, Database) -> Unit) :
    AppDatabaseFactory {

    private val dataSource: HikariDataSource by lazy {
        HikariDataSource(dbConfig)
    }

    override fun connect() {
        val database =  Database.connect(dataSource, databaseConfig = DatabaseConfig {
            @OptIn(ExperimentalKeywordApi::class)
            preserveKeywordCasing = false
        })

        callBack(dataSource, database)
    }

    override fun close() {
        dataSource.close()
    }
}