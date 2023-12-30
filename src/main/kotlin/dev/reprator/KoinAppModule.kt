package dev.reprator

import dev.reprator.core.AppLogger
import dev.reprator.core.DatabaseFactory
import dev.reprator.dao.DefaultDatabaseFactory
import dev.reprator.impl.LoggerImpl
import org.koin.dsl.module

val koinAppModule = module {
    single<DatabaseFactory> { params -> DefaultDatabaseFactory(appConfig = params.get()) }
    single<AppLogger> { LoggerImpl() }
}