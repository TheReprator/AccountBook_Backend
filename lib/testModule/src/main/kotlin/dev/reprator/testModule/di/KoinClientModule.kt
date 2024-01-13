package dev.reprator.testModule.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.reprator.base.action.JwtTokenService
import dev.reprator.commonFeatureImpl.di.APP_JWT_TOKEN_ACCESS
import dev.reprator.commonFeatureImpl.di.APP_JWT_TOKEN_REFRESH
import dev.reprator.commonFeatureImpl.di.APP_PLUGIN_CUSTOM_LIST
import dev.reprator.commonFeatureImpl.di.APP_RUNNING_PORT_ADDRESS
import dev.reprator.testModule.AppReflectionTypes
import dev.reprator.testModule.plugin.pluginClientResponseTransformation
import io.ktor.client.plugins.api.*
import io.ktor.server.config.*
import org.h2.tools.RunScript
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

val appTestCoreModule = module {
    single<Int>(named(APP_RUNNING_PORT_ADDRESS)) {
        ApplicationConfig("application-test.conf").property("ktor.deployment.port").getString().toInt()
    }

    single <AppReflectionTypes> {
        AppReflectionTypes()
    }

    single<List<ClientPlugin<Unit>>>(named(APP_PLUGIN_CUSTOM_LIST)) {
        listOf(pluginClientResponseTransformation(getKoin()))
    }
}

val appTestOverrideModule = module {

    single<Long>(named(APP_JWT_TOKEN_ACCESS)) {
        2 * JwtTokenService.SECOND_1
    }

    single<Long>(named(APP_JWT_TOKEN_REFRESH)) {
        3 * JwtTokenService.SECOND_1
    }
}

fun appTestDBModule(callBack: (HikariDataSource, Database) -> Unit)  = module {

    single<HikariConfig> {

        HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            username = "root"
            password = "password"
            jdbcUrl = "jdbc:h2:mem:db${UUID.randomUUID()};MODE=MYSQL"
            maximumPoolSize = 2
            isAutoCommit = true
            validate()
        }
    }

    single<(HikariDataSource, Database) -> Unit> { callBack }
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