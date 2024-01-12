package dev.reprator.testModule.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.commonFeatureImpl.di.APP_PLUGIN_CUSTOM_LIST
import dev.reprator.commonFeatureImpl.di.APP_RUNNING_PORT_ADDRESS
import dev.reprator.testModule.plugin.pluginClientResponseTransformation
import io.ktor.client.plugins.api.*
import io.ktor.server.config.*
import org.h2.tools.RunScript
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


val appTestCoreModule = module {
    single<Int>(named(APP_RUNNING_PORT_ADDRESS)) {
        ApplicationConfig("application-test.conf").property("ktor.deployment.port").getString().toInt()
    }


    factory<List<ClientPlugin<Unit>>>(named(APP_PLUGIN_CUSTOM_LIST)) {
        listOf(pluginClientResponseTransformation(getKoin()))
    }
}


val appTestDBModule = module {
    single<HikariConfig> {

        HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:db${UUID.randomUUID()}"
            username = "root"
            password = "password"
            maximumPoolSize = 2
            isAutoCommit = false
            validate()
        }

        //SchemaDefinition.createSchema(hikariSource)
        //hikariSource
    }
}


object SchemaDefinition {

    fun createSchema(dataSource: HikariDataSource) {
        RunScript.execute(
            dataSource.connection, Files.newBufferedReader(
                Paths.get("src/test/resources/db/schema.sql")
            )
        )
    }
}