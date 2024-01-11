package impl

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import org.h2.tools.RunScript
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalKeywordApi
import org.koin.core.annotation.Single
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Single
class DatabaseFactoryImpl : DatabaseFactory {

    private lateinit var source: HikariDataSource

    override fun connect() {
        source = hikari()
        SchemaDefinition.createSchema(source)
        Database.connect(source, databaseConfig = DatabaseConfig {
            @OptIn(ExperimentalKeywordApi::class)
            preserveKeywordCasing = false
        })
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:db${UUID.randomUUID()}"
        config.username = "root"
        config.password = "password"
        config.maximumPoolSize = 2
        config.isAutoCommit = false
        config.validate()
        return HikariDataSource(config)
    }

    override fun close() {
        source.close()
    }
}

private object SchemaDefinition {

    fun createSchema(dataSource: HikariDataSource) {
        RunScript.execute(
            dataSource.connection, Files.newBufferedReader(
                Paths.get("src/test/resources/db/schema.sql")
            )
        )
    }
}
