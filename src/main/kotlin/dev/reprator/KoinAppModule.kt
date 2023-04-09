package dev.reprator

import dev.reprator.core.DatabaseFactory
import dev.reprator.dao.DefaultDatabaseFactory
import org.koin.dsl.module

val koinAppModule = module {
    single<DatabaseFactory> { params -> DefaultDatabaseFactory(appConfig = params.get()) }
}